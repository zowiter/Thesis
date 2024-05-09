package network;

import network.unit.switches.*;
import network.unit.node.Node;
import network.common.IConstants;

import java.util.Vector;

/**
 * network.Network is the object through which all the node (Resource blocks) and all
 * the communication switches are connected to perform the desired goal into one
 * network.unit. According to the input parameters the network is built up. Different
 * types of network topologies can be created but for simulation at a time only
 * one type of network is instantiated. Mesh, Torus,WK Recursive, Butterfly Fat Tree and
 * Extended Butterfly Fat Tree, Octal are among the different types of network.
 * <p/>
 * N.B. Extended Butterfly Fat Tree (an extension of the Butterfly Fat Tree) and
 * Octal (an extension of the Octagon topology) are two architectures originally
 * proposed by the researchers in Dept. of CSE, BUET)
 *
 * @version 1.0
 */

public class Network {
    /**
     * A vector type variable holding the reference of all the resource node of
     * the network. This list is used for retrieving node reference to invoke
     * methods directly from network.
     */
    private Vector nodeList;

    /**
     * A vector type variable holding the reference of all the communication
     * switches of the network. This list is used for retrieving switches
     * reference to invoke methods directly from network.
     */
    private Vector switchList;

    /**
     * Constructor of the network.Network. In this version of the simulator, the type of
     * the networks that have been implemented are Mesh, Torus, Butterfly Fat
     * Tree, Extended Butterfly Fat Tree, and Octal. The constructor performs in
     * the following fashion.
     * <ul>
     * <li>Creates nodeList and switchList vector object.</li>
     * <li>Checks the type of the network to build.</li>
     * <li>The network of specified type is created along with creation of
     * adjacency relationship with other switches for all the switches.</li>
     * </ul>
     *
     * @param networkType An integer which determines which type of network the
     *                    constructor will build.
     * @see IConstants
     */
    public Network(int networkType) {
        nodeList = new Vector();
        switchList = new Vector();

        if (IConstants.NET_FAT_TREE == networkType) {
            createFatTreeNetwork();
            setAdjacentFatSwitch();
        } else if (IConstants.NET_WK == networkType) {
            createWKNetwork();
            setAdjacentWKSwitch();
        } else if (IConstants.NET_MESH == networkType) {
            createMeshNetwork();
            setAdjacentMeshSwitch();
        } else if (IConstants.NET_TORUS == networkType) {
            createMeshNetwork();
            setAdjacentTorusSwitch();
        } else if (IConstants.NET_EX_FAT_TREE == networkType) {
            createExFatTreeNetwork();
            setAdjacentExFatSwitch();
        } else if (IConstants.NET_OCTAL == networkType) {
            createOctalNetwork();
            setAdjacentOctalSwitch();

        }

    }


    private void createWKNetwork() {
        int i, address, numSwitch, row, col, noOfAdjNode;
        double factor;
        WKSwitch wkSwitch;
        Node nd;

        noOfAdjNode = IConstants.WK_ADJ_NODE;
        numSwitch = (int) Math.pow(IConstants.WK_W, IConstants.WK_L);
        IConstants.CURRENT_LINK_COUNT = IConstants.WK_ADJ_NODE + 4;

        /*
        * Readjusted Number of IP Nodes and Number of Switches .. Only n * m
        * swithces are possible.. where n = m or m+1
        */
        IConstants.NUMBER_OF_SWITCH = (int) Math.pow(IConstants.WK_W, IConstants.WK_L);
        IConstants.NUMBER_OF_IP_NODE = (int) Math.pow(IConstants.WK_W, IConstants.WK_L);

        // Creates the WK Switches
        wkNetwork(0, IConstants.WK_L);

        // assign index to node for statistical purpose
        for (i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            ((Node) nodeList.get(i)).setNodeListIndex(i);
        }

    }

    private int getIndexInNet(int address) {
        int index = 0;
        int currDigit = 0;

        int i = 0;
        while (true) {
            currDigit = (address & 3);
            if (i == 0) {
                index += currDigit;
            } else {
                index += (currDigit * (Math.pow(IConstants.WK_W, i)));
            }
            address >>>= 2;
            if (address == 0)
                break;
            i++;
        }

        return index;
    }

    private void wkNetwork(int baseAddr, int l) {
        if (l == 1) {
            double factor;
            WKSwitch wkSwitch;
            Node nd;
            for (int index = 0; index < IConstants.WK_W; index++) {
                int address = (baseAddr << 2) | index;
                int switchIndex = getIndexInNet(address);
                wkSwitch = new WKSwitch(IConstants.CURRENT_LINK_COUNT,
                        IConstants.CURRENT_VC_COUNT, address, IConstants.WK_ADJ_NODE,
                        IConstants.CURRENT_ADJ_SWITCH, switchIndex);
                if (switchIndex != switchList.size())
                    throw new RuntimeException("Invalid network.unit.switches.Switch index.");
                switchList.add(wkSwitch);
                // add IP network.unit.node.Node to the WK network.unit.switches.Switch
                factor = NetworkManager.getHelpingUtility()
                        .getNextRandomNumber();
                factor = factor * 5 + 4;
                int intVal = (int) factor;
                factor = (double) intVal / 10;

                if (IConstants.ASYNCHRONOUS)
                    nd = new Node(address, wkSwitch, 0,
                            IConstants.CURRENT_VC_COUNT, factor);
                else
                    nd = new Node(address, wkSwitch, 0,
                            IConstants.CURRENT_VC_COUNT, 1.0);
                wkSwitch.setAdjacentNode(nd, 0);
                nodeList.add(nd);
            }
        } else {
            for (int i = 0; i < IConstants.WK_W; i++) {
                wkNetwork((baseAddr << 2) | i, l - 1);
            }
        }
    }

    private void setAdjacentWKSwitch() {
        connectNodes(0, IConstants.WK_L);
    }

    public WKSwitch getNorthWestSwitch(int baseAddress, int level) {
        WKSwitch wkSwitch = null;
        //   StringBuilder addressBuilder = new StringBuilder(Integer.toString(baseAddress));
        int switchAddr = baseAddress;
        while (level >= 1) {
            //  addressBuilder.append(0);
            switchAddr = (switchAddr << 2) | 0;
            level--;
        }
        // wkSwitch=(network.unit.switches.WKSwitch)switchList.get(getIndexInNet(Integer.parseInt(addressBuilder.toString())));
        wkSwitch = (WKSwitch) switchList.get(getIndexInNet(switchAddr));
        return wkSwitch;
    }

    public WKSwitch getNorthEastSwitch(int baseAddress, int level) {
        WKSwitch wkSwitch = null;
        // StringBuilder addressBuilder = new StringBuilder(Integer.toString(baseAddress));
        int switchAddr = baseAddress;
        while (level >= 1) {
            //   addressBuilder.append(1);
            switchAddr = (switchAddr << 2) | 1;
            level--;
        }
        //  wkSwitch=(network.unit.switches.WKSwitch)switchList.get(getIndexInNet(Integer.parseInt(addressBuilder.toString())));
        wkSwitch = (WKSwitch) switchList.get(getIndexInNet(switchAddr));
        return wkSwitch;
    }

    public WKSwitch getSouthEastSwitch(int baseAddress, int level) {
        WKSwitch wkSwitch = null;
        //  StringBuilder addressBuilder = new StringBuilder(Integer.toString(baseAddress));
        int switchAddr = baseAddress;
        while (level >= 1) {
            //     addressBuilder.append(2);
            switchAddr = (switchAddr << 2) | 2;
            level--;
        }
        //    wkSwitch=(network.unit.switches.WKSwitch)switchList.get(getIndexInNet(Integer.parseInt(addressBuilder.toString())));
        wkSwitch = (WKSwitch) switchList.get(getIndexInNet(switchAddr));
        return wkSwitch;
    }

    public WKSwitch getSouthWestSwitch(int baseAddress, int level) {
        WKSwitch wkSwitch = null;
        //  StringBuilder addressBuilder = new StringBuilder(Integer.toString(baseAddress));
        int switchAddr = baseAddress;
        while (level >= 1) {
            //     addressBuilder.append(3);
            switchAddr = (switchAddr << 2) | 3;
            level--;
        }
        //  wkSwitch=(network.unit.switches.WKSwitch)switchList.get(getIndexInNet(Integer.parseInt(addressBuilder.toString())));
        wkSwitch = (WKSwitch) switchList.get(getIndexInNet(switchAddr));
        return wkSwitch;
    }

    public static int convertToBinaryAddress(int address) {
        int level = 0;
        int[] sourceDigits = new int[IConstants.WK_L];
        while (level < IConstants.WK_L) {
            sourceDigits[level] = address % 10;
            address /= 10;
            level++;
        }
        int binary = 0;
        level = IConstants.WK_L;
        while (level > 0) {
            binary = (binary << 2) | sourceDigits[level - 1];
            level--;
        }
        return binary;
    }

    public static int convertToDecimalAddress(int address) {
        int level = 0;
        int[] sourceDigits = new int[IConstants.WK_L];
        while (level < IConstants.WK_L) {
            sourceDigits[level] = (address & 3);
            address >>>= 2;
            level++;
        }
        int decimal = 0;
        level = IConstants.WK_L - 1;
        while (level >= 0) {
            decimal = (decimal * 10) + sourceDigits[level];
            level--;
        }
        return decimal;
    }

    private void connectNodes(int baseAddr, int wk_l) {
        if (IConstants.WK_L == wk_l) {
            WKSwitch n_w_WkSwitch = getNorthWestSwitch(baseAddr, wk_l);
            WKSwitch n_e_WkSwitch = getNorthEastSwitch(baseAddr, wk_l);
            WKSwitch s_e_WkSwitch = getSouthEastSwitch(baseAddr, wk_l);
            WKSwitch s_w_WkSwitch = getSouthWestSwitch(baseAddr, wk_l);

            n_w_WkSwitch.setInputLinkController(IConstants.SWITCH_NORTH_WEST
                    + IConstants.WK_ADJ_NODE, null);
            n_w_WkSwitch.setOutputLinkController(IConstants.SWITCH_NORTH_WEST
                    + IConstants.WK_ADJ_NODE, null);
            n_e_WkSwitch.setInputLinkController(IConstants.SWITCH_NORTH_EAST
                    + IConstants.WK_ADJ_NODE, null);
            n_e_WkSwitch.setOutputLinkController(IConstants.SWITCH_NORTH_EAST
                    + IConstants.WK_ADJ_NODE, null);
            s_e_WkSwitch.setInputLinkController(IConstants.SWITCH_SOUTH_EAST
                    + IConstants.WK_ADJ_NODE, null);
            s_e_WkSwitch.setOutputLinkController(IConstants.SWITCH_SOUTH_EAST
                    + IConstants.WK_ADJ_NODE, null);
            s_w_WkSwitch.setInputLinkController(IConstants.SWITCH_SOUTH_WEST
                    + IConstants.WK_ADJ_NODE, null);
            s_w_WkSwitch.setOutputLinkController(IConstants.SWITCH_SOUTH_WEST
                    + IConstants.WK_ADJ_NODE, null);
        }
        if (wk_l == 1) {
            int address = baseAddr << 2;
            WKSwitch n_w_WkSwitch = (WKSwitch) switchList.get(getIndexInNet(address | IConstants.SWITCH_NORTH_WEST));
            WKSwitch n_e_WkSwitch = (WKSwitch) switchList.get(getIndexInNet(address | IConstants.SWITCH_NORTH_EAST));
            WKSwitch s_e_WkSwitch = (WKSwitch) switchList.get(getIndexInNet(address | IConstants.SWITCH_SOUTH_EAST));
            WKSwitch s_w_WkSwitch = (WKSwitch) switchList.get(getIndexInNet(address | IConstants.SWITCH_SOUTH_WEST));

            n_w_WkSwitch.setAdjacentSwitch(n_e_WkSwitch, IConstants.SWITCH_NORTH_EAST);
            n_w_WkSwitch.setAdjacentSwitch(s_e_WkSwitch, IConstants.SWITCH_SOUTH_EAST);
            n_w_WkSwitch.setAdjacentSwitch(s_w_WkSwitch, IConstants.SWITCH_SOUTH_WEST);

            n_e_WkSwitch.setAdjacentSwitch(n_w_WkSwitch, IConstants.SWITCH_NORTH_WEST);
            n_e_WkSwitch.setAdjacentSwitch(s_w_WkSwitch, IConstants.SWITCH_SOUTH_WEST);
            n_e_WkSwitch.setAdjacentSwitch(s_e_WkSwitch, IConstants.SWITCH_SOUTH_EAST);

            s_e_WkSwitch.setAdjacentSwitch(n_e_WkSwitch, IConstants.SWITCH_NORTH_EAST);
            s_e_WkSwitch.setAdjacentSwitch(n_w_WkSwitch, IConstants.SWITCH_NORTH_WEST);
            s_e_WkSwitch.setAdjacentSwitch(s_w_WkSwitch, IConstants.SWITCH_SOUTH_WEST);

            s_w_WkSwitch.setAdjacentSwitch(n_e_WkSwitch, IConstants.SWITCH_NORTH_EAST);
            s_w_WkSwitch.setAdjacentSwitch(n_w_WkSwitch, IConstants.SWITCH_NORTH_WEST);
            s_w_WkSwitch.setAdjacentSwitch(s_e_WkSwitch, IConstants.SWITCH_SOUTH_EAST);

        } else {
            // for each super node
            for (int i = 0; i < 4; i++) {
                connectNodes((baseAddr << 2) | i, wk_l - 1);
            }
            getNorthEastSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_WEST, wk_l - 1).setAdjacentSwitch(getNorthWestSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_EAST, wk_l - 1), IConstants.SWITCH_NORTH_EAST);
            getSouthEastSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_WEST, wk_l - 1).setAdjacentSwitch(getNorthWestSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_EAST, wk_l - 1), IConstants.SWITCH_SOUTH_EAST);
            getSouthWestSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_WEST, wk_l - 1).setAdjacentSwitch(getNorthWestSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_WEST, wk_l - 1), IConstants.SWITCH_SOUTH_WEST);

            getNorthWestSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_EAST, wk_l - 1).setAdjacentSwitch(getNorthEastSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_WEST, wk_l - 1), IConstants.SWITCH_NORTH_WEST);
            getSouthEastSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_EAST, wk_l - 1).setAdjacentSwitch(getNorthEastSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_EAST, wk_l - 1), IConstants.SWITCH_SOUTH_EAST);
            getSouthWestSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_EAST, wk_l - 1).setAdjacentSwitch(getNorthEastSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_WEST, wk_l - 1), IConstants.SWITCH_SOUTH_WEST);

            getNorthEastSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_EAST, wk_l - 1).setAdjacentSwitch(getSouthEastSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_EAST, wk_l - 1), IConstants.SWITCH_NORTH_EAST);
            getNorthWestSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_EAST, wk_l - 1).setAdjacentSwitch(getSouthEastSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_WEST, wk_l - 1), IConstants.SWITCH_NORTH_WEST);
            getSouthWestSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_EAST, wk_l - 1).setAdjacentSwitch(getSouthEastSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_WEST, wk_l - 1), IConstants.SWITCH_SOUTH_WEST);

            getNorthEastSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_WEST, wk_l - 1).setAdjacentSwitch(getSouthWestSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_EAST, wk_l - 1), IConstants.SWITCH_NORTH_EAST);
            getNorthWestSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_WEST, wk_l - 1).setAdjacentSwitch(getSouthWestSwitch((baseAddr << 2) | IConstants.SWITCH_NORTH_WEST, wk_l - 1), IConstants.SWITCH_NORTH_WEST);
            getSouthEastSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_WEST, wk_l - 1).setAdjacentSwitch(getSouthWestSwitch((baseAddr << 2) | IConstants.SWITCH_SOUTH_EAST, wk_l - 1), IConstants.SWITCH_SOUTH_EAST);
        }
    }

    /**
     * Instantiates all the node and mesh switches of the mesh network as well
     * as assigns which node will be connected with which mesh switch. The
     * method performs in the following manner.
     * <p/>
     * <ul>
     * <li>Determines how many adjacent node per switch.</li>
     * <li>Determines the number of switches required in the network.</li>
     * <li>Calculates required number of mesh rows and columns with a target of
     * number of rows and columns being equal (if not possible then number of
     * columns being the higher).</li>
     * <li>Calculates the number of bits required to encode row number and
     * column number of a switch to form the address of a switch.</li>
     * <li>Instantiate all the switch by assigning them corresponding address
     * generated from row and column.</li>
     * <li>For every instantiated switch instantiate the required number of
     * adjacent node.</li>
     * </ul>
     * <p/>
     * <p/>
     * Assumptions:
     * <li>This method considers a topology that has either m*n Mesh Switches.
     * Here n = m or m+1. </li>
     * <li>This method also works for the Torus network. </li>
     * <li>The Number of IP Nodes and Switches adjusted according to the n * m
     * switch architecture. Where, n = m or m+1, i.e. the architecture is
     * adjusted to have a square shape.</li>
     * </p>
     *
     * @see MeshSwitch
     * @see NetworkManager
     * @see IConstants
     */
    public void createMeshNetwork() {
        int i, address, numSwitch, row, col, noOfAdjNode;
        double factor;
        MeshSwitch meshSwitch;
        Node nd;

        noOfAdjNode = IConstants.MESH_ADJ_NODE;
        numSwitch = IConstants.NUMBER_OF_IP_NODE / noOfAdjNode;

        /* Check the assumptions in the documentation of this method. */
        IConstants.MESH_ROW = (int) Math.floor(Math.sqrt(numSwitch));
        IConstants.MESH_COL = (int) Math.ceil(Math.sqrt(numSwitch));
        IConstants.MESH_NODE_BITS_REQ = (int) Math.ceil(Math
                .log(IConstants.MESH_ADJ_NODE)
                / Math.log(2));
        IConstants.CURRENT_LINK_COUNT = IConstants.MESH_ADJ_NODE + 4;
        IConstants.MESH_ROW_BITS = (int) Math.ceil(Math
                .log(IConstants.MESH_ROW)
                / Math.log(2));
        IConstants.MESH_COL_BITS = (int) Math.ceil(Math
                .log(IConstants.MESH_COL)
                / Math.log(2));

        /*
           * Readjusted Number of IP Nodes and Number of Switches .. Only n * m
           * swithces are possible.. where n = m or m+1
           */
        IConstants.NUMBER_OF_SWITCH = IConstants.MESH_ROW *
                IConstants.MESH_COL;
        IConstants.NUMBER_OF_IP_NODE = IConstants.NUMBER_OF_SWITCH
                * noOfAdjNode;

        // Creates the Mesh Switches
        for (i = 0; i < IConstants.NUMBER_OF_SWITCH; i++) {
            row = i / IConstants.MESH_COL;
            col = i % IConstants.MESH_COL;
            address = (row << IConstants.MESH_COL_BITS) + col;
            meshSwitch = new MeshSwitch(IConstants.CURRENT_LINK_COUNT,
                    IConstants.CURRENT_VC_COUNT, address, noOfAdjNode,
                    IConstants.CURRENT_ADJ_SWITCH, i);
            switchList.add(meshSwitch);

            // add IP Nodes to the Mesh network.unit.switches.Switch
            address = address << IConstants.MESH_NODE_BITS_REQ;
            for (int k = 0; k < noOfAdjNode; k++) {
                factor = NetworkManager.getHelpingUtility()
                        .getNextRandomNumber();
                factor = factor * 5 + 4;
                int intVal = (int) factor;
                factor = (double) intVal / 10;

                if (IConstants.ASYNCHRONOUS)
                    nd = new Node(address + k, meshSwitch, k,
                            IConstants.CURRENT_VC_COUNT, factor);
                else
                    nd = new Node(address + k, meshSwitch, k,
                            IConstants.CURRENT_VC_COUNT, 1.0);
                meshSwitch.setAdjacentNode(nd, k);
                nodeList.add(nd);
            }
        }

        // assign index to node for statistical purpose
        for (i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            ((Node) nodeList.get(i)).setNodeListIndex(i);
        }
    }

    /**
     * Completes the creation of mesh network by setting the adjacency
     * relationship between the switches of consecutive different rows and
     * columns. The switches are added in the network in row major order i.e.
     * first the earlier rows are filled. The method works in the following
     * steps.
     * <p/>
     * <ul>
     * <li>For every switch determine its row and column index.</li>
     * <li>Calculates minimum number of switches required in the network to
     * have an adjacent switch in the bottom direction for this switch.</li>
     * <li>Calculates minimum number of switches required in the network to
     * have an adjacent switch in the right direction for this switch.</li>
     * <li>If row index of this switch is greater than zero then this switch
     * will have adjacent switch in top direction. If it has adjacent switch in
     * top direction then that is found out (just one row earlier and same
     * column) and corresponding adjacency field is updated for each switch. And
     * then input/output link controllers for top direction are added.</li>
     * <li>If row index of this switch is less than the maximum number of rows
     * in the mesh network and the network has sufficient number of switches to
     * have an adjacent switch in bottom direction then the switch in bottom
     * direction is found out (just one row below and same column). The
     * adjacency fields are updated for both these switches along with adding
     * input/output link controller for bottom direction.</li>
     * <li>In the similar way left and right adjacent switches are found out by
     * using column index value of the switch and corresponding adjacency
     * information are updates.</li>
     * </ul>
     *
     * @see MeshSwitch
     * @see network.unit.switches.link.InputLinkController
     * @see network.unit.switches.link.OutputLinkController
     * @see IConstants
     */
    private void setAdjacentMeshSwitch() {
        MeshSwitch meshSwitch;
        int noOfAdjNode;
        int i, row, col, minSwitchBottomReq, minSwitchRightReq;

        int maxRow = IConstants.MESH_ROW - 1;
        int maxCol = IConstants.MESH_COL - 1;

        // int numSwitch = network.common.IConstants.NUMBER_OF_IP_NODE /
        // network.common.IConstants.MESH_ADJ_NODE ;
        int numSwitch = IConstants.NUMBER_OF_SWITCH;

        for (i = 0; i < numSwitch; i++) {
            row = i / IConstants.MESH_COL;
            col = i % IConstants.MESH_COL;
            meshSwitch = (MeshSwitch) (switchList.get(i));
            noOfAdjNode = IConstants.MESH_ADJ_NODE;
            minSwitchBottomReq = ((row + 1) * IConstants.MESH_COL) + col + 1;
            minSwitchRightReq = row * IConstants.MESH_COL + col + 1 + 1;

            if (row > 0) {
                // top
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get(((row - 1) * IConstants.MESH_COL) + col),
                        IConstants.SWITCH_TOP);
            } else {
                meshSwitch.setInputLinkController(IConstants.SWITCH_TOP
                        + noOfAdjNode, null);
                meshSwitch.setOutputLinkController(IConstants.SWITCH_TOP
                        + noOfAdjNode, null);
            }
            if (row < maxRow && numSwitch >= minSwitchBottomReq) {
                // bottom
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get(((row + 1) * IConstants.MESH_COL) + col),
                        IConstants.SWITCH_BOTTOM);
            } else {
                meshSwitch.setInputLinkController(IConstants.SWITCH_BOTTOM
                        + noOfAdjNode, null);
                meshSwitch.setOutputLinkController(IConstants.SWITCH_BOTTOM
                        + noOfAdjNode, null);
            }

            if (col > 0) {
                // left
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get((row * IConstants.MESH_COL) + (col - 1)),
                        IConstants.SWITCH_LEFT);
            } else {
                meshSwitch.setInputLinkController(IConstants.SWITCH_LEFT
                        + noOfAdjNode, null);
                meshSwitch.setOutputLinkController(IConstants.SWITCH_LEFT
                        + noOfAdjNode, null);
            }

            if (col < maxCol && numSwitch >= minSwitchRightReq) {
                // right
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get((row * IConstants.MESH_COL) + (col + 1)),
                        IConstants.SWITCH_RIGHT);
            } else {
                meshSwitch.setInputLinkController(IConstants.SWITCH_RIGHT
                        + noOfAdjNode, null);
                meshSwitch.setOutputLinkController(IConstants.SWITCH_RIGHT
                        + noOfAdjNode, null);
            }
        }
    }

    /**
     * <p/>
     * This method completes the creation of torus network by setting the
     * adjacency relationship between the switches of consecutive different
     * levels. The method works in almost the same way as was described for mesh
     * network except the circular relation.
     * </p>
     * <p/>
     * <p/>
     * The switches in the top most and bottom most rows have adjacency relation
     * in top and bottom directions respectively whereas in mesh network they
     * had no adjacency relations. And in similar way leftmost and rightmost
     * columns have adjacency relation in left and right directions respectively
     * whereas in mesh network they had no adjacency relations.
     * </p>
     *
     * @see MeshSwitch
     * @see network.unit.switches.link.InputLinkController
     * @see network.unit.switches.link.OutputLinkController
     * @see IConstants
     */

    private void setAdjacentTorusSwitch() {
        MeshSwitch meshSwitch;
        // int noOfAdjNode ;
        int i, row, col, minSwitchBottomReq, minSwitchRightReq;

        int maxRow = IConstants.MESH_ROW - 1;
        int maxCol = IConstants.MESH_COL - 1;

        // int numSwitch = network.common.IConstants.NUMBER_OF_IP_NODE
        // /network.common.IConstants.MESH_ADJ_NODE ;
        int numSwitch = IConstants.NUMBER_OF_SWITCH;
        for (i = 0; i < numSwitch; i++) {
            row = i / IConstants.MESH_COL;
            col = i % IConstants.MESH_COL;
            meshSwitch = (MeshSwitch) (switchList.get(i));
            // noOfAdjNode = network.common.IConstants.MESH_ADJ_NODE ;
            minSwitchBottomReq = ((row + 1) * IConstants.MESH_COL) + col + 1;
            minSwitchRightReq = row * IConstants.MESH_COL + col + 1 + 1;

            if (row > 0) {
                // top
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get(((row - 1) * IConstants.MESH_COL) + col),
                        IConstants.SWITCH_TOP);
            } else {
                // first to last row
                // System.out.println("size: " + switchList.size() + " ... data:
                // " + ( (maxRow - 1) *
                // network.common.IConstants.MESH_COL) + col);
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get(((maxRow - 1) * IConstants.MESH_COL) + col),
                        IConstants.SWITCH_TOP);
            }
            if (row < maxRow && numSwitch >= minSwitchBottomReq) {
                // bottom
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get(((row + 1) * IConstants.MESH_COL) + col),
                        IConstants.SWITCH_BOTTOM);
            } else {
                // last to first row
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList.get(col),
                        IConstants.SWITCH_BOTTOM);
            }

            if (col > 0) {
                // left
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get((row * IConstants.MESH_COL) + (col - 1)),
                        IConstants.SWITCH_LEFT);
            } else {
                // first to last col
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get((row + 1) * IConstants.MESH_COL - 1),
                        IConstants.SWITCH_LEFT);
            }

            if (col < maxCol && numSwitch >= minSwitchRightReq) {
                // right
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList
                        .get((row * IConstants.MESH_COL) + (col + 1)),
                        IConstants.SWITCH_RIGHT);
            } else {
                // last to first col
                meshSwitch.setAdjacentSwitch((MeshSwitch) switchList.get(row
                        * IConstants.MESH_COL), IConstants.SWITCH_RIGHT);
            }
        }
    }

    /**
     * Instantiates all the node and Butterfly Fat Tree switches of the fat
     * tree network as well as assigns which node will be connected with which
     * fat tree switch. The method performs in the flowing manner.
     * <p/>
     * <ul>
     * <li>Calculates the number of levels through which the network will be
     * distributed.</li>
     * <li>Calculates the number of bits required to identify each switch and
     * node.</li>
     * <li>Calculates number of fat tree switch will exist in level l.</li>
     * <li>Instantiates corresponding number of fat tree switches for the level
     * l.</li>
     * <li>If that level is the lowest level of the network of fat tree switch
     * then instantiate four node for every switch of that level.</li>
     * </ul>
     * <p/>
     * <p/>
     * Assumptions:
     * <li>This method also works for the ExtendedButterFly network. </li>
     * <li>The Number of IP Nodes and Switches adjusted according to the a
     * complete fat tree format. </li>
     * </p>
     *
     * @see FatTreeSwitch
     * @see network.unit.switches.link.InputLinkController
     * @see network.unit.switches.link.OutputLinkController
     * @see IConstants
     */
    public void createFatTreeNetwork() {
        int i, j, switchIndex, address, numLevel, numSwitch;
        double factor;
        FatTreeSwitch fatSwitch;
        Node nd;
        int temp = IConstants.FAT_NUM_ADJ_CHILD;

        // set IP network.unit.node.Node to a value power of 4
        while (temp < IConstants.NUMBER_OF_IP_NODE)
            temp *= IConstants.FAT_NUM_ADJ_CHILD;
        IConstants.NUMBER_OF_IP_NODE = temp;
        IConstants.FAT_NUM_INDEX_BIT = (int) Math.ceil(Math
                .log(IConstants.NUMBER_OF_IP_NODE)
                / Math.log(2));

        numLevel = (int) (Math.log(IConstants.NUMBER_OF_IP_NODE) / Math
                .log(IConstants.FAT_NUM_ADJ_CHILD));
        IConstants.FAT_NUM_ADDR_BITS = (int) Math.ceil(Math.log(numLevel + 1)
                / Math.log(2))
                + IConstants.FAT_NUM_INDEX_BIT;

        // Creates the Fat Tree Switches
        switchIndex = 0;
        for (i = 1; i <= numLevel; i++) {
            numSwitch = IConstants.NUMBER_OF_IP_NODE / (1 << (i + 1));
            for (j = 0; j < numSwitch; j++) {

                address = (i << IConstants.FAT_NUM_INDEX_BIT) + j;
                fatSwitch = new FatTreeSwitch(address, switchIndex);
                switchList.add(fatSwitch);
                if (i == 1) {
                    // add IP Nodes to the Mesh network.unit.switches.Switch
                    int nodeAddr = (address & ((1 << IConstants.FAT_NUM_INDEX_BIT) - 1))
                            * IConstants.FAT_NUM_ADJ_CHILD;
                    for (int k = 0; k < IConstants.FAT_NUM_ADJ_CHILD; k++) {
                        factor = NetworkManager.getHelpingUtility()
                                .getNextRandomNumber();
                        factor = factor * 5 + 4;
                        int intVal = (int) factor;
                        factor = (double) intVal / 10;

                        if (IConstants.ASYNCHRONOUS)
                            nd = new Node(nodeAddr + k, fatSwitch, k,
                                    IConstants.CURRENT_VC_COUNT, factor);
                        else
                            nd = new Node(nodeAddr + k, fatSwitch, k,
                                    IConstants.CURRENT_VC_COUNT, 1.0);

                        fatSwitch.setAdjacentNode(nd, k);
                        nodeList.add(nd);
                    }
                }
                if (i == numLevel) {
                    fatSwitch.setOutputLinkController(
                            IConstants.FAT_NUM_ADJ_CHILD, null);
                    fatSwitch.setOutputLinkController(
                            IConstants.FAT_NUM_ADJ_CHILD + 1, null);
                    fatSwitch.setInputLinkController(
                            IConstants.FAT_NUM_ADJ_CHILD, null);
                    fatSwitch.setInputLinkController(
                            IConstants.FAT_NUM_ADJ_CHILD + 1, null);

                }
                switchIndex++;
            }
        }
        IConstants.FAT_NUM_SWITCH = switchIndex;
        IConstants.NUMBER_OF_SWITCH = switchIndex;

        // assign index to node for statistical purpose
        for (i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            ((Node) nodeList.get(i)).setNodeListIndex(i);
        }
    }

    /**
     * This method completes the creation of butterfly fat tree network by
     * setting the adjacency relationship between the switches of consecutive
     * different levels. The method works in the following steps.
     * <p/>
     * <ul>
     * <li>Calculates the number of levels through which the network is
     * distributed.</li>
     * <li>Calculates the number of switches that will exist in top most level
     * of the network.</li>
     * <li>For every switch other than top level switches
     * <ul>
     * <li>Finds its level value and index value in that level.</li>
     * <li>Finds out the first parent switch exists in the upper level by using
     * current switchs level and index in level values.</li>
     * <li>Updates the parent child relationship between the found out parent
     * switch and current switch.</li>
     * <li>Find out the second parent switch exists in the upper level by using
     * current switchs level and index in level values.</li>
     * <li>Updates the parent child relationship between the found out parent
     * switch and current switch.</li>
     * <p/>
     * </ul>
     * </li>
     * </ul>
     */
    private void setAdjacentFatSwitch() {
        FatTreeSwitch fatSwitch, parentSwitch;
        int i, sLevel, sIndex, startIndex;
        int numSwitch;
        int numLevel;
        int nextSwitchIndex;
        int numTopLevelSwitch;

        numLevel = (int) (Math.log(IConstants.NUMBER_OF_IP_NODE) / Math
                .log(IConstants.FAT_NUM_ADJ_CHILD));
        nextSwitchIndex = IConstants.NUMBER_OF_IP_NODE
                / IConstants.FAT_NUM_ADJ_CHILD;
        numSwitch = switchList.size();
        numTopLevelSwitch = 1 << (numLevel - 1);

        for (i = 0; i < numSwitch - numTopLevelSwitch; i++) {
            fatSwitch = (FatTreeSwitch) (switchList.get(i));
            sLevel = fatSwitch.getSwitchLevel();
            sIndex = fatSwitch.getSwitchIndex();
            startIndex = getStartOfLevel(sLevel + 1)
                    + (sIndex / (1 << (sLevel + 1))) * (1 << sLevel)
                    + (sIndex % (1 << (sLevel - 1)));

            parentSwitch = (FatTreeSwitch) switchList.get(startIndex);
            fatSwitch.setParentSwitch(parentSwitch, 0);
            parentSwitch.setChildSwitch(fatSwitch,
                    (sIndex / (1 << (sLevel - 1)))
                            % IConstants.FAT_NUM_ADJ_CHILD);

            parentSwitch = (FatTreeSwitch) switchList.get(startIndex
                    + (1 << (sLevel - 1)));
            fatSwitch.setParentSwitch(parentSwitch, 1);
            parentSwitch.setChildSwitch(fatSwitch,
                    (sIndex / (1 << (sLevel - 1)))
                            % IConstants.FAT_NUM_ADJ_CHILD);
        }
    }

    /**
     * Instantiates all the node and mesh switches of the Octal network as well
     * as assigns which node will be connected with which Octal switch. The
     * switches are added grouped in a network.unit of 8 switches. These units in the
     * network are then added in row major order i.e. first the earlier rows are
     * filled. The method performs in the following manner.
     * <p/>
     * <ul>
     * <li>Determines how many adjacent node per switch.</li>
     * <li>Determines the number of switches required in the network.</li>
     * <li>Determines the number of units required in the network.</li>
     * <li>Calculates required number of network.unit rows and columns with a target of
     * number of rows and columns being equal (if not possible then number of
     * columns being the higher).</li>
     * <li>Calculates the number of bits required to encode network.unit row number and
     * column number of a network.unit to form the address of a network.unit.</li>
     * <li>Computes the switch address from the network.unit address to which it
     * belongs.</li>
     * <li>Instantiate all the switch by assigning them the corresponding
     * address.</li>
     * <li>For every instantiated switch instantiate the required number of
     * adjacent node.</li>
     * </ul>
     *
     * @see OctalSwitch
     * @see NetworkManager
     * @see IConstants
     */
    public void createOctalNetwork() {
        int noOfAdjNode, numSwitch, numUnit, unitRow, unitCol, unitAddress;
        int i, k, j, switchAddress, nodeAddress;
        OctalSwitch octalSwitch;
        double factor;
        Node nd;

        noOfAdjNode = IConstants.OCTAL_ADJ_NODE;
        numSwitch = IConstants.NUMBER_OF_IP_NODE / noOfAdjNode;
        numUnit = numSwitch / 8; // each network.unit contains 8 switches

        IConstants.OCTAL_UNIT_ROW = (int) Math.floor(Math.sqrt(numUnit));
        IConstants.OCTAL_UNIT_COL = (int) Math.ceil(Math.sqrt(numUnit));
        IConstants.OCTAL_NODE_BITS_REQ = (int) Math.ceil(Math
                .log(IConstants.OCTAL_ADJ_NODE)
                / Math.log(2));
        IConstants.OCTAL_UNIT_ROW_BITS_REQ = (int) Math.ceil(Math
                .log(IConstants.OCTAL_UNIT_ROW)
                / Math.log(2));
        IConstants.OCTAL_UNIT_COL_BITS_REQ = (int) Math.ceil(Math
                .log(IConstants.OCTAL_UNIT_COL)
                / Math.log(2));
        // network.common.IConstants.OCTAL_UNIT_SWITCH_BITS_REQ = 3;

        IConstants.CURRENT_ADJ_SWITCH = 4;
        IConstants.CURRENT_LINK_COUNT = IConstants.OCTAL_ADJ_NODE
                + IConstants.CURRENT_ADJ_SWITCH;

        /* Readjusted Number of Unit, network.unit.switches.Switch and Nodes */
        IConstants.NUMBER_OF_SWITCH = 8 * IConstants.OCTAL_UNIT_ROW
                * IConstants.OCTAL_UNIT_COL;
        numUnit = IConstants.OCTAL_UNIT_ROW * IConstants.OCTAL_UNIT_COL;
        IConstants.NUMBER_OF_IP_NODE = IConstants.NUMBER_OF_SWITCH
                * IConstants.OCTAL_ADJ_NODE;

        for (k = 0; k < numUnit; k++)// for each network.unit
        {
            unitRow = k / IConstants.OCTAL_UNIT_COL;
            unitCol = k % IConstants.OCTAL_UNIT_COL;
            unitAddress = (unitRow << IConstants.OCTAL_UNIT_COL_BITS_REQ)
                    + unitCol;
            for (i = 0; i < 8; i++)// for each switch in an network.unit
            {
                switchAddress = (unitAddress << 3) + i;
                octalSwitch = new OctalSwitch(IConstants.CURRENT_LINK_COUNT,
                        IConstants.CURRENT_VC_COUNT, switchAddress,
                        noOfAdjNode, IConstants.CURRENT_ADJ_SWITCH, k * 8 + i);
                switchList.add(octalSwitch);
                nodeAddress = (switchAddress << IConstants.OCTAL_NODE_BITS_REQ);

                for (j = 0; j < IConstants.OCTAL_ADJ_NODE; j++) {
                    factor = NetworkManager.getHelpingUtility()
                            .getNextRandomNumber();
                    factor = factor * 5 + 4;
                    int intVal = (int) factor;
                    factor = (double) intVal / 10;

                    if (IConstants.ASYNCHRONOUS)
                        nd = new Node(nodeAddress + j, octalSwitch, j,
                                IConstants.CURRENT_VC_COUNT, factor);
                    else
                        nd = new Node(nodeAddress + j, octalSwitch, j,
                                IConstants.CURRENT_VC_COUNT, 1.0);

                    octalSwitch.setAdjacentNode(nd, j);
                    nodeList.add(nd);

                }
            }
        }
        // assign index to node for statistical purpose
        for (i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            ((Node) nodeList.get(i)).setNodeListIndex(i);
        }

    }

    /**
     * Completes the creation of octal network by setting the adjacency
     * relationship between the switches of the same network.unit and the switches of
     * other units.The units are added in the network in row major order i.e.
     * first the earlier rows are filled. The method works in the following
     * steps.
     * <p/>
     * <ul>
     * <li>For every network.unit determine its row and column index.</li>
     * <li>Each switch in the network can have maximum 4 links with adjacent
     * switches. 3 of those can used to connect to switches of the same network.unit
     * (identified by, OCTAL_SWITCH_MID_1, RIGHT_NODE, LEFT_NODE).
     * OCTAL_SWITCH_MID_2 is used to connect with a switch in a different network.unit.
     * Although the name suggested by LEFT_NODE, RIGHT_NODE implies switches
     * arranged according to clockwise or anticlockwise orientation, the
     * relative address of the switchs to whitch the switch in question is
     * connected by these two links are specified by the
     * IConstatnts.ADJ_NODE[][]. The relative index of the switch to which this
     * switch in question is connected via OCTAL_SWITCH_MID_1 is obtained from
     * (7-this_switch_address_in_the_unit). The switch in adjacent network.unit to which
     * the switch in question is connected via OCTAL_SWITCH_MID_2 is also can be
     * obtained by similar process.</li>
     * <p/>
     * <li> For those switches, OCTAL_SWITCH_MID_2 is not possible, output link
     * controller and input link controller are set to NULL.</li>
     *
     * @see network.unit.switches.OctalSwitch
     * @see network.unit.switches.link.InputLinkController
     * @see network.unit.switches.link.OutputLinkController
     * @see IConstants
     */

    private void setAdjacentOctalSwitch() {
        OctalSwitch octalSwitch;

        int noOfAdjNode, unitRow, unitCol;
        int j, k, mid, adjUnitRow, adjUnitCol, adjUnit;

        // int numSwitch = network.common.IConstants.NUMBER_OF_IP_NODE /
        // network.common.IConstants.OCTAL_ADJ_NODE ;
        // int numUnit = numSwitch / 8;
        int numSwitch = IConstants.NUMBER_OF_SWITCH;
        int numUnit = numSwitch / 8;

        int maxUnitRow = IConstants.OCTAL_UNIT_ROW - 1;
        int maxUnitCol = IConstants.OCTAL_UNIT_COL - 1;

        noOfAdjNode = IConstants.OCTAL_ADJ_NODE;
        for (k = 0; k < numUnit; k++) // for each network.unit
        {
            unitRow = k / IConstants.OCTAL_UNIT_COL;
            unitCol = k % IConstants.OCTAL_UNIT_COL;

            for (j = 0; j < 8; j++) {
                mid = 7 - j;
                octalSwitch = (OctalSwitch) switchList.get(k * 8 + j);
                octalSwitch.setAdjacentSwitch((OctalSwitch) switchList.get(k
                        * 8 + mid), IConstants.OCTAL_MID_NODE_1);
                octalSwitch.setAdjacentSwitch((OctalSwitch) switchList.get(k
                        * 8 + IConstants.OCTAL_ADJ[j][0]),
                        IConstants.RIGHT_NODE);
                octalSwitch
                        .setAdjacentSwitch((OctalSwitch) switchList.get(k * 8
                                + IConstants.OCTAL_ADJ[j][1]),
                                IConstants.LEFT_NODE);
                adjUnitRow = unitRow + IConstants.OCTAL_RC[j][0];
                adjUnitCol = unitCol + IConstants.OCTAL_RC[j][1];
                if (adjUnitRow >= 0 && adjUnitRow <= maxUnitRow
                        && adjUnitCol >= 0 && adjUnitCol <= maxUnitCol) {
                    adjUnit = adjUnitRow * IConstants.OCTAL_UNIT_COL
                            + adjUnitCol;
                    octalSwitch.setAdjacentSwitch((OctalSwitch) switchList
                            .get(adjUnit * 8 + mid),
                            IConstants.OCTAL_MID_NODE_2);
                } else {
                    octalSwitch.setInputLinkController(
                            IConstants.OCTAL_SWITCH_MID_2 + noOfAdjNode, null);
                    octalSwitch.setOutputLinkController(
                            IConstants.OCTAL_SWITCH_MID_2 + noOfAdjNode, null);
                }

            }
        }

    }

    /**
     * Instantiates all the node and Extended Butterfly Fat Tree switches of
     * the extended fat tree network as well as assigns which node will be
     * connected with which extended fat tree switch. The method performs in the
     * flowing manner.
     * <p/>
     * <ul>
     * <li>Calculates the number of levels through which the network will be
     * distributed.</li>
     * <li>Calculates the number of bits required to identify each switch and
     * node.</li>
     * <li>Calculates number of fat tree switch will exist in level l.</li>
     * <li>Instantiates corresponding number of fat tree switches for the level
     * l.</li>
     * <li>If that level is the lowest level of the network of fat tree switch
     * then instantiate four node for every switch of that level.</li>
     * </ul>
     * <p/>
     * <p/>
     * The approach is similar to that of Fat Tree network.Network.
     * <p/>
     * <p/>
     * Assumptions:
     * <li>This method also works for the ExtendedButterFly network. </li>
     * <li>The Number of IP Nodes and Switches adjusted according to a complete
     * fat tree format.</li>
     * </p>
     *
     * @see ExFatTreeSwitch
     * @see network.unit.switches.link.InputLinkController
     * @see network.unit.switches.link.OutputLinkController
     * @see IConstants
     */

    public void createExFatTreeNetwork() {
        int i, j, switchIndex, address, numLevel, numSwitch;
        double factor;
        ExFatTreeSwitch fatSwitch;
        Node nd;
        int temp = IConstants.FAT_NUM_ADJ_CHILD;

        // set IP network.unit.node.Node to a value power of 4
        while (temp < IConstants.NUMBER_OF_IP_NODE)
            temp *= IConstants.FAT_NUM_ADJ_CHILD;
        IConstants.NUMBER_OF_IP_NODE = temp;
        IConstants.FAT_NUM_INDEX_BIT = (int) Math.ceil(Math
                .log(IConstants.NUMBER_OF_IP_NODE)
                / Math.log(2));

        numLevel = (int) (Math.log(IConstants.NUMBER_OF_IP_NODE) / Math
                .log(IConstants.FAT_NUM_ADJ_CHILD));
        IConstants.FAT_NUM_ADDR_BITS = (int) Math.ceil(Math.log(numLevel + 1)
                / Math.log(2))
                + IConstants.FAT_NUM_INDEX_BIT;

        // Creates the Ex Fat Tree Switches
        switchIndex = 0;
        for (i = 1; i <= numLevel; i++) {
            numSwitch = IConstants.NUMBER_OF_IP_NODE / (1 << (i + 1));
            for (j = 0; j < numSwitch; j++) {

                address = (i << IConstants.FAT_NUM_INDEX_BIT) + j;
                fatSwitch = new ExFatTreeSwitch(address, switchIndex);
                switchList.add(fatSwitch);
                if (i == 1) {
                    // add IP Nodes to the Mesh network.unit.switches.Switch
                    int nodeAddr = (address & ((1 << IConstants.FAT_NUM_INDEX_BIT) - 1))
                            * IConstants.FAT_NUM_ADJ_CHILD;
                    for (int k = 0; k < IConstants.FAT_NUM_ADJ_CHILD; k++) {
                        factor = NetworkManager.getHelpingUtility()
                                .getNextRandomNumber();
                        factor = factor * 5 + 4;
                        int intVal = (int) factor;
                        factor = (double) intVal / 10;

                        if (IConstants.ASYNCHRONOUS)
                            nd = new Node(nodeAddr + k, fatSwitch, k,
                                    IConstants.CURRENT_VC_COUNT, factor);
                        else
                            nd = new Node(nodeAddr + k, fatSwitch, k,
                                    IConstants.CURRENT_VC_COUNT, 1.0);
                        fatSwitch.setAdjacentNode(nd, k);
                        nodeList.add(nd);
                    }
                }
                switchIndex++;
            }
        }
        IConstants.FAT_NUM_SWITCH = switchIndex;

        IConstants.NUMBER_OF_SWITCH = switchIndex;

        // assign index to node for statistical purpose
        for (i = 0; i < IConstants.NUMBER_OF_IP_NODE; i++) {
            ((Node) nodeList.get(i)).setNodeListIndex(i);
        }

    }

    /**
     * Completes the creation of extended butterfly fat tree network by setting
     * the adjacency relationship between the switches of consecutive different
     * levels and switches of current level. Switches of extended butterfly fat
     * tree network have relationship among the switches of the same level
     * depending on specific formula along with the child and parent
     * relationship between the switches of lower and upper level switches. The
     * method performs same works as was done for butterfly fat tree network
     * along with some extra works. Those are described below.
     * <p/>
     * <ul>
     * <li>Like butterfly fat tree finds out two parent of a switch and update
     * the child parent relation among those.</li>
     * <li>Calculates the left sibling index and right sibling index among the
     * block in which the switches form local connections.</li>
     * <li>Updates the left and right sibling relationship among those
     * switches.</li>
     * </ul>
     */

    private void setAdjacentExFatSwitch() {
        ExFatTreeSwitch fatSwitch, adjSwitch;
        int i, lIndex, rIndex, sLevel, sIndex, startIndex;
        int numSwitch, numLevel, nextSwitchIndex, numTopLevelSwitch;

        numLevel = (int) (Math.log(IConstants.NUMBER_OF_IP_NODE) / Math
                .log(IConstants.FAT_NUM_ADJ_CHILD));
        nextSwitchIndex = IConstants.NUMBER_OF_IP_NODE
                / IConstants.FAT_NUM_ADJ_CHILD;
        numSwitch = switchList.size();
        numTopLevelSwitch = 1 << (numLevel - 1);

        for (i = 0; i < numSwitch - numTopLevelSwitch; i++) {
            fatSwitch = (ExFatTreeSwitch) (switchList.get(i));
            sLevel = fatSwitch.getSwitchLevel();
            sIndex = fatSwitch.getSwitchIndex();
            startIndex = getStartOfLevel(sLevel + 1)
                    + (sIndex / (1 << (sLevel + 1))) * (1 << sLevel)
                    + (sIndex % (1 << (sLevel - 1)));
            // first parent
            adjSwitch = (ExFatTreeSwitch) switchList.get(startIndex);
            fatSwitch.setParentSwitch(adjSwitch, 0);
            adjSwitch.setChildSwitch(fatSwitch, (sIndex / (1 << (sLevel - 1)))
                    % IConstants.FAT_NUM_ADJ_CHILD);
            // second parent
            adjSwitch = (ExFatTreeSwitch) switchList.get(startIndex
                    + (1 << (sLevel - 1)));
            fatSwitch.setParentSwitch(adjSwitch, 1);
            adjSwitch.setChildSwitch(fatSwitch, (sIndex / (1 << (sLevel - 1)))
                    % IConstants.FAT_NUM_ADJ_CHILD);

            // left sibling
            lIndex = getStartOfLevel(sLevel)
                    + (sIndex / (1 << (sLevel + 1)))
                    * (1 << (sLevel + 1))
                    + ((sIndex - (1 << (sLevel - 1)) + (1 << (sLevel + 1))) % (1 << (sLevel + 1)));
            adjSwitch = (ExFatTreeSwitch) switchList.get(lIndex);
            fatSwitch.setSiblingSwitch(adjSwitch, 0);
            adjSwitch.setSiblingSwitch(fatSwitch, 1);
            // right sibling
            rIndex = getStartOfLevel(sLevel) + (sIndex / (1 << (sLevel + 1)))
                    * (1 << (sLevel + 1))
                    + ((sIndex + (1 << (sLevel - 1))) % (1 << (sLevel + 1)));
            adjSwitch = (ExFatTreeSwitch) switchList.get(rIndex);
            fatSwitch.setSiblingSwitch(adjSwitch, 1);
            adjSwitch.setSiblingSwitch(fatSwitch, 0);
        }
    }

    /**
     * Invoked by network.Network.setAdjacentFatSwitch() and
     * network.Network.setAdjacentExFatSwitch() to find out the first parent switch
     * exists in the upper level by using current switch level and index in
     * level values.
     *
     * @param level Level in the Butterfly Fat Tree, Extended Butterfly Fat Tree
     *              topology
     * @return Index value in the switch list
     * @see Network#setAdjacentFatSwitch()
     * @see Network#setAdjacentExFatSwitch()
     */
    private int getStartOfLevel(int level) {
        int i, count = 0;
        for (i = 1; i < level; i++)
            count += IConstants.NUMBER_OF_IP_NODE / (1 << (i + 1));
        return count;
    }

    /**
     * This method is called by the controller of the simulator to set initial
     * events required for starting the simulator. The method performs in the
     * following steps.
     * <p/>
     * <ul>
     * <li>For all resource node set next message generation timestamps</li>
     * <li>Resets all the switches and switching info vector of the switches.</li>
     * <li>Resets the statistical data calculator. </li>
     * <ul>
     */
    public void setInitalEvents() {
        int i;
        Switch nocSwitch;
        for (i = 0; i < nodeList.size(); i++) {
            Node node = (Node) nodeList.get(i);
            node.nodeTraffic.setNextMsgGenTime(0);
        }

        for (i = 0; i < switchList.size(); i++) {
            nocSwitch = (Switch) switchList.get(i);
            nocSwitch.resetSwitchingInfoVector();
        }

        // track no of link active in each network.unit.switches.Switch. For statistical purpose
        for (i = 0; i < switchList.size(); i++) {
            nocSwitch = (Switch) switchList.get(i);
            NetworkManager.getStatDataInstance().setSwitchNumLink(i,
                    nocSwitch.getNumLinkActive());
        }

    }

    /**
     * This method is used to transfer the outgoing flits at the output buffer
     * of resource node to the input buffer of the parent switch of that node.
     * The method performs its duty by calling node updateOutput(nCycle)
     * method for every simulation cycle.
     *
     * @param nCycle Simulation cycle
     */

    public void moveNodeTrafficFromNodeToSwitch(int nCycle) {
        int i;
        Node node;
        for (i = 0; i < nodeList.size(); i++) {
            node = (Node) nodeList.get(i);
            node.updateOutput(nCycle);
        }
    }

    public void updateSwitchTrafficPathRequest(int nCycle) {
        int i;
        Switch nocSwitch;
        for (i = 0; i < switchList.size(); i++) {
            nocSwitch = (Switch) switchList.get(i);
            nocSwitch.updateSwitchOutPathRequest(nCycle);
        }
    }

    /**
     * This method is used to transfer the incoming flits at the input buffer of
     * the switch to output buffer of that switch depending on the switching
     * info vector. The method performs its duty by calling switches
     * moveInputBufferToOutputBuffer(nCycle) method for every simulation cycle.
     *
     * @param nCycle Simulation cycle
     */

    public void moveSwitchTrafficFromInputBufferToOutputBuffer(int nCycle) {
        int i;
        Switch nocSwitch;
        for (i = 0; i < switchList.size(); i++) {
            nocSwitch = (Switch) switchList.get(i);
            nocSwitch.moveInputBufferToOutputBuffer(nCycle);
        }
    }

    /**
     * This method is used to transfer the outgoing flits at the output buffer
     * of the switch to input buffer of the adjacent switches and/or resource
     * node. The method performs its assigned activities by calling the
     * switches moveSwitchOutputBufferToInputBufferOfNodeSwitch(nCycle) method
     * for every simulation cycle.
     *
     * @param nCycle Simulation cycle
     */
    public void moveSwitchTrafficFromOutputBufferToInputBufferOfNodeSwitch(
            int nCycle) {
        int i;
        Switch nocSwitch;
        for (i = 0; i < switchList.size(); i++) {
            nocSwitch = (Switch) switchList.get(i);
            nocSwitch.moveSwitchOutputBufferToInputBufferOfNodeSwitch(nCycle);
        }
    }

    /**
     * This method is used to transfer the incoming flits at the input buffer of
     * resource node to the message center of that node. The method performs its
     * duty by calling node forwardFlitToNodeMessageCenter(nCycle) method for
     * every simulation cycle
     *
     * @param nCycle Simulation cycle
     */
    public void moveNodeTrafficFromInputBufferToNodeMsgCenter(int nCycle) {
        int i;
        Node node;
        for (i = 0; i < nodeList.size(); i++) {
            node = (Node) nodeList.get(i);
            node.forwardFlitToNodeMessageCenter(nCycle);
        }
    }

    /**
     * The method performs its duty by calling node
     * updateStatusAfterCycle(nCycle) and switches'
     * updateStatusAfterCycle(nCycle) method for every simulation cycle.
     *
     * @param curCycle Simulation cycle
     */

    public void updateAfterCycleStatus(int curCycle) {
        int i;
        Switch nocSwitch;
        Node node;
        for (i = 0; i < nodeList.size(); i++) {
            node = (Node) nodeList.get(i);
            node.updateStatusAfterCycle(curCycle);
        }
        for (i = 0; i < switchList.size(); i++) {
            nocSwitch = (Switch) switchList.get(i);
            nocSwitch.updateStatusAfterCycle(curCycle);
        }
    }

}