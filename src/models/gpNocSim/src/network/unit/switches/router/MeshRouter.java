package network.unit.switches.router;

import network.common.IConstants;

/**
 * <p/>
 * The network.unit.switches.router.MeshRouter class is the implementation of the network.unit.switches.router.Router interface for the
 * Mesh network.Network.
 * </p>
 * <p/>
 * <p/>
 * This version contains an implementation of the deterministic XY routing
 * algorithm for the Mesh network.Network. To incorporate a different algorithm, modify
 * the determineRoute() method, or you might want to change the class
 * altogether.
 * </p>
 *
 * @version 1.0
 */

public class MeshRouter implements Router {

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
        // use different function for different routing algorithm
        return mesh8_static_route(source, dest, switchAddr);
    }

    /**
     * <p/>
     * <b>Deterministic XY routing algorithm for the Mesh network.Network.</b>
     * </p>
     * <p/>
     * This method determines the next path to be taken in the switch having
     * switchAddr by using the X-Y routing algorithm. The algorithm turns the
     * offsets (differences of x and y coordinate value between the source and
     * destination) into zero. If the both the offset is zero then destination
     * is reached. For this first the flits are transferred in one direction
     * (let Y-axis). When the offset in that direction is zero then the flits
     * are transferred in other direction (here X-axis).
     * </p>
     * <p/>
     * <p/>
     * This routing algorithm performs in the following way.
     * <ul>
     * <li>Checks whether the destination switch is received.</li>
     * <li>?If yes, then identifies the out link by (dest - (switchAddr <<
     * network.common.IConstants.MESH_NODE_BITS)) </li>
     * <li>If no, then checks whether y-offset is zero.</li>?
     * <li>?If yes, then delivers either in left or right direction if the
     * destination is on left (destination column is smaller than switch column)
     * or right (destination column is larger than switch column) of this switch
     * respectively.</li>
     * <li>If no, then delivers either in top or bottom direction if the
     * destination is on top (destination row is smaller than switch row) or
     * bottom (destination row is larger than switch row) of this switch
     * respectively.</li>
     * </ul>
     * </p>
     *
     * @param source
     * @param dest
     * @param switchAddr
     * @return output link number
     */

    private int mesh8_static_route(int source, int dest, int switchAddr) {
        int destS = -1, destRow = -1, destCol = -1;
        int switchRow = -1, switchCol = -1;

        // network.common.IConstants.meshRouteReq++;

        destS = dest >> IConstants.MESH_NODE_BITS_REQ;
        destRow = destS >> IConstants.MESH_COL_BITS;
        destCol = destS & ((1 << IConstants.MESH_COL_BITS) - 1);

        switchRow = switchAddr >> IConstants.MESH_COL_BITS;
        switchCol = switchAddr & ((1 << IConstants.MESH_COL_BITS) - 1);

        if (destS == switchAddr) {
            // System.out.println("Destination " + dest + " is in current switch
            // " + switchAddr);

            return dest - (switchAddr << IConstants.MESH_NODE_BITS_REQ);
        } else if (destRow == switchRow) {
            if (destCol < switchCol) {
                return IConstants.SWITCH_LEFT + IConstants.MESH_ADJ_NODE;
            } else if (destCol > switchCol) {
                return IConstants.SWITCH_RIGHT + IConstants.MESH_ADJ_NODE;
            }
        } else if (destRow < switchRow) {
            return IConstants.SWITCH_TOP + IConstants.MESH_ADJ_NODE;
        } else if (destRow > switchRow) {
            return IConstants.SWITCH_BOTTOM + IConstants.MESH_ADJ_NODE;
        }
        return -1;
    }

    // not implemented
    // private int mesh8_adaptive_route(int source, int dest, int switchAddr) {
    /*
      * int destS = -1, destRow = -1, destCol = -1 ; int switchRow = -1,
      * switchCol = -1 ;
      *
      * destS = dest >> network.common.IConstants.MESH_NODE_BITS ; destRow = destS >>
      * network.common.IConstants.MESH_COL_BITS ; destCol = destS & ( (1 <<
      * network.common.IConstants.MESH_COL_BITS) - 1) ;
      *
      * switchRow = switchAddr >> network.common.IConstants.MESH_COL_BITS ; switchCol =
      * switchAddr & ( (1 << network.common.IConstants.MESH_COL_BITS) - 1) ;
      *
      * if (destS == switchAddr) { //System.out.println("Destination " + dest + "
      * is in current switch " + switchAddr);
      *
      * return dest - (switchAddr << network.common.IConstants.MESH_NODE_BITS) ; } else if
      * (destRow == switchRow) { if (destCol < switchCol) { return
      * network.common.IConstants.SWITCH_LEFT + (1 << network.common.IConstants.MESH_NODE_BITS) ; } else if
      * (destCol > switchCol) { return network.common.IConstants.SWITCH_RIGHT + (1 <<
      * network.common.IConstants.MESH_NODE_BITS) ; } } else if (destRow < switchRow) { return
      * network.common.IConstants.SWITCH_TOP + (1 << network.common.IConstants.MESH_NODE_BITS) ; } else if
      * (destRow > switchRow) { return network.common.IConstants.SWITCH_BOTTOM + (1 <<
      * network.common.IConstants.MESH_NODE_BITS) ; }
      */
    // return -1;
    // }
}