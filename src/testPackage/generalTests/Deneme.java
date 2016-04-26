package testPackage.generalTests;

import java.util.LinkedList;
import java.util.ListIterator;

public class Deneme {

	public static void main(String[] args) {
		LinkedList<String> list = new LinkedList<>();
		list.add("Erdem");
		list.add("Erdem1");
		list.add("Erdem2");
		list.add("Erdem3");
		list.add("Erdem4");
		list.add("Erdem5");
		list.add("Erdem6");
		list.add("Erdem7");
		list.add("Erdem8");

		String str = "Erdem1";
		int i = -1;
		if (list.contains(str)) {
			i = list.indexOf(str);
		}
		System.out.println("i:" + i);
		System.out.println("it2");
		ListIterator<String> it2 = list.listIterator(i + 1);
		while (it2.hasNext()) {
			System.out.println(it2.next());
		}

		if (i > 0) {
			System.out.println("it1");
			ListIterator<String> it1 = list.subList(0, i).listIterator();
			while (it1.hasNext()) {
				System.out.println(it1.next());
			}
		}

	}

}
