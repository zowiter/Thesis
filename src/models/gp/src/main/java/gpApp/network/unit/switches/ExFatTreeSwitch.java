package gpApp.network.unit.switches;


import gpApp.network.unit.switches.link.OutputLinkController;
import gpApp.network.unit.switches.link.InputLinkController;
import gpApp.network.unit.node.Node;
import gpApp.network.unit.switches.router.Router;
import gpApp.network.unit.switches.router.ExFatTreeRouter;
import gpApp.network.common.IConstants;
import gpApp.network.traffic.Flit;
import gpApp.network.NetworkManager;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ExFatTreeSwitch implements Switch {

    // private int[] inLinkToOut = new int[network.common.IConstants.CURRENT_LINK_COUNT] ;
    private int[] lastVCServedList;

    private ExFatTreeSwitch[] upSwitchList;

    private ExFatTreeSwitch[] downSwitchList;

    private ExFatTreeSwitch[] siblingSwitchList;

    private Node[] nodeList;

    private int noOfPhysicalLink, noOfVirtualLink, address, switchIndex;

    private InputLinkController inputLC[];

    private OutputLinkController outputLC[];

    private boolean LinkUseStatus[];

    private Router router;

    private int[] switchingInfoVector;

    private int level, indexInLevel, startNodeCover, endNodeCover;

    public ExFatTreeSwitch(int address, int switchIndex) {
        int i;

        setAddress(address);
        setNoOfVirtualLink(IConstants.CURRENT_VC_COUNT);
        setNoOfPhysicalLink(IConstants.FAT_NUM_ADJ_CHILD
                + IConstants.FAT_NUM_ADJ_PARENT
                + IConstants.FAT_NUM_ADJ_SIBLING);

        this.level = address / (1 << IConstants.FAT_NUM_INDEX_BIT);
        this.indexInLevel = address % (1 << IConstants.FAT_NUM_INDEX_BIT);
        this.switchIndex = switchIndex;

        createLinkController(noOfPhysicalLink, noOfVirtualLink);
        createSwitchingInfoVector();

        if (this.level == 1) {
            nodeList = new Node[IConstants.FAT_NUM_ADJ_CHILD];
        } else {
            downSwitchList = new ExFatTreeSwitch[IConstants.FAT_NUM_ADJ_CHILD];

        }
        upSwitchList = new ExFatTreeSwitch[IConstants.FAT_NUM_ADJ_PARENT];
        siblingSwitchList = new ExFatTreeSwitch[IConstants.FAT_NUM_ADJ_SIBLING];

        createRouter();

        lastVCServedList = new int[this.noOfPhysicalLink]; // track which vc is
        // to serv next of
        // every link
        for (i = 0; i < this.noOfPhysicalLink; i++) {
            lastVCServedList[i] = 0;
        }

        if (IConstants.TRACE) {
            try {
                RandomAccessFile raf = new RandomAccessFile(
                        IConstants.TRACE_FILE, "rw");
                raf.seek(raf.length());
                raf
                        .writeBytes("\nCreated Extended Fat Tree network.unit.switches.Switch (level,index) = ("
                                + this.level
                                + ","
                                + this.indexInLevel
                                + ") address = " + this.address);
                raf.close();
            } catch (IOException ioex) {
            }
        }
    }

    public void createRouter() {
        router = new ExFatTreeRouter();
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

    public void setParentSwitch(ExFatTreeSwitch fatSwitch, int linkNo) {
        upSwitchList[linkNo] = fatSwitch;
    }

    public void setSiblingSwitch(ExFatTreeSwitch fatSwitch, int linkNo) {
        siblingSwitchList[linkNo] = fatSwitch;
    }

    public void setChildSwitch(ExFatTreeSwitch fatSwitch, int linkNo) {
        downSwitchList[linkNo] = fatSwitch;
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

                outputLC[destLinkNo].addOutputBufferData(flit, destVCId,
                        curCycle);

                if (IConstants.TRACE) {
                    try {
                        RandomAccessFile raf = new RandomAccessFile(
                                IConstants.TRACE_FILE, "rw");
                        raf.seek(raf.length());
                        if (IConstants.HEADER_FLIT == flit.getType()) {
                            raf.writeBytes("\nCycle " + curCycle + " ( "
                                    + flit.getSource() + "," + flit.getDest()
                                    + ") " + " Header network.traffic.Flit("
                                    + flit.getSourceNode() + ","
                                    + flit.getDestinationNode()
                                    + ") is SWITCHING from Link (" + srcLinkNo
                                    + "," + srcVCId + ") to (" + destLinkNo
                                    + "," + destVCId + ") at network.unit.switches.Switch "
                                    + this.address);
                        } else {
                            raf.writeBytes("\nCycle " + curCycle + " ( "
                                    + flit.getSource() + "," + flit.getDest()
                                    + ") "
                                    + " Data network.traffic.Flit is SWITCHING from Link ("
                                    + srcLinkNo + "," + srcVCId + ") to ("
                                    + destLinkNo + "," + destVCId
                                    + ") at network.unit.switches.Switch " + this.address);
                        }

                        raf.close();
                    } catch (IOException ioex) {
                    }
                }

            }
        }
    }

    private void forwardFlitToSwitch(ExFatTreeSwitch adjSwitch, int linkNo,
                                     int curCycle) {
        int count = 0, toLink, apLink, numNode;
        Flit flit;

        numNode = IConstants.FAT_NUM_ADJ_CHILD;
        // apLink = (int) (linkNo - numNode) ; // link for node is not
        // considered here
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
                        LinkUseStatus[linkNo] = true; // for Link Utilization
                        if (flit.getVirtualChannelNo() != lastVCServedList[linkNo]) {
                            System.out.println(curCycle + " Problem ");
                        }
                        // flit.increaseHop();
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
                        LinkUseStatus[linkNo] = true; // for Link Utilization
                        if (flit.getVirtualChannelNo() != lastVCServedList[linkNo]) {
                            System.out.println(curCycle + " Problem ");

                        }
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

    private void forwardFlitToNode(Node adjNode, int linkNo, int curCycle) {
        int count = 0, toLink;
        Flit flit;

        lastVCServedList[linkNo] = (int) ((++lastVCServedList[linkNo]) % noOfVirtualLink);
        while (count < noOfVirtualLink) {
            if (outputLC[linkNo].hasFlitToSend(lastVCServedList[linkNo])
                    && outputLC[linkNo].getBufferData(lastVCServedList[linkNo])
                    .getLastServiceTimeStamp() < curCycle) {
                // one VC found to send flit
                if (outputLC[linkNo].getBufferData(lastVCServedList[linkNo])
                        .getType() == IConstants.HEADER_FLIT) {
                    // header flit. So need a free VC
                    if (adjNode.isInputVCFree(lastVCServedList[linkNo])) {
                        flit = outputLC[linkNo].removeOutputBufferData(
                                lastVCServedList[linkNo], curCycle);
                        flit.setLastServiceTimeStamp(curCycle);
                        if (IConstants.TRACE) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(
                                        IConstants.TRACE_FILE, "rw");
                                raf.seek(raf.length());
                                raf.writeBytes("\nCycle " + curCycle
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
                        LinkUseStatus[linkNo] = true; // for Link Utilization
                        break;
                    }
                } else {
                    // data flit. need a free slot in VC buffer
                    if (adjNode.hasFreeSlotInInputVC(lastVCServedList[linkNo])) {
                        flit = outputLC[linkNo].removeOutputBufferData(
                                lastVCServedList[linkNo], curCycle);
                        flit.setLastServiceTimeStamp(curCycle);
                        if (IConstants.TRACE) {
                            try {
                                RandomAccessFile raf = new RandomAccessFile(
                                        IConstants.TRACE_FILE, "rw");
                                raf.seek(raf.length());
                                raf.writeBytes("\nCycle " + curCycle
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
                        LinkUseStatus[linkNo] = true; // for Link Utilization
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
        ExFatTreeSwitch adjSwitch;

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
            } else if (i >= IConstants.FAT_NUM_ADJ_CHILD
                    && i < (IConstants.FAT_NUM_ADJ_CHILD + IConstants.FAT_NUM_ADJ_PARENT)) {
                adjSwitch = upSwitchList[i - IConstants.FAT_NUM_ADJ_CHILD];
                if (null != adjSwitch) {
                    forwardFlitToSwitch(adjSwitch, i, curCycle);
                }
            } else if (i >= (IConstants.FAT_NUM_ADJ_CHILD + IConstants.FAT_NUM_ADJ_PARENT)) {
                adjSwitch = siblingSwitchList[i - IConstants.FAT_NUM_ADJ_CHILD
                        - IConstants.FAT_NUM_ADJ_PARENT];
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
        for (int i = 0; i < this.noOfPhysicalLink; i++) {
            if (outputLC[i] != null) {
                count++;
            }
        }
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
    }

    public int getSwitchLevel() {
        return this.address >> IConstants.FAT_NUM_INDEX_BIT;
    }

    public int getSwitchIndex() {
        return this.address & ((1 << IConstants.FAT_NUM_INDEX_BIT) - 1);
    }

    public void setStartNodeCover(int start) {
        this.startNodeCover = start;
    }

    public int getStartNodeCover() {
        return this.startNodeCover;
    }

    public void setEndNodeCover(int end) {
        this.endNodeCover = end;
    }

    public int getEndNodeCover() {
        return this.endNodeCover;
    }

    public void setAddress(int addr) {
        this.address = addr;
    }

    public int getAddress() {
        return this.address;
    }

    public int getAdjacentLinkNo(ExFatTreeSwitch fatSwitch) {
        int i;
        if (null != upSwitchList) {
            for (i = 0; i < upSwitchList.length; i++) {
                if (upSwitchList[i] == fatSwitch) {
                    return i + IConstants.FAT_NUM_ADJ_CHILD;
                }
            }
        }
        if (null != downSwitchList) {
            for (i = 0; i < downSwitchList.length; i++) {
                if (downSwitchList[i] == fatSwitch) {
                    return i;
                }
            }
        }
        if (null != siblingSwitchList) {
            for (i = 0; i < siblingSwitchList.length; i++) {
                if (siblingSwitchList[i] == fatSwitch) {
                    return i + IConstants.FAT_NUM_ADJ_CHILD
                            + IConstants.FAT_NUM_ADJ_PARENT;
                }
            }
        }

        return -1;
    }

    public void setAdjacentSwitch(Switch sw, int linkNo) {
        // switchList[linkNo] = (network.unit.switches.OctalSwitch)octalSwitch ;
    }

    public void setNoOfVirtualLink(int no) {
        this.noOfVirtualLink = no;
    }

    public void setNoOfPhysicalLink(int no) {
        this.noOfPhysicalLink = no;
    }

    public int getNoOfPhysicalLink() {
        return this.noOfPhysicalLink;
    }

    public int getNoOfVirtualLink() {
        return this.noOfVirtualLink;
    }

}
