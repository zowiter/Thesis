package gpApp.network.unit.switches.router;

import gpApp.network.common.IConstants;

/**
 * <p/>
 * The network.unit.switches.router.TorusRouter class is the implementation of the network.unit.switches.router.Router interface for the
 * Torus network.Network.
 * </p>
 * <p/>
 * <p/>
 * This version contains an implementation of the deterministic routing
 * algorithm for the Torus network.Network. To incorporate a different algorithm, modify
 * the determineRoute() method, or you might want to change the class
 * altogether.
 * </p>
 *
 * @version 1.0
 */
public class TorusRouter implements Router {

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
        return torus8_static_route(source, dest, switchAddr);
    }

    /**
     * <p/>
     * This method also determines the next path to be taken in the switch
     * having switchAddr by using the X-Y routing algorithm. The algorithm turns
     * the offsets (differences of x and y coordinate value between the source
     * and destination) into zero but with some difference than that of Mesh
     * network. Here the switches are round-way connected along with connection
     * with adjacent switches. So during routing this round-way connection
     * considered for optimal path. Like Mesh, here first the flits are
     * transferred in one direction (let Y-axis). When the offset in that
     * direction is zero then the flits are transferred in other direction (here
     * X-axis).
     * </p>
     * <p/>
     * <p/>
     * This routing algorithm performs in the following way.
     * <ul>
     * <li>Checks whether the destination switch is received.</li>
     * <li>If yes, then identifies the out link by (dest - (switchAddr <<
     * network.common.IConstants.MESH_NODE_BITS)) </li>
     * <li>If no, then checks whether y-offset is zero.</li>
     * <li>If yes, then delivers either through left or right direction by
     * determining which direction reaches the destination fast considering
     * round�way connection.</li>
     * <li>If no, then delivers either through top or bottom direction by
     * determining which direction reaches the destination fast considering
     * round�way connection.</li>
     * </ul>
     * </p>
     *
     * @param source     address of the source node
     * @param dest       address of the destination node
     * @param switchAddr address of the switch where the routing is taking place
     * @return output link number of the switch
     * @see MeshRouter
     */
    private int torus8_static_route(int source, int dest, int switchAddr) {
        int destS = -1, destRow = -1, destCol = -1;
        int switchRow = -1, switchCol = -1;
        float colCheck = (float) IConstants.MESH_COL / 2;
        float rowCheck = (float) IConstants.MESH_ROW / 2;

        // network.common.IConstants.torusRouteReq++;

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
            if ((destCol < switchCol) && ((switchCol - destCol) >= colCheck)) {
                return IConstants.SWITCH_RIGHT + IConstants.MESH_ADJ_NODE;
            } else if ((destCol < switchCol)
                    && ((switchCol - destCol) < colCheck)) {
                return IConstants.SWITCH_LEFT + IConstants.MESH_ADJ_NODE;
            } else if ((destCol > switchCol)
                    && ((destCol - switchCol) >= colCheck)) {
                return IConstants.SWITCH_LEFT + IConstants.MESH_ADJ_NODE;
            } else if ((destCol > switchCol)
                    && ((destCol - switchCol) < colCheck)) {
                return IConstants.SWITCH_RIGHT + IConstants.MESH_ADJ_NODE;
            }

        } else if ((destRow < switchRow) && ((switchRow - destRow) >= rowCheck)) {
            return IConstants.SWITCH_BOTTOM + IConstants.MESH_ADJ_NODE;
        } else if ((destRow < switchRow) && ((switchRow - destRow) < rowCheck)) {
            return IConstants.SWITCH_TOP + IConstants.MESH_ADJ_NODE;
        } else if ((destRow > switchRow) && ((destRow - switchRow) >= rowCheck)) {
            return IConstants.SWITCH_TOP + IConstants.MESH_ADJ_NODE;
        } else if ((destRow > switchRow) && ((destRow - switchRow) < rowCheck)) {
            return IConstants.SWITCH_BOTTOM + IConstants.MESH_ADJ_NODE;
        }

        return -1;
    }

}