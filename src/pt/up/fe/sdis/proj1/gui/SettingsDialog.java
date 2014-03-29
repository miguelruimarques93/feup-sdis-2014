package pt.up.fe.sdis.proj1.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;
import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.gui.utils.GuiUtils;
import pt.up.fe.sdis.proj1.utils.NetworkUtils;

public class SettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private final JPanel contentPanel = new JPanel();

    /**
     * Create the dialog.
     */
    public SettingsDialog(JFrame frame, BackupSystem bs) {
        super(frame, "Settings", true);
        
        _backupSystem = bs;
        Long space = _backupSystem.getTotalSpace();
        Unit unit = Unit.getAppropiateUnit(space);
        
        setResizable(false);
        setBounds(100, 100, 476, 238);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[132px][grow][][5px][50px,grow][][][5px][79px][]", "[20px][26px][][]"));
        {
            JLabel lblDafaultReplicationDegree = new JLabel("Dafault Replication Degree:");
            contentPanel.add(lblDafaultReplicationDegree, "cell 0 0,alignx left,aligny center");
        }
        {
            rdspinner = new JSpinner();
            rdspinner.setMinimumSize(new Dimension(100, 20));
            rdspinner.setPreferredSize(new Dimension(50, 20));
            rdspinner.setModel(new SpinnerNumberModel(_backupSystem.getDefaultReplicationDegree(), 1, 9, 1));
            contentPanel.add(rdspinner, "cell 1 0,alignx left,aligny top");
        }
        {
            JLabel lblAvailableSpace = new JLabel("Available Space:");
            contentPanel.add(lblAvailableSpace, "cell 0 1,alignx left,aligny center");
        }
        {
            spaceSpinner = new JSpinner();
            spaceSpinner.setMinimumSize(new Dimension(100, 20));
            spaceSpinner.setModel(new SpinnerNumberModel(new Long(space / Unit.getMultiplier(unit)), new Long(0), new Long(9000000), new Long(1)));
            spaceSpinner.setPreferredSize(new Dimension(50, 20));
            contentPanel.add(spaceSpinner, "cell 1 1");
        }
        {
            multComboBox = new JComboBox<Unit>();
            multComboBox.setModel(new DefaultComboBoxModel<Unit>(Unit.values()));
            multComboBox.setSelectedItem(unit);
            contentPanel.add(multComboBox, "cell 2 1,growx");
        }
        {
            JLabel lblProtocolVersion = new JLabel("Protocol Version:");
            contentPanel.add(lblProtocolVersion, "cell 0 2,alignx left");
        }
        {
            protocolVersion_cmb = new JComboBox<Integer>();
            protocolVersion_cmb.setModel(new DefaultComboBoxModel<Integer>(new Integer[] {1, 2}));
            protocolVersion_cmb.setSelectedItem(_backupSystem.getProtocolVersion());
            contentPanel.add(protocolVersion_cmb, "cell 1 2,growx");
        }
        {
            JLabel lblRestorePort = new JLabel("Restore Port:");
            contentPanel.add(lblRestorePort, "cell 0 3");
        }
        {
            restorePort_spn = new JSpinner();
            restorePort_spn.setModel(new SpinnerNumberModel(_backupSystem.getRestorePort(), NetworkUtils.MINIMUM_PORT, NetworkUtils.MAXIMUM_PORT, 1));
            restorePort_spn.setEditor(new JSpinner.NumberEditor(restorePort_spn, "#"));
            
            contentPanel.add(restorePort_spn, "cell 1 3,growx");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {
                            long multiplier = Unit.getMultiplier((Unit) multComboBox.getSelectedItem());
                            long avSpace = (Long)spaceSpinner.getValue();
                            int defaultRd = (Integer)rdspinner.getValue();
                            int protocolVersion = (Integer)protocolVersion_cmb.getSelectedItem();
                            int restorePort = (Integer)restorePort_spn.getValue();
                            
                            _backupSystem.setDefaultReplicationDegree(defaultRd);
                            _backupSystem.setTotalSpace(multiplier * avSpace);
                            _backupSystem.setProtocolVersion(protocolVersion);
                            _backupSystem.setRestorePort(restorePort);
                            
                            dispose();
                        } catch (Exception ex) {
                        }
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        dispose();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        
        GuiUtils.setSystemLookAndFeel();
        
        pack();
        setLocation((int)frame.getBounds().getCenterX() - this.getWidth() / 2,
                    (int)frame.getBounds().getCenterY() - this.getHeight() / 2);
    }

    
    private BackupSystem _backupSystem;
    private JSpinner spaceSpinner;
    private JSpinner rdspinner;
    private JComboBox<Unit> multComboBox;
    private JSpinner restorePort_spn;
    private JComboBox<Integer> protocolVersion_cmb;
}
