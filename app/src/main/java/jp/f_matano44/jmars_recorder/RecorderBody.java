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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;
import jp.f_matano44.jfloatwavio.Converter;

final class RecorderBody implements Cloneable {
    /* Constant */
    private static final byte[] defaultSignal = new byte[0];
    private static final double trimmingThreshold_db = -15.0;
    private static final double trimmingMargin_s = 0.2;
    /* for recording process */
    private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private final TargetDataLine line;
    private static boolean recording = false;
    /* data */
    private byte[] byteSignal = defaultSignal;

    public RecorderBody() {
        TargetDataLine line = null;
        try {
            line = AudioSystem.getTargetDataLine(AppConfig.format);
        } catch (final Exception e) {
            e.printStackTrace(AppConfig.logTargetStream);
            JOptionPane.showMessageDialog(
                null, 
                "DataLine couldn't open. Please check microphone or "
                + "configuration of this application.\n\n"
                + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
        this.line = line;
    }

    @Override
    public RecorderBody clone() throws CloneNotSupportedException {
        final RecorderBody cloneRecorder = new RecorderBody();
        cloneRecorder.byteSignal = this.byteSignal.clone();
        return cloneRecorder;
    }

    public final byte[] getByteSignal() {
        return this.byteSignal.clone();
    }

    public final double[] getDoubleSignal() {
        final int nbits = AppConfig.format.getSampleSizeInBits();
        final boolean isBigEndian = AppConfig.format.isBigEndian();
        return Converter.byte2double(this.byteSignal, nbits, isBigEndian);
    }

    private final byte[] getPartOfByteSignal(
        final double startPercent, final double endPercent
    ) {
        final double[] dSignal = this.getDoubleSignal();
        final int startIndex = (int) (dSignal.length * startPercent);
        final int endIndex = (int) (dSignal.length * endPercent);
        final double[] newSignal = new double[endIndex - startIndex];
        System.arraycopy(dSignal, startIndex, newSignal, 0, newSignal.length);
        final int nbits = AppConfig.format.getSampleSizeInBits();
        final boolean isBigEndian = AppConfig.format.isBigEndian();
        return Converter.double2byte(newSignal, nbits, isBigEndian);
    }

    private final double[] getNormalizationSignalPowerArray_db() {
        final double max = Arrays.stream(this.getDoubleSignal()).max().orElse(1.0);
        return Arrays.stream(this.getDoubleSignal())
            .map(y -> y / max)  // Normalization
            .map(y -> 10 * Math.log10(y * y + 1e-12))
            .toArray();
    }

    public final double getStartPointOfSpeechSection_percent() {
        final double[] power = this.getNormalizationSignalPowerArray_db();
        final int startPoint = (int) (AppConfig.format.getFrameRate() * 0.1);
        for (int i = startPoint; i < power.length; i++) {
            if (trimmingThreshold_db < power[i]) {
                final double ret = 
                    (double) (i - trimmingMargin_s * AppConfig.format.getSampleRate())
                    / power.length;
                return Math.max(ret, 0.0);
            }
        }
        return 0;
    }

    public final double getEndPointOfSpeechSection_percent() {
        final double[] power = this.getNormalizationSignalPowerArray_db();
        final int endPoint = power.length - (int) (AppConfig.format.getFrameRate() * 0.1);
        for (int i = endPoint; i >= 0; i--) {
            if (trimmingThreshold_db < power[i]) {
                final double ret = 
                    (double) (i + (trimmingMargin_s + 0.1) * AppConfig.format.getSampleRate())
                    / power.length;
                return Math.min(ret, 1.0);
            }
        }
        return 1.0;
    }

    public static final boolean isRecording() {
        return RecorderBody.recording;
    }

    public final boolean isClipping() {
        final double clippingThreshold = 0.99;
        final double max = Arrays.stream(this.getDoubleSignal())
            .map(Math::abs)
            .max()
            .getAsDouble();
        return clippingThreshold < max;
    }

    public final double getSignalNoiseRatio() {
        final double[] dSignal = this.getDoubleSignal();

        final List<Double> power = new ArrayList<>();
        try {
            final double width_s = 0.150;
            final double shift_s = 0.075;
            final double fs_hz = AppConfig.format.getSampleRate();
            final int windowSize = (int) (width_s * fs_hz);
            final int shiftSize = (int) (shift_s * fs_hz);

            final List<Double> thisPower = new ArrayList<>();
            for (int i = 0; i < dSignal.length; i += shiftSize) {
                thisPower.clear();
                for (int j = 0; j < windowSize; j++) {
                    thisPower.add(Math.pow(dSignal[i + j], 2));
                }
                power.add(thisPower.stream().mapToDouble(x -> x).average().getAsDouble());
            }
        } catch (IndexOutOfBoundsException e) {
            // Finish!!!
        }

        final double signalPower = Collections.max(power);
        final double noisePower = Collections.min(power);
        if (noisePower == 0) {
            throw new ArithmeticException();
        } else {
            return 10 * Math.log10(signalPower / noisePower);
        }
    }

    public final void startRecording() throws Exception {
        // Open input-line
        outStream.reset();
        line.open();
        line.start();
        // determine thread and start recording
        final Thread recordThread = new Thread(() -> {
            final byte[] buffer = new byte[1200]; // 1200 = lcm[1, 2, 3, 4] * 100
            while (this.line.isOpen()) {
                final int count = this.line.read(buffer, 0, buffer.length);
                if (0 < count) {
                    outStream.write(buffer, 0, count);
                }
            }
        });
        recordThread.setPriority(Thread.MAX_PRIORITY);
        recordThread.start();
        RecorderBody.recording = true;
    }

    public final void stopRecording() {
        this.line.close();
        final byte[] recordedSignal = this.outStream.toByteArray();
        final float fs = AppConfig.format.getSampleRate();
        final int nBits = AppConfig.format.getSampleSizeInBits();
        final float signalLength_s = (float) recordedSignal.length / ((nBits / 8) * fs);
        this.byteSignal = 0.2 /* s */ <= signalLength_s
            ? recordedSignal.clone() 
            : defaultSignal;
        RecorderBody.recording = false;
    }

    public final void saveSignalAsWav(
        final double startPercent, final double endPercent
    ) {
        final byte[] signal = this.getPartOfByteSignal(startPercent, endPercent);
        try (
            final AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(signal), AppConfig.format,
                signal.length / AppConfig.format.getFrameSize()
            )
        ) {
            final File file = AppConfig.getSavePath(Main.currentIndex);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
        } catch (Exception e) {
            e.printStackTrace(AppConfig.logTargetStream);
            JOptionPane.showMessageDialog(
                null, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public final void enforceStopRecording() {
        if (this.line != null) {
            this.line.close();
        }
        this.byteSignal = defaultSignal;
        RecorderBody.recording = false;
        // sse.reset();
    }

    public final void playSignal(
        final double startPercent, final double endPercent
    ) {
        try (
            final var ais = new AudioInputStream(
                new ByteArrayInputStream(this.getPartOfByteSignal(startPercent, endPercent)),
                AppConfig.format, this.byteSignal.length / AppConfig.format.getFrameSize());
        ) {
            // 再生終了は観測しない (特にする意味がない)
            final Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace(AppConfig.logTargetStream);
            JOptionPane.showMessageDialog(
                null, e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
