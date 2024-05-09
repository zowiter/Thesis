package gui;

import network.Controller;
import network.NetworkManager;

/**
 *
 * User: Amir Azimi
 * Date: Mar 6, 2009
 * Time: 1:53:07 AM
 */
public class GpNoCSimRunable implements Runnable {
    public void run() {
        Controller controller = new Controller();
        controller.main_loop();
        ResultFrame resultFrame = new ResultFrame();
    }
}
