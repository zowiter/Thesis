package network.unit.switches;

import network.unit.switches.Switch;
import network.unit.switches.link.InputLinkController;
import network.unit.switches.link.OutputLinkController;
import network.unit.node.Node;
import network.unit.switches.router.FatTreeRouter;
import network.unit.switches.router.Router;
import network.common.IConstants;
import network.traffic.Flit;
import network.NetworkManager;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * This switch is used for building the Butterfly Fat Tree topology network.
 * This type of switch is interconnected with different number of switches/node
 * according to its position in the network. The switches are arranged according
 * to a butter fly fat tree hierarchy. Here every switch has two parent switches
 * except the top level switches. Every switch has four child switches except
 * the lowest level switches. Lowest level switches have four child IP Nodes
 * (network.unit.node.Node class, resource of the network). All the resources (IP Nodes) are at
 * level 0.
 *
 * @version 1.0
 */

public class FatTreeSwitch implements Switch {

    /**
     * This is an int type variable used to hold the address of the switch in
     * the network.
     */
    private int address;

    /**
     * Index of the switch in the global array of switches in the
     *
     * @see network.Network
     */
    private int switchIndex;

    /**
     * This is an int type variable used to hold the number of physical links
     * attached with the switch to perform communication between adjacent
     * switches and node.
     */
    private int noOfPhysicalLink;

    /**
     * This is an int type variable used to hold the number of virtual channel
     * used for every physical link.
     */
    private int noOfVirtualLink;
    ;

    /**
     * This is an int type variable used to store the level value of this switch
     * in the fat tree network.
     */
    private int level;

    /**
     * This is an int type variable used to store the index value of this switch
     * it its level in the fat tree network. Both level and indexInLevel
     * constitute the address of the switch.
     */
    private int indexInLevel;

    // private int startNodeCover;
    // private int endNodeCover;

    /**
     * In order to facilitate round-robin service for the virtual channels, this
     * array keeps track of the index of last served virtual channel on
     * different links.
     */
    private int[] lastVCServedList;

    /**
     * This is an array of network.unit.switches.FatTreeSwitch used to store the list of reference of
     * adjacent parent switches if any. This list is used to get the parent
     * switches to whom this switch will communicate for transfer of flits.
     */
    private FatTreeSwitch[] upSwitchList;

    /**
     * This is an array of network.unit.switches.FatTreeSwitch used to store the list of reference of
     * adjacent child switches if any. This list is used to get the child
     * switches to whom this switch will communicate for transfer of flits to a
     * node that is reachable by going through the child switches.
     */
    private FatTreeSwitch[] downSwitchList;

    /**
     * This is array of network.unit.node.Node used to store the list of references of adjacent
     * node. One switch may be connected with multiple Nodes and when any flit
     * is to send or any status of signal is required then the corresponding
     * nodes reference is retrieved from the nodeList.
     */
    private Node[] nodeList;

    /**
     * These are array of
     * <p/>
     * {@link InputLinkController} used to store the list of input link
     * controller objects instantiated for input communication management with
     * the adjacent node and switches. There exists exactly one input link
     * controller for every physical link.
     */
    private InputLinkController inputLC[];

    /**
     * These are array of
     * <p/>
     * {@link network.unit.switches.link.OutputLinkController} used to store the list of output link
     * controller objects instantiated for output communication management with
     * the adjacent node and switches. There exists exactly one output link
     * controller for every physical link.
     */
    private OutputLinkController outputLC[];

    /**
     * This array keeps track of the status of the output links if there are any
     * transmission of flits in a particular simulation cycle.
     */
    private boolean LinkUseStatus[];

    /**
     * This is an object of type network.unit.switches.router.Router. Implements network.unit.switches.router.Router's determineRoute()
     * method.
     */
    private Router router;

    /**
     * This is an array of int variable having the size of noOfPhysicalLink *
     * noOfVirtualLink. This array is used for switching in crossbar switch i.e.
     * it determines the path from input buffer to output buffer. At index i of
     * this array a value x (>=0) is stored for path selection. Here x is the
     * input virtual channel identifier and i is the output virtual channel
     * identifier. That means the data from input port x is switched
     * (transferred) to the output port i. Here input and output physical and
     * virtual channels are identified as below.
     * <p/>
     * <ul>
     * <li>input physical channel number = x / noOfPhysicalLink </li>
     * <li>input virtual channel number = x mod noOfPhysicalLink</li>
     * <li>output physical channel number = i / noOfPhysicalLink</li>
     * <li>output virtual channel number = i mod noOfPhysicalLink</li>
     */
    private int[] switchingInfoVector;

    /**
     * This is the constructor of the network.unit.switches.FatTreeSwitch used for butterfly fat tree
     * topology. Here address is the address of this switch from which values of
     * level and indexInLevel member variables can be calculated and switchIndex
     * is an integer indicated its position in the list of all switches of the
     * network used for statistical data storing.
     *
     * @param address     Address of the switch
     * @param switchIndex index of this switch in the global array of switches in the
     * @see network.Network#createFatTreeNetwork()
     */

    public FatTreeSwitch(int address, int switchIndex) {
        int i;

        setAddress(address);
        setNoOfVirtualLink(IConstants.CURRENT_VC_COUNT);
        setNoOfPhysicalLink(IConstants.FAT_NUM_ADJ_CHILD
                + IConstants.FAT_NUM_ADJ_PARENT);

        this.level = address / (1 << IConstants.FAT_NUM_INDEX_BIT);
        this.indexInLevel = address % (1 << IConstants.FAT_NUM_INDEX_BIT);
        this.switchIndex = switchIndex;

        createLinkController(noOfPhysicalLink, noOfVirtualLink);
        createSwitchingInfoVector();

        if (this.level == 1)
            nodeList = new Node[IConstants.FAT_NUM_ADJ_CHILD];
        else
            downSwitchList = new FatTreeSwitch[IConstants.FAT_NUM_ADJ_CHILD];

        upSwitchList = new FatTreeSwitch[IConstants.FAT_NUM_ADJ_PARENT];

        createRouter();

        lastVCServedList = new int[IConstants.FAT_NUM_ADJ_CHILD
                + IConstants.FAT_NUM_ADJ_PARENT];// track which vc is to serv
        // next of every link
        for (i = 0; i < IConstants.FAT_NUM_ADJ_CHILD
                + IConstants.FAT_NUM_ADJ_PARENT; i++) {
            lastVCServedList[i] = 0;
        }

        if (IConstants.TRACE) {
            try {
                RandomAccessFile raf = new RandomAccessFile(
                        IConstants.TRACE_FILE, "rw");
                raf.seek(raf.length());
                raf.writeBytes("\nCreated Fat Tree network.unit.switches.Switch (level,index) = ("
                        + this.level + "," + this.indexInLevel + ") address = "
                        + this.address);
                raf.close();
            } catch (IOException ioex) {
            }
        }

    }

    /**
     * Assigns a network.unit.switches.router.Router implementation of the Butterfly Fat Tree network.
     */
    public void createRouter() {
        router = new FatTreeRouter();
    }

    /**
     * Instantiates the input and output link controllers.
     *
     * @param linkCount number of physical links
     * @param vcCount   number of virtual channels per link
     */
    private void createLinkController(int linkCount, int vcCount) {
        inputLC = new InputLinkController[noOfPhysicalLink];
        outputLC = new OutputLinkController[noOfPhysicalLink];
        LinkUseStatus = new boolean[noOfPhysicalLink];

        for (int i = 0; i < noOfPhysicalLink; i++) {
            inputLC[i] = new InputLinkController(this, i, vcCount, linkCount);
            outputLC[i] = new OutputLinkController(this, i, vcCount, linkCount);
            LinkUseStatus[i] = false;
        }
    }

    /**
     * @inheritDoc
     */
    public void setAdjacentNode(Node node, int linkNo) {
        nodeList[linkNo] = node;
    }

    /**
     * @inheritDoc
     */
    public int getNumAdjacentNode() {
        int i, count = 0;
        for (i = 0; i < nodeList.length; i++) {
            if (null != nodeList[i]) {
                count++;
            }
        }
        return count;
    }

    /**
     * Sets a parent switch.
     *
     * @param fatSwitch parent switch
     * @param linkNo    index of the physical link
     */
    public void setParentSwitch(FatTreeSwitch fatSwitch, int linkNo) {
        upSwitchList[linkNo] = fatSwitch;
    }

    /**
     * Sets a child switch.
     *
     * @param fatSwitch child switch
     * @param linkNo    index of the physical link
     */
    public void setChildSwitch(FatTreeSwitch fatSwitch, int linkNo) {
        downSwitchList[linkNo] = fatSwitch;
    }

    /**
     * @inheritDoc
     */
    public InputLinkController getInputLinkController(int linkNo) {
        return inputLC[linkNo];
    }

    /**
     * @inheritDoc
     */
    public void setInputLinkController(int linkNo, InputLinkController iLC) {
        inputLC[linkNo] = iLC;
    }

    /**
     * @inheritDoc
     */
    public OutputLinkController getOutputLinkController(int linkNo) {
        return outputLC[linkNo];
    }

    /**
     * @inheritDoc
     */
    public void setOutputLinkController(int linkNo, OutputLinkController oLC) {
        outputLC[linkNo] = oLC;
    }

    /**
     * Instantiates and initializes the switchingInfoVector data structure to
     * hold the path mapping information.
     */
    private void createSwitchingInfoVector() {
        switchingInfoVector = new int[noOfPhysicalLink * noOfVirtualLink];
        resetSwitchingInfoVector();
    }

    /**
     * Resets the switching info vector of the network.unit.switches.Switch instance which keeps track
     * of the assignment of a output virtual channel to a packet on a input
     * virtual channel.
     */
    public void resetSwitchingInfoVector() {
        int dim = noOfPhysicalLink * noOfVirtualLink;
        for (int i = 0; i < dim; i++) {
            switchingInfoVector[i] = -1;
        }
    }

    /**
     * @inheritDoc
     */
    public boolean addInputBufferData(int linkNo, Flit flit, int curCycle) {
        return inputLC[linkNo].addInputBufferData(flit, curCycle);
    }

    /**
     * @inheritDoc
     */
    public boolean addOutputBufferData(int linkNo, Flit flit, int vcId,
                                       int curCycle) {
        return outputLC[linkNo].addOutputBufferData(flit, vcId, curCycle);
    }

    /**
     * @inheritDoc
     */
    public boolean setSwitchingInfoVector(int dest, int src) {
        if (switchingInfoVector[dest] < 0) {
            switchingInfoVector[dest] = src;
            return true;
        }
        return false;
    }

    /**
     * @inheritDoc
     */
    public int getSwitchingInfoVector(int dest) {
        return switchingInfoVector[dest];
    }

    /**
     * @inheritDoc
     */
    public Flit removeInputBufferData(int linkNo, int vcId, int curCycle) {
        return inputLC[linkNo].removeInputBufferData(vcId, curCycle);
    }

    /**
     * @inheritDoc
     */
    public Flit removeOutputBufferData(int linkNo, int vcId, int curCycle) {
        return outputLC[linkNo].removeOutputBufferData(vcId, curCycle);
    }

    /**
     * @inheritDoc
     */
    public int determineRoute(int src, int dest) {
        return router.determineRoute(src, dest, this.address);
    }

    /*
      * public void setRouteInfo(int linkNo,int vcId,int toPort) {
      * inputLC[linkNo].getInputBuffer().setRouteInfo(vcId,toPort); }
      */

    /**
     * @inheritDoc
     */
    public boolean hasFreeSlotInVCBuffer(int linkNo, int vcId) {
        return inputLC[linkNo].hasFreeSlotInVCBuffer(vcId);
    }

    /*
      * public int getSwitchingRoute (int src) { return
      * this.switchingInfoVector[src] ; }
      */

    /**
     * This method is called by network in every cycle to set the request of
     * transferring flit from the input buffer to output buffer of the
     * respective switch. For every input physical link attached with the switch
     * input link controllers setOutPathRequest(curCycle) method is called.
     *
     * @param curCycle current simulation cycle
     * @see InputLinkController#setOutPathRequest(int)
     */
    public void updateSwitchOutPathRequest(int curCycle) {
        int i;
        for (i = 0; i < noOfPhysicalLink; i++) {
            if (null != inputLC[i]) {
                inputLC[i].setOutPathRequest(curCycle);
            }
        }
    }

    /**
     * This method is called by network in every cycle to transfer flit from
     * input buffer to output buffer. The working process of moving flit from
     * input buffer to output buffer is described below.
     * <p/>
     * <ul>
     * <li>Checks whether any mapping from input virtual channel to output
     * virtual channel exists.</li>
     * <li>If one found then input and output physical and virtual channel
     * number is determined.</li>
     * <li>network.traffic.Flit is retrieved from source virtual channel.</li>
     * <li>The retrieved flit is then added to output virtual channel.</li>
     * </ul>
     *
     * @param curCycle current simulation cycle
     * @see InputLinkController
     * @see network.Network#moveSwitchTrafficFromInputBufferToOutputBuffer(int)
     */
    public void moveInputBufferToOutputBuffer(int curCycle) {
        int i, numPLVC = noOfPhysicalLink * noOfVirtualLink;
        int srcLinkNo, srcVCId, destLinkNo, destVCId;
        Flit flit;

        for (i = 0; i < numPLVC; i++) {
            if (switchingInfoVector[i] >= 0) {
                destLinkNo = i / noOfVirtualLink;
                destVCId = i % noOfVirtualLink;

                srcLinkNo = switchingInfoVector[i] / noOfVirtualLink;
                srcVCId = switchingInfoVector[i] % noOfVirtualLink;

                flit = inputLC[srcLinkNo].removeInputBufferData(srcVCId,
                        curCycle);
                flit.setLastServiceTimeStamp(curCycle);

                outputLC[destLinkNo].addOutputBufferData(flit, destVCId,
                        curCycle);

                if (IConstants.TRACE) {
                    try {
                        RandomAccessFile raf = new RandomAccessFile(
                                IConstants.TRACE_FILE, "rw");
                        raf.seek(raf.length());
                        if (IConstants.HEADER_FLIT == flit.getType()) {
                            raf
                                    .writeBytes("\nCycle " + curCycle + " ( "
                                            + flit.getSource() + ","
                                            + flit.getDest() + ") "
                                            + " Header network.traffic.Flit("
                                            + flit.getSourceNode() + ","
                                            + flit.getDestinationNode()
                                            + ") is SWITCHING from Link ("
                                            + srcLinkNo + "," + srcVCId
                                            + ") to (" + destLinkNo + ","
                                            + destVCId + ") at network.unit.switches.Switch "
                                            + address);

                        } else {
                            raf.writeBytes("\nCycle " + curCycle + " ( "
                                    + flit.getSource() + "," + flit.getDest()
                                    + ") "
                                    + " Data network.traffic.Flit is SWITCHING from Link ("
                                    + srcLinkNo + "," + srcVCId + ") to ("
                                    + destLinkNo + "," + destVCId
                                    + ") at network.unit.switches.Switch " + address);

                        }

                        raf.close();
                    } catch (IOException ioex) {
                    }
                }

            }
        }
    }

    /**
     * <p/>
     * Performs the transmission of flit from the virtual channel (output
     * buffer) to physical channel and ultimately the flit is stored in the
     * corresponding virtual channel of the switch.
     * </p>
     * <p/>
     * <p/>
     * This method is called by
     * moveSwitchOutputBufferToInputBufferOfNodeSwitch(int curCycle) method of
     * this switch to complete transmission between this switch and actual
     * parameter adjSwitch (an adjacent switch) for this current cycle.
     * </p>
     * <p/>
     * <p/>
     * The method performs its activities in the order given below.
     * <ul>
     * <li>Finds out if there is any virtual channel (i.e. output buffer here)
     * to transmit flit to the switch. The virtual channels are served in a
     * round robin fashion as at a time only one flit can be transferred through
     * physical channel.</li>
     * <li>If one is found then the input physical link number of the adjacent
     * switch is determined and the type of that flit is checked.</li>
     * <li>If the flit is of header type then the corresponding adjacent switch
     * is requested for a free virtual channel. If the input virtual channel is
     * found free then the header flit is sent to that channel.</li>
     * <li>If the flit is of data type then the corresponding switch is
     * requested for a free slot in the input virtual channel the same one as
     * the output virtual channel that is used by this flit. If one free slot is
     * found then the data flit is transmitted.</li>
     * </ul>
     * </p>
     *
     * @param adjSwitch adjacent Butterfly Fat Tree switch
     * @param linkNo    physical link number which connects the switch 'adjSwitch'
     * @param curCycle  simulation cycle
     */
    private void forwardFlitToSwitch(FatTreeSwitch adjSwitch, int linkNo,
                                     int curCycle) {
        int count = 0, toLink, apLink, numNode;
        Flit flit;

        numNode = IConstants.FAT_NUM_ADJ_CHILD;
        apLink = (int) (linkNo - numNode); // link for node is not considered
        // here
        lastVCServedList[linkNo] = (int) ((++lastVCServedList[linkNo]) % noOfVirtualLink);
        while (count < noOfVirtualLink) {
            if ((outputLC[linkNo].hasFlitToSend(lastVCServedList[linkNo]))
                    && (outputLC[linkNo]
                    .getBufferData(lastVCServedList[linkNo])
                    .getLastServiceTimeStamp() < curCycle)) {
                // one VC found to send flit
                toLink = adjSwitch.getAdjacentLinkNo(this);
                if (outputLC[linkNo].getBufferData(lastVCServedList[linkNo])
                        .getType() == IConstants.HEADER_FLIT) {
                    // header flit. So need a free VC
                    if (adjSwitch.isVCFreeInSwitch(toLink,
                            lastVCServedList[linkNo])) {
                        flit = outputLC[linkNo].removeOutputBufferData(
                                lastVCServedList[linkNo], curCycle);
                        flit.setLastServiceTimeStamp(curCycle);
                        if (IConstants.TRACE) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(
                                        IConstants.TRACE_FILE, "rw");
                                raf.seek(raf.length());
                                raf.writeBytes("\nCycle " + curCycle + " ( "
                                        + flit.getSource() + ","
                                        + flit.getDest() + ") "
                                        + " Data network.traffic.Flit is MOVING from SWITCH "
                                        + address + " TO SWITCH "
                                        + adjSwitch.getAddress()
                                        + "  from (Link,VC) =  (" + linkNo
                                        + "," + lastVCServedList[linkNo]
                                        + ") to (Link,VC) = " + toLink + ","
                                        + lastVCServedList[linkNo] + ")");

                                raf.close();
                            } catch (IOException ioex) {
                            }
                        }

                        if (curCycle > IConstants.WARM_UP_CYCLE)
                            NetworkManager.getStatDataInstance()
                                    .incrementFlitLeavingFromSwitch(
                                            this.switchIndex);
                        LinkUseStatus[linkNo] = true;// for Link Utilization
                        if (flit.getVirtualChannelNo() != lastVCServedList[linkNo])
                            System.out.println(curCycle + " GORBOR");
                        adjSwitch.addInputBufferData(toLink, flit, curCycle);
                        break;
                    }
                    // else blocked. try next time
                } else {
                    // data flit. need a free slot in VC buffer
                    if (adjSwitch.hasFreeSlotInVCBuffer(toLink,
                            lastVCServedList[linkNo])) {
                        flit = outputLC[linkNo].removeOutputBufferData(
                                lastVCServedList[linkNo], curCycle);
                        flit.setLastServiceTimeStamp(curCycle);
                        if (IConstants.TRACE) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(
                                        IConstants.TRACE_FILE, "rw");
                                raf.seek(raf.length());
                                raf.writeBytes("\nCycle " + curCycle + " ( "
                                        + flit.getSource() + ","
                                        + flit.getDest() + ") "
                                        + " Data network.traffic.Flit is MOVING from SWITCH "
                                        + address + " TO SWITCH "
                                        + adjSwitch.getAddress()
                                        + "  from (Link,VC) =  (" + linkNo
                                        + "," + lastVCServedList[linkNo]
                                        + ") to (Link,VC) = " + toLink + ","
                                        + lastVCServedList[linkNo] + ")");

                                raf.close();
                            } catch (IOException ioex) {
                            }
                        }
                        if (curCycle > IConstants.WARM_UP_CYCLE)
                            NetworkManager.getStatDataInstance()
                                    .incrementFlitLeavingFromSwitch(
                                            this.switchIndex);
                        LinkUseStatus[linkNo] = true;// for Link Utilization
                        if (flit.getVirtualChannelNo() != lastVCServedList[linkNo])
                            System.out.println(curCycle + " GORBOR");
                        // flit.increaseHop();
                        adjSwitch.addInputBufferData(toLink, flit, curCycle);
                        break;
                    }
                }

            }
            lastVCServedList[linkNo] = (int) ((++lastVCServedList[linkNo]) % noOfVirtualLink);
            count++;
            // otherwise try for the next VC to send

        }
    }

    /**
     * <p/>
     * Transfers a flit to an adjacent node.
     * </p>
     * <p/>
     * This method is called by
     * moveSwitchOutputBufferToInputBufferOfNodeSwitch(int curCycle) method of
     * this switch to complete transmission between this switch and parameter
     * adjNode for this current cycle.
     * </p>
     * <p/>
     * <p/>
     * The method performs its activities in the order given below.
     * <ul>
     * <li>Finds out if there is any virtual channel (i.e. output buffer here)
     * to transmit flit to the node. The virtual channels are served in a round
     * robin fashion as at a time only one flit can be transferred through
     * physical channel.</li>
     * <li>If one is found then the input physical link number of the adjacent
     * node is determined and the type of that flit is checked.</li>
     * <li>If the flit is of header type then the corresponding adjacent node
     * is requested for a free virtual channel. If the input virtual channel is
     * found free then the header flit is sent to that channel.</li>
     * <li>If the flit is of data type then the corresponding node is requested
     * for a free slot in the input virtual channel the same as the one used by
     * this flit in the output virtual channel. If one free slot is found then
     * the data flit is transmitted.</li>
     * </ul>
     * </p>
     *
     * @param adjNode  node reference
     * @param linkNo   physical link number that connects the node to the switch
     * @param curCycle simulation cycle
     */
    private void forwardFlitToNode(Node adjNode, int linkNo, int curCycle) {
        int count = 0;
        Flit flit;

        lastVCServedList[linkNo] = (int) ((++lastVCServedList[linkNo]) % noOfVirtualLink);
        while (count < noOfVirtualLink) {
            if (outputLC[linkNo].hasFlitToSend(lastVCServedList[linkNo])
                    && outputLC[linkNo].getBufferData(lastVCServedList[linkNo])
                    .getLastServiceTimeStamp() < curCycle
                    && adjNode.getLastUsedOwnInCycle() < (NetworkManager
                    .getHelpingUtility().getConvertedCycle(curCycle,
                    adjNode.getClockRateFactor()) + 1)) {
                // one VC found to send flit

                if (outputLC[linkNo].getBufferData(lastVCServedList[linkNo])
                        .getType() == IConstants.HEADER_FLIT) {

                    // header flit. So need a free VC
                    if (adjNode.isInputVCFree(lastVCServedList[linkNo])) {
                        adjNode
                                .setLastUsedOwnInCycle(NetworkManager
                                        .getHelpingUtility().getConvertedCycle(
                                        curCycle,
                                        adjNode.getClockRateFactor()) + 1);

                        flit = outputLC[linkNo].removeOutputBufferData(
                                lastVCServedList[linkNo], curCycle);
                        flit.setLastServiceTimeStamp(curCycle);
                        if (IConstants.TRACE) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(
                                        IConstants.TRACE_FILE, "rw");
                                raf.seek(raf.length());
                                raf.writeBytes("\nNode " + adjNode.getAddress()
                                        + " In Cycle "
                                        + adjNode.getLastUsedOwnInCycle()
                                        + " network.unit.switches.Switch Cycle " + curCycle
                                        + " Header network.traffic.Flit("
                                        + flit.getSourceNode() + ","
                                        + flit.getDestinationNode()
                                        + ") is MOVING from SWITCH " + address
                                        + " TO NODE " + adjNode.getAddress()
                                        + " at (Link,VC) = (" + linkNo + ","
                                        + lastVCServedList[linkNo] + ")");

                                raf.close();
                            } catch (IOException ioex) {
                            }
                        }
                        // flit.increaseHop();
                        adjNode.addInputBufferData(flit, curCycle);
                        if (curCycle > IConstants.WARM_UP_CYCLE)
                            NetworkManager.getStatDataInstance()
                                    .incrementFlitLeavingFromSwitch(
                                            this.switchIndex);
                        LinkUseStatus[linkNo] = true;// for Link Utilization
                        break;
                    }
                } else {
                    // data flit. need a free slot in VC buffer
                    if (adjNode.hasFreeSlotInInputVC(lastVCServedList[linkNo])) {
                        adjNode
                                .setLastUsedOwnInCycle(NetworkManager
                                        .getHelpingUtility().getConvertedCycle(
                                        curCycle,
                                        adjNode.getClockRateFactor()) + 1);

                        flit = outputLC[linkNo].removeOutputBufferData(
                                lastVCServedList[linkNo], curCycle);
                        flit.setLastServiceTimeStamp(curCycle);
                        if (IConstants.TRACE) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(
                                        IConstants.TRACE_FILE, "rw");
                                raf.seek(raf.length());
                                raf.writeBytes("\nNode " + adjNode.getAddress()
                                        + " In Cycle "
                                        + adjNode.getLastUsedOwnInCycle()
                                        + " network.unit.switches.Switch Cycle " + curCycle
                                        + " Data network.traffic.Flit is MOVING from SWITCH "
                                        + address + " TO NODE "
                                        + adjNode.getAddress()
                                        + " at (Link,VC) = (" + linkNo + ","
                                        + lastVCServedList[linkNo] + ")");

                                raf.close();
                            } catch (IOException ioex) {
                            }
                        }
                        // flit.increaseHop();
                        adjNode.addInputBufferData(flit, curCycle);
                        if (curCycle > IConstants.WARM_UP_CYCLE)
                            NetworkManager.getStatDataInstance()
                                    .incrementFlitLeavingFromSwitch(
                                            this.switchIndex);
                        LinkUseStatus[linkNo] = true;// for Link Utilization
                        break;
                    }
                }

            }
            lastVCServedList[linkNo] = (int) ((++lastVCServedList[linkNo]) % noOfVirtualLink);
            count++;
            // otherwise try for the next VC to send

        }
    }

    /**
     * This method is called by network.Network in every cycle to transfer flit from
     * output buffer of the switch to input buffer of adjacent node and
     * switches. For every adjacent node forwardFlitToNode(adjNode, linkNo,
     * curCycle) is called and for every adjacent switch
     * forwardFlitToSwitch(adjSwitch, linkNo, curCycle) is called.
     *
     * @param curCycle simulation cycle
     * @see network.Network#moveSwitchTrafficFromOutputBufferToInputBufferOfNodeSwitch(int)
     * @see FatTreeSwitch#forwardFlitToNode(Node,int,int)
     * @see FatTreeSwitch#forwardFlitToSwitch(FatTreeSwitch,int,int)
     */
    public void moveSwitchOutputBufferToInputBufferOfNodeSwitch(int curCycle) {
        int i;
        Node adjNode;
        FatTreeSwitch adjSwitch;

        // for all adjacent node and switches
        for (i = 0; i < noOfPhysicalLink; i++) {
            if (this.level == 1 && i < IConstants.FAT_NUM_ADJ_CHILD) {
                adjNode = nodeList[i];
                if (null != adjNode) {
                    forwardFlitToNode(adjNode, i, curCycle);
                }
            } else if (this.level > 1 && i < IConstants.FAT_NUM_ADJ_CHILD) {
                adjSwitch = downSwitchList[i];
                if (null != adjSwitch) {
                    forwardFlitToSwitch(adjSwitch, i, curCycle);
                }
            } else if (i >= IConstants.FAT_NUM_ADJ_CHILD) {
                adjSwitch = upSwitchList[i - IConstants.FAT_NUM_ADJ_CHILD];
                if (null != adjSwitch) {
                    forwardFlitToSwitch(adjSwitch, i, curCycle);
                }
            }
        }
    }

    /**
     * @inheritDoc
     */
    public boolean isVCFreeInSwitch(int linkNo, int vcId) {
        if (null != inputLC[linkNo]) {
            return inputLC[linkNo].isVCFree(vcId);
        } else {
            return false;
        }
    }

    /**
     * @inheritDoc
     */
    public int getNumLinkActive() {
        int count = 0;
        for (int i = 0; i < this.noOfPhysicalLink; i++)
            if (outputLC[i] != null)
                count++;
        return count;
    }

    /**
     * @inheritDoc
     */
    public void updateStatusAfterCycle(int curCycle) {
        int i, useCount = 0, inBufUsed = 0, outBufUsed = 0;
        for (i = 0; i < noOfPhysicalLink; i++) {
            if (null != inputLC[i]) {
                inputLC[i].updateStatusAfterCycle();
                outputLC[i].updateStatusAfterCycle();

                // stat
                inBufUsed += inputLC[i].getInputBuffer().getNumSlotUsed();
                outBufUsed += outputLC[i].getOutputBuffer().getNumSlotUsed();

                if (LinkUseStatus[i] == true) {
                    useCount++;
                    LinkUseStatus[i] = false;
                }
            }
        }
        resetSwitchingInfoVector();

        // stat
        if (curCycle > IConstants.WARM_UP_CYCLE) {
            NetworkManager.getStatDataInstance().incrementSwitchLinkUse(
                    this.switchIndex, useCount);
            NetworkManager.getStatDataInstance().incrementSwitchInputBufferUse(
                    this.switchIndex, inBufUsed);
            NetworkManager.getStatDataInstance()
                    .incrementSwitchOutputBufferUse(this.switchIndex,
                            outBufUsed);
        }
    }

    /**
     * Returns the level of the Butterfly Fat Tree network where the switch is
     * placed.
     *
     * @return the level
     */
    public int getSwitchLevel() {
        return this.address >> IConstants.FAT_NUM_INDEX_BIT;
    }

    /**
     * Returns the index of the switch in the level of the Butterfly Fat Tree
     * network where it is placed.
     *
     * @return the index
     */
    public int getSwitchIndex() {
        return this.address & ((1 << IConstants.FAT_NUM_INDEX_BIT) - 1);
    }

    // public void setStartNodeCover(int start) {
    // this.startNodeCover = start;
    // }

    // public int getStartNodeCover() {
    // return this.startNodeCover;
    // }

    // public void setEndNodeCover(int end) {
    // this.endNodeCover = end;
    // }

    // public int getEndNodeCover() {
    // return this.endNodeCover;
    // }

    /**
     * @inheritDoc
     */
    public void setAddress(int addr) {
        this.address = addr;
    }

    /**
     * @inheritDoc
     */
    public int getAddress() {
        return this.address;
    }

    /**
     * Returns the number of the output physical link that connects a
     * neighboring switch, either situated above or beneath this switch.
     *
     * @param fatSwitch neighboring Fat Tree network.unit.switches.Switch
     * @return output link number
     */
    public int getAdjacentLinkNo(FatTreeSwitch fatSwitch) {
        int i;
        for (i = 0; i < upSwitchList.length; i++) {
            if (upSwitchList[i] == fatSwitch)
                return i + IConstants.FAT_NUM_ADJ_CHILD;
        }
        for (i = 0; i < downSwitchList.length; i++) {
            if (downSwitchList[i] == fatSwitch)
                return i;
        }
        return -1;

    }

    /**
     * This method is not required for other purpose, so left as a blank
     * implementation of the corresponding specified in the network.unit.switches.Switch interface.
     */
    public void setAdjacentSwitch(Switch sw, int linkNo) {
        // switchList[linkNo] = (network.unit.switches.OctalSwitch)octalSwitch ;
    }

    /**
     * @inheritDoc
     */
    public void setNoOfVirtualLink(int no) {
        this.noOfVirtualLink = no;
    }

    /**
     * @inheritDoc
     */
    public void setNoOfPhysicalLink(int no) {
        this.noOfPhysicalLink = no;
    }

    /**
     * @inheritDoc
     */
    public int getNoOfPhysicalLink() {
        return this.noOfPhysicalLink;
    }

    /**
     * @inheritDoc
     */
    public int getNoOfVirtualLink() {
        return this.noOfVirtualLink;
    }
}
