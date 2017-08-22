package visualisation;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import implementations.io.ConversionImp;
import implementations.algorithm.AlgorithmImp;
import implementations.io.InputImp;
import interfaces.io.Conversion;
import interfaces.io.Input;
import interfaces.structures.DAG;
import interfaces.structures.NodeSchedule;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * A class to test the JGraphT library for the MVC view 
 * @author dariusau
 *
 */
public class GraphStreamView extends JFrame implements GraphView {
	
	private static HashMap<interfaces.structures.Node,Node> _graphNodeNodeMap;
	private static HashMap<String,Node> _graphStringNodeMap;
	public static List<Color> _processorColours;
	private static DAG _dag;
	private static int _processorCount;
	private static Graph _GRAPH;
	
	
	/**
	 * GraphStreamView constructor
	 * @param dag
	 * @param processorCount
	 */
	public GraphStreamView(DAG dag, int processorCount){
		
		//Initialising fields
		_graphNodeNodeMap = new HashMap<interfaces.structures.Node,Node>();
		_graphStringNodeMap = new HashMap<String,Node>();
		_processorColours = new ArrayList<Color>();
		_processorCount = processorCount;
		
		//Gets random colours for different processors
		setProcessorColours();
		_dag = dag;
		
		List<interfaces.structures.Node> nodes = dag.getAllNodes();
		
		
		//Create a graph visual
		_GRAPH = new SingleGraph("Tutorial 1");
		
		//Stylesheet settings - needs to be modified
		_GRAPH.setAttribute("stylesheet", 
				"node { "
		        + "     shape: rounded-box; "
		        + "     padding: 5px;"
		        + "		size: 20px; "
		        + "     fill-mode: dyn-plain; "
		        + "     stroke-mode: plain; "
		        + "		stroke-color: black;"
		        + "		stroke-width: 5;"
		        + "     size-mode: fit;"
		        + "} "
		        + "edge { "
		        + "     shape: freeplane;"
		        + "		fill-color: black; "
		        + "}"
		        + "edge.marked { "
		        + "     fill-color: black;"
		        + "}");
		
		//Loop through all nodes
		for (interfaces.structures.Node currentNode : nodes){
			Node graphNode = _GRAPH.addNode(currentNode.getName());
			
			//Add to hashmap mapping our Node objects to GraphStream's Node objects
			_graphNodeNodeMap.put(currentNode, graphNode);
			
			//Add to hashmap mapping node names to GraphStream's Node objects
			_graphStringNodeMap.put(currentNode.getName(), graphNode);
			
			graphNode.setAttribute("ui.label", currentNode.getName());
			
		}
		
		//Looping through all nodes
		Iterator it = _graphNodeNodeMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			interfaces.structures.Node dagNode = (interfaces.structures.Node) pair.getKey();
			List<interfaces.structures.Node> successors = dagNode.getSuccessors();
		    
			//Drawing directed arcs from current node to all predecessors
		    for (interfaces.structures.Node succ : successors){
		    	String arcName = dagNode.getName() + succ.getName();
		    	_GRAPH.addEdge(arcName,dagNode.getName(),succ.getName(),true);
		    }
		}
		
		//Display the graph with auto layout
		//Viewer viewer = _GRAPH.display(false);
		
	}
	@Override
	public void addButtonListener(ActionListener listener) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Interesting method??? Review this method if needed
	 * @param node
	 */
	public void changeNodeColour(interfaces.structures.Node node){
		Node graphNode = _graphNodeNodeMap.get(node);
		graphNode.setAttribute("ui.class","marked");
	}
	
	/**
	 * method to return GraphStream graph in a JPanel. Must call the constructor before calling this.
	 * @author Pulkit
	 * @return
	 */
	public JPanel getPanel(){
		JPanel jp = new JPanel();
		Viewer viewer = new Viewer(_GRAPH, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		
		viewer.enableAutoLayout();
        ViewPanel viewPanel = viewer.addDefaultView(false);
        //viewPanel.setVisible(true);
        jp.add(viewPanel);
        //jp.setVisible(true);
        return jp;
	}
	/**
	 * Method to assign different random colours to every processor
	 * https://stackoverflow.com/questions/4246351/creating-random-colour-in-java
	 */
	private void setProcessorColours(){
		Random randomNum = new Random();
		for (int i=0; i<_processorCount; i++){
			float r = (float) (randomNum.nextFloat()/2f + 0.2);
			float g = (float) (randomNum.nextFloat()/2f + 0.2);
			float b = (float) (randomNum.nextFloat()/2f + 0.2);
			
			Color newColour = new Color(r,g,b);
			_processorColours.add(newColour);
		}
	}



	@Override
	public JTable getTable() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Getter for name-to-gsnode hashmap
	 * @return
	 */
	public HashMap<String,Node> getStringNodeHashMap(){
		return _graphStringNodeMap;
	}
	
	
	

	
}