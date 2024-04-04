package com.quas.ygo_rdl_bot.data;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.quas.ygo_rdl_bot.data.RushCard.BanlistStatus;

public class Banlist {
	
	private static BanlistUpdate[] banlist;
	static {
		Gson gson = new Gson();
		try (FileReader in = new FileReader("data/banlist.json")) {
			banlist = gson.fromJson(in, BanlistUpdate[].class);
			Arrays.sort(banlist, (a, b) -> -a.date.compareTo(b.date));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static BanlistStatus getStatus(int id) {
		for (BanlistUpdate update : banlist) {
			if (update.banlist.containsKey(Integer.toString(id))) {
				return update.banlist.get(Integer.toString(id));
			}
		}
		return BanlistStatus.UNLIMITED;
	}
	
	private class BanlistUpdate {
		String date;
		TreeMap<String, BanlistStatus> banlist;
	}
}
