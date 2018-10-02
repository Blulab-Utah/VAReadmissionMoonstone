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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtils {

	public static String getTuffyString(String str) {
		if (str != null && str.length() > 2) {
			if (!Character.isLetterOrDigit(str.charAt(0))) {
				str = str.substring(1);
			}
			if (!Character.isLetterOrDigit(str.charAt(str.length() - 1))) {
				str = str.substring(0, str.length() - 1);
			}
			str = Character.toUpperCase(str.charAt(0)) + str.substring(1);
		}
		return str;
	}

	public static String firstCharacterToUpperCase(String str) {
		if (str != null) {
			str = Character.toUpperCase(str.charAt(0)) + str.substring(1);
		}
		return str;
	}

	public static boolean isAllUpperCase(String str) {
		if (str != null && str.length() > 1) {
			for (int i = 0; i < str.length(); i++) {
				char ch = str.charAt(i);
				if (Character.isLowerCase(ch)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static String getStringToWhitespace(String line, int start) {
		String str = "";
		str = "";
		for (int i = start; i < line.length(); i++) {
			char c = line.charAt(i);
			if (!Character.isWhitespace(c)) {
				str += c;
			} else {
				break;
			}
		}
		return str;
	}

	public static String getBlanks(int num) {
		String str = "";
		for (int i = 0; i < num; i++) {
			str += "  ";
		}
		return str;
	}

	public static boolean containsIgnoreCase(Vector<String> v, String str) {
		if (v != null) {
			for (String vstr : v) {
				if (vstr.equalsIgnoreCase(str)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getStackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String str = sw.toString();
		if (str != null && str.length() > 1000) {
			str = str.substring(0, 1000);
		}
		return str;
	}

	public static boolean stringsContainSameNumber(String str1, String str2) {
		if (str1 != null && str2 != null) {
			Vector<String> v1 = getNumberSnippets(str1);
			Vector<String> v2 = getNumberSnippets(str2);
			if (v1 != null && v2 != null) {
				for (String nstr1 : v1) {
					if (nstr1.length() >= 3 && v2.contains(nstr1)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static Vector<String> getNumberSnippets(String str) {
		Vector<String> v = null;
		if (str != null) {
			StringBuffer sb = new StringBuffer();
			char lastc = (char) -1;
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (Character.isDigit(c)) {
					if (Character.isDigit(lastc)) {
						sb.append(c);
					} else {
						if (sb.length() > 0) {
							v = VUtils.add(v, sb.toString());
						}
						sb.delete(0, sb.length());
						sb.append(c);
					}
				} else {
					if (sb.length() > 0) {
						v = VUtils.add(v, sb.toString());
					}
					sb.delete(0, sb.length());
				}
				lastc = c;
			}
			if (sb.length() > 0) {
				v = VUtils.add(v, sb.toString());
			}
		}
		return v;
	}

	public static boolean stringsEndInSameDigits(String s1, String s2,
			int mincount) {
		if (s1 != null && s2 != null
				&& Character.isDigit(s1.charAt(s1.length() - 1))
				&& Character.isDigit(s2.charAt(s2.length() - 1))) {
			int count = 0;
			for (int i = s1.length() - 1, j = s2.length() - 1; i >= 0 && j >= 0; i--, j--, count++) {
				char c1 = s1.charAt(i);
				char c2 = s2.charAt(j);
				if (!Character.isDigit(c1) && !Character.isDigit(c2)) {
					return count > mincount;
				}
				if (c1 != c2) {
					return false;
				}
			}
			return count > mincount;
		}
		return false;
	}

	public static boolean stringContainsNonLetterCharacter(String str) {
		if (str != null) {
			for (int i = 0; i < str.length(); i++) {
				if (!Character.isLetter(str.charAt(i))) {
					return true;
				}
			}
		}
		return false;
	}

	public static String wrapForSQL(Vector v) {
		StringBuffer sb = new StringBuffer();
		if (v != null) {
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				sb.append("\'" + e.nextElement().toString() + "\'");
				if (e.hasMoreElements()) {
					sb.append(", ");
				}
			}
		}
		return sb.toString();
	}

	public static boolean stringEquals(String s1, String s2) {
		return s1 != null && s1.equals(s2);
	}

	public static boolean stringEquals(String str1, String str2, int start) {
		if (start + str2.length() > str1.length()) {
			return false;
		}
		for (int i = start, j = 0; j < str2.length(); i++, j++) {
			if (str1.charAt(i) != str2.charAt(j)) {
				return false;
			}
		}
		return true;
	}

	public static boolean matchesEndSubstring(String str, String toMatch) {
		if (str != null && toMatch != null && str.length() > toMatch.length()) {
			String lcstr = str.toLowerCase();
			String tmstr = toMatch.toLowerCase();
			int index = lcstr.lastIndexOf(tmstr);
			if (index >= 0 && (index + tmstr.length()) == lcstr.length()) {
				return true;
			}
		}
		return false;
	}

	public static int getMatchingStringIndex(String[] strings, String toMatch) {
		for (int i = 0; i < strings.length; i++) {
			if (toMatch.equals(strings[i])) {
				return i;
			}
		}
		return -1;
	}

	public static int extractInteger_BEFORE_4_24_2017(String str) {
		if (str != null && Character.isDigit(str.charAt(0))) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < str.length(); i++) {
				if (Character.isDigit(str.charAt(i))) {
					sb.append(str.charAt(i));
				} else {
					break;
				}
			}
			if (sb.toString().length() > 0) {
				return Integer.parseInt(sb.toString());
			}
		}
		return -1;
	}
	
	public static int extractInteger(String str) {
		if (str != null) {
			StringBuffer sb = new StringBuffer();
			boolean inNumber = false;
			for (int i = 0; i < str.length(); i++) {
				char c = str.charAt(i);
				if (Character.isDigit(c)) {
					inNumber = true;
					sb.append(c);
				} else if (inNumber) {
					break;
				}
			}
			if (sb.toString().length() > 0) {
				return Integer.parseInt(sb.toString());
			}
		}
		return -1;
	}

	public static String trimAllWhiteSpace(String str) {
		Vector v = new Vector(0);
		String s = "";
		String newstr = null;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isWhitespace(c)) {
				if (s.length() > 0) {
					v.add(s);
					s = "";
				}
			} else {
				s += c;
			}
		}
		if (s.length() > 0) {
			v.add(s);
		}
		if (v.size() > 0) {
			newstr = stringListConcat(v, " ");
		}
		return newstr;
	}

	// 2/22/2015
	public static String convertToLettersDigitsAndSpaces(String text) {
		StringBuffer sb = new StringBuffer();
		if (text != null) {
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (Character.isLetter(c) || Character.isDigit(c)) {
					sb.append(c);
				} else {
					sb.append(' ');
				}
			}
		}
		return sb.toString();
	}

	public static String appendWithNewline(String result, String str) {
		if (result == null) {
			result = str;
		} else {
			result += "\n" + str;
		}
		return result;
	}

	public static String addNewlines(String text) {
		StringBuffer sb = new StringBuffer();
		int j = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (j > 80 && Character.isWhitespace(c)) {
				j = 0;
				sb.append('\n');
			} else if (c == '\n') {
				sb.append("\n\n");
			} else {
				sb.append(c);
			}
			j++;
		}
		return sb.toString();
	}

	// 1/16/2013
	public static Vector<String> stringList(String str, String delim) {
		Vector rv = null;
		if (str != null && delim != null && str.length() > delim.length()) {
			Pattern pattern = Pattern.compile(delim);
			Matcher m = pattern.matcher(str);
			int index = 0;
			while (m.find()) {
				int start = m.start();
				int end = m.end();
				if (index < start) {
					String substr = str.substring(index, start);
					rv = VUtils.add(rv, substr);
				}
				index = end;
			}
			if (index < str.length()) {
				String substr = str.substring(index);
				rv = VUtils.add(rv, substr);
			}
		}
		return rv;
	}

	public static Vector stringList(String str) {
		return stringList(str, '\n');
	}

	public static Vector<String> stringList(String str, char delim) {
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

	// 10/18/2013
	public static Vector<String> stringList(char[] chars, char delim) {
		Vector rv = null;
		if (chars != null) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < chars.length; i++) {
				char c = chars[i];
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

	public static String stringListConcat(Vector v, String delim) {
		String str = null;
		if (v != null) {
			StringBuffer sb = new StringBuffer();
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				Object o = e.nextElement();
				sb.append(o.toString());
				if (e.hasMoreElements()) {
					sb.append(delim);
				}
			}
			str = sb.toString();
		}
		return str;
	}

	public static Vector trimAll(Vector strings) {
		Vector newstrings = null;
		if (strings != null && strings.size() > 0) {
			newstrings = new Vector(0);
			for (Enumeration e = strings.elements(); e.hasMoreElements();) {
				String s = (String) e.nextElement();
				s = s.trim();
				s = s.toLowerCase();
				newstrings.add(s);
			}
		}
		return newstrings;
	}

	public static String trim(String str, char[] trimChars, boolean letterOnly) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			boolean found = false;
			for (int j = 0; !found && j < trimChars.length; j++) {
				if (c == trimChars[j]) {
					found = true;
				}
			}
			if (!found && (!letterOnly || Character.isLetter(c))) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static boolean isVariable(String str) {
		return (str != null && str.charAt(0) == '?');
	}

	public static boolean isQuoted(String str) {
		return str != null && str.charAt(0) == '"';
	}

	public static String addQuotes(String str) {
		str = removeQuotes(str);
		return '"' + str + '"';
	}

	public static String removeQuotes(String str) {
		return removeChar(str, '"');
	}

	public static String removeWhitespace(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String removeNonAlphaDigitSpaceCharacters(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == ' ' || Character.isDigit(c) || Character.isLetter(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String replaceNonAlphaNumericCharactersWithSpaces(String str) {
		StringBuffer sb = new StringBuffer();
		boolean lastBlank = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isDigit(c) || Character.isLetter(c)) {
				sb.append(c);
				lastBlank = false;
			} else if (!lastBlank) {
				sb.append(" ");
				lastBlank = true;
			}
		}
		return sb.toString();
	}
	
	public static String replaceChars(String str, char target, char replace) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == target) {
				c = replace;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static String replaceNonAlphaNumericCharactersWithDelim(String str,
			char delim) {
		StringBuffer sb = new StringBuffer();
		boolean lastNonAlpha = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isDigit(c) || Character.isLetter(c)) {
				sb.append(c);
				lastNonAlpha = false;
			} else if (!lastNonAlpha) {
				sb.append(delim);
				lastNonAlpha = true;
			}
		}
		return sb.toString();
	}

	public static String replaceNonDelimNonAlphaNumericCharactersWithSpaces(
			String str, char delim) {
		StringBuffer sb = new StringBuffer();
		boolean lastBlank = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isDigit(c) || Character.isLetter(c) || c == delim) {
				sb.append(c);
				lastBlank = false;
			} else if (!lastBlank) {
				sb.append(" ");
				lastBlank = true;
			}
		}
		return sb.toString();
	}

	public static String removeNonAlphaDigitCharacters(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isDigit(c) || Character.isLetter(c)) {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String removeNonAlphaDigitUnderlineCharacters(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isDigit(c) || Character.isLetter(c) || c == '_') {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String removeChar(String str, char c) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != c) {
				sb.append(str.charAt(i));
			}
		}
		return sb.toString();
	}

	public static String createBlankString(int length, String suffix) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			sb.append(' ');
		}
		if (suffix != null) {
			sb.append(suffix);
		}
		return sb.toString();
	}

	public static class LengthSorterAscending implements Comparator {
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			if (s1.length() < s2.length()) {
				return -1;
			}
			if (s2.length() < s1.length()) {
				return 1;
			}
			return 0;
		}
	}

	public static class LengthSorterDescending implements Comparator {
		public int compare(Object o1, Object o2) {
			try {
				String s1 = (String) o1;
				String s2 = (String) o2;
				if (s2.length() < s1.length()) {
					return -1;
				}
				if (s1.length() < s2.length()) {
					return 1;
				}
				return 0;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}
	}

	public static String addEscape(String str) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\'' || c == '\"') {
				sb.append('\\');
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static String textToHtml(String s) {
		StringBuilder builder = new StringBuilder();
		boolean previousWasASpace = false;
		for (char c : s.toCharArray()) {
			if (c == ' ') {
				if (previousWasASpace) {
					// builder.append("&nbsp;");
					builder.append("&#160;");
					previousWasASpace = false;
					continue;
				}
				previousWasASpace = true;
			} else {
				previousWasASpace = false;
			}
			switch (c) {
			case '<':
				builder.append("&lt;");
				break;
			case '>':
				builder.append("&gt;");
				break;
			case '&':
				builder.append("&amp;");
				break;
			case '"':
				builder.append("&quot;");
				break;
			case '\n':
				builder.append("<br>");
				break;
			// We need Tab support here, because we print StackTraces as HTML
			case '\t':
				builder.append("&nbsp; &nbsp; &nbsp;");
				break;
			default:
				builder.append(c);

			}
		}
		String converted = builder.toString();
		String str = "(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:\'\".,<>?Â«Â»â€œâ€�â€˜â€™]))";
		Pattern patt = Pattern.compile(str);
		Matcher matcher = patt.matcher(converted);
		converted = matcher.replaceAll("<a href=\"$1\">$1</a>");
		return converted;
	}

}
