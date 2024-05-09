package network.unit.switches.router;

import network.common.IConstants;
import network.Network;

/**
 * 
 *
 * @auther <a href="maileto:manofseven2@gmail.com">Amir Azimi</a>
 */
public class WKRouter implements Router {
    /**
     * <p/>
     * Implementation of the determineRoute() method of the network.unit.switches.router.Router interface.
     * Returns which path the flit passed be transmitted to ultimately reach the
     * destination.
     * </p>
     * <p/>
     * <p/>
     * To incorporate a different routing algorithm, make necessary
     * modifications.
     * </p>
     *
     * @param source     address of the source node
     * @param dest       address of the destination node
     * @param switchAddr address of the switch where the routing is taking place
     * @return output link number of the switch
     */
    public int determineRoute(int source, int dest, int switchAddr) {
        int[] swInts = new int[IConstants.WK_L];
        int[] destInts = new int[IConstants.WK_L];
        int level = 0;
        int destTemp = dest;
        int switchAddrTemp = switchAddr;

        while (level < IConstants.WK_L) {
            destInts[level] = destTemp & 3;
            swInts[level] = switchAddrTemp & 3;
            destTemp >>>= 2;
            switchAddrTemp >>>= 2;
            level++;
        }
        int decimal = Network.convertToDecimalAddress(switchAddr);
        int binary = Network.convertToBinaryAddress(decimal);
        if (switchAddr == dest) {
            return 0;
        } else {
            for (int i = IConstants.WK_L - 1; i >= 0; i--) {
                if (swInts[i] != destInts[i]) {
                    if (IConstants.DEBUG)
                        System.out.println(" src: " + Network.convertToDecimalAddress(source) + " dest: " + Network.convertToDecimalAddress(dest) + "  currentS: " + Network.convertToDecimalAddress(switchAddr) + "  link num: " + (destInts[i]));
                    return destInts[i] + IConstants.WK_ADJ_NODE;
                }
            }
            return -1;
        }
    }
}
