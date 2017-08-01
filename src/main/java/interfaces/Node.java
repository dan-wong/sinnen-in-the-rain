package interfaces;


import java.util.List;

/**
 * Created by st970 (Mike Lee) on 31/07/2017.
 */
interface Node {
    /**
     * getName() returns the name of the current node.
     * @return String
     */
    String getName();

    /**
     * getWeight() returns the weight of the current node.
     * The weight is represented in integers.
     * @return int
     */
    int getWeight();

    /**
     * addOutArc(Arc arc) adds an outward arc to the current node.
     * @param arc
     */
    void addOutArc(Arc arc);

    /**
     * addInArc(Arc arc) adds an inward arc to the current node.
     * @param arc
     */
    void addInArc(Arc arc);

    /**
     * getPredecessors( ) returns the list of predecessor nodes.
     * @return List<Node>
     */
    List<Node> getPredecessors( );

    /**
     * getSuccessors( ) returns the list of successor nodes.
     * @return List<Node>
     */
    List<Node> getSuccessors( );
}
