package gpApp.tool;

import gpApp.network.common.HelpingUtility;

import java.io.*;
import java.util.HashMap;

/**
 * User: Amir Azimi
 * Date: Mar 8, 2009
 * Time: 10:39:41 PM
 */
public class FileExtractor {
    public void resultFileReader() {
        try {
            File resultFile = new File(new HelpingUtility().getFilePath("nocSimOutput.txt"));
            DataInput input = new DataInputStream(new FileInputStream(resultFile));
            String line = input.readLine();
            HashMap resultArray[][] = new HashMap[8][6];
            while (line != null) {
                if (line.contains("Input Configuration: ......")) {
                    HashMap trafficInfo = new HashMap();
                    line = input.readLine();
                    while (!line.contains("Total")) {
                        if (line.contains("  network.Network")) {
                            line = line.replace("  network.Network", "  network.Network\t");
                        } else if (line.contains("  Throughput[Net]")) {
                            line = line.replace("  Throughput[Net]", "  Throughput[Net]\t");
                        } else if (line.contains("  Throughput[Flits leaving network.unit.switches.Switch]")) {
                            line = line.replace("  Throughput[Flits leaving network.unit.switches.Switch]",
                                    "  Throughput[Flits leaving network.unit.switches.Switch]\t");
                        } else
                        if (line.contains("  Buffer(network.unit.node.Node & network.unit.switches.Switch) Utilization")) {
                            line = line.replace("  Buffer(network.unit.node.Node & network.unit.switches.Switch) Utilization",
                                    "  Buffer(network.unit.node.Node & network.unit.switches.Switch) Utilization\t");
                        } else if (line.contains("  Avg Packet Delay")) {
                            line = line.replace("  Avg Packet Delay", "  Avg Packet Delay\t");
                        } else if (line.contains("  Avg Hop Count")) {
                            line = line.replace("  Avg Hop Count", "  Avg Hop Count\t");
                        } else if (line.contains("  Avg Packet Sent/Run")) {
                            line = line.replace("  Avg Packet Sent/Run", "  Avg Packet Sent/Run\t");
                        }
                        String str[] = line.split(" *\t+ *");
                        int networkType = 0;
                        if (str.length == 2) {
                            if (str[0].equalsIgnoreCase("  network.Network")) {
                                trafficInfo.put("Network", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Avg Msg Production Rate(cycle/msg)")) {
                                trafficInfo.put("Avg Msg Production Rate", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Throughput[Net]")) {
                                trafficInfo.put("Throughput[Net]", str[1]);
                            } else
                            if (str[0].equalsIgnoreCase("  Throughput[Flits leaving network.unit.switches.Switch]")) {
                                trafficInfo.put("Throughput[Flits leaving network.unit.switches.Switch]", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Avg Packet Delay")) {
                                trafficInfo.put("Avg Packet Delay", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Link Utilization")) {
                                trafficInfo.put("Link Utilization", str[1]);
                            } else
                            if (str[0].equalsIgnoreCase("  Buffer(network.unit.node.Node & network.unit.switches.Switch) Utilization")) {
                                trafficInfo.put("Buffer(network.unit.node.Node & network.unit.switches.Switch) Utilization", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Avg Packet Injection Rate")) {
                                trafficInfo.put("Avg Packet Injection Rate", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Avg Packet Not Produced")) {
                                trafficInfo.put("Avg Packet Not Produced", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Input Buffer(network.unit.node.Node) Utilization")) {
                                trafficInfo.put("Input Buffer(network.unit.node.Node) Utilization", str[1]);
                            } else
                            if (str[0].equalsIgnoreCase("  Input Buffer(network.unit.switches.Switch) Utilization")) {
                                trafficInfo.put("Input Buffer(network.unit.switches.Switch) Utilization", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Output Buffer(network.unit.node.Node) Utilization")) {
                                trafficInfo.put("Output Buffer(network.unit.node.Node) Utilization", str[1]);
                            } else
                            if (str[0].equalsIgnoreCase("  Output Buffer(network.unit.switches.Switch) Utilization")) {
                                trafficInfo.put("Output Buffer(network.unit.switches.Switch) Utilization", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Avg Hop Count")) {
                                trafficInfo.put("Avg Hop Count", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Avg Packet Sent/Run")) {
                                trafficInfo.put("Avg Packet Sent/Run", str[1]);
                            } else if (str[0].equalsIgnoreCase("  Avg Packet Received/Run")) {
                                trafficInfo.put("Avg Packet Received/Run", str[1]);
                            }
                        }
                        line = input.readLine();
                    }
                    resultArray[Integer.valueOf(trafficInfo.get("Avg Msg Production Rate").toString()).intValue() / 25 - 1]
                            [Integer.valueOf(trafficInfo.get("Network").toString()).intValue()] = trafficInfo;
                } else {
                    line = input.readLine();
                }
            }
            createResultHTMLBasedOnTrafficRate(resultArray);
            createResultHTMLBasedOnTopology(resultArray);
            createResultHTMLBasedOnNetParameters(resultArray);

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void createResultHTMLBasedOnNetParameters(HashMap[][] resultArray) {
        try {
            HelpingUtility helpingUtility = new HelpingUtility();
            String dirPath = helpingUtility.getFilePath("results");
            File dirFile = new File(dirPath);
            if(!dirFile.isDirectory()){
                dirFile.mkdir();
            }

            File htmlFile = new File(helpingUtility.getFilePath("results/extracted-results-parameters.html"));
            if (!htmlFile.exists()) {
                htmlFile.createNewFile();
            }
            DataOutput output = new DataOutputStream(new FileOutputStream(htmlFile));
            output.write(("<html><head><style>\n" +
                    "body{\n" +
                    "font-size:14;\n" +
                    "font-family:tahoma;\n" +
                    "}\n" +
                    "table{\n" +
                    "border:1 solid black;\n" +
                    "\n" +
                    "}\n" +
                    "th{\n" +
                    "background-color:#0099CC;\n" +
                    "font-size:16px;\n" +
                    "}\n" +
                    "</style>" +
                    "<Title>Extracted Results</title>" +
                    "</head><body>\n").getBytes());

            /**
             * Throughput Table For All Topologies
             */
            output.write(("<br><h1>Net Throughput</h1><br>").getBytes());
            output.write(("<table border=\"1\"><tr >" +
                    "<th>Avg Msg Production Rate</th>" +
                    "<th>Fat Tree</th>" +
                    "<th>Mesh</th>" +
                    "<th>Torus</th>" +
                    "<th>Extended Fat Tree</th>" +
                    "<th>Octal</th>" +
                    "<th>WK-Recursive</th>" +
                    "</tr>\n<tr>").getBytes());
            for (int i = 0; i < 8; i++) {

                output.write(("<td>" + resultArray[i][0].get("Avg Msg Production Rate") + "</td>").getBytes());
                for (int j = 0; j < 6; j++) {
                    output.write((
                            "<td>" + resultArray[i][j].get("Throughput[Net]") + "</td>").getBytes());
                }
                output.write("</tr>".getBytes());
            }
            output.write("</table><br>".getBytes());

            /**
             * Switch Throughput Table For All Topologies
             */
            output.write(("<br><h1>Switch Throughput</h1><br>").getBytes());
            output.write(("<table border=\"1\"><tr >" +
                    "<th>Avg Msg Production Rate</th>" +
                    "<th>Fat Tree</th>" +
                    "<th>Mesh</th>" +
                    "<th>Torus</th>" +
                    "<th>Extended Fat Tree</th>" +
                    "<th>Octal</th>" +
                    "<th>WK-Recursive</th>" +
                    "</tr>\n<tr>").getBytes());
            for (int i = 0; i < 8; i++) {

                output.write(("<td>" + resultArray[i][0].get("Avg Msg Production Rate") + "</td>").getBytes());
                for (int j = 0; j < 6; j++) {
                    output.write((
                            "<td>" + resultArray[i][j].get("Throughput[Flits leaving network.unit.switches.Switch]") + "</td>").getBytes());
                }
                output.write("</tr>".getBytes());
            }
            output.write("</table><br>".getBytes());

            /**
             * Average Packet Delay Table For All Topologies
             */
            output.write(("<br><h1>Average Packet Delay</h1><br>").getBytes());
            output.write(("<table border=\"1\"><tr >" +
                    "<th>Avg Msg Production Rate</th>" +
                    "<th>Fat Tree</th>" +
                    "<th>Mesh</th>" +
                    "<th>Torus</th>" +
                    "<th>Extended Fat Tree</th>" +
                    "<th>Octal</th>" +
                    "<th>WK-Recursive</th>" +
                    "</tr>\n<tr>").getBytes());
            for (int i = 0; i < 8; i++) {

                output.write(("<td>" + resultArray[i][0].get("Avg Msg Production Rate") + "</td>").getBytes());
                for (int j = 0; j < 6; j++) {
                    output.write((
                            "<td>" + resultArray[i][j].get("Avg Packet Delay") + "</td>").getBytes());
                }
                output.write("</tr>".getBytes());
            }
            output.write("</table><br>".getBytes());

            /**
             * Link Utilization Table For All Topologies
             */
            output.write(("<br><h1>Link Utilization</h1><br>").getBytes());
            output.write(("<table border=\"1\"><tr >" +
                    "<th>Avg Msg Production Rate</th>" +
                    "<th>Fat Tree</th>" +
                    "<th>Mesh</th>" +
                    "<th>Torus</th>" +
                    "<th>Extended Fat Tree</th>" +
                    "<th>Octal</th>" +
                    "<th>WK-Recursive</th>" +
                    "</tr>\n<tr>").getBytes());
            for (int i = 0; i < 8; i++) {

                output.write(("<td>" + resultArray[i][0].get("Avg Msg Production Rate") + "</td>").getBytes());
                for (int j = 0; j < 6; j++) {
                    output.write((
                            "<td>" + resultArray[i][j].get("Link Utilization") + "</td>").getBytes());
                }
                output.write("</tr>".getBytes());
            }
            output.write("</table><br>".getBytes());

            /**
             * Utilization Utilization Table For All Topologies
             */
            output.write(("<br><h1>Buffer Utilization</h1><br>").getBytes());
            output.write(("<table border=\"1\"><tr >" +
                    "<th>Avg Msg Production Rate</th>" +
                    "<th>Fat Tree</th>" +
                    "<th>Mesh</th>" +
                    "<th>Torus</th>" +
                    "<th>Extended Fat Tree</th>" +
                    "<th>Octal</th>" +
                    "<th>WK-Recursive</th>" +
                    "</tr>\n<tr>").getBytes());
            for (int i = 0; i < 8; i++) {

                output.write(("<td>" + resultArray[i][0].get("Avg Msg Production Rate") + "</td>").getBytes());
                for (int j = 0; j < 6; j++) {
                    output.write((
                            "<td>" + resultArray[i][j].get("Buffer(network.unit.node.Node & network.unit.switches.Switch) Utilization") + "</td>").getBytes());
                }
                output.write("</tr>".getBytes());
            }
            output.write("</table><br>".getBytes());

            /**
             * Avg Packet Injection Rate Table For All Topologies
             */
            output.write(("<br><h1>Avg Packet Injection Rate</h1><br>").getBytes());
            output.write(("<table border=\"1\"><tr >" +
                    "<th>Avg Msg Production Rate</th>" +
                    "<th>Fat Tree</th>" +
                    "<th>Mesh</th>" +
                    "<th>Torus</th>" +
                    "<th>Extended Fat Tree</th>" +
                    "<th>Octal</th>" +
                    "<th>WK-Recursive</th>" +
                    "</tr>\n<tr>").getBytes());
            for (int i = 0; i < 8; i++) {

                output.write(("<td>" + resultArray[i][0].get("Avg Msg Production Rate") + "</td>").getBytes());
                for (int j = 0; j < 6; j++) {
                    output.write((
                            "<td>" + resultArray[i][j].get("Avg Packet Injection Rate") + "</td>").getBytes());
                }
                output.write("</tr>".getBytes());
            }
            output.write("</table><br>".getBytes());

            /**
             * Avg Packet Not Produced Table For All Topologies
             */
            output.write(("<br><h1>Avg Packet Not Produced</h1><br>").getBytes());
            output.write(("<table border=\"1\"><tr >" +
                    "<th>Avg Msg Production Rate</th>" +
                    "<th>Fat Tree</th>" +
                    "<th>Mesh</th>" +
                    "<th>Torus</th>" +
                    "<th>Extended Fat Tree</th>" +
                    "<th>Octal</th>" +
                    "<th>WK-Recursive</th>" +
                    "</tr>\n<tr>").getBytes());
            for (int i = 0; i < 8; i++) {

                output.write(("<td>" + resultArray[i][0].get("Avg Msg Production Rate") + "</td>").getBytes());
                for (int j = 0; j < 6; j++) {
                    output.write((
                            "<td>" + resultArray[i][j].get("Avg Packet Not Produced") + "</td>").getBytes());
                }
                output.write("</tr>".getBytes());
            }
            output.write("</table><br>".getBytes());

            /**
             * Avg Hop Count Table For All Topologies
             */
            output.write(("<br><h1>Avg Hop Count</h1><br>").getBytes());
            output.write(("<table border=\"1\"><tr >" +
                    "<th>Avg Msg Production Rate</th>" +
                    "<th>Fat Tree</th>" +
                    "<th>Mesh</th>" +
                    "<th>Torus</th>" +
                    "<th>Extended Fat Tree</th>" +
                    "<th>Octal</th>" +
                    "<th>WK-Recursive</th>" +
                    "</tr>\n<tr>").getBytes());
            for (int i = 0; i < 8; i++) {

                output.write(("<td>" + resultArray[i][0].get("Avg Msg Production Rate") + "</td>").getBytes());
                for (int j = 0; j < 6; j++) {
                    output.write((
                            "<td>" + resultArray[i][j].get("Avg Hop Count") + "</td>").getBytes());
                }
                output.write("</tr>".getBytes());
            }
            output.write("</table><br>".getBytes());


            output.write("</body>".getBytes());
            //System.out.print("Success to write html file");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void createResultHTMLBasedOnTopology(HashMap[][] resultArray) {
        try {
            HelpingUtility helpingUtility = new HelpingUtility();
            String dirPath = helpingUtility.getFilePath("results");
            File dirFile = new File(dirPath);
            if(!dirFile.isDirectory()){
                dirFile.mkdir();
            }

            File htmlFile = new File(helpingUtility.getFilePath("results/extracted-results-topology.html"));

            if (!htmlFile.exists()) {
                htmlFile.createNewFile();
            }
            DataOutput output = new DataOutputStream(new FileOutputStream(htmlFile));
            output.write(("<html><head><style>\n" +
                    "body{\n" +
                    "font-size:14;\n" +
                    "font-family:tahoma;\n" +
                    "}\n" +
                    "table{\n" +
                    "border:1 solid black;\n" +
                    "\n" +
                    "}\n" +
                    "th{\n" +
                    "background-color:#0099CC;\n" +
                    "font-size:16px;\n" +
                    "}\n" +
                    "</style>" +
                    "<Title>Extracted Results</title>" +
                    "</head><body>\n").getBytes());
            for (int i = 0; i < 6; i++) {
                String netName = "";
                if (i == 0) {
                    netName = "Fat Tree";
                } else if (i == 1) {
                    netName = "Mesh";
                } else if (i == 2) {
                    netName = "Torus";
                } else if (i == 3) {
                    netName = "Extended Fat Tree";
                } else if (i == 4) {
                    netName = "Octal";
                } else if (i == 5) {
                    netName = "WK-Recursive";
                }
                output.write(("<br><h1> " + netName + "</h1><br>").getBytes());
                output.write(("<table border=\"1\"><tr >" +
                        "<th>Avg Msg Production Rate</th>" +
                        "<th>Net Throughput</th>" +
                        "<th>Switch Throughput</th>" +
                        "<th>Avg Packet Delay</th>" +
                        "<th>Link Utilization</th>" +
                        "<th>Buffer Utilization</th>" +
                        "<th>Avg Packet Injection Rate</th>" +
                        "<th>Avg Packet Not Produced</th>" +
                        "<th>Avg Hop Count" + "</th></tr>\n").getBytes());
                for (int j = 0; j < 8; j++) {

                    output.write(("<tr>" +
                            "<td>" + resultArray[j][i].get("Avg Msg Production Rate") + "</td>" +
                            "<td class=\"trafficType\">" + resultArray[j][i].get("Throughput[Net]") + "</td>" +
                            "<td>" + resultArray[j][i].get("Throughput[Flits leaving network.unit.switches.Switch]") + "</td>" +
                            "<td>" + resultArray[j][i].get("Avg Packet Delay") + "</td>" +
                            "<td>" + resultArray[j][i].get("Link Utilization") + "</td>" +
                            "<td>" + resultArray[j][i].get("Buffer(network.unit.node.Node & network.unit.switches.Switch) Utilization") + "</td>" +
                            "<td>" + resultArray[j][i].get("Avg Packet Injection Rate") + "</td>" +
                            "<td>" + resultArray[j][i].get("Avg Packet Not Produced") + "</td>" +
                            "<td>" + resultArray[j][i].get("Avg Hop Count") + "</td>" +
                            "</tr>").getBytes());
                }
                output.write("</table><br>".getBytes());
            }
            output.write("</body>".getBytes());
            //System.out.print("Success to write html file");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void createResultHTMLBasedOnTrafficRate(HashMap[][] resultArray) {
        try {
            HelpingUtility helpingUtility = new HelpingUtility();
            String dirPath = helpingUtility.getFilePath("results");
            File dirFile = new File(dirPath);
            if(!dirFile.isDirectory()){
                dirFile.mkdir();
            }
            File htmlFile = new File(helpingUtility.getFilePath("results/extracted-results-traffic-rate.html"));
            if (!htmlFile.exists()) {
                htmlFile.createNewFile();
            }
            DataOutput output = new DataOutputStream(new FileOutputStream(htmlFile));
            output.write(("<html><head><style>\n" +
                    "body{\n" +
                    "font-size:14;\n" +
                    "font-family:tahoma;\n" +
                    "}\n" +
                    "table{\n" +
                    "border:1 solid black;\n" +
                    "\n" +
                    "}\n" +
                    "th{\n" +
                    "background-color:#0099CC;\n" +
                    "font-size:16px;\n" +
                    "}\n" +
                    "</style>" +
                    "<Title>Extracted Results</title>" +
                    "</head><body>\n").getBytes());
            for (int i = 0; i < 8; i++) {
                output.write(("<br><h1>traffic rate " + resultArray[i][0].get("Avg Msg Production Rate") + "</h1><br>").getBytes());
                output.write(("<table border=\"1\"><tr >" +
                        "<th>Topology</th>" +
                        "<th>Net Throughput</th>" +
                        "<th>Switch Throughput</th>" +
                        "<th>Avg Packet Delay</th>" +
                        "<th>Link Utilization</th>" +
                        "<th>Buffer Utilization</th>" +
                        "<th>Avg Packet Injection Rate</th>" +
                        "<th>Avg Packet Not Produced</th>" +
                        "<th>Avg Hop Count" + "</th></tr>\n").getBytes());
                for (int j = 0; j < 6; j++) {
                    String netName = "";
                    if (j == 0) {
                        netName = "Fat Tree";
                    } else if (j == 1) {
                        netName = "Mesh";
                    } else if (j == 2) {
                        netName = "Torus";
                    } else if (j == 3) {
                        netName = "Extended Fat Tree";
                    } else if (j == 4) {
                        netName = "Octal";
                    } else if (j == 5) {
                        netName = "WK-Recursive";
                    }

                    output.write(("<tr>" +
                            "<td>" + netName + "</td>" +
                            "<td class=\"trafficType\">" + resultArray[i][j].get("Throughput[Net]") + "</td>" +
                            "<td>" + resultArray[i][j].get("Throughput[Flits leaving network.unit.switches.Switch]") + "</td>" +
                            "<td>" + resultArray[i][j].get("Avg Packet Delay") + "</td>" +
                            "<td>" + resultArray[i][j].get("Link Utilization") + "</td>" +
                            "<td>" + resultArray[i][j].get("Buffer(network.unit.node.Node & network.unit.switches.Switch) Utilization") + "</td>" +
                            "<td>" + resultArray[i][j].get("Avg Packet Injection Rate") + "</td>" +
                            "<td>" + resultArray[i][j].get("Avg Packet Not Produced") + "</td>" +
                            "<td>" + resultArray[i][j].get("Avg Hop Count") + "</td>" +
                            "</tr>").getBytes());
                }
                output.write("</table><br>".getBytes());
            }
            output.write("</body>".getBytes());
            //System.out.print("Success to write html file");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void main(String[] args) {
        FileExtractor f = new FileExtractor();
        f.resultFileReader();
    }
}
