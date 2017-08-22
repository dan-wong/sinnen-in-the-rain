import implementations.algorithm.AlgorithmImp;
import implementations.io.ConversionImp;
import implementations.io.InputImp;
import implementations.io.OutputImp;
import interfaces.algorithm.Algorithm;
import interfaces.io.Conversion;
import interfaces.io.Input;
import interfaces.structures.DAG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    // these for for future use, not used at the moment
    private static boolean visualisation = false;
    private static boolean processorInfo = false;
    private static boolean outputSpec = false;
    private static String outputPath;

    public static void main(String args[]) {
        //java jar scheduler.jar INPUT.dot P [OPTION]
        //Optional :
        //-p N
        //-V
        //-o OUTPUT

        //convert to ArrayList
        List<String> argsList = new ArrayList<>(Arrays.asList(args));

        // check filepath of .dot file
        final String filePath = argsList.get(0);
        if (!filePath.contains(".dot")) {
            throw new IllegalArgumentException("filePath doesn't contain .dot file.");
        }

        //check no. Of Processors is a valid integer
        final String noOfProcessors = argsList.get(1);
        try {
            //test if it is an int
            Integer.parseInt(noOfProcessors);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("no Of Processors not a valid integer");
        }

        //optional options
        //hard-coded for now
        if (argsList.size() > 2) {


            for (int i = 0; i < argsList.size() - 1; i++) {
                String str = argsList.get(i);

                if (str.contains("-v")) {
                    visualisation = true;
                } else if (str.contains("-p")) {
                    processorInfo = true;

                    final String noOfParallerCores = argsList.get(i + 1);
                    try {
                        // test if it is an int
                        Integer.parseInt(noOfParallerCores);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("no Of parallel cores not a valid integer");
                    }

                } else if (str.contains("-o")) {
                    outputPath = argsList.get(i + 1);
                    outputSpec = true;
                }
            }
        }

        Input input = new InputImp(filePath, noOfProcessors);

        Conversion conversion = new ConversionImp(input);

        DAG dag = conversion.getDAG();

        Algorithm alg = new AlgorithmImp(dag, input.getProcessorCount());

        OutputImp outputImp;

        if (outputSpec){
            outputImp = new OutputImp(alg.getCurrentBestSchedule(), filePath, outputPath);
        } else {
            outputImp = new OutputImp(alg.getCurrentBestSchedule(), filePath);
        }

        outputImp.outputToFile();
    }
}

