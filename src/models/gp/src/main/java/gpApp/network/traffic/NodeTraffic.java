package gpApp.network.traffic;

import java.util.Vector;

/**
 * The network.traffic.NodeTraffic abstract class defines the necessary methods for setting up
 * traffic configuration for the generated messages.
 *
 * @version 1.0
 */

public abstract class NodeTraffic {
    /**
     * Address of the node
     */
    protected int address;

    /**
     * It is an int type variable. It holds the next timestamp when the node
     * will generate message. The timestamp is calculated by exponential
     * distribution depending on the average message generation time.
     */
    protected int nextMsgGenTime;

    /**
     * Constructor of the network.traffic.NodeTraffic class
     *
     * @param nodeAddress address of the node
     */
    public NodeTraffic(int nodeAddress) {
        address = nodeAddress;
    }

    /**
     * <p/>
     * Generates a packet including its header flit and all of the data flits
     * and stores into a Vector data structure. Also sets the next message
     * generation time for the node.
     * </p>
     *
     * @param curCycle        simulation cycle
     * @param curMessageCount number of messages stored in the node's internal buffer
     * @return a Vector consisting of all flits in the packet
     */
    abstract public Vector generateMessage(int curCycle, int curMessageCount);

    /**
     * Updates the next message generation time (nextMessageGenTime)
     *
     * @param curCycle simulation cycle
     */
    abstract public void setNextMsgGenTime(int curCycle);

    /**
     * Returns the next message generation time for the node.
     *
     * @return next message generation time
     */
    abstract public int getNextMsgGenTime();

    /**
     * Determines a destination for the generated message.
     *
     * @return address of the destination
     */
    abstract protected int getDestination();

    /**
     * Determines the number of flits in the the generated message.
     *
     * @return number of flits in the message
     */
    abstract protected int getMessageSize();

    /**
     * Generates an encoded Header flit suitable for the NoC architecture
     *
     * @param destination address of the destination node
     * @param noOfFlit    number of flits in the message
     * @param vcId        virtual channel number
     * @param curCycle    simualtion cycle
     * @return Header flit
     */
    abstract protected Flit createHeaderFlit(int destination, int noOfFlit,
                                             int vcId, int curCycle);

    /**
     * Generates an encoded Data flit suitable for the NoC architecture.
     *
     * @param destination address of the destination node
     * @param vcId        virtual channel number
     * @param curCycle    simualatin cycle
     * @return Data flit
     */
    abstract protected Flit createDataFlit(int destination, int vcId,
                                           int curCycle);
}
