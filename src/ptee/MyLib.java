package ptee;

import java.io.File;

public final class MyLib {
	
	public static String fileExt(String fileName) {
		File file = new File(fileName);
		String[] tmpFileName;

		tmpFileName = file.getName().split("[/.]");
		return tmpFileName[tmpFileName.length - 1];
	}
	
	public static String fileNoExt(String fileName) {
		File file = new File(fileName);
		String[] tmpFileName;
		String result = "";

		tmpFileName = file.getName().split("[/.]");
		for (int i = 0; i < tmpFileName.length - 1; i++) {
			result = result + tmpFileName[i];
		}
		return result;
	}
}
