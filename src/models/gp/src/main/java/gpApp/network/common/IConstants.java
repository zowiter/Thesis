package gpApp.network.common;

/**
 * This class contains all configuration variables.
 *
 * @version 1.0
 */

public class IConstants {
    public static final int SWITCH_NORTH_WEST = 0;
    public static final int SWITCH_NORTH_EAST = 1;
    public static final int SWITCH_SOUTH_EAST = 2;
    public static final int SWITCH_SOUTH_WEST = 3;
    public static boolean DEBUG = false;
    public static int ADJACENT_TRAFIC=3;



    public static int WK_L = 2;
    public static int WK_W = 4;
    /**
     * Number of adjacent node per switch in the WK network.Network
     */
    public static int WK_ADJ_NODE = 1;

    /**
     * Type of the WK network.Network
     */
    public static int NET_WK = 5;


    public IConstants() {
    }
    /**
     * Input file name
     */
    public static String PARAM_FILE = "nocSimParameter.txt";

    /**
     * Output file name
     */
    public static String OUT_FILE = "nocSimOutput.txt";

    /**
     * Debug file name
     */
    public static String TRACE_FILE = "nocSimTrace.txt";

    /**
     * Type of the Header network.traffic.Flit
     */
    public static int HEADER_FLIT = 0;

    /**
     * Type of the Data network.traffic.Flit
     */
    public static int DATA_FLIT = 1;

    /**
     * Type of the NULL network.traffic.Flit
     */
    public static int NULL_FLIT = 2;

    // public static int INNER_NUM_FLIT_BUFFER = 10;

    /**
     * Number of rows in the Mesh/Torus network.Network
     */
    public static int MESH_ROW = 10;

    /**
     * Number of columns in the Mesh/Torus network.Network
     */
    public static int MESH_COL = 10;

    /**
     * Number of bits required to encode the row information in the Mesh/Torus
     * network.Network
     */
    public static int MESH_ROW_BITS = 14;

    /**
     * Number of bits required to encode the column information in the
     * Mesh/Torus network.Network
     */
    public static int MESH_COL_BITS = 14;

    /**
     * Number of adjacent node per switch in the Mesh/Torus network.Network
     */
    public static int MESH_ADJ_NODE = 1;

    /**
     * Number of bits required to encode the adjacent node index in the
     * Mesh/Torus network.Network
     */
    public static int MESH_NODE_BITS_REQ = 2;

    /**
     * Number of bits in an integer value
     */
    public static int INT_SIZE = 32;

    // / usually used in the mesh and torus network
    /**
     * Link no for the switch to the left
     */
    public static int SWITCH_LEFT = 0;

    /**
     * Link no for the switch to the top
     */
    public static int SWITCH_TOP = 1;

    /**
     * Link no for the switch to the right
     */
    public static int SWITCH_RIGHT = 2;

    /**
     * Link no for the switch to the bottom
     */
    public static int SWITCH_BOTTOM = 3;

    /**
     * Number of bits required to encode the address of a node.
     */
    public static int NUM_ADDR_BITS = 6;

    /**
     * Number of bits required to encode the virtual channel.
     */
    public static int NUM_VCID_BITS = 3;

    /**
     * Number of bits requird in the header flit to encode the total number of
     * flits per packet
     */
    public static int NUM_FLITS_BITS = 16;

    /**
     * Number of bits required to encode the type of the flit.
     */
    public static int NUM_FLIT_TYPE_BITS = 1;

    /**
     * Current simulation cycle
     */
    public static int CUR_CYCLE = 0;

    // public static int MAX_VC_COUNT = 16;

    /**
     * Maximum number of packet storage in the network.unit.node.Node's internal buffer.
     */
    public static int MAX_MESSAGE_NUMBER = 200;

    /**
     * Default number of virtual channels per physical channel
     */
    public static int DEFAULT_VC_COUNT = 4;

    // Traffic Generation

    /**
     * Type of the Traffic
     */
    public static int TRAFFIC_TYPE = 0;

    /**
     * Type of the Union Traffic Distribution
     */
    public static int TRAFFIC_UNIFORM = 0;

    /**
     * Type of the Local Traffic Distribution
     */
    public static int TRAFFIC_LOCAL = 1;

    /**
     * Type of the message size. If true, then message size is uniform, else
     * exponentially distributed
     */
    public static boolean FIXED_MESSAGE_LENGTH = false;

    // Fat Tree network.unit.switches.Switch
    /**
     * Number of bits required to encode the index of the node the Fat Tree
     * network.Network
     */
    public static int FAT_NUM_INDEX_BIT = 10;

    // public static int FAT_NUM_LEVEL_BIT = 6;

    /**
     * Number of adjacent child node/switches in the Fat Tree network.Network
     */
    public static int FAT_NUM_ADJ_CHILD = 4;

    /**
     * Number of adjacent parent switches in the Fat Tree network.Network
     */
    public static int FAT_NUM_ADJ_PARENT = 2;

    /**
     * Number of adjacent sibling switches in the Extended Fat Tree network.Network
     */
    public static int FAT_NUM_ADJ_SIBLING = 2;

    // public static int FAT_NODE_BITS = 2;

    /**
     * Number of switches that come into consideration in the Fat Tree network.Network
     */
    public static int FAT_NUM_SWITCH = 1;

    /**
     * Number of bits required to encode the address of a node in the Fat Tree
     * network.Network
     */
    public static int FAT_NUM_ADDR_BITS = 20;

    // network.Network Type
    /**
     * Type of the Fat Tree network.Network
     */
    public static int NET_FAT_TREE = 0;

    /**
     * Type of the Mesh network.Network
     */
    public static int NET_MESH = 1;

    /**
     * Type of the Torus network.Network
     */
    public static int NET_TORUS = 2;

    /**
     * Type of the Extended Fat Tree network.Network
     */
    public static int NET_EX_FAT_TREE = 3;

    /**
     * Type of the Octal network.Network
     */
    public static int NET_OCTAL = 4;


    /**
     * Number of warm up cycles.
     */
    public static int WARM_UP_CYCLE = 0;

    // input from file rquired

    /**
     * Type of the current network topology
     */
    public static int CURRENT_NET = 2;

    /**
     * Average message generation interval in cycles
     */
    public static int AVG_INTER_ARRIVAL = 20;

    /**
     * Average message length in bytes
     */
    public static int AVG_MESSAGE_LENGTH = 100;// bytes

    /**
     * network.traffic.Flit length in bits
     */
    public static int FLIT_LENGTH = 64; // bits

    /**
     * Number of IP node in the network
     */
    public static int NUMBER_OF_IP_NODE = 100;

    /**
     * Number of switches in the network
     */
    public static int NUMBER_OF_SWITCH = 100;

    /**
     * Number of maximum adjacent switches in the Mesh/Torus network.Network
     */
    public static int CURRENT_ADJ_SWITCH = 4;

    /**
     * Number of maximum physical links per switch
     */
    public static int CURRENT_LINK_COUNT = 8;

    /**
     * Number of virtual channels per physical link
     */
    public static int CURRENT_VC_COUNT = 4;

    /**
     * Number of flits per buffer for a virtual channel
     */
    public static int NUM_FLIT_PER_BUFFER = 2;

    /**
     * Number of simulation cycles. Statistics are collected only for these
     * number of cycles, excluding the warm-up cycles.
     */
    public static int NUM_CYCLE = 300;

    /**
     * Number of total simulation runs
     */
    public static int NUM_RUN = 4;

    /**
     * Trace-on variable. If debug-mode is on, then it is true. Otherwise, false
     */
    public static boolean TRACE = false;

    /**
     * Asycnchronous/Synchronous mode. If asynchronous operation, then it is
     * true. Otherwise, false
     */
    public static boolean ASYNCHRONOUS = false;

    // used in the octal network
    public static int LEFT_NODE = 0;

    public static int TOP_NODE = 1;

    public static int RIGHT_NODE = 2;

    public static int BOTTOM_NODE = 3;

    // Octal network.unit.switches.Switch
    // public static double OCTAL_LOCAL_TRAFFIC_PROBABILITY[]={0.4, 0.3, 0.2,
    // 0.05, 0.04, 0.01};
    public static double OCTAL_LOCAL_TRAFFIC_PROBABILITY = 0.5;

    public static int OCTAL_NUM_ADDR_BITS = 20; // not used

    public static int OCTAL_MID_NODE_1 = 1;

    public static int OCTAL_MID_NODE_2 = 3;

    public static int OCTAL_SWITCH_MID_1 = 1;

    public static int OCTAL_SWITCH_MID_2 = 3;

    public static int OCTAL_ADJ_NODE = 1;

    public static int OCTAL_UNIT_ROW = 4;

    public static int OCTAL_UNIT_COL = 4;

    public static int OCTAL_NODE_BITS_REQ = 4;

    public static int OCTAL_UNIT_ROW_BITS_REQ = 4;

    public static int OCTAL_UNIT_COL_BITS_REQ = 4;

    // public static int OCTAL_UNIT_SWITCH_BITS_REQ = 3;
    public static int OCTAL_RC[][] = {{-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

    public static int OCTAL_ADJ[][] = {{1, 3}, {2, 0}, {4, 1}, {0, 5},
            {2, 7}, {3, 6}, {7, 5}, {4, 6}};

}
