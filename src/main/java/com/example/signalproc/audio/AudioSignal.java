package com.example.signalproc.audio;
import javax.sound.sampled.*;
import java.lang.Math.*;
import java.util.Arrays;

/** A container for an audio signal backed by a double buffer so as to allow floating point calculation
 * for signal processing and avoid saturation effects. Samples are 16 bit wide in this implementation. */

public class AudioSignal {
    private double[] sampleBuffer; // floating point representation of audio samples
    private double dBlevel; // current signal level

    /**
     * Construct an AudioSignal that may contain up to "frameSize" samples.
     * 9 * @param frameSize the number of samples in one audio frame
     */
    public AudioSignal(int frameSize) {
        this.sampleBuffer = new double[frameSize];
    }

    /**
     * Sets the content of this signal from another signal.
     * 13 * @param other other.length must not be lower than the length of this signal.
     */
    public void setFrom(AudioSignal other) {
        if (other.sampleBuffer.length >= this.sampleBuffer.length) {
            for (int i = 0; i < this.sampleBuffer.length; i++) {
                this.sampleBuffer[i] = other.sampleBuffer[i];
            }
        }
        this.dBlevel = other.dBlevel;
    }

    /**
     * Fills the buffer content from the given input. Byte's are converted on the fly to double's.
     * 17 * @return false if at end of stream
     */
    public boolean recordFrom(TargetDataLine audioInput) {
        byte[] byteBuffer = new byte[sampleBuffer.length * 2]; // 16 bit samples
        if (audioInput.read(byteBuffer, 0, byteBuffer.length) == -1) return false;
        for (int i = 0; i < sampleBuffer.length; i++)
            sampleBuffer[i] = ((byteBuffer[2 * i] << 8) + byteBuffer[2 * i + 1]) / 32768.0; // big endian
        this.dBlevel = 10 * Math.log(10);  //TODO
        return true;
    }

    /**
     * Plays the buffer content to the given output.
     * 28 * @return false if at end of stream
     */
    public boolean playTo(SourceDataLine audioOutput) {
        byte[] byteBuffer = new byte[sampleBuffer.length * 2];
        for (int i = 0; i < this.sampleBuffer.length; i++) {
            long lg = Double.doubleToLongBits(this.sampleBuffer[i]);
            for (int j = 0; j < byteBuffer.length; j++) {
                byteBuffer[j] = (byte) ((lg >> ((7 - i) * 8)) & 0xff);
            }

            audioOutput.write(byteBuffer, 0, byteBuffer.length);
        }
        return (true);
    }

    // Can be implemented much later: Complex[] computeFFT()

    public Complex[] fft(Complex[] x){
        double[] r = this.getSampleBuffer();

        int n = x.length;

            // base case
        if (n == 1) return new Complex[] { x[0] };

            // radix 2 Cooley-Tukey FFT
        if (n % 2 != 0) {
            throw new IllegalArgumentException("n is not a power of 2");
        }

            // compute FFT of even terms
        Complex[] even = new Complex[n/2];
        for (int k = 0; k < n/2; k++) {
            even[k] = x[2*k];
        }
        Complex[] evenFFT = fft(even);

            // compute FFT of odd terms
        Complex[] odd  = even;  // reuse the array (to avoid n log n space)
        for (int k = 0; k < n/2; k++) {
            odd[k] = x[2*k + 1];
        }
        Complex[] oddFFT = fft(odd);

            // combine
        Complex[] y = new Complex[n];
        for (int k = 0; k < n/2; k++) {
            double kth = -2 * k * Math.PI / n;
            Complex wk = new Complex(Math.cos(kth), Math.sin(kth));
            y[k]       = evenFFT[k].plus (wk.times(oddFFT[k]));
            y[k + n/2] = evenFFT[k].minus(wk.times(oddFFT[k]));
        }
        return y;
    }

    public Complex[] computeFFT(){
        double[] r = this.sampleBuffer;
        Complex[] x = new Complex[r.length];
        for(int i =0;i<r.length;i++){
            x[i] = new Complex(r[i],0);
        }
        return fft(x);
    }

    public double[] getSampleBuffer() {return this.sampleBuffer;}

    public void setSampleBuffer(double[] sampleBuffer) {
        this.sampleBuffer = sampleBuffer;
    }

    public double getdBlevel() {
        return this.dBlevel;
    }

    public int getFrameSize() {
        return this.sampleBuffer.length;
    }

    public static void main(String args[]) {
        AudioSignal test = new AudioSignal(3);
        double[] testsample = new double[]{1, 2, 3};
        test.setSampleBuffer(testsample);
        System.out.println(Arrays.toString(test.getSampleBuffer()));
        System.out.println((test.getdBlevel()));

        AudioSignal test2 = new AudioSignal(2);
        test2.setFrom(test);
        System.out.println(Arrays.toString(test2.getSampleBuffer()));
    }
}




