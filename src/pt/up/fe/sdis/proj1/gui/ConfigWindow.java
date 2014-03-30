package pt.up.fe.sdis.proj1.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.InetAddress;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import pt.up.fe.sdis.proj1.config.BackupSystemConfiguration;
import pt.up.fe.sdis.proj1.gui.utils.GuiUtils;
import pt.up.fe.sdis.proj1.gui.utils.IpVerifier;
import pt.up.fe.sdis.proj1.gui.utils.PortVerifier;
import pt.up.fe.sdis.proj1.gui.utils.TextFieldWithPlaceholder;
import pt.up.fe.sdis.proj1.utils.NetworkUtils;
import pt.up.fe.sdis.proj1.utils.Pair;

public class ConfigWindow {

    private JFrame frame;
    private JTextField txt_mc_ip;
    private JTextField txt_mdb_ip;
    private JTextField txt_mdr_ip;
    private JTextField txt_mc_port;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ConfigWindow window = new ConfigWindow();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public ConfigWindow() {
        initializeConfigs();
        initializeGUI();
    }

    private FlowLayout _fl_panel = new FlowLayout(FlowLayout.CENTER, 5, 5);
    private JPanel _panel = new JPanel();
    private JPanel _panelMain = new JPanel();
    private GridBagLayout _gbl__panelMain = new GridBagLayout();
    private JLabel _lblNetworkInterface = new JLabel("Network Interface:");
    private JComboBox<InetAddress> _cmbNetworkInterface;
    private final JLabel lblInvalidMC = new JLabel("Invalid IP/Port");
    private JTextField txt_mdb_port;
    private JTextField txt_mdr_port;
    private JLabel label;
    private JLabel label_1;
    private JLabel label_2;
    private JButton btnNewButton;
    private JLabel lblWorkingDirectory;
    private JTextField workingDirectory_txt;
    private JLabel lblBrowse;
    
    private void initializeConfigs() {
        String userHome = System.getProperty("user.home");
        if (userHome == null) {
            userHome = "";
        }
        
        File userHomeDir = new File(userHome).getAbsoluteFile();
        File configFile = new File(userHomeDir.getAbsolutePath() + File.separator + "backupSystem.config.ser");
        
        _systemConfiguration = BackupSystemConfiguration.loadFromFile(configFile);
    }
    
    /**
     * Initialize the contents of the frame.
     */
    private void initializeGUI() {
        frame = new JFrame();
        frame.setResizable(false);        
        frame.setBounds(100, 100, 738, 226);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        frame.setLocationRelativeTo(null);
        
        InetAddress[] comboBoxElements = NetworkUtils.getPossibleInterfaces();

        
        frame.getContentPane().add(_panel, BorderLayout.CENTER);
        
        _panel.setLayout(_fl_panel);

        
        _panel.add(_panelMain);
        
        _gbl__panelMain.columnWidths = new int[] {30, 0, 110, 0, 10, 30, 10, 70, 0, 0};
        _gbl__panelMain.rowHeights = new int[] {0, 20, 20, 20, 10, 10, 0};
        _gbl__panelMain.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
        _gbl__panelMain.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0};
        _panelMain.setLayout(_gbl__panelMain);
        
        lblWorkingDirectory = new JLabel("Working Directory:");
        GridBagConstraints gbc_lblWorkingDirectory = new GridBagConstraints();
        gbc_lblWorkingDirectory.anchor = GridBagConstraints.EAST;
        gbc_lblWorkingDirectory.insets = new Insets(0, 0, 5, 5);
        gbc_lblWorkingDirectory.gridx = 1;
        gbc_lblWorkingDirectory.gridy = 0;
        _panelMain.add(lblWorkingDirectory, gbc_lblWorkingDirectory);
        
        workingDirectory_txt = new JTextField();
        workingDirectory_txt.setEditable(false);
        workingDirectory_txt.setText(_systemConfiguration.getWorkingDir());
        GridBagConstraints gbc_workingDirectory_txt = new GridBagConstraints();
        gbc_workingDirectory_txt.gridwidth = 6;
        gbc_workingDirectory_txt.insets = new Insets(0, 0, 5, 5);
        gbc_workingDirectory_txt.fill = GridBagConstraints.HORIZONTAL;
        gbc_workingDirectory_txt.gridx = 2;
        gbc_workingDirectory_txt.gridy = 0;
        _panelMain.add(workingDirectory_txt, gbc_workingDirectory_txt);
        workingDirectory_txt.setColumns(10);
                        
                        lblBrowse = new JLabel("<html><u>Browse...</u></html>");
                        lblBrowse.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        lblBrowse.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent arg0) {
                                File defaultDir = new File(workingDirectory_txt.getText());
                                if (!defaultDir.exists())
                                    defaultDir = defaultDir.getParentFile();
                                
                                JFileChooser fileChooser = new JFileChooser(defaultDir);
                                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                                int returnVal = fileChooser.showOpenDialog(frame);
                                if (returnVal == JFileChooser.APPROVE_OPTION) {
                                    workingDirectory_txt.setText(fileChooser.getSelectedFile().getAbsolutePath());
                                }
                            }
                        });
                        GridBagConstraints gbc_lblBrowse = new GridBagConstraints();
                        gbc_lblBrowse.insets = new Insets(0, 0, 5, 0);
                        gbc_lblBrowse.gridx = 8;
                        gbc_lblBrowse.gridy = 0;
                        _panelMain.add(lblBrowse, gbc_lblBrowse);
                        lblBrowse.setHorizontalAlignment(SwingConstants.LEFT);
                        lblBrowse.setFont(new Font("Tahoma", Font.PLAIN, 11));
                        lblBrowse.setForeground(Color.BLUE);
                
                        
                        _lblNetworkInterface.setHorizontalAlignment(SwingConstants.RIGHT);
                        GridBagConstraints gbc__lblNetworkInterface = new GridBagConstraints();
                        gbc__lblNetworkInterface.fill = GridBagConstraints.BOTH;
                        gbc__lblNetworkInterface.insets = new Insets(0, 0, 5, 5);
                        gbc__lblNetworkInterface.gridx = 1;
                        gbc__lblNetworkInterface.gridy = 1;
                        _panelMain.add(_lblNetworkInterface, gbc__lblNetworkInterface);
                
                _cmbNetworkInterface = new JComboBox<InetAddress>(comboBoxElements);
                _lblNetworkInterface.setLabelFor(_cmbNetworkInterface);
                
                
                        GridBagConstraints gbc__cmbNetworkInterface = new GridBagConstraints();
                        gbc__cmbNetworkInterface.gridwidth = 3;
                        gbc__cmbNetworkInterface.fill = GridBagConstraints.BOTH;
                        gbc__cmbNetworkInterface.insets = new Insets(0, 0, 5, 5);
                        gbc__cmbNetworkInterface.gridx = 2;
                        gbc__cmbNetworkInterface.gridy = 1;
                        _panelMain.add(_cmbNetworkInterface, gbc__cmbNetworkInterface);
        
                JLabel lblNewLabel = new JLabel("Controller Channel:");
                lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
                gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
                gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
                gbc_lblNewLabel.gridx = 1;
                gbc_lblNewLabel.gridy = 2;
                _panelMain.add(lblNewLabel, gbc_lblNewLabel);
        
        txt_mc_ip = new TextFieldWithPlaceholder("IP", Color.LIGHT_GRAY);
        txt_mc_ip.setText((_systemConfiguration.getMC() == null ? "" : _systemConfiguration.getMC().first));
                GridBagConstraints gbc_txt_mc_ip = new GridBagConstraints();
                gbc_txt_mc_ip.fill = GridBagConstraints.BOTH;
                gbc_txt_mc_ip.insets = new Insets(0, 0, 5, 5);
                gbc_txt_mc_ip.gridx = 2;
                gbc_txt_mc_ip.gridy = 2;
                _panelMain.add(txt_mc_ip, gbc_txt_mc_ip);
                txt_mc_ip.setColumns(10);
        
        txt_mc_port = new TextFieldWithPlaceholder("Port", Color.lightGray);
        txt_mc_port.setText((_systemConfiguration.getMC() == null ? "" : _systemConfiguration.getMC().second.toString()));
        GridBagConstraints gbc_txt_mc_port = new GridBagConstraints();
        gbc_txt_mc_port.fill = GridBagConstraints.BOTH;
        gbc_txt_mc_port.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mc_port.gridx = 4;
        gbc_txt_mc_port.gridy = 2;
        _panelMain.add(txt_mc_port, gbc_txt_mc_port);
        txt_mc_port.setColumns(5);
        
        label = new JLabel(":");
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.gridx = 3;
        gbc_label.gridy = 2;
        _panelMain.add(label, gbc_label);
        txt_mc_ip.setInputVerifier(new IpVerifier(lblInvalidMC));
        txt_mc_port.setInputVerifier(new PortVerifier(lblInvalidMC));
        
                
                lblInvalidMC.setHorizontalAlignment(SwingConstants.LEFT);
                lblInvalidMC.setForeground(Color.RED);
                lblInvalidMC.setVisible(false);
                
                GridBagConstraints gbc_lblInvalidMC = new GridBagConstraints();
                gbc_lblInvalidMC.fill = GridBagConstraints.BOTH;
                gbc_lblInvalidMC.insets = new Insets(0, 0, 5, 5);
                gbc_lblInvalidMC.gridx = 6;
                gbc_lblInvalidMC.gridy = 2;
                _panelMain.add(lblInvalidMC, gbc_lblInvalidMC);
        
        JLabel lblNewLabel_1 = new JLabel("Data Backup Channel:");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 1;
        gbc_lblNewLabel_1.gridy = 3;
        _panelMain.add(lblNewLabel_1, gbc_lblNewLabel_1);
        
        txt_mdb_ip = new TextFieldWithPlaceholder("IP", Color.lightGray);
        txt_mdb_ip.setText((_systemConfiguration.getMDB() == null ? "" : _systemConfiguration.getMDB().first));
        GridBagConstraints gbc_txt_mdb_ip = new GridBagConstraints();
        gbc_txt_mdb_ip.fill = GridBagConstraints.BOTH;
        gbc_txt_mdb_ip.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mdb_ip.gridx = 2;
        gbc_txt_mdb_ip.gridy = 3;
        _panelMain.add(txt_mdb_ip, gbc_txt_mdb_ip);
        txt_mdb_ip.setColumns(10);
        
        txt_mdb_port = new TextFieldWithPlaceholder("Port", Color.lightGray);
        txt_mdb_port.setText((_systemConfiguration.getMDB() == null ? "" : _systemConfiguration.getMDB().second.toString()));
        GridBagConstraints gbc_txt_mdb_port = new GridBagConstraints();
        gbc_txt_mdb_port.fill = GridBagConstraints.HORIZONTAL;
        gbc_txt_mdb_port.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mdb_port.gridx = 4;
        gbc_txt_mdb_port.gridy = 3;
        _panelMain.add(txt_mdb_port, gbc_txt_mdb_port);
        txt_mdb_port.setColumns(5);
        
        label_1 = new JLabel(":");
        GridBagConstraints gbc_label_1 = new GridBagConstraints();
        gbc_label_1.insets = new Insets(0, 0, 5, 5);
        gbc_label_1.anchor = GridBagConstraints.EAST;
        gbc_label_1.gridx = 3;
        gbc_label_1.gridy = 3;
        _panelMain.add(label_1, gbc_label_1);
        
        JLabel lblInvalidMDB = new JLabel("Invalid IP/Port");
        lblInvalidMDB.setForeground(Color.RED);
        lblInvalidMDB.setHorizontalAlignment(SwingConstants.LEFT);
        lblInvalidMDB.setVisible(false);
        txt_mdb_ip.setInputVerifier(new IpVerifier(lblInvalidMDB));
        txt_mdb_port.setInputVerifier(new PortVerifier(lblInvalidMDB));
        
                GridBagConstraints gbc_lblInvalidMDB = new GridBagConstraints();
                gbc_lblInvalidMDB.fill = GridBagConstraints.BOTH;
                gbc_lblInvalidMDB.insets = new Insets(0, 0, 5, 5);
                gbc_lblInvalidMDB.gridx = 6;
                gbc_lblInvalidMDB.gridy = 3;
                _panelMain.add(lblInvalidMDB, gbc_lblInvalidMDB);
        
        JLabel lblDataRestoreChannel = new JLabel("Data Restore Channel:");
        lblDataRestoreChannel.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints gbc_lblDataRestoreChannel = new GridBagConstraints();
        gbc_lblDataRestoreChannel.fill = GridBagConstraints.BOTH;
        gbc_lblDataRestoreChannel.insets = new Insets(0, 0, 5, 5);
        gbc_lblDataRestoreChannel.gridx = 1;
        gbc_lblDataRestoreChannel.gridy = 4;
        _panelMain.add(lblDataRestoreChannel, gbc_lblDataRestoreChannel);
        
        txt_mdr_ip = new TextFieldWithPlaceholder("IP", Color.lightGray);
        txt_mdr_ip.setText((_systemConfiguration.getMDR() == null ? "" : _systemConfiguration.getMDR().first));
        txt_mdr_ip.setColumns(10);
        GridBagConstraints gbc_txt_mdr_ip = new GridBagConstraints();
        gbc_txt_mdr_ip.fill = GridBagConstraints.BOTH;
        gbc_txt_mdr_ip.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mdr_ip.gridx = 2;
        gbc_txt_mdr_ip.gridy = 4;
        _panelMain.add(txt_mdr_ip, gbc_txt_mdr_ip);
        
        label_2 = new JLabel(":");
        GridBagConstraints gbc_label_2 = new GridBagConstraints();
        gbc_label_2.insets = new Insets(0, 0, 5, 5);
        gbc_label_2.anchor = GridBagConstraints.EAST;
        gbc_label_2.gridx = 3;
        gbc_label_2.gridy = 4;
        _panelMain.add(label_2, gbc_label_2);
        
        txt_mdr_port = new TextFieldWithPlaceholder("Port", Color.lightGray);
        txt_mdr_port.setText((_systemConfiguration.getMDR() == null ? "" : _systemConfiguration.getMDR().second.toString()));
        GridBagConstraints gbc_txt_mdr_port = new GridBagConstraints();
        gbc_txt_mdr_port.fill = GridBagConstraints.HORIZONTAL;
        gbc_txt_mdr_port.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mdr_port.gridx = 4;
        gbc_txt_mdr_port.gridy = 4;
        _panelMain.add(txt_mdr_port, gbc_txt_mdr_port);
        txt_mdr_port.setColumns(5);
        
        JLabel lblInvalidMDR = new JLabel("Invalid IP/Port");
        lblInvalidMDR.setHorizontalAlignment(SwingConstants.LEFT);
        lblInvalidMDR.setForeground(Color.RED);
        lblInvalidMDR.setVisible(false);
        txt_mdr_ip.setInputVerifier(new IpVerifier(lblInvalidMDR));
        txt_mdr_port.setInputVerifier(new PortVerifier(lblInvalidMDR));
        

        GridBagConstraints gbc_lblInvalidMDR = new GridBagConstraints();
        gbc_lblInvalidMDR.insets = new Insets(0, 0, 5, 5);
        gbc_lblInvalidMDR.fill = GridBagConstraints.BOTH;
        gbc_lblInvalidMDR.gridx = 6;
        gbc_lblInvalidMDR.gridy = 4;
        _panelMain.add(lblInvalidMDR, gbc_lblInvalidMDR);
        
        btnNewButton = new JButton("Start System");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    final InetAddress intf = (InetAddress) _cmbNetworkInterface.getSelectedItem();

                    final Pair<String, Integer> mc = Pair.make_pair(txt_mc_ip.getText(), Integer.parseInt(txt_mc_port.getText()));
                    final Pair<String, Integer> mdb = Pair.make_pair(txt_mdb_ip.getText(), Integer.parseInt(txt_mdb_port.getText()));
                    final Pair<String, Integer> mdr = Pair.make_pair(txt_mdr_ip.getText(), Integer.parseInt(txt_mdr_port.getText()));
                    final String workingDir = workingDirectory_txt.getText();
                    
                    if (NetworkUtils.isIPAddress(mc.first) && NetworkUtils.isIPAddress(mdb.first) && NetworkUtils.isIPAddress(mdr.first) && NetworkUtils.isValidPort(mc.second) && NetworkUtils.isValidPort(mdb.second) && NetworkUtils.isValidPort(mdr.second)) {
                        _systemConfiguration.setMC(mc);
                        _systemConfiguration.setMDB(mdb);
                        _systemConfiguration.setMDR(mdr);
                        _systemConfiguration.setWorkingDir(workingDir);
                        
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    MainFrame frm = new MainFrame(_systemConfiguration, intf);
                                    frame.dispose();
                                    frm.setVisible(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        
                        _systemConfiguration.save();
                    }
                } catch (Exception e) {
                    return;
                }
            }
        });
        GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
        gbc_btnNewButton.gridwidth = 3;
        gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
        gbc_btnNewButton.gridx = 2;
        gbc_btnNewButton.gridy = 6;
        _panelMain.add(btnNewButton, gbc_btnNewButton);

        GuiUtils.setSystemLookAndFeel();
    }

    BackupSystemConfiguration _systemConfiguration = null;
}
