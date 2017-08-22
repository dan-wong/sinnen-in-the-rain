package visualisation;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import interfaces.structures.DAG;
import net.miginfocom.swing.MigLayout;
/**
 * Common GUI Interface to be used to display all visualisation components.
 * @author Pulkit
 *
 */
@SuppressWarnings("serial")
public class ComboView extends JFrame {
	
	private JPanel _contentPane;
	private JPanel _panelLeft;
	private JPanel _panelMiddle;
	private JPanel _panelRight;
	private TableModel _tableModel;
	private DAG _dag;
	private int _cores;

	/**
	 * Create the frame.
	 * @param _numberOfCores 
	 * @param _dag 
	 */
	public ComboView(TableModel tableModel, DAG dag, int numberOfCores) {
		_tableModel = tableModel;
		_dag = dag;
		_cores = numberOfCores;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 700);
		_contentPane = buildPanel();
		_contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(_contentPane);
		
		GraphViewImp table = new GraphViewImp(_tableModel);
		// set scrolling to bottom of table
		JScrollPane pane = table.getPane();
		/*JScrollBar vertical = pane.getVerticalScrollBar();
		vertical.setValue(vertical.getMaximum());*/
		_panelMiddle.add(pane);
		
		GraphStreamView gv = new GraphStreamView(_dag,_cores);
		/*JFrame f = new JFrame();
		f.add(gv);
		f.setVisible(true);*/
		
		JPanel p = gv.getPanel();
		p.setVisible(true);
		_panelLeft.add(p);
		
		//pack();
        setLocationRelativeTo(null);

    	setVisible(true);

	}
	
	
	private JPanel buildPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fill"));
        buildView(panel);
        return panel;
    }

    private void buildView(JPanel panel) {
    	JLabel titleLabel = new JLabel("Sinnen-In-The-Rain GUI");
    	titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));

        JLabel progressLabel = new JLabel("Progress Label");
        JProgressBar progressBar = new JProgressBar(0,100);
        progressBar.setValue(20);
        
        // JPanel for Graph Stream:
        _panelLeft = new JPanel();
        _panelLeft.setLayout(new BorderLayout());
        
        // JPanel for JTable:
        _panelMiddle = new JPanel();
        _panelMiddle.setLayout(new BorderLayout());
        
        // JPanel for Statistics:
        _panelRight = new JPanel();
        _panelRight.setLayout(new BorderLayout());
        _panelRight.add(new JLabel("Statistics"),BorderLayout.NORTH);
        _panelRight.add(new JLabel("Time Elapsed: 0:10"),BorderLayout.SOUTH);

        JButton stopBttn = new JButton("Stop Process");
        JButton helpBttn = new JButton("Help");
        JButton quitBttn = new JButton("Quit");
    	
        panel.add(titleLabel, "span, center, height 10%");

        //wrap keyword starts a new row
        panel.add(progressLabel, "align label, height 10%");
        panel.add(progressBar, "wrap");

        //align label triggers platform-specific label alignment
        panel.add(_panelLeft, "width 50%,height 70%");
        panel.add(_panelMiddle, "width 38%, height 70%");
        panel.add(_panelRight, "wrap, width 12%, height 70%");

        //tag identifies the type of button
        panel.add(stopBttn, "tag ok, span, split 3, sizegroup bttn, height 10%");

        //sizegroups set all members to the size of the biggest member
        panel.add(helpBttn, "tag help2, sizegroup bttn");
        panel.add(quitBttn, "tag ok, sizegroup bttn");
    }

}
