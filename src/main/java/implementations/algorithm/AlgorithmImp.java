package implementations.algorithm;//####[1]####
//####[1]####
import java.util.ArrayList;//####[3]####
import java.util.Arrays;//####[4]####
import java.util.Collections;//####[5]####
import java.util.Comparator;//####[6]####
import java.util.HashMap;//####[7]####
import java.util.HashSet;//####[8]####
import java.util.List;//####[9]####
import java.util.NoSuchElementException;//####[10]####
import java.util.Set;//####[11]####
import java.util.concurrent.Semaphore;//####[13]####
import java.lang.InterruptedException;//####[14]####
import implementations.structures.DAGImp;//####[16]####
import implementations.structures.NodeScheduleImp;//####[17]####
import implementations.structures.ScheduleImp;//####[18]####
import interfaces.algorithm.Algorithm;//####[19]####
import interfaces.algorithm.AlgorithmNode;//####[20]####
import interfaces.structures.DAG;//####[21]####
import interfaces.structures.Node;//####[22]####
import interfaces.structures.NodeSchedule;//####[23]####
import interfaces.structures.Schedule;//####[24]####
import visualisation.BarChartModel;//####[25]####
import visualisation.Clock;//####[26]####
import visualisation.ComboView;//####[27]####
import visualisation.TableModel;//####[28]####
//####[28]####
//-- ParaTask related imports//####[28]####
import pt.runtime.*;//####[28]####
import java.util.concurrent.ExecutionException;//####[28]####
import java.util.concurrent.locks.*;//####[28]####
import java.lang.reflect.*;//####[28]####
import pt.runtime.GuiThread;//####[28]####
import java.util.concurrent.BlockingQueue;//####[28]####
import java.util.ArrayList;//####[28]####
import java.util.List;//####[28]####
//####[28]####
/**
 * This class represents the algorithm to solve the scheduling problem.
 * The class is responsible for all DFS searches and maintaining a current best result.
 * The class also acts as a controller for the View to update the visualisation.
 * 
 * Algorithm @author: Daniel, Victor, Wayne
 * 
 * Visualisation @author: Pulkit
 *///####[38]####
public class AlgorithmImp implements Algorithm {//####[39]####
    static{ParaTask.init();}//####[39]####
    /*  ParaTask helper method to access private/protected slots *///####[39]####
    public void __pt__accessPrivateSlot(Method m, Object instance, TaskID arg, Object interResult ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {//####[39]####
        if (m.getParameterTypes().length == 0)//####[39]####
            m.invoke(instance);//####[39]####
        else if ((m.getParameterTypes().length == 1))//####[39]####
            m.invoke(instance, arg);//####[39]####
        else //####[39]####
            m.invoke(instance, arg, interResult);//####[39]####
    }//####[39]####
//####[40]####
    private DAG _dag;//####[40]####
//####[41]####
    private int _numberOfCores;//####[41]####
//####[42]####
    private HashMap<String, NodeSchedule> _currentBestSchedule;//####[42]####
//####[43]####
    private int _recursiveCalls = 0;//####[43]####
//####[44]####
    private Semaphore _threads;//####[44]####
//####[45]####
    private int _numberOfThreads = 0;//####[45]####
//####[47]####
    private TableModel _model;//####[47]####
//####[48]####
    private BarChartModel _chartModel;//####[48]####
//####[49]####
    private ComboView _schedule;//####[49]####
//####[51]####
    private int _bestTime = Integer.MAX_VALUE;//####[51]####
//####[53]####
    private Set<Set<AlgorithmNode>> _uniqueProcessed;//####[53]####
//####[55]####
    private boolean _visualisation;//####[55]####
//####[58]####
    public AlgorithmImp(int numberOfCores, boolean visualisation, int noOfParallerCores) {//####[58]####
        _dag = DAGImp.getInstance();//####[59]####
        _numberOfCores = numberOfCores;//####[60]####
        _currentBestSchedule = new HashMap<String, NodeSchedule>();//####[61]####
        _visualisation = visualisation;//####[62]####
        _numberOfThreads = noOfParallerCores - 1;//####[63]####
        _threads = new Semaphore(_numberOfThreads);//####[64]####
        if (_visualisation) //####[66]####
        {//####[66]####
            _model = TableModel.getInstance();//####[68]####
            _model.initModel(_currentBestSchedule, _dag, _numberOfCores);//####[69]####
            _chartModel = new BarChartModel();//####[71]####
            _schedule = new ComboView(_model, _dag, _numberOfCores, _chartModel);//####[73]####
            _schedule.setParallelLabel(noOfParallerCores);//####[74]####
        }//####[75]####
        _uniqueProcessed = Collections.synchronizedSet(new HashSet<Set<AlgorithmNode>>());//####[78]####
        produceSequentialSchedule();//####[80]####
        produceGreedySchedule();//####[81]####
        Schedule emptySchedule = new ScheduleImp(_numberOfCores);//####[83]####
        recursiveScheduleGeneration(new ArrayList<AlgorithmNode>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()), emptySchedule);//####[84]####
        try {//####[86]####
            _threads.acquire(_numberOfThreads);//####[87]####
        } catch (InterruptedException ex) {//####[88]####
            ex.printStackTrace();//####[89]####
        }//####[90]####
        if (_visualisation) //####[93]####
        {//####[93]####
            _model.changeData(_currentBestSchedule, _bestTime);//####[95]####
            _model = TableModel.resetInstance();//####[97]####
            Clock.getInstance().stopClock();//####[99]####
            _schedule.setStatusLabel(Clock.getInstance().getProcessStatus());//####[100]####
        }//####[101]####
    }//####[102]####
//####[107]####
    /**
	 * helper method for firing update.
	 *///####[107]####
    private void fireUpdateToGUI(int bestTime) {//####[107]####
        _chartModel.addDataToSeries(bestTime);//####[109]####
        _model.changeData(_currentBestSchedule, bestTime);//####[110]####
        _schedule.setBestTimeText(bestTime);//####[111]####
    }//####[112]####
//####[120]####
    /**
	 * This method will produce a sequential schedule to set the lower bound.
	 * 
	 * This will be used together will the greedy schedule to bound
	 * the DFS.
	 *///####[120]####
    private void produceSequentialSchedule() {//####[120]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[121]####
        List<Node> completedNodes = new ArrayList<Node>();//####[122]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[123]####
        reachableNodes.addAll(_dag.getStartNodes());//####[125]####
        remainingNodes.addAll(_dag.getAllNodes());//####[126]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[128]####
        while (!reachableNodes.isEmpty()) //####[130]####
        {//####[130]####
            Node toBeScheduled = reachableNodes.get(0);//####[131]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[133]####
            algNode.setCore(1);//####[134]####
            schedule = schedule.getNextSchedule(algNode);//####[135]####
            completedNodes.add(toBeScheduled);//####[138]####
            reachableNodes.remove(toBeScheduled);//####[139]####
            remainingNodes.remove(toBeScheduled);//####[140]####
            for (Node rn : remainingNodes) //####[141]####
            {//####[141]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[142]####
                {//####[142]####
                    reachableNodes.add(rn);//####[143]####
                }//####[144]####
            }//####[145]####
        }//####[146]####
        setNewBestSchedule(schedule);//####[149]####
        _bestTime = schedule.getTotalTime();//####[150]####
    }//####[151]####
//####[159]####
    /**
	 * This method will produce a greedy schedule to set the lower bound.
	 * 
	 * This will be used together will the sequential schedule to bound
	 * the DFS.
	 *///####[159]####
    private void produceGreedySchedule() {//####[159]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[160]####
        List<Node> completedNodes = new ArrayList<Node>();//####[161]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[162]####
        reachableNodes.addAll(_dag.getStartNodes());//####[164]####
        remainingNodes.addAll(_dag.getAllNodes());//####[165]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[167]####
        while (!reachableNodes.isEmpty()) //####[169]####
        {//####[169]####
            List<Integer> reachableAmount = new ArrayList<Integer>();//####[171]####
            for (Node n : reachableNodes) //####[172]####
            {//####[172]####
                reachableAmount.add(n.getSuccessors().size());//####[173]####
            }//####[174]####
            int maxIndex = reachableAmount.indexOf(Collections.max(reachableAmount));//####[175]####
            Node toBeScheduled = reachableNodes.get(maxIndex);//####[176]####
            List<Integer> earliestStartTimes = new ArrayList<Integer>();//####[179]####
            for (int i = 1; i <= _numberOfCores; i++) //####[180]####
            {//####[180]####
                int coreStart = schedule.getFinishTimeForCore(i);//####[181]####
                AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[182]####
                algNode.setCore(i);//####[183]####
                int depStart = schedule.getDependencyBasedStartTime(toBeScheduled, algNode);//####[184]####
                earliestStartTimes.add((coreStart > depStart) ? coreStart : depStart);//####[185]####
            }//####[186]####
            int earliestCoreNo = earliestStartTimes.indexOf(Collections.min(earliestStartTimes)) + 1;//####[187]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[189]####
            algNode.setCore(earliestCoreNo);//####[190]####
            schedule = schedule.getNextSchedule(algNode);//####[191]####
            completedNodes.add(toBeScheduled);//####[194]####
            reachableNodes.remove(toBeScheduled);//####[195]####
            remainingNodes.remove(toBeScheduled);//####[196]####
            for (Node rn : remainingNodes) //####[197]####
            {//####[197]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[198]####
                {//####[198]####
                    reachableNodes.add(rn);//####[199]####
                }//####[200]####
            }//####[201]####
        }//####[202]####
        if (schedule.getTotalTime() < _bestTime) //####[204]####
        {//####[204]####
            setNewBestSchedule(schedule);//####[205]####
            _bestTime = schedule.getTotalTime();//####[206]####
        }//####[207]####
    }//####[208]####
//####[215]####
    /**
	 * Purely for benchmarking purposes
	 *
	 * @return number of times the recursive method was called
	 *///####[215]####
    public int getRecursiveCalls() {//####[215]####
        return _recursiveCalls;//####[216]####
    }//####[217]####
//####[225]####
    /**
	 * This method is a thread-safe way of comparing a schedule against the current 
	 * best schedule so far, replacing it if the new one is better.
	 * 
	 * @param newSchedule 	- The new Schedule to compare 
	 *///####[225]####
    private synchronized void compareSchedules(Schedule newSchedule) {//####[225]####
        if (newSchedule.getTotalTime() < _bestTime) //####[226]####
        {//####[226]####
            setNewBestSchedule(newSchedule);//####[227]####
            _bestTime = newSchedule.getTotalTime();//####[228]####
        }//####[229]####
    }//####[230]####
//####[241]####
    private static volatile Method __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = null;//####[241]####
    private synchronized static void __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet() {//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            try {//####[241]####
                __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = ParaTaskHelper.getDeclaredMethod(new ParaTaskHelper.ClassGetter().getCurrentClass(), "__pt__recursiveScheduleGenerationTask", new Class[] {//####[241]####
                    List.class, List.class, Schedule.class//####[241]####
                });//####[241]####
            } catch (Exception e) {//####[241]####
                e.printStackTrace();//####[241]####
            }//####[241]####
        }//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setTaskIdArgIndexes(0);//####[241]####
        taskinfo.addDependsOn(processed);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(0);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setTaskIdArgIndexes(1);//####[241]####
        taskinfo.addDependsOn(remainingNodes);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[241]####
        taskinfo.addDependsOn(processed);//####[241]####
        taskinfo.addDependsOn(remainingNodes);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(0);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(1);//####[241]####
        taskinfo.addDependsOn(remainingNodes);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(1);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(1);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(0);//####[241]####
        taskinfo.addDependsOn(processed);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(0, 1);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setTaskIdArgIndexes(2);//####[241]####
        taskinfo.addDependsOn(prev);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[241]####
        taskinfo.addDependsOn(processed);//####[241]####
        taskinfo.addDependsOn(prev);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(0);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(2);//####[241]####
        taskinfo.addDependsOn(prev);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[241]####
        taskinfo.addDependsOn(remainingNodes);//####[241]####
        taskinfo.addDependsOn(prev);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setTaskIdArgIndexes(0, 1, 2);//####[241]####
        taskinfo.addDependsOn(processed);//####[241]####
        taskinfo.addDependsOn(remainingNodes);//####[241]####
        taskinfo.addDependsOn(prev);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(0);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[241]####
        taskinfo.addDependsOn(remainingNodes);//####[241]####
        taskinfo.addDependsOn(prev);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(1);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(2);//####[241]####
        taskinfo.addDependsOn(prev);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(1);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[241]####
        taskinfo.addDependsOn(processed);//####[241]####
        taskinfo.addDependsOn(prev);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(0, 1);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(2);//####[241]####
        taskinfo.addDependsOn(prev);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(2);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(2);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(0);//####[241]####
        taskinfo.addDependsOn(processed);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(0, 2);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(2);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(1);//####[241]####
        taskinfo.addDependsOn(remainingNodes);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(2);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[241]####
        taskinfo.addDependsOn(processed);//####[241]####
        taskinfo.addDependsOn(remainingNodes);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(0, 2);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(1);//####[241]####
        taskinfo.addDependsOn(remainingNodes);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(1, 2);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(1, 2);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setTaskIdArgIndexes(0);//####[241]####
        taskinfo.addDependsOn(processed);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(0, 1, 2);//####[241]####
        taskinfo.setIsPipeline(true);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[241]####
    public void __pt__recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[241]####
        recursiveScheduleGeneration(processed, remainingNodes, prev);//####[242]####
        _threads.release();//####[243]####
    }//####[244]####
//####[244]####
//####[258]####
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
	 *///####[258]####
    private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[258]####
        if (_visualisation) //####[259]####
        {//####[259]####
            _schedule.setCallsButtonText(_recursiveCalls++);//####[260]####
        }//####[261]####
        if (remainingNodes.size() == 0) //####[264]####
        {//####[264]####
            Schedule finalSchedule = prev;//####[265]####
            compareSchedules(finalSchedule);//####[268]####
        } else {//####[269]####
            for (int i = 0; i < remainingNodes.size(); i++) //####[270]####
            {//####[270]####
                Schedule newSchedule;//####[271]####
                for (int j = 1; j <= _numberOfCores; j++) //####[274]####
                {//####[274]####
                    List<AlgorithmNode> newProcessed = new ArrayList<AlgorithmNode>(processed);//####[278]####
                    AlgorithmNode node = remainingNodes.get(i).createClone();//####[279]####
                    node.setCore(j);//####[280]####
                    newProcessed.add(node);//####[281]####
                    Set<AlgorithmNode> algNodesSet = new HashSet<AlgorithmNode>(newProcessed);//####[283]####
                    if (checkValidSchedule(newProcessed)) //####[285]####
                    {//####[285]####
                        int idleTime = prev.getTotalIdleTime();//####[294]####
                        double maxIdleTime = 0;//####[296]####
                        for (int k = 1; k <= _numberOfCores; k++) //####[297]####
                        {//####[297]####
                            int processorIdleTime = prev.getTotalTime() - prev.getFinishTimeForCore(i);//####[298]####
                            if (maxIdleTime < processorIdleTime) //####[299]####
                            {//####[299]####
                                maxIdleTime = processorIdleTime;//####[300]####
                            }//####[301]####
                        }//####[302]####
                        double maxNodeWeight = 0;//####[304]####
                        double remainingTime = 0;//####[305]####
                        for (AlgorithmNode algNode : remainingNodes) //####[306]####
                        {//####[306]####
                            Node coNode = _dag.getNodeByName(algNode.getNodeName());//####[307]####
                            if (maxNodeWeight < coNode.getWeight()) //####[308]####
                            {//####[308]####
                                maxNodeWeight = coNode.getWeight();//####[309]####
                            }//####[310]####
                            remainingTime += coNode.getWeight();//####[311]####
                        }//####[312]####
                        double shortestTimePossible = Math.max(Math.ceil((remainingTime - idleTime) / _numberOfCores), maxNodeWeight - maxIdleTime);//####[314]####
                        if (prev.getTotalTime() + shortestTimePossible >= _bestTime) //####[315]####
                        {//####[315]####
                            continue;//####[316]####
                        }//####[317]####
                        newSchedule = prev.getNextSchedule(node);//####[320]####
                        if ((newSchedule.getTotalTime() >= _bestTime)) //####[323]####
                        {//####[323]####
                            continue;//####[324]####
                        }//####[325]####
                    } else {//####[326]####
                        break;//####[327]####
                    }//####[328]####
                    if (_uniqueProcessed.contains(algNodesSet)) //####[339]####
                    {//####[339]####
                        continue;//####[340]####
                    } else {//####[342]####
                        _uniqueProcessed.add(algNodesSet);//####[343]####
                    }//####[344]####
                    List<AlgorithmNode> newRemaining = new ArrayList<AlgorithmNode>(remainingNodes);//####[348]####
                    newRemaining.remove(i);//####[349]####
                    List<Integer> coresAssigned = new ArrayList<Integer>();//####[364]####
                    for (AlgorithmNode algNode : processed) //####[365]####
                    {//####[365]####
                        coresAssigned.add(algNode.getCore());//####[366]####
                    }//####[367]####
                    if (!coresAssigned.contains(node.getCore())) //####[369]####
                    {//####[369]####
                        recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);//####[371]####
                        break;//####[372]####
                    } else {//####[373]####
                        if (_dag.getNodeByName(node.getNodeName()).getSuccessors().size() > 1 && _threads.tryAcquire()) //####[378]####
                        {//####[378]####
                            recursiveScheduleGenerationTask(newProcessed, newRemaining, newSchedule);//####[379]####
                        } else {//####[380]####
                            recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);//####[381]####
                        }//####[382]####
                    }//####[383]####
                }//####[384]####
            }//####[385]####
        }//####[386]####
    }//####[387]####
//####[389]####
    private void setNewBestSchedule(Schedule finalSchedule) {//####[389]####
        for (int i = 0; i < finalSchedule.getSizeOfSchedule(); i++) //####[390]####
        {//####[390]####
            NodeSchedule nodeSchedule = new NodeScheduleImp(finalSchedule.getNodeStartTime(i), finalSchedule.getNodeCore(i));//####[391]####
            _currentBestSchedule.put(finalSchedule.getNodeName(i), nodeSchedule);//####[392]####
        }//####[393]####
        if (_visualisation) //####[395]####
        {//####[395]####
            fireUpdateToGUI(finalSchedule.getTotalTime());//####[396]####
        }//####[397]####
    }//####[398]####
//####[407]####
    /**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 *///####[407]####
    private boolean checkValidSchedule(List<AlgorithmNode> schedule) {//####[407]####
        if (schedule == null) //####[408]####
        {//####[408]####
            return false;//####[409]####
        }//####[410]####
        Node currentNode = _dag.getNodeByName(schedule.get(schedule.size() - 1).getNodeName());//####[413]####
        List<Node> predecessors = currentNode.getPredecessors();//####[414]####
        if (predecessors.size() == 0) //####[417]####
        {//####[417]####
            return true;//####[418]####
        } else if (schedule.size() == 1) //####[419]####
        {//####[419]####
            return false;//####[420]####
        }//####[421]####
        int counter = 0;//####[424]####
        for (int i = schedule.size() - 2; i >= 0; i--) //####[425]####
        {//####[425]####
            for (Node preNode : predecessors) //####[426]####
            {//####[426]####
                if (schedule.get(i).getNodeName().equals(preNode.getName())) //####[427]####
                {//####[427]####
                    counter++;//####[428]####
                    break;//####[429]####
                }//####[430]####
            }//####[431]####
        }//####[432]####
        if (counter != predecessors.size()) //####[435]####
        {//####[435]####
            return false;//####[436]####
        }//####[437]####
        return true;//####[438]####
    }//####[439]####
//####[442]####
    @Override//####[442]####
    public HashMap<String, NodeSchedule> getCurrentBestSchedule() {//####[442]####
        return _currentBestSchedule;//####[443]####
    }//####[444]####
//####[447]####
    @Override//####[447]####
    public int getBestTotalTime() {//####[447]####
        return _bestTime;//####[448]####
    }//####[449]####
//####[454]####
    /**
	 * The wrapper methods purely for testing. (as the methods were declared to be private)
	 *///####[454]####
    public boolean checkValidScheduleWrapper(List<AlgorithmNode> s1) {//####[454]####
        return checkValidSchedule(s1);//####[455]####
    }//####[456]####
}//####[456]####
