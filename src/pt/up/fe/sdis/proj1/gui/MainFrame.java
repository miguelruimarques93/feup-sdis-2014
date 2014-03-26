package pt.up.fe.sdis.proj1.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import pt.up.fe.sdis.proj1.protocols.initiator.FileBackup;
import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.Pair;
import rx.Observer;
import rx.schedulers.Schedulers;

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

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private BackupSystem _backupSystem = null;

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
        setBounds(100, 100, 450, 300);
        setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
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
        
        final JProgressBar progressBar = new JProgressBar(0, 100);
        
        JMenuItem mntmBackup = new JMenuItem("Backup");
        mntmBackup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(MainFrame.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    FileBackup b = _backupSystem.backupFile(file);
                    
                    b.getProgressionObservable().observeOn(Schedulers.newThread()).subscribe(new Observer<Double>() {

                        @Override
                        public void onCompleted() {
                            System.out.println("Completed");
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
        
        JList list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(15);
        list.setModel(new AbstractListModel() {
            String[] values = new String[] {"Ola"};
            public int getSize() {
                return values.length;
            }
            public Object getElementAt(int index) {
                return values[index];
            }
        });
        contentPane.add(list, BorderLayout.WEST);
        
        
        contentPane.add(progressBar, BorderLayout.SOUTH);
        
        GuiUtils.setSystemLookAndFeel();
    }

    public void initializeBackupSystem(Pair<String, Integer> mc, Pair<String, Integer> mdb, Pair<String, Integer> mdr, InetAddress intf) throws IOException {
        _backupSystem = new BackupSystem(mc, mdb, mdr, intf);
    }

    @Override
    public void dispose() {
        if (_backupSystem != null) _backupSystem.shutdown();
        super.dispose();
    }
}
