package ptee;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ProxySetup extends JDialog {

	public static final ProxySetup INSTANCE = new ProxySetup(null);
	
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	public JTextField prxServer;
	public JTextField prxPort;
	public JTextField prxLogin;
	public JTextField prxPassword;
	private boolean result;
	private JCheckBox chckbxProxyServerRequires;
	private JLabel lblLogin;
	private JLabel lblPassword;

	public ProxySetup(java.awt.Frame parent) {
		super(parent, true);

		initComponents(); // инициализация компонентов окна
	}

	public boolean execute() {
		this.setVisible(true);
		return result;
	}

	private void initComponents() {
		
		setTitle("Proxy setup");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 375, 205);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[][][][]",
				"[][][9.00][][][12.00][]"));

		JLabel lblProxySe = new JLabel("Proxy server type");
		contentPane.add(lblProxySe, "cell 0 0,alignx right");

		String[] listType = { "HTTP", "HTTPS", "SOCKS 4", "SOCKS 5" };
		JComboBox comboBox = new JComboBox(listType);
		contentPane.add(comboBox, "cell 1 0,growx");

		JLabel lblServer = new JLabel("Server");
		contentPane.add(lblServer, "cell 0 1,alignx right");

		prxServer = new JTextField();
		contentPane.add(prxServer, "cell 1 1,growx");
		prxServer.setColumns(10);

		JLabel lblPort = new JLabel("Port");
		contentPane.add(lblPort, "cell 2 1,alignx trailing");

		prxPort = new JTextField();
		contentPane.add(prxPort, "cell 3 1,growx");
		prxPort.setColumns(10);

		chckbxProxyServerRequires = new JCheckBox(
				"Proxy server requires authorization");
		chckbxProxyServerRequires.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(chckbxProxyServerRequires.isSelected()) {
					lblLogin.setEnabled(true);
					prxLogin.setEnabled(true);
					lblPassword.setEnabled(true);
					prxPassword.setEnabled(true);
				}else{
					lblLogin.setEnabled(false);
					prxLogin.setEnabled(false);
					lblPassword.setEnabled(false);
					prxPassword.setEnabled(false);
				}
			}
		});
		contentPane.add(chckbxProxyServerRequires, "cell 0 3 3 1");

		lblLogin = new JLabel("Login");
		lblLogin.setEnabled(false);
		contentPane.add(lblLogin, "cell 0 4,alignx trailing");

		prxLogin = new JTextField();
		prxLogin.setEnabled(false);
		contentPane.add(prxLogin, "cell 1 4,growx");
		prxLogin.setColumns(10);

		lblPassword = new JLabel("Password");
		lblPassword.setEnabled(false);
		contentPane.add(lblPassword, "cell 2 4,alignx trailing");

		prxPassword = new JTextField();
		prxPassword.setEnabled(false);
		contentPane.add(prxPassword, "cell 3 4,growx");
		prxPassword.setColumns(10);

		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				result = true; // пользователь нажал ОК
				dispose(); // уничтожить окно
			}
		});
		contentPane.add(btnOk, "cell 1 6,alignx right");

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = false;
				dispose(); // уничтожить окно
			}
		});
		contentPane.add(btnCancel, "cell 2 6");

	}

}
