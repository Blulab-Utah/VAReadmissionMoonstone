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
package moonstone.probability;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import tsl.knowledge.engine.KnowledgeEngine;
import tsl.startup.StartupParameters;
import tsl.utilities.FUtils;
import tsl.utilities.HUtils;
import moonstone.annotation.Annotation;
import moonstone.rule.Rule;
import moonstone.rulebuilder.MoonstoneRuleInterface;

public class LPCFG {
	private MoonstoneRuleInterface moonstoneRuleInterface = null;
	private String LPCFGFileDirectoryPath = null;
	private String LPCFGTemporaryFilePath = null;
	private Hashtable<String, Integer> oldTokenCountHash = new Hashtable();
	private Hashtable<String, Integer> newTokenCountHash = new Hashtable();
	private boolean useRuleConditionalsOnlyInProbabilityCalculation = false;

	private static String LPCFGMeaningTokenCountFileDirectoryParameter = "LCPFGMeaningTokenCountFiles";
	private static String LPCFGMeaningTokenCountTemporaryFileParameter = "LCPFGMeaningTokenTemporaryFile";
	private static String LCPFGMeaningTokenDelimiter = "##";
	private static String LCPFGTokenCountDelimiter = "==";

	/*****************
	 * NOTES:
	 * 
	 * prob(rule) = count(rule) / count(allRules)
	 * 
	 * prob(rule | subrules + content) = count(rule & subrules & content)
	 * 
	 * prob(rule | content) == count(rule + content) / count (content)
	 * 
	 * prob(rule | subrules) == count(rule + subrules) / count(subrules)
	 * 
	 * LCPFG formula: prob(LHS(rule) | content) * product(prob(rule | content))
	 * 
	 * PROBLEM: There isn't an LHS(rule), so I'm starting with just the product
	 * for now.
	 ********************/

	public LPCFG(MoonstoneRuleInterface msri) {
		this.moonstoneRuleInterface = msri;
		String dname = msri.getKnowledgeEngine().getStartupParameters()
				.getPropertyValue(LPCFGMeaningTokenCountFileDirectoryParameter);
		if (dname != null) {
			this.LPCFGFileDirectoryPath = msri.getResourceDirectoryName() + File.separatorChar + dname;
		}
		String tfname = msri.getKnowledgeEngine().getStartupParameters()
				.getPropertyValue(LPCFGMeaningTokenCountTemporaryFileParameter);
		if (tfname != null) {
			this.LPCFGTemporaryFilePath = msri.getResourceDirectoryName() + File.separatorChar + tfname;
		}
		this.loadMeaningCounts();
	}
	
	// 6/30/2016
	public void calculatePCFGProbability(Annotation annotation) {
		String ruleToken = this.getPCFGRuleToken(annotation);
		Rule rule = annotation.getRule();
		if (ruleToken == null || !annotation.hasChildren() || !annotation.hasRule()) {
			annotation.setPCFGProbability(1);
			annotation.setSumOfPCFGProbabilities(1);
			return;
		}
		double sumprob = 0;
		int dcount = 0;
		Integer totalRuleSubruleCount = 0;
		for (Annotation child : annotation.getChildAnnotations()) {
			child.getPCFGProbability(); // Don't need this- done automatically
											// at annotation creation.
			dcount += child.getNumberOfPCFGPaths();
			dcount++;
			sumprob += child.getSumOfPCFGProbabilities();
			String subruleToken = this.getPCFGRuleToken(child);
			String ruleSubruleToken = this.getPCFGRuleSubruleToken(annotation, child);
			if (subruleToken != null && ruleSubruleToken != null) {
				Integer subruleCount = this.getTokenCountHash(subruleToken);
				Integer ruleSubruleCount = this.getTokenCountHash(ruleSubruleToken);
				totalRuleSubruleCount += ruleSubruleCount;
				double ruleGivenSubruleProb = 0;
				if (ruleSubruleCount > 0) {
					ruleGivenSubruleProb = ruleSubruleCount.doubleValue() / subruleCount.doubleValue();
				}
				sumprob += ruleGivenSubruleProb;
			}
		}
		annotation.setNumberOfPCFGPaths(dcount);
		annotation.setSumOfPCFGProbabilities(sumprob);

		double pcfgweight = 0.8;
		double specweight = 0.1;
		double abstractweight = -0.1;
		double lpcfgfactor = sumprob / dcount;
		double specfactor = (rule != null && rule.isSpecialized() ? 1 : 0);
		double abstractfactor = (rule != null && rule.isComplexConcept() ? 1 : 0);
		double prob = pcfgweight * lpcfgfactor + specweight * specfactor + abstractweight * abstractfactor;
		annotation.setPCFGProbability(prob);
	}

	public void calculateLPCFGProbability(Annotation annotation) {
		String ruleToken = this.getLPCFGRuleToken(annotation);
		Rule rule = annotation.getRule();
		if (ruleToken == null || !annotation.hasChildren() || !annotation.hasRule()) {
			annotation.setLPCFGProbability(1);
			annotation.setSumOfLPCFGProbabilities(1);
			return;
		}
		double sumprob = 0;
		int dcount = 0;
		Integer totalRuleSubruleCount = 0;
		for (Annotation child : annotation.getChildAnnotations()) {
			child.getLPCFGProbability(); // Don't need this- done automatically
											// at annotation creation.
			dcount += child.getNumberOfLPCFGPaths();
			dcount++;
			sumprob += child.getSumOfLPCFGProbabilities();
			String subruleToken = this.getLPCFGRuleToken(child);
			String ruleSubruleToken = this.getLPCFGRuleSubruleToken(annotation, child);
			if (subruleToken != null && ruleSubruleToken != null) {
				Integer subruleCount = this.getTokenCountHash(subruleToken);
				Integer ruleSubruleCount = this.getTokenCountHash(ruleSubruleToken);
				totalRuleSubruleCount += ruleSubruleCount;
				double ruleGivenSubruleProb = 0;
				if (ruleSubruleCount > 0) {
					ruleGivenSubruleProb = ruleSubruleCount.doubleValue() / subruleCount.doubleValue();
				}
				sumprob += ruleGivenSubruleProb;
			}
		}
		annotation.setNumberOfLPCFGPaths(dcount);
		annotation.setSumOfLPCFGProbabilities(sumprob);

		// 6/17/2016 TEST:
		double lpcfgweight = 0.8;
		double specweight = 0.1;
		double abstractweight = -0.1;
		double lpcfgfactor = sumprob / dcount;
		double specfactor = (rule != null && rule.isSpecialized() ? 1 : 0);
		double abstractfactor = (rule != null && rule.isComplexConcept() ? 1 : 0);
		double prob = lpcfgweight * lpcfgfactor + specweight * specfactor + abstractweight * abstractfactor;
		annotation.setLPCFGProbability(prob);
	}

	public Integer getTokenCountHash(String token) {
		if (token != null) {
			Integer rv = this.oldTokenCountHash.get(token);
			if (rv == null) {
				rv = this.newTokenCountHash.get(token);
			}
			if (rv != null) {
				return rv;
			}
		}
		return new Integer(0);
	}
	
	public void processAnnotationMeaningCounts(Annotation annotation) {
		this.processPCFGMeaningCounts(annotation);
		this.processLPCFGMeaningCounts(annotation);
		this.storeMeaningCounts();
	}

	public void processLPCFGMeaningCounts(Annotation annotation) {
		String ptoken = getLPCFGRuleToken(annotation);
		if (ptoken != null) {
			HUtils.incrementCount(this.newTokenCountHash, ptoken);
			if (annotation.hasChildren()) {
				for (Annotation child : annotation.getChildAnnotations()) {
					processLPCFGMeaningCounts(child);
					String pctoken = this.getLPCFGRuleSubruleToken(annotation, child);
					if (pctoken != null) {
						HUtils.incrementCount(this.newTokenCountHash, pctoken);
					}
				}
			}
		}
	}
	
	public void processPCFGMeaningCounts(Annotation annotation) {
		String ptoken = getPCFGRuleToken(annotation);
		if (ptoken != null) {
			HUtils.incrementCount(this.newTokenCountHash, ptoken);
			if (annotation.hasChildren()) {
				for (Annotation child : annotation.getChildAnnotations()) {
					processPCFGMeaningCounts(child);
					String pctoken = this.getPCFGRuleSubruleToken(annotation, child);
					if (pctoken != null) {
						HUtils.incrementCount(this.newTokenCountHash, pctoken);
					}
				}
			}
		}
	}

	public String getLPCFGRuleToken(Annotation annotation) {
		String str = null;
		if (annotation.hasRule()) {
			str = annotation.getRule().getRuleID();
			if (annotation.getConcept() != null) {
				str += LCPFGMeaningTokenDelimiter + annotation.getConcept();
			}
		}
		return str;
	}
	
	public String getPCFGRuleToken(Annotation annotation) {
		String str = null;
		if (annotation.hasRule()) {
			str = annotation.getRule().getRuleID();
		}
		return str;
	}

	public String getLPCFGRuleSubruleToken(Annotation parent, Annotation child) {
		String str = null;
		String ruletoken = this.getLPCFGRuleToken(parent);
		String childruletoken = this.getLPCFGRuleToken(child);
		if (ruletoken != null && childruletoken != null) {
			str = ruletoken + LCPFGMeaningTokenDelimiter + childruletoken;
		}
		return str;
	}
	
	public String getPCFGRuleSubruleToken(Annotation parent, Annotation child) {
		String str = null;
		String ruletoken = this.getPCFGRuleToken(parent);
		String childruletoken = this.getPCFGRuleToken(child);
		if (ruletoken != null && childruletoken != null) {
			str = ruletoken + LCPFGMeaningTokenDelimiter + childruletoken;
		}
		return str;
	}

	public void loadMeaningCounts() {
		this.oldTokenCountHash = new Hashtable();
		StartupParameters sp = KnowledgeEngine.getCurrentKnowledgeEngine().getStartupParameters();
		Vector<File> files = FUtils.readFilesFromDirectory(this.LPCFGFileDirectoryPath);
		if (files != null) {
			for (File file : files) {
				try {
					BufferedReader in = new BufferedReader(new FileReader(file));
					String line = null;
					while ((line = in.readLine()) != null) {
						String[] strs = line.split(LCPFGTokenCountDelimiter);
						if (strs.length == 2) {
							String key = strs[0];
							int value = Integer.valueOf(strs[1]);
							Integer oldvalue = this.oldTokenCountHash.get(key);
							if (oldvalue != null) {
								value += oldvalue;
							}
							this.oldTokenCountHash.put(key, value);
						}
					}
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void storeMeaningCounts() {
		try {
			if (!this.newTokenCountHash.isEmpty()) {
				BufferedWriter out = new BufferedWriter(new FileWriter(this.LPCFGTemporaryFilePath));
				for (Enumeration<String> e = this.newTokenCountHash.keys(); e.hasMoreElements();) {
					String mtoken = e.nextElement();
					int count = HUtils.getCount(this.newTokenCountHash, mtoken);
					String str = mtoken + LCPFGTokenCountDelimiter + count + "\n";
					out.write(str);
				}
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isUseRuleConditionalsOnlyInProbabilityCalculation() {
		return useRuleConditionalsOnlyInProbabilityCalculation;
	}

	public void setUseRuleConditionalsOnlyInProbabilityCalculation(
			boolean useRuleConditionalsOnlyInProbabilityCalculation) {
		this.useRuleConditionalsOnlyInProbabilityCalculation = useRuleConditionalsOnlyInProbabilityCalculation;
	}
	

}
