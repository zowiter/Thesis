package network;

import network.common.HelpingUtility;
import network.common.StatisticalData;
import network.common.ParamDTO;
import network.common.IConstants;

import java.util.Vector;

/**
 * <p/>
 * The network.NetworkManager class is responsible for instantiating a network instance. It
 * also initializes the network.common.StatisticalData class to re-establish its variables.
 * This class also initializes the configuration parameters in the network.common.IConstants
 * class by the input parameters in the system retrieve from the input file.
 * </p>
 * <p/>
 * <p/>
 * The network.NetworkManager class implements the Singleton Design Pattern.
 * </p>
 *
 * @version 1.0
 */

public class NetworkManager {
    /**
     * An static instance of the
     * <p/>
     * {@link NetworkManager} class which is useful for the implementation of
     * Singleton design pattern.
     */
    private static NetworkManager netManager = null;

    /**
     * An instance of the
     * <p/>
     * {@link Network} class to refer to the current simulating network.
     */
    private static Network network = null;

    /**
     * An instance of the
     * <p/>
     * {@link StatisticalData} class to perform the related statistical
     * computations.
     */
    private static StatisticalData statData = null;

    /**
     * An instance of the
     * <p/>
     * {@link HelpingUtility} class to re-initialize the random seed and read
     * the input file.
     */
    private static HelpingUtility helpUtility = null;

    // private static int networkType=0;

    /**
     * Keeps track of the current simulating network to read the relevant input
     * parameters.
     */
    private static int curSet;

    /**
     * Name of the input configuration file.
     */
    private static String paramFile;

    /**
     * Default percentage of warm_up_cycles to that of total simulation cycles.
     */
    private static double warm_up_percentage = 0.1;

    /**
     * Constructor of the network.NetworkManager class. Initializes the network.common.HelpingUtility
     * class and invokes its pertinent method read parameters from the input
     * file.
     *
     * @param parameterFile Name of the input configuration file
     * @see HelpingUtility
     */
    public NetworkManager(String parameterFile) {
        NetworkManager.paramFile = parameterFile;

        helpUtility = new HelpingUtility();
        helpUtility.readParameterFromFile(NetworkManager.paramFile);
        curSet = 0;
    }

    /**
     * Creates a network.Network class instance and loads the related configuration
     * parameters in the network.common.IConstants class. It also re-initializes the
     * network.common.StatisticalData class.
     *
     * @return true if a network.Network class instance is created, false otherwise.
     * @see IConstants
     * @see network.common.HelpingUtility
     * @see Network
     * @see StatisticalData
     */
    public boolean createNextNetwork() {
        helpUtility.setRandomSeed();
        Vector paramSet = helpUtility.getParamSet(curSet);
        if (null != paramSet) {
            curSet++;
            loadSet(paramSet);
            network = new Network(IConstants.CURRENT_NET);
            statData = new StatisticalData(IConstants.CURRENT_NET);
            return true;
        }
        return false;
    }

    /**
     * Initializes the current simulating network and the statistical counters.
     *
     * @see Network
     * @see StatisticalData
     */
    public void initializeNetwork() {
        if (null != network) {
            network = new Network(IConstants.CURRENT_NET);
            statData = new StatisticalData(IConstants.CURRENT_NET);
        }
    }

    /**
     * Loads the configuration parameters in the network.common.IConstants class for the
     * current simulating network.
     *
     * @param set total set of pairs of input parameter and its value
     */
    private void loadSet(Vector set) {
        int i;
        ParamDTO parDTO;
        String param, val;

        if (null != set) {
            for (i = 0; i < set.size(); i++) {
                parDTO = (ParamDTO) set.get(i);
                param = parDTO.getParam();
                val = parDTO.getVal();
                if (param.equalsIgnoreCase("WK_W"))
                    IConstants.WK_W = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("DEBUG"))
                    IConstants.DEBUG = Boolean.getBoolean(val);
                else if (param.equalsIgnoreCase("WK_ADJ_NODE"))
                    IConstants.WK_ADJ_NODE = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("WK_L"))
                    IConstants.WK_L = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("CURRENT_NET"))
                    IConstants.CURRENT_NET = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("AVG_INTER_ARRIVAL"))
                    IConstants.AVG_INTER_ARRIVAL = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("AVG_MESSAGE_LENGTH"))
                    IConstants.AVG_MESSAGE_LENGTH = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("FLIT_LENGTH"))
                    IConstants.FLIT_LENGTH = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("NUMBER_OF_IP_NODE"))
                    IConstants.NUMBER_OF_IP_NODE = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("CURRENT_ADJ_SWITCH"))
                    IConstants.CURRENT_ADJ_SWITCH = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("CURRENT_LINK_COUNT"))
                    IConstants.CURRENT_LINK_COUNT = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("CURRENT_VC_COUNT"))
                    IConstants.CURRENT_VC_COUNT = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("NUM_FLIT_PER_BUFFER"))
                    IConstants.NUM_FLIT_PER_BUFFER = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("NUM_CYCLE"))
                    IConstants.NUM_CYCLE = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("NUM_RUN"))
                    IConstants.NUM_RUN = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("TRACE"))
                    IConstants.TRACE = Boolean.valueOf(val).booleanValue();
                else if (param.equalsIgnoreCase("ASYNCHRONOUS"))
                    IConstants.ASYNCHRONOUS = Boolean.valueOf(val)
                            .booleanValue();
                else if (param.equalsIgnoreCase("TRAFFIC_TYPE"))
                    IConstants.TRAFFIC_TYPE = Integer.parseInt(val);
                else if (param.equalsIgnoreCase("WARM_UP_CYCLE"))
                    warm_up_percentage = Double.parseDouble(val);
                else if (param.equalsIgnoreCase("FIXED_MESSAGE_LENGTH"))
                    IConstants.FIXED_MESSAGE_LENGTH = Boolean.valueOf(val)
                            .booleanValue();
                else if (param.equalsIgnoreCase("LOCALITY_PROBABILITY"))
                    IConstants.OCTAL_LOCAL_TRAFFIC_PROBABILITY = Double
                            .parseDouble(val);
            }
        }

        // default value of warm_up_percentage= 0.1
        IConstants.WARM_UP_CYCLE = (int) (IConstants.NUM_CYCLE * warm_up_percentage);
    }

    /**
     * Returns a network.NetworkManager class instance, if not already defined.
     * Otherwise, returns the previously defined object.
     *
     * @param paramFile name of the input configuration file
     * @return an instance of the network.NetworkManager class
     */
    public static NetworkManager getInstance(String paramFile) {
        if (netManager == null) {
            netManager = new NetworkManager(paramFile);
        }
        return netManager;
    }

    /**
     * Returns a network.NetworkManager class instance, if not already defined.
     * Otherwise, returns the previously defined object.
     *
     * @return an instance of the network.NetworkManager class
     */
    public static NetworkManager getInstance() {
        if (netManager == null) {
            netManager = new NetworkManager(IConstants.PARAM_FILE);
        }
        return netManager;
    }

    /**
     * Returns the network variable of this class. 'network' is an instance of
     * the network.Network class.
     *
     * @return reference to the current simulating network
     */
    public static Network getNetworkInstance() {
        return network;
    }

    /**
     * Returns the helpUtility variable of this class. 'helpUtility' is an
     * instance of the network.common.HelpingUtility class.
     *
     * @return instance of the network.common.HelpingUtility class
     * @see network.common.HelpingUtility
     */
    public static HelpingUtility getHelpingUtility() {
        return helpUtility;
    }

    /**
     * Returns the statData variable of this class. 'statData' is an instance of
     * the network.common.StatisticalData class
     *
     * @return instance of the network.common.StatisticalData class
     * @see network.common.StatisticalData
     */
    public static StatisticalData getStatDataInstance() {
        if (netManager == null) {
            netManager = new NetworkManager(paramFile);
        }
        return statData;
    }

}