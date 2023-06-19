package jp.f_matano44.mreccorpus2;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;

class DataClass {
    private AudioFormat format;
    private byte[] byteSignal;
    // private double[] doubleSignal;

    public void setFormat(float fs, int nbits, int channels) {
        this.format = new AudioFormat(fs, nbits, channels, true, false);
    }
    
    public AudioFormat getFormat() {
        return format;
    }

    public void setSignal(ByteArrayOutputStream out) {
        byteSignal = out.toByteArray();
    }

    public byte[] getByteSignal() {
        return byteSignal.clone();
    }
}
