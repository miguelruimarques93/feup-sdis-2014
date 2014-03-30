package pt.up.fe.sdis.proj1.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
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

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import pt.up.fe.sdis.proj1.BackupSystem;
import pt.up.fe.sdis.proj1.FileVersion;
import pt.up.fe.sdis.proj1.config.BackupSystemConfiguration;
import pt.up.fe.sdis.proj1.gui.utils.GuiUtils;
import pt.up.fe.sdis.proj1.protocols.FileProtocol;
import pt.up.fe.sdis.proj1.protocols.initiator.ChunkBackup.ChunkBackupException;
import pt.up.fe.sdis.proj1.protocols.initiator.FileBackup;
import pt.up.fe.sdis.proj1.protocols.initiator.FileRestore;
import pt.up.fe.sdis.proj1.utils.LogFormatter;
import rx.Observer;
import rx.schedulers.Schedulers;
import javax.swing.ScrollPaneConstants;

public class MainFrame extends JFrame {

    private final class RestoreAction implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            FileVersion fileVersion = list.getSelectedValue();
            if (fileVersion == null)
                return;

            final File oldFile = new File(fileVersion.getFilePath());
            String fileName = oldFile.getName();

            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fc.showOpenDialog(MainFrame.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File dir = fc.getSelectedFile();

                FileRestore r = _backupSystem.restoreFile(oldFile.getAbsolutePath(), dir.getAbsolutePath() + File.separator + fileName,
                        fileVersion.getModificationMillis());

                final TaskController tc = addTaskController("Restore: " + fileVersion.toString(), "Receving chunks.");
                final TaskCanceller taskCanceller = new TaskCanceller(r);

                tc.addCancelListener(taskCanceller);

                final Date start = new Date();

                r.getProgressionObservable().observeOn(Schedulers.newThread()).subscribe(new Observer<Double>() {

                    @Override
                    public void onCompleted() {
                        tc.setState("Completed in " + (new Date().getTime() - start.getTime()) + " ms");
                        tc.removeCancelListener(taskCanceller);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof UserRequestException) {
                            tc.setState("Cancelled by user request.");
                        } else {
                            tc.setState("Failed. " + e.getMessage());
                            JOptionPane.showMessageDialog(MainFrame.this, "Could not restore file '" + oldFile.getAbsolutePath() + "'.\nTry again later.",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
                        }
                        tc.removeCancelListener(taskCanceller);
                    }

                    @Override
                    public void onNext(final Double t) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                tc.setProgressBarValue((int) (t * 100.0));
                            }
                        });
                    }
                });

                r.Restore();
            }

        }
    }

    private final class TaskCanceller implements ActionListener {
        private final FileProtocol _p;

        private TaskCanceller(FileProtocol p) {
            _p = p;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            _p.onError(new UserRequestException());
        }
    }

    private final class BackupAction implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            FileBackupDialog fbd = new FileBackupDialog(MainFrame.this, _backupSystem);
            fbd.setVisible(true);

            if (fbd.succeed()) {
                final File file = new File(fbd.getBackupFilePath()).getAbsoluteFile();
                Integer replicationDegree = fbd.getBackupReplicationDegree();
                final FileBackup b = _backupSystem.backupFile(file, replicationDegree);

                if (b == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "File already backed up.", "Error!", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                final TaskController tc = addTaskController("Backup: " + b.getFileVersion().toString(), "Sending chunks.");
                final TaskCanceller taskCanceller = new TaskCanceller(b);

                tc.addCancelListener(taskCanceller);

                final Date start = new Date();

                b.getProgressionObservable().observeOn(Schedulers.newThread()).subscribe(new Observer<Double>() {

                    @Override
                    public void onCompleted() {
                        tc.setState("Completed in " + (new Date().getTime() - start.getTime()) + " ms");
                        tc.removeCancelListener(taskCanceller);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof UserRequestException)
                            return;

                        if (e instanceof ChunkBackupException) {
                            ChunkBackupException cbe = (ChunkBackupException) e;
                            JOptionPane.showMessageDialog(MainFrame.this, "Error sending chunk " + cbe.getChunkNo() + " from file '" + file.getAbsolutePath()
                                    + "'.\nCould not backup file '" + file.getAbsolutePath() + "'.", "Error!", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(MainFrame.this, e.getMessage() + "\nCould not backup file '" + file.getAbsolutePath() + "'.",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    @Override
                    public void onNext(final Double t) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                tc.setProgressBarValue((int) (t * 100.0));
                            }
                        });
                    }
                });

                b.Send();
            }
        }
    }

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    private BackupSystem _backupSystem = null;
    private JList<FileVersion> list;
    private DefaultListModel<FileVersion> listModel;
    private JTextArea textArea;

    private JPanel _taskControllerPanel;

    /**
     * Create the frame.
     * 
     * @throws IOException
     */
    public MainFrame(BackupSystemConfiguration configs, InetAddress intf) throws IOException {
        initializeGUI();
        initializeBackupSystem(configs, intf);
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
        setBounds(100, 100, 800, 600);
        setMinimumSize(new Dimension(615, 310));
        this.setLocationRelativeTo(null);
        
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
                _backupSystem.commitSettings();
            }
        });

        JMenuItem mntmBackupFile = new JMenuItem("Backup File");
        mntmBackupFile.addActionListener(new BackupAction());
        mnFile.add(mntmBackupFile);
        mnFile.add(mntmSettings);
        mnFile.add(mntmExit);
        listModel = new DefaultListModel<FileVersion>();
        DefaultCaret caret = new DefaultCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.tabsOverlapBorder", true);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
        tabbedPane.setOpaque(true);
        tabbedPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        JPanel panel_2 = new JPanel();
        tabbedPane.addTab("Tasks", null, panel_2, null);
        panel_2.setLayout(new BorderLayout(0, 0));

        _taskControllerPanel = new JPanel();

        panel_2.add(_taskControllerPanel);
        BoxLayout taskLayout = new BoxLayout(_taskControllerPanel, BoxLayout.Y_AXIS);
        _taskControllerPanel.setLayout(taskLayout);

        JScrollPane scroll_1 = new JScrollPane(_taskControllerPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel_2.add(scroll_1);

        JPanel panel_1 = new JPanel();
        tabbedPane.addTab("Message Log", null, panel_1, null);
        panel_1.setLayout(new BorderLayout(0, 0));

        textArea = new JTextArea();
        textArea.setCaret(caret);
        textArea.setEditable(false);
        panel_1.add(textArea);

        JScrollPane scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel_1.add(scroll);
        
                list = new JList<FileVersion>();
                
                list.setValueIsAdjusting(true);
                list.setModel(listModel);
                list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                list.setVisibleRowCount(10);
                
                        JScrollPane scrollPane = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        contentPane.add(scrollPane, BorderLayout.SOUTH);
                        
                        // panel.add(list, BorderLayout.CENTER);

                        JPopupMenu popupMenu = new JPopupMenu();
                        addPopup(list, popupMenu);
                        
                                JMenuItem mntmRestore = new JMenuItem("Restore");
                                mntmRestore.addActionListener(new RestoreAction());
                                
                                        JMenuItem mntmDelete = new JMenuItem("Delete");
                                        mntmDelete.addActionListener(new ActionListener() {
                                            public void actionPerformed(ActionEvent e) {
                                                FileVersion fileVersion = list.getSelectedValue();
                                                if (fileVersion == null)
                                                    return;

                                                _backupSystem.deleteFile(fileVersion.getFilePath(), fileVersion.getModificationMillis());
                                            }
                                        });
                                        popupMenu.add(mntmDelete);
                                        popupMenu.add(mntmRestore);

        GuiUtils.setSystemLookAndFeel();
    }

    public void initializeBackupSystem(BackupSystemConfiguration configs, InetAddress intf) throws IOException {
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

        _backupSystem = new BackupSystem(configs, intf);

        if (_backupSystem != null) {
            _backupSystem.Files.setFileListener(new BackupSystem.BackupFileListener() {

                @Override
                public void FileVersionRemoved(FileVersion filePath) {
                    listModel.removeElement(filePath);
                    list.setModel(listModel);
                }

                @Override
                public void FileVersionAdded(FileVersion filePath) {
                    System.out.println(filePath);
                    listModel.addElement(filePath);
                    list.setModel(listModel);
                }
            });

            List<FileVersion> ownFiles = _backupSystem.Files.getOwnFileVersions();
            for (FileVersion fileVersion : ownFiles)
                listModel.addElement(fileVersion);
        }
    }

    public TaskController addTaskController(String taskName, String initialState) {
        final TaskController tc = new TaskController(taskName, initialState);
        _taskControllerPanel.add(tc);
        tc.addCancelListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                _taskControllerPanel.remove(tc);
                _taskControllerPanel.revalidate();
                _taskControllerPanel.repaint();
            }
        });
        _taskControllerPanel.revalidate();
        _taskControllerPanel.repaint();
        return tc;
    }

    @Override
    public void dispose() {
        if (_backupSystem != null)
            _backupSystem.shutdown();
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
