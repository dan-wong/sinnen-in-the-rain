package conversion;

import implementations.NodeImp;
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

    public Conversion(Input input) {
        _graphData = input.getGraphData();
    }

    public DAG getDAG() {
        HashMap<String, Node> nodes = new HashMap<>();

        for (String[] values : _graphData) {
            String name = values[0];
            int weight = Integer.valueOf(values[1]);

            String[] namesArray = name.split("\\s+");
            if (namesArray.length == 2) { //If it's an arc
                String srcName = namesArray[0];
                String destName = namesArray[1];

                Node srcNode = nodes.get(srcName);
                Node destNode = nodes.get(destName);

                Arc arc = new ArcImpl(weight, srcNode, destNode);
            } else { //Else it's a node
                Node node = new NodeImp(name, weight);
                nodes.put(name, node);
            }
        }
    }
}
