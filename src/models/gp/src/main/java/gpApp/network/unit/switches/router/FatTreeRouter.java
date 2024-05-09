package gpApp.network.unit.switches.router;

import gpApp.network.common.IConstants;
import gpApp.network.NetworkManager;

/**
 * <p/>
 * The network.unit.switches.router.FatTreeRouter class is the implementation of the network.unit.switches.router.Router interface for the
 * Butterfly Fat Tree network.Network.
 * </p>
 *
 * @version 1.0
 */

public class FatTreeRouter implements Router {
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
        return fat_tree_static__route(dest, switchAddr);
    }

    /**
     * <p/>
     * This method determines the next path to be taken in the switch
     * considering range of IP node covered by this switch.
     * </p>
     * <p/>
     * <p/>
     * <ul>
     * This routing algorithm performs in the following way.
     * <li>?Calculates the level of the switch by <i>sLevel = switchAddr >>
     * network.common.IConstants.FAT_NUM_INDEX_BIT</i></li>
     * <li>?Calculates index of the switch in this level by <i>sIndex =
     * switchAddr & ( (1 << network.common.IConstants.FAT_NUM_INDEX_BIT) - 1) </i></li>
     * <li>Calculates the range of IP node covered by (descendent of this
     * switch) this switch by <i>lowRange = sIndex / (1 << (sLevel - 1)) * (1 <<
     * (2 ^ sLevel)) </i> and <i>highRange = lowRange + (1 << (2 ^ sLevel)) - 1
     * </i> </li>
     * <li>?If the destination is not in the cover range then chooses any one
     * of the parent physical links randomly.</li>
     * <li>If the destination is in the cover range then divides the range into
     * four equal sub range corresponding to four child physical link and
     * chooses the link whose cover range includes the destination IP node.</li>
     * </ul>
     * </p>
     *
     * @param dest       address of the destination node
     * @param switchAddr address of the switch where the routing is taking place
     * @return output link number of the switch
     */
    public int fat_tree_static__route(int dest, int switchAddr) {
        int noOfIP, part, count, lowRange, highRange, sLevel, sIndex;

        sLevel = switchAddr >> IConstants.FAT_NUM_INDEX_BIT;
        sIndex = switchAddr & ((1 << IConstants.FAT_NUM_INDEX_BIT) - 1);
        lowRange = sIndex / (1 << (sLevel - 1)) * (1 << (2 * sLevel));
        highRange = lowRange + (1 << (2 * sLevel)) - 1;

        if (dest >= lowRange && dest <= highRange) {
            noOfIP = highRange - lowRange + 1;
            part = noOfIP / IConstants.FAT_NUM_ADJ_CHILD;
            count = 1;
            while (dest >= (lowRange + count * part)) {
                count++;
            }
            return count - 1;
        } else {
            return (int) (NetworkManager.getHelpingUtility()
                    .getNextRandomNumber() * IConstants.FAT_NUM_ADJ_PARENT)
                    + IConstants.FAT_NUM_ADJ_CHILD;
        }
    }

}