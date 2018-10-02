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
package moonstone.rule;

import java.util.Comparator;

public class RuleUsage {

	private Rule rule = null;
	private int matchedCount = 0;
	private int fullyMatchedCount = 0;
	private float ratio = 0f;

	public RuleUsage(Rule rule, int matched, int fullymatched) {
		this.rule = rule;
		this.matchedCount = matched;
		this.fullyMatchedCount = fullymatched;
		this.ratio = (float) fullymatched / (float) matched;
	}

	public static class RuleUsageFullMatchSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			int fm1 = ((RuleUsage) o1).fullyMatchedCount;
			int fm2 = ((RuleUsage) o2).fullyMatchedCount;
			if (fm1 > fm2) {
				return -1;
			}
			if (fm1 < fm2) {
				return 1;
			}
			return 0;
		}
	}

	public static class RuleUsageMatchSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			int m1 = ((RuleUsage) o1).matchedCount;
			int m2 = ((RuleUsage) o2).matchedCount;
			if (m1 > m2) {
				return -1;
			}
			if (m1 < m2) {
				return 1;
			}
			return 0;
		}
	}

	public static class RuleUsageRatioSorter implements Comparator {
		public int compare(Object o1, Object o2) {
			float r1 = ((RuleUsage) o1).ratio;
			float r2 = ((RuleUsage) o2).ratio;
			if (r1 > r2) {
				return -1;
			}
			if (r1 < r2) {
				return 1;
			}
			return 0;
		}
	}

	public String toString() {
		String str = "Rule=" + this.rule.getRuleID() + "("
				+ this.rule.getSourceFileName() + "),Matched="
				+ this.matchedCount + ",FullyMatched=" + this.fullyMatchedCount
				+ ",Ratio=" + this.ratio;
		return str;
	}

	public Rule getRule() {
		return this.rule;
	}

	public int getMatchedCount() {
		return matchedCount;
	}

	public int getFullyMatchedCount() {
		return fullyMatchedCount;
	}

	public float getRatio() {
		return ratio;
	}

}
