package ptee;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class OutputToFile extends Thread {
	private List<Cve> cveList;
	private Printf p;
	private String fileName = null;

	public OutputToFile(List<Cve> cveList, Printf p, String fileName) {
		this.cveList = cveList;
		this.p = p;
		this.fileName = fileName;
	}

	public void run() {
		outputFileHtml(fileName);
	}

	public void outputFileHtml(String fileName) {
		
		fileName = fileName + ".html";
		p.println("Записывается файл " + fileName + "\n");
		int count = 0;
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName), "UTF-8"));
			out.write("<!DOCTYPE HTML>\n"
					+ " <html lang='ru'>\n"
					+ " <head>\n"
					+ " <title>Результаты поиска эксплоитов.</title>\n"
					+ " <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n"
					+ " </head>\n" + "<body>\n");
			out.write("<h2>Результаты поиска эксплоитов.</h2>\n"
					+ "<h5>PTEE version " + MainWindow.version + " build "
					+ MainWindow.build + "</h5>\n"
					+ "<h5>Дата генерации отчёта: "
					+ new java.util.Date().toString() + "</h5>\n");

			for (Cve cve : cveList) {
				if (cve.status != "No result")
					if (cve.status != null)
						count++;
			}
			out.write("<h3>Всего найдено:" + count + "</h3>\n");

			for (Cve cve : cveList) {
				if (cve.status != "No result")
					if (cve.status != null) {
						out.write("<br><b>" + cve.name + "</b> CVSS base "
								+ cve.cvssBase + " CVSS temp " + cve.cvssTemp
								+ "<br><br>\n");

						if (cve.description == null) {
							out.write("National Vulnerability Database: <a href='http://web.nvd.nist.gov/view/vuln/detail?vulnId="
									+ cve.name
									+ "'>Vulnerability Summary for "+ cve.name +"</a><br><br>\n");
						} else {
							out.write(cve.description + "<br><br>");
						}
						out.write("Exploit status <b>" + cve.status
								+ "<br>\n Link </b><a href='" + cve.link + "'>"
								+ cve.link + "</a>" + "<br>\n");
						out.write("<b>Module Name:</b> " + cve.moduleName
								+ "<br>\n");
						out.write("<b>Module Description: </b>"
								+ cve.moduleDescription + "<br><br>\n");
						out.write("<b>Hosts:</b>" + "<br>\n");
						for (String ip : cve.node) {
							out.write(ip + "<br>\n");
						}
						out.write("<hr>\n");
					}
			}
			out.write("</body>\n </html>");
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		p.println("Готово.\n");
	}

}
