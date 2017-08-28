package interfaces.structures;

import interfaces.algorithm.AlgorithmNode;

import java.util.List;

/**
 * The schedule interface represents the abstraction of a schedule (or a partial schedule)
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
 * See the implementation class for more details on implementation. 
 * @see implementations.structures.ScheduleImp
 * 
 * @author Victor
 *
 */
public interface Schedule {
	/**
	 * This method sets the start time for an algorithm node.
	 * 
	 * If the index is equal to the size, in which case this is the first 
	 * time a start time has been assigned to this object, it would add
	 * it to the List<Integer>.
	 * Otherwise, an update is done. 
	 * 
	 * @param startTime
	 * @param index --- The index should match the index for the List<AlgorithmNodes>.
	 */
    void setStartTimeForNode(int startTime, int index);

	/**
	 * The use of this method may break encapsulation.
	 * Use the methods getNodeName(), getNodeStartTime(),
	 * getNodeCore() instead.
	 * @return
	 */
    @Deprecated
    List<AlgorithmNode> getAlgorithmNodes();

	/**
	 * The use of this method may break encapsulation. 
	 * Use the methods getNodeName(), getNodeStartTime(), 
	 * getNodeCore() instead. 
	 * @return
	 */
    @Deprecated
    List<Integer> getstartTimeForNodes();

	/**
	 * The getters below should be called
	 * when processing node time information.
	 *
	 * @return
	 */
    int getSizeOfSchedule();
    String getNodeName(int index);
    int getNodeStartTime(int index);
    int getNodeCore(int index);
    int getTotalTime();

    /**
     * This setter will set the total time field of the schedule.
     * Warning: this method may break encapsulation.
     * 
     * This method is no longer being used as the new method of 
     * generating schedules no longer needs to set total time
     * from a different class. 
     * @param totalTime
     */
    @Deprecated
    void setTotalTime(int totalTime);
    
    /**
     * This method returns the last algorithm node that is executing
     * on a particular core given "this" schedule.
     * @param core
     * @return
     */
    AlgorithmNode getLastNodeOnCore(int core);
    
    /**
     * This method should take the current node that's being processed,
	 * and add it to the schedule. 
	 * 
	 * The method will return a new schedule with all the old info +
	 * the new current node. 
	 * 
	 * This is the method that gets called most frequently in the algorithm
	 * class.
     * @param currentNode
     * @return
     */
    Schedule getNextSchedule(AlgorithmNode currentNode);
    
    /**
     * A debugging method for printing schedule information.
     */
    void printSchedule();

    int getIdleTime();
    
	/**
	 * This method returns the finish time for a particular core
	 * based on "this" schedule. 
	 * 
	 * @param coreNo
	 * @return
	 */
    int getFinishTimeForCore (int coreNo);
    
	/**
	 * This method calculates the earliest start time for the current node, 
	 * based on the dependencies (predecessors) it has and where they have
	 * been scheduled.
	 * 
	 * @param currentNode
	 * @param currentAlgNode
	 * @return
	 */
    int getDependencyBasedStartTime (Node currentNode, AlgorithmNode currentAlgNode);
    
    /**
     * This method gets the total idle time of all processors.
     * The idle time is defined as: the difference between this processor's
     * finish time to the schedule time. 
     * @return
     */
    int getTotalIdleTime();
}
