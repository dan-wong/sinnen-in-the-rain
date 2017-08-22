package implementations.structures;

import implementations.algorithm.AlgorithmNodeImp;
import implementations.io.InputImp;
import interfaces.io.Input;
import interfaces.structures.Schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
 * @author Victor
 *
 */
public class ScheduleImp implements Schedule {
	private List<AlgorithmNodeImp> _algNodes;
	//The index for this field should match the index for the list of nodes.
	private int[] _startTimeForNode;// = new int[];
	private int _totalTime;
	
	private Map<Integer, AlgorithmNodeImp> _lastAlgNodeOnCore;

	/**
	 * The default constructor should only be called when 
	 * the schedule is empty. (No node is in the schedule).
	 */
	public ScheduleImp() {
		_algNodes = new ArrayList<AlgorithmNodeImp>();
		//TODO: Work on lastAlgNode.
	}
	
	public ScheduleImp(List<AlgorithmNodeImp> algNodes) {
		_algNodes = algNodes;
		_startTimeForNode = new int[_algNodes.size()];
	}
	
	/**
	 * @param startTime
	 * @param index --- The index should match the index for the List<AlgorithmNodes>.
	 */
	@Override
	public void setStartTimeForNode (int startTime, int index) {
		_startTimeForNode[index] = startTime;
	}

	/**
	 * The use of this method may break encapsulation.
	 * Use the methods getNodeName(), getNodeStartTime(),
	 * getNodeCore() instead.
	 * @return
	 */
	@Deprecated
	public List<AlgorithmNodeImp> getAlgorithmNodes() {
		return _algNodes;
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
	
	/**
	 * The use of this method may break encapsulation. 
	 * Use the methods getNodeName(), getNodeStartTime(), 
	 * getNodeCore() instead. 
	 * @return
	 */
	@Deprecated
	public int[] getstartTimeForNodes() {
		return _startTimeForNode;
	}

	@Override
	public String getNodeName(int index) {
		return _algNodes.get(index).getNodeName();
	}

	@Override
	public int getNodeStartTime (int index) {
		return _startTimeForNode[index];
	}

	@Override
	public int getNodeCore (int index) {
		return _algNodes.get(index).getCore();
	}

	@Override
	public int getTotalTime() {
		return _totalTime;
	}

	@Override
	public void setTotalTime(int totalTime) {
		_totalTime = totalTime;
	}
}
