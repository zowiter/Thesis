package gpApp.network.unit.switches.router;

import gpApp.network.common.IConstants;

public class OctalRouter implements Router {
    public int determineRoute(int source, int dest, int switchAddr) {
        return octal_static_route(source, dest, switchAddr);
    }

    private int octalDetermineOutputLink(int destSwitch, int ownSwitch) {
        int destRow, destCol, ownCol, ownRow, tempOwnSwitch, tempDestSwitch;
        if ((7 - destSwitch) == ownSwitch)
            return IConstants.OCTAL_MID_NODE_1;
        tempDestSwitch = (destSwitch >= 4) ? (destSwitch + 1) : destSwitch;
        tempOwnSwitch = (ownSwitch >= 4) ? (ownSwitch + 1) : ownSwitch;
        destRow = tempDestSwitch / 3;
        destCol = tempDestSwitch % 3;
        ownRow = tempOwnSwitch / 3;
        ownCol = tempOwnSwitch % 3;
        if ((Math.abs(ownRow - destRow) + Math.abs(ownCol - destCol)) > 2) {
            // return network.common.IConstants.OCTAL_MID_NODE_2;
            return IConstants.OCTAL_MID_NODE_1;
        }

        if (IConstants.OCTAL_ADJ[ownSwitch][0] == destSwitch) {
            return IConstants.RIGHT_NODE;
        } else if (IConstants.OCTAL_ADJ[IConstants.OCTAL_ADJ[ownSwitch][0]][0] == destSwitch
                || IConstants.OCTAL_ADJ[IConstants.OCTAL_ADJ[ownSwitch][0]][1] == destSwitch) {
            return IConstants.RIGHT_NODE;
        } else {
            return IConstants.LEFT_NODE;
        }
    }

    private int octal_static_route(int source, int dest, int switchAddr) {
        // System.out.print("Source: " + source + " Destination: " + dest + "
        // SwitchAddr: " + switchAddr+" Link: ");

        int destSwitchAddress, destUnitAddress, ownUnitAddress;
        int destSwitch, ownSwitch, destUnitRow, destUnitCol, ownUnitRow, ownUnitCol;
        // int node;
        int toSwitch;
        destSwitchAddress = (dest >> IConstants.OCTAL_NODE_BITS_REQ);
        if (destSwitchAddress == switchAddr) {
            // System.out.println("1: " + (dest - (switchAddr <<
            // network.common.IConstants.OCTAL_NODE_BITS_REQ)));
            return dest - (switchAddr << IConstants.OCTAL_NODE_BITS_REQ);
        } else {
            destUnitAddress = (destSwitchAddress >> 3);
            ownUnitAddress = (switchAddr >> 3);
            if (destUnitAddress == ownUnitAddress) {
                destSwitch = (destSwitchAddress & 7);
                ownSwitch = (switchAddr & 7);

                return octalDetermineOutputLink(destSwitch, ownSwitch)
                        + IConstants.OCTAL_ADJ_NODE;
            } else {
                destUnitRow = (destUnitAddress >> IConstants.OCTAL_UNIT_COL_BITS_REQ);
                destUnitCol = (destUnitAddress & ((1 << IConstants.OCTAL_UNIT_COL_BITS_REQ) - 1));

                ownUnitRow = (ownUnitAddress >> IConstants.OCTAL_UNIT_COL_BITS_REQ);
                ownUnitCol = (ownUnitAddress & ((1 << IConstants.OCTAL_UNIT_COL_BITS_REQ) - 1));

                toSwitch = 0;
                if (destUnitRow == ownUnitRow) {
                    if (destUnitCol < ownUnitCol) {
                        toSwitch = 3;
                    } else {
                        toSwitch = 4;
                    }

                } else if (destUnitCol == ownUnitCol) {
                    if (destUnitRow < ownUnitRow) {
                        toSwitch = 1;
                    } else
                        toSwitch = 6;
                } else {
                    if (destUnitRow < ownUnitRow && destUnitCol < ownUnitCol)
                        toSwitch = 0;
                    else if (destUnitRow < ownUnitRow
                            && destUnitCol > ownUnitCol)
                        toSwitch = 2;
                    else if (destUnitRow > ownUnitRow
                            && destUnitCol < ownUnitCol)
                        toSwitch = 5;
                    else if (destUnitRow > ownUnitRow
                            && destUnitCol > ownUnitCol)
                        toSwitch = 7;
                }
            }
        }
        ownSwitch = (switchAddr & 7);
        if (ownSwitch == toSwitch) {
            return (IConstants.OCTAL_MID_NODE_2 + IConstants.OCTAL_ADJ_NODE);
        } else {
            return (octalDetermineOutputLink(toSwitch, ownSwitch) + IConstants.OCTAL_ADJ_NODE);
        }

    }

}