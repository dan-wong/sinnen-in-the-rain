package implementations.algorithm;

import interfaces.DAG;
import interfaces.Node;

import java.util.ArrayList;
import java.util.Collections;
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

    //todo
    public boolean checkAlgorithmNode( List<AlgorithmNode> schedule){
        List<Node> startNodes = _dag.getStartNodes();
        //check first node, if not a starting node, just return
        if (!startNodes.contains(schedule.get(0))){
            return false;
        }

        // loop the given schedule
        for(int i = schedule.size()-1; i > 0; i--) {
            AlgorithmNode current_aNode = schedule.get(i);
            Node currentNode =
                    _dag.getNodeByName(current_aNode.getNodeName());

            List<Node> currentPredecessors = currentNode.getPredecessors();

            int countdown = currentPredecessors.size();
            while (i-1>0) {
                Node node =
                        _dag.getNodeByName(schedule.get(i-1).getNodeName());

                i--;
                if (!currentPredecessors.contains(node)) {
                    break;
                } else {
                    //it is a predecessor
                    countdown--;
                }
            }

            if (countdown != 0){
                if(!currentPredecessors.contains(schedule.get(i-1))){
                    // more nodes to be processed
                    // but not allowed to move to the next node unless all predecessors have been finished
                    return false;
                }
            }
        }
        return true;
    }

    //helper
    private boolean equalLists(List<String> one, List<String> two){
        if (one == null && two == null){
            return true;
        }

        if((one == null && two != null)
                || one != null && two == null
                || one.size() != two.size()){
            return false;
        }

        //to avoid messing the order of the lists we will use a copy
        //as noted in comments by A. R. S.
        one = new ArrayList<String>(one);
        two = new ArrayList<String>(two);

        Collections.sort(one);
        Collections.sort(two);
        return one.equals(two);
    }
}
