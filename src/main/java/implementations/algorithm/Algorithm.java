package implementations.algorithm;

import implementations.SchedulerTime;
import interfaces.DAG;
import interfaces.Node;

import java.util.*;

/**
 * This class implements the algorithm to solve the scheduling problem
 */
public class Algorithm {
	private DAG _dag;
	private int _numberOfCores;
	private List<List<AlgorithmNode>> _generatedSchedules; //This holds all the generated schedules

	public Algorithm(DAG dag, int numberOfCores) {
		_dag = dag;
		_numberOfCores = numberOfCores;
		_generatedSchedules = new ArrayList<>();

		recursiveScheduleGeneration(new ArrayList<>(), AlgorithmNode.convertNodetoAlgorithimNode(_dag.getAllNodes()));
	}

	/**
	 * This method recursively generates all possible schedules given a list of nodes.
	 *
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 */
	private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes) {
		//Base Case
		if (remainingNodes.size() == 0) {
			_generatedSchedules.add(processed);
		} else {
			for (int i = 0; i < remainingNodes.size(); i++) {
				for (int j = 0; j < _numberOfCores; j++) {
					List<AlgorithmNode> newProcessed = new ArrayList<>(processed);
					AlgorithmNode node = remainingNodes.get(i).createClone();
					node.setCore(j);
					newProcessed.add(node);

					List<AlgorithmNode> newRemaining = new ArrayList<>(remainingNodes);
					newRemaining.remove(i);

					recursiveScheduleGeneration(newProcessed, newRemaining);
				}
			}
		}
	}

	//For testing
	public List<List<AlgorithmNode>> getSchedules() {
		return _generatedSchedules;
	}

	/**
	 * Calculates the time cost of executing the given schedule, returning a complete SchedulerTime object.
	 * @param algNodes - A {@code List<AlgorithmNode>} given in the order of execution
	 * @return - SchedulerTime object with cost and execution time information
	 */
	public SchedulerTime calculateTotalTime(List<AlgorithmNode> algNodes) {
		//making a copy that I can manipulate
		//this copy is going work as a list of nodes that still need their start time calculated
		List<AlgorithmNode> remainingAlgNodes = new ArrayList<>(algNodes);
		//creating a corresponding array of Nodes
		//make sure to remove the corresponding Node object when you remove a AlgNode object
		List<Node> remainingNodes = new ArrayList<>();

		//populating new nodes array with corresponding Node objects
		for (AlgorithmNode algNode : remainingAlgNodes) {
			remainingNodes.add(_dag.getNodeByName(algNode.getNodeName()));
		}

		//creating ArrayLists to represent the schedule for each core
		//NOTE: could change the coreSchedule to just an ArrayList that holds the most recently scheduled node for each core
		ArrayList<ArrayList<AlgorithmNode>> coreSchedules = new ArrayList<>();
		for (int i = 0; i < _numberOfCores; i++) {
			coreSchedules.add(new ArrayList<>());
		}

		//creating a SchedulerTime object for holding the schedule start times for each node
		SchedulerTime st = new SchedulerTime(algNodes);

		while (!remainingAlgNodes.isEmpty()) {
			//maintained list of AlgNodes and Nodes which need to be removed after each finished for loop
			List<AlgorithmNode> algNodesToRemove = new ArrayList<>();
			List<Node> nodesToRemove = new ArrayList<>();

			for (AlgorithmNode currentAlgNode : remainingAlgNodes) {
				Node currentNode = remainingNodes.get(remainingAlgNodes.indexOf(currentAlgNode));
				int highestCost = 0;

				//calculate the highest time delay caused by dependencies
				for (Node node : currentNode.getPredecessors()) {
					//get index position of corresponding AlgNode object, to check the start time in SchedulerTime
					int nodeIndex = 0;
					for (AlgorithmNode anotherAlgNode : algNodes) {
						if (anotherAlgNode.getNodeName().equals(node.getName())) {
							nodeIndex = algNodes.indexOf(anotherAlgNode);
							break;
						}
					}

					//calculating the maximum delay caused by this particular dependent node
					int cost = st.getNodeStartTime(nodeIndex) + node.getWeight();
					if (!(algNodes.get(nodeIndex).getCore() == currentAlgNode.getCore())) {
						//add on arc weight, since they're on different cores
						cost += currentNode.getInArc(node).getWeight();
					}

					if (cost > highestCost) {
						highestCost = cost;
					}
				}

				//calculate the highest time delay caused by previous processes on the same core
				int coreNum = currentAlgNode.getCore();
				ArrayList<AlgorithmNode> currentCore = coreSchedules.get(coreNum);
				currentCore.add(currentAlgNode);

				//check when previous process on given core is finished, if there is one
				if (currentCore.size() > 1) {
					Node previousNode = _dag.getNodeByName(currentCore.get(currentCore.size() - 2).getNodeName());
					int cost = previousNode.getWeight() + getStartTimeFromSchedulerTime(previousNode.getName(), algNodes, st);
					if (cost > highestCost) {
						highestCost = cost;
					}
				}

				//set SchedulerTime startTime for this node
				st.setStartTimeForNode(highestCost, algNodes.indexOf(currentAlgNode));
				algNodesToRemove.add(currentAlgNode);
				nodesToRemove.add(currentNode);
			}

			//have to remove the nodes which have successfully had their start times calculated
			//make sure to remove from both lists
			remainingAlgNodes.removeAll(algNodesToRemove);
			remainingNodes.removeAll(nodesToRemove);
		}

		int totalTime = 0;
		for (int i = 0; i < _numberOfCores; i++) {
			ArrayList<AlgorithmNode> currentCore = coreSchedules.get(i);
			AlgorithmNode algNode = currentCore.get(currentCore.size() - 1);
			int timeTaken = st.getNodeStartTime(algNodes.indexOf(algNode)) + _dag.getNodeByName(algNode.getNodeName()).getWeight();

			if (timeTaken > totalTime) {
				totalTime = timeTaken;
			}
		}

		st.setTotalTime(totalTime);

		return st;
	}

	/**
	 * Finds the corresponding index for accessing/setting the right startTime in a SchedulerTime object.
	 * @param nodeName - name of the {@code Node} or {@code AlgorithmNode} to find the index for
	 * @param algNodes - {@code List<AlgorithmNode>} that was originally passed to create the SchedulerTime object
	 * @param st - {@code SchedulerTime} object to find the index within
	 * @return - startTime of the given {@code Node/AlgorithmNode}
	 */
	private int getStartTimeFromSchedulerTime(String nodeName, List<AlgorithmNode> algNodes, SchedulerTime st) {
		int index = 0;
		for (AlgorithmNode algNode : algNodes) {
			if (nodeName.equals(algNode.getNodeName())) {
				index = algNodes.indexOf(algNode);
				break;
			}
		}

		return st.getNodeStartTime(index);
	}
}
