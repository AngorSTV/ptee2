package ptee;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.EventQueue;
//import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.miginfocom.swing.MigLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainWindow {

	// TODO добавить парсинг CWE и подтягивание инфы из mitre.org
	private JFrame					frmPtee;
	private JTextField				textField_1;
	private JTextField				textField;
	private static JTextArea		textArea;
	public JProgressBar				progressBar;
	private JScrollPane				scrollPane;
	private JLayeredPane			layeredPane;
	public ProxySetup				proxySetup;

	private List<Cve>				cveList			= new ArrayList<Cve>();
	private Printf					p;
	private static LocalDataBase	db;
	final static String				version			= "2.5.0";
	final static Integer			build			= 16;
	static boolean					readyToSave		= false;				// лочит
																			// запись
																			// результатов
	static boolean					lockLoadData	= false;				// лочит
																			// загрузку
																			// данных
	static boolean					ssEliteLock		= false;
	static boolean					offLineMode		= false;
	static boolean					sfmodule		= false;

	private String openFile(boolean save) {

		String fileName = "";
		JFileChooser chooser = new JFileChooser();
		Component parent = null;

		chooser.setCurrentDirectory(new File("."));
		if (save)
			chooser.setApproveButtonText("Save");
		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			fileName = chooser.getSelectedFile().getAbsolutePath();
		}
		layeredPane.repaint();
		scrollPane.repaint();

		return fileName;
	}

	private void outputToFile(String fileName) {
		if (!fileName.isEmpty() && readyToSave) {
			OutputToFile o = new OutputToFile(cveList, p, fileName);
			o.start();
		} else {
			if (fileName.isEmpty()) {
				textField_1.setText(Messages.getString("MainWindow.outputToFile.text1"));// "Не задано имя файла.");
			} else {
				textField_1.setText(Messages.getString("MainWindow.outputToFile.text2"));// "");
			}
		}
	}

	private void parsInputs(String fileName) {

		textField_1.setText("");
		cveList.clear();

		if (!fileName.isEmpty() && !lockLoadData) {
			textField.setText(fileName);
			ParsInputs pi = new ParsInputs(fileName, p, cveList);
			pi.start();
		} else {
			if (fileName.isEmpty()) {
				textField_1.setText("Не задано имя файла.");
			} else {
				textField_1.setText("Идёт обработка данных, загрузка невозможна.");
			}
		}
	}

	private void findExploits() {

		textField_1.setText("");
		if (!cveList.isEmpty()) {
			lockLoadData = true;

			FindExploitsTh f = new FindExploitsTh(cveList, p, progressBar, db);
			f.start();
		} else {
			textField_1.setText("Нет данных для обработки");
		}
	}

	private void checkUpdate() {
		// TODO переделать в более функциональный модуль без вывода инфы
		String strUrl = "http://www.ptee.100ms.ru/carentversion.html";
		Elements sections;
		Document doc = null;

		p.println("Попытка подключения к серверу обновлений...");
		try {
			for (int i = 0; i < 5; i++, Thread.sleep(2000)) {
				try {
					doc = Jsoup.connect(strUrl).get();
					break;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		sections = doc.getElementsByClass("build");
		if (!sections.isEmpty()) {
			for (Element section : sections) {
				String str = section.text();
				int serverBuild = Integer.parseInt(str);
				p.println("Найдена версия " + serverBuild);
				if (serverBuild > build) {
					javax.swing.JOptionPane.showMessageDialog(frmPtee,
							"Имеется более новая версия.\nСкачать последнюю версию с сайта\n www.ptee.100ms.ru.",
							"Проверка обновлений.", javax.swing.JOptionPane.INFORMATION_MESSAGE);
				} else {
					javax.swing.JOptionPane.showMessageDialog(frmPtee, "У вас самая последняя версия программы.",
							"Проверка обновлений.", javax.swing.JOptionPane.INFORMATION_MESSAGE);
				}
			}
		} else {
			p.println("Ошибка определения протокола.");
		}
	}

	private static void report() {

		// ReportToTheSite r = new ReportToTheSite();
		// r.start();
	}

	private void setProxy() {
		// TODO Auto-generated method stub
		System.setProperty("http.proxyHost", proxySetup.prxServer.getText());
		System.setProperty("http.proxyPort", proxySetup.prxPort.getText());
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		Printf p = new Printf();
		report();
		if (args.length > 0) {
			// запуск консольной версии
			p.setOutput(CollectionOutput.CONSOLE);
			db = new LocalDataBase("offline.db");
			ConsoleMode con = new ConsoleMode(args, p, db);
			con.start();
			try {
				con.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			// запуск оконной версии

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						MainWindow mainWindow = new MainWindow();
						mainWindow.frmPtee.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * Create the application.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public MainWindow() {
		initialize();
		p = new Printf();
		p.setOutput(CollectionOutput.TEXTAREA);
		p.setEnv(textArea);
		proxySetup = new ProxySetup(frmPtee);

		db = new LocalDataBase("offline.db");

		textField_1.setText("Статус offline базы эксплоитов: " + db.getStatus() + "\tПодключено эксплоитов: "
				+ db.getSize());

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmPtee = new JFrame();
		frmPtee.setTitle(Messages.getString("MainWindow.frmPtee.title")); //$NON-NLS-1$
		frmPtee.setResizable(false);
		frmPtee.setOpacity(1.0f);
		frmPtee.setBounds(100, 100, 537, 570);
		frmPtee.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmPtee.getContentPane().setLayout(new MigLayout("", "[][grow]", "[][][grow][][]"));

		JButton btnNewButton = new JButton(Messages.getString("MainWindow.btnNewButton.text")); //$NON-NLS-1$
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parsInputs(openFile(false));
			}

		});
		frmPtee.getContentPane().add(btnNewButton, "cell 0 0,growx");

		JButton button = new JButton(Messages.getString("MainWindow.button.text")); //$NON-NLS-1$
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				findExploits();
			}
		});
		frmPtee.getContentPane().add(button, "cell 0 1,growx");

		textField = new JTextField();
		frmPtee.getContentPane().add(textField, "cell 1 0,growx");
		textField.setColumns(10);

		JButton button_1 = new JButton(Messages.getString("MainWindow.button_1.text")); //$NON-NLS-1$
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				outputToFile(openFile(true));
			}
		});
		frmPtee.getContentPane().add(button_1, "flowx,cell 1 1,alignx left");

		JCheckBox chckbxOfflineMode = new JCheckBox(Messages.getString("MainWindow.chckbxOfflineMode.text")); //$NON-NLS-1$
		chckbxOfflineMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				final int SELECTED = 1;
				if (arg0.getStateChange() == SELECTED) {
					MainWindow.offLineMode = true;
				} else {
					MainWindow.offLineMode = false;
				}
			}
		});

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		frmPtee.getContentPane().add(horizontalStrut_2, "cell 1 1");

		frmPtee.getContentPane().add(chckbxOfflineMode, "cell 1 1");
		JCheckBox chckbxSfMode = new JCheckBox(Messages.getString("MainWindow.chckbxSfMode.text")); //$NON-NLS-1$
		chckbxSfMode.setToolTipText(Messages.getString("MainWindow.chckbxSfMode.toolTipText")); //$NON-NLS-1$
		chckbxSfMode.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				int SELECTED = 1;
				if (arg0.getStateChange() == SELECTED) {
					MainWindow.sfmodule = true;
				} else {
					MainWindow.sfmodule = false;
				}
			}
		});
		this.frmPtee.getContentPane().add(chckbxSfMode, "cell 1 1");

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		frmPtee.getContentPane().add(progressBar, "cell 0 3 2 1,growx");

		textField_1 = new JTextField();
		textField_1.setForeground(Color.RED);
		textField_1.setFont(new Font("Tahoma", Font.BOLD, 13));
		textField_1.setEditable(false);
		frmPtee.getContentPane().add(textField_1, "cell 0 4 2 1,growx");
		textField_1.setColumns(10);

		layeredPane = new JLayeredPane();
		frmPtee.getContentPane().add(layeredPane, "cell 0 2 2 1,grow");

		scrollPane = new JScrollPane();
		scrollPane.setOpaque(false);
		layeredPane.setLayer(scrollPane, 1);
		scrollPane.setBounds(0, 0, 517, 406);
		layeredPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setOpaque(false);
		textArea.setBackground(new Color(0, 0, 0, 0));
		scrollPane.setViewportView(textArea);

		JLabel label = new JLabel(Messages.getString("MainWindow.label.text")); //$NON-NLS-1$
		label.setEnabled(true);
		layeredPane.setLayer(label, 0);
		label.setBounds(0, 0, 517, 406);
		layeredPane.add(label);
		label.setIcon(new ImageIcon(MainWindow.class.getResource("/ptee/ss.png")));

		JMenuBar menuBar = new JMenuBar();
		frmPtee.setJMenuBar(menuBar);

		Component horizontalStrut_1 = Box.createHorizontalStrut(10);
		menuBar.add(horizontalStrut_1);

		JMenu menu = new JMenu(Messages.getString("MainWindow.menu.text")); //$NON-NLS-1$
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem(Messages.getString("MainWindow.menuItem.text")); //$NON-NLS-1$
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parsInputs(openFile(false));
			}
		});
		menu.add(menuItem);

		JMenuItem menuItem_3 = new JMenuItem(Messages.getString("MainWindow.menuItem_3.text")); //$NON-NLS-1$
		menuItem_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				outputToFile(openFile(true));
			}
		});
		menu.add(menuItem_3);

		JMenuItem menuItem_4 = new JMenuItem(Messages.getString("MainWindow.menuItem_4.text")); //$NON-NLS-1$
		menuItem_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});

		JSeparator separator = new JSeparator();
		menu.add(separator);
		menu.add(menuItem_4);

		JMenu menu_2 = new JMenu(Messages.getString("MainWindow.menu_2.text")); //$NON-NLS-1$
		menuBar.add(menu_2);

		JMenuItem mntmBat = new JMenuItem(Messages.getString("MainWindow.mntmBat.text")); //$NON-NLS-1$
		mntmBat.setEnabled(false);
		menu_2.add(mntmBat);

		JMenuItem mntmSh = new JMenuItem(Messages.getString("MainWindow.mntmSh.text")); //$NON-NLS-1$
		mntmSh.setEnabled(false);
		menu_2.add(mntmSh);

		JSeparator separator_1 = new JSeparator();
		menu_2.add(separator_1);

		JMenuItem mntmActivateSsElite = new JMenuItem(Messages.getString("MainWindow.mntmActivateSsElite.text"));
		mntmActivateSsElite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!ssEliteLock) {
					SSEliteGuard ss = new SSEliteGuard();
					ssEliteLock = true;
					ss.start();

					textArea.setOpaque(true);
					textArea.setBackground(new Color(0, 0, 0, 0));
					Font f = new Font("", Font.BOLD, 20);

					textArea.setFont(f);
					textArea.append("Achtung!\nSie gesendet Elite SS-Einheiten. \nAlles bereit für die sofortige Evakuierung.\n");
					textArea.setCaretPosition(textArea.getDocument().getLength());
				}
			}
		});
		menu_2.add(mntmActivateSsElite);

		JMenuItem mntmProxy = new JMenuItem(Messages.getString("MainWindow.mntmProxy.text")); //$NON-NLS-1$
		mntmProxy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// рисование окошка для настройки проксика.

				// proxySetup = new ProxySetup(frame);

				if (proxySetup.execute()) {
					setProxy();
				} else {

				}
			}
		});
		menu_2.add(mntmProxy);

		JMenuItem menuItem_6 = new JMenuItem(Messages.getString("MainWindow.menuItem_6.text")); //$NON-NLS-1$
		menuItem_6.setEnabled(false);
		menu_2.add(menuItem_6);

		Component horizontalGlue = Box.createHorizontalGlue();
		menuBar.add(horizontalGlue);

		JMenu menu_1 = new JMenu(Messages.getString("MainWindow.menu_1.text")); //$NON-NLS-1$
		menuBar.add(menu_1);

		JMenuItem menuItem_1 = new JMenuItem(Messages.getString("MainWindow.menuItem_1.text")); //$NON-NLS-1$
		menuItem_1.setEnabled(false);
		menuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				URL helpURL = MainWindow.class.getResource("index.html");
				final Desktop desktop = Desktop.getDesktop();

				JFrame j = new JFrame("Справка PTEE " + MainWindow.version);
				j.setOpacity(1.0f);
				j.setBounds(170, 150, 770, 650);
				j.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

				JScrollPane scrollPane = new JScrollPane();

				JTextPane textPane = new JTextPane();
				textPane.setEditable(false);
				textPane.setLocale(new Locale("ru", "RU"));
				scrollPane.setViewportView(textPane);

				HyperlinkListener hll = new HyperlinkListener() {
					@Override
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
							try {
								desktop.browse(new URI(e.getURL().toString()));
							} catch (IOException e1) {
								e1.printStackTrace();
							} catch (URISyntaxException e1) {
								e1.printStackTrace();
							}
						}

					}

				};
				textPane.addHyperlinkListener(hll);

				try {
					textPane.setContentType("text/html; charset=UTF-8");
					textPane.setPage(helpURL);

				} catch (IOException e1) {
					e1.printStackTrace();
				}

				j.getContentPane().add(scrollPane);
				j.setVisible(true);
			}

		});

		menu_1.add(menuItem_1);

		JMenuItem menuItem_2 = new JMenuItem(Messages.getString("MainWindow.menuItem_2.text")); //$NON-NLS-1$
		menuItem_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				javax.swing.JOptionPane.showMessageDialog(frmPtee, "PTEE программа поиска общедоступных\n"
						+ "             эксплоитов в интернете.\n" + "                        версия " + version
						+ "\n                             build " + build + "\n\n"
						+ "                  Автор: AngorSTV\n" + "                    angor@inbox.ru\n\n\n",
						"О программе.", javax.swing.JOptionPane.INFORMATION_MESSAGE);
			}
		});

		JMenuItem menuItem_5 = new JMenuItem(Messages.getString("MainWindow.menuItem_5.text")); //$NON-NLS-1$
		menuItem_5.setEnabled(false);
		menuItem_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				checkUpdate();
			}
		});
		menu_1.add(menuItem_5);
		menu_1.add(menuItem_2);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		menuBar.add(horizontalStrut);
	}
}
