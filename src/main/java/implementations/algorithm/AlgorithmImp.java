package implementations.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import implementations.structures.DAGImp;
import implementations.structures.NodeScheduleImp;
import implementations.structures.ScheduleImp;
import interfaces.algorithm.Algorithm;
import interfaces.algorithm.AlgorithmNode;
import interfaces.structures.DAG;
import interfaces.structures.Node;
import interfaces.structures.NodeSchedule;
import interfaces.structures.Schedule;
import visualisation.BarChartModel;
import visualisation.Clock;
import visualisation.ComboView;
import visualisation.TableModel;

/**
 * This class represents the algorithm to solve the scheduling problem.
 * The class is responsible for all DFS searches and maintaining a current best result.
 * The class also acts as a controller for the View to update the visualisation.
 * 
 * Algorithm @author: Daniel, Victor, Wayne
 * 
 * Visualisation @author: Pulkit
 */
public class AlgorithmImp implements Algorithm {
	private DAG _dag;
	private int _numberOfCores;
	private HashMap<String, NodeSchedule> _currentBestSchedule;
	private int _recursiveCalls = 0; //For benchmarking purposes only

	private TableModel _model;
	private BarChartModel _chartModel;
	private ComboView _schedule;
	
	private int _bestTime = Integer.MAX_VALUE;

	private Set<Set<AlgorithmNode>> _uniqueProcessed;

	private boolean _visualisation;
	

	public AlgorithmImp(int numberOfCores, boolean visualisation, int noOfParallerCores) {
		_dag = DAGImp.getInstance();
		_numberOfCores = numberOfCores;
		_currentBestSchedule = new HashMap<>();
		_visualisation = visualisation;
		
		if (_visualisation){
			// TODO: Check if visualization is true, only then do we create the gui. 
			_model = TableModel.getInstance();
			_model.initModel(_currentBestSchedule, _dag, _numberOfCores);
			// Initialize BarChart Model:
			_chartModel = new BarChartModel();
			// set-up the GUI
			_schedule = new ComboView(_model,_dag, _numberOfCores,_chartModel);
			_schedule.setParallelLabel(noOfParallerCores);
		}
		

		_uniqueProcessed = new HashSet<Set<AlgorithmNode>>();

		produceSequentialSchedule();
		produceGreedySchedule();


		Schedule emptySchedule = new ScheduleImp(_numberOfCores);
		recursiveScheduleGeneration(new ArrayList<AlgorithmNode>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()), emptySchedule);
		
		// update view once we have our final schedule:
		if (_visualisation) {
			
			_model.changeData(_currentBestSchedule, _bestTime);
			// reset model once we're done with it - to help with testing.
			_model = TableModel.resetInstance();
			// STOP THE CLOCK, now that we're done with it and set Label to done.
			Clock.getInstance().stopClock();
			_schedule.setStatusLabel(Clock.getInstance().getProcessStatus());
		}
	}

	/**
	 * helper method for firing update.
	 */
	private void fireUpdateToGUI(int bestTime) {
		/// update view, now that a new schedule is available
		_chartModel.addDataToSeries(bestTime);
		_model.changeData(_currentBestSchedule, bestTime);
		_schedule.setBestTimeText(bestTime);
	}

	/**
	 * This method will produce a sequential schedule to set the lower bound.
	 * 
	 * This will be used together will the greedy schedule to bound
	 * the DFS.
	 */
	private void produceSequentialSchedule() {
		List<Node> reachableNodes = new ArrayList<Node>();
		List<Node> completedNodes = new ArrayList<Node>();
		List<Node> remainingNodes = new ArrayList<Node>();

		reachableNodes.addAll(_dag.getStartNodes());
		remainingNodes.addAll(_dag.getAllNodes());

		Schedule schedule = new ScheduleImp(_numberOfCores);

		while (!reachableNodes.isEmpty()) {
			Node toBeScheduled = reachableNodes.get(0);

			AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());
			algNode.setCore(1);
			schedule = schedule.getNextSchedule(algNode);

			//Configure the list of reachable, completed and remaining nodes again.
			completedNodes.add(toBeScheduled);
			reachableNodes.remove(toBeScheduled);
			remainingNodes.remove(toBeScheduled);
			for (Node rn : remainingNodes) {
				if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)){
					reachableNodes.add(rn);
				}
			}
		}

		//Set the best time to be sequential.
		setNewBestSchedule(schedule);
		_bestTime = schedule.getTotalTime();
	}

	/**
	 * This method will produce a greedy schedule to set the lower bound.
	 * 
	 * This will be used together will the sequential schedule to bound
	 * the DFS.
	 */
	private void produceGreedySchedule() {
		List<Node> reachableNodes = new ArrayList<Node>();
		List<Node> completedNodes = new ArrayList<Node>();
		List<Node> remainingNodes = new ArrayList<Node>();

		reachableNodes.addAll(_dag.getStartNodes());
		remainingNodes.addAll(_dag.getAllNodes());

		Schedule schedule = new ScheduleImp(_numberOfCores);

		while (!reachableNodes.isEmpty()) {
			//Prioritize the node with the maximum outwards arcs
			List<Integer> reachableAmount = new ArrayList<Integer>();
			for (Node n: reachableNodes) {
				reachableAmount.add(n.getSuccessors().size());
			}
			int maxIndex = reachableAmount.indexOf(Collections.max(reachableAmount));
			Node toBeScheduled = reachableNodes.get(maxIndex);

			//Select a core such that the finish time will be the earliest.
			List<Integer> earliestStartTimes = new ArrayList<Integer>(); //Note that index 0 = core 1!!!
			for (int i = 1; i<= _numberOfCores; i++) {
				int coreStart = schedule.getFinishTimeForCore(i);
				AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());
				algNode.setCore(i);
				int depStart = schedule.getDependencyBasedStartTime(toBeScheduled, algNode);
				earliestStartTimes.add((coreStart > depStart) ? coreStart : depStart);
			}
			int earliestCoreNo = earliestStartTimes.indexOf(Collections.min(earliestStartTimes)) + 1;

			AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());
			algNode.setCore(earliestCoreNo);
			schedule = schedule.getNextSchedule(algNode);

			//Configure the list of reachable, completed and remaining nodes again.
			completedNodes.add(toBeScheduled);
			reachableNodes.remove(toBeScheduled);
			remainingNodes.remove(toBeScheduled);
			for (Node rn : remainingNodes) {
				if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)){
					reachableNodes.add(rn);
				}
			}
		}

		if (schedule.getTotalTime() < _bestTime) {
			setNewBestSchedule(schedule);
			_bestTime = schedule.getTotalTime();
		}
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
	 * This method recursively does the branch and bound traversal.
	 * It takes the list of processed, remaining and previous schedule and from there determines if we need to keep going
	 * or checking if it's better than the current time.
	 *
	 * Branch down by adding each node to all the cores and then branching. Check times against heuristics and best time
	 * to decide whether to bound.
	 *
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 */
	private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {
		if (_visualisation){
			_schedule.setCallsButtonText(_recursiveCalls++); // Updating visualisation.
		}

		//Base Case when there are no remaining nodes left to process
		if (remainingNodes.size() == 0) {
			Schedule finalSchedule = prev;
			//Found a new best schedule, 
			//or the same time but no current best schedule (the first time reaching a trivial schedule)
			if (finalSchedule.getTotalTime() < _bestTime) {
				setNewBestSchedule(finalSchedule);
				_bestTime = finalSchedule.getTotalTime();
			}
		} else {
			for (int i = 0; i < remainingNodes.size(); i++) {
				Schedule newSchedule;

				//Assign the node to each core and continue recursive call down the branch
				for (int j = 1; j <= _numberOfCores; j++) {
					
					//Create a clone of the next node and assign it to a core. Place that new node
					//on a copy of the processed list
					List<AlgorithmNode> newProcessed = new ArrayList<>(processed);
					AlgorithmNode node = remainingNodes.get(i).createClone();
					node.setCore(j);
					newProcessed.add(node);

					Set<AlgorithmNode> algNodesSet = new HashSet<AlgorithmNode>(newProcessed);

					if (checkValidSchedule(newProcessed)) {
						/*
						 * Bounding using the cost function:
						 * 
						 * The cost function is defined as, the maximum of:
						 * 
						 * 1. (Sum of all remaining nodes weight - totalIdleTime ) / Cores
						 * 2. The largest node - largest idle time of any processor
						 */
						int idleTime = prev.getTotalIdleTime();
						
						int remainingTime = remainingNodes.stream()
								.map(rn -> _dag.getNodeByName(rn.getNodeName()))
								.map(n -> n.getWeight())
								.reduce(0, (a, b) -> a+b);
						
						double maxIdleTime = 0;
						for (int k = 1; k <= _numberOfCores; k++) {
							int processorIdleTime = prev.getTotalTime() - prev.getFinishTimeForCore(i);
							if (maxIdleTime < processorIdleTime) {
								maxIdleTime = processorIdleTime;
							}
						}
						
						double maxNodeWeight = 0;
						for (AlgorithmNode algNode : remainingNodes) {
							Node coNode = _dag.getNodeByName(algNode.getNodeName());
							if (maxNodeWeight < coNode.getWeight()) {
								maxNodeWeight = coNode.getWeight();
							}
						}
						
						double shortestTimePossible = Math.max(Math.ceil((remainingTime - idleTime + 0.0) / _numberOfCores), maxNodeWeight - maxIdleTime);
						if (prev.getTotalTime() + shortestTimePossible >= _bestTime){
							continue;
						}
						//=============END OF COST FUNCTION BOUNDING===============//
						
						newSchedule = prev.getNextSchedule(node);

						//If current >= best time, bound by moving to the next processor.
						if ((newSchedule.getTotalTime() >= _bestTime) ) {
							continue;
						}

						if (costFunction(newSchedule, remainingNodes, i) >= _bestTime) {
							continue;
						}
					} else { //Schedule is invalid, then pruning the subtree by moving to next node.
						break;
					}

					/*Pruning:
					 * 
					 * Heuristic #3 - Duplication detection.
					 * (a1 c1 b1) is equivalent to (a1 b1 c1)
					 * 
					 * Implementation logic:
					 * Maintain a set of visited "set of algorithm nodes".
					 * If a particular algorithm nodes set is already processed, continue
					*/
					if (_uniqueProcessed.contains(algNodesSet)) {
						continue;
					}
					else {
						_uniqueProcessed.add(algNodesSet);
					}


					//Create a new remaining list and remove the node that has been added to the processed list
					List<AlgorithmNode> newRemaining = new ArrayList<>(remainingNodes);
					newRemaining.remove(i);

					recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);

					/*
					 * Pruning:
					 * 
					 * Heuristic #1 - Symmetry. (a1... would have a symmetry with 
					 * a2 ...)
					 * Also, (a1 b2 ...) would have a symmetry with (a1 b3...)
					 * 
					 * Heuristic #2 - Partial symmetry. (a1 b1 c2) would be the same as
					 * (a1 b1 c3), in which case this is a partial symmetry on subtree. 
					 * 
					 * Implementation logic: we can break if the current node's core has
					 * never appeared before. -> This will implement both heuristic #1 & #2
					 */
					List<Integer> coresAssigned = new ArrayList<Integer>();
					for (AlgorithmNode algNode : processed) {
						coresAssigned.add(algNode.getCore());
					}

					if (!coresAssigned.contains(node.getCore())) {
						break; 
					}
				}
			}
		}
	}

	private int costFunction(Schedule st, List<AlgorithmNode> remaining, int index) {
		int heuristicCost, remainingWeights = 0;

		for (int i = 0; i < remaining.size(); i++) {
			if (i != index) {
				AlgorithmNode node = remaining.get(i);
				remainingWeights += _dag.getNodeByName(node.getNodeName()).getWeight();
			}
		}

		return st.getTotalTime() + (remainingWeights - st.getIdleTime()) / _numberOfCores;
	}

	private void setNewBestSchedule(Schedule finalSchedule) {
		for (int i = 0; i < finalSchedule.getSizeOfSchedule(); i++) {
			NodeSchedule nodeSchedule = new NodeScheduleImp(finalSchedule.getNodeStartTime(i), finalSchedule.getNodeCore(i));
			_currentBestSchedule.put(finalSchedule.getNodeName(i), nodeSchedule);
		}
		
		if (_visualisation) {
			fireUpdateToGUI(finalSchedule.getTotalTime());
		}
	}

	/**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 */
	private boolean checkValidSchedule(List<AlgorithmNode> schedule) {
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
		return counter == predecessors.size();
	}

	/**
	 * Calculates the time cost of executing the given schedule, returning a complete ScheduleImp object.
	 * @param algNodes - A {@code List<AlgorithmNode>} given in the order of execution
	 * @return - ScheduleImp object with cost and execution time information
	 */
	@Deprecated
	private ScheduleImp calculateTotalTime(List<AlgorithmNode> algNodes) {
		//creating a corresponding array of Nodes
		List<Node> nodes = new ArrayList<>();

		//populating new nodes array with corresponding Node objects
		for (AlgorithmNode algNode : algNodes) {
			nodes.add(_dag.getNodeByName(algNode.getNodeName()));
		}

		//creating ArrayLists to represent the schedule for each core
		//NOTE: could change the coreSchedule to just an ArrayList that holds the most recently scheduled node for each core
		List<AlgorithmNode> latestAlgNodeInSchedules = Arrays.asList(new AlgorithmNode[_numberOfCores]);

		//creating a ScheduleImp object for holding the schedule start times for each node
		ScheduleImp st = new ScheduleImp(algNodes, _numberOfCores);

		//looping through all the AlgorithmNodes to set startTimes
		for (AlgorithmNode currentAlgNode : algNodes) {
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
			AlgorithmNode latestNode = latestAlgNodeInSchedules.get(currentAlgNode.getCore() - 1);
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
	 * @param latestAlgNodeInSchedules - {@code List<AlgorithmNode>} containing the last node in each processor
	 * @param algNodes - the same {@code List<AlgorithmnNode>} used to construct the {@code ScheduleImp} object
	 * @param st - {@code ScheduleImp} object to set the total time of
	 */
	@Deprecated
	private void setTimeForSchedule(List<AlgorithmNode> latestAlgNodeInSchedules, List<AlgorithmNode> algNodes, ScheduleImp st) {
		int totalTime = 0;
		for (int i = 1; i <= _numberOfCores; i++) {
			AlgorithmNode latestAlgNode = latestAlgNodeInSchedules.get(i - 1);

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
	 * Finds and returns the index position of the corresponding {@code AlgorithmNode} within the given {@code List<AlgorithmNode}
	 * @param node - {@code Node} to find the corresponding index position for
	 * @param algNodes - {@code List<AlgorithmNode>} to find the index for
	 * @return the index position of the corresponding {@code AlgorithmNode} object
	 */
	@Deprecated
	private int getIndexOfList(Node node, List<AlgorithmNode> algNodes) {
		AlgorithmNode correspondingNode = algNodes.stream().filter(n -> node.getName().equals(n.getNodeName()))
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
	@Deprecated
	public ScheduleImp calculateTotalTimeWrapper(List<AlgorithmNode> algNodes) {
		return calculateTotalTime(algNodes);
	}

	public boolean checkValidScheduleWrapper(List<AlgorithmNode> s1) {
		return checkValidSchedule(s1);
	}
}