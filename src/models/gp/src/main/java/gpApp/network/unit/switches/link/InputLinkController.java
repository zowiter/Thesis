package gpApp.network.unit.switches.link;

import gpApp.network.unit.switches.Switch;
import gpApp.network.unit.switches.buffer.InputVCBuffer;
import gpApp.network.traffic.Flit;
import gpApp.network.common.IConstants;

/**
 * network.unit.switches.link.InputLinkController controls the incoming flits and helps in determining the
 * route of the flits in a switch. It works as a wrapper of the input buffer
 * where input virtual channels are implemented.
 *
 * @version 1.0
 */

public class InputLinkController {
    /**
     * Base class type variable of different types of switches for different
     * types of networks. It holds the reference of the switch to which the link
     * controller is attached.
     *
     * @see Switch
     */
    private Switch parentSwitch;

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
     * {@link InputVCBuffer}s associated with the physical link
     */
    private InputVCBuffer inputBuffer;

    /**
     * Constructor for the network.unit.switches.link.InputLinkController class.
     *
     * @param parent    switch to which the link controller is attached.
     * @param linkNo    physical link to which the link controller is attached.
     * @param vcCount   total no. of virtual channels per physical link
     * @param linkCount total no. of physical links associated with the switch
     */
    public InputLinkController(Switch parent, int linkNo, int vcCount,
                               int linkCount) {
        this.parentSwitch = parent;
        this.linkNo = linkNo;
        this.numLinkCount = linkCount;
        this.numVCCount = vcCount;
        this.inputBuffer = new InputVCBuffer(vcCount, linkNo);
    }

    /**
     * Adds an incoming flit to a input FIFO buffer at a specified virtual
     * channel.
     * <p/>
     * Invoked by the adjacent IP node and Switches of the parent switch
     * through the reference of the parent switch. It performs its
     * functionalities in following steps.
     * <ul>
     * <li> Determines the type of the incoming flit.</li>
     * <li> If the incoming flit is of header type then checks whether the
     * virtual channel specified in the flit is empty.</li>
     * <li> If the specified virtual channel is empty then adds this header flit
     * to that virtual channel. Determines the route through which the flit will
     * travel with the help of parent switch�s router. Then updates the found
     * route information in the specified virtual channel of the input buffer.</li>
     * <li> If specified virtual channel is not empty then the header flits is
     * lost and so subsequent data flits are also lost.</li>
     * <li> If the incoming flit is data type then checks whether the specified
     * virtual channel in the flit has at least one free slot of buffer to store
     * this flit. Otherwise the data flit is lost.</li>
     * </ul>
     *
     * @param flit     flit data, either Header or Data flit
     * @param curCycle cycle at which the operation is being performed
     * @return boolean value whether the operation has been successful or not.
     * @see InputVCBuffer
     * @see Flit
     */

    public boolean addInputBufferData(Flit flit, int curCycle) {
        int routeNo;

        if (IConstants.HEADER_FLIT == flit.getType()) {
            if (inputBuffer.isVCFree(flit.getVirtualChannelNo())) {
                inputBuffer.addBufferData(flit, flit.getVirtualChannelNo(),
                        curCycle);
                routeNo = parentSwitch.determineRoute(flit.getSourceNode(),
                        flit.getDestinationNode());
                inputBuffer.setRouteInfo(flit.getVirtualChannelNo(), routeNo);
            } else {
                System.out.println("Header network.traffic.Flit Loss " + flit.toString());
                return false;
            }
        } else if (inputBuffer.hasFreeSlotInVC(flit.getVirtualChannelNo())) {
            inputBuffer.addBufferData(flit, flit.getVirtualChannelNo(),
                    curCycle);
        } else {
            System.out.println("Data network.traffic.Flit Loss " + flit.toString());
            return false;
        }
        return true;
    }

    /**
     * Removes a flit from a virtual channel.
     *
     * @param vcId     virtual channel index
     * @param curCycle current simulation cycle
     * @return network.traffic.Flit
     * @see InputVCBuffer
     */
    public Flit removeInputBufferData(int vcId, int curCycle) {
        return inputBuffer.removeBufferData(vcId, curCycle);
    }

    /**
     * Returns true, if the virtual channel has more slots to store additional
     * flit. False-otherwise.
     *
     * @param vcId virtual channel index
     * @return boolean value
     * @see InputVCBuffer
     */
    public boolean hasFreeSlotInVCBuffer(int vcId) {
        return inputBuffer.hasFreeSlotInVC(vcId);
    }

    /**
     * Returns 'true' if the virual channel is free, false-otherwise.
     *
     * @param vcId virtual channel index
     * @return boolean value
     * @see InputVCBuffer
     */
    public boolean isVCFree(int vcId) {
        return inputBuffer.isVCFree(vcId);
    }

    /**
     * Returns free virtual channel index for the corresponding physical link.
     *
     * @return the index of the free virtual channel for this input link
     * @see InputVCBuffer
     */
    private int getFreeVC() {
        return inputBuffer.getFreeVC();
    }

    /**
     * Returns the input buffer associated with the input link controller.
     *
     * @return input buffer
     * @see InputVCBuffer
     */
    public InputVCBuffer getInputBuffer() {
        return inputBuffer;
    }

    /**
     * Updates status information at the end of each simulation cycle.
     *
     * @see InputVCBuffer
     */
    public void updateStatusAfterCycle() {
        inputBuffer.updateStatusAfterCycle();
    }

    /**
     * Invoked in every simulation cycle to set a request for switching the
     * flits through the crossbar switch to the output link controller of the
     * switch. It works in the following steps.
     * <ul>
     * <li> For every virtual channel, checks whether a flit is there whose
     * arrival time is at least one cycle ahead of current cycle time (so that
     * flit can be switched)</li>
     * <li> If one such flit is found then determines the type of that flit.</li>
     * <li> If that flit is of header type then gets the stored route
     * information from the virtual channel. Finds a free virtual channel by
     * requesting the corresponding output link controller (found from route
     * info). If a free virtual channel is found then stores this outgoing
     * virtual channel info in the input buffer for use by subsequent data flits
     * and also sets the request for this path to the parent switch.</li>
     * <li> If that flit is of data type then retrieves the previously stored
     * outgoing virtual channel path from the input buffer and sets a request
     * for this path to the parent switch. </li>
     * </ul>
     *
     * @param curCycle the simulation cycle at which the operation is being performed
     */
    public void setOutPathRequest(int curCycle) {
        int i, toLink;
        int newVC;

        for (i = 0; i < this.numVCCount; i++) {
            if ((null != this.inputBuffer.getBufferData(i))
                    && (this.inputBuffer.getBufferData(i)
                    .getLastServiceTimeStamp() < curCycle)) {
                if (this.inputBuffer.getBufferData(i).getType() == IConstants.HEADER_FLIT) {
                    toLink = this.inputBuffer.getRouteInfo(i);

                    /* Special Implementation for the OCTAL network.Network */

                    if (IConstants.CURRENT_NET == IConstants.NET_OCTAL) {
                        newVC = this.parentSwitch.getOutputLinkController(
                                toLink).getFreeVC_NEW(
                                this.inputBuffer.getBufferData(i));
                    } else {
                        newVC = this.parentSwitch.getOutputLinkController(
                                toLink).getFreeVC();
                    }

                    // a free buffer found
                    if (newVC >= 0) {
                        parentSwitch.setSwitchingInfoVector(toLink * numVCCount
                                + newVC, this.linkNo * numVCCount + i);
                        inputBuffer.setPathInfo(i, toLink * numVCCount + newVC);
                    }

                    // can not be transferred to output port. network.traffic.Flit is blocked
                    // here.
                } else {
                    toLink = this.inputBuffer.getPathInfo(i) / numVCCount;
                    int toVC = this.inputBuffer.getPathInfo(i) % numVCCount;
                    if (this.parentSwitch.getOutputLinkController(toLink)
                            .hasFreeSlotInVCBuffer(toVC))
                        parentSwitch.setSwitchingInfoVector(this.inputBuffer
                                .getPathInfo(i), this.linkNo * numVCCount + i);

                }
            }
        }
    }

}