package network.unit.switches.buffer;

import network.traffic.Flit;
import network.common.IConstants;

import java.util.Vector;

/**
 * There is one input buffer for each of the input physical link. As we are
 * using wormhole switching and virtual channel technique to avoid deadlock
 * developed in wormhole switching input buffer provides these virtual channel
 * support. A virtual channel is simply a buffer. So input buffer object holds
 * as many buffer objects as many virtual channels are required. network.unit.switches.buffer.InputVCBuffer
 * class defines such input buffer object.
 *
 * @version 1.0
 */

public class InputVCBuffer {
    /**
     * A Vector type object instantiated to hold the Buffer objects. This vector
     * holds VCN number of buffer objects, where VCN indicates number of virtual
     * channels per physical channel used in the system.
     */
    private Vector data[];

    /**
     * Number of virtual channels per physical channel used in the system.
     */
    private int vcCount;

    /**
     * The physical link no. on which this input buffer is associated.
     */
    private int pLinkNo;

    /**
     * An array of integer of size VCN. One cell is for every virtual channel.
     * It holds the routing path (outgoing physical channel ) for the incoming
     * flits.
     */
    private int[] routeInfo;

    /**
     * An array of integer of size VCN. One cell is for every virtual channel.
     * It identifies the outgoing virtual channel on the routing path (outgoing
     * physical channel, found from routeInfo) for the incoming flits.
     */
    private int[] pathInfo;

    /**
     * An array of integer of size VCN. One cell is for every virtual channel.
     * Every cell contains the number of remaining flits to transfer for the
     * current packet before allocating the channel to a new packet. It is used
     * to track the number of flit is to pass (in a packet)
     */
    private int[] flitCounter;

    /**
     * An array of booleans of size VCN. It is used temporarily to hold the
     * stattus of a buffer to indicate whether it can be used in the next cycle
     * for storage. This value is set when a flit is removed from the buffer.
     */
    private boolean buffMidStatus[];

    /**
     * Default Constructor.
     *
     * @param linkNo Physical link no
     */
    public InputVCBuffer(int linkNo) {
        this(IConstants.DEFAULT_VC_COUNT, linkNo);
    }

    /**
     * Instantiates all the necessary data required to manage the input buffer
     * and then initialize all the data.
     *
     * @param vcCount Number of virtual channel for the input physical link.
     * @param linkNo  Physical link no.
     */
    public InputVCBuffer(int vcCount, int linkNo) {
        this.pLinkNo = linkNo;
        this.vcCount = vcCount;
        data = new Vector[vcCount];
        routeInfo = new int[vcCount];
        pathInfo = new int[vcCount];
        buffMidStatus = new boolean[vcCount];
        flitCounter = new int[vcCount];

        for (int i = 0; i < vcCount; i++) {
            data[i] = new Vector();
            data[i].ensureCapacity(IConstants.NUM_FLIT_PER_BUFFER);
            routeInfo[i] = -1;
            pathInfo[i] = -1;
            buffMidStatus[i] = false;
            flitCounter[i] = 0;
        }
    }

    /**
     * Stores a flit in a specified virtual channel of the input buffer. The
     * method is called by an Input Link network.Controller. Everytime a flit enters the
     * input buffer, the no. of hops it has crossed is increased in this method.
     * Similarly, the last service timestamp of the flit is also modified.
     *
     * @param flit     network.traffic.Flit data
     * @param vcId     Virtual Channel no. to which this flit has to be delegated
     * @param curCycle Current timestamp
     * @return true (always, as before invoking this method, usually the status
     *         of the buffer is checked whether it can hold anymore flit)
     */
    public boolean addBufferData(Flit flit, int vcId, int curCycle) {
        // header flit. So have to keep track of how many more flit is to
        // process.
        if (flit.getType() == IConstants.HEADER_FLIT) {
            flitCounter[vcId] = flit.getPacketLength();
        }
        flit.setLastServiceTimeStamp(curCycle);
        flit.increaseHop();
        data[vcId].add(flit);
        return true;
    }

    /**
     * Remove the flit from the specified virtual channel of the input buffer.
     * The method is called by an Input Link network.Controller. The flit can be removed
     * if it is stored there at least one cycle ahead of the current timestamp
     * specified by 'curCycle'.
     *
     * @param vcId     Virtual channel number from where the flit is to be removed
     * @param curCycle Current timestamp
     * @return the flit data if available at the buffer and the flit is stored
     *         in the buffer one cycle prior to this operation, else returns
     *         null
     */
    public Flit removeBufferData(int vcId, int curCycle) {
        Flit flit = (Flit) data[vcId].firstElement();
        if (flit.getLastServiceTimeStamp() < curCycle) {
            flit = (Flit) data[vcId].remove(0);
            flit.setLastServiceTimeStamp(curCycle);
            buffMidStatus[vcId] = true; // the storage corresponding to this can
            // be used in next cycle
            // flitCounter[vcId]--; // flitCounter will be decremented later.
            return flit;
        }
        return null;
    }

    /**
     * Retrieve the flit from the specified virtual channel number of this input
     * buffer object. Here the flit is not removed from the buffer. The flit may
     * be required for testing. For those cases this method is called.
     *
     * @param vcId virtual channel number
     * @return network.traffic.Flit data
     */
    public Flit getBufferData(int vcId) {
        if (data[vcId].size() == 0) {
            return null;
        }
        return (Flit) data[vcId].firstElement();

    }

    /**
     * Re-initializes the temporary status variables after each cycle. If a flit
     * has been removed from the buffer, it gets such information from the
     * buffMidStatus variable. Then it decrements the flitCounter variables.
     * Finallly, if all flits of the packet leaves the input buffer, then it
     * re-initializes other route-info holding variables.
     */
    public void updateStatusAfterCycle() {
        for (int i = 0; i < vcCount; i++) {
            if (buffMidStatus[i] == true) {
                flitCounter[i]--;
                if (flitCounter[i] == 0) {
                    routeInfo[i] = -1;
                    pathInfo[i] = -1;
                }
                buffMidStatus[i] = false;
            }
        }
    }

    /**
     * Checks whether a virtual channel can be assigned to an incoming packet.
     *
     * @param vcId Virtual channel no.
     * @return true-if the VC is free, false-otherwise.
     */

    public boolean isVCFree(int vcId) {
        if ((buffMidStatus[vcId] == false) && (flitCounter[vcId] == 0)) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether the virtual channel can hold another incoming flit of the
     * packet that already holds the virtual channel.
     *
     * @param vcId Virtual channel no.
     * @return true-if the VC has free slots, false-otherwise.
     */
    public boolean hasFreeSlotInVC(int vcId) {
        if ((data[vcId].size() < IConstants.NUM_FLIT_PER_BUFFER)
                && (buffMidStatus[vcId] == false)) {
            return true;
        } else if (((data[vcId].size() + 1) < IConstants.NUM_FLIT_PER_BUFFER)
                && (buffMidStatus[vcId] == true)) {
            return true;
        }
        return false;
    }

    /**
     * Sets the outgoing physical link no. that the packet occupying the virtual
     * channel must follow to reach the destination.
     *
     * @param vcId Virtual channel no.
     * @param dest Outgoing physical link no.
     */
    public void setRouteInfo(int vcId, int dest) {
        routeInfo[vcId] = dest;
    }

    /**
     * Returns the outgoing physical link no. that the packet occupying the
     * virtual channel must follow to reach the destination.
     *
     * @param vcId Virtual channel no.
     * @return Outgoing physical link no.
     */
    public int getRouteInfo(int vcId) {
        return routeInfo[vcId];
    }

    /**
     * Resets the outgoing physical link no. information for the virtual channel
     *
     * @param vcId Virtual channel no.
     */
    public void resetRouteInfo(int vcId) {
        routeInfo[vcId] = -1;
    }

    /**
     * Returns a free virtual channel on the physical link.
     *
     * @return free Virtual channel no.
     */
    public int getFreeVC() {
        for (int i = 0; i < vcCount; i++) {
            if ((buffMidStatus[i] == false) && (flitCounter[i] == 0)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns whether there are more flits to send from the virtual channel.
     *
     * @param vcId Virtual channel no.
     * @return true-if one or more flits occupy the virtual channel, false-if
     *         the virtual channel is empty.
     */
    public boolean hasFlitToSend(int vcId) {
        return data[vcId].size() > 0;
    }

    /**
     * Sets the outgoing path information for the packet occupying the virtual
     * channel. Path information is equals to (outgoing physical link no.* total
     * no. of virtual channel + outgoing virtual channel no. on the outgoing
     * link)
     *
     * @param vcId Virtual channel no
     * @param dest Path information
     */
    public void setPathInfo(int vcId, int dest) {
        pathInfo[vcId] = dest;
    }

    /**
     * Returns the outgoing path information for the packet occupying the
     * virtual channel.
     *
     * @param vcId Virtual channel no
     * @return Path information
     */
    public int getPathInfo(int vcId) {
        return pathInfo[vcId];
    }

    /**
     * Returns total no. of slots occupied by various flits in the input buffer.
     *
     * @return Number of slots used
     */
    public int getNumSlotUsed() {
        int i, count = 0;
        for (i = 0; i < this.vcCount; i++) {
            count += data[i].size();
        }
        return count;
    }

}
