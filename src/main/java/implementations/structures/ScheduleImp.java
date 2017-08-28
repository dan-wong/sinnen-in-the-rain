package implementations.structures;

import interfaces.algorithm.AlgorithmNode;
import interfaces.structures.DAG;
import interfaces.structures.Node;
import interfaces.structures.Schedule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents the abstraction of a schedule (or a partial schedule)
 * A schedule contains an ordered list of nodes together with an assigned core. 
 * (This is known as AlgorithmNode).
 * 
 * Schedule also contains the starting time for each node,
 * as well as the total running time for the entire schedule. 
 * (These time are computed and set using setters.)
 * 
 * An object of this class should be returned when the time for
 * a schedule (or a partial schedule) is calculated.
 * 
 * The Schedule class is also responsible for computing the next schedule, 
 * given a new incoming node. 
 *  
 * @author Victor
 *
 */
public class ScheduleImp implements Schedule {
	private List<AlgorithmNode> _algNodes;
	//The index for this field should match the index for the list of nodes.
	private List<Integer> _startTimeForNodes;
	private int _totalTime;
	private int _numberOfCores;
	private DAG _dag = DAGImp.getInstance();

	private Map<Integer, AlgorithmNode> _lastAlgNodeOnCore;

	/**
	 * The default constructor should only be called when 
	 * the schedule is empty. (No node is in the schedule).
	 */
	public ScheduleImp(int numberOfCores) {
		_algNodes = new ArrayList<AlgorithmNode>();
		_numberOfCores = numberOfCores;
		_startTimeForNodes = new ArrayList<Integer>();

		_lastAlgNodeOnCore = new HashMap<Integer, AlgorithmNode>();
		for (int i = 0; i < _numberOfCores; i++) {
			_lastAlgNodeOnCore.put(i+1, null); //Empty schedule.
		}
	}

	/**
	 * This constructor complies with the old way of creating a scheudle
	 * (that is pass in all the algNodes generated as a "valid schedule")
	 * This is only used in old test cases, and should no longer be used in 
	 * final code.  
	 * 
	 * @param algNodes
	 * @param numberOfCores
	 */
	@Deprecated
	public ScheduleImp(List<AlgorithmNode> algNodes, int numberOfCores) {
		_algNodes = algNodes;
		_startTimeForNodes = new ArrayList<Integer>();
		_numberOfCores = numberOfCores;

		//Calculate last schedule on core.
		_lastAlgNodeOnCore = new HashMap<Integer, AlgorithmNode>();
		for (int i = 0; i < _numberOfCores; i++) {
			_lastAlgNodeOnCore.put(i+1, null); //Empty schedule.
		}

		List<Integer> list = algNodes.stream().map(AlgorithmNode::getCore).collect(Collectors.toList());
		for (int i = 0; i < _numberOfCores; i++) {
			int lastNodeIndex = list.lastIndexOf(i+1);//i+1 to match the core as cores starts from 1.
			if (lastNodeIndex != -1) {
				_lastAlgNodeOnCore.put(lastNodeIndex, algNodes.get(lastNodeIndex));
			}
		}
	}

	public int getIdleTime() {
		int idleTime = _totalTime * _numberOfCores;

		for (AlgorithmNode aNode : _algNodes) {
			idleTime -= _dag.getNodeByName(aNode.getNodeName()).getWeight();
		}

		return idleTime;
	}

	/**
	 * private constructor for copying purpose only.
	 * 
	 * @param algNodes
	 * @param numberOfCores
	 * @param lastAlgNodeOnCore
	 */
	private ScheduleImp(List<AlgorithmNode> algNodes, int numberOfCores, 
			Map<Integer, AlgorithmNode> lastAlgNodeOnCore, 
			List<Integer> startTimeForNodes, int totalTime) {
		_algNodes = algNodes;
		_startTimeForNodes = new ArrayList<Integer>();
		_numberOfCores = numberOfCores;

		//Calculate last schedule on core.
		_lastAlgNodeOnCore = new HashMap<Integer, AlgorithmNode>();
		for (int i = 0; i < _numberOfCores; i++) {
			_lastAlgNodeOnCore.put(i+1, null); //Empty schedule.
		}

		List<Integer> list = algNodes.stream().map(AlgorithmNode::getCore).collect(Collectors.toList());
		for (int i = 0; i < _numberOfCores; i++) {
			int lastNodeIndex = list.lastIndexOf(i+1);//i+1 to match the core as cores starts from 1.
			if (lastNodeIndex != -1) {
				_lastAlgNodeOnCore.put(lastNodeIndex, algNodes.get(lastNodeIndex));
			}
		}

		_lastAlgNodeOnCore = lastAlgNodeOnCore;
		_startTimeForNodes = startTimeForNodes;
		_totalTime = totalTime;
	}

	/**
	 * private helper method for clone the old Schedule, create a new Schedule object
	 * with the new node appended on the end. 
	 * 
	 * @param current
	 * @param startTime
	 * @return
	 */
	private Schedule appendNodeToSchedule(AlgorithmNode current, int startTime, int totalTime) {
		//Clone the nodes, append the current node.
		List<AlgorithmNode> algNodes = new ArrayList<AlgorithmNode>();
		algNodes.addAll(_algNodes);
		algNodes.add(current);

		//Clone the start time, append the start time
		List<Integer> startTimeForNodes = new ArrayList<Integer>();
		startTimeForNodes.addAll(_startTimeForNodes);
		startTimeForNodes.add(startTime);

		//Clone the last alg nodes on core, update with the current one. 
		Map<Integer, AlgorithmNode> lastAlgNodeOnCore = new HashMap<Integer, AlgorithmNode>();
		lastAlgNodeOnCore.putAll(_lastAlgNodeOnCore);
		lastAlgNodeOnCore.put(current.getCore(), current);

		return new ScheduleImp (algNodes, _numberOfCores, lastAlgNodeOnCore, startTimeForNodes, totalTime);
	}


	/**
	 *  {@inheritDoc}
	 */
	public Schedule getNextSchedule(AlgorithmNode currentAlgNode) {
		Schedule newSchedule;
		Node currentNode =  _dag.getNodeByName(currentAlgNode.getNodeName());

		if (this.getSizeOfSchedule() == 0) { //Empty schedule, this is the first node.
			//will start on time 0, and total time for schedule is weight of this node. 
			newSchedule = this.appendNodeToSchedule(currentAlgNode, 0, currentNode.getWeight()); 
		} else {

			//The method calculates the earliest possible start time for current node based on finish time of the core
			int endTimeForCore = getFinishTimeForCore(currentAlgNode.getCore());

			//The method calculates the earliest possible start time for current node based on predecessor
			int startTimeBasedOnPredecessor = getDependencyBasedStartTime(currentNode, currentAlgNode);
			
			//The actual start time is dependent on both endTime for core and predecessor.
			int startTime = (endTimeForCore > startTimeBasedOnPredecessor) ? endTimeForCore : startTimeBasedOnPredecessor;

			//Now need to compute the new total time. Either the old schedule time maintains
			//or if the new one on that core makes scheduler longer
			int finishTime = startTime + currentNode.getWeight();
			int newTotalTime = (_totalTime > finishTime) ? _totalTime : finishTime;

			newSchedule = this.appendNodeToSchedule(currentAlgNode, startTime, newTotalTime);
		}

		return newSchedule;
	}

	/**
	 * Private method for finding and returning the index position of the corresponding {@code AlgorithmNode} 
	 * within the given {@code List<AlgorithmNode}
	 * 
	 * @param node - {@code Node} to find the corresponding index position for
	 * @param algNodes - {@code List<AlgorithmNode>} to find the index for
	 * @return the index position of the corresponding {@code AlgorithmNode} object
	 */
	private int getIndexOfList(Node node, List<AlgorithmNode> algNodes) {
		AlgorithmNode correspondingNode = algNodes.stream().filter(n -> node.getName().equals(n.getNodeName()))
				.findFirst()
				.get();

		return algNodes.indexOf(correspondingNode);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getFinishTimeForCore (int coreNo) {
		AlgorithmNode lastNodeOnCore = this.getLastNodeOnCore(coreNo);

		//This section calculates the earliest possible start time for current node based on finish time of the core
		int endTimeForCore;
		if (lastNodeOnCore == null) { 
			endTimeForCore = 0;
		} else { //need the finish time for that core.
			//Note: index Should never be -1, schedule should have that node.
			int indexInSchedule = _algNodes.indexOf(lastNodeOnCore); 
			int startTimeForLastNode = _startTimeForNodes.get(indexInSchedule);
			int lastNodeWeight = _dag.getNodeByName(lastNodeOnCore.getNodeName()).getWeight();
			endTimeForCore = startTimeForLastNode + lastNodeWeight;
		}
		return endTimeForCore;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getDependencyBasedStartTime (Node currentNode, AlgorithmNode currentAlgNode) {
		//Check for predecessors of this currentNode and see when they've been scheduled
		//Predecessors on a different core also has an arc weight to be added on top. 
		List<Node> predecessors = currentNode.getPredecessors();
		int startTimeBasedOnPredecessor = 0;
		try {
			startTimeBasedOnPredecessor = predecessors.stream().map(pNode -> {
				int startTime = getNodeStartTime(getIndexOfList(pNode, _algNodes));
				int possibleStartTimeForCurrent = startTime + pNode.getWeight();
				if (_algNodes.get(getIndexOfList(pNode, _algNodes)).getCore() != currentAlgNode.getCore()){
					//Not on the same core, so need to add the arc weight
					possibleStartTimeForCurrent += currentNode.getInArc(pNode).getWeight();
				}
				return possibleStartTimeForCurrent;
			}).max(Comparator.naturalOrder()).get();
		} catch (Exception e) {
			//Means there are no predecessors, earliestPossibleStart time remains 0
		}
		return startTimeBasedOnPredecessor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStartTimeForNode (int startTime, int index) {
		if (index == _startTimeForNodes.size()) {
			_startTimeForNodes.add(startTime);
		} else {
			_startTimeForNodes.set(index, startTime);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public List<AlgorithmNode> getAlgorithmNodes() {
		return _algNodes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public List<Integer> getstartTimeForNodes() {
		return _startTimeForNodes;
	}

	/**
	 * The getters below should be called
	 * when processing node time information.
	 *
	 * @return
	 */
	@Override
	public int getSizeOfSchedule() {
		return _algNodes.size();
	}

	@Override
	public String getNodeName(int index) {
		return _algNodes.get(index).getNodeName();
	}

	@Override
	public int getNodeStartTime (int index) {
		return _startTimeForNodes.get(index);
	}

	@Override
	public int getNodeCore (int index) {
		return _algNodes.get(index).getCore();
	}

	@Override
	public int getTotalTime() {
		return _totalTime;
	}
	/*==========END OF GETTERS===========*/

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	@Override
	public void setTotalTime(int totalTime) {
		_totalTime = totalTime;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getTotalIdleTime() {
		int idleTimes = 0;
		for (int i = 1; i <= _numberOfCores; i++) {
			int processorIdleTime = _totalTime - getFinishTimeForCore(i);
			idleTimes += processorIdleTime;
		}
		return idleTimes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AlgorithmNode getLastNodeOnCore (int core) {
		return _lastAlgNodeOnCore.get(core);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void printSchedule() {
		System.out.print("size:" + this.getSizeOfSchedule() + "    ");
		for (int i = 0; i<_algNodes.size(); i++) {
			System.out.print(_algNodes.get(i).getNodeName() + "c" +_algNodes.get(i).getCore() + "s" + _startTimeForNodes.get(i) + " ");
		}
		System.out.println();
	}
	
	
}