import implementations.algorithm.AlgorithmImp;
import implementations.io.ConversionImp;
import implementations.io.InputImp;
import interfaces.io.Conversion;
import interfaces.structures.DAG;
import org.junit.Test;

public class ApplicationTest {
    private long startTime;
    private String core = "2";

    @Test
    public void testWithTwoCores() {
        core = "2";

		Nodes_7_OutTree();
		Nodes_8_Random();
		Nodes_9_SeriesParallel();
		Nodes_10_Random();
		//Nodes_11_OutTree();
    }

    @Test
    public void testWithFourCores() {
        core = "4";

//        Nodes_7_OutTree();
//        Nodes_8_Random();
//        Nodes_9_SeriesParallel();
//        Nodes_10_Random();
//        //Nodes_11_OutTree();
    }

    public void Nodes_7_OutTree() {
        System.out.println("Nodes_7_OutTree");

        startTimer();
        runAlgorithm("testFiles/Nodes_7_OutTree.dot");
        endTimer();
    }

    public void Nodes_8_Random() {
        System.out.println("Nodes_8_Random");

        startTimer();
        runAlgorithm("testFiles/Nodes_8_Random.dot");
        endTimer();
    }

    public void Nodes_9_SeriesParallel() {
        System.out.println("Nodes_9_SeriesParallel");

        startTimer();
        runAlgorithm("testFiles/Nodes_9_SeriesParallel.dot");
        endTimer();
    }

    public void Nodes_10_Random() {
        System.out.println("Nodes_10_Random");

        startTimer();
        runAlgorithm("testFiles/Nodes_10_Random.dot");
        endTimer();
    }

    public void Nodes_11_OutTree() {
        System.out.println("Nodes_11_OutTree");

        startTimer();
        runAlgorithm("testFiles/Nodes_11_OutTree.dot");
        endTimer();
    }

    private void runAlgorithm(String fileName) {
        InputImp input = new InputImp(fileName, core);
        Conversion conversion = new ConversionImp(input);

        DAG dag = conversion.getDAG();

        AlgorithmImp alg = new AlgorithmImp(dag, input.getProcessorCount());
        //Output output = new Output (alg.getCurrentBestSchedule(), fileName);
        //output.printOutput();
        System.out.println("BestTime: " + alg.getBestTotalTime());
		System.out.println("Recursive Calls: " + alg.getRecursiveCalls());
	}

    private void startTimer() {
        startTime = System.currentTimeMillis();
    }

    private void endTimer() {
        final long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime)  + "\n");
    }
}
