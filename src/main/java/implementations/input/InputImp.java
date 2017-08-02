package implementations.input;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import interfaces.Input;

/**
 * This class is in the module responsible for taking input files and 
 * do some basic pre-processing.
 *  
 * It will implement the Input interface for the conversion module to
 * convert the input into a DAG.
 * @author Victor
 *
 */
public class InputImp implements Input {
	private int _noOfProcessors;
	private List<String[]> _graphData = new ArrayList<String[]>();
	
	/**
	 * The constructor will take arguments directly from the command line.
	 * @param filePath
	 * @param noOfProcessors
	 */
	public InputImp (String filePath, String noOfProcessors) {
		_noOfProcessors = Integer.parseInt(noOfProcessors);
		processFile(filePath);
	}

	/**
	 * See {@link Input#getProcessorCount()};
	 */
	@Override
	public int getProcessorCount() {
		return _noOfProcessors;
	}
	
	/**
	 * See {@link Input#getGraphData()}
	 */
	@Override
	public List<String[]> getGraphData() {
		return _graphData;
	}
	
	/**
	 * This helper method will read from the input file, and create the List<String> needed
	 * for graph data.
	 */
	private void processFile(String filePath) {
		File file = new File (filePath);
		try {
			Scanner inputScanner = new Scanner (file);
			
			inputScanner.nextLine(); //ignore line1.
			
			//Loop to read all lines. Only exit if reaches a single line with closing bracket }.
			while (true) {
				String line = inputScanner.nextLine();
				
				//Final line escape.
				if (line.trim().equals("}")) {
					break;
				}
				
				String[] lineArray = line.split("\\[");
				
				//Processing the name (index 1)
				lineArray[0] = lineArray[0].trim();				
				if (lineArray[0].length() == 1) {
					//Do nothing (single node already)
				}
				else { //For an arc
					String[] arcSplitted = lineArray[0].split("->");
					lineArray[0] = arcSplitted[0].trim() + " " + arcSplitted[1].trim();
				}
				
				//Processing the weight (index 2)
				lineArray[1] = lineArray[1].replaceAll("\\D", "");
				
				_graphData.add(lineArray);				
			}
			
			inputScanner.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

}
