package gpApp.gui;

import gpApp.network.Controller;

import java.io.File;

/**
 *
 * User: Amir Azimi
 * Date: Mar 6, 2009
 * Time: 1:53:07 AM
 */
public class GpNoCSimRunable implements Runnable {

    private final String inputFile;

    private final String outputFile;

    public GpNoCSimRunable(String inputFile, String outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public void run() {
        Controller controller = new Controller();
        controller.main_loop(inputFile, outputFile);
    }
}
