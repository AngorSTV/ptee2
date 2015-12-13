package ptee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParsInputs extends Thread {

	private String		fileName	= null;
	private List<Cve>	cveList;
	int					count		= 0;
	private Printf		p;

	public ParsInputs(String fileName, Printf p, List<Cve> cveList) {
		this.fileName = fileName;
		this.p = p;
		this.cveList = cveList;
	}

	public void run() {

		MainWindow.readyToSave = false;

		if (checkMP8XML(fileName)) {
			p.println("Разбор информации в MP8-XML отчёте: " + MyLib.fileNoExt(fileName) + ".xml");
			parsXml(fileName);
		} else {
			p.println("Файл не опознан как MP8-XML отчёт и будет разобран на общих основаниях.");
			parsOther(fileName);
		}
		p.println("Всего обнаружено " + count + " уникальных уязвимостей.");
		p.println("Для поиска эксплоитов нажмите кнопку <Найти эксплоиты>");
	}

	private String findCve(String str) {
		Pattern p = Pattern.compile("[Cc][Vv][Ee]-[0-9]{4}-[0-9]{4,}");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return m.group(0);
		} else {
			return null;
		}
	}

	private boolean checkMP8XML(String fileName) {
		boolean result = false;
		BufferedReader input = null;
		String tmp;
		Pattern pt = Pattern.compile("maxpatrol80");

		try {
			input = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			p.println("File \"" + fileName + "\" not find.");
			return result;
		}
		try {
			while ((tmp = input.readLine()) != null) {
				Matcher m = pt.matcher(tmp);
				if (m.find()) {
					result = true;
					input.close();
					return result;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private void parsOther(String fileName) {
		ArrayList<String> tmpList = new ArrayList<>();// TODO ???
		BufferedReader input = null;
		String tmp;

		try {
			input = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			p.println("Файл \"" + fileName + "\" не найден");
			return;
		}

		// парсинг файла и построение списка не повторяющихся CVE
		p.println("Первый проход по файлу ...");
		try {
			while ((tmp = input.readLine()) != null) {

				tmp = findCve(tmp);
				if (tmp != null) {
					if (!tmpList.contains(tmp)) {// TODO очень сомнительная
													// строка
						tmpList.add(tmp);
					}
				}
			}
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		p.println("Вторичный проход по файлу ...");
		// создание списка объектов Cve из списка не повторяющихся CVE
		for (String temp : tmpList) {
			Cve cve = new Cve(temp);
			count++;
			cveList.add(cve);
		}
	}

	private void parsXml(String fileName) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		String tmpTitle = "";
		String temp_score = "", base_score = "";// , temp_score_decomp,
		// base_score_decomp;
		String ip, tmp_id, str;
		try {
			// строим DOM по входному файлу
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new File(fileName));
			// из дерева выбираем ветки с именем vulner
			NodeList listVuln = document.getElementsByTagName("vulner");
			// из дерева выбираем ветки с именем host
			NodeList listHost = document.getElementsByTagName("host");

			// каждую ветку разбираем на листочки
			for (int i = 0; i < listVuln.getLength(); i++) {
				Node node = listVuln.item(i);
				// выдираем id во временную переменную
				NamedNodeMap map = node.getAttributes();
				tmp_id = map.getNamedItem("id").toString()
						.substring(4, map.getNamedItem("id").toString().lastIndexOf("\""));
				NodeList listInNode = node.getChildNodes();
				// смотрим что за листик и пытаемся из него
				// достать нужные данные
				for (int j = 0; j < listInNode.getLength(); j++) {
					Node nodeIn = listInNode.item(j);
					if (nodeIn.getNodeName() == "title")
						tmpTitle = nodeIn.getTextContent();
					if (nodeIn.getNodeName() == "cvss") {
						NamedNodeMap mapIn = nodeIn.getAttributes();
						temp_score = mapIn.getNamedItem("temp_score").toString().substring(12, 15);
						base_score = mapIn.getNamedItem("base_score").toString().substring(12, 15);
					}
					if (nodeIn.getNodeName() == "global_id") {
						NamedNodeMap mapIn = nodeIn.getAttributes();
						str = mapIn.getNamedItem("value").toString();
						str = findCve(str);
						if (str != null) {
							Cve cve = new Cve(str);
							cve.description = tmpTitle;
							cve.cvssBase = base_score;
							cve.cvssTemp = temp_score;
							cve.id = tmp_id;
							// cve добавляем в список
							count++;
							cveList.add(cve);
						}
					}
				}
			}
			// каждую ветку разбираем на листочки
			for (int i = 0; i < listHost.getLength(); i++) {
				Node node = listHost.item(i);
				NodeList listInNode = node.getChildNodes();
				NamedNodeMap map = node.getAttributes();
				ip = map.getNamedItem("ip").toString()
						.substring(4, map.getNamedItem("ip").toString().lastIndexOf("\""));
				ip = ip
						+ "&nbsp&nbsp&nbsp&nbsp"
						+ map.getNamedItem("fqdn").toString()
								.substring(6, map.getNamedItem("fqdn").toString().lastIndexOf("\""));
				// смотрим листик (vulner)и пытаемся из него
				// достать нужные данные
				for (int j = 0; j < listInNode.getLength(); j++) {
					Node nodeIn = listInNode.item(j);
					NodeList listInNode2 = nodeIn.getChildNodes();
					for (int k = 0; k < listInNode2.getLength(); k++) {
						Node nodeIn2 = listInNode2.item(k);
						NodeList listInNode3 = nodeIn2.getChildNodes();
						for (int m = 0; m < listInNode3.getLength(); m++) {
							Node nodeIn3 = listInNode3.item(m);
							NodeList listInNode4 = nodeIn3.getChildNodes();
							for (int n = 0; n < listInNode4.getLength(); n++) {
								Node nodeIn4 = listInNode4.item(n);
								if (nodeIn4.getNodeName() == "vulner") {
									NamedNodeMap mapIn4 = nodeIn4.getAttributes();
									tmp_id = mapIn4.getNamedItem("id").toString()
											.substring(4, mapIn4.getNamedItem("id").toString().lastIndexOf("\""));
									for (Cve cve : cveList) {
										if (cve.id.equals(tmp_id))
											cve.node.add(ip);
									}
								}
							}
						}
					}
				}
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	private class Test {
	}
}
