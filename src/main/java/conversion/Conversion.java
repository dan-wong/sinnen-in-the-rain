package conversion;

import implementations.ArcImpl;
import implementations.NodeImp;
import implementations.DAGImp;
import interfaces.Arc;
import interfaces.DAG;
import interfaces.Input;
import interfaces.Node;

import java.util.HashMap;
import java.util.List;

/**
 * This class converts the raw input data into a DAG object
 *
 * @author Daniel
 */
public class Conversion {
    private List<String[]> _graphData;

    /**
     * Constructor for Conversion module.
     * @param input - Input class
     */
    public Conversion(Input input) {
        _graphData = input.getGraphData();
    }

    /**
     * Generate and return the DAG from the input data.
     * @return DAG - Graph generated
     */
    public DAG getDAG() {
        HashMap<String, Node> nodes = new HashMap<>();

        for (String[] values : _graphData) {
            String name = values[0];
            int weight = Integer.valueOf(values[1]);

            String[] namesArray = name.split("\\s+");
            if (namesArray.length == 2) { //If it's an arc
                Node srcNode = nodes.get(namesArray[0]);
                Node destNode = nodes.get(namesArray[1]);

                Arc arc = new ArcImpl(weight, srcNode, destNode);

                srcNode.addOutArc(arc);
                destNode.addInArc(arc);
            } else { //Else it's a node
                Node node = new NodeImp(name, weight);
                nodes.put(name, node);
            }
        }

        //Add to the DAG object all the nodes
        DAG dag = new DAGImp();
        nodes.values().forEach(dag::add);

        return dag;
    }
}
