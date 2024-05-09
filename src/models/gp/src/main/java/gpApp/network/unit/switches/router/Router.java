package gpApp.network.unit.switches.router;

/**
 * The network.unit.switches.router.Router interface provides the declaration of the method to find out the
 * possible output link of a switch to reach the destination node.
 *
 * @version 1.0
 */

public interface Router {
    /**
     * Returns the physical link number to transfer a flit from an input buffer
     * to the output buffer on that link to reach the destination node.
     *
     * @param src  address of the source node
     * @param dest address of the destination node
     * @param addr address of the switch
     * @return link number to reach the destination node
     */
    public abstract int determineRoute(int src, int dest, int addr);
}