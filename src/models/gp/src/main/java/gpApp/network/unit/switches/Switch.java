package gpApp.network.unit.switches;

import gpApp.network.traffic.Flit;
import gpApp.network.unit.switches.buffer.InputVCBuffer;
import gpApp.network.unit.switches.router.Router;
import gpApp.network.unit.node.Node;
import gpApp.network.unit.switches.link.InputLinkController;
import gpApp.network.unit.switches.link.OutputLinkController;

/**
 * <p/>
 * Switches are the objects of the network where switching of flits from various
 * input ports to output ports are performed as well as the path which will be
 * taken from the various output paths is selected (routing). To support the
 * wormhole switching the switch provides various facilities.
 * </p>
 * <p/>
 * <p/>
 * The network.unit.switches.Switch interface defines all the necessary functions that any class
 * simulating the characteristics of the switch in the NoC must implement.
 * </p>
 *
 * @version 1.0
 */
public interface Switch {
    /**
     * Sets the maximum number of physical links information.
     *
     * @param no maximum number of physical links
     */
    public abstract void setNoOfPhysicalLink(int no);

    /**
     * Returns the maximum number of physical links information.
     *
     * @return maximum number of physical links
     */
    public abstract int getNoOfPhysicalLink();

    /**
     * Sets the number of virtual channel per physical link information.
     *
     * @param noVlink number of virtual channel per physical link
     */
    public abstract void setNoOfVirtualLink(int noVlink);

    /**
     * Returns the number of virtual channel per physical link information.
     *
     * @return number of virtual channel per physical link
     */
    public abstract int getNoOfVirtualLink();

    /**
     * Sets the address of the switch.
     *
     * @param addr address of the switch
     */
    public abstract void setAddress(int addr);

    /**
     * Returns the address of the switch.
     *
     * @return address of the switch
     */
    public abstract int getAddress();

    /**
     * Assigns a node reference to which this network.unit.switches.Switch is connected.
     *
     * @param node   network.unit.node.Node
     * @param linkNo the physical link number that connects the node
     * @see Node
     */
    public abstract void setAdjacentNode(Node node, int linkNo);

    /**
     * Returns the number of adjacent node to which the switch is connected.
     *
     * @return number of adjacent node
     */
    public abstract int getNumAdjacentNode();

    /**
     * Assigns a switch reference to which this network.unit.switches.Switch is connected.
     *
     * @param switchref adjacent switch reference
     * @param linkNo    the physical link number that connects these two switches
     */
    public abstract void setAdjacentSwitch(Switch switchref, int linkNo);

    /**
     * Returns the input link controller of the switch on a particular physical
     * link.
     *
     * @param linkNo link number
     * @return input link controller reference of the switch on the link
     *         'linkNo'
     * @see InputLinkController
     */
    public abstract InputLinkController getInputLinkController(int linkNo);

    /**
     * Assigns an input link controller to the switch on a particular physical
     * link.
     *
     * @param linkNo link number
     * @param iLC    input link controller
     * @see InputLinkController
     */
    public abstract void setInputLinkController(int linkNo,
        InputLinkController iLC);

    /**
     * Returns the output link controller of the switch on a particular physical
     * link.
     *
     * @param linkNo link number
     * @return output link controller reference of the switch on the link
     *         'linkNo'
     */
    public abstract OutputLinkController getOutputLinkController(int linkNo);

    /**
     * Assigns an output link controller to the switch on a particular physical
     * link.
     *
     * @param linkNo link number
     * @param oLC    output link controller
     */
    public abstract void setOutputLinkController(int linkNo,
        OutputLinkController oLC);

    /**
     * Resets the switching info vector of the network.unit.switches.Switch instance which keeps track
     * of the assignment of a output virtual channel to a packet on a input
     * virtual channel.
     */
    public abstract void resetSwitchingInfoVector();

    /**
     * Adds a flit to the input buffer on a particular physical link.
     *
     * @param linkNo   link number
     * @param flit     network.traffic.Flit data
     * @param curCycle simulation cycle
     * @return true-if flit can be added, false-otherwise.
     * @see InputVCBuffer#addBufferData(Flit,int,int)
     */
    public abstract boolean addInputBufferData(int linkNo, Flit flit,
        int curCycle);

    /**
     * Adds a flit to the output buffer on a particular physical link.
     *
     * @param linkNo   link number
     * @param flit     network.traffic.Flit data
     * @param vcId     virtual channel number
     * @param curCycle simulation cycle
     * @return true-if flit can be added, false-otherwise.
     * @see OutputLinkController#addOutputBufferData(Flit,int,int)
     */
    public abstract boolean addOutputBufferData(int linkNo, Flit flit,
        int vcId, int curCycle);

    /**
     * Sets the path information for a pair of input virtual channel and output
     * virtual channel.
     *
     * @param dest ((output link number * number of VC per link) + output virtual
     *             channel number)
     * @param src  ((input link number * number of VC per link) + input virtual
     *             channel number)
     * @return true-if the path can be set, false-otherwise
     * @see InputLinkController#setOutPathRequest(int)
     */
    public abstract boolean setSwitchingInfoVector(int dest, int src);

    /**
     * <p/>
     * Returns the input virtual channel information for a packet flowing
     * through the output virtual channel.
     * </p>
     * <p/>
     * This pair of values identify the switching path in the switch for a
     * packet.
     * </p>
     * <p/>
     * Here the input virtual channel information is encoded as <i> ((input link
     * number * number of VC per link) + input virtual channel number)</i> and
     * the output virtual channel information is encoded as <i>((output link
     * number * number of VC per link) + output virtual channel number)</i>.
     * </p>
     *
     * @param dest encoded output virtual channel information
     * @return encoded input virtual channel information
     * @see InputLinkController#setOutPathRequest(int)
     */
    public abstract int getSwitchingInfoVector(int dest);

    /**
     * Removes a flit data from an input virtual channel of the input buffer on
     * a particular physical link.
     *
     * @param linkNo   input physical link number
     * @param vcId     input virtual channel number
     * @param curCycle simulation cycle
     * @return network.traffic.Flit
     * @see InputLinkController#removeInputBufferData(int,int)
     */
    public abstract Flit removeInputBufferData(int linkNo, int vcId,
        int curCycle);

    /**
     * Removes a flit data from an output virtual channel of the output buffer
     * on a particular physical link.
     *
     * @param linkNo   output physical link number
     * @param vcId     output virtual channel number
     * @param curCycle simulation cycle
     * @return network.traffic.Flit
     * @see OutputLinkController#removeOutputBufferData(int,int)
     */
    public abstract Flit removeOutputBufferData(int linkNo, int vcId,
        int curCycle);

    /**
     * Determines the outgoing link to switch a packet orgininated from a source
     * node (src) and destined to the destination node (dest).
     *
     * @param src  address of the source node
     * @param dest address of the destination node
     * @return outgoing physical link of the switch
     * @see Router#determineRoute(int,int,int)
     */
    public abstract int determineRoute(int src, int dest);

    /**
     * Checks whether there is one or more slots in the input virtual channel of
     * an input buffer or not.
     *
     * @param linkNo input physical link number
     * @param vcId   virtual channel number on the input link
     * @return true-if more slots are available, false-otherwise
     * @see InputLinkController#hasFreeSlotInVCBuffer(int)
     */
    public abstract boolean hasFreeSlotInVCBuffer(int linkNo, int vcId);

    // public abstract int getSwitchingRoute (int src);

    /**
     * Assigns or updates the path information for the flits stored in the input
     * buffer.
     *
     * @param curCycle simualtion cycle
     * @see InputLinkController#setOutPathRequest(int)
     */
    public abstract void updateSwitchOutPathRequest(int curCycle);

    /**
     * Transfers the flits from the input buffer to the output buffer.
     *
     * @param curCycle simulation cycle
     */
    public abstract void moveInputBufferToOutputBuffer(int curCycle);

    /**
     * Transfers the flits from the output buffer to the input buffer of the
     * adjacent switches or node.
     *
     * @param curCycle
     */
    public abstract void moveSwitchOutputBufferToInputBufferOfNodeSwitch(
        int curCycle);

    /**
     * Checks if the virtual channels in the input buffer of a particular
     * physical link is available to be assigned to a new incoming packet.
     *
     * @param linkNo input physical link number
     * @param vcId   virtual channel number
     * @return true-if the virtual channel is free, false-otherwise.
     * @see InputLinkController#isVCFree(int)
     */
    public abstract boolean isVCFreeInSwitch(int linkNo, int vcId);

    /**
     * Returns the number of physical links for the switch
     *
     * @return number of valid physical links
     */
    public abstract int getNumLinkActive();

    /**
     * Updates the statistical counters for a particular simulation cycle and
     * re-initializes the temporary status variables.
     *
     * @param curCycle simulation cycle
     */
    public abstract void updateStatusAfterCycle(int curCycle);

    /**
     * Instantiates a concrete network.unit.switches.router.Router instance for this particular architecture.
     */
    public abstract void createRouter();

}
