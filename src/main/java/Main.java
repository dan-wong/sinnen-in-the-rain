import implementations.algorithm.AlgorithmImp;
import implementations.io.ConversionImp;
import implementations.io.InputImp;
import interfaces.Conversion;
import interfaces.algorithm.Algorithm;
import interfaces.io.Input;
import interfaces.structures.DAG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
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
            boolean contains_v = false;
            boolean contains_p = false;
            boolean contains_o = false;

            for (int i = 0; i < argsList.size() - 1; i++) {
                String str = argsList.get(i);

                if (str.contains("-v")) {
                    contains_v = true;
                } else if (str.contains("-p")) {
                    contains_p = true;

                    final String noOfParallerCores = argsList.get(i + 1);
                    try {
                        // test if it is an int
                        Integer.parseInt(noOfParallerCores);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("no Of parallel cores not a valid integer");
                    }

                } else if (str.contains("-o")) {
                    String outputFile = argsList.get(i + 1);
                    contains_o = true;
                }
            }
        }

        Input input = new InputImp(filePath, noOfProcessors);

        Conversion conversion = new ConversionImp(input);

        DAG dag = conversion.getDAG();

        Algorithm alg = new AlgorithmImp(dag, input.getProcessorCount());
    }
}
