package testVisualisation;

import implementations.io.ConversionImp;
import implementations.algorithm.AlgorithmImp;
import implementations.io.InputImp;
import implementations.io.OutputImp;
import interfaces.io.Conversion;
import interfaces.algorithm.Algorithm;
import interfaces.io.Input;
import interfaces.structures.DAG;
import visualisation.GraphView;
import visualisation.GraphViewImp;
import visualisation.TableModel;

public class VisualMain {
	public static void main(String args[]) {
		/*Input input = new InputImp(args[0], args[1]);
		Conversion conversion = new ConversionImp(input);

		DAG dag = conversion.getDAG();

		AlgorithmImp alg = new AlgorithmImp(dag, input.getProcessorCount());
		TableModel _schedule = new TableModel(alg,dag,input.getProcessorCount());
		
		OutputImp outputImp = new OutputImp(alg.getCurrentBestSchedule(), args[0]);
		outputImp.printOutput();
		GraphView schedule = new GraphViewImp(_schedule);
		/*try        
		{
		    Thread.sleep(2000);
		} 
		catch(InterruptedException ex) 
		{
		    Thread.currentThread().interrupt();
		}
		String[][] newdata = { { "0", "A", "" },
				{ "1", "A", "" },
				{ "2", "B", "" },
				{ "3", "B", "" },
				{ "4", "", "C" } };
		_schedule.changeData(newdata);
		_schedule.fireTableDataChanged();*/
	}
}
