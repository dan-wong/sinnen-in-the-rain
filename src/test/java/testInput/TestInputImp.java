package testInput;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import implementations.input.InputImp;
import interfaces.Input;

/**
 * This class tests the InputImp under the input module
 * @author Victor
 *
 */
public class TestInputImp {
	public static final String FILENAME = "test.dot";
	
    /**
     * Correct process count
     */
	@Test
    public void testProcessorNumber() {
        Input input = new InputImp(FILENAME, "3");
        assertEquals(input.getProcessorCount(), 3);
    }
	
	/**
	 * Correct line 1 (which is a node).
	 */
    @Test
    public void testNode() {
    	Input input = new InputImp(FILENAME, "3");
    	List<String[]> graph = input.getGraphData();
    	assertEquals(graph.get(0)[0], "a"); //First row, first col, should be node A.
    	assertEquals(graph.get(0)[1], "2"); 
    }
    
	/**
	 * Correct line 3 (which is an arc).
	 */
    @Test
    public void testArc() {
    	Input input = new InputImp(FILENAME, "3");
    	List<String[]> graph = input.getGraphData();
    	assertEquals(graph.get(2)[0], "a b"); //Third row, first col, should be Arc a->b.
    	assertEquals(graph.get(2)[1], "1"); 
    }

}
