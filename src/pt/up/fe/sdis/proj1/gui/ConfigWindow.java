package pt.up.fe.sdis.proj1.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

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
        initialize();
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
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setResizable(false);        
        frame.setBounds(100, 100, 517, 208);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout(0, 0));
        frame.setLocationRelativeTo(null);
        
        InetAddress[] comboBoxElements = NetworkUtils.getPossibleInterfaces();

        
        frame.getContentPane().add(_panel, BorderLayout.CENTER);
        
        _panel.setLayout(_fl_panel);

        
        _panel.add(_panelMain);
        
        _gbl__panelMain.columnWidths = new int[] {30, 110, 110, 0, 70, 110, 0};
        _gbl__panelMain.rowHeights = new int[] {20, 20, 20, 20, 10, 0};
        _gbl__panelMain.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
        _gbl__panelMain.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0};
        _panelMain.setLayout(_gbl__panelMain);
        
        _cmbNetworkInterface = new JComboBox<InetAddress>(comboBoxElements);
        _lblNetworkInterface.setLabelFor(_cmbNetworkInterface);
        
                
                _lblNetworkInterface.setHorizontalAlignment(SwingConstants.RIGHT);
                GridBagConstraints gbc__lblNetworkInterface = new GridBagConstraints();
                gbc__lblNetworkInterface.fill = GridBagConstraints.BOTH;
                gbc__lblNetworkInterface.insets = new Insets(0, 0, 5, 5);
                gbc__lblNetworkInterface.gridx = 1;
                gbc__lblNetworkInterface.gridy = 0;
                _panelMain.add(_lblNetworkInterface, gbc__lblNetworkInterface);
        
        
                GridBagConstraints gbc__cmbNetworkInterface = new GridBagConstraints();
                gbc__cmbNetworkInterface.gridwidth = 3;
                gbc__cmbNetworkInterface.fill = GridBagConstraints.BOTH;
                gbc__cmbNetworkInterface.insets = new Insets(0, 0, 5, 5);
                gbc__cmbNetworkInterface.gridx = 2;
                gbc__cmbNetworkInterface.gridy = 0;
                _panelMain.add(_cmbNetworkInterface, gbc__cmbNetworkInterface);
        
                JLabel lblNewLabel = new JLabel("Controller Channel:");
                lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
                gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
                gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
                gbc_lblNewLabel.gridx = 1;
                gbc_lblNewLabel.gridy = 1;
                _panelMain.add(lblNewLabel, gbc_lblNewLabel);
        
        txt_mc_ip = new TextFieldWithPlaceholder("IP", Color.LIGHT_GRAY);
        
                GridBagConstraints gbc_txt_mc_ip = new GridBagConstraints();
                gbc_txt_mc_ip.fill = GridBagConstraints.BOTH;
                gbc_txt_mc_ip.insets = new Insets(0, 0, 5, 5);
                gbc_txt_mc_ip.gridx = 2;
                gbc_txt_mc_ip.gridy = 1;
                _panelMain.add(txt_mc_ip, gbc_txt_mc_ip);
                txt_mc_ip.setColumns(10);
        
        label = new JLabel(":");
        GridBagConstraints gbc_label = new GridBagConstraints();
        gbc_label.insets = new Insets(0, 0, 5, 5);
        gbc_label.anchor = GridBagConstraints.EAST;
        gbc_label.gridx = 3;
        gbc_label.gridy = 1;
        _panelMain.add(label, gbc_label);
        
        txt_mc_port = new TextFieldWithPlaceholder("Port", Color.lightGray);
        txt_mc_port.setInputVerifier(new PortVerifier(lblInvalidMC));
        GridBagConstraints gbc_txt_mc_port = new GridBagConstraints();
        gbc_txt_mc_port.anchor = GridBagConstraints.WEST;
        gbc_txt_mc_port.fill = GridBagConstraints.VERTICAL;
        gbc_txt_mc_port.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mc_port.gridx = 4;
        gbc_txt_mc_port.gridy = 1;
        _panelMain.add(txt_mc_port, gbc_txt_mc_port);
        txt_mc_port.setColumns(5);
        txt_mc_ip.setInputVerifier(new IpVerifier(lblInvalidMC));
        
                
                lblInvalidMC.setHorizontalAlignment(SwingConstants.LEFT);
                lblInvalidMC.setForeground(Color.RED);
                lblInvalidMC.setVisible(false);
                
                GridBagConstraints gbc_lblInvalidMC = new GridBagConstraints();
                gbc_lblInvalidMC.fill = GridBagConstraints.BOTH;
                gbc_lblInvalidMC.insets = new Insets(0, 0, 5, 0);
                gbc_lblInvalidMC.gridx = 5;
                gbc_lblInvalidMC.gridy = 1;
                _panelMain.add(lblInvalidMC, gbc_lblInvalidMC);
        
        JLabel lblNewLabel_1 = new JLabel("Data Backup Channel:");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
        gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
        gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblNewLabel_1.gridx = 1;
        gbc_lblNewLabel_1.gridy = 2;
        _panelMain.add(lblNewLabel_1, gbc_lblNewLabel_1);
        
        txt_mdb_ip = new TextFieldWithPlaceholder("IP", Color.lightGray);
        GridBagConstraints gbc_txt_mdb_ip = new GridBagConstraints();
        gbc_txt_mdb_ip.fill = GridBagConstraints.BOTH;
        gbc_txt_mdb_ip.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mdb_ip.gridx = 2;
        gbc_txt_mdb_ip.gridy = 2;
        _panelMain.add(txt_mdb_ip, gbc_txt_mdb_ip);
        txt_mdb_ip.setColumns(10);
        
        JLabel lblInvalidMDB = new JLabel("Invalid IP/Port");
        lblInvalidMDB.setForeground(Color.RED);
        lblInvalidMDB.setHorizontalAlignment(SwingConstants.LEFT);
        lblInvalidMDB.setVisible(false);
        
        label_1 = new JLabel(":");
        GridBagConstraints gbc_label_1 = new GridBagConstraints();
        gbc_label_1.insets = new Insets(0, 0, 5, 5);
        gbc_label_1.anchor = GridBagConstraints.EAST;
        gbc_label_1.gridx = 3;
        gbc_label_1.gridy = 2;
        _panelMain.add(label_1, gbc_label_1);
        
        txt_mdb_port = new TextFieldWithPlaceholder("Port", Color.lightGray);
        txt_mdb_port.setInputVerifier(new PortVerifier(lblInvalidMDB));
        GridBagConstraints gbc_txt_mdb_port = new GridBagConstraints();
        gbc_txt_mdb_port.anchor = GridBagConstraints.WEST;
        gbc_txt_mdb_port.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mdb_port.gridx = 4;
        gbc_txt_mdb_port.gridy = 2;
        _panelMain.add(txt_mdb_port, gbc_txt_mdb_port);
        txt_mdb_port.setColumns(5);
        txt_mdb_ip.setInputVerifier(new IpVerifier(lblInvalidMDB));
        
                GridBagConstraints gbc_lblInvalidMDB = new GridBagConstraints();
                gbc_lblInvalidMDB.fill = GridBagConstraints.BOTH;
                gbc_lblInvalidMDB.insets = new Insets(0, 0, 5, 0);
                gbc_lblInvalidMDB.gridx = 5;
                gbc_lblInvalidMDB.gridy = 2;
                _panelMain.add(lblInvalidMDB, gbc_lblInvalidMDB);
        
        JLabel lblDataRestoreChannel = new JLabel("Data Restore Channel:");
        lblDataRestoreChannel.setHorizontalAlignment(SwingConstants.RIGHT);
        GridBagConstraints gbc_lblDataRestoreChannel = new GridBagConstraints();
        gbc_lblDataRestoreChannel.fill = GridBagConstraints.BOTH;
        gbc_lblDataRestoreChannel.insets = new Insets(0, 0, 5, 5);
        gbc_lblDataRestoreChannel.gridx = 1;
        gbc_lblDataRestoreChannel.gridy = 3;
        _panelMain.add(lblDataRestoreChannel, gbc_lblDataRestoreChannel);
        
        txt_mdr_ip = new TextFieldWithPlaceholder("IP", Color.lightGray);
        txt_mdr_ip.setColumns(10);
        GridBagConstraints gbc_txt_mdr_ip = new GridBagConstraints();
        gbc_txt_mdr_ip.fill = GridBagConstraints.BOTH;
        gbc_txt_mdr_ip.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mdr_ip.gridx = 2;
        gbc_txt_mdr_ip.gridy = 3;
        _panelMain.add(txt_mdr_ip, gbc_txt_mdr_ip);
        
        JLabel lblInvalidMDR = new JLabel("Invalid IP/Port");
        lblInvalidMDR.setHorizontalAlignment(SwingConstants.LEFT);
        lblInvalidMDR.setForeground(Color.RED);
        lblInvalidMDR.setVisible(false);
        
        label_2 = new JLabel(":");
        GridBagConstraints gbc_label_2 = new GridBagConstraints();
        gbc_label_2.insets = new Insets(0, 0, 5, 5);
        gbc_label_2.anchor = GridBagConstraints.EAST;
        gbc_label_2.gridx = 3;
        gbc_label_2.gridy = 3;
        _panelMain.add(label_2, gbc_label_2);
        
        txt_mdr_port = new TextFieldWithPlaceholder("Port", Color.lightGray);
        txt_mdr_port.setInputVerifier(new PortVerifier(lblInvalidMDR));
        GridBagConstraints gbc_txt_mdr_port = new GridBagConstraints();
        gbc_txt_mdr_port.anchor = GridBagConstraints.WEST;
        gbc_txt_mdr_port.insets = new Insets(0, 0, 5, 5);
        gbc_txt_mdr_port.gridx = 4;
        gbc_txt_mdr_port.gridy = 3;
        _panelMain.add(txt_mdr_port, gbc_txt_mdr_port);
        txt_mdr_port.setColumns(5);
        txt_mdr_ip.setInputVerifier(new IpVerifier(lblInvalidMDR));
        

        GridBagConstraints gbc_lblInvalidMDR = new GridBagConstraints();
        gbc_lblInvalidMDR.insets = new Insets(0, 0, 5, 0);
        gbc_lblInvalidMDR.fill = GridBagConstraints.BOTH;
        gbc_lblInvalidMDR.gridx = 5;
        gbc_lblInvalidMDR.gridy = 3;
        _panelMain.add(lblInvalidMDR, gbc_lblInvalidMDR);
        
        btnNewButton = new JButton("Start System");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    final InetAddress intf = (InetAddress) _cmbNetworkInterface.getSelectedItem();

                    final Pair<String, Integer> mc = Pair.make_pair(txt_mc_ip.getText(), Integer.parseInt(txt_mc_port.getText()));
                    final Pair<String, Integer> mdb = Pair.make_pair(txt_mdb_ip.getText(), Integer.parseInt(txt_mdb_port.getText()));
                    final Pair<String, Integer> mdr = Pair.make_pair(txt_mdr_ip.getText(), Integer.parseInt(txt_mdr_port.getText()));

                    if (NetworkUtils.isIPAddress(mc.first) && NetworkUtils.isIPAddress(mdb.first) && NetworkUtils.isIPAddress(mdr.first) && NetworkUtils.isValidPort(mc.second) && NetworkUtils.isValidPort(mdb.second) && NetworkUtils.isValidPort(mdr.second)) {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    MainFrame frm = new MainFrame(mc, mdb, mdr, intf);
                                    frame.dispose();
                                    frm.setVisible(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    return;
                }
            }
        });
        GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
        gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
        gbc_btnNewButton.gridx = 2;
        gbc_btnNewButton.gridy = 5;
        _panelMain.add(btnNewButton, gbc_btnNewButton);

        GuiUtils.setSystemLookAndFeel();
    }
}
