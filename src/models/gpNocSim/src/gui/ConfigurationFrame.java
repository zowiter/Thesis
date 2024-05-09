package gui;

import network.common.HelpingUtility;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.io.*;


/**
 *
 * User: Hadi Bahrbegi
 * Date: Mar 4, 2009
 * Time: 8:11:22 PM
 */
public class ConfigurationFrame {
    JFrame configDialogFrame = new JFrame("NoC Parameter Configuration");
    JPanel containerPanel = new JPanel();
    JPanel networkPanel = new JPanel();
    JPanel networkPanelUp = new JPanel();
    JPanel networkPanelDown = new JPanel();

    JPanel trafficPanel = new JPanel();
    JPanel trafficPanelUp = new JPanel();
    JPanel trafficPanelMiddle = new JPanel();
    JPanel trafficPanelDown = new JPanel();

    JPanel bufferPanel = new JPanel();

    JPanel runPanel = new JPanel();
    JPanel runPanelUp = new JPanel();
    JPanel runPanelDown = new JPanel();

    JPanel buttonPanel = new JPanel();

    JLabel curNetLabel = new JLabel("Current Network");
    JComboBox curNetComboBox = new JComboBox(new String[]{"Fat Tree",
            "Mesh", "Torus", "Extended Fat Tree",
            "Octal", "WK-Recursive"});

    JLabel wkWLabel = new JLabel("Free Edge(W)");
    JTextField wkWTextField = new JTextField(2);

    JLabel wkLLabel = new JLabel("Level(L)");
    JTextField wkLTextField = new JTextField(2);

    JLabel numberOfIpNodeLabel = new JLabel("Number of IP Nodes");
    JTextField numberOfIpNodeTextField = new JTextField(3);

    JLabel avgInterArrivalLabel = new JLabel("Average Inter Arrival");
    JTextField avgInterArrivalTextField = new JTextField(3);

    JLabel avgMessageLengthLabel = new JLabel("Average Message Length");
    JTextField avgMessageLengthTextField = new JTextField(4);

    JLabel flitLengthLabel = new JLabel("Flit Length");
    JTextField flitLengthTextField = new JTextField(3);

    JLabel trafficTypeLabel = new JLabel("Traffic Type");
    JComboBox trafficTypeComboBox = new JComboBox(new String[]{"Uniform", "Local"});

    JLabel fixedMessageLengthLabel = new JLabel("Fixed Message Length");
    JCheckBox fixedMessageLengthCheckBox = new JCheckBox();

    JLabel curVCCountLabel = new JLabel("Current Virtual Channel Count");
    JTextField curVCCountTextField = new JTextField(2);

    JLabel numberOfFlitPerBufferLabel = new JLabel("Number of Flit Per Buffer");
    JTextField numberOfFlitPerBufferTextField = new JTextField(2);

    JLabel cycleNumberLabel = new JLabel("Cycle Number");
    JTextField cycleNumberTextField = new JTextField(7);

    JLabel runNumberLabel = new JLabel("Run Number");
    JTextField runNumberTextField = new JTextField(2);

    JLabel warmUpCycleLabel = new JLabel("Warm Up Cycle");
    JTextField warmUpCycleTextField = new JTextField(3);

    JLabel asynchronousLabel = new JLabel("Acynchronous");
    JCheckBox asynchronousCheckBox = new JCheckBox();

    JButton saveButton = new JButton("Save");
    JButton cancelButton = new JButton("Cancel");


    public ConfigurationFrame() {
        configDialogFrame.setSize(500, 600);
        configDialogFrame.setResizable(false);
        configDialogFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        configDialogFrame.getContentPane().add(containerPanel);
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        networkPanel.setLayout(new BoxLayout(networkPanel, BoxLayout.Y_AXIS));
        trafficPanel.setLayout(new BoxLayout(trafficPanel, BoxLayout.Y_AXIS));
        runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.Y_AXIS));

        containerPanel.add(networkPanel);
        containerPanel.add(trafficPanel);
        containerPanel.add(bufferPanel);
        containerPanel.add(runPanel);
        containerPanel.add(buttonPanel);

        networkPanel.add(networkPanelUp);
        networkPanel.add(networkPanelDown);

        trafficPanel.add(trafficPanelUp);
        trafficPanel.add(trafficPanelMiddle);
        trafficPanel.add(trafficPanelDown);

        runPanel.add(runPanelUp);
        runPanel.add(runPanelDown);


        networkPanelUp.add(curNetLabel);
        networkPanelUp.add(curNetComboBox);
        networkPanelUp.add(wkWLabel);
        networkPanelUp.add(wkWTextField);
        networkPanelUp.add(wkLLabel);
        networkPanelUp.add(wkLTextField);

        networkPanelDown.add(numberOfIpNodeLabel);
        networkPanelDown.add(numberOfIpNodeTextField);

        trafficPanelUp.add(avgInterArrivalLabel);
        trafficPanelUp.add(avgInterArrivalTextField);
        trafficPanelUp.add(avgMessageLengthLabel);
        trafficPanelUp.add(avgMessageLengthTextField);

        trafficPanelMiddle.add(flitLengthLabel);
        trafficPanelMiddle.add(flitLengthTextField);
        trafficPanelMiddle.add(trafficTypeLabel);
        trafficPanelMiddle.add(trafficTypeComboBox);

        trafficPanelDown.add(fixedMessageLengthCheckBox);
        trafficPanelDown.add(fixedMessageLengthLabel);

        bufferPanel.add(curVCCountLabel);
        bufferPanel.add(curVCCountTextField);
        bufferPanel.add(numberOfFlitPerBufferLabel);
        bufferPanel.add(numberOfFlitPerBufferTextField);

        runPanelUp.add(cycleNumberLabel);
        runPanelUp.add(cycleNumberTextField);
        runPanelUp.add(runNumberLabel);
        runPanelUp.add(runNumberTextField);

        runPanelDown.add(warmUpCycleLabel);
        runPanelDown.add(warmUpCycleTextField);
        runPanelDown.add(asynchronousCheckBox);
        runPanelDown.add(asynchronousLabel);

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        configDialogFrame.pack();
        configDialogFrame.setVisible(true);

    }


    public void addListners() {
        configDialogFrame.addWindowListener(new WindowListener() {
            public void windowOpened(WindowEvent e) {
                File paramFile = new File(new HelpingUtility().getFilePath("nocSimParameter.txt"));
                if (paramFile.exists()) {
                    try {
                        DataInput input = new DataInputStream(new FileInputStream(paramFile));
                        String line = input.readLine();
                        while (line != null && !line.equals("")) {
                            String[] str = line.split(" *= *");
                            if (str.length == 2) {
                                if (str[0].equals("CURRENT_NET")) {
                                    curNetComboBox.setSelectedIndex(Integer.valueOf(str[1]).intValue());
                                } else if (str[0].equals("WK_W")) {
                                    wkWTextField.setText(str[1]);
                                } else if (str[0].equals("WK_L")) {
                                    wkLTextField.setText(str[1]);
                                } else if (str[0].equals("AVG_INTER_ARRIVAL")) {
                                    avgInterArrivalTextField.setText(str[1]);
                                } else if (str[0].equals("AVG_MESSAGE_LENGTH")) {
                                    avgMessageLengthTextField.setText(str[1]);
                                } else if (str[0].equals("FLIT_LENGTH")) {
                                    flitLengthTextField.setText(str[1]);
                                } else if (str[0].equals("NUMBER_OF_IP_NODE")) {
                                    numberOfIpNodeTextField.setText(str[1]);
                                } else if (str[0].equals("CURRENT_VC_COUNT")) {
                                    curVCCountTextField.setText(str[1]);
                                } else if (str[0].equals("NUM_FLIT_PER_BUFFER")) {
                                    numberOfFlitPerBufferTextField.setText(str[1]);
                                } else if (str[0].equals("NUM_CYCLE")) {
                                    cycleNumberTextField.setText(str[1]);
                                } else if (str[0].equals("NUM_RUN")) {
                                    runNumberTextField.setText(str[1]);
                                } else if (str[0].equals("TRAFFIC_TYPE")) {
                                    trafficTypeComboBox.setSelectedIndex(Integer.valueOf(str[1]).intValue());
                                } else if (str[0].equals("WARM_UP_CYCLE")) {
                                    warmUpCycleTextField.setText(str[1]);
                                } else if (str[0].equals("FIXED_MESSAGE_LENGTH")) {
                                    fixedMessageLengthCheckBox.setSelected(str[1].equalsIgnoreCase("false") ? false : true);
                                } else if (str[0].equals("ASYNCHRONOUS")) {
                                    asynchronousCheckBox.setSelected(str[1].equalsIgnoreCase("false") ? false : true);
                                }
                            }
                            line = input.readLine();
                        }
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (IOException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }

            public void windowClosing(WindowEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void windowClosed(WindowEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void windowIconified(WindowEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void windowDeiconified(WindowEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void windowActivated(WindowEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void windowDeactivated(WindowEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                configDialogFrame.dispose();
            }

        });
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (curNetComboBox.getSelectedIndex() == 5 &&
                        (wkWTextField.getText().equals("") ||
                        wkLTextField.getText().equals(""))) {
                    JOptionPane.showMessageDialog(configDialogFrame, "You must fill \"Free Edge(W)\" and \"Level(L)\" text boxes.", "Fill text boxes", JOptionPane.OK_OPTION);
                    return;

                }
                if (!numberOfIpNodeTextField.getText().equals("") &&
                        !avgInterArrivalTextField.getText().equals("") &&
                        !avgMessageLengthTextField.getText().equals("") &&
                        !flitLengthTextField.getText().equals("") &&
                        !curVCCountTextField.getText().equals("") &&
                        !numberOfFlitPerBufferTextField.getText().equals("") &&
                        !cycleNumberTextField.getText().equals("") &&
                        !runNumberTextField.getText().equals("") &&
                        !warmUpCycleTextField.getText().equals("")) {
                    File paramFile = new File(new HelpingUtility().getFilePath("nocSimParameter.txt"));
                    DataOutput output = null;
                    try {

                        output = new DataOutputStream(new FileOutputStream(paramFile));
                    } catch (FileNotFoundException e1) {
                        try {
                            paramFile.createNewFile();
                            output = new DataOutputStream(new FileOutputStream(paramFile));
                        } catch (IOException e2) {
                            System.err.println("Error: " + e2.getMessage());
                        }
                    }
                    try {
                        output.writeBytes("CURRENT_NET = " + curNetComboBox.getSelectedIndex() + "\r\n");
                        output.writeBytes("WK_W = " + wkWTextField.getText() + "\r\n");
                        output.writeBytes("WK_L = " + wkLTextField.getText() + "\r\n");
                        output.writeBytes("AVG_INTER_ARRIVAL = " + avgInterArrivalTextField.getText() + "\r\n");
                        output.writeBytes("AVG_MESSAGE_LENGTH = " + avgMessageLengthTextField.getText() + "\r\n");
                        output.writeBytes("FLIT_LENGTH = " + flitLengthTextField.getText() + "\r\n");
                        output.writeBytes("NUMBER_OF_IP_NODE = " + numberOfIpNodeTextField.getText() + "\r\n");
                        output.writeBytes("CURRENT_VC_COUNT = " + curVCCountTextField.getText() + "\r\n");
                        output.writeBytes("NUM_FLIT_PER_BUFFER = " + numberOfFlitPerBufferTextField.getText() + "\r\n");
                        output.writeBytes("NUM_CYCLE = " + cycleNumberTextField.getText() + "\r\n");
                        output.writeBytes("NUM_RUN = " + runNumberTextField.getText() + "\r\n");
                        output.writeBytes("TRAFFIC_TYPE = " + trafficTypeComboBox.getSelectedIndex() + "\r\n");
                        output.writeBytes("WARM_UP_CYCLE = " + warmUpCycleTextField.getText() + "\r\n");
                        output.writeBytes("FIXED_MESSAGE_LENGTH = " + (fixedMessageLengthCheckBox.isSelected() ? "true" : "false") + "\r\n");
                        output.writeBytes("TRACE = false" + "\n");
                        output.writeBytes("ASYNCHRONOUS = " + (asynchronousCheckBox.isSelected() ? "true" : "false") + "\r\n");
                        output.writeBytes("DEBUG = false" + "\r\n\n");
                    } catch (IOException e1) {
                        System.err.println("Error: " + e1.getMessage());
                    }
                    finally {

                    }
                    System.out.println("Configuration Changes successfuly saved.");
                    configDialogFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(configDialogFrame, "You must fill blank text boxes.", "Fill text boxes", JOptionPane.OK_OPTION);
                }
            }
        });
    }
}
