package pt.up.fe.sdis.proj1.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import net.miginfocom.swing.MigLayout;
import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.FileVersion;
import pt.up.fe.sdis.proj1.gui.utils.GuiUtils;
import pt.up.fe.sdis.proj1.protocols.initiator.FileBackup;
import pt.up.fe.sdis.proj1.protocols.initiator.FileRestore;
import pt.up.fe.sdis.proj1.utils.LogFormatter;
import pt.up.fe.sdis.proj1.utils.Pair;
import rx.Observer;
import rx.schedulers.Schedulers;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private BackupSystem _backupSystem = null;
    private JList<FileVersion> list;
    private DefaultListModel<FileVersion> listModel;
    private JProgressBar progressBar;
    private JTextArea textArea;

    /**
     * Create the frame.
     * 
     * @throws IOException
     */
    public MainFrame(Pair<String, Integer> mc, Pair<String, Integer> mdb, Pair<String, Integer> mdr, InetAddress intf, String workingDir) throws IOException {
        initializeGUI();
        initializeBackupSystem(mc, mdb, mdr, intf, workingDir);
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
        
        JMenuItem mntmSettings = new JMenuItem("Settings");
        mntmSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                SettingsDialog sd = new SettingsDialog(MainFrame.this, _backupSystem);
                sd.setVisible(true);
            }
        });
        mnFile.add(mntmSettings);
        mnFile.add(mntmExit);
        
        JMenuItem mntmBackup = new JMenuItem("Backup");
        mntmBackup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog(MainFrame.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    FileBackup b = _backupSystem.backupFile(file);
                    
                    if (b == null) {
                        return;
                    }
                    
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
        listModel = new DefaultListModel<FileVersion>();
        panel.setLayout(new MigLayout("", "[100px,grow]", "[100px][14]"));
        
        list = new JList<FileVersion>();
        list.setValueIsAdjusting(true);
        list.setModel(listModel);
        list.setPreferredSize(new Dimension(100, 100));
        panel.add(list, "cell 0 0,growx,aligny top");
        list.setSize(100, 500);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(34);
        list.setMinimumSize(new Dimension(100, 150));
        
        JPopupMenu popupMenu = new JPopupMenu();
        addPopup(list, popupMenu);
        
        JMenuItem mntmRestore = new JMenuItem("Restore");
        mntmRestore.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                FileVersion fileVersion = list.getSelectedValue();
                if (fileVersion == null) return;
                
                File oldFile = new File(fileVersion.getFilePath());
                String fileName = oldFile.getName();
                
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fc.showOpenDialog(MainFrame.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File dir = fc.getSelectedFile();
                    
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            progressBar.setValue(0);
                        }
                    });
                    
                    FileRestore r = _backupSystem.restoreFile(oldFile.getAbsolutePath(), dir.getAbsolutePath() + File.separator + fileName, fileVersion.getModificationMillis());
                    
                    final Date start = new Date();
                    
                    r.getProgressionObservable().observeOn(Schedulers.newThread()).subscribe(new Observer<Double>() {
                        
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
                    
                    r.Restore();
                }
                
            }
        });
        
        JMenuItem mntmDelete = new JMenuItem("Delete");
        mntmDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                FileVersion fileVersion = list.getSelectedValue();
                if (fileVersion == null) return;
                
                _backupSystem.deleteFile(fileVersion.getFilePath(), fileVersion.getModificationMillis());
            }
        });
        popupMenu.add(mntmDelete);
        popupMenu.add(mntmRestore);
        
        progressBar = new JProgressBar(0, 100);
        panel.add(progressBar, "cell 0 1,growx,aligny center");
        
        textArea = new JTextArea();
        DefaultCaret caret = new DefaultCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        textArea.setCaret(caret);
        textArea.setEditable(false);
        contentPane.add(textArea, BorderLayout.CENTER);
        
        JScrollPane scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        contentPane.add(scroll, BorderLayout.CENTER);
        
        GuiUtils.setSystemLookAndFeel();
    }

    public void initializeBackupSystem(Pair<String, Integer> mc, Pair<String, Integer> mdb, Pair<String, Integer> mdr, InetAddress intf, String workingDir) throws IOException {
        BackupSystem.Log.addHandler(new Handler() {
            private LogFormatter formatter = new LogFormatter();
            
            @Override
            public void publish(LogRecord arg0) {
                textArea.append(formatter.format(arg0) + "\n");
            }
            
            @Override
            public void flush() {
            }
            
            @Override
            public void close() throws SecurityException {
            }
        });
        
        
        
        _backupSystem = new BackupSystem(mc, mdb, mdr, intf, workingDir);
        
        if (_backupSystem != null) {
            _backupSystem.Files.setFileListener(new BackupSystem.BackupFileListener() {
                
                @Override
                public void FileVersionRemoved(FileVersion filePath) {
                    listModel.removeElement(filePath);              
                    list.setModel(listModel);
                }
                
                @Override
                public void FileVersionAdded(FileVersion filePath) {
                    listModel.addElement(filePath);
                    list.setModel(listModel);
                }
            });
            
            List<FileVersion> ownFiles = _backupSystem.Files.getOwnFileVersions();
            for (FileVersion fileVersion : ownFiles) 
                listModel.addElement(fileVersion);
        }
    }

    @Override
    public void dispose() {
        if (_backupSystem != null) _backupSystem.shutdown();
        super.dispose();
    }
    private static void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		if (e.isPopupTrigger()) {
        			showMenu(e);
        		}
        	}
        	public void mouseReleased(MouseEvent e) {
        		if (e.isPopupTrigger()) {
        			showMenu(e);
        		}
        	}
        	private void showMenu(MouseEvent e) {
        		popup.show(e.getComponent(), e.getX(), e.getY());
        	}
        });
    }
}
