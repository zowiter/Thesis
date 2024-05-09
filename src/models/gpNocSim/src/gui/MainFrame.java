package gui;

import network.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintStream;
import tool.FileExtractor;

/**
 *
 * User: Hadi Bahrbegi
 * Date: Mar 4, 2009
 * Time: 7:48:25 PM
 */
public class MainFrame {
    JFrame mainFrame = new JFrame("gpNoCsim++");
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem runMenuItem = new JMenuItem("Run");
    JMenuItem suspendMenuItem = new JMenuItem("Suspend");
    JMenuItem resumeMenuItem = new JMenuItem("Resume");
    JMenuItem stopMenuItem = new JMenuItem("Stop");
    JMenuItem showResultMenuItem = new JMenuItem("Show Result");
    JMenuItem exitMenuItem = new JMenuItem("Exit");
    JMenu settingMenu = new JMenu("Setting");
    JMenuItem configMenuItem = new JMenuItem("Configuration");
    JMenu helpMenu = new JMenu("Help");
    JMenuItem aboutMenuItem = new JMenuItem("About");
    JMenu reportMenu = new JMenu("Report");
    JMenuItem resultExtractorMenuItem = new JMenuItem("Extract Results");
    JTextArea outTextArea = new JTextArea(20, 40);
    JScrollPane resultScrollPane = new JScrollPane(outTextArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    OutputStream outputStream = new OutputStream() {
        public void write(int b) throws IOException {
            //String ch = Character.toString(Character.toChars(b)[0]);
            outTextArea.append(Character.toString(Character.toChars(b)[0]));
        }
    };
    PrintStream printAppResults = new PrintStream(outputStream, true);
    private Thread runnable;


    public MainFrame() {
        mainFrame.setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(runMenuItem);
        fileMenu.add(suspendMenuItem);
        fileMenu.add(resumeMenuItem);
        fileMenu.add(stopMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(showResultMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        menuBar.add(settingMenu);
        settingMenu.add(configMenuItem);
        runMenuItem.setMnemonic('R');
        suspendMenuItem.setMnemonic('p');
        resumeMenuItem.setMnemonic('s');
        stopMenuItem.setMnemonic('t');
        configMenuItem.setMnemonic('C');
        showResultMenuItem.setMnemonic('S');
        exitMenuItem.setMnemonic('E');
        mainFrame.add(resultScrollPane);
        resultScrollPane.setAutoscrolls(true);
        menuBar.add(reportMenu);
        reportMenu.add(resultExtractorMenuItem);
        menuBar.add(helpMenu);
        helpMenu.add(aboutMenuItem);
        System.setOut(printAppResults);
        mainFrame.setSize(600, 500);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);
    }

    public void addListners() {
        resultExtractorMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileExtractor fileExtractor = new FileExtractor();
                fileExtractor.resultFileReader();
                System.out.print("The result file now xreated.");
            }
        });
        runMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runnable = new Thread(new GpNoCSimRunable());
                runnable.start();
            }
        });
        configMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ConfigurationFrame conf = new ConfigurationFrame();
                conf.addListners();
            }
        });
        showResultMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ResultFrame resultFrame = new ResultFrame();
            }
        });
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mainFrame.dispose();
            }
        });
        mainFrame.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {

            }

            public void windowClosing(WindowEvent e) {

            }

            public void windowClosed(WindowEvent e) {
                if (runnable != null) {
                    runnable.stop();
                }
            }

            public void windowIconified(WindowEvent e) {
            }

            public void windowDeiconified(WindowEvent e) {
            }

            public void windowActivated(WindowEvent e) {
            }

            public void windowDeactivated(WindowEvent e) {
            }
        });
        resumeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runnable.resume();
            }
        });
        suspendMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runnable.suspend();
            }
        });
        stopMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (runnable != null) {
                    runnable.stop();
                }
            }
        });
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AboutFrame aboutFrame = new AboutFrame();
            }
        });
    }
}
