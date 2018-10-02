/*
Copyright 2018 Wendy Chapman (wendy.chapman\@utah.edu) & Lee Christensen (leenlp\@q.com)

Licensed under the Apache License, Version 2.0 (the \"License\");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an \"AS IS\" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Vector;

import tsl.utilities.VUtils;

public class UnixFormat {

	public static void convertFilesToUnixFormat(String dname, String newdname)
			throws Exception {
		File dir = new File(dname);
		if (!dir.exists()) {
			String msg = "Unable to locate directory " + dname;
			System.out.println(msg);
			throw new Exception(msg);
		}
		File[] children = dir.listFiles();
		for (int i = 0; i < children.length; i++) {
			File cfile = children[i];
			String cleantext = convertToUnixFormat(cfile);
			String newpath = newdname + File.separator + cfile.getName();
			unixWriteFile(newpath, cleantext);
		}
	}

	private static String convertToUnixFormat(File file) throws Exception {
		StringBuffer sb = new StringBuffer();
		if (file.exists() && file.isFile()) {
			BufferedReader in = new BufferedReader(new FileReader(file));
			int cval = -1;
			char c = (char) -1;
			char lastc = (char) -1;
			while ((cval = in.read()) != -1) {
				c = (char) cval;
				if (lastc == 0xD && c != 0xA) {
					sb.append(0xA);
				}
				if (c != 0xD) {
					sb.append(c);
				}
				lastc = c;
			}
		}
		return sb.toString();
	}

	public static String convertToUnixFormat(String str) throws Exception {
		StringBuffer sb = new StringBuffer();
		if (str != null) {
			char c = (char) -1;
			char lastc = (char) -1;
			for (int i = 0; i < str.length(); i++) {
				c = str.charAt(i);
				if (lastc == 0xD && c != 0xA) {
					sb.append(0xA);
				}
				if (c != 0xD) {
					sb.append(c);
				}
				lastc = c;
			}
		}
		return sb.toString();
	}

	private static String unixWriteFile(String filename, String text)
			throws Exception {
		String input = null;
		File file = new File(filename);
		unixFindOrCreateDirectory(file, true);
		if (file.exists()) {
			file.delete();
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(filename));
		out.write(text);
		out.close();
		return input;
	}

	private static void unixFindOrCreateDirectory(File file, boolean withFile)
			throws Exception {
		if (!file.getParentFile().exists()) {
			Vector<String> v = stringList(file.getAbsolutePath(),
					File.separatorChar);
			String dirName = "";
			int size = (withFile ? v.size() - 1 : v.size());
			for (int i = 0; i < size; i++) {
				dirName += File.separator + v.elementAt(i);
				File dir = new File(dirName);
				if (!dir.exists()) {
					dir.mkdir();
				}
			}
		}
	}

	private static Vector<String> stringList(String str, char delim) {
		Vector rv = null;
		if (str != null) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (c == delim) {
					if (sb.length() > 0) {
						rv = VUtils.add(rv, sb.toString().trim());
						sb = new StringBuffer();
					}
				} else {
					sb.append(String.valueOf(c));
				}
			}
			if (sb.length() > 0) {
				rv = VUtils.add(rv, sb.toString().trim());
			}
		}
		return rv;
	}

	// 11/5/2015
	public static int getOffsetAdjustedForCarriageReturn(String str, int offset) {
		int adjusted = 0;
		if (str != null) {
			for (int i = 0; i < offset; i++) {
				char c = str.charAt(i);
				adjusted++;
				if (c == '\r') {
					adjusted++;
				}
			}
		}
		return adjusted;
	}

}
