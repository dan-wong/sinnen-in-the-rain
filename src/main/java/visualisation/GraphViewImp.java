package visualisation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class GraphViewImp extends JFrame implements GraphView {
	private JTable table;
	//private JButton addColumnBtn;
	
	public GraphViewImp(TableModel tableModel) {
		setLayout(new FlowLayout());
		TableCellRenderer colorRenderer = new ColorRenderer();
		table = new JTable(tableModel){
			public TableCellRenderer getCellRenderer(int row, int column) {
				if ((column != 0)) {
		        	return colorRenderer;
		        }
		        return super.getCellRenderer(row, column);
		    }
		};
    
		//table.scrollRectToVisible(table.getCellRect(table.getRowCount()-1, 0, true));
		table.setFillsViewportHeight(true);
	}
	
	public JScrollPane getPane(){
		JScrollPane pane;
		if (table!=null){
			pane = new JScrollPane(table);
			add(pane, BorderLayout.CENTER);
		} else {
			throw new RuntimeException("Table not initialised. Constructor for Table Model not called"
					+ " or did not execute as expected.");
		}

		return pane;
	}
	
	public JTable getTable(){
		return table;
	}

	@Override
	public void addButtonListener(ActionListener listener) {
		// TODO Auto-generated method stub
		
	}
}
