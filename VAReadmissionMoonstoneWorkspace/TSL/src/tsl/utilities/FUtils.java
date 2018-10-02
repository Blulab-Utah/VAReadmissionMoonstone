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
package tsl.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class FUtils {

	public static int maxDocSize = 10000000;

	// public static char[] readBuffer = new char[maxDocSize];

	public static String getCurrentWorkingDirectory() {
		URL location = FUtils.class.getProtectionDomain().getCodeSource()
				.getLocation();
		String path = location.getFile();
		return path;
	}

	public static String getAbsolutePathname(String path) {
		String result = path;
		return path;
		// if (path != null) {
		// String cwd = getCurrentWorkingDirectory();
		// char c = path.charAt(0);
		// if (c != File.separatorChar && Character.isLetterOrDigit(c)) {
		// result = cwd + File.pathSeparator + path;
		// }
		// }
		// return result;
	}

	// 4/13/2017
	public static Vector<File> readFilesFromDirectory(String dname,
			String pathConstraint, String filenameConstraint) {
		Vector<File> v = null;
		if (dname != null) {
			File sourcedir = new File(dname);
			if (sourcedir != null && sourcedir.exists()
					&& sourcedir.isDirectory()) {
				v = readFilesFromDirectory(sourcedir, pathConstraint,
						filenameConstraint);
			}
		}
		return v;
	}

	public static Vector<File> readFilesFromDirectory(File sourcedir,
			String pathConstraint, String filenameConstraint) {
		Vector<File> v = null;
		if (sourcedir != null && sourcedir.exists()) {
			File[] files = sourcedir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String fpath = file.getAbsolutePath();
				String fplc = fpath.toLowerCase();
				if (file.isFile()
						&& file.getName().charAt(0) != '.'
						&& (pathConstraint == null || (pathConstraint.length() > 2 && fplc
								.contains(pathConstraint.toLowerCase())))
						&& (filenameConstraint == null || (filenameConstraint
								.length() > 2 && fplc
								.contains(filenameConstraint.toLowerCase())))) {
					v = VUtils.add(v, file);
				} else if (file.isDirectory()) {
					v = VUtils.append(
							v,
							readFilesFromDirectory(file, pathConstraint,
									filenameConstraint));
				}
			}
		}
		return v;
	}

	public static Vector<File> readFilesFromDirectory_Before_12_11_2017(
			File sourcedir, String contained) {
		Vector<File> v = null;
		if (sourcedir != null && sourcedir.exists()) {
			File[] files = sourcedir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String fpath = file.getAbsolutePath();
				String fplc = fpath.toLowerCase();
				if (file.isFile()
						&& file.getName().charAt(0) != '.'
						&& (contained == null || (contained.length() > 2 && fplc
								.contains(contained.toLowerCase())))) {
					v = VUtils.add(v, file);
				} else if (file.isDirectory()) {
//					v = VUtils.append(v,
//							readFilesFromDirectory(file, contained));
				}
			}
		}
		return v;
	}

	public static Vector<File> readFilesFromDirectory(String dname) {
		Vector<File> v = null;
		if (dname != null) {
			File sourcedir = new File(dname);
			if (sourcedir != null && sourcedir.exists()
					&& sourcedir.isDirectory()) {
				v = readFilesFromDirectory(sourcedir);
			}
		}
		return v;
	}

	public static Vector<File> readFilesFromDirectory(File sourcedir) {
		Vector<File> v = null;
		if (sourcedir != null && sourcedir.exists()) {
			File[] files = sourcedir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile() && file.getName().charAt(0) != '.') {
					v = VUtils.add(v, file);
				} else if (file.isDirectory()) {
					v = VUtils.append(v, readFilesFromDirectory(file));
				}
			}
		}
		return v;
	}

	public static Vector<File> getSubdirectories(String dname) {
		return getSubdirectories(new File(dname));
	}

	public static Vector<String> getSubdirectoryNames(String dname) {
		Vector<File> subdirs = getSubdirectories(new File(dname));
		Vector<String> sdnames = null;
		if (subdirs != null) {
			for (File subdir : subdirs) {
				sdnames = VUtils.add(sdnames, subdir.getName());
			}
		}
		return sdnames;
	}

	public static Vector<File> getSubdirectories(File sourcedir) {
		Vector<File> v = null;
		if (sourcedir != null && sourcedir.exists() && sourcedir.isDirectory()) {
			File[] files = sourcedir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isDirectory() && file.getName().charAt(0) != '.') {
					v = VUtils.add(v, file);
				}
			}
		}
		return v;
	}

	public static boolean isLispFile(String filename) {
		boolean foundfirstchar = false;
		boolean foundlispchar = false;
		String str = readFile(filename);
		for (int i = 0; i < str.length() && !foundfirstchar; i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				foundfirstchar = true;
				if (c == '\'' || c == '(') {
					foundlispchar = true;
				}
			}
		}
		return foundlispchar;
	}

	public static Vector<Vector> gatherWordLineInformation(String filename) {
		Vector<Vector> results = null;
		try {
			Vector<Integer> lineoffsets = null;
			Vector<Vector<Integer>> wordoffsets = null;
			Vector<Vector<String>> allwords = null;
			File f = new File(filename);
			if (f.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line = null;
				int lineoffset = 0;
				while ((line = in.readLine()) != null) {
					lineoffsets = VUtils.add(lineoffsets, new Integer(
							lineoffset));
					Vector<Integer> woffsets = null;
					Vector<String> words = null;
					lineoffset += line.length();
					lineoffset += 1; // Carriage return at end. (Does not handle
										// two character EOLs...)
					if (line.length() > 0) {
						StringBuffer sb = new StringBuffer();
						int lastwordoffset = 0;
						boolean inword = false;
						for (int i = 0; i < line.length(); i++) {
							char c = line.charAt(i);
							if (!Character.isWhitespace(c)) {
								sb.append(c);
								if (!inword) {
									lastwordoffset = i;
								}
								inword = true;
							} else {
								inword = false;
								if (sb.length() > 0) {
									Integer wo = new Integer(lastwordoffset);
									woffsets = VUtils.add(woffsets, wo);
									words = VUtils.add(words, sb.toString());
									sb = new StringBuffer();
								}
							}
						}
						if (sb.length() > 0) {
							Integer wo = new Integer(lastwordoffset);
							woffsets = VUtils.add(woffsets, wo);
							words = VUtils.add(words, sb.toString());
							sb = null;
						}
					}
					wordoffsets = VUtils.add(wordoffsets, woffsets);
					allwords = VUtils.add(allwords, words);
				}
			}
			results = new Vector(0);
			results.add(lineoffsets);
			results.add(wordoffsets);
			results.add(allwords);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

	// public static String readFile(File file) {
	// String input = null;
	// try {
	// if (file.exists() && file.isFile()) {
	// BufferedReader in = new BufferedReader(new FileReader(file));
	// int size = 0;
	// char[] readBuffer = new char[1024];
	// while ((size = in.read(readBuffer, 0, 1024)) > 0) {
	//
	// if (size > 0) {
	// readBuffer[size] = '\0';
	// input = String.valueOf(readBuffer, 0, size + 1);
	// readBuffer[0] = '\0';
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return input;
	// }

	public static String readFile(File file) {
		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader read = new BufferedReader(new FileReader(file));
			StringWriter swr = new StringWriter();
			if (file.exists() && file.isFile()) {
				char[] byt = new char[1024];
				int len = read.read(byt);
				while (len > 0) {
					swr.write(byt, 0, len);
					len = read.read(byt);
				}
				sb = swr.getBuffer();
			}
			read.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	public static String readFile(String filename) {
		String input = null;
		try {
			if (filename != null) {
				File f = new File(filename);
				if (f.exists()) {
					return readFile(f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return input;
	}

	public static void writeFile(String filename, String text) {
		writeFile(filename, text, true);
	}

	public static void appendFile(String filename, String text) {
		StringBuffer sb = new StringBuffer();
		String existing = FUtils.readFile(filename);
		if (existing != null) {
			sb.append(existing);
		}
		sb.append(text);
		FUtils.writeFile(filename, sb.toString());
	}

	public static String writeFile(String filename, String text, boolean doclean) {
		String input = null;
		try {
			File file = new File(filename);
			findOrCreateDirectory(file, true);
			if (doclean && file.exists()) {
				file.delete();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
			if (doclean) {
				out.write(text);
			} else {
				out.append(text);
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return input;
	}

	public static void findOrCreateDirectory(String dname, boolean withFile) {
		File file = new File(dname);
		findOrCreateDirectory(file, withFile);
	}

	public static void findOrCreateDirectory(File file, boolean withFile) {
		try {
			Vector<String> v = StrUtils.stringList(file.getAbsolutePath(),
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void copyFile(String srcFile, String destFile)
			throws Exception {
		File f1 = new File(srcFile);
		File f2 = new File(destFile);
		if (f1.exists()) {
			if (f2.exists()) {
				f2.delete();
			}
			if (!f2.exists()) {
				f2.createNewFile();
			}
			InputStream in = new FileInputStream(srcFile);
			OutputStream out = new FileOutputStream(destFile);
			byte[] buffer = new byte[2048];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) >= 0) {
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			in.close();
		}
	}

	public static void copyDirectory(String sourcedirname, String destdirname)
			throws Exception {
		File sourcedir = new File(sourcedirname);
		if (sourcedir.exists()) {
			File destdir = new File(destdirname);
			destdir.mkdir();
			File[] files = sourcedir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {
					String filename = file.getName();
					String oldpath = sourcedir.getPath() + File.separator
							+ filename;
					String newpath = destdir.getPath() + File.separator
							+ filename;
					copyFile(oldpath, newpath);
				}
			}
		}
	}

	public static String convertStreamToString(InputStream is)
			throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[64000];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return null;
		}
	}

	public static String convertFileSeparators(String fname) {
		String newfname = null;
		if (fname != null) {
			newfname = "";
			String[] strings = fname.split("/");
			for (int i = 0; i < strings.length; i++) {
				newfname += strings[i];
				if (i < strings.length - 1) {
					newfname += File.separator;
				}
			}
		}
		return newfname;
	}

	public static String getShortFileName(String fname) {
		String sname = null;
		if (fname != null) {
			int index = fname.lastIndexOf(File.separatorChar);
			if (index > 0) {
				sname = fname.substring(index + 1);
			}
		}
		return sname;
	}

	public static String getFileName(String s1, String s2) {
		return s1 + File.separator + s2;
	}

	public static String getFileName(String s1, String s2, String s3) {
		return s1 + File.separator + s2 + File.separator + s3;
	}

	public static String getFileName(String s1, String s2, String s3, String s4) {
		return s1 + File.separator + s2 + File.separator + s3 + File.separator
				+ s4;
	}

	public static String getFileName(String s1, String s2, String s3,
			String s4, String s5) {
		return s1 + File.separator + s2 + File.separator + s3 + File.separator
				+ s4 + File.separator + s5;
	}

	public static File getDirectory(String filename) {
		File dfile = null;
		if (filename != null) {
			File file = new File(filename);
			if (file.exists()) {
				if (file.isDirectory()) {
					dfile = file;
				} else {
					dfile = file.getParentFile();
				}
			}
		} else {
			String currentDir = System.getProperty("user.dir");
			dfile = new File(currentDir);
		}
		return dfile;
	}

	public static File chooseFile(String directory, String dirIfNull, String msg) {
		if (directory == null && dirIfNull != null) {
			directory = dirIfNull;
		}
		return chooseFile(getDirectory(directory), msg);
	}

	public static File chooseFile(String directory, String msg) {
		return chooseFile(getDirectory(directory), msg);
	}

	public static File chooseFile(File directory, String msg) {
		JFileChooser chooser = new JFileChooser(msg);
		if (directory != null && directory.exists()) {
			chooser.setCurrentDirectory(directory);
		}
		int rv = chooser.showOpenDialog(new JFrame());
		if (rv == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	public static File chooseDirectory(String directory, String dirIfNull) {
		if (directory == null && dirIfNull != null) {
			directory = dirIfNull;
		}
		return chooseDirectory(directory);
	}

	public static File chooseDirectory(String directory) {
		return chooseDirectory(getDirectory(directory));
	}

	public static File chooseDirectory(File currentDirectory) {
		JFrame frame = new JFrame();
		JFileChooser chooser = new JFileChooser();
		if (currentDirectory != null) {
			chooser.setCurrentDirectory(currentDirectory);
		}
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		int rv = chooser.showOpenDialog(frame);
		if (rv == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	public static boolean fileExists(String path) {
		File file = new File(path);
		return file.exists();
	}

	public static String fileStringIfExists(String path) {
		File file = new File(path);
		return file.exists() ? path : "";
	}

	public static void deleteFileIfExists(String path) {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}

}
