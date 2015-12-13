package ptee;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ReportToTheSite extends Thread {

	public ReportToTheSite() {

	}

	public void run() {
		String strUrl = "http://www.ptee.100ms.ru/collector.php";
		Document doc = null;
		String hostname = null;

		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
		} catch (UnknownHostException e2) {

			e2.printStackTrace();
		}

		try {
			for (int i = 0; i < 5; i++, Thread.sleep(2000)) {
				try {

					doc = Jsoup.connect(strUrl)
							.data("version", MainWindow.build.toString())
							.data("host", hostname).timeout(5000)
							.userAgent("Mozilla").post();
					break;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
