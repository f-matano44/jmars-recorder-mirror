/*
 * mRecCorpus2
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

package jp.f_matano44.mreccorpus2;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JPanel;


final class ScriptsManager extends JPanel {
    private final int[] currentIndex;
    private final List<String> lines;

    public ScriptsManager(AppConfig conf, int[] currentIndex) {
        this.currentIndex = currentIndex;
        this.lines = new ArrayList<>();
        this.readFile(conf.script.getAbsolutePath());
    }

    public final void nextLine() {
        currentIndex[0]++;
        if (lines.size() <= currentIndex[0]) {
            currentIndex[0] = 0;
        }
    }

    public final void prevLine() {
        if (currentIndex[0] <= 0) {
            currentIndex[0] = lines.size();
        }
        currentIndex[0]--;
    }

    public final String getScriptText() {
        return lines.get(currentIndex[0]);
    }

    public final int getScriptSize() {
        return lines.size();
    }

    private final void readFile(final String filePath) {
        try (final var sc = new Scanner(new File(filePath))) {
            while (sc.hasNextLine()) {
                final String[] parts = sc.nextLine().split("[\t,:]");
                final StringBuilder sb = new StringBuilder();
                for (final String temp : parts) {
                    sb.append(temp + "\n");
                }
                lines.add(sb.toString());
            }
        } catch (IOException e) {
            lines.clear();
            lines.add("Error: Can't read script file.");
        }

        if (lines.isEmpty()) {
            lines.clear();
            lines.add("Error: Can't read script file.");
        }
    }
}
