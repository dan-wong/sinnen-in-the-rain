package implementations.algorithm;

import implementations.structures.NodeScheduleImp;
import implementations.structures.SchedulerTimeImp;
import interfaces.algorithm.Algorithm;
import interfaces.algorithm.AlgorithmNode;
import interfaces.structures.DAG;
import interfaces.structures.Node;
import interfaces.structures.NodeSchedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * This class implements the algorithm to solve the scheduling problem
 */
public class AlgorithmImp implements Algorithm {
    private DAG _dag;
	private int _numberOfCores;
	private HashMap<String, NodeSchedule> _currentBestSchedule;

	private int _bestTime = Integer.MAX_VALUE;

    public AlgorithmImp(DAG dag, int numberOfCores) {
        _dag = dag;
		_numberOfCores = numberOfCores;
		_currentBestSchedule = new HashMap<>();

        recursiveScheduleGeneration(new ArrayList<>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()));
    }

	/**
	 * This method recursively generates all possible schedules given a list of nodes.
	 *
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 */
    private void recursiveScheduleGeneration(List<AlgorithmNodeImp> processed, List<AlgorithmNodeImp> remainingNodes) {
        //Base Case
		if (remainingNodes.size() == 0) {
            SchedulerTimeImp st = calculateTotalTime(processed);
            if (st.getTotalTime() < _bestTime) { //Found a new best schedule
				setNewBestSchedule(st);
				_bestTime = st.getTotalTime();
			}
		} else {
			for (int i = 0; i < remainingNodes.size(); i++) {
				for (int j = 1; j <= _numberOfCores; j++) {
                    List<AlgorithmNodeImp> newProcessed = new ArrayList<>(processed);
                    AlgorithmNodeImp node = remainingNodes.get(i).createClone();
                    node.setCore(j);
					newProcessed.add(node);

					if (checkValidSchedule(newProcessed)) {
                        SchedulerTimeImp st = calculateTotalTime(newProcessed);
                        //Bound if >= best time
						if (st.getTotalTime() >= _bestTime) {
							continue;
						}
					} else {
						continue;
					}

//					newProcessed.forEach(n -> {
//						System.out.printf(n.getNodeName() + n.getCore() + " ");
//					});
//
//					System.out.println();

                    List<AlgorithmNodeImp> newRemaining = new ArrayList<>(remainingNodes);
                    newRemaining.remove(i);

					recursiveScheduleGeneration(newProcessed, newRemaining);
				}
			}
		}
	}

    private void setNewBestSchedule(SchedulerTimeImp st) {
        for (int i = 0; i < st.getSizeOfScheduler(); i++) {
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

        for (int i = 0; i < schedule.size(); i++) {
			//Get the currentNode's predecessors
			Node currentNode = _dag.getNodeByName(schedule.get(i).getNodeName());
			List<Node> predecessors = currentNode.getPredecessors();

            //If there are no predecessors, continue
            if (predecessors.size() == 0) {
                continue;
            }

			//Loop through the previous nodes in the schedule and count when a predecessor is found
			int counter = 0;
			for (int j = i - 1; j >= 0; j--) {
				for (Node preNode : predecessors) {
					if (schedule.get(j).getNodeName().equals(preNode.getName())) {
						counter++;
						break;
					}
				}
			}

			//Check if all the predecessors were found
			if (counter != predecessors.size()) {
				return false;
			}
		}
		return true;
	}

	/**
     * Calculates the time cost of executing the given schedule, returning a complete SchedulerTimeImp object.
     * @param algNodes - A {@code List<AlgorithmNodeImp>} given in the order of execution
     * @return - SchedulerTimeImp object with cost and execution time information
     */
    private SchedulerTimeImp calculateTotalTime(List<AlgorithmNodeImp> algNodes) {
        //creating a corresponding array of Nodes
		List<Node> nodes = new ArrayList<>();

		//populating new nodes array with corresponding Node objects
        for (AlgorithmNodeImp algNode : algNodes) {
            nodes.add(_dag.getNodeByName(algNode.getNodeName()));
		}

		//creating ArrayLists to represent the schedule for each core
		//NOTE: could change the coreSchedule to just an ArrayList that holds the most recently scheduled node for each core
        List<AlgorithmNodeImp> latestAlgNodeInSchedules = Arrays.asList(new AlgorithmNodeImp[_numberOfCores]);

        //creating a SchedulerTimeImp object for holding the schedule start times for each node
        SchedulerTimeImp st = new SchedulerTimeImp(algNodes);

		//looping through all the AlgorithmNodes to set startTimes
        for (AlgorithmNodeImp currentAlgNode : algNodes) {
            Node currentNode = nodes.get(algNodes.indexOf(currentAlgNode));
			int highestCost = 0;

			//calculate the highest time delay caused by dependencies
			for (Node node : currentNode.getPredecessors()) {
				//calculating the maximum delay caused by this particular dependent node
				int cost = st.getNodeStartTime(getIndexOfList(node, algNodes)) + node.getWeight();
				if (!(algNodes.get(getIndexOfList(node, algNodes)).getCore() == currentAlgNode.getCore())) {
					//add on arc weight, since they're on different cores
					cost += currentNode.getInArc(node).getWeight();
				}

				if (cost > highestCost) {
					highestCost = cost;
				}
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

            //set SchedulerTimeImp startTime for this node
            st.setStartTimeForNode(highestCost, algNodes.indexOf(currentAlgNode));
		}

		setTimeForSchedulerTime(latestAlgNodeInSchedules, algNodes, st);

		return st;
	}

	/**
     * Calculates and sets the total time in the {@code SchedulerTimeImp} object given.
     * Main purpose is to make the code more readable.
     * @param latestAlgNodeInSchedules - {@code List<AlgorithmNodeImp>} containing the last node in each processor
     * @param algNodes - the same {@code List<AlgorithmnNode>} used to construct the {@code SchedulerTimeImp} object
     * @param st - {@code SchedulerTimeImp} object to set the total time of
     */
    private void setTimeForSchedulerTime(List<AlgorithmNodeImp> latestAlgNodeInSchedules, List<AlgorithmNodeImp> algNodes, SchedulerTimeImp st) {
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
        for (AlgorithmNodeImp algNode : algNodes) {
            if (node.getName().equals(algNode.getNodeName())) {
				return algNodes.indexOf(algNode);
			}
		}

		return -1;
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
    public SchedulerTimeImp calculateTotalTimeWrapper(List<AlgorithmNodeImp> algNodes) {
        return calculateTotalTime(algNodes);
	}

    public boolean checkValidScheduleWrapper(List<AlgorithmNodeImp> s1) {
        return checkValidSchedule(s1);
	}
}
