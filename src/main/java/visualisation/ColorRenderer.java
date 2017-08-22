package visualisation;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
/**
 * Visualisation Renderer
 * @author Pulkit
 *
 */
@SuppressWarnings("serial")
public class ColorRenderer extends JLabel implements TableCellRenderer {
	
	private HashMap<String, Color> colourMatrix;
	private List<Color> colorList;
	public ColorRenderer() {
		setOpaque(true); //MUST do this for background to show up.
		colorList = new ArrayList<Color>();
		colourMatrix = new HashMap<String, Color>();
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		
		JLabel cellLabel = this;
		cellLabel.setOpaque(true);
		//Get the status for the current row.
		TableModel gModel = (TableModel) table.getModel();
		String rowVal = String.valueOf(gModel.getValueAt(row,column));  
		// temporary fix, should be a better way.
		cellLabel.setText(rowVal);
		cellLabel.setHorizontalAlignment(JLabel.CENTER);
		
		if (rowVal.isEmpty()){
			cellLabel.setBackground(Color.WHITE);
		}
		else if (colourMatrix!=null && colourMatrix.containsKey(rowVal)){
			cellLabel.setBackground(colourMatrix.get(rowVal));
		} else {
			Color colour = getRandomColour();
			while (!colorList.isEmpty() && colorList.contains(colour)){
				colour = getRandomColour();
			}
			colorList.add(colour);
			colourMatrix.put(rowVal, getRandomColour());
			cellLabel.setBackground(colourMatrix.get(rowVal));
		}
		

		return cellLabel;
	}
	/**
	 * Get random color that is not too dark.
	 * @return Color with all R,G and B values between 0 and 153.
	 */
	public Color getRandomColour(){
		int minHue = 100;
		int maxHue = 255;
		
		Random rand = new Random();
		int r = rand.nextInt((maxHue - minHue) + 1) + minHue;
		int g = rand.nextInt((maxHue - minHue) + 1) + minHue;
		int b = rand.nextInt((maxHue - minHue) + 1) + minHue;
		
		return new Color(r, g, b);
	}
}
