package implementations.algorithm;

import implementations.structures.NodeScheduleImp;
import implementations.structures.ScheduleImp;
import interfaces.algorithm.Algorithm;
import interfaces.algorithm.AlgorithmNode;
import interfaces.structures.DAG;
import interfaces.structures.Node;
import interfaces.structures.NodeSchedule;
import visualisation.ComboView;
import visualisation.TableModel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements the algorithm to solve the scheduling problem
 */
public class AlgorithmImp implements Algorithm {
	private DAG _dag;
	private int _numberOfCores;
	private HashMap<String, NodeSchedule> _currentBestSchedule;
	private int _recursiveCalls = 0; //For benchmarking purposes only
	private TableModel _model;
	private int _bestTime = Integer.MAX_VALUE;

	public AlgorithmImp(DAG dag, int numberOfCores) {
		_dag = dag;
		_numberOfCores = numberOfCores;
		_currentBestSchedule = new HashMap<>();
		
		_model = TableModel.getInstance();
		_model.initModel(_currentBestSchedule, _dag, _numberOfCores);
		ComboView schedule = new ComboView(_model,_dag,_numberOfCores);
		
		recursiveScheduleGeneration(new ArrayList<>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()));
	}

	/**
	 * Purely for benchmarking purposes
	 *
	 * @return number of times the recursive method was called
	 */
	public int getRecursiveCalls() {
		return _recursiveCalls;
	}

	/**
	 * This method recursively generates all possible schedules given a list of nodes.
	 *
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 */
	private void recursiveScheduleGeneration(List<AlgorithmNodeImp> processed, List<AlgorithmNodeImp> remainingNodes) {
		_recursiveCalls++;

		//Base Case
		if (remainingNodes.size() == 0) {
			ScheduleImp st = calculateTotalTime(processed);
			if (st.getTotalTime() < _bestTime) { //Found a new best schedule
				setNewBestSchedule(st);
				_bestTime = st.getTotalTime();
				// update view, now that a new schedule is available. This is too fast for small schedules
				
				// slowing down (Temporary) to visualise. Will be done using a form of timer in the future.
				/*try {
					Thread.sleep(1000);
				} catch (InterruptedException e){
					e.printStackTrace();
				}*/
				
				
				_model.changeData(_currentBestSchedule, _bestTime);
			}
		} else {
			for (int i = 0; i < remainingNodes.size(); i++) {
				for (int j = 1; j <= _numberOfCores; j++) {
					List<AlgorithmNodeImp> newProcessed = new ArrayList<>(processed);
					AlgorithmNodeImp node = remainingNodes.get(i).createClone();
					node.setCore(j);
					newProcessed.add(node);

					if (checkValidSchedule(newProcessed)) {
						ScheduleImp st = calculateTotalTime(newProcessed);

						//If current >= best time, bound by moving to the next processor.
						if (st.getTotalTime() >= _bestTime) {
							continue;
						}
					} else { //Schedule is invalid, then pruning the subtree by moving to next node.
						break;
					}

					List<AlgorithmNodeImp> newRemaining = new ArrayList<>(remainingNodes);
					newRemaining.remove(i);

					recursiveScheduleGeneration(newProcessed, newRemaining);
					/*
					 * Heuristic #1 - Checking for symmetry. (a1... would have a symmetry with 
					 * a2 ...)
					 * Also, (a1 b2 ...) would have a symmetry with (a1 b3...)
					 * 
					 * Implementation logic: we can break if schedules has no repetition of 
					 * cores, because assigning it to a different core always causes symmetry.
					 */

					List<Integer> coresAssigned = newProcessed.stream()
							.map(AlgorithmNodeImp::getCore)
							.collect(Collectors.toList());
					long noOfDistinctCores = coresAssigned.stream()
							.distinct()
							.count();

					if (coresAssigned.size() == noOfDistinctCores) {
						break; //I.e. there is no duplicate.
					}
				}
			}
		}
	}

	private void setNewBestSchedule(ScheduleImp st) {
		for (int i = 0; i < st.getSizeOfSchedule(); i++) {
			NodeSchedule nodeSchedule = new NodeScheduleImp(st.getNodeStartTime(i), st.getNodeCore(i));
			_currentBestSchedule.put(st.getNodeName(i), nodeSchedule);

			//TODO fireUpdates to visualisation
		}
	}

	/**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 */
	private boolean checkValidSchedule(List<AlgorithmNodeImp> schedule) {
		if (schedule == null) {
			return false;
		}
		
		//Get the last node's predecessors
		Node currentNode = _dag.getNodeByName(schedule.get(schedule.size()-1).getNodeName());
		List<Node> predecessors = currentNode.getPredecessors();

		//If there are no predecessors, then it is a starting node.
		if (predecessors.size() == 0) {
			return true;
		} else if (schedule.size() == 1) { //if has predecessor, but is the only node, then invalid. 
			return false;
		}

		//Loop through the previous nodes in the schedule and count when a predecessor is found
		int counter = 0;
		for (int i = schedule.size() - 2; i >= 0; i--) {
			for (Node preNode : predecessors) {
				if (schedule.get(i).getNodeName().equals(preNode.getName())) {
					counter++;
					break;
				}
			}
		}

		//Check if all the predecessors were found
		if (counter != predecessors.size()) {
			return false;
		}
		return true;
	}

	/**
	 * Calculates the time cost of executing the given schedule, returning a complete ScheduleImp object.
	 * @param algNodes - A {@code List<AlgorithmNodeImp>} given in the order of execution
	 * @return - ScheduleImp object with cost and execution time information
	 */
	private ScheduleImp calculateTotalTime(List<AlgorithmNodeImp> algNodes) {
		//creating a corresponding array of Nodes
		List<Node> nodes = new ArrayList<>();

		//populating new nodes array with corresponding Node objects
		for (AlgorithmNodeImp algNode : algNodes) {
			nodes.add(_dag.getNodeByName(algNode.getNodeName()));
		}

		//creating ArrayLists to represent the schedule for each core
		//NOTE: could change the coreSchedule to just an ArrayList that holds the most recently scheduled node for each core
		List<AlgorithmNodeImp> latestAlgNodeInSchedules = Arrays.asList(new AlgorithmNodeImp[_numberOfCores]);

		//creating a ScheduleImp object for holding the schedule start times for each node
		ScheduleImp st = new ScheduleImp(algNodes);

		//looping through all the AlgorithmNodes to set startTimes
		for (AlgorithmNodeImp currentAlgNode : algNodes) {
			Node currentNode = nodes.get(algNodes.indexOf(currentAlgNode));
			int highestCost = 0;

			//calculate the highest time delay caused by dependencies
			try {
				highestCost = currentNode.getPredecessors().stream().map(node -> {
					int cost = st.getNodeStartTime(getIndexOfList(node, algNodes)) + node.getWeight();
					if (!(algNodes.get(getIndexOfList(node, algNodes)).getCore() == currentAlgNode.getCore())) {
						//add on arc weight, since they're on different cores
						cost += currentNode.getInArc(node).getWeight();
					}

					return cost;
				}).max(Comparator.naturalOrder()).get();
			} catch (NoSuchElementException e) {
				//Means there are no predecessors, highestCost stays 0
			}

			//calculate the time delay caused by previous processes on the same core
			AlgorithmNodeImp latestNode = latestAlgNodeInSchedules.get(currentAlgNode.getCore() - 1);
			if (latestNode != null) {
				Node previousNode = _dag.getNodeByName(latestNode.getNodeName());
				int cost = previousNode.getWeight() + st.getNodeStartTime(algNodes.indexOf(latestNode));
				if (cost > highestCost) {
					highestCost = cost;
				}
			}

			//set currentAlgNode as the newest node to be scheduled on it's core
			latestAlgNodeInSchedules.set(currentAlgNode.getCore() - 1, currentAlgNode);

			//set ScheduleImp startTime for this node
			st.setStartTimeForNode(highestCost, algNodes.indexOf(currentAlgNode));
		}

		setTimeForSchedule(latestAlgNodeInSchedules, algNodes, st);

		return st;
	}

	/**
	 * Calculates and sets the total time in the {@code ScheduleImp} object given.
	 * Main purpose is to make the code more readable.
	 * @param latestAlgNodeInSchedules - {@code List<AlgorithmNodeImp>} containing the last node in each processor
	 * @param algNodes - the same {@code List<AlgorithmnNode>} used to construct the {@code ScheduleImp} object
	 * @param st - {@code ScheduleImp} object to set the total time of
	 */
	private void setTimeForSchedule(List<AlgorithmNodeImp> latestAlgNodeInSchedules, List<AlgorithmNodeImp> algNodes, ScheduleImp st) {
		int totalTime = 0;
		for (int i = 1; i <= _numberOfCores; i++) {
			AlgorithmNodeImp latestAlgNode = latestAlgNodeInSchedules.get(i - 1);

			int timeTaken = 0;
			if (latestAlgNode != null) {
				timeTaken = st.getNodeStartTime(algNodes.indexOf(latestAlgNode)) + _dag.getNodeByName(latestAlgNode.getNodeName()).getWeight();
			}

			if (timeTaken > totalTime) {
				totalTime = timeTaken;
			}
		}

		st.setTotalTime(totalTime);
	}

	/**
	 * Finds and returns the index position of the corresponding {@code AlgorithmNodeImp} within the given {@code List<AlgorithmNodeImp}
	 * @param node - {@code Node} to find the corresponding index position for
	 * @param algNodes - {@code List<AlgorithmNodeImp>} to find the index for
	 * @return the index position of the corresponding {@code AlgorithmNodeImp} object
	 */
	private int getIndexOfList(Node node, List<AlgorithmNodeImp> algNodes) {
		AlgorithmNodeImp correspondingNode = algNodes.stream().filter(n -> node.getName().equals(n.getNodeName()))
				.findFirst()
				.get();

		return algNodes.indexOf(correspondingNode);
	}

	@Override
	public HashMap<String, NodeSchedule> getCurrentBestSchedule() {
		return _currentBestSchedule;
	}

	@Override
	public int getBestTotalTime() {
		return _bestTime;
	}

	/**
	 * The wrapper methods purely for testing. (as the methods were declared to be private)
	 * @param algNodes
	 * @return
	 */
	public ScheduleImp calculateTotalTimeWrapper(List<AlgorithmNodeImp> algNodes) {
		return calculateTotalTime(algNodes);
	}

	public boolean checkValidScheduleWrapper(List<AlgorithmNodeImp> s1) {
		return checkValidSchedule(s1);
	}
}
