package implementations.io;

import interfaces.io.Output;
import interfaces.structures.NodeSchedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * This class takes the result from the algorithm module, and 
 * prints this output to a .dot file.
 *
 * The output file follows the exact format (and order) as the input file 
 * by reading from the input line-by-line and appending information if necessary. 
 *
 * The user can specify a name for the output .dot file through the commandline
 * argument. If this is not provided, the default name is to append "_schedule" 
 * to the end of the input file name.
 *
 * @author Victor
 *
 */
public class OutputImp implements Output {
	private HashMap<String, NodeSchedule> _bestSchedule;
	private String _inputPath;
	private String _outputPath;
	public final static String DEFAULT_OUT = "_schedule.dot";
	public final static String NEWLINE = System.getProperty("line.separator");

	/**
	 * The best schedule hash map should come from the result of running the algorithm.
	 * @param bestSchedule
	 * @param inputPath
	 */
	public OutputImp(HashMap<String, NodeSchedule> bestSchedule, String inputPath) {
		this (bestSchedule, inputPath, null);
	}

	public OutputImp(HashMap<String, NodeSchedule> bestSchedule, String inputPath, String outputPath) {
		_bestSchedule = bestSchedule;
		_inputPath = inputPath;
		_outputPath = outputPath;
	}

	public void printOutput() {
		System.out.println(generateStringOutput());
	}

	public void outputToFile() {
		String output = generateStringOutput();
		String outputPath;

		//don't use default if there is a specified output path
		if (_outputPath != null) {
			outputPath = _outputPath;
		} else {
			outputPath = _inputPath.substring(0, _inputPath.length() - 4) + DEFAULT_OUT;
		}
		try {
			PrintWriter writer = new PrintWriter(outputPath, "UTF-8");
			writer.println(output);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private String generateStringOutput() {
		File file = new File (_inputPath);
		StringBuilder sb = new StringBuilder();

		try {
			Scanner inputScanner = new Scanner (file);

			//Append "output" to the digraph name.
			String firstLine = inputScanner.nextLine();
			int qutIndex = firstLine.indexOf("\"");
			sb.append(firstLine.substring(0, qutIndex+1));
			sb.append("output");
			sb.append(firstLine.substring(qutIndex+1, qutIndex+2).toUpperCase());
			sb.append(firstLine.substring(qutIndex+2, firstLine.length()));
			sb.append(NEWLINE);

			//Loop to read all lines. Only exit if reaches a single line with closing bracket }.
			while (true) {
				String line = inputScanner.nextLine();

				//Final line escape.
				if (line.trim().equals("}")) {
					sb.append(line);
					break;
				}

				String[] lineArray = line.split("\\[");

				//Processing the name (index 1)
				lineArray[0] = lineArray[0].trim();
				if (!lineArray[0].contains("->")) {//For a node
					String nameOfNode = lineArray[0].trim();
					NodeSchedule ns = _bestSchedule.get(nameOfNode);

					int sqbkIndex = line.indexOf("]");
					sb.append(line.substring(0, sqbkIndex));
					sb.append(",Start=" + ns.getBestStartTime() + ",Processor=" + ns.getBestProcessor());
					sb.append(line.substring(sqbkIndex, line.length()));
					sb.append(NEWLINE);

				} else { //For an arc
					sb.append(line);
					sb.append(NEWLINE);
				}
			}
			inputScanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}
}
