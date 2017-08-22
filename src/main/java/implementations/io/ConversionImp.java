package implementations.io;

import implementations.structures.ArcImpl;
import implementations.structures.DAGImp;
import implementations.structures.NodeImp;
import interfaces.io.Conversion;
import interfaces.io.Input;
import interfaces.structures.Arc;
import interfaces.structures.DAG;
import interfaces.structures.Node;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class converts the raw input data into a DAG object
 *
 * @author Daniel
 */
public class ConversionImp implements Conversion {
    private List<String[]> _graphData;

    /**
     * Constructor for ConversionImp module.
     * @param input - Input class
     */
    public ConversionImp(Input input) {
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
        dag.addStartNodes(getRootNodes(nodes)); //Add the root nodes to the DAG
        nodes.values().forEach(dag::add);

        return dag;
    }

	/**
	 * This method returns the root node in a graph. This is done by finding all
	 * nodes which do not have any incoming arcs.
	 *
	 * @param nodes - Hashmap of <Node name, Node object>
	 * @return List of nodes which are the root node in the graph
	 */
	private List<Node> getRootNodes(HashMap<String, Node> nodes) {
    	return nodes.values().stream()
				.filter(n -> n.getPredecessors().size() == 0)
				.collect(Collectors.toList());
	}
}
