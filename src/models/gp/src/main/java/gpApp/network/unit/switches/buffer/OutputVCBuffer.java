package gpApp.network.unit.switches.buffer;

import gpApp.network.traffic.Flit;
import gpApp.network.common.IConstants;

import java.util.Vector;

/**
 * There is one output buffer for each of the output physical link. As we are
 * using wormhole switching and virtual channel technique to avoid deadlock
 * developed in wormhole switching output buffer provides these virtual channel
 * support. A virtual channel is simply a buffer. So buffer buffer object holds
 * as many buffer objects as many virtual channels are required. OutputBuffer
 * class defines such output buffer object.
 * <p/>
 * Note: some of the codes have been commented as they are deprecated, no longer
 * used. ['not used' precedes such comments.]
 *
 * @version 1.0
 */

public class OutputVCBuffer {
    /**
     * A Vector type object instantiated to hold the Buffer objects. This vector
     * holds VCN number of buffer objects, where VCN indicates number of virtual
     * channels per physical channel used in the system.
     */
    private Vector data[];

    /**
     * The physical link no. on which this input buffer is associated.
     */
    private int pLinkNo;

    /**
     * Number of virtual channels per physical channel used in the system.
     */
    private int numVCCount;

    // not used
    // private int[] vcMappingInfo ; //to track the conversion from ine VCId to
    // another VCId

    /**
     * An array of integer of size VCN. One cell is for every virtual channel.
     * It is used to track the number of flit is to pass (in a packet)
     */
    private int[] flitCounter; // to track the number of flit is to pass (in a

    // packet)

    /**
     * An array of booleans of size VCN. It is used temporarily to hold the
     * stattus of a buffer to indicate whether it can be used in the next cycle
     * for storage. This value is set when a flit is removed from the buffer.
     */
    private boolean buffMidStatus[];

    /**
     * An array of booleans of size VCN. It is used temporarily to hold the
     * stattus of a buffer to indicate whether it has been assigned in the
     * simulation cycle or not.
     */
    private boolean buffAssignedStatus[];

    /**
     * Default Constructor.
     *
     * @param linkNo Physical link no
     */
    public OutputVCBuffer(int linkNo) {
        this(IConstants.DEFAULT_VC_COUNT, linkNo);
    }

    /**
     * Instantiates all the necessary data required to manage the output buffer
     * and then initialize all the data.
     *
     * @param vcCount Number of virtual channel for the output physical link.
     * @param linkNo  Physical link no.
     */
    public OutputVCBuffer(int vcCount, int linkNo) {
        this.numVCCount = vcCount;
        this.pLinkNo = linkNo;
        data = new Vector[vcCount];
        flitCounter = new int[vcCount];
        buffMidStatus = new boolean[vcCount];
        buffAssignedStatus = new boolean[vcCount];

        // not used
        // vcMappingInfo = new int[vcCount] ;

        for (int i = 0; i < vcCount; i++) {
            data[i] = new Vector();
            data[i].ensureCapacity(IConstants.NUM_FLIT_PER_BUFFER);
            flitCounter[i] = 0;
            buffMidStatus[i] = false;
            buffAssignedStatus[i] = false;

            // not used
            // vcMappingInfo[i] = -1 ;
        }
    }

    /**
     * Stores a flit in a specified virtual channel of the output buffer. The
     * method is called by an Input Link network.Controller. Everytime a flit needs to
     * be transferred to an adjacent switch or node, at firth the flit is
     * transferred to the output buffer of the destined physical link from an
     * input buffer of the switch. Similarly, the last service timestamp of the
     * flit is also modified.
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
        flit.setVirtualChannelNo(vcId);
        flit.setLastServiceTimeStamp(curCycle);
        data[vcId].add(flit);
        return true;
    }

    /**
     * Remove the flit from the specified virtual channel of the output buffer.
     * The method is called by an Input Link network.Controller. The flit can be removed
     * if it is stored there at least one cycle ahead of the current timestamp
     * specified by 'curCycle'.
     *
     * @param vcId     Virtual channel number from where the flit is to be removed
     * @param curCycle Current timestamp
     * @return the flit, if there is any flit stored in the buffer one cycle
     *         prior to this operation, else null
     */
    public Flit removeBufferData(int vcId, int curCycle) {
        Flit flit = (Flit) data[vcId].get(0);
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
     * Re-initializes the temporary status variables after each cycle. If a flit
     * has been removed from the buffer, it gets such information from the
     * buffMidStatus variable. Then it decrements the flitCounter variables.
     * Finallly, if all flits of the packet leaves the input buffer, then it
     * re-initializes other route-info holding variables.
     */
    public void updateStatusAfterCycle() {
        for (int i = 0; i < numVCCount; i++) {
            if (buffMidStatus[i] == true) {
                flitCounter[i]--;
                if (flitCounter[i] == 0) {
                    // not used
                    // vcMappingInfo[i] = -1 ;
                }
                buffMidStatus[i] = false;
            }
        }
        for (int i = 0; i < numVCCount; i++) {
            buffAssignedStatus[i] = false;

        }
    }

    // not used
    /*
      * public int getOutputVC (int src) { for (int i = 0 ; i < numVCCount ; i++) {
      * if (vcMappingInfo[i] == src) { return this.pLinkNo * numVCCount + i ; } }
      * return -1 ; }
      */

    /**
     * Retrieve the flit from the specified virtual channel number of this
     * output buffer object. Here the flit is not removed from the buffer. The
     * flit may be required for testing. For those cases this method is called.
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
     * Returns a free virtual channel on the physical link.
     *
     * @return free Virtual channel no.
     */
    public int getFreeVC() {
        for (int i = 0; i < numVCCount; i++) {
            if ((buffAssignedStatus[i] == false) && (flitCounter[i] == 0)) {
                buffAssignedStatus[i] = true;
                return i;
            }
        }
        return -1;
    }

    /**
     * An alternative of the getFreeVC()
     *
     * @param f flit to get its input vc no
     * @return a free output channel if available, -1 otherwise
     */
    public int getFreeVC_NEW(Flit f) {
        int i = f.getHopCount();

        if (i >= this.numVCCount) {
            i = this.numVCCount - 1;
        }

        if ((buffAssignedStatus[i] == false) && (flitCounter[i] == 0)) {
            buffAssignedStatus[i] = true;
            return i;
        } else
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

    // not used
    /*
      * public void setVCMappingInfo (int vcId, int src) { vcMappingInfo[vcId] =
      * src ; }
      */

    /**
     * Returns the number of used VCs in a particular simulation cycle.
     *
     * @return the number of used VCs
     */
    public int getNumUsedVC() {
        int count = 0;
        for (int i = 0; i < numVCCount; i++) {
            if ((buffAssignedStatus[i] == true) || (flitCounter[i] > 0)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns total no. of slots occupied by various flits in the output
     * buffer.
     *
     * @return Number of slots used
     */
    public int getNumSlotUsed() {
        int i, count = 0;
        for (i = 0; i < this.numVCCount; i++) {
            count += data[i].size();
        }
        if (IConstants.CURRENT_VC_COUNT * IConstants.NUM_FLIT_PER_BUFFER < count)
            System.out.println("\n\nBuf Over Use\n\n");
        return count;
    }
}
