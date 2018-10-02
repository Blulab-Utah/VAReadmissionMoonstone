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

import java.util.Comparator;

public class NumUtils {
	
	// e.g. "5/6/2014"
	public static boolean isDateString(String text) {
		if (text != null) {
			int slashcount = 0;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (!(Character.isDigit(c) || c == '/')) {
					return false;
				}
				if (c == '/') {
					slashcount++;
				}
			}
			return slashcount == 1;
		}
		return false;
	}

	public static class InverseIntegerSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			Integer i1 = (Integer) o1;
			Integer i2 = (Integer) o2;
			if (i1 > i2) {
				return -1;
			}
			if (i1 < i2) {
				return 1;
			}
			return 0;
		}
	}

}
