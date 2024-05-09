

import java.io.*;

/**
 * User: hadi
 * Date: Mar 6, 2009
 * Time: 8:21:15 PM
 */
public class TestRun {
    public static void main(String[] args){
        for(int i = 25; i<=200; i = i+25){
            for(int j = 0; j<=5; j++){
                try {
                    DataOutput out = new DataOutputStream(new FileOutputStream("D:\\hadi-e\\projects\\java\\gpNoCsim\\working\\gpNoCsimpp\\classes\\nocSimParameter.txt"));
                    String str = "CURRENT_NET = "+ j +"\r\n" +
                            "WK_W = 4\r\n" +
                            "WK_L = 2\r\n" +
                            "AVG_INTER_ARRIVAL = " + i +"\r\n" +
                            "AVG_MESSAGE_LENGTH = 200\r\n" +
                            "FLIT_LENGTH = 64\r\n" +
                            "NUMBER_OF_IP_NODE = 16\r\n" +
                            "CURRENT_VC_COUNT = 4\r\n" +
                            "NUM_FLIT_PER_BUFFER = 10\r\n" +
                            "NUM_CYCLE = 20\r\n" +
                            "NUM_RUN = 1\r\n" +
                            "TRAFFIC_TYPE = 0\r\n" +
                            "WARM_UP_CYCLE = 0.1\r\n" +
                            "FIXED_MESSAGE_LENGTH = false\r\n" +
                            "TRACE = false\r\n" +
                            "ASYNCHRONOUS = true\r\n" +
                            "DEBUG=false\r\n\n";
                     out.write(str.getBytes());
                    Process p = Runtime.getRuntime().exec("run.bat");
                    p.waitFor();

                    System.out.println(i+" "+j);


                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
    }
}
