package demo;

import java.util.*;

public class test {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		Deque<Integer> deque = new ArrayDeque<Integer>();
		int n = in.nextInt();
		int m = in.nextInt();
		if ((m < n) && (m >= 1 && m <= 100000 && (n <= 100000 && n >= 1))) {
			int max = 0, current = 0;
			for (int i = 0; i < n; i++) {
				int num = in.nextInt();
				if (num >= 0 && num <= 10000000) {
					if (deque.size() >= m) {
						max = current;
						int tmp = (int) deque.removeFirst();
						if (!deque.contains(tmp))
							current--;
					}
					if (!deque.contains(num)) {
						current++;
					}
					deque.add(num);
				}
			}
			System.out.println(max);
		}
	}
}
