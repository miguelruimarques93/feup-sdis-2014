package pt.up.fe.sdis.proj1.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.IOException;
import java.net.InetAddress;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import pt.up.fe.sdis.proj1.utils.BackupSystem;
import pt.up.fe.sdis.proj1.utils.Pair;

import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);
        
        JMenuItem mntmExit = new JMenuItem("Exit");
        mntmExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                dispose();
            }
        });
        mnFile.add(mntmExit);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));
        
        JList list = new JList();
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
