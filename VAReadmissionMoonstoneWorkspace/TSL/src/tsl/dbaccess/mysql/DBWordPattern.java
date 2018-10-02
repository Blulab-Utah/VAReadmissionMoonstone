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
package tsl.dbaccess.mysql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Vector;

import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.tokenizer.Token;
import tsl.utilities.VUtils;

public class DBWordPattern {

	private static String[][] fileConceptMap = {
			{ "/Users/leechristensen/Desktop/EvaluationWorkbenchFolder/gazeteers/CaTIES_NegExPreNegationPhrases.lst",
					"directionality=negated" },
			{ "/Users/leechristensen/Desktop/EvaluationWorkbenchFolder/gazeteers/CaTIES_NegExPostNegationPhrases.lst",
					"directionality=negated" },
			{ "/Users/leechristensen/Desktop/EvaluationWorkbenchFolder/gazeteers/Triggers_History.lst", "temporality=historical" }, };
	private static Hashtable<String, String> tagConceptMap = new Hashtable();

	public static boolean isWordAttributeValue(String str) {
		return str != null && str.contains("=");
	}
	
	public static Vector<DBTermIndex> getConTextDBTermIndexes(Sentence sentence) {
		Vector<DBTermIndex> dbv = null;
		if (sentence.getTokens() != null) {
			int docid = sentence.getDocument().getId();
			int sindex = sentence.getDocumentIndex();
			for (int i = 0; i < sentence.tokens.size(); i++) {
				for (int j = sentence.tokens.size() - 1; j >= i; j--) {
					Vector<Token> subTokens = VUtils.subVector(sentence.tokens,
							i, j + 1);
					String substr = Token.stringListConcat(subTokens)
							.toLowerCase();
					int textStart = subTokens.firstElement().getStart();
					int textEnd = subTokens.lastElement().getEnd();
					String concept = tagConceptMap.get(substr);
					if (concept != null) {
						DBTermIndex dbt = new DBTermIndex(concept, -1, docid,
								sindex, textStart, textEnd);
						dbv = VUtils.add(dbv, dbt);
						break;
					}
				}
			}
		}
		return dbv;
	}

	public static void initialize() {
		try {
			for (int i = 0; i < fileConceptMap.length; i++) {
				String fname = fileConceptMap[i][0];
				String concept = fileConceptMap[i][1];
				Vector<String> fstrs = null;
				File f = new File(fname);
				if (f.exists()) {
					BufferedReader in = new BufferedReader(new FileReader(f));
					String line = null;
					while ((line = in.readLine()) != null) {
						if (line.length() > 0
								&& Character.isLetter(line.charAt(0))) {
							String str = line.trim().toLowerCase();
							fstrs = VUtils.add(fstrs, str);
						}
					}
				}
				if (fstrs != null) {
					for (String str : fstrs) {
						tagConceptMap.put(str, concept);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
