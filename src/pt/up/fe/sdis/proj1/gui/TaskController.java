package pt.up.fe.sdis.proj1.gui;

import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

public class TaskController extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JLabel _lblTaskName;
    private JLabel _lblState;
    private JProgressBar _prgProgression;
    private JButton _btnCancel;

    /**
     * Create the panel.
     */
    public TaskController(String taskName, String initialState) {
        setBorder(UIManager.getBorder("TextPane.border"));
        MigLayout layout = new MigLayout("", "[68px,grow][42px,grow][146px,grow][39px]", "[23px][]");
        setLayout(layout);
        
        _lblTaskName = new JLabel(taskName);
        add(_lblTaskName, "cell 0 0,alignx left,aligny center");
        
        _lblState = new JLabel(initialState);
        add(_lblState, "cell 1 0,alignx left,aligny center");
        
        _prgProgression = new JProgressBar();
        _prgProgression.setMinimum(0);
        _prgProgression.setMaximum(100);
        _prgProgression.setValue(0);
        add(_prgProgression, "cell 0 1 3 1,growx,aligny center");
        
        _btnCancel = new JButton("X");
        add(_btnCancel, "cell 3 1,alignx left,aligny center");
        
        Dimension min = layout.minimumLayoutSize(this);
        Dimension prefered = layout.preferredLayoutSize(this);
        Dimension max = layout.maximumLayoutSize(this);
        setPreferredSize(new Dimension(prefered.width, min.height));
        setMaximumSize(new Dimension(max.width, min.height));
    }
    
    public void addCancelListener(ActionListener lis) {
        _btnCancel.addActionListener(lis);
    }
    
    public void removeCancelListener(ActionListener lis) {
        _btnCancel.removeActionListener(lis);
    }
    
    public void setProgressBarValue(int value) {
        _prgProgression.setValue(value);
    }
    
    public void setState(String st) {
        _lblState.setText(st);
    }

}
