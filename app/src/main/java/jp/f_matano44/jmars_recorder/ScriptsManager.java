/*
 * jMARS Recorder
 * Copyright (C) 2023  Fumiyoshi MATANO
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package jp.f_matano44.jmars_recorder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

final class ScriptsManager {
    private final List<String> lines = readFile();
    public final int minOfIndex = 0;
    public final int maxOfIndex = lines.size() - 1;
    public final int minOfLabel = 1;
    public final int maxOfLabel = lines.size();

    public final int nextLine(final int currentIndex) {
        final int nextIndex = currentIndex + 1;
        return Math.min(nextIndex, maxOfIndex);
    }

    public final String getScriptText(final int currentIndex) {
        return lines.get(currentIndex);
    }

    private static final List<String> readFile() {
        final List<String> lines = new ArrayList<>();

        try (
            final Scanner sc = new Scanner(
                new FileInputStream(AppConfig.script),
                StandardCharsets.UTF_8.name())
            ) {
            while (sc.hasNextLine()) {
                final String str = sc.nextLine();
                if (!str.isEmpty()) {
                    lines.add(str
                        .replace(":", System.lineSeparator())
                        .replace(",", System.lineSeparator())
                    );
                }
            }
        } catch (IOException e) {
            // StackTrace to String
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            final String stacktrace = sw.toString();

            lines.clear();
            lines.add(
                "Error: Can't read script file.\n"
                + "\n"
                + stacktrace
            );
        }

        if (lines.isEmpty()) {
            lines.clear();
            lines.add("Error: Script file is empty.");
        }

        return lines;
    }
}
