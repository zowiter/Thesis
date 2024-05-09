/**
 * <p>Title: gpNoCsimpp - General Purpose Netowork-on-Chip Simulation Plus Plus
 * 	  					Framework</p>
 *  * <p>Developed: Department of Computer Science and Engineering of Shabestar Azad University</p>
 * @version 1.0
 */

//import gui.MainFrame;
//import gui.ResultFrame;
import network.Controller;

/**
 * gpNoCsimpp is the starting class of the simulator.
 * <p/>
 * Contains the main method and initiates the {@link gui.MainFrame} instance.
 *
 * @version 1.0
 */
public class gpNoCsimpp {

    /**
     * starting point of the simulator
     *
     * @param args command line arguements
     * @see gui.MainFrame
     */
    public static void main(String[] args) {
        //MainFrame app = new MainFrame();
        //app.addListners();
        Controller controller = new Controller();
        //controller.main_loop();
        //ResultFrame resultFrame = new ResultFrame();
        
    }
}
