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

import java.util.Vector;

public class Test {

	public static void main(String[] args) {
		Vector v = new Vector(0);
		v.add(1f);
		v.add(2f);
		try {
			Object rv = TLisp.getTLisp().applyJLet("'(jlet (x y) (+ x y))", v);
			System.out.println("Result=" + rv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
