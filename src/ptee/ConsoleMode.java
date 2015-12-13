package ptee;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ConsoleMode extends Thread {

	private String[] args;
	private Printf p;
	private static List<Cve> cveList = new ArrayList<Cve>();
	private LocalDataBase db;

	public ConsoleMode(String[] args, Printf p, LocalDataBase db) {
		this.args = args;
		this.p = p;
		this.db = db;
	}

	public void run() {
		String fileName = "";
		// float cCount = 0;
		// float percent;
		int outAim = 0;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		showHeader();

		// Обработка аргументов коммандной строки
		if (args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].contains("-con"))
					outAim = 0;
				if (args[i].contains("-cvs"))
					outAim = 1;
				if (args[i].contains("-html"))
					outAim = 2;
				if (args[i].contains("-txt"))
					outAim = 3;
				fileName = args[i];
			}
		} else {
			showLogo();
			p.println("File name is not set ");
			System.exit(0);
		}
		p.println("File handling started at " + new Date() + " ...");

		freeMem();

		ParsInputs pi = new ParsInputs(fileName, p, cveList);
		pi.start();
		try {
			pi.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//p.println("File handling " + fileName + " finished. " + new Date());
		//p.println("Total number of unique CVE found " + cveList.size()
		//		+ " pcs.");
		//p.println("Search exploits ...");

		MainWindow.lockLoadData = true;

		FindExploitsTh f = new FindExploitsTh(cveList, p, null, db);
		f.start();
		try {
			f.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		switch (outAim) {
		case 0:
			outputConsole();
			break;
		case 1:
			outputFileCvs();
			break;
		case 2:
			OutputToFile o = new OutputToFile(cveList, p, fileName);
			o.start();
			try {
				o.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		case 3:
			outputTxt(fileName);
			break;
		}
		// System.out.println("Update offline database...");
		// saveCveList("data.dat");
		//p.println("..........");
		//p.println("Finish.");
	}

	private void outputTxt(String fileName) {
		
		fileName = fileName + "_output.txt";
		p.println("Записывается файл " + fileName + "\n");
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(fileName)));
			for (Cve cve : cveList) {
				if (cve.status != "No result")
					if (cve.status != "Hmmm") {
						out.write(cve.name + ";"+cve.link+";"+cve.moduleName+"\n");
					}
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		p.println("Готово.\n");

	}

	// -----------------------------------------------------------------
	public static void showHeader() {
		System.out.println("");
		System.out
				.println("Search links to exploits vulnerabilities.  Version: "
						+ MainWindow.version
						+ "     (c) 'Positive Technologies'");
		System.out.println("");
	}

	public static void showLogo() {

		System.out.println("Using:");
		System.out
				.println("            java - jar mp8scare.jar [-con/-txt/-html] [имя файла отчёта]");
		System.out.println("");
		System.out.println("Options:");
		System.out
				.println("		-con	output the results to the console (default)");
		System.out.println("		-cvs		output the results to a cvs file");
		System.out.println("		-html	output the results in a html file");
		System.out.println("");
	}

	public static void freeMem() {

		Runtime rt = Runtime.getRuntime();
		long isFree = rt.freeMemory();
		long wasFree;

		do {
			wasFree = isFree;
			rt.gc();
			isFree = rt.freeMemory();
		} while (isFree > wasFree);
		rt.runFinalization();
	}

	public static void outputConsole() {
		System.out.println("");
		System.out.println("Results:");
		System.out.println("");
		for (Cve cve : cveList) {
			if (cve.status != "No result") {
				System.out.println(cve.name + "    CVSS base " + cve.cvssBase
						+ "    CVSS temp " + cve.cvssTemp);
				System.out.println(cve.description);
				System.out.println("Exploit status " + cve.status
						+ "             Link " + cve.link);
				System.out.println("Hosts:");
				for (String ip : cve.node) {
					System.out.println("    " + ip);
				}
			}
		}
	}

	public static void outputFileCvs() {
		System.out.println("Is under constraction ....");
		// TODO сделать вывод в формате CVS
	}

}
