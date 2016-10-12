package demo;

import java.util.*;

public class test {

    public static void main(String[] args) {

        String A = "ma";
        String B = "max";
        /* Enter your code here. Print output to STDOUT. */
        System.out.println(A.length() + B.length());
        System.out.println(A.compareTo(B) > 0 ? "Yes" : "No");
        String mix = "";
        if (A.length() > 0) {
            mix += Character.toUpperCase(A.charAt(0)) + ((A.length() >= 2) ? A.substring(1) : "");  
        }
        if (B.length() > 0) {
            mix += " " + Character.toUpperCase(B.charAt(0)) + ((B.length() >= 2) ? B.substring(1) : "");  
        }
        System.out.println(mix.trim());
    }
//	public static void main(String[] args) {
//            String test = "AAAA";
//            test.
//		Scanner in = new Scanner(System.in);
//		Deque<Integer> deque = new ArrayDeque<Integer>();
//		int n = in.nextInt();
//		int m = in.nextInt();
//		if ((m < n) && (m >= 1 && m <= 100000 && (n <= 100000 && n >= 1))) {
//			int max = 0, current = 0;
//			for (int i = 0; i < n; i++) {
//				int num = in.nextInt();
//				if (num >= 0 && num <= 10000000) {
//					if (deque.size() >= m) {
//						max = current;
//						int tmp = (int) deque.removeFirst();
//						if (!deque.contains(tmp))
//							current--;
//					}
//					if (!deque.contains(num)) {
//						current++;
//					}
//					deque.add(num);
//				}
//			}
//			System.out.println(max);
//		}
//	}
}
