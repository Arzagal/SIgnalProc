package com.example.signalproc.audio;
import javax.sound.sampled.*;
import java.util.Arrays;


/** The main audio processing class, implemented as a Runnable so
 2 * as to be run in a separated execution Thread. */
public class AudioProcessor implements Runnable {

    private AudioSignal inputSignal, outputSignal;
    private TargetDataLine audioInput;
    private SourceDataLine audioOutput;
    private boolean isThreadRunning; // makes it possible to "terminate" thread

        /** Creates an AudioProcessor that takes input from the given TargetDataLine, and plays back
 11 * to the given SourceDataLine.
 12 * @param frameSize the size of the audio buffer. The shorter, the lower the latency. */
        public AudioProcessor(TargetDataLine audioInput, SourceDataLine audioOutput, int frameSize) {
            this.audioInput = audioInput;
            this.audioOutput = audioOutput;
            this.inputSignal = new AudioSignal(frameSize);
            this.outputSignal = new AudioSignal(frameSize);

         }

        /** Audio processing thread code. Basically an infinite loop that continuously fills the sample
 17 * buffer with audio data fed by a TargetDataLine and then applies some audio effect, if any,
 18 * and finally copies data back to a SourceDataLine.*/

        @Override
        public void run() {
        isThreadRunning = true;
        while (isThreadRunning) {
            inputSignal.recordFrom(audioInput);

             // your job: copy inputSignal to outputSignal with some audio effect

            outputSignal.playTo(audioOutput);
        }
        }

        /** Tells the thread loop to break as soon as possible. This is an asynchronous process. */
        public void terminateAudioThread() {
            Thread.currentThread().interrupt();
        }

        // todo here: all getters and setters

    public AudioSignal getInputSignal() {
        return inputSignal;
    }

    public void setInputSignal(AudioSignal inputSignal) {
        this.inputSignal = inputSignal;
    }

    public AudioSignal getOutputSignal() {
        return outputSignal;
    }

    public void setOutputSignal(AudioSignal outputSignal) {
        this.outputSignal = outputSignal;
    }

    public boolean isThreadRunning() {
        return isThreadRunning;
    }

    public void setThreadRunning(boolean threadRunning) {
        isThreadRunning = threadRunning;
    }

    public void updateInput(){
            this.inputSignal.recordFrom(audioInput);
    }

    /* an example of a possible test code */
        public static void main(String[] args) throws LineUnavailableException {
            TargetDataLine inLine = AudioIO.obtainAudioInput("Réseau de microphones (Realtek(", 16000);
            SourceDataLine outLine = AudioIO.obtainAudioOutput("Haut-parleurs (Realtek(R) Audio)", 16000);
            AudioProcessor as = new AudioProcessor(inLine, outLine, 1024);
            inLine.open(); inLine.start(); outLine.open(); outLine.start();
            new Thread(as).start();
            System.out.println("A new thread has been created!");

        }
}
