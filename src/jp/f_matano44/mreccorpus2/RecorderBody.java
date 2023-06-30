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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

class RecorderBody {
    // 収録に使う変数
    private AudioFormat format;
    private TargetDataLine line;
    private ByteArrayOutputStream outStream;
    // 収録した音声を格納する変数
    private byte[] byteSignal;
    // private double[] doubleSignal;


    public byte[] getByteSignal() {
        return byteSignal.clone();
    }


    public void startRecording(float fs, int nbits, int channels) throws Exception {
        this.format = new AudioFormat(fs, nbits, channels, true, false);
        this.outStream = new ByteArrayOutputStream();

        final int baseBufferSize = 10240;
        final int frameSize = format.getFrameSize();
        final int bufferSize = (int) (baseBufferSize / frameSize) * frameSize;

        // recording main
        line = AudioSystem.getTargetDataLine(format);
        line.open(format);
        line.start();
        new Thread(() -> {
            byte[] buffer = new byte[bufferSize];
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
            byteSignal = outStream.toByteArray();
            System.gc(); // 録音終了時に gc 回すといい感じになりそうな気がする (気がするだけ)
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


    public void saveAsWav(final File[] lastSelectedDir){
        final JFileChooser fileChooser = new JFileChooser(lastSelectedDir[0]) {
            @Override
            public void approveSelection() {
                final File file = getSelectedFile();
                if (!file.getName().endsWith(".wav")) {
                    setSelectedFile(new File(file.toString() + ".wav"));
                }
                super.approveSelection();
            }
        };

        fileChooser.setFileFilter(
            new FileNameExtensionFilter(
                "WAV files", "wav"
            )
        );

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            lastSelectedDir[0] = file.getParentFile();

            final byte[] audio = this.byteSignal;
            final AudioFormat format = this.format;
            try (
                AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(audio),
                    format, audio.length / format.getFrameSize()
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
}
