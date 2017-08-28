import implementations.algorithm.AlgorithmImp;
import implementations.io.Conversion;
import implementations.io.InputImp;
import implementations.io.OutputImp;
import implementations.structures.DAGImp;
import interfaces.io.Output;

import org.junit.Test;

public class ApplicationTest {
    private long startTime;
    private String core = "2";

    @Test
    public void testWithTwoCores() {
        core = "2";

//        Nodes_4_Sequential();
		Nodes_7_OutTree();
		Nodes_8_Random();
		Nodes_9_SeriesParallel();
		Nodes_10_Random();
//		Nodes_11_OutTree();
    }

    @Test
    public void testWithFourCores() {
        core = "4";

//        Nodes_7_OutTree();
//        Nodes_8_Random();
//        Nodes_9_SeriesParallel();
//        Nodes_10_Random();
        Nodes_11_OutTree();
    }
    
    public void Nodes_4_Sequential() {
        System.out.println("Nodes_4_Sequential.dot");

        startTimer();
        runAlgorithm("testFiles/Nodes_4_Sequential.dot");
        endTimer();
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
        DAGImp.getNewInstance();
        Conversion conversion = new Conversion(input);

        AlgorithmImp alg = new AlgorithmImp(input.getProcessorCount(),true,8);
//        Output output = new OutputImp (alg.getCurrentBestSchedule(), fileName);
//        output.printOutput();
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