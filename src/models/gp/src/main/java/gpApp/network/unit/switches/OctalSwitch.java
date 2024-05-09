package gpApp.network.unit.switches;

import gpApp.network.unit.switches.link.OutputLinkController;
import gpApp.network.unit.switches.link.InputLinkController;
import gpApp.network.unit.node.Node;
import gpApp.network.unit.switches.router.Router;
import gpApp.network.unit.switches.router.OctalRouter;
import gpApp.network.common.IConstants;
import gpApp.network.traffic.Flit;
import gpApp.network.NetworkManager;

import java.io.IOException;
import java.io.RandomAccessFile;

public class OctalSwitch implements Switch {

    // private int[] inLinkToOut = new int[network.common.IConstants.CURRENT_LINK_COUNT] ;
    private int[] lastVCServedList;

    public OctalSwitch[] switchList;

    public Node[] nodeList;

    private int noOfPhysicalLink, noOfVirtualLink, address, switchIndex;

    private InputLinkController inputLC[];

    private OutputLinkController outputLC[];

    private boolean LinkUseStatus[];

    private Router router;

    private int[] switchingInfoVector;

    // / for debug purpose
    public int getSwitchIndex() {
        return this.switchIndex;
    }

    public void setNoOfPhysicalLink(int no) {
        this.noOfPhysicalLink = no;
    }

    public int getNoOfPhysicalLink() {
        return this.noOfPhysicalLink;
    }

    public void setNoOfVirtualLink(int noVlink) {
        this.noOfVirtualLink = noVlink;
    }

    public int getNoOfVirtualLink() {
        return this.noOfVirtualLink;
    }

    public void setAddress(int addr) {
        this.address = addr;
    }

    public int getAddress() {
        return this.address;
    }

    public OctalSwitch(int pLink, int vLink, int address, int noOfAdjNode,
                       int noOfAdjSwitch, int switchIndex) {
        int i;

        setNoOfPhysicalLink(pLink);
        setNoOfVirtualLink(vLink);
        setAddress(address);
        this.switchIndex = switchIndex;
        createLinkController(noOfPhysicalLink, noOfVirtualLink);
        createSwitchingInfoVector();

        nodeList = new Node[noOfAdjNode];
        switchList = new OctalSwitch[noOfAdjSwitch];
        // router = new network.unit.switches.router.Router() ;
        createRouter();

        lastVCServedList = new int[noOfAdjNode + noOfAdjSwitch];// track which
        // vc is to serv
        // next of every
        // link
        for (i = 0; i < noOfAdjNode + noOfAdjSwitch; i++) {
            lastVCServedList[i] = 0;
        }
        if (IConstants.TRACE) {
            try {
                RandomAccessFile raf = new RandomAccessFile(
                        IConstants.TRACE_FILE, "rw");
                raf.seek(raf.length());
                raf.writeBytes("\nCreated Octal network.unit.switches.Switch address = "
                        + this.address);
                raf.close();
            } catch (IOException ioex) {
            }
        }

    }

    public void createRouter() {
        router = new OctalRouter();
    }

    private void createLinkController(int linkCount, int vcCount) {
        inputLC = new InputLinkController[linkCount];
        outputLC = new OutputLinkController[linkCount];
        LinkUseStatus = new boolean[linkCount];

        for (int i = 0; i < linkCount; i++) {
            inputLC[i] = new InputLinkController(this, i, vcCount, linkCount);
            outputLC[i] = new OutputLinkController(this, i, vcCount, linkCount);
            LinkUseStatus[i] = false;
        }
    }

    public void setAdjacentNode(Node node, int linkNo) {
        nodeList[linkNo] = node;
    }

    public int getNumAdjacentNode() {
        int i, count = 0;
        for (i = 0; i < nodeList.length; i++) {
            if (null != nodeList[i]) {
                count++;
            }
        }
        return count;
    }

    public void setAdjacentSwitch(OctalSwitch octalSwitch, int linkNo) {
        switchList[linkNo] = octalSwitch;
    }

    public InputLinkController getInputLinkController(int linkNo) {
        return inputLC[linkNo];
    }

    public void setInputLinkController(int linkNo, InputLinkController iLC) {
        inputLC[linkNo] = iLC;
    }

    public OutputLinkController getOutputLinkController(int linkNo) {
        return outputLC[linkNo];
    }

    public void setOutputLinkController(int linkNo, OutputLinkController oLC) {
        outputLC[linkNo] = oLC;
    }

    private void createSwitchingInfoVector() {
        switchingInfoVector = new int[noOfPhysicalLink * noOfVirtualLink];
        resetSwitchingInfoVector();
    }

    public void resetSwitchingInfoVector() {
        int dim = noOfPhysicalLink * noOfVirtualLink;
        for (int i = 0; i < dim; i++) {
            switchingInfoVector[i] = -1;
        }
    }

    public boolean addInputBufferData(int linkNo, Flit flit, int curCycle) {
        return inputLC[linkNo].addInputBufferData(flit, curCycle);
    }

    public boolean addOutputBufferData(int linkNo, Flit flit, int vcId,
                                       int curCycle) {
        return outputLC[linkNo].addOutputBufferData(flit, vcId, curCycle);
    }

    public boolean setSwitchingInfoVector(int dest, int src) {
        if (switchingInfoVector[dest] < 0) {
            switchingInfoVector[dest] = src;
            return true;
        }
        return false;
    }

    public int getSwitchingInfoVector(int dest) {
        return switchingInfoVector[dest];
    }

    public Flit removeInputBufferData(int linkNo, int vcId, int curCycle) {
        return inputLC[linkNo].removeInputBufferData(vcId, curCycle);
    }

    public Flit removeOutputBufferData(int linkNo, int vcId, int curCycle) {
        return outputLC[linkNo].removeOutputBufferData(vcId, curCycle);
    }

    public int determineRoute(int src, int dest) {
        return router.determineRoute(src, dest, this.address);
    }

    /*
      * public void setRouteInfo(int linkNo,int vcId,int toPort) {
      * inputLC[linkNo].getInputBuffer().setRouteInfo(vcId,toPort); }
      */

    public boolean hasFreeSlotInVCBuffer(int linkNo, int vcId) {
        return inputLC[linkNo].hasFreeSlotInVCBuffer(vcId);
    }

    /*
      * public int getSwitchingRoute (int src) { return
      * this.switchingInfoVector[src] ; }
      */

    public void updateSwitchOutPathRequest(int curCycle) {
        int i;
        for (i = 0; i < noOfPhysicalLink; i++) {
            if (null != inputLC[i]) {
                // inputLC[i
                inputLC[i].setOutPathRequest(curCycle);
            }
        }
    }

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

                // if(flit==null)
                // {
                // System.err.println("keno error hoilo");
                // System.exit(1);
                // }
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

    private void forwardFlitToSwitch(OctalSwitch adjSwitch, int linkNo,
                                     int curCycle) {
        int count = 0, toLink, apLink, numNode;
        Flit flit;

        numNode = IConstants.OCTAL_ADJ_NODE;
        apLink = (int) (linkNo - numNode); // link for node is not considered
        // here
        lastVCServedList[linkNo] = (int) ((++lastVCServedList[linkNo]) % noOfVirtualLink);
        while (count < noOfVirtualLink) {
            if ((outputLC[linkNo].hasFlitToSend(lastVCServedList[linkNo]))
                    && (outputLC[linkNo]
                    .getBufferData(lastVCServedList[linkNo])
                    .getLastServiceTimeStamp() < curCycle)) {
                // one VC found to send flit
                toLink = numNode;
                int numOfSwitch = 0;
                numOfSwitch = getAddress();
                numOfSwitch = (numOfSwitch & 7);
                // System.out.println("address: " + getAddress() + " Swtich
                // number = " + numOfSwitch);
                if (apLink == IConstants.LEFT_NODE && numOfSwitch != 5
                        && numOfSwitch != 6) {
                    toLink += IConstants.RIGHT_NODE;
                } else if (apLink == IConstants.LEFT_NODE
                        && (numOfSwitch == 5 || numOfSwitch == 6)) {
                    toLink += IConstants.LEFT_NODE;
                } else if (apLink == IConstants.RIGHT_NODE && numOfSwitch != 2
                        && numOfSwitch != 4) {
                    toLink += IConstants.LEFT_NODE;
                } else if (apLink == IConstants.RIGHT_NODE
                        && (numOfSwitch == 2 || numOfSwitch == 4)) {
                    toLink += IConstants.RIGHT_NODE;
                } else {
                    toLink += apLink;
                }

                if (outputLC[linkNo].getBufferData(lastVCServedList[linkNo])
                        .getType() == IConstants.HEADER_FLIT) {
                    // header flit. So need a free VC
                    // if(curCycle>network.common.IConstants.WARM_UP_CYCLE)
                    // System.out.println("network.unit.switches.Switch->network.unit.switches.Switch Test: From"+
                    // this.getAddress() + " to: " + adjSwitch.getAddress() + "
                    // cycle: "+ curCycle + " aplink: " +apLink + " toLink: " +
                    // (toLink - numNode) + " ... " +
                    // adjSwitch.isVCFreeInSwitch(toLink,
                    // lastVCServedList[linkNo]));

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
                                        + " Header network.traffic.Flit("
                                        + flit.getSourceNode() + ","
                                        + flit.getDestinationNode()
                                        + ") Length " + flit.getPacketLength()
                                        + " is MOVING from SWITCH " + address
                                        + " TO SWITCH "
                                        + adjSwitch.getAddress()
                                        + " from (Link,VC) =  (" + linkNo + ","
                                        + lastVCServedList[linkNo]
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

                        adjSwitch.addInputBufferData(toLink, flit, curCycle);

                        if (curCycle > IConstants.WARM_UP_CYCLE)
                            System.out.println("network.unit.switches.Switch->network.unit.switches.Switch Header: From"
                                    + this.getAddress() + " to: "
                                    + adjSwitch.getAddress() + " Dest: "
                                    + flit.getDest() + " Src: "
                                    + flit.getSource() + " cycle: " + curCycle
                                    + " aplink: " + apLink + " toLink: "
                                    + (toLink - numNode) + " length: "
                                    + flit.getPacketLength());

                        break;
                    }
                    // else blocked. try next time
                } else {
                    // if(curCycle>network.common.IConstants.WARM_UP_CYCLE)
                    // System.out.println("network.unit.switches.Switch->network.unit.switches.Switch Test: From"+
                    // this.getAddress() + " cycle: "+ curCycle + " aplink: "
                    // +apLink + " toLink: " + (toLink - numNode) + " ... " +
                    // adjSwitch.isVCFreeInSwitch(toLink,
                    // lastVCServedList[linkNo]));

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

                        adjSwitch.addInputBufferData(toLink, flit, curCycle);

                        // if(curCycle>=network.common.IConstants.WARM_UP_CYCLE)
                        // System.out.println("network.unit.switches.Switch->network.unit.switches.Switch Data: From"+
                        // this.address + " to: " + adjSwitch.getAddress() + "
                        // Dest: " + flit.getDest() + " Src: "+ flit.getSource()
                        // + " cycle: "+ curCycle + " aplink: " +apLink + "
                        // toLink: " + (toLink - numNode) );

                        break;
                    }
                }

            }
            lastVCServedList[linkNo] = (int) ((++lastVCServedList[linkNo]) % noOfVirtualLink);
            count++;
            // otherwise try for the next VC to send

        }
    }

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

                        adjNode.addInputBufferData(flit, curCycle);
                        if (curCycle > IConstants.WARM_UP_CYCLE)
                            NetworkManager.getStatDataInstance()
                                    .incrementFlitLeavingFromSwitch(
                                            this.switchIndex);
                        LinkUseStatus[linkNo] = true;// for Link Utilization
                        // if(curCycle>network.common.IConstants.WARM_UP_CYCLE)
                        // System.out.println("network.unit.switches.Switch->network.unit.node.Node Header: From"+
                        // this.address + " to: " + adjNode.getAddress() + "
                        // Dest: " + flit.getDest() + " Src: "+ flit.getSource()
                        // + " cycle: "+ curCycle);
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

                        adjNode.addInputBufferData(flit, curCycle);
                        if (curCycle > IConstants.WARM_UP_CYCLE)
                            NetworkManager.getStatDataInstance()
                                    .incrementFlitLeavingFromSwitch(
                                            this.switchIndex);
                        LinkUseStatus[linkNo] = true;// for Link Utilization

                        // if(curCycle>network.common.IConstants.WARM_UP_CYCLE)
                        // System.out.println("network.unit.switches.Switch->network.unit.node.Node Data: From"+
                        // this.address + " to: " + adjNode.getAddress() + "
                        // Dest: " + flit.getDest() + " Src: "+ flit.getSource()
                        // + " cycle: "+ curCycle);
                        break;
                    }

                }

            }

            lastVCServedList[linkNo] = (int) ((++lastVCServedList[linkNo]) % noOfVirtualLink);
            count++;
            // otherwise try for the next VC to send

        }
    }

    public void moveSwitchOutputBufferToInputBufferOfNodeSwitch(int curCycle) {
        int i;
        Node adjNode;
        OctalSwitch adjSwitch;

        // faor all adjacent node and switches
        for (i = 0; i < noOfPhysicalLink; i++) {
            // front links are for adjacent node
            if (i < nodeList.length) {
                adjNode = nodeList[i];
                if (null != adjNode) {
                    forwardFlitToNode(adjNode, i, curCycle);
                }
            }
            // rear links are for adjacent switches
            else {
                int link = (int) (i - nodeList.length);
                adjSwitch = switchList[link];
                if (null != adjSwitch) {
                    forwardFlitToSwitch(adjSwitch, i, curCycle);
                }
            }
        }
    }

    public boolean isVCFreeInSwitch(int linkNo, int vcId) {
        if (null != inputLC[linkNo]) {
            return inputLC[linkNo].isVCFree(vcId);
        } else {
            return false;
        }
    }

    public int getNumLinkActive() {
        int count = 0;
        for (int i = 0; i < this.noOfPhysicalLink; i++)
            if (null != outputLC[i])
                count++;
        return count;
    }

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

        // debug purpose

        /*
           * if(curCycle==1500){ System.out.println(".............. DEBUG: CYCLE
           * 1500............."); System.out.println(".............. network.unit.switches.Switch: " +
           * address + " .............");
           *
           * for(i=0;i<5;i++){ if(this.inputLC[i]!=null){ System.out.print("\t
           * In: " + i + " : "); network.traffic.Flit flit =
           * inputLC[i].getInputBuffer().getBufferData(0);
           *
           * if(flit!=null) System.out.print(" network.traffic.Flit. src: " + flit.getSource() + "
           * dest: " + flit.getDest() + " gent: " + flit.getGenTimeStamp() + "
           * hop: "+ flit.getHopCount()); System.out.println(); }
           * if(this.outputLC[i]!=null){ System.out.print("\t In: " + i + " : ");
           * network.traffic.Flit flit = this.outputLC[i].getOutputBuffer().getBufferData(0);
           * if(flit!=null) System.out.print(" network.traffic.Flit. src: " + flit.getSource() + "
           * dest: " + flit.getDest() + " gent: " + flit.getGenTimeStamp() + "
           * hop: "+ flit.getHopCount());
           *
           *
           * System.out.println(); } System.out.println(); } System.out.println(); }
           */
    }

    public void setAdjacentSwitch(Switch octalSwitch, int linkNo) {
        switchList[linkNo] = (OctalSwitch) octalSwitch;
    }

}
