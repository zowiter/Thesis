package gpApp.network.unit.switches.router;

import gpApp.network.common.IConstants;
import gpApp.network.NetworkManager;

public class ExFatTreeRouter implements Router {
    public int determineRoute(int src, int dest, int sAddr) {
        return extended_fat_tree_static__route(dest, sAddr);
    }

    public int extended_fat_tree_static__route(int dest, int sAddr) {
        int noOfIP, part, count, ownRangeStart, leftRangeStart, rightRangeStart, sLevel, sIndex, coverRange;
        int leftIndex, rightIndex;

        sLevel = sAddr >> IConstants.FAT_NUM_INDEX_BIT;
        sIndex = sAddr & ((1 << IConstants.FAT_NUM_INDEX_BIT) - 1);
        coverRange = 1 << (2 * sLevel);
        ownRangeStart = (sIndex / (1 << (sLevel - 1))) * (1 << (2 * sLevel));

        leftIndex = (sIndex / (1 << (sLevel + 1))) * (1 << (sLevel + 1))
                + (sIndex + 3 * (1 << (sLevel - 1))) % (1 << (sLevel + 1));
        leftRangeStart = (leftIndex / (1 << (sLevel - 1)))
                * (1 << (2 * sLevel));

        rightIndex = (sIndex / (1 << (sLevel + 1))) * (1 << (sLevel + 1))
                + (sIndex + (1 << (sLevel - 1))) % (1 << (sLevel + 1));
        rightRangeStart = (rightIndex / (1 << (sLevel - 1)))
                * (1 << (2 * sLevel));

        if (dest >= ownRangeStart && dest < (ownRangeStart + coverRange)) {
            // route the correct child
            part = coverRange / IConstants.FAT_NUM_ADJ_CHILD;
            count = 1;
            while (dest > (ownRangeStart + count * part - 1)) {
                count++;
            }
            return count - 1;
        } else if (dest >= leftRangeStart
                && dest < (leftRangeStart + coverRange)) {
            // route left
            return IConstants.FAT_NUM_ADJ_PARENT + IConstants.FAT_NUM_ADJ_CHILD;
        } else if (dest >= rightRangeStart
                && dest < (rightRangeStart + coverRange)) {
            // route right
            return IConstants.FAT_NUM_ADJ_PARENT + IConstants.FAT_NUM_ADJ_CHILD
                    + 1;
        } else {
            // route any one of the parent randomly
            return (int) (NetworkManager.getHelpingUtility()
                    .getNextRandomNumber() * IConstants.FAT_NUM_ADJ_PARENT)
                    + IConstants.FAT_NUM_ADJ_CHILD;
        }
    }

}