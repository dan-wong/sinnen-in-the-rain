package implementations;

import java.util.ArrayList;
import java.util.List;

import interfaces.Arc;
import interfaces.Node;

/**
 * This class is responsible for representing a node in a DAG.
 * It stores the node's name, weight, ingoing and outgoing arcs to other nodes.
 * 
 * This class implements the Node interface.
 * @author Darius
 *
 */
public class NodeImp implements Node {

	//Node attributes
	private String _name;
	private int _weight;
	private List<Arc> _outgoing;
	private List<Arc> _ingoing;
	
	int bestStartTime;
	int bestProcessor;
	int currentStartTime;
	int currentProcessor;
	
	/**
	 * Constructor for the NodeImp to make a NodeImp object given a name and a weight
	 * @param name - Letter representing the node
	 * @param weight - Cost to execute the task in arbitrary units
	 */
	public NodeImp(String name, int weight){
		_name = name;
		_weight = weight;
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return _name;
	}

	@Override
	public int getWeight() {
		// TODO Auto-generated method stub
		return _weight;
	}

	@Override
	public void addOutArc(Arc arc) {
		// TODO Auto-generated method stub
		_outgoing.add(arc);
	}

	@Override
	public void addInArc(Arc arc) {
		// TODO Auto-generated method stub
		_ingoing.add(arc);
	}

	@Override
	public List<Node> getPredecessors() {
		// TODO Auto-generated method stub
		List<Node> predecessors = new ArrayList<Node>();
		for (Arc arc : _ingoing){
			predecessors.add(arc.getSource());
		}
		return predecessors;
	}

	@Override
	public List<Node> getSuccessors() {
		// TODO Auto-generated method stub
		List<Node> successors = new ArrayList<Node>();
		for (Arc arc : _outgoing){
			successors.add(arc.getDestination());
		}
		return successors;
	}

}
