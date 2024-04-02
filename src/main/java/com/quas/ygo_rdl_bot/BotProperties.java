package com.quas.ygo_rdl_bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BotProperties {

	private static final Properties PROPS = new Properties();
	static {
		try (FileInputStream in = new FileInputStream("bot.properties")) {
			PROPS.load(in);
		} catch (IOException e) {
			System.err.println("Could not read bot properties");
			System.exit(1);
		}
	}
	
	public static String getToken() {
		return PROPS.getProperty("token");
	}
}
