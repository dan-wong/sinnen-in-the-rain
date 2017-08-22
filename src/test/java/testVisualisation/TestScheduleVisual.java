package testVisualisation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import implementations.io.ConversionImp;
import implementations.algorithm.AlgorithmImp;
import implementations.io.InputImp;
import implementations.io.OutputImp;
import interfaces.io.Conversion;
import interfaces.io.Input;
import interfaces.structures.DAG;
import visualisation.GraphView;
import visualisation.GraphViewImp;
import visualisation.TableModel;

public class TestScheduleVisual {
	/*public static final String FILENAME = "test2.dot";
	private AlgorithmImp alg;
	private OutputImp output;
	private DAG dag;
	private int _cores;
	private TableModel _schedule;
	
	@Before
	public void initialise(){
		Input input = new InputImp(FILENAME, "2");
		Conversion conversion = new ConversionImp(input);

		dag = conversion.getDAG();
		_cores = input.getProcessorCount();
		
		alg = new AlgorithmImp(dag, _cores);
		output = new OutputImp (alg.getCurrentBestSchedule(),FILENAME);
		_schedule = TableModel.getInstance();
		_schedule.initModel(alg, dag, _cores);
		
		//GraphView schedule = new GraphViewImp(_schedule);
	}
	
	@Test
    public void testColumnName() {
		String[] colNames = _schedule.initColumnNames();
		String[] expectedColNames = new String[] {"Time","P1","P2"};
		assertArrayEquals(expectedColNames,colNames);
    }
	
	//@Test
	public void testData(){
		String[][] expectedData = { { "1", "A", "" },
				{ "2", "A", "" },
				{ "3", "C", "" },
				{ "4", "C", "B" },
				{ "5", "C", "B" }, 
				{ "6", "", "B" },
				{ "7", "", "D" },
				{ "8", "", "D" }
		};
		String[][] data = _schedule.initData();
		assertArrayEquals(expectedData,data);
	}
	
	//@Test
	/*public void testData2(){
		String[][] expectedData = { { "1", "A", "" },
				{ "2", "A", "" },
				{ "3", "C", "" },
				{ "4", "C", "B" },
				{ "5", "C", "B" }, 
				{ "6", "", "B" },
				{ "7", "", "D" },
				{ "8", "", "D" }
		};
		String[][] data = _schedule.changeData();
		assertArrayEquals(expectedData,data);
	}*/
	
}
