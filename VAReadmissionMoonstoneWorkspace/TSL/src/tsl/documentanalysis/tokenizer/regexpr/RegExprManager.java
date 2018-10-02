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
package tsl.documentanalysis.tokenizer.regexpr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tsl.documentanalysis.document.Document;
import tsl.documentanalysis.tokenizer.Token;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class RegExprManager {

	private Vector<RegExprPacket> packets = null;
	private Hashtable<Object, Vector<RegExprPacket>> packetHash = new Hashtable();
	private static RegExprManager RXManager = null;
	private static String RegExprDirectory = "RegExprDirectory";
	private static String RelevantRegExprTypesParameter = "RelevantRegExprTypes";

	public static void main(String[] args) {
		String pstr = "[0-9]{2}/[0-9]{2}/[0-9]{4}";
		String text = "10/12/2014 10/12/2014 10/12/2014 10/12/2014 ";
		Pattern pattern = Pattern.compile(pstr, Pattern.CASE_INSENSITIVE);
		Matcher m = pattern.matcher(text);
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			System.out.println(text.substring(start, end));
			int x = 1;
			x = x;
		}
	}

	public RegExprManager(String dname, String rtypestr) {
		RXManager = this;
		Vector<File> files = FUtils.readFilesFromDirectory(dname);
		Vector<String> rtypes = StrUtils.stringList(rtypestr, ",");
		if (files != null) {
			for (File file : files) {
				try {
					if (file.exists() && file.isFile()
							&& Character.isLetter(file.getName().charAt(0))) {
						BufferedReader in = new BufferedReader(new FileReader(
								file));
						String line = null;
						while ((line = in.readLine()) != null) {
							RegExprPacket packet = RegExprPacket
									.createRegExprPacket(this, rtypes, line);
							if (packet != null) {
								VUtils.pushHashVector(packetHash,
										packet.getType(), packet);
								this.packets = VUtils.add(this.packets, packet);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void createRegExprManager() {
		String dname = KnowledgeEngine.getCurrentKnowledgeEngine()
				.getStartupParameters().getRegExpPatternDirectory();
		String rtypes = KnowledgeEngine.getCurrentKnowledgeEngine()
				.getStartupParameters()
				.getPropertyValue(RelevantRegExprTypesParameter);
		if (dname != null && RXManager == null) {
			RXManager = new RegExprManager(dname, rtypes);
		}
	}

	public static RegExprManager getRegExprManager() {
		return RXManager;
	}

	public void applyRegExPatterns(Document document) {
		if (this.packets != null) {
			for (RegExprPacket packet : this.packets) {
				Matcher m = packet.getPattern().matcher(document.getText());
				while (m.find()) {
					int start = m.start();
					int end = m.end();
					if (end < document.getText().length()) {
						String str = document.getText().substring(start, end);
						RegExprToken token = new RegExprToken(Token.REGEX, str,
								start, end - 1, packet);
						if (token.isURL()) {
							String href = getHREF(str);
							token.setValue(href);
						}
						token.setSubtype(tsl.documentanalysis.tokenizer.Token.REGEX);
						document.addRegexToken(token);
					}
				}

			}
		}
	}

	private static String getHREF(String url) {
		String[] strings = url.split("href=");
		int start = strings[1].indexOf("\"");
		int end = strings[1].indexOf("\"", start + 1);
		String href = strings[1].substring(start + 1, end);
		return href;
	}

}
