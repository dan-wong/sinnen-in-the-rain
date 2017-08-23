package interfaces.structures;

import java.util.List;

import interfaces.algorithm.AlgorithmNode;

public interface Schedule {
    void setStartTimeForNode(int startTime, int index);

    @Deprecated
    List<AlgorithmNode> getAlgorithmNodes();

    @Deprecated
    int[] getstartTimeForNodes();

    int getSizeOfSchedule();

    String getNodeName(int index);

    int getNodeStartTime(int index);

    int getNodeCore(int index);

    int getTotalTime();

    void setTotalTime(int totalTime);
    
    AlgorithmNode getLastNodeOnCore(int core);
    
    Schedule appendNodeToSchedule(AlgorithmNode current, int startTime);
}
