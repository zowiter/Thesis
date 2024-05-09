package network.unit.switches.link;

import network.unit.switches.Switch;
import network.unit.switches.buffer.OutputVCBuffer;
import network.traffic.Flit;

/**
 * Output Link network.Controller controls the outgoing flits. It helps in sending the
 * flits to the input link controllers of adjacent switches and node by using
 * round robin algorithm among the virtual channels as at a time a single flit
 * from one virtual channel can be sent. It works as a wrapper of the output
 * buffer where output virtual channels are implemented.
 *
 * @version 1.0
 */

public class OutputLinkController {
    /**
     * Base class type variable of different types of switches for different
     * types of networks. It holds the reference of the switch to which the link
     * controller is attached.
     *
     * @see network.unit.switches.Switch
     */
    private Switch parentSwitch;

    /**
     * {@link network.unit.switches.buffer.OutputVCBuffer}s associated with the physical link
     */
    private OutputVCBuffer outputBuffer;

    /**
     * The input physical link to whitch the link controller is associated
     */
    private int linkNo;

    /**
     * The number of virtual channels per physical link
     */
    private int numVCCount;

    /**
     * The number of physical links for the switch
     */
    private int numLinkCount;

    /**
     * Constructor for the network.unit.switches.link.OutputLinkController class.
     *
     * @param parent    switch to which the link controller is attached.
     * @param linkNo    physical link to which the link controller is attached.
     * @param vcCount   total no. of virtual channels per physical link
     * @param linkCount total no. of physical links associated with the switch
     */
    public OutputLinkController(Switch parent, int linkNo, int vcCount,
                                int linkCount) {
        this.parentSwitch = parent;
        this.outputBuffer = new OutputVCBuffer(vcCount, linkNo);
        this.linkNo = linkNo;
        this.numVCCount = vcCount;
        this.numLinkCount = linkCount;

    }

    /**
     * Adds a flit to a output FIFO buffer at a specified virtual channel.
     *
     * @param flit     flit data, either Header or Data flit
     * @param vcId     virtual channel no. to which this flit is to be added/
     * @param curCycle cycle at which the operation is being performed
     * @return true - if flits can be added, false - otherwise
     */
    public boolean addOutputBufferData(Flit flit, int vcId, int curCycle) {
        return outputBuffer.addBufferData(flit, vcId, curCycle);
    }

    /**
     * Removes a flit from a virtual channel.
     *
     * @param vcId     virtual channel index
     * @param curCycle current simulation cycle
     * @return network.traffic.Flit
     * @see OutputVCBuffer
     */
    public Flit removeOutputBufferData(int vcId, int curCycle) {
        return outputBuffer.removeBufferData(vcId, curCycle);
    }

    /**
     * Returns true, if the virtual channel has more slots to store additional
     * flit. False-otherwise.
     *
     * @param vcId virtual channel index
     * @return boolean value
     * @see OutputVCBuffer
     */
    public boolean hasFreeSlotInVCBuffer(int vcId) {
        return outputBuffer.hasFreeSlotInVC(vcId);
    }

    /**
     * Returns true, if the virtual channel has more flits to send.
     * False-otherwise.
     *
     * @param vcId virtual channel index
     * @return boolean value
     * @see OutputVCBuffer
     */
    public boolean hasFlitToSend(int vcId) {
        return outputBuffer.hasFlitToSend(vcId);
    }

    public void updateStatusAfterCycle() {
        outputBuffer.updateStatusAfterCycle();
    }

    /**
     * Returns a flit from the virtual channel specified in the parameter
     *
     * @param vcId virtual channel number
     * @return network.traffic.Flit data
     * @see OutputVCBuffer
     * @see network.traffic.Flit
     */
    public Flit getBufferData(int vcId) {
        return outputBuffer.getBufferData(vcId);
    }

    /**
     * Returns free virtual channel index for the corresponding physical link.
     *
     * @return the index of the free virtual channel for this output link
     * @see OutputVCBuffer
     */
    public int getFreeVC() {
        return outputBuffer.getFreeVC();
    }

    /**
     * An alternative of getFreeVC();
     *
     * @param vc virtual channel number
     * @return a free output virtual channel number
     */
    public int getFreeVC_NEW(Flit vc) {
        return outputBuffer.getFreeVC_NEW(vc);
    }

    /**
     * Returns the output buffer associated with the output link controller.
     *
     * @return output buffer
     * @see OutputVCBuffer
     */
    public OutputVCBuffer getOutputBuffer() {
        return outputBuffer;
    }

    // not used
    /*
      * public void setOutPathRequest(int curCycle) { network.unit.switches.buffer.InputVCBuffer ivcbuff; int
      * i,j; int newVC;
      *
      * for(i=0;i<numLinkCount;i++) { if(null !=
      * this.parentSwitch.getInputLinkController(i)) { ivcbuff =
      * this.parentSwitch.getInputLinkController(i).getInputBuffer() ; for (j = 0 ;
      * j < this.numVCCount ; j++) { //will be passed through this link and come
      * before this cycle if (null != ivcbuff.getBufferData(j) &&
      * ivcbuff.getRouteInfo(j) == this.linkNo &&
      * ivcbuff.getBufferData(j).getLastServiceTimeStamp() < curCycle) { //header
      * flit. So we need a free Virtual Buffer if
      * (ivcbuff.getBufferData(j).getType() == network.common.IConstants.HEADER_FLIT) { newVC =
      * this.outputBuffer.getFreeVC() ; // a free buffer found if (newVC >= 0) {
      * parentSwitch.setSwitchingInfoVector(i * numVCCount + j, linkNo *
      * numVCCount + newVC) ; //outputBuffer.setVCMappingInfo(newVC, i *
      * numVCCount + j); } // can not be transferred to output port. network.traffic.Flit is
      * blocked here. } else //data flit. So we have assigned the VC earlier And
      * that is to maintain { // get the previous destination port and Virtual
      * Buffer ID int dest = this.outputBuffer.getOutputVC(i * numVCCount + j) ;
      * if (dest >= 0) { parentSwitch.setSwitchingInfoVector(i * numVCCount + j,
      * dest) ; } } } } } } }
      */
}
