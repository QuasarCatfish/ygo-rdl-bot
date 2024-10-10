package com.quas.ygo_rdl_bot.data;

import com.quas.ygo_rdl_bot.data.RushCard.Rarity;

public class Approximate {

	public static RushSkill skill(String name) {
		if (name == null || name.length() == 0) return null;
		
		final String search = name.toLowerCase().replaceAll("\\W", "");
		return RushSkill.values().stream()
				.filter(s -> s.getName().toLowerCase().replaceAll("\\W",  "").contains(search))
				.findFirst().orElse(null);
	}
	
	public static RushCard card(String name) {
		if (name == null || name.length() == 0) return null;
		
		final String search = name.toLowerCase().replaceAll("\\W", "");
		return RushCard.values().stream()
				.filter(c -> c.getRarity() != Rarity.NPC)
				.filter(c -> c.getName().toLowerCase().replaceAll("\\W", "").contains(search))
				.findFirst().orElse(null);
	}
}
