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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package moonstone.utility;

import java.text.NumberFormat;

/**
 *
 * @author leechristensen
 */
public class ThreadUtils {

	private static long lowestFreeMemory = 1000000000000L;

	public static void printFreeMemory(String prefix) {
		printFreeMemory(prefix, true);
	}

	public static void printFreeMemory(String prefix, boolean onlyWithDrop) {
		Runtime runtime = Runtime.getRuntime();

		NumberFormat format = NumberFormat.getInstance();

		StringBuilder sb = new StringBuilder();
		long max = runtime.maxMemory();
		long allocated = runtime.totalMemory();
		long free = runtime.freeMemory();
		long current = (free + (max - allocated)) / 1024;
		long diff = Math.abs(current - lowestFreeMemory);
		long lowest = lowestFreeMemory;
		
		if (current < lowestFreeMemory) {
			System.out.println("\n" + 
					prefix + ": Lowest=" + lowestFreeMemory + "K, Current=" + current + "K, Diff=" + diff);
			lowestFreeMemory = current;
		}
	}
}
