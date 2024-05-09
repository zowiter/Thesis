package gpApp.network.traffic;

import gpApp.network.common.IConstants;
import gpApp.network.NetworkManager;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

/**
 * The network.traffic.ConcreteNodeTraffic class extends the functionality of the abstract
 * network.traffic.NodeTraffic class. It defines the traffic configuration of the generated
 * messages of a node and also facilitates the encoding of Header and Data
 * flits.
 *
 * @version 1.0
 */
public class ConcreteNodeTraffic extends NodeTraffic {

    public static long LOCAL_IN = 0;

    public static long LOCAL_OUT = 0;

    /**
     * Constructor of the class. It also invokes constructor of the base
     * network.traffic.NodeTraffic class.
     *
     * @param address address of the node to which this class belongs
     */
    public ConcreteNodeTraffic(int address) {
        super(address);

    }

    /**
     * <p/>
     * Generates a packet and sets the next message generation time for the
     * node.
     * </p>
     * <p/>
     * <p/>
     * moveNodeTrafficFromNodeToSwitch(nCycle) method of network.Network object calls
     * the updateOutput(nCycle) method of network.unit.node.Node which then checks if actual
     * parameter nCycle is equal to the state variable nextMessageGenTime of
     * network.traffic.NodeTraffic objects and if so then that method calls this generateMessage
     * method.
     * </p>
     * <p/>
     * <p/>
     * The methods performs in the following steps.
     * <ul>
     * <li>Gets total number of flits for this packet by exponential
     * distribution. </li>
     * <li>Finds out destination node number other than itself in the range of
     * number of Nodes. The destination node number is then converted to address
     * format depending on network type. </li>
     * <li>Generates one header flit and stores information</li>
     * <li>Generates remaining data flits with random data.</li>
     * <li>Calls the setNextMsgGenTime to set the next message generation time.
     * </li>
     * </ul>
     * </p>
     *
     * @param curCycle        current simulation cycle
     * @param curMessageCount current message stored in the buffer of the node
     * @return a Vector data structure containing the whole packet
     */
    public Vector generateMessage(int curCycle, int curMessageCount) {
        Vector packet = new Vector();
        int vcId = 0; // dummy VC ID is given while Flits are produced from
        // packet;
        int i;

        if (curMessageCount >= IConstants.MAX_MESSAGE_NUMBER) {
            nextMsgGenTime = curCycle + 1;
            return null;
        }
        int noOfFlit = this.getMessageSize();

        int destination = 0;

        destination = this.getDestination();
        packet.add(createHeaderFlit(destination, noOfFlit, vcId, curCycle));

        for (i = 1; i < noOfFlit; i++) {
            packet.add(createDataFlit(vcId, curCycle, destination));
        }
        setNextMsgGenTime(curCycle);

        return packet;
    }

    /**
     * Updates the state variable nextMessageGenTime of this class (inherited
     * from network.traffic.NodeTraffic class) by calculating a time from exponential
     * distribution using the average inter message generation time and adding
     * this time with curCycle. The method is called by network.Network Initialization
     * method and updateOutput() method of network.unit.node.Node just after calling
     * generateMessage().
     *
     * @param curCycle current simulation cycle
     */
    public void setNextMsgGenTime(int curCycle) {

        nextMsgGenTime = (int) (-1 * IConstants.AVG_INTER_ARRIVAL * Math
                .log(NetworkManager.getHelpingUtility().getNextRandomNumber()))
                + curCycle + 1;

        if (IConstants.TRACE) {
            try {
                RandomAccessFile raf = new RandomAccessFile(
                        IConstants.TRACE_FILE, "rw");
                raf.seek(raf.length());
                raf.writeBytes("\nCycle " + curCycle + " network.unit.node.Node " + address
                        + " will produce Message at " + nextMsgGenTime);

                raf.close();
            } catch (IOException ioex) {
            }
        }

    }

    /**
     * Return the nextMsgGenTime (next message generation time) variable of this
     * class.
     *
     * @return next message generation time
     */

    public int getNextMsgGenTime() {

        return this.nextMsgGenTime;
    }

    /**
     * Returns the address of the destination node. This method calls an
     * appropriate method for generating address of the destination according to
     * the network topology.
     *
     * @return address of the destination node
     */

    protected int getDestination() {

        int destination = 0;
        if (IConstants.CURRENT_NET == IConstants.NET_WK) {
            destination = generateDestinationForWKNetwork(address);
        } else if (IConstants.CURRENT_NET == IConstants.NET_MESH) {
            destination = generateDestinationForMeshNetwork(address);
        } else if (IConstants.CURRENT_NET == IConstants.NET_TORUS) {
            destination = generateDestinationForTorusNetwork(address);
        } else if (IConstants.CURRENT_NET == IConstants.NET_FAT_TREE
                || IConstants.CURRENT_NET == IConstants.NET_EX_FAT_TREE) {

            destination = generateDestinationForFatTreeNetwork(address);

        } else if (IConstants.CURRENT_NET == IConstants.NET_OCTAL) {
            destination = generateDestinationForOctalNetwork(address);
        }

        return destination;
    }

    /**
     * Generates a header flit and return it to message generator method. It
     * generates the header flit in following fashion.
     * <ul>
     * <li> Check if the flit length is sufficient to contain the header flit
     * info. If not update the flit length to minimum size required to hold the
     * header info. </li>
     * <li> Then encode the following information in that order from LSB to MSB.
     * <ul>
     * <li> network.traffic.Flit Type (here header flit) � 1 or 2 bits </li>
     * <li> Virtual channel number � bit length depends on number of virtual
     * channel per physical channel used in the system. </li>
     * <li> Calculate number of bits required to encode source and destination
     * addresses and this number is encoded in 6 bits (changeable). </li>
     * <li> Source address is then encoded by using calculated number of bits.</li>
     * <li> Destination address is then encoded by using calculated number of
     * bits.</li>
     * <ul>
     * </li>
     * </ul>
     *
     * @param destination address of the destination node
     * @param noOfFlit    no of flits in the packet
     * @param vcId        virtual channel no
     * @param curCycle    current simulation cycle
     * @return Header network.traffic.Flit
     */

    protected Flit createHeaderFlit(int destination, int noOfFlit, int vcId,
                                    int curCycle) {

        int flitData = 0, bitUsed = 0;
        int noOfInt = 0;
        int data[];
        int minFlitLength = IConstants.FLIT_LENGTH;
        IConstants.NUM_VCID_BITS = (int) Math.ceil(Math
                .log(IConstants.CURRENT_VC_COUNT)
                / Math.log(2));

        if (IConstants.CURRENT_NET == IConstants.NET_WK) {
            minFlitLength = IConstants.NUM_FLIT_TYPE_BITS
                    + IConstants.NUM_VCID_BITS + IConstants.NUM_ADDR_BITS
                    + IConstants.NUM_FLITS_BITS + IConstants.WK_L * 2 * 2;
        } else if (IConstants.CURRENT_NET == IConstants.NET_MESH
                || IConstants.CURRENT_NET == IConstants.NET_TORUS) {
            minFlitLength = IConstants.NUM_FLIT_TYPE_BITS
                    + IConstants.NUM_VCID_BITS + IConstants.NUM_ADDR_BITS
                    + IConstants.NUM_FLITS_BITS + IConstants.MESH_ROW_BITS
                    + IConstants.MESH_COL_BITS + IConstants.MESH_NODE_BITS_REQ
                    + IConstants.MESH_ROW_BITS + IConstants.MESH_COL_BITS
                    + IConstants.MESH_NODE_BITS_REQ;
        } else if (IConstants.CURRENT_NET == IConstants.NET_FAT_TREE
                || IConstants.CURRENT_NET == IConstants.NET_EX_FAT_TREE) {
            minFlitLength = IConstants.NUM_FLIT_TYPE_BITS
                    + IConstants.NUM_VCID_BITS + IConstants.NUM_ADDR_BITS
                    + IConstants.NUM_FLITS_BITS + IConstants.FAT_NUM_ADDR_BITS
                    + IConstants.FAT_NUM_ADDR_BITS;

        } else if (IConstants.CURRENT_NET == IConstants.NET_OCTAL) {
            minFlitLength = IConstants.NUM_FLIT_TYPE_BITS
                    + IConstants.NUM_VCID_BITS + IConstants.NUM_ADDR_BITS
                    + IConstants.NUM_FLITS_BITS
                    + IConstants.OCTAL_UNIT_ROW_BITS_REQ
                    + IConstants.OCTAL_UNIT_COL_BITS_REQ + 3 + // 3 for 8
                    // switches per
                    // network.unit
                    IConstants.OCTAL_NODE_BITS_REQ

                    + IConstants.OCTAL_UNIT_ROW_BITS_REQ
                    + IConstants.OCTAL_UNIT_COL_BITS_REQ + 3 + // 3 for 8
                    // switches per
                    // network.unit
                    IConstants.OCTAL_NODE_BITS_REQ;

        }

        if (minFlitLength % IConstants.INT_SIZE > 0) {
            minFlitLength += IConstants.INT_SIZE
                    - (minFlitLength % IConstants.INT_SIZE);
        }
        if (minFlitLength < IConstants.FLIT_LENGTH) {
            minFlitLength = IConstants.FLIT_LENGTH;
        }

        data = new int[minFlitLength / IConstants.INT_SIZE];

        flitData = IConstants.HEADER_FLIT; // 1 bit for flit type
        bitUsed += IConstants.NUM_FLIT_TYPE_BITS;

        flitData = (vcId << bitUsed) | flitData; // network.common.IConstants.CURRENT_VC_COUNT
        // bits for VCID
        bitUsed += IConstants.NUM_VCID_BITS;

        int numAddrBits = 30;
        if (IConstants.CURRENT_NET == IConstants.NET_WK) {

            numAddrBits = IConstants.WK_L * 2;
        } else if (IConstants.CURRENT_NET == IConstants.NET_MESH
                || IConstants.CURRENT_NET == IConstants.NET_TORUS) {

            numAddrBits = IConstants.MESH_ROW_BITS + IConstants.MESH_COL_BITS
                    + IConstants.MESH_NODE_BITS_REQ;
        } else if (IConstants.CURRENT_NET == IConstants.NET_FAT_TREE
                || IConstants.CURRENT_NET == IConstants.NET_EX_FAT_TREE) {

            numAddrBits = IConstants.FAT_NUM_ADDR_BITS;
        } else if (IConstants.CURRENT_NET == IConstants.NET_OCTAL) {

            numAddrBits = IConstants.OCTAL_UNIT_ROW_BITS_REQ
                    + IConstants.OCTAL_UNIT_COL_BITS_REQ + 3 + // 3 for 8
                    // switches per
                    // network.unit
                    IConstants.OCTAL_NODE_BITS_REQ;
        }

        flitData = (numAddrBits << bitUsed) | flitData; //
        bitUsed += IConstants.NUM_ADDR_BITS; // how many bits required to
        // represent number of bits in
        // address

        if (bitUsed + IConstants.NUM_FLITS_BITS > IConstants.INT_SIZE) {
            int rest = bitUsed + IConstants.NUM_FLITS_BITS
                    - IConstants.INT_SIZE;
            int temp = noOfFlit
                    % (int) Math.pow(2, IConstants.NUM_FLITS_BITS - rest);

            flitData = (temp << bitUsed) | flitData;
            data[noOfInt++] = flitData;
            flitData = noOfFlit >>> (IConstants.NUM_FLITS_BITS - rest);
            bitUsed = rest;
        } else {
            flitData = (noOfFlit << bitUsed) | flitData;
            bitUsed += IConstants.NUM_FLITS_BITS;
        }
        // source address
        if (bitUsed + numAddrBits > IConstants.INT_SIZE) {
            int rest = bitUsed + numAddrBits - IConstants.INT_SIZE;
            int temp = address % (int) Math.pow(2, numAddrBits - rest);
            flitData = (temp << bitUsed) | flitData;
            data[noOfInt] = flitData;
            noOfInt++;
            flitData = address >>> (numAddrBits - rest);
            bitUsed = rest;
        } else {
            flitData = (address << bitUsed) | flitData;
            bitUsed += numAddrBits;
        }

        // dest address
        if (bitUsed + numAddrBits > IConstants.INT_SIZE) {
            int rest = bitUsed + numAddrBits - IConstants.INT_SIZE;
            int temp = destination % (int) Math.pow(2, numAddrBits - rest);
            flitData = (temp << bitUsed) | flitData;
            data[noOfInt] = flitData;
            noOfInt++;
            flitData = destination >>> (numAddrBits - rest);
            bitUsed = rest;
        } else {
            flitData = (destination << bitUsed) | flitData;
            bitUsed += numAddrBits;
        }
        // rest bits
        data[noOfInt] = flitData;
        Flit header = new Flit(data, curCycle);
        header.setDest(destination);
        header.setSource(this.address);
        /*
           * System.out.println("Header network.traffic.Flit (" + header.getSourceNode() + "," +
           * header.getDestinationNode() + ") Length = " +
           * header.getPacketLength()) ;
           */

        // System.out.println("Address BITs: "+network.common.IConstants.NUM_ADDR_BITS);
        return header;
    }

    /**
     * Generates a data flit and return it to message generator method. It
     * generates the data flit in following fashion:
     * <p/>
     * <ul>
     * <li> Calculate the required size of flit in bit. If current size is
     * smaller than required then current size is updated. </li>
     * <li> Then encode the following information in that order from LSB to MSB
     * <ul>
     * <li> network.traffic.Flit Type (here data flit) � 1 or 2 bits </li>
     * <li> Virtual channel number � bit length depends on number of virtual
     * channel per physical channel used in the system. This virtual channel
     * number acts as trace of packet in the network. </li>
     * <li> Then the data part for which this transmission process is encoded.
     * </li>
     * <p/>
     * </ul>
     * </li>
     * </ul>
     *
     * @param destination address of the destination node
     * @param vcId        virtual channel no
     * @param curCycle    current simulation cycle
     * @return Data network.traffic.Flit
     */
    protected Flit createDataFlit(int vcId, int curCycle, int destination) {
        int flitData = 0;
        int bitUsed = 0;
        int minFlitLength = IConstants.FLIT_LENGTH;
        IConstants.NUM_VCID_BITS = (int) Math.ceil(Math
                .log(IConstants.CURRENT_VC_COUNT)
                / Math.log(2));
        if (IConstants.CURRENT_NET == IConstants.NET_WK) {
            minFlitLength = IConstants.NUM_FLIT_TYPE_BITS
                    + IConstants.NUM_VCID_BITS + IConstants.NUM_ADDR_BITS
                    + IConstants.NUM_FLITS_BITS + IConstants.WK_L * 2 * 2;
        } else if (IConstants.CURRENT_NET == IConstants.NET_MESH
                || IConstants.CURRENT_NET == IConstants.NET_TORUS) {
            minFlitLength = IConstants.NUM_FLIT_TYPE_BITS
                    + IConstants.NUM_VCID_BITS + IConstants.NUM_ADDR_BITS
                    + IConstants.NUM_FLITS_BITS + IConstants.MESH_ROW_BITS
                    + IConstants.MESH_COL_BITS + IConstants.MESH_NODE_BITS_REQ
                    + IConstants.MESH_ROW_BITS + IConstants.MESH_COL_BITS
                    + IConstants.MESH_NODE_BITS_REQ;
        } else if (IConstants.CURRENT_NET == IConstants.NET_FAT_TREE
                || IConstants.CURRENT_NET == IConstants.NET_EX_FAT_TREE) {
            minFlitLength = IConstants.NUM_FLIT_TYPE_BITS
                    + IConstants.NUM_VCID_BITS + IConstants.NUM_ADDR_BITS
                    + IConstants.NUM_FLITS_BITS + IConstants.FAT_NUM_ADDR_BITS
                    + IConstants.FAT_NUM_ADDR_BITS;
        } else if (IConstants.CURRENT_NET == IConstants.NET_OCTAL) {
            minFlitLength = IConstants.NUM_FLIT_TYPE_BITS
                    + IConstants.NUM_VCID_BITS + IConstants.NUM_ADDR_BITS
                    + IConstants.NUM_FLITS_BITS
                    + IConstants.OCTAL_UNIT_ROW_BITS_REQ
                    + IConstants.OCTAL_UNIT_COL_BITS_REQ + 3 + // 3 for 8
                    // switches per
                    // network.unit
                    IConstants.OCTAL_NODE_BITS_REQ
                    + IConstants.OCTAL_UNIT_ROW_BITS_REQ
                    + IConstants.OCTAL_UNIT_COL_BITS_REQ + 3 + // 3 for 8
                    // switches per
                    // network.unit
                    IConstants.OCTAL_NODE_BITS_REQ;
        }

        if (minFlitLength % IConstants.INT_SIZE > 0) {
            minFlitLength += IConstants.INT_SIZE
                    - (minFlitLength % IConstants.INT_SIZE);
        }
        if (minFlitLength < IConstants.FLIT_LENGTH) {
            minFlitLength = IConstants.FLIT_LENGTH;
        }

        int noOfInt = minFlitLength / IConstants.INT_SIZE;
        int data[] = new int[noOfInt];

        flitData = IConstants.DATA_FLIT; // 1 bit for flit type
        bitUsed += IConstants.NUM_FLIT_TYPE_BITS;

        flitData = (vcId << bitUsed) | flitData; // network.common.IConstants.CURRENT_VC_COUNT
        // bits for VCID
        bitUsed += IConstants.NUM_VCID_BITS;

        data[0] = flitData; // 0xDDDDDD00 | flitData ;

        for (int i = 1; i < noOfInt; i++) {
            data[i] = Integer.MAX_VALUE - address; // (int)
            // (network.NetworkManager.getHelpingUtility().getNextRandomNumber()
            // * Integer.MAX_VALUE) ;

        }
        Flit dataFlit = new Flit(data, curCycle);
        dataFlit.setDest(destination);
        dataFlit.setSource(this.address);

        return dataFlit;
    }

    /**
     * <p/>
     * Returns the message size in flits, which may be either fixed or
     * exponential random number computed from the average message length(in
     * bytes) and flit length (in bits).
     * </p>
     *
     * @return message length in flits
     */
    protected int getMessageSize() {
        int noOfFlit;

        if (IConstants.FIXED_MESSAGE_LENGTH == false) {
            noOfFlit = (int) (-8
                    * IConstants.AVG_MESSAGE_LENGTH
                    * Math.log(NetworkManager.getHelpingUtility()
                    .getNextRandomNumber()) / IConstants.FLIT_LENGTH);
            noOfFlit = noOfFlit > 1 ? noOfFlit : 2;
        } else {
            noOfFlit = 8 * IConstants.AVG_MESSAGE_LENGTH
                    / IConstants.FLIT_LENGTH;
            noOfFlit = noOfFlit > 1 ? noOfFlit : 2;
        }

        System.out.println("min flit: " + noOfFlit);
        return noOfFlit;
        // return 10;
    }

    private int generateDestinationForWKNetwork(int address) {

        int destination = address;
        if (IConstants.TRAFFIC_TYPE == IConstants.ADJACENT_TRAFIC) {
            double random = Math.random();
            if (random > 0.9) {
                while (destination == address) {
                    int temp = ((int) (Math.random() * 3)) + 1; // random number between 1..3
                    destination = address >> 4;
                    destination = destination ^ temp;
                    temp = (int) (NetworkManager.getHelpingUtility()
                            .getNextRandomNumber() * 16);
                    destination = (destination << 4) + temp;
                }
            } else if (random > 0.75) {
                while (destination == address) {
                    int temp = ((int) (Math.random() * 3)) + 1; // random number between 1..3
                    destination = address >> 2;
                    destination = destination ^ temp;
                    temp = (int) (NetworkManager.getHelpingUtility()
                            .getNextRandomNumber() * 4);
                    destination = (destination << 2) + temp;
                }
            } else {
                while (destination == address) {
                    int temp = ((int) (Math.random() * 4));
                    destination = (address >> 2) << 2;
                    destination = destination | temp;
                }
            }
            return destination;
        } else if (IConstants.TRAFFIC_TYPE == IConstants.TRAFFIC_UNIFORM) {
            while (destination == address) {
                destination = (int) (NetworkManager.getHelpingUtility()
                        .getNextRandomNumber() * IConstants.NUMBER_OF_IP_NODE);
            }
            return destination;
        } else if (IConstants.TRAFFIC_TYPE == IConstants.TRAFFIC_LOCAL) {
            return -1;
        }
        return destination;
    }

    /**
     * Returns the address of a destination node in the Mesh network randomly.
     * This address is encoded in the appropriate format for the Mesh network
     * and it is computed in the following manner:
     * <p/>
     * <ul>
     * <li>Gets the destination node number randomly.</li>
     * <li>?Calculates the row of this node.
     * <p/>
     * row = (destination/(C*N)). <br>
     * Here C = Number of Columns in the Mesh network.Network, and N = Number of IP
     * node per network.unit.switches.Switch.
     * </p>
     * </li>
     * <li>Calculates the column of this node.
     * <p/>
     * column = ((destination mod (C*N)) / N)
     * </p>
     * </li>
     * <li>?Calculates its position, pos, in the switch.
     * <p/>
     * (pos = destination mod N)
     * </p>
     * </li>
     * <li>Finally the address of the destination node is encoded.
     * <p/>
     * address = row * 2 ^ (bitC + bitN) + col * bitN + pos
     * </p>
     * <p/>
     * bitC = Number of bits used in representing column number the Mesh
     * network.Network.<br>
     * bitN = Number of bits used in representing node number of the Mesh network.unit.switches.Switch
     * in network.Network.
     * </p>
     * <p/>
     * </li>
     * </ul>
     *
     * @param address address of the source node
     * @return address of a destination node (mesh network)
     */

    private int generateDestinationForMeshNetwork(int address) {
        int destination = address;
        int tempRow = 0, tempCol = 0, tempNode = 0;
        if (IConstants.TRAFFIC_TYPE == IConstants.TRAFFIC_UNIFORM || IConstants.TRAFFIC_TYPE == IConstants.ADJACENT_TRAFIC) {
            destination = address;
            while (destination == address) {
                destination = (int) (NetworkManager.getHelpingUtility()
                        .getNextRandomNumber() * IConstants.NUMBER_OF_IP_NODE);
                tempRow = destination
                        / (IConstants.MESH_COL * IConstants.MESH_ADJ_NODE);
                tempCol = (destination % (IConstants.MESH_COL * IConstants.MESH_ADJ_NODE))
                        / IConstants.MESH_ADJ_NODE;
                tempNode = destination % IConstants.MESH_ADJ_NODE;
                destination = (tempRow << (IConstants.MESH_COL_BITS + IConstants.MESH_NODE_BITS_REQ))
                        + (tempCol << IConstants.MESH_NODE_BITS_REQ) + tempNode;
            }

            return destination;
        } else if (IConstants.TRAFFIC_TYPE == IConstants.TRAFFIC_LOCAL) {
            return -1;
        }
        return -1;
    }

    /**
     * Returns the address of a destination node in the Torus network randomly.
     * This address is encoded in the appropriate format for the Torus network
     * and it is computed in the following manner:
     * <p/>
     * <ul>
     * <li>Gets the destination node number randomly.</li>
     * <li>?Calculates the row of this node.
     * <p/>
     * row = (destination/(C*N)). <br>
     * Here C = Number of Columns in the Torus network.Network, and N = Number of IP
     * node per network.unit.switches.Switch.
     * </p>
     * </li>
     * <li>Calculates the column of this node.
     * <p/>
     * column = ((destination mod (C*N)) / N)
     * </p>
     * </li>
     * <li>?Calculates its position, pos, in the switch.
     * <p/>
     * (pos = destination mod N)
     * </p>
     * </li>
     * <li>Finally the address of the destination node is encoded.
     * <p/>
     * address = row * 2 ^ (bitC + bitN) + col * bitN + pos
     * </p>
     * <p/>
     * bitC = Number of bits used in representing column number the Torus
     * network.Network.<br>
     * bitN = Number of bits used in representing node number of the Torus
     * network.unit.switches.Switch in network.Network.
     * </p>
     * <p/>
     * </li>
     * </ul>
     *
     * @param address address of the source node
     * @return address of a destination node (Torus network)
     */
    private int generateDestinationForTorusNetwork(int address) {
        int destination = address;
        int tempRow = 0, tempCol = 0, tempNode = 0;
        if (IConstants.TRAFFIC_TYPE == IConstants.TRAFFIC_UNIFORM || IConstants.TRAFFIC_TYPE == IConstants.ADJACENT_TRAFIC) {
            destination = address;
            while (destination == address) {
                destination = (int) (NetworkManager.getHelpingUtility()
                        .getNextRandomNumber() * IConstants.NUMBER_OF_IP_NODE);
                tempRow = destination
                        / (IConstants.MESH_COL * IConstants.MESH_ADJ_NODE);
                tempCol = (destination % (IConstants.MESH_COL * IConstants.MESH_ADJ_NODE))
                        / IConstants.MESH_ADJ_NODE;
                tempNode = destination % IConstants.MESH_ADJ_NODE;
                destination = (tempRow << (IConstants.MESH_COL_BITS + IConstants.MESH_NODE_BITS_REQ))
                        + (tempCol << IConstants.MESH_NODE_BITS_REQ) + tempNode;
            }

            return destination;
        } else if (IConstants.TRAFFIC_TYPE == IConstants.TRAFFIC_LOCAL) {
            return -1;
        }
        return -1;
    }

    /**
     * Returns the address of a destination node in the fat tree network
     * randomly.
     *
     * @param address address of the source node
     * @return address of a destination node (fat tree network)
     */
    private int generateDestinationForFatTreeNetwork(int address) {
        int destination = address;
        while (destination == address) {
            destination = (int) (NetworkManager.getHelpingUtility()
                    .getNextRandomNumber() * IConstants.NUMBER_OF_IP_NODE);
        }

        return destination;
    }

    private int generateDestinationForOctalNetwork(int address) {
        int destination = address;
        int destinationSwitch = 0, tempUnitSwitch = 0, tempUnitRow = 0, tempUnitCol = 0, tempUnitNode = 0;
        if (IConstants.TRAFFIC_TYPE == IConstants.TRAFFIC_UNIFORM || IConstants.TRAFFIC_TYPE == IConstants.ADJACENT_TRAFIC) {

            int tempUnit = 0;

            while (destination == address) {
                destination = (int) (NetworkManager.getHelpingUtility()
                        .getNextRandomNumber() * IConstants.NUMBER_OF_IP_NODE);

                destinationSwitch = destination / IConstants.OCTAL_ADJ_NODE;
                tempUnitSwitch = destinationSwitch % 8;

                tempUnit = destinationSwitch >> 3;
                tempUnitRow = tempUnit / IConstants.OCTAL_UNIT_COL;
                tempUnitCol = tempUnit % IConstants.OCTAL_UNIT_COL;

                tempUnitNode = destination % IConstants.OCTAL_ADJ_NODE;

                destination = tempUnitNode
                        + (tempUnitSwitch << IConstants.OCTAL_NODE_BITS_REQ)
                        + (tempUnitCol << (3 + IConstants.OCTAL_NODE_BITS_REQ))
                        + (tempUnitRow << (3 + IConstants.OCTAL_NODE_BITS_REQ + IConstants.OCTAL_UNIT_COL_BITS_REQ));

            }

            return destination;

        } else if (IConstants.TRAFFIC_TYPE == IConstants.TRAFFIC_LOCAL) {
            int ownUnitRow, ownUnitCol, ownUnit;

            ownUnit = address >> (IConstants.OCTAL_NODE_BITS_REQ + 3);
            ownUnitCol = ownUnit
                    & (int) (Math.pow(2, IConstants.OCTAL_UNIT_COL_BITS_REQ) - 1);
            ownUnitRow = (ownUnit >> IConstants.OCTAL_UNIT_COL_BITS_REQ)
                    & (int) (Math.pow(2, IConstants.OCTAL_UNIT_ROW_BITS_REQ) - 1);

            double randVal = NetworkManager.getHelpingUtility()
                    .getNextRandomNumber();

            if (randVal <= IConstants.OCTAL_LOCAL_TRAFFIC_PROBABILITY
                    || (IConstants.OCTAL_UNIT_ROW == 1 && IConstants.OCTAL_UNIT_COL == 1)) {
                while (true) {
                    tempUnitSwitch = (int) Math.floor(8 * NetworkManager
                            .getHelpingUtility().getNextRandomNumber());
                    tempUnitNode = (int) Math.floor(IConstants.OCTAL_ADJ_NODE
                            * NetworkManager.getHelpingUtility()
                            .getNextRandomNumber());

                    destination = tempUnitNode
                            + (tempUnitSwitch << IConstants.OCTAL_NODE_BITS_REQ)
                            + (ownUnitCol << (3 + IConstants.OCTAL_NODE_BITS_REQ))
                            + (ownUnitRow << (3 + IConstants.OCTAL_NODE_BITS_REQ + IConstants.OCTAL_UNIT_COL_BITS_REQ));

                    if (destination != address)
                        break;
                }

                System.out.println("Local-1: Address - " + address
                        + ", Destination - " + destination);
                LOCAL_IN++;
            } else {
                tempUnitSwitch = (int) Math.floor(8 * NetworkManager
                        .getHelpingUtility().getNextRandomNumber());
                tempUnitNode = (int) Math.floor(IConstants.OCTAL_ADJ_NODE
                        * NetworkManager.getHelpingUtility()
                        .getNextRandomNumber());
                tempUnitRow = ownUnitRow;
                tempUnitCol = ownUnitCol;

                while ((tempUnitRow == ownUnitRow)
                        && (tempUnitCol == ownUnitCol)) {
                    tempUnitRow = (int) Math.floor(IConstants.OCTAL_UNIT_ROW
                            * NetworkManager.getHelpingUtility()
                            .getNextRandomNumber());
                    tempUnitCol = (int) Math.floor(IConstants.OCTAL_UNIT_COL
                            * NetworkManager.getHelpingUtility()
                            .getNextRandomNumber());

                }

                destination = tempUnitNode
                        + (tempUnitSwitch << IConstants.OCTAL_NODE_BITS_REQ)
                        + (tempUnitCol << (3 + IConstants.OCTAL_NODE_BITS_REQ))
                        + (tempUnitRow << (3 + IConstants.OCTAL_NODE_BITS_REQ + IConstants.OCTAL_UNIT_COL_BITS_REQ));

                System.out.println("Local-2: Address - " + address
                        + ", Destination - " + destination);
                LOCAL_OUT++;
            }

            return destination;
        }

        return -1;

    }

}
