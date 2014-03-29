package pt.up.fe.sdis.proj1.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;

import java.awt.Color;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.gui.utils.GuiUtils;
import net.miginfocom.swing.MigLayout;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class FileBackupDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private final JPanel contentPanel = new JPanel();
    private JTextField backupFilePath_txt;
    private JSpinner replicationDegree_spn;
    private boolean _succeed = false;
    
    /**
     * Create the dialog.
     */
    public FileBackupDialog(JFrame frame, final BackupSystem backupSystem) {
        setResizable(false);
        setModal(true);
        setBounds(100, 100, 426, 143);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[20px][86px][47px][94px][31px]", "[20px][]"));
        {
            JLabel lblFile = new JLabel("File:");
            contentPanel.add(lblFile, "cell 0 0,alignx left,aligny center");
        }
        {
            backupFilePath_txt = new JTextField();
            backupFilePath_txt.setEditable(false);
            String userHome = System.getProperty("user.home");
            if (userHome != null) 
                backupFilePath_txt.setText(userHome);
            contentPanel.add(backupFilePath_txt, "cell 1 0 3 1,growx,aligny top");
            backupFilePath_txt.setColumns(10);
        }
        {
            JLabel lblbrowse = new JLabel("<html><u>Browse...</u></html>");
            lblbrowse.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent arg0) {
                    File defaultDir = new File(backupFilePath_txt.getText());
                    if (!defaultDir.exists())
                        defaultDir = defaultDir.getParentFile();
                    
                    JFileChooser fileChooser = new JFileChooser(defaultDir);
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    int returnVal = fileChooser.showOpenDialog(FileBackupDialog.this);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        backupFilePath_txt.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });
            lblbrowse.setForeground(Color.BLUE);
            contentPanel.add(lblbrowse, "cell 4 0,alignx left,aligny center");
        }
        {
            JLabel lblReplicationDegree = new JLabel("Replication Degree:");
            contentPanel.add(lblReplicationDegree, "cell 0 1,alignx left,aligny center");
        }
        {
            replicationDegree_spn = new JSpinner();
            int rd = backupSystem.getDefaultReplicationDegree();
            if (rd < 1 || rd > 9) rd = 1;
            replicationDegree_spn.setModel(new SpinnerNumberModel(rd, 1, 9, 1));
            contentPanel.add(replicationDegree_spn, "cell 1 1,growx,aligny top");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        File f = new File(backupFilePath_txt.getText());
                        _succeed = f.exists() && f.isFile();
                        if (!_succeed) {
                            JOptionPane.showMessageDialog(FileBackupDialog.this,
                                    "Invalid file selected.",
                                    "Error!",
                                    JOptionPane.ERROR_MESSAGE);
                        } else 
                            dispose();
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        _succeed = false;
                        dispose();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
            
            GuiUtils.setSystemLookAndFeel();
            
            pack();
            setLocation((int)frame.getBounds().getCenterX() - this.getWidth() / 2,
                        (int)frame.getBounds().getCenterY() - this.getHeight() / 2);
        }
    }

    public boolean succeed() {
        return _succeed;
    }
    
    public String getBackupFilePath() { 
         return backupFilePath_txt.getText(); 
    }
    
    public Integer getBackupReplicationDegree() { 
        return (Integer)replicationDegree_spn.getValue();
    }
    
}
