package network.traffic;

import network.common.IConstants;

/**
 * A message (a packet) is the summation of some flits. network.traffic.Flit object simply
 * contains encoded data as member variable. The data is divided into different
 * fields. Both header and data flit contain flit type (1 bit) and virtual
 * channel number used. For data flit the remaining bits of the data block is
 * simply raw data taken from the message (packet). For header flit contains
 * number bit used to represent the source and destination node, number of
 * flits, source address and destination address sequentially.
 *
 * @version 1.0
 */

public class Flit {
    /**
     * Contains the array of 32 bit data.
     */
    private int[] data;

    /**
     * The time stamp (simulation cycle) when the flit has been generated.
     */
    private int genTimeStamp;

    /**
     * The time stamp (simulation cycle) when the flit has been last served.
     */
    private int lastServiceTimeStamp;

    /**
     * The address of the source node.
     */
    private int src;

    /**
     * The address of the destination node.
     */
    private int dest;

    /**
     * The number of hops the flit has traversed
     */
    private int hop;

    public Flit(int[] data, int genTimeStamp) {
        this.data = new int[data.length];
        this.genTimeStamp = genTimeStamp;
        this.lastServiceTimeStamp = genTimeStamp;
        this.hop = 0;
        for (int i = 0; i < data.length; i++) {
            this.data[i] = data[i];
        }
    }

    /**
     * Increases the hop count of the flit, after it traverses a physical link.
     */
    public void increaseHop() {
        hop++;
    }

    /**
     * Returns the hop count that the flit has traversed
     *
     * @return the hop count
     */
    public int getHopCount() {
        return hop;
    }

    /**
     * Returns the type of the flit.
     *
     * @return one bit information to distinguish HEADER or DATA flit
     */
    public int getType() {
        return this.data[0] & ((1 << IConstants.NUM_FLIT_TYPE_BITS) - 1);
    }

    /**
     * Returns the virtual channel information of the flit
     *
     * @return virtual channel number
     */
    public int getVirtualChannelNo() {
        if (null == data) {
            return -1;
        }
        return (data[0] >> IConstants.NUM_FLIT_TYPE_BITS)
                & ((1 << IConstants.NUM_VCID_BITS) - 1);
    }

    /**
     * Returns the number of bits required to encode an address in the flit.
     *
     * @return number of bits to encode an address
     */
    public int getAddressLength() {
        int temp;
        int noOfBit = IConstants.NUM_FLIT_TYPE_BITS + IConstants.NUM_VCID_BITS;
        int noOfInt = noOfBit / IConstants.INT_SIZE;
        int rest = IConstants.INT_SIZE - (noOfBit % IConstants.INT_SIZE);

        if (rest >= IConstants.NUM_ADDR_BITS) {
            temp = (data[noOfInt] >>> (IConstants.INT_SIZE - rest))
                    & ((1 << IConstants.NUM_ADDR_BITS) - 1);
        } else {
            temp = (data[noOfInt] >>> (IConstants.INT_SIZE - rest))
                    & ((1 << rest) - 1);
            temp = ((data[noOfInt + 1] & ((1 << (IConstants.NUM_ADDR_BITS - rest)) - 1)) << rest)
                    | temp;
        }

        return temp;
    }

    /**
     * Returns the number of flits in the packet to which this flit belongs.
     *
     * @return packet length in flits
     */
    public int getPacketLength() {
        int temp;
        int noOfBit = IConstants.NUM_FLIT_TYPE_BITS + IConstants.NUM_VCID_BITS
                + IConstants.NUM_ADDR_BITS;
        int noOfInt = noOfBit / IConstants.INT_SIZE;
        int rest = IConstants.INT_SIZE - (noOfBit % IConstants.INT_SIZE);

        if (rest >= IConstants.NUM_FLITS_BITS) {
            temp = (data[noOfInt] >>> (IConstants.INT_SIZE - rest))
                    & ((1 << IConstants.NUM_FLITS_BITS) - 1);
        } else {
            temp = (data[noOfInt] >>> (IConstants.INT_SIZE - rest))
                    & ((1 << rest) - 1);
            temp = ((data[noOfInt + 1] & ((1 << (IConstants.NUM_FLITS_BITS - rest)) - 1)) << rest)
                    | temp;
        }

        return temp;

    }

    /**
     * Returns the address of the source node. While doing so, it decodes the
     * flit data to retrieve the sought information. As the address might
     * overlap in two adjacent 32 bit data, it takes care of such situation.
     *
     * @return address of the source node.
     */
    public int getSourceNode() {
        int temp;
        int addrSize = getAddressLength();
        int noOfBit = IConstants.NUM_FLIT_TYPE_BITS + IConstants.NUM_VCID_BITS
                + IConstants.NUM_ADDR_BITS + IConstants.NUM_FLITS_BITS;
        int noOfInt = noOfBit / IConstants.INT_SIZE;
        int rest = IConstants.INT_SIZE - (noOfBit % IConstants.INT_SIZE);

        if (rest >= addrSize) {
            temp = (data[noOfInt] >>> (IConstants.INT_SIZE - rest))
                    & ((1 << addrSize) - 1);
        } else {
            temp = (data[noOfInt] >>> (IConstants.INT_SIZE - rest))
                    & ((1 << rest) - 1);
            temp = ((data[noOfInt + 1] & ((1 << (addrSize - rest)) - 1)) << rest)
                    | temp;
        }

        if (IConstants.CURRENT_NET == IConstants.NET_WK) {
            return getSource();
        }
        return temp;

    }

    /**
     * Returns the address of the destination node. While doing so, it decodes
     * the flit data to retrieve the sought information. As the address might
     * overlap in two adjacent 32 bit data, it takes care of such situation.
     *
     * @return address of the destination node.
     */
    public int getDestinationNode() {

        int temp;
        int addrSize = getAddressLength();
        int noOfBit = IConstants.NUM_FLIT_TYPE_BITS + IConstants.NUM_VCID_BITS
                + IConstants.NUM_ADDR_BITS + IConstants.NUM_FLITS_BITS
                + addrSize;
        int noOfInt = noOfBit / IConstants.INT_SIZE;
        int rest = IConstants.INT_SIZE - (noOfBit % IConstants.INT_SIZE);

        if (rest >= addrSize) {
            temp = (data[noOfInt] >>> (IConstants.INT_SIZE - rest))
                    & ((1 << addrSize) - 1);
        } else {
            temp = (data[noOfInt] >>> (IConstants.INT_SIZE - rest))
                    & ((1 << rest) - 1);
            temp = ((data[noOfInt + 1] & ((1 << (addrSize - rest)) - 1)) << rest)
                    | temp;
        }
        if (IConstants.CURRENT_NET == IConstants.NET_WK) {
            return getDest();
        }
        return temp;
    }

    /**
     * Returns the data of the flit.
     *
     * @return an intege array of data of the flit
     */
    public int[] getData() {
        int[] temp = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            temp[i] = data[i];
        }
        temp[0] >>>= (IConstants.NUM_FLIT_TYPE_BITS + IConstants.NUM_VCID_BITS);
        return temp;
    }

    /**
     * Returns the Hex value of the flit data.
     */
    public String toString() {
        String temp = "";
        for (int i = 0; i < data.length; i++) {
            temp += Integer.toHexString(data[i]);
        }
        return temp;
    }

    /**
     * Sets the virtual channel information of the flit data
     *
     * @param vcId virtual channel number
     */
    public void setVirtualChannelNo(int vcId) {
        int mask = (1 << IConstants.NUM_VCID_BITS) - 1;
        mask <<= IConstants.NUM_FLIT_TYPE_BITS;
        mask = ~mask;
        data[0] &= mask;
        data[0] |= (vcId << IConstants.NUM_FLIT_TYPE_BITS);
    }

    /**
     * Sets the last service time information by the specified time stamp
     *
     * @param timeStamp the time stamp (simulation cycle) when the flit receives some
     *                  service
     */
    public void setLastServiceTimeStamp(int timeStamp) {
        this.lastServiceTimeStamp = timeStamp;
    }

    /**
     * Returns the last service time when the flit was served
     *
     * @return the last service time information
     */
    public int getLastServiceTimeStamp() {
        return this.lastServiceTimeStamp;
    }

    /**
     * Sets the generation time stamp
     *
     * @param timeStamp the time information when the flit is generated
     */
    public void setGenTimeStamp(int timeStamp) {
        this.genTimeStamp = timeStamp;
    }

    /**
     * Returns the generation time stamp of the flit
     *
     * @return generation time of the flit.
     */

    public int getGenTimeStamp() {
        return this.genTimeStamp;
    }

    /**
     * Return the source address of the flit.
     *
     * @return address of the source node
     */
    public int getSource() {
        return src;
    }

    /**
     * Return the destination address of the flit.
     *
     * @return address of the destination node
     */
    public int getDest() {
        return dest;
    }

    /**
     * Sets the source address of the flit.
     *
     * @param src address of the source node
     */
    public void setSource(int src) {
        this.src = src;
    }

    /**
     * Sets the destination address of the flit
     *
     * @param dest address of the destination node
     */
    public void setDest(int dest) {
        this.dest = dest;
    }

}