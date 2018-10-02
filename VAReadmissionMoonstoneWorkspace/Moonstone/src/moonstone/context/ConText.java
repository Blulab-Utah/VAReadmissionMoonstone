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
package moonstone.context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class ConText {

	private Vector<String> headers = null;
	private Hashtable<String, ConTextItem> wordHash = new Hashtable();
	private Hashtable<String, Vector<ConTextItem>> headerHash = new Hashtable();
	private char delimiter = ',';
//	protected static String exWordHeader = "ITEM";
//	protected static String exCategoryHeader = "CATEGORY";
//	protected static String exClosureHeader = "CLOSURE";
//	protected static String exActionHeader = "EN (SV) ACTION";

	public ConText(String fname, char delim) {
		if (delim != (char) -1) {
			this.delimiter = delim;
		}
		readLexicon(fname);
	}

	public static ConText createConText(String fname, char delim) {
		if (fname != null) {
			File file = new File(fname);
			if (file.exists()) {
				return new ConText(fname, delim);
			}
		}
		return null;
	}

	public Vector<String> getConTextItems(Vector<String> hvpairs) {
		Vector<ConTextItem> cis = null;
		Vector<ConTextItem> matchCIs = null;
		if (hvpairs != null) {
			cis = getHeaderHash(hvpairs.firstElement().toLowerCase());
		}
		if (cis != null) {
			for (int i = 1; i < hvpairs.size(); i++) {
				String hvstr = hvpairs.elementAt(i);
				String[] hvpair = hvstr.split("=");
				if (hvpair != null && hvpair.length == 2) {
					String header = hvpair[0].toLowerCase();
					String value = hvpair[1].toLowerCase();
					for (ConTextItem ci : cis) {
						if (!(ci.isMatch() && value.equals(ci
								.getProperty(header)))) {
							ci.setMatch(false);
						}
					}
				}

			}
			for (ConTextItem ci : cis) {
				if (ci.isMatch()) {
					matchCIs = VUtils.add(matchCIs, ci);
				}
				ci.setMatch(true);
			}
		}
		return getConTextStrings(matchCIs);
	}

	private void readLexicon(String fname) {
		try {
			File file = new File(fname);
			if (file.exists() && file.isFile()) {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line = null;
				while ((line = in.readLine()) != null) {
					line = line.toLowerCase();
					Vector<String> sstrs = getDelimitedStrings(
							line.toLowerCase(), this.delimiter);
					if (sstrs != null && sstrs.size() > 1) {
						if (this.headers == null) {
							this.headers = sstrs;
						} else {
							for (int i = 0; i < headers.size(); i++) {
								String header = headers.elementAt(i);
								String value = "*";
								if (i < sstrs.size()
										&& sstrs.elementAt(i).length() > 0) {
									value = sstrs.elementAt(i);
								}
							}
							this.createConTextItem(sstrs);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void createConTextItem(Vector<String> sstrs) {
		if (sstrs != null && sstrs.size() > 2) {
			ConTextItem cti = new ConTextItem(this);
			cti.setString(sstrs.firstElement());
			for (int i = 0; i < sstrs.size(); i++) {
				if (i < this.headers.size()) {
					String header = this.headers.elementAt(i);
					String value = sstrs.elementAt(i);
					if (value.length() > 0) {
						cti.setProperty(header, value);
					}
				}
			}
			this.wordHash.put(cti.getString(), cti);
			for (Enumeration<String> e = cti.getProperties().keys(); e
					.hasMoreElements();) {
				String header = e.nextElement();
				String value = cti.getProperty(header);
				String vstr = header + "=" + value;
				
				if (vstr.toLowerCase().contains("negated")) {
					int x = 1;
					x = x;
				}
				
				VUtils.pushHashVector(this.headerHash, vstr, cti);
			}
		}
	}

	public static Vector<String> getConTextStrings(Vector<ConTextItem> cis) {
		Vector<String> cistrs = null;
		if (cis != null) {
			for (ConTextItem ci : cis) {
				cistrs = VUtils.add(cistrs, ci.getString());
			}
		}
		return cistrs;
	}

	private ConTextItem getConTextItem(String str) {
		return wordHash.get(str);
	}

	private Vector<ConTextItem> getHeaderHash(String header) {
		return headerHash.get(header);
	}
	
	private Vector<String> getDelimitedStrings(String line, char delim) {
		boolean incomment = false;
		String str = "";
		Vector<String> strs = null;
		if (line != null && line.length() > 0) {
			for (int i = 0; i < line.length(); i++) {
				char c = line.charAt(i);
				if (c == delim) {
					if (!incomment) {
						str = StrUtils.trimAllWhiteSpace(str);
						if (str == null) {
							str = "";
						}
						strs = VUtils.add(strs, str);
						str = "";
					} else {
						str += c;
					}
				} else if (c == '"') {
					incomment = !incomment;
				} else {
					str += c;
				}
			}
			str = StrUtils.trimAllWhiteSpace(str);
			if (str == null) {
				str = "";
			}
			strs = VUtils.add(strs, str);
		}
		return strs;
	}

	// Before 1/7/2015:  Getting empty strings...
//	private Vector<String> getDelimitedStrings(String line, char delim) {
//		boolean incomment = false;
//		String str = "";
//		Vector<String> strs = null;
//		if (line != null && line.length() > 0) {
//			for (int i = 0; i < line.length(); i++) {
//				char c = line.charAt(i);
//				if (c == delim) {
//					if (!incomment) {
//						str = StrUtils.trimAllWhiteSpace(str);
//						strs = VUtils.add(strs, str);
//						str = "";
//					} else {
//						str += c;
//					}
//				} else if (c == '"') {
//					incomment = !incomment;
//				} else {
//					str += c;
//				}
//			}
//			str = StrUtils.trimAllWhiteSpace(str);
//			strs = VUtils.add(strs, str);
//		}
//		return strs;
//	}

}
