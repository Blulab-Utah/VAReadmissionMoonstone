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
package tsl.jlisp;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Listener {
	JLisp jLisp = null;


	Listener(JLisp jl) {
		this.jLisp = jl;
	}

	public void readEvalPrint() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			String str = "";
			boolean quit = false;
			while (!quit) {
				System.out.print("JLisp> ");
				str = in.readLine();
				if ("quit".equals(str)) {
					return;
				}
				Object rv = this.jLisp.evalString(str);
				System.out.println(rv);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
