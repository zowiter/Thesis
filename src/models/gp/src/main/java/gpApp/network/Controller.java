package gpApp.network;

import gpApp.gui.GpNoCSimRunable;
import gpApp.network.traffic.ConcreteNodeTraffic;
import gpApp.network.common.IConstants;
import gpApp.network.common.HelpingUtility;

import java.io.*;

/**
 * The network.Controller class dictates the total flow of execution of the simulator.
 *
 * @version 1.0
 */
public class Controller {

    /**
     * This is an object of
     * <p/>
     * {@link NetworkManager} type which holds the current network instance,
     * statistical data holder and various helping utility object like input
     * file reader.
     */
    private NetworkManager netManager = null;

    /**
     * This is the reference of the instaniated
     * <p/>
     * {@link Network} in {@link NetworkManager}. It is used to invoke various
     * methods of the network.
     */
    private Network network = null;

    /**
     * Default Constructor
     */
    public Controller() {
    }

    /*
      * private void initialize (int netType) { network =
      * network.NetworkManager.getNetworkInstance() ; network.setInitalEvents() ; }
      */

    // main loop

    /**
     * The controller object performs its duty mainly by this method. The method
     * performs in the following steps.
     * <p/>
     * <ul>
     * <li>i. Gets the network manager object by invoking the static
     * getInstance() method of the network.NetworkManager class.</li>
     * <li>ii. Determines if there is any other network configuration in the
     * input file for simulation through network manager. If there exists one
     * then create that network and perform the subsequent steps otherwise
     * terminate the simulation process.</li>
     * <li>iii. For every simulation run of the same network (simulation is
     * performed several times, let 10 times, for same network configuration to
     * get actual statistical scenario) performs the following operations.
     * <ul>
     * <li>?Initializes the network.</li>
     * <li>?Sets random seed for various random data.</li>
     * <li>Initializes the statistical data object.</li>
     * <li>Sets initial events of the network</li>
     * <li>?For every simulation cycle (one simulation run is complete after
     * huge number of cycles, let 10 million, found from input file) performs
     * the following operations.
     * <ul>
     * <li>Moves outgoing traffic those can be moved from every node to
     * adjacent switches.</li>
     * <li>Updates path requests of the incoming traffic of the switches.</li>
     * <li>Moves incoming traffics those can be moved from input buffer to
     * output buffer of the switches.</li>
     * <li>Moves outgoing traffics those can be moved from output buffer of the
     * switch to the input buffer of the adjacent switches and/or switches</li>
     * <li>Moves incoming traffic those can be moved from the input buffer of
     * the resource node to the message center of that node.</li>
     * <li>Performs any clerical job required (change of various status and
     * flags) to do at the end of the cycle</li>
     * </ul>
     * </li>
     * <li>Updates the statistical parameter for this simulation run.</li>
     * </ul>
     * </li>
     * <li>iv. Calculates the average of the statistical parameters found in
     * the various simulation runs for the same network configuration. And then
     * writes the various values of the statistical parameters and network
     * configuration in the output file.</li>
     * </ul>
     */
    public void main_loop(String inputFile, String outputFile) {
        int i, run;
        boolean toGo;
        double nodeThroughput, throughput, avgDelay, avgPktNotProduced, linkUtil, packetRate;
        double inBufUtilNode, outBufUtilNode, inBufUtilSwitch, outBufUtilSwitch, totalBufUtil;
        double packetHopCount;
        double packetSent, packetReceived;

        netManager = NetworkManager.getInstance(inputFile);
        toGo = netManager.createNextNetwork();

        System.out.println("ToGo: " + toGo);
        while (toGo) {
            if (IConstants.TRACE) {
                try {
                    RandomAccessFile raf = new RandomAccessFile(
                            new HelpingUtility().getFilePath(IConstants.TRACE_FILE), "rw");
                    raf.seek(raf.length());
                    raf
                            .writeBytes("\n\n\nStarting Trace Info for the network.Network\n\n");
                    String str = "";
                    str += "  network.Network\t" + IConstants.CURRENT_NET;
                    str += "\n  Number of IP\t" + IConstants.NUMBER_OF_IP_NODE;
                    str += "\n  Avg Msg Length(bytes)\t"
                            + IConstants.AVG_MESSAGE_LENGTH;
                    str += "\n  network.traffic.Flit Length(bits)\t" + IConstants.FLIT_LENGTH;
                    str += "\n  Avg Msg Production Rate(cycle/msg)\t"
                            + IConstants.AVG_INTER_ARRIVAL;
                    str += "\n  Number of Virtual Channel/Link\t"
                            + IConstants.CURRENT_VC_COUNT;
                    str += "\n  Number of network.traffic.Flit/Buffer\t"
                            + IConstants.NUM_FLIT_PER_BUFFER;
                    str += "\n  Number of Cycles\t" + IConstants.NUM_CYCLE;
                    raf.writeBytes(str + "\n\n");
                    raf.close();
                } catch (IOException ioex) {
                }
            }

            NetworkManager.getHelpingUtility().setRandomSeed();

            // //////////////////////////////////////////////////////////////////
            // Initialization of the performance parameters for each network ///
            // //////////////////////////////////////////////////////////////////

            nodeThroughput = throughput = avgDelay = avgPktNotProduced = linkUtil = packetRate = 0;
            inBufUtilNode = outBufUtilNode = inBufUtilSwitch = outBufUtilSwitch = totalBufUtil = 0;
            packetHopCount = 0;
            packetSent = packetReceived = 0;

            for (run = 0; run < IConstants.NUM_RUN; run++) {
                if (IConstants.TRACE) {
                    try {
                        RandomAccessFile raf = new RandomAccessFile(
                                IConstants.TRACE_FILE, "rw");
                        raf.seek(raf.length());
                        raf.writeBytes("\n\n  For run=" + run + "\n");
                        raf.close();
                    } catch (IOException ioex) {
                    }
                }

                // ///////////////////////////////////////////////////////////
                // Initialization at every run ///
                // ///////////////////////////////////////////////////////////
                NetworkManager.getInstance(inputFile).initializeNetwork();
                network = NetworkManager.getNetworkInstance();
                NetworkManager.getHelpingUtility().setRandomSeed();
                NetworkManager.getStatDataInstance().initializeStat();
                network.setInitalEvents();
                for (i = 0; i < IConstants.NUM_CYCLE + IConstants.WARM_UP_CYCLE; i++) {
                    // ///////////////////////////////////////////////////////////
                    // Operations at each cycle ///
                    // ///////////////////////////////////////////////////////////
                    network.moveNodeTrafficFromNodeToSwitch(i);
                    network.updateSwitchTrafficPathRequest(i);
                    network.moveSwitchTrafficFromInputBufferToOutputBuffer(i);
                    network.moveSwitchTrafficFromOutputBufferToInputBufferOfNodeSwitch(i);
                    network.moveNodeTrafficFromInputBufferToNodeMsgCenter(i);
                    network.updateAfterCycleStatus(i);
                }

                // ///////////////////////////////////////////////////////////
                // Updates the performance parameters after each run ///
                // ///////////////////////////////////////////////////////////

                nodeThroughput += NetworkManager.getStatDataInstance()
                        .getNetworkNodeThroughput();
                throughput += NetworkManager.getStatDataInstance()
                        .getThroughput(IConstants.NUM_CYCLE);
                avgDelay += NetworkManager.getStatDataInstance()
                        .getAvgPacketDelay();
                avgPktNotProduced += NetworkManager.getStatDataInstance()
                        .getAvgMessageNotProduced();
                linkUtil += NetworkManager.getStatDataInstance()
                        .getLinkUtilization();
                packetRate += (double) NetworkManager.getStatDataInstance()
                        .getNumberOfPacketProduced()
                        / IConstants.NUM_CYCLE;

                packetHopCount += NetworkManager.getStatDataInstance()
                        .getAvgPacketHopCount();

                inBufUtilNode += NetworkManager.getStatDataInstance()
                        .getInputBufferNodeUtilization();
                outBufUtilNode += NetworkManager.getStatDataInstance()
                        .getOutputBufferNodeUtilization();
                inBufUtilSwitch += NetworkManager.getStatDataInstance()
                        .getInputBufferSwitchUtilization();
                outBufUtilSwitch += NetworkManager.getStatDataInstance()
                        .getOutputBufferSwitchUtilization();
                totalBufUtil += NetworkManager.getStatDataInstance()
                        .getBufferUtilization();

                packetSent += NetworkManager.getStatDataInstance()
                        .getNumberOfPacketSent();
                packetReceived += NetworkManager.getStatDataInstance()
                        .getNumberOfPacketReceived();
            }

            // ///////////////////////////////////////////////////////////////////////////////////////////////
            // Calculates the average of the statistical parameters found in the
            // various simulation runs for//
            // the same network configuration. //
            // ///////////////////////////////////////////////////////////////////////////////////////////////
            nodeThroughput /= run;
            throughput /= run;
            avgDelay /= run;
            avgPktNotProduced /= run;
            linkUtil /= run;
            packetRate /= run;

            inBufUtilNode /= run;
            outBufUtilNode /= run;
            inBufUtilSwitch /= run;
            outBufUtilSwitch /= run;
            totalBufUtil /= run;

            packetHopCount /= run;
            packetSent /= run;
            packetReceived /= run;

            try {
                // ///////////////////////////////////////////////////////////////////////////////////////////////////////
                // Writes the various values of the statistical parameters and
                // network configuration in the output file.//
                // ///////////////////////////////////////////////////////////////////////////////////////////////////////

                String Str = new String("");

                Str += "Performance Measurements.......";

                Str += "\n  Throughput[Net] " + nodeThroughput;
                Str += "\n  Throughput[Flits leaving network.unit.switches.Switch] " + throughput;
                Str += "\n  Avg Packet Delay " + avgDelay;
                Str += "\n  Link Utilization " + linkUtil;
                Str += "\n  Buffer(network.unit.node.Node & network.unit.switches.Switch) Utilization " + totalBufUtil;
                Str += "\n  Avg Packet Injection Rate " + packetRate;
                Str += "\n  Avg Packet Not Produced " + avgPktNotProduced;
                Str += "\n  Input Buffer(network.unit.node.Node) Utilization " + inBufUtilNode;
                Str += "\n  Input Buffer(network.unit.switches.Switch) Utilization "
                        + inBufUtilSwitch;
                Str += "\n  Output Buffer(network.unit.node.Node) Utilization " + outBufUtilNode;
                Str += "\n  Output Buffer(network.unit.switches.Switch) Utilization "
                        + outBufUtilSwitch;
                Str += "\n  Avg Hop Count " + packetHopCount;
                Str += "\n  Avg Packet Sent/Run " + packetSent;
                Str += "\n  Avg Packet Received/Run " + packetReceived;
                Str += "\n\n\n";

                Str += "\n  Total LOCA_IN = " + ConcreteNodeTraffic.LOCAL_IN;
                Str += "\n  Total LOCA_OUT = " + ConcreteNodeTraffic.LOCAL_OUT;
                Str += "\n  Total LOCA_IN Percentage = "
                        + ConcreteNodeTraffic.LOCAL_IN
                        / (ConcreteNodeTraffic.LOCAL_IN + (double) ConcreteNodeTraffic.LOCAL_OUT);
                Str += "\n\n\n";
                System.out.println(Str);

                File jarDir = new File(GpNoCSimRunable.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                String jarDirpath = jarDir.getParent();

                FileOutputStream outputStream = new FileOutputStream(outputFile);
                byte[] strToBytes = Str.getBytes();
                outputStream.write(strToBytes);

                outputStream.close();


                /*
                File f = new File(Controller.class.getResource(IConstants.OUT_FILE).getFile());
                DataOutputStream out = new DataOutputStream(new FileOutputStream(f));
                out.write(Str.getBytes());

                 */
                /*RandomAccessFile raf = new RandomAccessFile(
                        IConstants.OUT_FILE, "rw");
                raf.seek(raf.length());
                raf.writeBytes(Str);                
                raf.close();
                 */
            } catch (IOException e) {
                e.printStackTrace();
            }

            toGo = netManager.createNextNetwork();
        }

        System.out.println("Simulation Complete");
    }

}