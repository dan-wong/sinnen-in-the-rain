package implementations.algorithm;

import interfaces.DAG;

import java.util.ArrayList;
import java.util.List;

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
}