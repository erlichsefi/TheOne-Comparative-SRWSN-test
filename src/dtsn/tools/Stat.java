package dtsn.tools;

import java.sql.Array;
import java.util.Arrays;

public class Stat {
    public static int SOURCE_BEFFER  ;
    public static int last_ack;
    public static String[] last_nack;

    public static String DEST_MINIMAL_MISSING;

    public static int runThread(){
        new Thread(){
            public void run(){
                while(true) {
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    System.out.println("**************");
                    System.out.println("source: beffer= " + SOURCE_BEFFER + "  last_ack= " + last_ack + " last_nack= " + Arrays.toString(last_nack));
                    System.out.println("dest: minimal missing= " + DEST_MINIMAL_MISSING);
                    System.out.println("**************");
                }

            }
        }.start();
        return 1;
    }
}
