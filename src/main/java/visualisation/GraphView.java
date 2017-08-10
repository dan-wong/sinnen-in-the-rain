package visualisation;

import java.awt.event.ActionListener;

import javax.swing.JTable;

/**
 * This interface allows different for views using different libraries to be plugged into the 
 * MVC architecture.
 * 
 * @author dariusau pulkitkalra.
 * 
 * 
 *
 */
public interface GraphView {
	// We don't need a model!
	//Adds an ActionListener - the controller of the MVC structure
	public void addButtonListener(ActionListener listener);
	
	//Gets the table - the model of the MVC structure
	public JTable getTable();
	

}
