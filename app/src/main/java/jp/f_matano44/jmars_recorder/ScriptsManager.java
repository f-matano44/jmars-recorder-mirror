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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

final class ScriptsManager {
    private final List<String> lines = readFile();
    private final int maxOfIndex = lines.size() - 1;

    public final int nextLine(final int currentIndex) {
        final int nextIndex = currentIndex + 1;
        return Math.min(nextIndex, maxOfIndex);
    }

    public final String getScriptText(final int currentIndex) {
        return lines.get(currentIndex);
    }

    public final int getScriptSize() {
        return lines.size();
    }

    private static final List<String> readFile() {
        List<String> lines = new ArrayList<>();

        try (final var sc = new Scanner(AppConfig.script, StandardCharsets.UTF_8)) {
            final String splitChars = "[\t,:]";
            while (sc.hasNextLine()) {
                final String[] parts = sc.nextLine().split(splitChars);
                final StringBuilder sb = new StringBuilder();
                for (final String temp : parts) {
                    Util.appendLn(sb, temp);
                }
                lines.add(sb.toString());
            }
        } catch (IOException e) {
            // StackTrace to String
            final var sw = new StringWriter();
            final var pw = new PrintWriter(sw);
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
