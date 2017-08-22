package visualisation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import implementations.algorithm.AlgorithmImp;
import interfaces.structures.DAG;
import interfaces.structures.NodeSchedule;

@SuppressWarnings("serial")
public class TableModel extends AbstractTableModel {

	private static TableModel instance = null;
	protected TableModel() {
		// Exists only to defeat instantiation.
	}
	
	public static TableModel getInstance() {
		if(instance == null) {
			instance = new TableModel();
		}
		return instance;
	}

	private String[] _columnNames; // initialize based on number of processors.

	private String[][] _data; // initialize based on schedular object.
	private int _cores;
	private int _bestTime;// total best time possible - will represent number of rows.
	private HashMap<String, NodeSchedule> _map;
	private DAG _dag;
	
	/**
	 * Must be called the first time the singleton is initialized. Should only be called only once, since DAG and 
	 * cores don't change.
	 * @param alg AlgorithmImp object
	 * @param dag DAG object
	 * @param cores integer representing the number of cores. 
	 */
	public void initModel(HashMap<String, NodeSchedule> map, DAG dag, int cores){
		_cores = cores;
		_map = map;
		_dag = dag;
		_columnNames = initColumnNames();
		_data = new String[][]{};
	}
	/**
	 * Method is called when a better schedule object (with lower schedule time) is available. The TableModel is updated
	 * IMPORTANT: fireTableData must be called from the Controller (algorithm) following every call of this method.
	 * @param map
	 */
	public void changeData(HashMap<String, NodeSchedule> map, int betterTime){
		_bestTime = betterTime;
		_map = map;
		_data = initData();
		fireTableDataChanged();
	}

	public String[] initColumnNames(){
		String[] colNames = new String[_cores+1];
		colNames[0] = "Time";
		for (int i =1; i < _cores+1;i++){
			colNames[i] = "P" + i;
		}
		return colNames;
	}
	/**
	 * Method converts the bestSchedule HashMap to a 2D array, preparing it to be passed into a JTable.
	 * Effectively, the adapter method.
	 * @return
	 */
	public String[][] initData(){
		String[][] data = new String[_bestTime][_cores+1];
		// Initializing array with time values and empty strings.
		for (int i =0; i < _bestTime;i++){
			data[i][0] = (i + 1) + "";
			for (int j = 1; j < _cores+1;j++){
				data[i][j] = "";
			}
		}

		for (Entry<String, NodeSchedule> entry : _map.entrySet()) {

			String key = entry.getKey();
			NodeSchedule value = entry.getValue();
			int startTime = value.getBestStartTime();
			int core = value.getBestProcessor();
			int nodeWeight = _dag.getNodeByName(key).getWeight();

			// go through all rows which have the same node based on node weight.
			for (int i =0;i<nodeWeight;i++){
				data[startTime+i][core] = key.toUpperCase();
			}
		}
		return data;
	}

	@Override
	public int getRowCount() {
		return _data.length;
	}

	@Override
	public int getColumnCount() {
		return _columnNames.length;
	}


	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		String s = "";
		try {
			s = _data[rowIndex][columnIndex];
		} catch (Exception e){
			e.printStackTrace();
			printData(_data);
		}
		return s;
	}
	
	public void printData(String[][] array){
		System.out.println(Arrays.deepToString(array));
	}
	
	
	@Override
	public String getColumnName(int col) {
		return _columnNames[col];
	}

	/*
	 * Don't need to implement this method unless your table's
	 * data can change.
	 */
	public void setValueAt(Object value, int row, int col) {
		_data[row][col] = (String) value;
		fireTableCellUpdated(row, col);
	}

	public boolean isCellEditable(int row, int col){ 
		return false; 
	}

	public void changeData(String[][] newdata){
		_data = newdata;
	}

}
