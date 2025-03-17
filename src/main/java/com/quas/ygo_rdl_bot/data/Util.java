package com.quas.ygo_rdl_bot.data;

import java.util.concurrent.ThreadLocalRandom;

public class Util {

	public static String nextDigitString() {
		StringBuilder sb = new StringBuilder();
		for (int q = 0; q < 30; q++) sb.append(ThreadLocalRandom.current().nextInt(10));
		return sb.toString();
	}
	
	@SafeVarargs
	public static <T> T[] arr(T...arr) {
		return arr;
	}
}
