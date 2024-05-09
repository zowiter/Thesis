package gui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 *
 * User: Amir Azimi
 * Date: Mar 14, 2009
 * Time: 12:49:09 AM
 */
public class AboutFrame {
    JFrame aboutFrame = new JFrame("About gpNoCsim++");
    JPanel container = new JPanel();
    JButton okButton = new JButton("Ok");
    JLabel aboutLableLabel = new JLabel("This software is developed by:");
    JLabel mehdiLabel = new JLabel("Mehdi Bahrbegi");
    JLabel hadiLabel = new JLabel("Hadi Bahrbegi");
    JLabel amirLabel =  new JLabel("Amir Azimi Alasti Ahrabi");

    public AboutFrame() {
        aboutFrame.setSize(300, 200);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        aboutFrame.add(container);
        okButton.setSize(50, 40);
        container.add(aboutLableLabel);
        container.add(mehdiLabel);
        container.add(hadiLabel);
        container.add(amirLabel);
        container.add(okButton);
        aboutFrame.setResizable(false);
        //aboutFrame.pack();
        aboutFrame.setVisible(true);
        actionListner();
    }

    private void actionListner() {
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                aboutFrame.dispose();
            }
        });
    }
}
