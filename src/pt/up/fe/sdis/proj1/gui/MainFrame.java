package pt.up.fe.sdis.proj1.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import pt.up.fe.sdis.proj1.protocols.initiator.FileBackup;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.Pair;
import rx.Observer;
import rx.schedulers.Schedulers;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.ListSelectionModel;
import javax.swing.JProgressBar;

import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import net.miginfocom.swing.MigLayout;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private BackupSystem _backupSystem = null;
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private JProgressBar progressBar;

    /**
     * Create the frame.
     * 
     * @throws IOException
     */
    public MainFrame(Pair<String, Integer> mc, Pair<String, Integer> mdb, Pair<String, Integer> mdr, InetAddress intf) throws IOException {
        initializeGUI();
        initializeBackupSystem(mc, mdb, mdr, intf);
    }

    protected MainFrame() {
        initializeGUI();
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void initializeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 970, 605);
        // setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        
        JMenuBar menuBar = new JMenuBar();
        contentPane.add(menuBar, BorderLayout.NORTH);
        
        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);
        
        JMenuItem mntmExit = new JMenuItem("Exit");
        mntmExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });
        mnFile.add(mntmExit);
        
        JMenuItem mntmBackup = new JMenuItem("Backup");
        mntmBackup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(MainFrame.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    FileBackup b = _backupSystem.backupFile(file);
                    
                    final Date start = new Date();
                    
                    b.getProgressionObservable().observeOn(Schedulers.newThread()).subscribe(new Observer<Double>() {
                        
                        @Override
                        public void onCompleted() {
                            System.out.println("Completed in " + (new Date().getTime() - start.getTime()) + " ms");
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(final Double t) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run() {
                                    progressBar.setValue((int)(t * 100.0));
                                }
                            });
                        }
                    });
                    
                    b.Send();
                }
            }
        });
        menuBar.add(mntmBackup);
        
        JPanel panel = new JPanel();
        panel.setSize(new Dimension(0, 10));
        panel.setMinimumSize(new Dimension(100, 500));
        contentPane.add(panel, BorderLayout.SOUTH);
        listModel = new DefaultListModel<String>();
        panel.setLayout(new MigLayout("", "[100px,grow]", "[100px][14]"));
        
        list = new JList<String>();
        list.setValueIsAdjusting(true);
        list.setModel(listModel);
        list.setPreferredSize(new Dimension(100, 100));
        panel.add(list, "cell 0 0,growx,aligny top");
        list.setSize(100, 500);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(34);
        list.setMinimumSize(new Dimension(100, 150));
        
        progressBar = new JProgressBar(0, 100);
        panel.add(progressBar, "cell 0 1,growx,aligny center");
        
        GuiUtils.setSystemLookAndFeel();
    }

    public void initializeBackupSystem(Pair<String, Integer> mc, Pair<String, Integer> mdb, Pair<String, Integer> mdr, InetAddress intf) throws IOException {
        _backupSystem = new BackupSystem(mc, mdb, mdr, intf);
        
        if (_backupSystem != null) {
            _backupSystem.Files.setFileListener(new BackupSystem.BackupFileListener() {
                
                @Override
                public void FileRemoved(String filePath) {
                    listModel.removeElement(filePath);              
                    list.setModel(listModel);
                }
                
                @Override
                public void FileAdded(String filePath) {
                    listModel.addElement(filePath);
                    list.setModel(listModel);
                }
            });
        }
    }

    @Override
    public void dispose() {
        if (_backupSystem != null) _backupSystem.shutdown();
        super.dispose();
    }
}
