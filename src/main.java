import javax.sound.sampled.*;

public class main {

    public static void main(String[] args) throws Exception{

        System.out.println("Init Audio Agent ...");
        if(AudioAgent.init()){
            System.out.println("Audio Agent Init Success!");
            Management management = new Management();
            management.init(7000);
            System.out.println("If there are no errors, management must be running at port 7000 and ready to work!");
        }else {
            System.out.println("Audio Agent Init Failed!");
            System.exit(-1);
        }

    }

}
