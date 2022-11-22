package com.example.signalproc.audio;
import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A collection of static utilities related to the audio system. */
public class AudioIO {
    /**
     * Displays every audio mixer available on the current system.
     */
    public static AudioFormat af = new AudioFormat(8000, 8, 1, true, true);

    public static void printAudioMixers() {
        System.out.println("Mixers:");
        Arrays.stream(AudioSystem.getMixerInfo())
                .forEach(e -> {
                    System.out.println("- name=\"" + e.getName() + "\" description=" + e.getDescription() + " by " + e.getVendor() + "\"");
                });
    }

    public static String[] getAudioMixersIN(){
        List<String> listRes = new ArrayList<String>();
        Arrays.stream(AudioSystem.getMixerInfo())
                .forEach(e -> {
                    if (e.getDescription().equals("Direct Audio Device: DirectSound Capture")) {
                        listRes.add((e.getName()));
                    }
                });
        String[] res = new String[listRes.size()];
        listRes.toArray(res);
        return res;
    }

    /**
     * @return a Mixer.Info whose name matches the given string.
     * 13 Example of use: getMixerInfo("Macbook default output")
     */
    public static Mixer.Info getMixerInfo(String mixerName) {
        return Arrays.stream(AudioSystem.getMixerInfo())
                .filter(e -> e.getName().equalsIgnoreCase(mixerName)).findFirst().get();
    }

    /**
     * Return a line that's appropriate for recording sound from a microphone.
     * 21 * Example of use:x
     * 22 * TargetDataLine line = obtainInputLine("USB Audio Device", 8000);
     * 23 * @param mixerName a string that matches one of the available mixers.
     * 24 * @see AudioSystem.getMixerInfo() which provides a list of all mixers on your system.
     * 25
     */

    static void showLineInfoFormats(final Line.Info lineInfo)
    {
        if (lineInfo instanceof DataLine.Info)
        {
            final DataLine.Info dataLineInfo = (DataLine.Info)lineInfo;

            Arrays.stream(dataLineInfo.getFormats())
                    .forEach(format -> System.out.println("    " + format.toString()));
        }
    }

    public static TargetDataLine obtainAudioInput(String mixerName, int sampleRate) throws LineUnavailableException {
        Mixer.Info mixerInfo = getMixerInfo(mixerName);
        Mixer mixer = AudioSystem.getMixer(mixerInfo);
        Line.Info[] dlInfo =  mixer.getTargetLineInfo();
        for (Line.Info lI: dlInfo) {
            if(lI instanceof DataLine.Info){
                final DataLine.Info dataLineInfo = (DataLine.Info)lI;
                if(dataLineInfo.isFormatSupported(af)){return(AudioSystem.getTargetDataLine(af,mixerInfo));}
            }
        }
        return null;
    }

    /**
     * Return a line that's appropriate for playing sound to a loudspeaker.
     */
    public static SourceDataLine obtainAudioOutput(String mixerName, int sampleRate) throws LineUnavailableException {
        Mixer.Info mixerInfo = getMixerInfo(mixerName);
        Mixer mixer = AudioSystem.getMixer(mixerInfo);
        Line.Info[] dlInfo = mixer.getSourceLineInfo();

        return(AudioSystem.getSourceDataLine(af,mixerInfo));
    }




    public static void main(String[] args) {
        AudioIO.printAudioMixers();
        AudioSignal testSig = new AudioSignal(10000);
        //Mixer.Info mi = AudioIO.getMixerInfo("Port Réseau de microphones (Realtek(");

        try{
            TargetDataLine tdl = AudioIO.obtainAudioInput("Réseau de microphones (Realtek(",8000);
            tdl.open();
            tdl.start();
            testSig.recordFrom(tdl);
            System.out.println(Arrays.toString(testSig.getSampleBuffer()));
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
