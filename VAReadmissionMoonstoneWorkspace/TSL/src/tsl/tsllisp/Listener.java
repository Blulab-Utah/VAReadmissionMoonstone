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
package tsl.tsllisp;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import tsl.utilities.StrUtils;

public class Listener {
	TLisp tLisp = null;

	public static void main(String[] args) {
		TLisp tl = TLisp.getTLisp();
		new Listener(tl);
	}

	Listener(TLisp tl) {
		this.tLisp = tl;
		readEvalPrint();
	}

	public void readEvalPrint() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String str = "";
		boolean quit = false;
		String laststr = null;
		while (!quit) {
			System.out.print("LeeLisp> ");
			try {
				str = in.readLine();
				if (str != null && str.toLowerCase().contains("quit")) {
					System.out.println("BYE!");
					return;
				}
				if ("!!".equals(str) && laststr != null) {
					str = laststr;
				}
				long start = System.currentTimeMillis();
				Object rv = this.tLisp.evalString(str);
				System.out.println(rv);
				float seconds = (System.currentTimeMillis() - start) / 1000f;
				laststr = str;
				System.out.println("Duration=" + seconds + " seconds");
			} catch (Exception e) {
				// System.out.println("TLISP: " + e.getMessage());
				System.out.println(StrUtils.getStackTrace(e));
				TLisp.tLisp.popAllSymbolTables();
			}
		}
	}

}
