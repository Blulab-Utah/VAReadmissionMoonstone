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
package moonstone.temporality;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import moonstone.annotation.Annotation;
import moonstone.annotation.WordSequenceAnnotation;
import moonstone.grammar.Grammar;
import moonstone.rulebuilder.MoonstoneRuleInterface;
import tsl.documentanalysis.document.Document;
import tsl.expression.term.constant.StringConstant;
import tsl.expression.term.type.TypeConstant;
import tsl.utilities.FUtils;
import tsl.utilities.StrUtils;
import tsl.utilities.VUtils;

public class TemporalAnnotationManager {

	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private Hashtable<String, Vector<TemporalAnnotation>> annotationHash = null;
	private String temporalAnnotationFilePath = null;
	public static String FileStringDelimiter = "$$";
	public static String TemporalAnnotationFilePath = "TemporalAnnotationFilePath";

	/*
	 * DO: In UIMA, create TemporalAnnotations, and at the end, store them to
	 * file.
	 * 
	 * In Moonstone, create an instanceof TemporalAnnotationManager, then for
	 * each document sentence, call readSentenceTemporalAnnotations(wsa,
	 * grammar)
	 * 
	 */

	public TemporalAnnotationManager(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
		this.temporalAnnotationFilePath = this.moonstoneRuleInterface.getStartupParameters()
				.getPropertyValue(TemporalAnnotationFilePath);
		this.readAnnotationsFromFile();
	}

	public void readSentenceTemporalAnnotations(WordSequenceAnnotation wsa, Grammar grammar) {
		Document doc = wsa.getDocument();
		int sindex = wsa.getSentence().getDocumentIndex();
		String key = doc.getName() + ":" + sindex;
		Vector<TemporalAnnotation> annotations = this.annotationHash.get(key);
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				annotation.setSentenceAnnotation(wsa);
				grammar.addAnnotation(annotation);
			}
		}
	}

	public void readAnnotationsFromFile() {
		try {
			File file = new File(temporalAnnotationFilePath);
			this.annotationHash = new Hashtable();
			if (file.exists()) {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String line = null;
				while ((line = in.readLine()) != null) {
					Vector<String> strs = StrUtils.stringList(line, FileStringDelimiter);
					if (strs != null) {
						// document,concept,stype,textStart,textEnd,tokenStart,tokenEnd,sentenceIndex
						String dname = strs.elementAt(0);
						String tastr = strs.elementAt(1);
						String conceptstr = strs.elementAt(2);
						String stypestr = strs.elementAt(3);
						int textStart = Integer.parseInt(strs.elementAt(4));
						int textEnd = Integer.parseInt(strs.elementAt(5));
						int tokenStart = Integer.parseInt(strs.elementAt(6));
						int tokenEnd = Integer.parseInt(strs.elementAt(7));
						int sentenceIndex = Integer.parseInt(strs.elementAt(8));
						TypeConstant stype = TypeConstant.createTypeConstant(stypestr);
						StringConstant concept = StringConstant.createStringConstant(conceptstr, stype, false);
						TemporalAnnotation ta = new TemporalAnnotation(conceptstr, tastr, tokenStart, tokenEnd,
								textStart, textEnd, -1, -1, stype);
						ta.setSentenceIndex(sentenceIndex);
						String key = dname + ":" + sentenceIndex;
						VUtils.pushHashVector(this.annotationHash, key, ta);
					}
				}
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeAnnotationsToFile() {
		if (this.annotationHash != null && this.temporalAnnotationFilePath != null) {
			StringBuffer sb = new StringBuffer();
			for (Enumeration<String> e = this.annotationHash.keys(); e.hasMoreElements();) {
				String key = e.nextElement();
				Vector<TemporalAnnotation> tas = this.annotationHash.get(key);
				if (tas != null) {
					for (TemporalAnnotation ta : tas) {
						String str = ta.toFileString();
						sb.append(str + "\n");
					}
				}
			}
			FUtils.writeFile(this.temporalAnnotationFilePath, sb.toString());
		}
	}

}
