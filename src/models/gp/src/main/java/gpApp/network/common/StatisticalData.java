package gpApp.network.common;

/**
 * <p/>
 * network.common.StatisticalData class captures various performance parameters of the
 * simulator for comparison among various network configurations. The data
 * presented by this class are updated only during the warm up cycles.
 * </p>
 * <p/>
 * <p/>
 * Performance parameters are <b>Latency</b>, <b>Throughput</b>, <b>Buffer
 * Utilization</b>, <b>Link Utilization</b>, <b>Hop Count</b> (Routing
 * Performance).
 * </p>
 *
 * @version 1.0
 */

public class StatisticalData {
    /**
     * Number of switches in the network
     */
    private int noOfSwitch;

    /**
     * An array capturing the number of flits leaving from each switch
     */
    private long flitLeavingFromSwitch[];

    /**
     * An array capturing the delay of the packets received in each node
     */
    private long packetDelay[];

    /**
     * An array capturing the number of the packets generated in each node
     */
    private long packetProduced[];

    /**
     * An array capturing the number of the packets received in each node
     */
    private long packetReceived[];

    /**
     * An array capturing the number of the packets <b>NOT</b> generated in
     * each node, due to buffer shortage
     */
    private long messageNotProduced[];

    /**
     * An array capturing the number of the packets actually transmitted from
     * each node
     */
    private long packetSent[];

    /**
     * An array capturing the number of the flits received from each node
     */
    private long flitReceived[];

    /**
     * An array capturing the physical link usage status for each switch
     */
    private long switchLinkUseStatus[];

    /**
     * An array capturing the number of physical links in each switch
     */
    private long numSwitchLink[];

    /**
     * An array capturing the physical link usage status for each node
     */
    private long nodeLinkUseStatus[];

    /**
     * An array capturing the usage status of the slots of the input buffer for
     * each switch
     */
    private long switchInBufferUseStatus[];

    /**
     * An array capturing the usage status of the slots of the output buffer
     * for each switch
     */
    private long switchOutBufferUseStatus[];

    /**
     * An array capturing the usage status of the slots of the input buffer for
     * each node
     */
    private long nodeInBufferUseStatus[];

    /**
     * An array capturing the usage status of the slots of the output buffer for
     * each node
     */
    private long nodeOutBufferUseStatus[];

    /**
     * An array capturing the hop traversed by the packets received in each node
     */
    private long[] packetHopCount;

    // / debug purpose
    public int sameUnit = 0;

    /**
     * Constructor of the network.common.StatisticalData class. Initializes the member
     * variables.
     *
     * @param netType type of the network
     */
    public StatisticalData(int netType) {
        int i;
        messageNotProduced = new long[IConstants.NUMBER_OF_IP_NODE];
        packetDelay = new long[IConstants.NUMBER_OF_IP_NODE];
        packetProduced = new long[IConstants.NUMBER_OF_IP_NODE];
        packetReceived = new long[IConstants.NUMBER_OF_IP_NODE];
        packetSent = new long[IConstants.NUMBER_OF_IP_NODE];
        nodeLinkUseStatus = new long[IConstants.NUMBER_OF_IP_NODE];
        nodeInBufferUseStatus = new long[IConstants.NUMBER_OF_IP_NODE];
        nodeOutBufferUseStatus = new long[IConstants.NUMBER_OF_IP_NODE];
        flitReceived = new long[IConstants.NUMBER_OF_IP_NODE];
        packetHopCount = new long[IConstants.NUMBER_OF_IP_NODE];

        for (i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            messageNotProduced[i] = 0;
            packetDelay[i] = 0;
            packetProduced[i] = 0;
            packetReceived[i] = 0;
            packetSent[i] = 0;
            nodeLinkUseStatus[i] = 0;
            nodeInBufferUseStatus[i] = 0;
            nodeOutBufferUseStatus[i] = 0;
            flitReceived[i] = 0;
            packetHopCount[i] = 0;
        }

        /*
           * if (network.common.IConstants.CURRENT_NET == network.common.IConstants.NET_MESH ||
           * network.common.IConstants.CURRENT_NET == network.common.IConstants.NET_TORUS) { noOfSwitch =
           * network.common.IConstants.NUMBER_OF_IP_NODE / network.common.IConstants.MESH_ADJ_NODE ; } else if
           * (network.common.IConstants.CURRENT_NET == network.common.IConstants.NET_FAT_TREE ||
           * network.common.IConstants.CURRENT_NET == network.common.IConstants.NET_EX_FAT_TREE) { noOfSwitch =
           * network.common.IConstants.FAT_NUM_SWITCH ; } else if (network.common.IConstants.CURRENT_NET ==
           * network.common.IConstants.NET_OCTAL) { noOfSwitch = network.common.IConstants.NUMBER_OF_IP_NODE /
           * network.common.IConstants.OCTAL_ADJ_NODE ; }
           */
        noOfSwitch = IConstants.NUMBER_OF_SWITCH;

        flitLeavingFromSwitch = new long[noOfSwitch];
        switchLinkUseStatus = new long[noOfSwitch];
        numSwitchLink = new long[noOfSwitch];
        switchInBufferUseStatus = new long[noOfSwitch];
        switchOutBufferUseStatus = new long[noOfSwitch];
        for (i = 0; i < noOfSwitch; i++) {
            flitLeavingFromSwitch[i] = 0;
            switchLinkUseStatus[i] = 0;
            numSwitchLink[i] = 0;
            switchInBufferUseStatus[i] = 0;
            switchOutBufferUseStatus[i] = 0;
        }

    }

    /**
     * Re-initializes the member variables. This method is useful if the
     * simulator is executed to compute performance for more than one network in
     * a single execution.
     */
    public void initializeStat() {
        int i;
        messageNotProduced = new long[IConstants.NUMBER_OF_IP_NODE];
        packetDelay = new long[IConstants.NUMBER_OF_IP_NODE];
        packetProduced = new long[IConstants.NUMBER_OF_IP_NODE];
        packetReceived = new long[IConstants.NUMBER_OF_IP_NODE];
        packetSent = new long[IConstants.NUMBER_OF_IP_NODE];
        nodeLinkUseStatus = new long[IConstants.NUMBER_OF_IP_NODE];
        nodeInBufferUseStatus = new long[IConstants.NUMBER_OF_IP_NODE];
        nodeOutBufferUseStatus = new long[IConstants.NUMBER_OF_IP_NODE];
        flitReceived = new long[IConstants.NUMBER_OF_IP_NODE];
        packetHopCount = new long[IConstants.NUMBER_OF_IP_NODE];

        for (i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            messageNotProduced[i] = 0;
            packetDelay[i] = 0;
            packetProduced[i] = 0;
            packetReceived[i] = 0;
            packetSent[i] = 0;
            nodeLinkUseStatus[i] = 0;
            nodeInBufferUseStatus[i] = 0;
            nodeOutBufferUseStatus[i] = 0;
            flitReceived[i] = 0;
            packetHopCount[i] = 0;
        }

        /*
           * if (network.common.IConstants.CURRENT_NET == network.common.IConstants.NET_MESH ||
           * network.common.IConstants.CURRENT_NET == network.common.IConstants.NET_TORUS) { noOfSwitch =
           * network.common.IConstants.NUMBER_OF_IP_NODE / network.common.IConstants.MESH_ADJ_NODE ; } else if
           * (network.common.IConstants.CURRENT_NET == network.common.IConstants.NET_FAT_TREE ||
           * network.common.IConstants.CURRENT_NET == network.common.IConstants.NET_EX_FAT_TREE ) { noOfSwitch =
           * network.common.IConstants.FAT_NUM_SWITCH ; } else if (network.common.IConstants.CURRENT_NET ==
           * network.common.IConstants.NET_OCTAL) { noOfSwitch = network.common.IConstants.NUMBER_OF_IP_NODE /
           * network.common.IConstants.OCTAL_ADJ_NODE ; }
           */
        noOfSwitch = IConstants.NUMBER_OF_SWITCH;

        flitLeavingFromSwitch = new long[noOfSwitch];
        switchLinkUseStatus = new long[noOfSwitch];
        numSwitchLink = new long[noOfSwitch];
        switchInBufferUseStatus = new long[noOfSwitch];
        switchOutBufferUseStatus = new long[noOfSwitch];

        for (i = 0; i < noOfSwitch; i++) {
            flitLeavingFromSwitch[i] = 0;
            switchLinkUseStatus[i] = 0;
            numSwitchLink[i] = 0;
            switchInBufferUseStatus[i] = 0;
            switchOutBufferUseStatus[i] = 0;
        }
    }

    /**
     * Increments the number of flits received in a particular node.
     *
     * @param nodeIndex index of the IP node
     */
    public void incrementFlitReceived(int nodeIndex) {
        flitReceived[nodeIndex]++;
    }

    /**
     * Increments the number of packets that could not be generated in a
     * particular node due to the shortage of internal buffer.
     *
     * @param nodeIndex index of the IP node
     */
    public void incrementMessageNotProduced(int nodeIndex) {
        messageNotProduced[nodeIndex]++;
    }

    /**
     * Increments the number of flits moving to an adjacent node/switch from a
     * particular switch.
     *
     * @param switchIndex index of the switch
     */
    public void incrementFlitLeavingFromSwitch(int switchIndex) {
        flitLeavingFromSwitch[switchIndex]++;
    }

    /**
     * Increments the total hop count traversed by all packets received in a
     * particular node by an amount specified in the arguement 'hop'.
     *
     * @param nodeIndex index of the IP node
     * @param hop       hop count
     */
    public void incrementPacketHopCount(int nodeIndex, int hop) {
        packetHopCount[nodeIndex] += hop;
    }

    /**
     * Increments the total delay experienced by all packets received in a
     * particular node by an amount specified in the arguement 'delay'.
     *
     * @param nodeIndex index of the IP node
     * @param delay     delay of the packet to reach the destination
     */
    public void incrementPacketDelay(int nodeIndex, int delay) {
        packetDelay[nodeIndex] += delay;
        packetReceived[nodeIndex]++;
    }

    /**
     * Increments the number of packets sent from a particular node.
     *
     * @param nodeIndex index of the IP node
     */
    public void incrementPacketSent(int nodeIndex) {
        packetSent[nodeIndex]++;
    }

    /**
     * Increments the number of packets generated from a particular node.
     *
     * @param nodeIndex index of the IP node
     */
    public void incrementPacketProduced(int nodeIndex) {
        packetProduced[nodeIndex]++;
    }

    /**
     * Increments the number of total link usages of a particular switch by an
     * amount 'useCount'.
     *
     * @param switchIndex index of the switch
     * @param useCount    the number of links used to transmit packets in a single cycle
     */

    public void incrementSwitchLinkUse(int switchIndex, int useCount) {
        switchLinkUseStatus[switchIndex] += useCount;
    }

    /**
     * Sets the number of physical links for a particular switch.
     *
     * @param switchIndex index of the switch
     * @param count       number of physical linsk
     */
    public void setSwitchNumLink(int switchIndex, int count) {
        numSwitchLink[switchIndex] = count;
    }

    /**
     * Increments the count of usage of physical link of a node, if it transmits
     * flit in a particular cycle.
     *
     * @param nodeIndex index of the IP node
     */
    // node's link used or not
    public void incrementNodeLinkUse(int nodeIndex) {
        nodeLinkUseStatus[nodeIndex]++;
    }

    /**
     * Increments the count of usage of input buffer of a switch by an amount
     * 'useCount'
     *
     * @param switchIndex index of the switch
     * @param useCount    the number of slots of the input buffer occupied by various
     *                    flits.
     */
    // how many slots of input buffer of a switch is used
    public void incrementSwitchInputBufferUse(int switchIndex, int useCount) {
        switchInBufferUseStatus[switchIndex] += useCount;
    }

    /**
     * Increments the count of usage of output buffer of a switch by an amount
     * 'useCount'
     *
     * @param switchIndex index of the switch
     * @param useCount    the number of slots of the output buffer occupied by various
     *                    flits.
     */
    // how many slots of output buffer of a switch is used
    public void incrementSwitchOutputBufferUse(int switchIndex, int useCount) {
        switchOutBufferUseStatus[switchIndex] += useCount;
    }

    /**
     * Increments the count of usage of input buffer of a node by an amount
     * 'useCount'
     *
     * @param nodeIndex index of the IP node
     * @param useCount  the number of slots of the input buffer occupied by various
     *                  flits.
     */
    // how many slots of input buffer of a node is used
    public void incrementNodeInputBufferUse(int nodeIndex, int useCount) {
        nodeInBufferUseStatus[nodeIndex] += useCount;
    }

    /**
     * Increments the count of usage of output buffer of a node by an amount
     * 'useCount'
     *
     * @param nodeIndex index of the IP node
     * @param useCount  the number of slots of the output buffer occupied by various
     *                  flits.
     */
    // how many slots of output buffer of a node is used
    public void incrementNodeOutputBufferUse(int nodeIndex, int useCount) {
        nodeOutBufferUseStatus[nodeIndex] += useCount;
    }

    /**
     * <p/>
     * Returns the average number of flits leaving a switch in every cycle. This
     * is calculated by dividing the total number of flits leaving from all the
     * switches with in the total simulation cycles by the multiplication of
     * number of switches and number of simulation cycles.
     * </p>
     * <p/>
     * Don't be confused by the name of the method. It's not the exact
     * <b>Throughput</b>. See another method getNetworkNodeThroughput() in the
     * same class which supplies the original throughput.
     * </p>
     *
     * @param numCycle total number of simulation cycles
     * @return average number of flits leaving a switch in every cycle
     */
    public double getThroughput(int numCycle) {
        long temp = 0;
        for (int i = 0; i < noOfSwitch; i++) {
            temp += flitLeavingFromSwitch[i];
        }
        System.out.println("Num network.unit.switches.Switch = " + noOfSwitch);
        return (double) temp / ((double) noOfSwitch * numCycle);
    }

    /**
     * <p/>
     * Returns the latency or average packet delay.
     * </p>
     * <p/>
     * Latency is defined as the number of clock cycles required for complete
     * transfer of a packet from source node to destination on average. This
     * calculated by dividing the summation of all the packets� delay by number
     * of packets reached to the destination.
     * </p>
     *
     * @return latency or average packet delay
     */
    public double getAvgPacketDelay() {
        double temp1 = 0;
        long numSent = 0, numRecv = 0;
        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            temp1 += packetDelay[i];
            numSent += packetSent[i];
            numRecv += packetReceived[i];
            // System.out.println("network.unit.node.Node: " + i + " .. Sent: "+packetSent[i]);
            // System.out.println("network.unit.node.Node: " + i + " .. Recv: "+packetReceived[i]+
            // ".. Delay: " + packetDelay[i]/packetReceived[i]);

        }
        System.out.println("Number of Packets Sent =" + numSent);
        System.out.println("Number of Packets Received = " + numRecv);

        // System.out.println("Number of Packets Sent in the same network.unit =���� " +
        // sameUnit) ;

        return (double) temp1 / numRecv;
    }

    /**
     * <p/>
     * Returns the average hop count. This is computed by dividing the total hop
     * counts by the the number of packets received by the node.
     * </p>
     *
     * @return average number of hops traversed by the packets
     */
    public double getAvgPacketHopCount() {
        long temp1 = 0;
        long numRecv = 0;
        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            temp1 += packetHopCount[i];
            numRecv += packetReceived[i];
        }
        return (double) temp1 / numRecv;

    }

    /**
     * Returns the number of packets transmitted by all node.
     *
     * @return the number of packets sent
     */
    public double getNumberOfPacketSent() {
        double numSent = 0;
        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            numSent += packetSent[i];

        }

        return numSent;
    }

    /**
     * Returns the number of packets received by all node.
     *
     * @return the number of packets received
     */
    public double getNumberOfPacketReceived() {
        double numReceived = 0;
        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            numReceived += packetReceived[i];

        }

        return numReceived;
    }

    /**
     * Returns the number of packets <b>NOT</b> produced by all node due to
     * lack of internal buffer.
     *
     * @return the number of packets not produced
     */
    public double getNumberOfPacketProduced() {
        double numProd = 0;
        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            numProd += packetProduced[i];
            System.out.println("network.unit.node.Node: " + i + " .. produces"
                    + packetProduced[i]);
        }
        return numProd;
    }

    /**
     * Returns the average number of packets <b>NOT</b> produced by the node
     * due to lack of internal buffer.
     *
     * @return the average number of packets not produced
     */
    public double getAvgMessageNotProduced() {
        double temp1 = 0;

        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            temp1 += messageNotProduced[i];
            System.out.println("network.unit.node.Node: " + i + " .. not  produces"
                    + messageNotProduced[i]);
        }
        return temp1 / IConstants.NUMBER_OF_IP_NODE;
    }

    /**
     * <p/>
     * Returns the link utilization of all Nodes and Switches for a single
     * simulation run.
     * </p>
     * <p/>
     * <p/>
     * This is the indication of how much time the physical links are used in
     * transferring data out of its total duration. This is calculated by
     * dividing the summation of number of physical links used in flit transfer
     * in every simulation cycle by the multiplication of total number of
     * simulation cycle and total number of physical links.
     * </p>
     *
     * @return link utilization of all node and switches
     */
    public double getLinkUtilization() {
        double temp1 = 0;
        float numLink = 0;
        for (int i = 0; i < noOfSwitch; i++) {
            temp1 += switchLinkUseStatus[i];
            numLink += numSwitchLink[i];
            System.out.println("network.unit.switches.Switch: " + i + " .. link Utilization: "
                    + (double) switchLinkUseStatus[i] / IConstants.NUM_CYCLE);
        }

        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            temp1 += nodeLinkUseStatus[i];
            // System.out.println("network.unit.node.Node: "+ i+ " .. link Utilization: " +
            // (double)nodeLinkUseStatus[i]/network.common.IConstants.NUM_CYCLE);
        }
        numLink += IConstants.NUMBER_OF_IP_NODE;

        System.out.println("Net =" + IConstants.CURRENT_NET + " Active Link "
                + numLink);

        return temp1 / (numLink * IConstants.NUM_CYCLE);
    }

    /**
     * <p/>
     * Returns the input buffer utilization of all Nodes in a single simulation
     * run.
     * </p>
     * <p/>
     * <p/>
     * This is the indication of how much of the input buffer are used in
     * storing flits out of its maximum possible usage. This is calculated by
     * dividing the summation of number of input buffer in flit network.unit used in
     * storing flit in every simulation cycle by the multiplication of total
     * number of simulation cycle and total amount of input buffer in flit network.unit.
     * </p>
     *
     * @return the input buffer utilization of all node
     */
    // calculate input buffer utilization of node
    public double getInputBufferNodeUtilization() {
        double temp1 = 0.0, temp2;

        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            temp1 += nodeInBufferUseStatus[i];
        }
        temp2 = ((double) IConstants.CURRENT_VC_COUNT
                * (double) IConstants.NUM_FLIT_PER_BUFFER
                * (double) IConstants.NUMBER_OF_IP_NODE * (double) IConstants.NUM_CYCLE);
        return temp1 / temp2;
    }

    /**
     * <p/>
     * Returns the output buffer utilization of all Nodes in a single simulation
     * run.
     * </p>
     * <p/>
     * <p/>
     * This is the indication of how much of the output buffer are used in
     * storing flits out of its maximum possible usage. This is calculated by
     * dividing the summation of number of output buffer in flit network.unit used in
     * storing flit in every simulation cycle by the multiplication of total
     * number of simulation cycle and total amount of output buffer in flit network.unit.
     * </p>
     *
     * @return the output buffer utilization of all node
     */
    // calculate output buffer utilization of node
    public double getOutputBufferNodeUtilization() {
        double temp1 = 0.0, temp2;

        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            temp1 += nodeOutBufferUseStatus[i];
        }
        temp2 = ((double) IConstants.CURRENT_VC_COUNT
                * (double) IConstants.NUM_FLIT_PER_BUFFER
                * (double) IConstants.NUMBER_OF_IP_NODE * (double) IConstants.NUM_CYCLE);
        return temp1 / temp2;

    }

    /**
     * <p/>
     * Returns the input buffer utilization of all Switches in a single
     * simulation run.
     * </p>
     * <p/>
     * <p/>
     * This is the indication of how much of the input buffer are used in
     * storing flits out of its maximum possible usage. This is calculated by
     * dividing the summation of number of input buffer in flit network.unit used in
     * storing flit in every simulation cycle by the multiplication of total
     * number of simulation cycle and total amount of input buffer in flit network.unit.
     * </p>
     *
     * @return the input buffer utilization of all switches
     */
    // calculate input buffer utilization of network.unit.switches.Switch
    public double getInputBufferSwitchUtilization() {
        double temp1 = 0.0, temp2;
        long numLink = 0;

        for (int i = 0; i < noOfSwitch; i++) {
            temp1 += switchInBufferUseStatus[i];
            numLink += numSwitchLink[i];
            System.out.println("network.unit.switches.Switch: " + i + " .. inbuf: "
                    + (double) switchInBufferUseStatus[i]
                    / IConstants.NUM_CYCLE);
        }

        //temp2 = (double) network.common.IConstants.CURRENT_VC_COUNT
        //		* (double) network.common.IConstants.NUM_FLIT_PER_BUFFER * (double) numLink
        //		* (double) noOfSwitch * (double) network.common.IConstants.NUM_CYCLE;

        temp2 = (double) IConstants.CURRENT_VC_COUNT
                * (double) IConstants.NUM_FLIT_PER_BUFFER * (double) numLink
                * (double) IConstants.NUM_CYCLE;

        return temp1 / temp2;
    }

    /**
     * <p/>
     * Returns the output buffer utilization of all Switches in a single
     * simulation run.
     * </p>
     * <p/>
     * <p/>
     * This is the indication of how much of the output buffer are used in
     * storing flits out of its maximum possible usage. This is calculated by
     * dividing the summation of number of output buffer in flit network.unit used
     * in storing flit in every simulation cycle by the multiplication of
     * total number of simulation cycle and total amount of output buffer
     * in flit network.unit.
     * </p>
     *
     * @return the output buffer utilization of all switches
     */
    // calculate output buffer utilization of network.unit.switches.Switch
    public double getOutputBufferSwitchUtilization() {
        // long temp1 = 0 ;
        double temp1 = 0.0, temp2;
        long numLink = 0;

        for (int i = 0; i < noOfSwitch; i++) {
            temp1 += switchOutBufferUseStatus[i];
            numLink += numSwitchLink[i];
            System.out.println("network.unit.switches.Switch: " + i + " .. outbuffer Utilization: "
                    + (double) switchOutBufferUseStatus[i]
                    / IConstants.NUM_CYCLE);
        }

        //temp2 = (double) network.common.IConstants.CURRENT_VC_COUNT
        //		* (double) network.common.IConstants.NUM_FLIT_PER_BUFFER * (double) numLink
        //		* (double) noOfSwitch * (double) network.common.IConstants.NUM_CYCLE;

        temp2 = (double) IConstants.CURRENT_VC_COUNT
                * (double) IConstants.NUM_FLIT_PER_BUFFER * (double) numLink
                * (double) IConstants.NUM_CYCLE;

        return temp1 / temp2;

    }

    /**
     * <p/>
     * Returns the buffer utilization of all Nodes and Switches in a single
     * simulation run.
     * </p>
     * <p/>
     * <p/>
     * This is the indication of how much of the input and output buffer are
     * used in storing flits out of its maximum possible usage. This is
     * calculated by dividing the summation of number of input output buffer
     * in flit network.unit used in storing flit in every simulation cycle by the
     * multiplication of total number of simulation cycle and total amount
     * of input and output buffer in flit network.unit by
     * </p>
     *
     * @return the buffer utilization of all node and switches
     */
    // calculate all buffer utilization of network.unit.node.Node & network.unit.switches.Switch
    public double getBufferUtilization() {
        double temp1 = 0.0, temp2;
        long numLinkSwitch = 0;

        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            temp1 += nodeInBufferUseStatus[i];
            temp1 += nodeOutBufferUseStatus[i];
        }
        for (int i = 0; i < noOfSwitch; i++) {
            temp1 += switchInBufferUseStatus[i];
            temp1 += switchOutBufferUseStatus[i];
            numLinkSwitch += numSwitchLink[i];
        }
        //temp2 = ((double) network.common.IConstants.CURRENT_VC_COUNT
        //		* (double) network.common.IConstants.NUM_FLIT_PER_BUFFER
        //		* ((double) noOfSwitch * (double) numLink + (double) network.common.IConstants.NUMBER_OF_IP_NODE) * (double) network.common.IConstants.NUM_CYCLE);

        temp2 = 2 * ((double) IConstants.CURRENT_VC_COUNT
                * (double) IConstants.NUM_FLIT_PER_BUFFER
                * ((double) numLinkSwitch + IConstants.NUMBER_OF_IP_NODE) * (double) IConstants.NUM_CYCLE);
        return temp1 / temp2;
    }

    /**
     * <p/>
     * Returns the throughput of the network.
     * </p>
     * <p/>
     * Throughput is measured as average number of flits received in a node in
     * every cycle. This is calculated by dividing the total number of flits
     * received in all node within the total simulation cycles by the
     * multiplication of the number of node and number of simulation cycles.
     * </p>
     * <p/>
     * <p/>
     * Don't be confused by a similar function getThroughput() in the same
     * class. See the API for that function, it operates in a different way.
     * </p>
     *
     * @return throughput
     */
    // calculate all Net Throughput considering flit Received
    public double getNetworkNodeThroughput() {
        long temp1 = 0;
        // int numLink = 0 ;
        for (int i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            temp1 += flitReceived[i];
        }
        return (double) temp1
                / (IConstants.NUMBER_OF_IP_NODE * IConstants.NUM_CYCLE);
    }
}