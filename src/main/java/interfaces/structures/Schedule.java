package interfaces.structures;

import interfaces.algorithm.AlgorithmNode;

import java.util.List;

/**
 * The class which implements this interface represents 
 * the abstraction of a schedule (or a partial schedule)
 * A schedule contains an ordered list of nodes together with an assigned core. 
 * 
 * See the implementation class for more details. 
 * @see implementations.structures.ScheduleImp
 * 
 * @author Victor
 *
 */
public interface Schedule {
    void setStartTimeForNode(int startTime, int index);

    @Deprecated
    List<AlgorithmNode> getAlgorithmNodes();

    @Deprecated
    List<Integer> getstartTimeForNodes();

    int getSizeOfSchedule();

    String getNodeName(int index);

    int getNodeStartTime(int index);

    int getNodeCore(int index);

    int getTotalTime();

    void setTotalTime(int totalTime);
    
    AlgorithmNode getLastNodeOnCore(int core);
    
    Schedule getNextSchedule(AlgorithmNode currentNode);
    
    void printSchedule();

    int getIdleTime();
    
}
