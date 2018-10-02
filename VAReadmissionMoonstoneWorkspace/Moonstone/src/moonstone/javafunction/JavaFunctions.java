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
package moonstone.javafunction;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import tsl.documentanalysis.document.Header;
import tsl.documentanalysis.document.NarrativeContent;
import tsl.documentanalysis.document.Sentence;
import tsl.documentanalysis.lexicon.Word;
import tsl.documentanalysis.tokenizer.Token;
import tsl.expression.term.Term;
import tsl.expression.term.function.javafunction.JavaFunctionTerm;
import tsl.expression.term.relation.JavaRelationSentence;
import tsl.expression.term.relation.RelationSentence;
import tsl.expression.term.type.TypeConstant;
import tsl.expression.term.variable.Variable;
import tsl.expression.term.vector.VectorTerm;
import tsl.knowledge.ontology.umls.UMLSTypeConstant;
import moonstone.annotation.Annotation;
import moonstone.annotation.NumberAnnotation;
import moonstone.annotation.RegExprAnnotation;
import moonstone.annotation.StructureAnnotation;
import moonstone.annotation.WordSequenceAnnotation;
import moonstone.annotation.TagAnnotation;
import moonstone.grammar.GrammarModule;
import moonstone.io.readmission.CombinedHeaderSentence;
import moonstone.rule.Rule;
import moonstone.semantic.Interpretation;

public class JavaFunctions {

	private static Object[][] NumberWordValues = { { "one", new Float(1) },
			{ "two", new Float(2) }, { "three", new Float(3) },
			{ "four", new Float(4) }, { "five", new Float(5) },
			{ "six", new Float(6) }, { "seven", new Float(7) },
			{ "eight", new Float(8) }, { "nine", new Float(9) } };

	private static Hashtable<String, Float> NumberWordValueHash = null;

	// 6/1/2015: Invokes parser with new chart to analyze structure annotation
	// elements.
	public static Annotation getCoveringNarrativeAnnotation(
			StructureAnnotation sa) {
		return GrammarModule.CurrentGrammarModule
				.getCoveringNarrativeAnnotation(sa);
	}

	public static boolean isAncestor(Annotation a1, Annotation a2) {
		return a1.isAncestorOf(a2);
	}

	public static String getPatientName(Annotation annotation) {
		return annotation.getPatientName();
	}

	public static boolean hasPatientName(Annotation annotation, String patient) {
		return patient.equals(annotation.getPatientName());
	}

	public static boolean isSubtype(TypeConstant ctype, Object to) {
		TypeConstant stype = null;
		if (to instanceof String) {
			stype = TypeConstant.findByName(to.toString());
		} else if (to instanceof TypeConstant) {
			stype = (TypeConstant) to;
		}
		return ctype.isSubsumedBy(stype);
	}

	// 5/5/2015
	public static boolean ruleIDContainsString(Annotation annotation, String str) {
		Rule rule = annotation.getRule();
		return (rule != null && rule.getRuleID().toLowerCase().contains(str));
	}

	public static boolean ruleIDContainsStringRecursive(Annotation annotation,
			String str) {
		if (ruleIDContainsString(annotation, str)) {
			return true;
		}
		if (annotation.hasChildren()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				if (ruleIDContainsStringRecursive(child, str)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean tsc(Term t, Object item) {
		return termStringContains(t, item);
	}

	public static boolean termStringContains(Object t, Object item) {
		if (t != null && item != null) {
			if (t.toString().toLowerCase()
					.contains(item.toString().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static String gts(Object o) {
		return getTermString(o);
	}

	public static String getTermString(Object o) {
		return o.toString();
	}

	// 5/6/2015
	public static boolean termDescribedByTerm(Term t1, Term t2) {
		if (t1 != null && t2 != null && t1.getModifierSentences() != null) {
			for (RelationSentence mrs : t1.getModifierSentences()) {
				if (mrs.isBinary()) {
					if (t2.equals(mrs.getModifier())) {
						return true;
					}
					if (termDescribedByTerm(mrs.getModifier(), t2)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 4/16/2015
	public static Annotation getAVPairAttribute(Annotation annotation) {
		if (StructureAnnotation.isAttributeValuePair(annotation)) {
			return ((StructureAnnotation) annotation).getAVPairAttribute();
		}
		return null;
	}

	public static Annotation getAVPairValue(Annotation annotation) {
		if (StructureAnnotation.isAttributeValuePair(annotation)) {
			return ((StructureAnnotation) annotation).getAVPairValue();
		}
		return null;
	}

	public static String getAnnotationString(Annotation annotation) {
		if (annotation != null) {
			return annotation.getString();
		}
		return null;
	}

	public static boolean annotationStringEquals(Annotation annotation, Object o) {
		boolean rv = o.toString().equals(annotation.getText().toLowerCase());
		if (rv) {
			int x = 1;
		}
		return rv;
	}

	public static String getHeaderString(Annotation annotation) {
		return annotation.getHeaderString();
	}

	public static boolean headerContainsString(Annotation annotation, String str) {
		return annotation.headerContainsString(str);
	}

	public static boolean headerContainsAllOf(Annotation annotation,
			Vector<String> strs) {
		Sentence s = annotation.getSentenceAnnotation().getSentence();
		Header h = s.getHeader();
		String hstr = h.getText().toLowerCase();
		for (String vstr : strs) {
			if (!hstr.contains(vstr)) {
				return false;
			}
		}
		return true;
	}

	public static boolean headerContainsOneOf(Annotation annotation,
			Vector<String> strs) {
		Sentence s = annotation.getSentenceAnnotation().getSentence();
		Header h = s.getHeader();
		String hstr = h.getText().toLowerCase();
		for (String vstr : strs) {
			if (hstr.contains(vstr)) {
				return true;
			}
		}
		return false;
	}

	public static boolean headerContainsSingleSentenceWithSingleWord(
			Annotation annotation) {
		Sentence s = annotation.getSentenceAnnotation().getSentence();
		Header h = s.getHeader();
		if (h.getSentences().size() == 1) {
			Sentence ss = h.getSentences().firstElement();
			Vector<Token> wtokens = Token.gatherWordTokens(ss.getTokens());
			return (wtokens != null && wtokens.size() == 1);
		}
		return false;
	}

	// 4/1/2016: ??? What I want is a header with a single sentence that
	// contains
	// a single word...
	public static boolean headerCoversSingleWord(Annotation annotation) {
		Sentence s = annotation.getSentenceAnnotation().getSentence();
		return (s.getTokenLength() < 3);
	}

	public static boolean headerCoversSingleSentence(Annotation annotation) {
		Sentence s = annotation.getSentenceAnnotation().getSentence();
		Header h = s.getHeader();
		return h.getSentences().size() == 1;
	}

	// 4/2/2015: Get the number of tokens covered by an annotation.
	public static Float getTokenLength(Annotation annotation) {
		int length = annotation.getTokenLength();
		return new Float(length);
	}

	// 4/2/2015: NOT TESTED...
	public static Object getVariableValue(Annotation annotation, String vname) {
		return Variable.findValue(annotation.getVariables(), vname);
	}

	// 3/25/2015: Words in regular possessive form such as "patient's",
	// "daughter's" etc.
	public static boolean isPossessiveWord(Annotation annotation) {
		return annotation.isPossessiveWord();
	}

	// 3/14/2015: Datetime functions
	public static Calendar getCalendar(Annotation annotation) {
		if (annotation instanceof RegExprAnnotation) {
			RegExprAnnotation da = (RegExprAnnotation) annotation;
			return da.getCalendar();
		} else if (annotation.getChildAnnotations() != null) {
			for (Annotation child : annotation.getChildAnnotations()) {
				Calendar c = getCalendar(child);
				if (c != null) {
					return c;
				}
			}
		}
		return null;
	}

	public static String getDateString(Annotation annotation) {
		String str = "*";
		Calendar c = getCalendar(annotation);
		if (c != null) {
			str = "<Month=" + c.get(Calendar.MONTH) + ",Day="
					+ c.get(Calendar.DAY_OF_MONTH) + ",Year="
					+ c.get(Calendar.YEAR) + ">";
		}
		return str;
	}

	public static boolean timeIsBefore(Annotation a1, Annotation a2) {
		Calendar c1 = getCalendar(a1);
		Calendar c2 = getCalendar(a2);
		if (c1 != null && c2 != null) {
			return c1.before(c2);
		}
		return false;
	}

	public static int differenceBetweenDatesInDays(Annotation a1, Annotation a2) {
		Calendar c1 = getCalendar(a1);
		Calendar c2 = getCalendar(a2);
		if (c1 != null && c2 != null) {
			if (c1.after(c2)) {
				Calendar c = c1;
				c1 = c2;
				c2 = c;
			}
			long diff = c2.getTimeInMillis() - c1.getTimeInMillis();
			int days = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
			return days;
		}
		return -1;
	}

	public static boolean neg(Annotation annotation) {
		return isNegated(annotation);
	}

	public static boolean notneg(Annotation annotation) {
		return isNotNegated(annotation);
	}

	public static boolean isNotNegated(Annotation annotation) {
		return !isNegated(annotation);
	}

	public static boolean isNegated(Annotation annotation) {
		return hasPropertyValue(annotation, "directionality", "negated")
				|| conceptContainsString(annotation, "false");
	}

	public static boolean isAffirmed(Annotation annotation) {
		return hasPropertyValue(annotation, "directionality", "affirmed")
				|| conceptContainsString(annotation, "true");
	}

	public static Object gc(Annotation annotation) {
		return getConcept(annotation);
	}

	public static Object getConcept(Annotation annotation) {
		if (annotation != null) {
			return annotation.getConcept();
		}
		return null;
	}

	public static String gcs(Annotation annotation) {
		return getConceptString(annotation);
	}

	public static String getConceptString(Annotation annotation) {
		if (annotation.getConcept() != null) {
			return annotation.getConcept().toString();
		}
		return null;
	}

	// ///////////////////////////////////////////////////////
	// 7/7/2016
	public static Object getConceptRecursive(Annotation annotation,
			Object concept) {
		String cstr = concept.toString();
		Object c = annotation.getConcept();
		if (c != null
				&& c.toString().toLowerCase().contains(cstr.toLowerCase())) {
			return c;
		}
		if (annotation.hasChildren()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				if ((c = getConceptRecursive(child, cstr)) != null) {
					return c;
				}
			}
		}
		return null;
	}

	public static boolean containsAllConcepts(Annotation annotation,
			Vector concepts) {
		for (Object concept : concepts) {
			if (!hasConceptRecursive(annotation, concept)) {
				return false;
			}
		}
		return true;
	}

	public static boolean containsSomeConcepts(Annotation annotation,
			Vector concepts) {
		for (Object concept : concepts) {
			if (hasConceptRecursive(annotation, concept)) {
				return true;
			}
		}
		return false;
	}

	public static boolean hc(Annotation annotation, Object concept) {
		return hasConcept(annotation, concept, false);
	}

	public static boolean hcr(Annotation annotation, Object concept) {
		return hasConceptRecursive(annotation, concept);
	}

	public static boolean hasConceptRecursive(Annotation annotation,
			Object concept) {
		boolean rv = hasConcept(annotation, concept, true);
		return rv;
	}

	// 7/14/2016
	public static boolean hasType(Annotation annotation, TypeConstant type) {
		return hasType(annotation, type, false);
	}

	public static boolean hasTypeRecursive(Annotation annotation,
			TypeConstant type) {
		return hasType(annotation, type, true);
	}

	public static boolean hasConcept(Annotation annotation, Object concept) {
		return hasConcept(annotation, concept, false);
	}

	// TEST
	public static boolean hcsr(Annotation annotation, Vector concepts) {
		return hasConceptsRecursive(annotation, concepts);
	}

	public static boolean hasConceptsRecursive(Annotation annotation,
			Vector concepts) {
		for (Object concept : concepts) {
			if (!hasConcept(annotation, concept, true)) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasConcept(Annotation annotation, Object concept,
			boolean recursive) {
		if (annotation != null && concept != null) {
			Object c = annotation.getConcept();
			if (c != null && concept != null
					&& c.toString().equals(concept.toString())) {
				return true;
			}
			if (recursive && annotation.getChildAnnotations() != null) {
				for (Annotation child : annotation.getChildAnnotations()) {
					if (hasConcept(child, concept, recursive)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 8/3/2016
	public static Annotation getAnnotationWithConcept(Annotation annotation,
			Object concept) {
		if (annotation != null && concept != null) {
			Object c = annotation.getConcept();
			if (c != null && c.toString().equals(concept.toString())) {
				return annotation;
			}
			if (annotation.getChildAnnotations() != null) {
				for (Annotation child : annotation.getChildAnnotations()) {
					Annotation ca = getAnnotationWithConcept(child, concept);
					if (ca != null) {
						return ca;
					}
				}
			}
		}
		return null;
	}

	// 7/14/2016
	public static boolean hasType(Annotation annotation, TypeConstant type,
			boolean recursive) {
		if (annotation != null && type != null) {
			TypeConstant t = annotation.getType();
			if (type.equals(t)) {
				return true;
			}
			if (recursive && annotation.getChildAnnotations() != null) {
				for (Annotation child : annotation.getChildAnnotations()) {
					if (hasType(child, type, recursive)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean ccs(Object o, String cstr) {
		return conceptContainsString(o, cstr);
	}

	public static boolean conceptContainsString(Object o, String cstr) {
		return conceptContainsString(o, cstr, false);
	}

	public static boolean conceptStartsWithString(Annotation annotation,
			String cstr) {
		boolean rv = annotation.hasConcept()
				&& annotation.getConcept().toString().startsWith(cstr);
		return rv;
	}

	public static boolean ccsr(Object o, Object so) {
		return conceptContainsStringRecursive(o, so.toString());
	}

	public static boolean conceptContainsStringRecursive(Object o, String cstr) {
		boolean rv = conceptContainsString(o, cstr, true);
		return rv;
	}

	public static boolean conceptContainsString(Object o, String cstr,
			boolean recursive) {
		if (o != null && cstr != null) {
			if (o.toString().toLowerCase().contains(cstr.toLowerCase())) {
				return true;
			}
			if (o instanceof Annotation) {
				Annotation annotation = (Annotation) o;
				Object c = annotation.getConcept();
				if (c != null && conceptContainsString(c, cstr, recursive)) {
					return true;
				}
				if (recursive && annotation.hasChildren()) {
					for (Annotation child : annotation.getChildAnnotations()) {
						if (conceptContainsString(child, cstr, recursive)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static Object getProperty(Annotation annotation, String property) {
		if (annotation != null) {
			return annotation.getProperty(property);
		}
		return null;
	}

	public static Boolean setProperty(Annotation annotation, String pname,
			Object value) {
		annotation.setProperty(pname, value);
		return true;
	}

	public static Object gpr(Annotation annotation, String property) {
		return getPropertyRecursive(annotation, property);
	}

	public static Object getPropertyRecursive(Annotation annotation,
			String property) {
		Object o = getProperty(annotation, property);
		if (o == null && annotation.getChildAnnotations() != null) {
			for (Annotation child : annotation.getChildAnnotations()) {
				o = getPropertyRecursive(child, property);
				if (o != null) {
					break;
				}
			}
		}
		return o;
	}

	public static boolean copcs(Annotation annotation, String property,
			String str) {
		return conceptOrPropertyContainsString(annotation, property, str, false);
	}

	public static boolean copcsr(Annotation annotation, String property,
			String str) {
		return conceptOrPropertyContainsString(annotation, property, str, true);
	}

	public static boolean conceptOrPropertyContainsString(
			Annotation annotation, String property, String str) {
		return conceptOrPropertyContainsString(annotation, property, str, false);
	}

	public static boolean conceptOrPropertyContainsStringRecursive(
			Annotation annotation, String property, String str) {
		return conceptOrPropertyContainsString(annotation, property, str, true);
	}

	public static boolean conceptOrPropertyContainsString(
			Annotation annotation, String property, String str,
			boolean recursive) {
		return (conceptContainsString(annotation, str, recursive) || propertyValueContainsString(
				annotation, property, str, recursive));
	}

	public static boolean pvcs(Annotation annotation, String property,
			Object value) {
		return propertyValueContainsString(annotation, property, value, false);
	}

	public static boolean pvcsr(Annotation annotation, String property,
			Object value) {
		return propertyValueContainsStringRecursive(annotation, property, value);
	}

	public static boolean propertyValueContainsString(Annotation annotation,
			String property, Object value) {
		return propertyValueContainsString(annotation, property, value, false);
	}

	public static boolean propertyValueContainsStringRecursive(
			Annotation annotation, String property, Object value) {
		return propertyValueContainsString(annotation, property, value, true);
	}

	public static boolean propertyValueContainsString(Annotation annotation,
			String property, Object value, boolean recursive) {
		Object o = null;
		if (recursive) {
			o = getPropertyRecursive(annotation, property);
		} else {
			o = getProperty(annotation, property);
		}
		return (o != null && o.toString().toLowerCase()
				.contains(value.toString().toLowerCase()));
	}

	public static boolean hasPropertyRecursive(Annotation annotation,
			String property) {
		if (annotation.getProperty(property) != null) {
			return true;
		}
		if (annotation.hasChildren()) {
			for (Annotation child : annotation.getChildAnnotations()) {
				if (hasPropertyRecursive(child, property)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean hpv(Annotation annotation, String property,
			Object value) {
		return hasPropertyValue(annotation, property, value, false);
	}

	public static boolean hpvr(Annotation annotation, String property,
			Object value) {
		return hasPropertyValueRecursive(annotation, property, value);
	}

	public static boolean hasPropertyValue(Annotation annotation,
			String property, Object value) {
		return hasPropertyValue(annotation, property, value, false);
	}

	public static boolean hasPropertyValueRecursive(Annotation annotation,
			String property, Object value) {
		return hasPropertyValue(annotation, property, value, true);
	}

	public static boolean hasPropertyValue(Annotation annotation,
			String property, Object value, boolean recursive) {
		if (annotation != null && property != null) {
			Object o = annotation.getProperty(property);
			if (o != null && value != null) {
				if (o.equals(value) || value.equals(o)) {
					return true;
				}
				if (o instanceof Annotation) {
					Annotation other = (Annotation) o;
					if (other.hasConcept(value)) {
						return true;
					}
				}
			}
			if (recursive && annotation.getChildAnnotations() != null) {
				for (Annotation child : annotation.getChildAnnotations()) {
					if (hasPropertyValue(child, property, value, recursive)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean hcopvr(Annotation annotation, String property,
			Object value) {
		return hasConceptOrPropertyValue(annotation, property, value, true);
	}

	public static boolean hasConceptOrPropertyValue(Annotation annotation,
			String property, Object value, boolean recursive) {
		if (hasConcept(annotation, value.toString(), true)) {
			return true;
		}
		if (hasPropertyValue(annotation, property, value)) {
			return true;
		}
		if (recursive && annotation.getChildAnnotations() != null) {
			for (Annotation child : annotation.getChildAnnotations()) {
				if (hasConceptOrPropertyValue(child, property, value, recursive)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Float gnpv(Annotation annotation, String property) {
		return getNumericPropertyValue(annotation, property);
	}

	public static Float getNumericPropertyValue(Annotation annotation,
			String property) {
		Annotation child = (Annotation) getChildAnnotationViaProperty(
				annotation, property);
		if (child instanceof Annotation) {
			return getNumericValue(child);
		}
		return null;
	}

	private static Hashtable<String, Float> getNumberWordValueHash() {
		if (NumberWordValueHash == null) {
			NumberWordValueHash = new Hashtable();
			for (int i = 0; i < NumberWordValues.length; i++) {
				String word = (String) NumberWordValues[i][0];
				Float value = (Float) NumberWordValues[i][1];
				NumberWordValueHash.put(word, value);
			}
		}
		return NumberWordValueHash;
	}

	public static Float getNumericValue(Object o) {
		if (o instanceof Float) {
			return (Float) o;
		}
		if (o instanceof NumberAnnotation) {
			NumberAnnotation na = (NumberAnnotation) o;
			return (Float) na.getValue();
		}
		if (o instanceof Annotation) {
			Annotation annotation = (Annotation) o;
			if (annotation.hasChildren()) {
				for (Annotation child : annotation.getChildAnnotations()) {
					Float val = getNumericValue(child);
					if (val != null) {
						return val;
					}
				}
			}
			Float f = getNumberWordValueHash().get(annotation.getText());
			if (f != null) {
				return f;
			}
		}
		return null;
	}

	public static Float convertToFahrenheit(Object o) {
		if (o instanceof Float) {
			Float val = getNumericValue(o);
			float product = (float) 9 / (float) 5;
			if (val < 50) {
				val = product * val + 32;
			}
			return val;
		}
		return new Float(0);
	}

	public static Annotation getChildAnnotationViaProperty(
			Annotation annototion, String property) {
		if (property != null && annototion.getSemanticInterpretation() != null) {
			Interpretation si = annototion.getSemanticInterpretation();
			Object value = si.getProperty(property);
			if (value instanceof Annotation) {
				return (Annotation) value;
			}
			if (annototion.getChildAnnotations() != null) {
				for (Annotation child : annototion.getChildAnnotations()) {
					value = getChildAnnotationViaProperty(child, property);
					if (value instanceof Annotation) {
						return (Annotation) value;
					}
				}
			}
		}
		return null;
	}

	public static Annotation getTypedChildAnnotation(Annotation a, String tname) {
		if (a.hasTypeName(tname)) {
			return a;
		}
		if (a.getChildAnnotations() != null) {
			for (Annotation child : a.getChildAnnotations()) {
				Annotation rv = getTypedChildAnnotation(child, tname);
				if (rv != null) {
					return rv;
				}
			}
		}
		return null;
	}

	public static boolean asc(Annotation annotation, Vector<String> strs) {
		return annotationStringContains(annotation, strs);
	}

	public static boolean annotationStringContains(Annotation annotation,
			Vector<String> strs) {
		for (String str : strs) {
			if (annotation.getString().contains(str)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCondition(Annotation annotation) {
		TypeConstant gtype = annotation.getGeneralType();
		if (gtype != null && gtype.isCondition()) {
			return true;
		}
		return false;
	}

	// / 12/3/2014
	// public static int getDay(Date date) {
	// Calendar cal = Calendar.getInstance();
	// cal.setTime(date);
	// return date.getDay();
	// }

	public static int getSentenceIndex(Annotation a) {
		return a.getSentenceAnnotation().getSentence().getDocumentIndex();
	}

	public static int getStartTokenIndex(Annotation a) {
		return a.getTokenStart();
	}

	public static int getEndTokenIndex(Annotation a) {
		return a.getTokenEnd();
	}

	public static boolean sameSentenceIndex(Annotation a1, Annotation a2) {
		int si1 = getSentenceIndex(a1);
		int si2 = getSentenceIndex(a2);
		return si1 != -1 && si1 == si2;
	}

	public static boolean withinNSentences(Annotation a1, Annotation a2, Float n) {
		int si1 = getSentenceIndex(a1);
		int si2 = getSentenceIndex(a2);
		return (si1 <= si2 && (si2 - si1) <= n);
	}

	public static boolean beforeNTokens(Annotation a1, Annotation a2,
			Float numTokens) {
		if (sameSentenceIndex(a1, a2)) {
			int ti1 = a1.getTokenEnd();
			int ti2 = a2.getTokenStart();
			boolean rv = ti2 > ti1 && (ti2 - ti1) <= numTokens;
			return rv;
		}
		return false;
	}

	public static boolean fromSentenceStartNTokens(Annotation a, Float numTokens) {
		boolean rv = a.getRelativeTokenStart() <= numTokens;
		return rv;
	}

	public static boolean atSentenceStart(Annotation a) {
		int index = a.getRelativeTokenStart();
		boolean rv = (index == 0);
		return rv;
	}

	public static TypeConstant getType(Annotation a) {
		return a.getType();
	}

	public static boolean sameType(Annotation a1, Annotation a2) {
		return a1.getType() != null && a1.getType() == a2.getType();
	}

	public static boolean sameConcept(Annotation a1, Annotation a2) {
		return a1.getConcept() != null
				&& a1.getConcept().equals(a2.getConcept());
	}

	public static boolean typesAreSubsumedBy(TypeConstant t1, TypeConstant t2,
			Object t3) {
		TypeConstant unifier = null;
		if (t3 instanceof TypeConstant) {
			unifier = (TypeConstant) t3;
		} else {
			unifier = TypeConstant.findByName(t3.toString());
		}
		return TypeConstant.areSubsumedBy(t1, t2, unifier);
	}

	public static boolean annotationTypeSubstring(Annotation a, String substr) {
		if (a.getType() != null) {
			String tstr = a.getType().toString().toLowerCase();
			boolean rv = tstr.contains(substr);
			return rv;
		}
		return false;
	}

	public static boolean annotationHasType(Annotation a, Object o) {
		if (o instanceof TypeConstant) {
			TypeConstant type = (TypeConstant) o;
			boolean rv = type.equals(a.getType());
			if (rv) {
				int x = 1;
			}
			return rv;
		}
		return false;
	}

	public static boolean annotationIsBetween(Annotation a1, Annotation a2,
			Annotation a3) {
		if (a1 instanceof Annotation && a2 instanceof Annotation
				&& a3 instanceof Annotation) {
			if (a1.getTokenEnd() < a2.getTokenStart()
					&& a2.getTokenEnd() < a3.getTokenStart()) {
				return true;
			}
		}
		return false;
	}

	// 12/4/2014: Need to handle domain-specific functions elsewhere...
	public static boolean typeIsTempEVALEvent(TypeConstant type) {
		String tstr = type.toString();
		boolean rv = tstr.contains("event") || tstr.contains(":t0");
		return rv;
	}

	public static boolean anotationIsTempEVALEvent(Annotation a) {
		if (a.getType() != null) {
			String tstr = a.getType().toString();
			boolean rv = tstr.contains("event") || tstr.contains(":t0");
			return rv;
		}
		return false;
	}

	// Not tested
	public static boolean isSingleNumberTokenSentence(Annotation a) {
		WordSequenceAnnotation sa = a.getSentenceAnnotation();
		if (sa != null && sa.getTokenLength() == 1) {
			Token token = a.getTokens().firstElement();
			if (token.isNumber()) {
				return true;
			}
		}
		return false;
	}

	// 6/29/2015: Actions associated with ConText rules.
	public static void applyModifierProperties(Rule rule, Annotation annotation) {
		if (annotation != null && annotation.isInterpreted() && rule != null
				&& rule.getPropertyPredicates() != null) {
			Rule annotationRule = annotation.getRule();
			for (Vector pp : rule.getPropertyPredicates()) {
				String aname = (String) pp.firstElement();
				Object value = pp.lastElement();
				// 1/16/2018: If an annotation's rule explicitly remoes a
				// property, don't set it
				// externally, e.g. "Are you homeless? No" should not result in
				// NotHomeless, negated.
				// (This is not a full solution..)
				if (!(annotationRule.getPropertiesToRemove() != null && annotationRule
						.getPropertiesToRemove().contains(aname))) {
					annotation.getSemanticInterpretation().setProperty(aname,
							value);
				}
			}
		}
	}

	// 6/9/2016 TEST
	public static void invalidateSentence(Annotation annotation, String reason) {
		WordSequenceAnnotation wsa = annotation.getSentenceAnnotation();
		if (!(wsa.getSentence() instanceof CombinedHeaderSentence)) {
			wsa.setInvalidReason(reason);
			if (annotation.getRule() != null
					&& annotation.getRule().isDoDebug()) {
				// System.out.println("SENTENCE INVALIDATION: Annotation="
				// + annotation.getText()
				// + ", Sentence="
				// + annotation.getSentenceAnnotation().getSentence()
				// .getText());
				int x = 1;
			}
		}
	}

	public static boolean sentenceStartsWithString(Annotation annotation,
			String str) {
		return annotation.getSentenceAnnotation().getSentence().getText()
				.toLowerCase().startsWith(str);
	}

	public static boolean sentenceContainsString(Annotation annotation,
			String str) {
		return annotation.getSentenceAnnotation().getSentence().getText()
				.toLowerCase().contains(str);
	}

	public static boolean isInterpreted(Annotation annotation) {
		return annotation.isInterpreted();
	}

	// Need a better way to store / retrieve rule information.
	public static String getConTextValue(TagAnnotation tag,
			Annotation annotation, Rule rule) {
		if (!sameSentenceIndex(tag, annotation)) {
			return null;
		}
		Annotation first, second;
		if (tag.getSentenceTokenStart() < annotation.getSentenceTokenStart()) {
			first = tag;
			second = annotation;
		} else {
			first = annotation;
			second = tag;
		}
		if (Math.abs(second.getSentenceTokenEnd()
				- first.getSentenceTokenStart()) > rule.getWindow()) {
			return null;
		}
		String mdir = rule.getModifierDirection();
		Vector<Vector> preds = rule.getPropertyPredicates();
		if (preds == null) {
			return null;
		}
		String mvalue = (String) preds.firstElement().lastElement();
		if (Annotation.intervalContainsStrings(tag, annotation,
				rule.getStopWords())) {
			return null;
		}
		if ("forward".equals(mdir)
				&& tag.getTokenEnd() < annotation.getTokenStart()) {
			return mvalue;
		}
		if ("backward".equals(mdir)
				&& tag.getTokenStart() > annotation.getTokenEnd()) {
			return mvalue;
		}
		if ("bidirectional".equals(mdir)) {
			return mvalue;
		}
		return null;
	}

	public static String getConTextProperty(Rule rule) {
		String property = null;
		Vector<Vector> preds = rule.getPropertyPredicates();
		if (preds != null) {
			property = (String) preds.firstElement().firstElement();
		}
		return property;
	}

	public static boolean annotationsStringsNotEqual(Annotation a1,
			Annotation a2) {
		boolean rv = !a1.getString().toLowerCase()
				.equals(a2.getString().toLowerCase());
		return rv;
	}

	public static boolean isConjunct(Annotation annotation) {
		return annotation.isConjunct();
	}

	public static boolean isUMLSCondition(Annotation annotation) {
		if (annotation.getType() instanceof UMLSTypeConstant) {
			UMLSTypeConstant utc = (UMLSTypeConstant) annotation.getType();
			boolean result = utc.isCondition();
			return result;
		}
		return false;
	}

	public static boolean isUMLSLocation(Annotation annotation) {
		if (annotation.getType() instanceof UMLSTypeConstant) {
			UMLSTypeConstant utc = (UMLSTypeConstant) annotation.getType();
			boolean result = utc.isLocation();
			return result;
		}
		return false;
	}

	public static int getRatioTop(Annotation annotation) {
		String[] values = annotation.getText().split("/");
		if (values.length == 2) {
			return Integer.valueOf(values[0]).intValue();
		}
		return -1;
	}

	public static int getRatioBottom(Annotation annotation) {
		String[] values = annotation.getText().split("/");
		if (values.length == 2) {
			return Integer.valueOf(values[1]).intValue();
		}
		return -1;
	}

	public static boolean isHistorical(Annotation annotation) {
		String value = (String) annotation.getProperty("temporality");
		return ("historical".equals(value));
	}

	public static boolean hasNoPunctuation(Annotation annotation) {
		for (Token token : annotation.getTokens()) {
			if (token.isPunctuation()) {
				return false;
			}
		}
		return true;
	}

	// 9/18/2015
	public static boolean containsPOS(Annotation annotation, String pos) {
		Token first = annotation.getTokens().firstElement();
		if (first != null && pos != null && first.isWord()) {
			Word word = first.getWord();
			if (word == null
					|| word.getPartsOfSpeech().contains(pos.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsTarget(Annotation annotation) {
		return annotation.containsTargetConcept();
	}

	// public static boolean childContainsTarget(Annotation annotation) {
	// return annotation.childContainsTargetConcept();
	// }

	// 1/18/2016
	public static boolean usesCombinedHeaderSentence(Annotation annotation) {
		Sentence s = annotation.getSentenceAnnotation().getSentence();
		return s instanceof CombinedHeaderSentence;
	}

	// 2/2/2016: For kloogy test in negation rule.
	public static boolean firstWordBeginsWithUppercase(Annotation annotation) {
		char c = annotation.getText().charAt(0);
		if (Character.isUpperCase(c)) {
			int x = 1;
		}
		return Character.isUpperCase(c);
	}

	// 3/4/2016 Test:
	public static boolean containsComma(Annotation annotation) {
		Vector<Token> tokens = annotation.getSentenceAnnotation().getSentence()
				.getTokens();
		for (Token token : tokens) {
			if (token.isComma()) {
				return true;
			}
		}
		return false;
	}

	public static boolean atct(Annotation annotation, String str) {
		return annotationTextContainsString(annotation, str);
	}

	public static boolean annotationTextContainsString(Annotation annotation,
			String str) {
		String text = annotation.getText().toLowerCase();
		boolean rv = text.contains(str.toLowerCase());
		return rv;
	}

	// FOR VINCI
	public static boolean annotationTypeSubsumedBy(Annotation annotation,
			String tname) {
		TypeConstant type = TypeConstant.getType(tname);
		return annotation.isTypeSubsumedBy(type);
	}

	public static boolean containsUnderline(Annotation annotation) {
		Vector<Token> tokens = annotation.getSentenceAnnotation().getSentence()
				.getTokens();
		for (Token token : tokens) {
			if (token.getType() == Token.UNDERLINE) {
				System.out.println(annotation.toString()
						+ ": contains underline");
				return true;
			}
		}
		return false;
	}

	public static boolean atctv(Annotation annotation, Vector<String> strs) {
		for (String str : strs) {
			if (atct(annotation, str)) {
				return true;
			}
		}
		return false;
	}

	public static Float getSentenceWordTokenLength(Object o) {
		if (o instanceof Annotation) {
			Annotation annotation = (Annotation) o;
			int len = annotation.getSentenceAnnotation().getWordTokens().size();
			return new Float(len);
		}
		return new Float(0);
	}

	public static boolean ruleHasComplexConcept(Rule rule) {
		boolean rv = false;
		if (rule != null) {
			rv = rule.isComplexConcept();
		}
		return rv;
	}

	public static float getGoodness(Annotation annotation) {
		return (float) annotation.getGoodness();
	}

	// 7/7/2016 IMPORTANT: THIS IS CURRENTLY THE ONLY WAY TO NEGATE SENTENCES IN
	// FCIE.
	public static boolean isJavaNot(Object o) {
		if (o instanceof Boolean) {
			boolean rv = (Boolean) o;
			return !((Boolean) o).booleanValue();
		}
		return false;
	}

	// 8/3/2016
	public static boolean isJavaOr(Object o) {
		if (o instanceof Vector) {
			for (Object so : (Vector) o) {
				if (so instanceof Term) {
					so = ((Term) so).eval();
				}
				if (so instanceof Boolean) {
					Boolean b = (Boolean) so;
					if (b) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isJavaAnd(Object o) {
		if (o instanceof Vector) {
			for (Object so : (Vector) o) {
				if (so instanceof Boolean) {
					Boolean b = (Boolean) so;
					if (!b) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	public static boolean doTest(Annotation annotation, Object item) {
		System.out.printf("IE: Annotation=%s, Item=%s\n", annotation.getText(),
				item);
		return true;
	}

	// 1/17/2018: Check for newline between negation term and negated term.
	public static boolean containsNewlineBetweenAnnotations(Annotation a1,
			Annotation a2) {
		String dtext = a1.getDocument().getText();
		Annotation start, end;
		if (a1.getTextStart() < a2.getTextStart()) {
			start = a1;
			end = a2;
		} else {
			start = a2;
			end = a1;
		}
		for (int i = start.getTextEnd() + 1; i < end.getTextStart(); i++) {
			char c = dtext.charAt(i);
			if (c == '\n') {
				return true;
			}
		}
		return false;
	}
	
	// 1/22/2018
	// For "discharge instruction: xxx", only want to count "home" if it appears in 
	// the first sentence of the header, e.g. "discharge:  to home.  xxx"
	public static boolean annotationInFirstHeaderSentence(Annotation a) {
		Sentence s = a.getSentenceAnnotation().getSentence();
		Header h = s.getHeader();
		return (s == h.getSentences().firstElement());
	}

}
