package implementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
	
	private int _bestStartTime;
	private int _bestProcessor;
	private int _currentStartTime;
	private int _currentProcessor;
	
	/**
	 * Constructor for the NodeImp to make a NodeImp object given a name and a weight
	 * @param name - Letter representing the node
	 * @param weight - Cost to execute the task in arbitrary units
	 */
	public NodeImp(String name, int weight){
		_name = name;
		_weight = weight;
		_outgoing = new ArrayList<Arc>();
		_ingoing = new ArrayList<Arc>();
	}
	
	@Override
	public String getName() {
		return _name;
	}

	@Override
	public int getWeight() {
		return _weight;
	}

	@Override
	public void addOutArc(Arc arc) {
		_outgoing.add(arc);
	}

	@Override
	public void addInArc(Arc arc) {
		_ingoing.add(arc);
	}

	@Override
	public List<Node> getPredecessors() {
		return _ingoing.stream()
				.map(a -> a.getSource())
				.collect(Collectors.toList());
	}

	@Override
	public List<Node> getSuccessors() {
		return _outgoing.stream()
				.map(a -> a.getDestination())
				.collect(Collectors.toList());
	}

	public int getBestProcessor() {
		return _bestProcessor;
	}

	public void setBestProcessor(int bestProcessor) {
		_bestProcessor = bestProcessor;
	}
	
	public int getCurrentProcessor() {
		return _currentProcessor;
	}

	public void setCurrentProcessor(int currentProcessor) {
		_currentProcessor = currentProcessor;
	}
	
	public int getBestStartTime() {
		return _bestStartTime;
	}

	public void setBestStartTime(int bestStartTime) {
		_bestStartTime = bestStartTime;
	}

	public int getCurrentStartTime() {
		return _currentStartTime;
	}

	public void setCurrentStartTime(int currentStartTime) {
		_currentStartTime = currentStartTime;
	}
}
