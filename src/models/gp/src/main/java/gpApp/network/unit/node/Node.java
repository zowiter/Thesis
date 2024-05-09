package gpApp.network.unit.node;

import gpApp.network.unit.switches.Switch;
import gpApp.network.unit.switches.buffer.InputVCBuffer;
import gpApp.network.unit.switches.buffer.OutputVCBuffer;
import gpApp.network.traffic.ConcreteNodeTraffic;
import gpApp.network.traffic.Flit;
import gpApp.network.NetworkManager;
import gpApp.network.Network;
import gpApp.network.common.IConstants;
import gpApp.network.traffic.NodeTraffic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

/**
 * IP network.unit.node.Node (network.unit.node.Node class) is the resource that is used in the system for message
 * generation as well as message consumption. network.unit.node.Node is connected with a switch
 * (depends on network type) through one input physical link and one output
 * physical link.
 *
 * @version 1.0
 */

public class Node {
    /**
     * <p/>
     * int type variable - holds the address of the node. The address is
     * represented in different ways for different types of networks.
     * </p>
     * <p/>
     * For example for Mesh and Torus network the address is represented from
     * row and column as well as position of the node in the switch. For Fat
     * Tree network the address is represented from the level of the node and
     * the position of the node in the level.
     * </p>
     */
    private int address;

    /**
     * Reference to the parent switch object.
     */
    public Switch parent;

    /**
     * An array of integer values containing the VC assigned to each generated
     * message.
     */
    private int messageVCIndex[];

    /**
     * No of generated messages stored in the node's internal buffer that are
     * yet to be transferred completely.
     */
    private int messageCount = 0;

    /**
     * Vector type variable - holds the reference of packets which are to
     * transmit. Packet is the variable size list of flits on the other hand.
     */
    private Vector[] messageList;

    /**
     * Vector type variable - holds the reference of packets which have been
     * received.
     */

    private Vector[] receiveMessageList;

    /**
     * It keeps track of the number of flits in a packet that are yet to be
     * received.
     */
    private int nodeReceivedFlitCounter[];

    /**
     * An array of integers that keep track of whether a virtual channel has
     * already been assigned or not.
     */
    private int[] outVCUsedList;

    // not used in this version
    // private int[] inVCUsedList;

    /**
     * Total no. of virtual channel per physical link
     */
    private int vcCount;

    // private int curVCId;

    /**
     * The link number of the parent switch to which the node is connected.
     */
    private int linkNo;

    /**
     * Since in one cycle only one packet can be assigned a free virtual
     * channel, this variable keeps track of the packet number that was last
     * serviced.
     */
    private int lastSender = 0;

    /**
     * This variable keeps track of the last output virtual channel from which a
     * flit has been transferred to the adjacent switch.
     */
    private int lastOutVCServed = 0;

    /**
     * This variable keeps track of the last input virtual channel from which a
     * flit has been received and transferred into the message center.
     */
    private int lastInVCServed = 0;

    /**
     * The index for this node in the universal array of node in the
     *
     * @see Network
     */
    private int nodeListIndex;

    /**
     * This is a boolean value which is set for a particual cycle, if the node
     * transmits a flit to its adjacent node. It is reset again in the
     * updateStatusAfterCycle method.
     */
    private boolean linkUsed = false;

    /**
     * The speed factor of the node with respect to the switches.
     */
    private double clockRateFactor;

    /**
     * The last cycle in which it receives an flit from its adjacent switch.
     * This is converted according to its clock rate factor with respect to the
     * speed of the switches.
     */
    private int lastUsedOwnInCycle = -1;

    /**
     * The last cycle in which it an flit is transferred to the adjacent switch.
     * This is converted according to its clock rate factor with respect to the
     * speed of the switches.
     */
    private int lastUsedOwnOutCycle = -1;

    /**
     * An object of network.unit.switches.buffer.InputVCBuffer class. It represents the incoming physical
     * link through which the switch sends data to the node. In our system for
     * node we do not use link controller but incorporated only buffer as for a
     * link controller has one buffer and a node contains only one link
     * controller. Moreover no routing is required for node. The functional code
     * of link controller is incorporated in buffer and link controller is
     * working as a wrapper of the buffer.
     */
    private InputVCBuffer inputBuffer;

    // private network.unit.switches.link.InputLinkController iLC ;

    /**
     * An object of OutputVCBufer class. It is almost same as inputBuffer with
     * only the change of data flow direction. Data is transferred to parent
     * switch through this buffer. Parent switch switches it to other switches
     * and node.
     */
    private OutputVCBuffer outputBuffer;

    // private network.unit.switches.link.OutputLinkController oLC ;

    /**
     * Reference to the NodeTraffic class. NodeTraffic is an abstract class, its
     * implementation defines the traffic pattern for the packet generated by
     * this node object.
     */
    public NodeTraffic nodeTraffic;

    /**
     * <p/>
     * Constructor of network.unit.node.Node class
     * </p>
     * <p/>
     * The constructor instantiates all the necessary objects (e.g. input/output
     * buffer, messageList and so) for internal uses and keeps references of
     * external objects (e.g. parent switch)
     * </p>
     *
     * @param address       address of the node
     * @param parent        parent switch of the node
     * @param pLink         the physical link number through which the node is connected
     *                      with the switch
     * @param vcCount       the number of virtual channel used for every physical channel
     * @param clkRateFactor clock rate factor of the node with respect to those of the
     *                      switches.
     */
    public Node(int address, Switch parent, int pLink, int vcCount,
                double clkRateFactor) {
        int i;
        this.address = address;
        this.parent = parent;
        this.linkNo = pLink;
        this.vcCount = vcCount;
        this.clockRateFactor = clkRateFactor;
        messageVCIndex = new int[IConstants.MAX_MESSAGE_NUMBER];

        messageList = new Vector[IConstants.MAX_MESSAGE_NUMBER];
        receiveMessageList = new Vector[vcCount];
        nodeReceivedFlitCounter = new int[vcCount];

        outVCUsedList = new int[vcCount];
        // inVCUsedList = new int[vcCount];

        inputBuffer = new InputVCBuffer(vcCount, 0);
        outputBuffer = new OutputVCBuffer(vcCount, 0);

        for (i = 0; i < IConstants.MAX_MESSAGE_NUMBER; i++) {
            messageVCIndex[i] = -1;
        }

        for (i = 0; i < vcCount; i++) {
            outVCUsedList[i] = 0;
            // inVCUsedList[i] = 0;
        }

        nodeTraffic = new ConcreteNodeTraffic(address);

    }

    /**
     * Sets the clock rate factor (variable clockRateFactor) of the node class.
     *
     * @param clkRateFactor clock rate factor
     */
    public void setClockRateFactor(double clkRateFactor) {
        this.clockRateFactor = clkRateFactor;
    }

    /**
     * Returns the clock rate factor of the node class.
     *
     * @return clock rate factor
     */
    public double getClockRateFactor() {
        return this.clockRateFactor;
    }

    /**
     * Sets the lastUsedOwnInCycle variable of the node class
     *
     * @param cycle the last cycle in which it receives an flit from its adjacent
     *              switch
     */
    public void setLastUsedOwnInCycle(int cycle) {
        this.lastUsedOwnInCycle = cycle;
    }

    /**
     * Returns the lastUsedOwnInCycle variable of the node class
     *
     * @return the last cycle in which it receives an flit from its adjacent
     *         switch
     */
    public int getLastUsedOwnInCycle() {
        return this.lastUsedOwnInCycle;
    }

    /**
     * Sets the lastUsedOwnOutCycle variable of the node class
     *
     * @param cycle the last cycle in which it an flit is transferred to the
     *              adjacent switch.
     */
    public void setLastUsedOwnOutCycle(int cycle) {
        this.lastUsedOwnOutCycle = cycle;
    }

    /**
     * Returns the lastUsedOwnOutCycle variable of the node class
     *
     * @return the last cycle in which it an flit is transferred to the adjacent
     *         switch.
     */
    public int getLastUsedOwnOutCycle() {
        return this.lastUsedOwnOutCycle;
    }

    /**
     * Sets the address of the node.
     *
     * @param addr address
     */
    public void setAddress(int addr) {
        this.address = addr;
    }

    /**
     * Returns the address of the node.
     *
     * @return address
     */
    public int getAddress() {
        return this.address;
    }

    /**
     * this method generates the message of L length. L is found by exponential
     * distribution of mean avgMessageLength. this method is called T cycle. T
     * is found by exponential distribution of mean avgInterArrival.
     */
    private void generateMessage(int curCycle) {
        Vector packet;
        int i;

        packet = nodeTraffic.generateMessage(curCycle, messageCount);

        if (packet == null) {
            if (curCycle > IConstants.WARM_UP_CYCLE) {
                NetworkManager.getStatDataInstance()
                        .incrementMessageNotProduced(nodeListIndex);

            }
            return;
        }

        i = 0;
        while (i < IConstants.MAX_MESSAGE_NUMBER && messageList[i] != null) {
            i++;
        }

        if (i < IConstants.MAX_MESSAGE_NUMBER && messageList[i] == null) {
            this.messageList[i] = packet;
            this.messageCount++;
        }
        if (curCycle > IConstants.WARM_UP_CYCLE) {
            NetworkManager.getStatDataInstance().incrementPacketProduced(
                    this.nodeListIndex);
        }

    }

    // public void receiveFlit(network.traffic.Flit flit) {

    /*
      * if (network.common.IConstants.TRACE) { System.out.println("network.traffic.Flit received by node " +
      * address + " at clock " + network.common.IConstants.CUR_CYCLE) ; System.out.println("Data
      * is : " + flit.toString()) ; System.out.println("") ; }
      */
    // }

    /**
     * Returns the number of output virtual channels that are already occupied.
     *
     * @return the number of used output virtual channel
     */

    private int getNumUsedVC() {
        int i, num = 0;
        for (i = 0; i < vcCount; i++) {
            if (outVCUsedList[i] != 0) {
                num++;
            }
        }
        return num;
    }

    /**
     * This method is one of the core methods ot the network.unit.node.Node class. It organizes
     * the operation of the network.unit.node.Node object.
     * <p/>
     * <p/>
     * It first checks if this is the right cycle to generate message.
     * </p>
     * <p/>
     * <p/>
     * It, then, looks up for a free VC to assign to its generated messages that
     * are yet to be assigned a VC.
     * </p>
     * <p/>
     * <p/>
     * It calls the appropriate method (fillEmptyBuffer) to transfer a flit to
     * the output buffer.
     * </p>
     * <p/>
     * <p/>
     * It calls the appropriate method (forwardFlitToSwitch) to transfer a flit
     * to the adjacent switch.
     * </p>
     *
     * @param curCycle current simulation cycle.
     */
    public void updateOutput(int curCycle) {
        if (curCycle == nodeTraffic.getNextMsgGenTime()) {
            generateMessage(curCycle);
            // setNextMsgGenTime(curCycle) ;
        }

        checkForMsgFreeVC();
        fillEmptyBuffer(curCycle);
        forwardFlitToSwitch(curCycle);
    }

    /**
     * Performs the following operations.
     * <ul>
     * <li>Checks whether there exists a packet without allocation of a virtual
     * channel number.</li>
     * <li>If found then asks the output buffer to provide a free virtual
     * channel.</li>
     * <li>If free virtual channel is found then tags it with that message by
     * updating member variable messageVCIndex[]. This virtual channel is never
     * assigned to other packet until the packet is fully transmitted to the
     * switch.</li>
     * </ul>
     */
    private void checkForMsgFreeVC() {
        int count, vc;
        int numUsedVC = 0;

        numUsedVC = outputBuffer.getNumUsedVC();
        // Free VC available
        if (numUsedVC < vcCount) {
            count = 0;
            while (count < IConstants.MAX_MESSAGE_NUMBER) {
                // a meesage that is looking for VC is found
                if (messageList[lastSender] != null
                        && messageVCIndex[lastSender] < 0) {
                    // get the free VC and assign this VC to the meesage.
                    vc = outputBuffer.getFreeVC();
                    if (vc >= 0) {
                        messageVCIndex[lastSender] = vc;
                        outVCUsedList[vc] = 1;
                    }
                    break;
                } else {
                    lastSender = (int) ((lastSender + 1) % IConstants.MAX_MESSAGE_NUMBER);
                }
                count++;
            }
        }
    }

    /**
     * Performs the following operations.
     * <ul>
     * <li>Finds out whether there exists a packet that is assigned a virtual
     * channel number.</li>
     * <li>If found then output buffer is asked for a free slot in the
     * corresponding virtual channel</li>
     * <li>If found then a flit is moved from the packet to the virtual
     * channel.</li>
     * <li>network.traffic.Flit�s lastServiceTime statistical field is updated by current clock
     * cycle</li>
     * <li>network.traffic.Flit�s virtual channel number is updated by the number found from
     * messageVCIndex[].</li>
     * <li>This process is executed for all the packets whose are assigned a
     * virtual channel.</li>
     * </ul>
     *
     * @param curCycle current simulation cycle
     */
    private void fillEmptyBuffer(int curCycle) {
        int i = 0;
        Vector packet;
        Flit flit;
        for (i = 0; i < IConstants.MAX_MESSAGE_NUMBER; i++) {
            // identify which message's flit can be dumped into buffer
            // i-th message is assigned a VC and It has already sent some flit
            if (messageVCIndex[i] >= 0) { // &&
                // outVCUsedList[messageVCIndex[i]]
                // > 0)
                // if free slots in the VC for that message exists then fill all
                // the free slots
                packet = (Vector) messageList[i];
                while (packet.size() > 0
                        && outputBuffer.hasFreeSlotInVC(messageVCIndex[i])) {
                    flit = (Flit) packet.remove(0);
                    // flit.setGenTimeStamp(curCycle - 1) ;

                    flit.setLastServiceTimeStamp(curCycle);
                    /** ** Repeated task: Done in the network.unit.switches.buffer.OutputVCBuffer class *** */
                    /*
                          * flit.setLastServiceTimeStamp(curCycle);
                          * flit.setVirtualChannelNo(messageVCIndex[i]);
                          */
                    /** ** Eliminated *** */

                    outputBuffer.addBufferData(flit, messageVCIndex[i],
                            curCycle);
                    if (IConstants.HEADER_FLIT == flit.getType()) {
                        if (curCycle > IConstants.WARM_UP_CYCLE) {
                            NetworkManager.getStatDataInstance()
                                    .incrementPacketSent(nodeListIndex);

                            if ((flit.getSource() >> 3) == (flit.getDest() >> 3)) {
                                NetworkManager.getStatDataInstance().sameUnit++;
                            }
                        }
                        if (curCycle > IConstants.WARM_UP_CYCLE) {
                            if (IConstants.CURRENT_NET == IConstants.NET_WK) {
                                if (IConstants.DEBUG)
                                    System.out.println("Sent: from " + Network.convertToDecimalAddress(this.address)
                                            + " to: " + Network.convertToDecimalAddress(flit.getDest()) + " cycle: "
                                            + curCycle + " length: "
                                            + flit.getPacketLength() + " gentime: "
                                            + flit.getGenTimeStamp());
                            } else {
                                System.out.println("Sent: from " + this.address
                                        + " to: " + flit.getDest() + " cycle: "
                                        + curCycle + " length: "
                                        + flit.getPacketLength() + " gentime: "
                                        + flit.getGenTimeStamp());
                            }
                        }

                    }
                    if (packet.size() == 0) {
                        messageList[i] = null;
                        messageVCIndex[i] = -1;
                        messageCount--;
                    } else {
                        packet = (Vector) messageList[i];
                    }
                    break; // as one flit at a cycle.
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
     * The method performs its activities in the order given below.
     * <ul>
     * <li>Finds out if there is any virtual channel (i.e. output buffer here)
     * to transmit flit to the switch. The virtual channels are served in a
     * round robin fashion as at a time only one flit can be transferred through
     * physical channel.</li>
     * <li>If one is found then the type of that flit is checked.</li>
     * <li>If the flit is of header type then the corresponding parent switch
     * is requested for a free virtual channel. If one is found free then header
     * flit is sent to that channel otherwise the header flit cannot be
     * transmitted as header flit is always assigned a free virtual channel.</li>
     * <li>If the flit is of data type then the corresponding parent switch is
     * requested for a free slot in the virtual channel that is used by this
     * flit. If one free slot is found then the data flit is transmitted. For
     * data flit there is no need of free virtual channel and it always follows
     * the virtual path taken by the header flit of this packet.</li>
     * </ul>
     * </p>
     *
     * @param curCycle current simulation cycle
     */
    private void forwardFlitToSwitch(int curCycle) {
        int count = 0;
        Flit flit;
        // network.common.HelpingUtility utility

        lastOutVCServed = (int) (++lastOutVCServed % vcCount);
        while (count < vcCount) {
            if (outVCUsedList[lastOutVCServed] > 0
                    && outputBuffer.hasFlitToSend(lastOutVCServed)
                    && NetworkManager.getHelpingUtility()
                    .getConvertedCycle(
                            outputBuffer.getBufferData(lastOutVCServed)
                                    .getLastServiceTimeStamp(),
                            clockRateFactor) < NetworkManager
                    .getHelpingUtility().getConvertedCycle(curCycle,
                    clockRateFactor)
                    && lastUsedOwnOutCycle < NetworkManager.getHelpingUtility()
                    .getConvertedCycle(curCycle, clockRateFactor)) {

                // a VC found whose turn and whose data is to send

                if (outputBuffer.getBufferData(lastOutVCServed).getType() == IConstants.HEADER_FLIT) {
                    // header flit. So need a free VC
                    if (parent.isVCFreeInSwitch(linkNo, lastOutVCServed)) {

                        lastUsedOwnOutCycle = NetworkManager
                                .getHelpingUtility().getConvertedCycle(
                                curCycle, clockRateFactor);

                        flit = outputBuffer.removeBufferData(lastOutVCServed,
                                curCycle);

                        flit.setLastServiceTimeStamp(curCycle);
                        /** ** Eliminated : Done in the network.unit.switches.buffer.InputVCBuffer Class *** */

                        /*
                               * flit.setLastServiceTimeStamp(curCycle);
                               * flit.increaseHop();
                               */
                        /** ** Eliminated *** */

                        parent.addInputBufferData(linkNo, flit, curCycle);
                        this.linkUsed = true;
                        if (IConstants.TRACE) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(
                                        IConstants.TRACE_FILE, "rw");
                                raf.seek(raf.length());
                                raf.writeBytes("\nNode " + address
                                        + " Cycle out " + lastUsedOwnOutCycle
                                        + " network.unit.switches.Switch Cycle " + curCycle
                                        + " Header network.traffic.Flit("
                                        + flit.getSourceNode() + ","
                                        + flit.getDestinationNode()
                                        + ") is moving from network.unit.node.Node " + address
                                        + " OutBuffer VC index "
                                        + lastOutVCServed + " to network.unit.switches.Switch "
                                        + parent.getAddress()
                                        + " (Link,VC)) = " + linkNo + ","
                                        + lastOutVCServed + ")");

                                raf.close();
                            } catch (IOException ioex) {
                            }
                        }

                        break;
                    }
                    // else blocked. try next time
                } else {
                    // data flit. need a free slot in VC buffer
                    if (parent.hasFreeSlotInVCBuffer(linkNo, lastOutVCServed)) {

                        lastUsedOwnOutCycle = NetworkManager
                                .getHelpingUtility().getConvertedCycle(
                                curCycle, clockRateFactor);

                        flit = outputBuffer.removeBufferData(lastOutVCServed,
                                curCycle);

                        flit.setLastServiceTimeStamp(curCycle);
                        /** ** Eliminated : Done in the network.unit.switches.buffer.InputVCBuffer Class *** */

                        /*
                               * flit.setLastServiceTimeStamp(curCycle);
                               * flit.increaseHop();
                               */
                        /** ** Eliminated *** */

                        parent.addInputBufferData(linkNo, flit, curCycle);
                        this.linkUsed = true;

                        if (IConstants.TRACE) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(
                                        IConstants.TRACE_FILE, "rw");
                                raf.seek(raf.length());
                                raf.writeBytes("\nNode " + address
                                        + " Cycle out " + lastUsedOwnOutCycle
                                        + " network.unit.switches.Switch Cycle " + curCycle
                                        + " Data network.traffic.Flit is moving from network.unit.node.Node "
                                        + address + " OutBuffer VC index "
                                        + lastOutVCServed + " to network.unit.switches.Switch "
                                        + parent.getAddress()
                                        + " (Link,VC)) = " + linkNo + ","
                                        + lastOutVCServed + ")");

                                raf.close();
                            } catch (IOException ioex) {
                            }
                        }

                        break;
                    }
                }
            }
            lastOutVCServed = (int) (++lastOutVCServed % vcCount);
            count++;
            // otherwise try for the next VC to send
        }
    }

    /**
     * Checks whether the input virtual channel is free or not.
     *
     * @param vcId virtual channel no
     * @return true-if the virtual channel is free, false-otherwise
     */
    public boolean isInputVCFree(int vcId) {
        return inputBuffer.isVCFree(vcId);
    }

    /**
     * Checks whether the input virtual channel has a free slot or not.
     *
     * @param vcId virtual channel no.
     * @return true-if the virtual channel has one or more free slots,
     *         false-otherwise.
     */
    public boolean hasFreeSlotInInputVC(int vcId) {
        return inputBuffer.hasFreeSlotInVC(vcId);
    }

    /**
     * This method is called by the parent switch when flits are available in
     * the switch sent by some other node to transfer to this node. The method
     * simply calls the addBufferData() method of the inputBuffer object that is
     * instantiated for the node. The input buffer performs the real functions.
     *
     * @param flit     flit data
     * @param curCycle current simulation cycle
     * @return true-if flit added to the buffer, false-otherwise.
     */
    public boolean addInputBufferData(Flit flit, int curCycle) {
        if (this.address != flit.getDest()) {
            System.err.println("WRONG: network.unit.node.Node Address" + this.address
                    + " FlitSrc: " + flit.getSource() + " FlitDest: "
                    + flit.getDest() + " Type:" + flit.getType());
        }

        if (IConstants.TRACE) {
            try {
                RandomAccessFile raf = new RandomAccessFile(
                        IConstants.TRACE_FILE, "rw");
                raf.seek(raf.length());
                if (IConstants.HEADER_FLIT == flit.getType()) {
                    raf.writeBytes("\nNode " + address + " Cycle In "
                            + lastUsedOwnInCycle + " network.unit.switches.Switch Cycle " + curCycle
                            + " ( " + flit.getSource() + "," + flit.getDest()
                            + ") " + " Header network.traffic.Flit(" + flit.getSourceNode()
                            + "," + flit.getDestinationNode()
                            + ") is received from network.unit.switches.Switch "
                            + parent.getAddress() + " at network.unit.node.Node " + address
                            + " VC index " + flit.getVirtualChannelNo());
                } else {
                    raf.writeBytes("\nNode " + address + " Cycle In "
                            + lastUsedOwnInCycle + " network.unit.switches.Switch Cycle " + curCycle
                            + " ( " + flit.getSource() + "," + flit.getDest()
                            + ") " + " Data network.traffic.Flit is received from network.unit.switches.Switch "
                            + parent.getAddress() + " at network.unit.node.Node " + address
                            + " VC index " + flit.getVirtualChannelNo());

                }
                raf.close();
            } catch (IOException ioex) {
            }
        }

        if (curCycle > IConstants.WARM_UP_CYCLE) {
            NetworkManager.getStatDataInstance().incrementFlitReceived(
                    nodeListIndex);
        }
        return inputBuffer.addBufferData(flit, flit.getVirtualChannelNo(),
                curCycle);

    }

    /**
     * This method is called by the network.Network class at every cycle. The method
     * performs in the following way.
     * <ul>
     * <li>Checks the virtual channels of the input buffer to find if there is
     * any one channel that can send the flit to the message center i.e. the
     * flit has arrived at least one cycle ahead than current cycle.</li>
     * <li>If such a channel is found then type of the first flit of the buffer
     * is checked.</li>
     * <li>If the flit is of header type then a new packet (vector of flits) is
     * generated and stored in the message center for the rest data flits to
     * arrive and complete this packet transmission.</li>
     * <li>If the flit is of data type then this flit is added to the
     * corresponding packet of this flit stored in the message center.</li>
     * </ul>
     *
     * @param curCycle current simulation cycle
     */
    public void forwardFlitToNodeMessageCenter(int curCycle) {
        int count = 0;
        Vector packet;
        Flit flit;

        lastInVCServed = (int) (++lastInVCServed % vcCount);
        while (count < vcCount) {
            if (null != inputBuffer.getBufferData(lastInVCServed)
                    && inputBuffer.hasFlitToSend(lastInVCServed)
                    && inputBuffer.getBufferData(lastInVCServed)
                    .getLastServiceTimeStamp() < curCycle) {
                // a VC found whose turn and whose data is to send
                if (inputBuffer.getBufferData(lastInVCServed).getType() == IConstants.HEADER_FLIT) {
                    // header flit. So need a new packet
                    flit = inputBuffer.removeBufferData(lastInVCServed,
                            curCycle);
                    flit.setLastServiceTimeStamp(curCycle);

                    packet = new Vector();
                    packet.add(flit);
                    receiveMessageList[flit.getVirtualChannelNo()] = packet;
                    nodeReceivedFlitCounter[flit.getVirtualChannelNo()] = flit
                            .getPacketLength() - 1;

                    if (curCycle > IConstants.WARM_UP_CYCLE) {

                        if (IConstants.CURRENT_NET == IConstants.NET_WK) {
                            System.out.println("Receive Header: Dest: " + Network.convertToDecimalAddress(address)
                                    + " Src: " + Network.convertToDecimalAddress(flit.getSource()) + " cycle: "
                                    + curCycle);
                        } else {
                            System.out.println("Receive Header: Dest: " + address
                                    + " Src: " + flit.getSource() + " cycle: "
                                    + curCycle);
                        }
                    }
                    if (IConstants.TRACE) {
                        try {
                            RandomAccessFile raf = new RandomAccessFile(
                                    IConstants.TRACE_FILE, "rw");
                            raf.seek(raf.length());
                            raf
                                    .writeBytes("\nCycle "
                                            + curCycle
                                            + " ( "
                                            + flit.getSource()
                                            + ","
                                            + flit.getDest()
                                            + ") "
                                            + " Header network.traffic.Flit("
                                            + flit.getSourceNode()
                                            + ","
                                            + flit.getDestinationNode()
                                            + ") is received at Message Center from network.unit.node.Node Input Buffer at network.unit.node.Node "
                                            + address + " VC index "
                                            + lastInVCServed);

                            raf.close();
                        } catch (IOException ioex) {
                        }
                    }

                    break;

                } else {
                    // data flit. need to identify the packet
                    flit = inputBuffer.removeBufferData(lastInVCServed,
                            curCycle);
                    flit.setLastServiceTimeStamp(curCycle);

                    packet = receiveMessageList[flit.getVirtualChannelNo()];

                    if (IConstants.TRACE) {
                        try {
                            RandomAccessFile raf = new RandomAccessFile(
                                    IConstants.TRACE_FILE, "rw");
                            raf.seek(raf.length());
                            raf
                                    .writeBytes("\nCycle "
                                            + curCycle
                                            + " ( "
                                            + flit.getSource()
                                            + ","
                                            + flit.getDest()
                                            + ") "
                                            + " Data network.traffic.Flit is received at Message Center from network.unit.node.Node Input Buffer at network.unit.node.Node "
                                            + address + " VC index "
                                            + lastInVCServed);

                            raf.close();
                        } catch (IOException ioex) {
                        }
                    }

                    // if(curCycle>network.common.IConstants.WARM_UP_CYCLE)
                    // System.out.println("Receive Data: Dest: " + address + "
                    // Src: "+ flit.getSource() + " cycle: "+ curCycle);

                    if (null != packet) {
                        packet.add(flit);
                    } else {
                        System.err.println("Packet not found");
                    }

                    nodeReceivedFlitCounter[flit.getVirtualChannelNo()]--;

                    if (nodeReceivedFlitCounter[flit.getVirtualChannelNo()] == 0) {
                        if (curCycle > IConstants.WARM_UP_CYCLE) {

                            if (curCycle > IConstants.WARM_UP_CYCLE) {
                                if (IConstants.CURRENT_NET == IConstants.NET_WK) {
                                    System.out.println("Receive Completed: Dest: "
                                            + Network.convertToDecimalAddress(address) + " Src: " + Network.convertToDecimalAddress(flit.getSource())
                                            + " cycle: " + curCycle + " hop: "
                                            + flit.getHopCount() + " genTime: "
                                            + flit.getGenTimeStamp() + " time: "
                                            + (curCycle - flit.getGenTimeStamp()));
                                } else {
                                    System.out.println("Receive Completed: Dest: "
                                            + address + " Src: " + flit.getSource()
                                            + " cycle: " + curCycle + " hop: "
                                            + flit.getHopCount() + " genTime: "
                                            + flit.getGenTimeStamp() + " time: "
                                            + (curCycle - flit.getGenTimeStamp()));
                                }
                            }
                            NetworkManager.getStatDataInstance()
                                    .incrementPacketDelay(nodeListIndex,
                                            curCycle - flit.getGenTimeStamp());
                            NetworkManager.getStatDataInstance()
                                    .incrementPacketHopCount(nodeListIndex,
                                            flit.getHopCount());

                            // debug purpose

                        }
                        receiveMessageList[flit.getVirtualChannelNo()] = null;
                        dumpMessage(packet);
                    }
                    break;
                }
            }
            lastInVCServed = (int) (++lastInVCServed % vcCount);
            count++;
            // otherwise try for the next VC to send
        }

    }

    /**
     * This method is invoked after a packet is completely received. This method
     * can be used to get the total hops traversed by this packet.
     *
     * @param packet received packet
     */
    private void dumpMessage(Vector packet) {
        int count = 1, prevHop = 0;
        Flit flit;

        if (packet.isEmpty() == false) {
            flit = (Flit) packet.firstElement();
            prevHop = flit.getHopCount();
            // System.out.println("New Packet Received from " +
            // flit.getSourceNode() + " to " +
            // flit.getDestinationNode() + " Length = " +
            // flit.getPacketLength()) ;
        }
        while (packet.isEmpty() == false) {
            flit = (Flit) packet.remove(0);
            if (prevHop != flit.getHopCount()) {
                System.out.println("Hop Mismatch " + prevHop + " , "
                        + flit.getHopCount());
                // System.out.println("network.traffic.Flit " + count + " = " + flit.toString())
                // ;
            }
            count++;
        }
    }

    /**
     * This method is called after the operation of every cycle to update
     * statistical counters and reset the temporary status variables.
     *
     * @param curCycle current simulation cycle
     */
    public void updateStatusAfterCycle(int curCycle) {
        inputBuffer.updateStatusAfterCycle();
        outputBuffer.updateStatusAfterCycle();

        // stat
        if (this.linkUsed == true) {
            this.linkUsed = false;
            if (curCycle > IConstants.WARM_UP_CYCLE) {
                NetworkManager.getStatDataInstance().incrementNodeLinkUse(
                        nodeListIndex);
            }
        }
        if (curCycle > IConstants.WARM_UP_CYCLE) {
            NetworkManager.getStatDataInstance().incrementNodeInputBufferUse(
                    nodeListIndex, inputBuffer.getNumSlotUsed());
            NetworkManager.getStatDataInstance().incrementNodeOutputBufferUse(
                    nodeListIndex, outputBuffer.getNumSlotUsed());
        }
    }

    /**
     * Sets the nodeListIndex variable of this class.
     *
     * @param index index of this node class in the universal nodelist
     */
    public void setNodeListIndex(int index) {
        this.nodeListIndex = index;
    }

    /**
     * Returns the nodeListIndex variable of this class.
     *
     * @return index of this node class in the universal nodelist
     */
    public int getNodeListIndex() {
        return this.nodeListIndex;
    }

}