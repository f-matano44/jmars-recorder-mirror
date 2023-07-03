package jp.f_matano44.mreccorpus2;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class RecorderBody {
    // 収録に使う変数
    private AudioFormat format;
    private TargetDataLine line;
    private ByteArrayOutputStream outStream;
    // 収録した音声を格納する変数
    private byte[] byteSignal;
    // private double[] doubleSignal;
    // 発話区間推定
    private JSlider startSlider;
    private JSlider endSlider;


    public byte[] getByteSignal() {
        return this.byteSignal.clone();
    }


    public AudioFormat getFormat() {
        return this.format;
    }


    public void startRecording(
        final float fs, final int nbits, final int channels
    ) throws Exception {
        this.format = new AudioFormat(fs, nbits, channels, true, false);
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

            // 録音終了時に gc 回すといい感じになりそうな気がする (気がするだけ)
            System.gc();
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


    public JPanel speechSectionPanel(){
        startSlider = new JSlider(JSlider.HORIZONTAL, 0, 100000, 30000);
        endSlider = new JSlider(JSlider.HORIZONTAL, 0, 100000, 70000);

        final Dimension preferredSize = startSlider.getPreferredSize();
        preferredSize.width = 500; // 新しい幅を設定
        startSlider.setPreferredSize(preferredSize);
        endSlider.setPreferredSize(preferredSize);

        startSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (endSlider.getValue() <= startSlider.getValue()) {
                    endSlider.setValue(startSlider.getValue() + 1);
                }
            }
        });

        endSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (endSlider.getValue() <= startSlider.getValue()) {
                    startSlider.setValue(endSlider.getValue() - 1);
                }
            }
        });

        final JPanel speechSectionPanel = new JPanel();
        speechSectionPanel.setLayout(new BoxLayout(speechSectionPanel, BoxLayout.Y_AXIS));
        speechSectionPanel.add(startSlider);
        speechSectionPanel.add(endSlider);
        speechSectionPanel.setBorder(
            BorderFactory.createTitledBorder("Speech Section: Start / End")
        );

        return speechSectionPanel;
    }
}
