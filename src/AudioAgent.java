import javax.sound.sampled.*;

public class AudioAgent {

    public static TargetDataLine microphone;
    public static SourceDataLine speaker;

    public static SourceDataLine getSpeaker() {
        return speaker;
    }

    public static TargetDataLine getMicrophone() {
        return microphone;
    }

    public static boolean init(){

        try {

            AudioFormat audioFormat = getAudioFormat();
            DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
            microphone.open(audioFormat);
            microphone.start();

            DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
            speaker.open(audioFormat);
            speaker.start();

            return true;

        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return false;
        }

    }

    private static AudioFormat getAudioFormat() {
        float sampleRate = 16000.0F;
        int sampleSizeBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = false;

        return new AudioFormat(sampleRate, sampleSizeBits, channels, signed, bigEndian);
    }


}
