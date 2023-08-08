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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;

class RecorderBody {
    private final AppConfig conf;
    private final AudioFormat format;
    private TargetDataLine line;
    private ByteArrayOutputStream outStream;
    private byte[] byteSignal;
    private final int[] currentIndex;


    public RecorderBody(AppConfig conf, int[] currentIndex) {
        this.conf = conf;
        this.format = conf.format;
        this.currentIndex = currentIndex;
    }


    public byte[] getByteSignal() {
        return this.byteSignal.clone();
    }


    public void startRecording() throws Exception {
        this.outStream = new ByteArrayOutputStream();

        final int baseBufferSize = 10240;
        final int frameSize = format.getFrameSize();
        final int bufferSize = (int) (baseBufferSize / frameSize) * frameSize;

        line = AudioSystem.getTargetDataLine(format);
        line.open(format);
        line.start();
        new Thread(() -> {
            final byte[] buffer = new byte[bufferSize];
            while (line.isOpen()) {
                int count = line.read(buffer, 0, buffer.length);
                if (0 < count) {
                    outStream.write(buffer, 0, count);
                }
            }
        }).start();
    }


    public void stopRecording() {
        if (line != null) {
            line.close();
            this.byteSignal = outStream.toByteArray();
        }
    }


    public void playSignal() {
        final AudioFormat format = this.format;
        final byte[] audio = this.byteSignal;

        try (
            final var ais = new AudioInputStream(
                new ByteArrayInputStream(audio), 
                format, audio.length / format.getFrameSize());
        ) {
            // 再生終了は観測しない (特にする意味がない)
            final Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                null, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public File getSavePath(final int number) {
        return new File(
            conf.saveTo, 
            "corpus_" + String.format("%04d", number) + ".wav"
        );
    }

    public void saveAsWav(){
        final File file = this.getSavePath(this.currentIndex[0] + 1);
        final byte[] audio = this.byteSignal;

        try (
            final AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(audio),
                this.format, audio.length / this.format.getFrameSize()
            )
        ){
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
