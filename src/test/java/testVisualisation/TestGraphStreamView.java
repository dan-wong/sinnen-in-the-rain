package testVisualisation;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.graphstream.graph.Node;
import org.junit.Test;

import implementations.io.ConversionImp;
import implementations.algorithm.AlgorithmImp;
import implementations.io.InputImp;
import interfaces.io.Conversion;
import interfaces.io.Input;
import interfaces.structures.DAG;
import interfaces.structures.NodeSchedule;
import visualisation.GraphStreamView;

public class TestGraphStreamView {

	//@Test
	public void testStartUp(){
		Input input = new InputImp("test.dot", "2");
		Conversion conversion = new ConversionImp(input);

		//Create a GraphStreamView object to make a DAG visual
		DAG dag = conversion.getDAG();
		GraphStreamView g = new GraphStreamView(dag,input.getProcessorCount());
		interfaces.structures.Node test = dag.getNodeByName("a");

		Timer timer = new Timer();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				AlgorithmImp alg = new AlgorithmImp(dag, input.getProcessorCount());
				HashMap<String,NodeSchedule> currentSchedule = alg.getCurrentBestSchedule();


				Iterator itSchedule = currentSchedule.entrySet().iterator();
				while (itSchedule.hasNext()) {
					Map.Entry pair = (Map.Entry)itSchedule.next();

					HashMap<String,Node> mapping = g.getStringNodeHashMap();			
					Node currentNode = mapping.get((String) pair.getKey());

					//public static view :(
					currentNode.addAttribute("ui.color", GraphStreamView._processorColours.get(((NodeSchedule) pair.getValue()).getBestProcessor()-1));
					currentNode.addAttribute("bestStartTime", ((NodeSchedule) pair.getValue()).getBestStartTime());
				}



			}

		}, 2*1000);

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

}
