package gui;

import network.common.IConstants;
import network.common.HelpingUtility;

import javax.swing.*;
import java.io.*;

/**
 *
 * User: Hadi Bahrbegi
 * Date: Mar 5, 2009
 * Time: 12:01:56 AM
 */
public class ResultFrame {
    JFrame contentFrame = new JFrame("Simulation Results");
    JTextArea resultTextArea = new JTextArea(25, 60);
    JScrollPane resultScrollPane = new JScrollPane(resultTextArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

  
    public ResultFrame() {
        contentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        contentFrame.getContentPane().add(resultScrollPane);
        File resultFile = new File(new HelpingUtility().getFilePath(IConstants.OUT_FILE));
        DataInput in = null;
        try {
            in = new DataInputStream(new FileInputStream(resultFile));
            String line = in.readLine();
            while (line != null && !line.equals("")) {
                resultTextArea.append(line + "\n");
                line = in.readLine();
            }
        } catch (FileNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
            resultTextArea.setText("output file not found or curropted");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            resultTextArea.setText("output file not found or curropted");
        }
        resultTextArea.setEditable(false);
        contentFrame.setResizable(false);
        contentFrame.pack();
        contentFrame.setVisible(true);
    }
}
