package com.quas.ygo_rdl_bot.data;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.google.gson.Gson;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

public class RushCard {

	private static TreeMap<Integer, RushCard> MAPPED_CARDS = new TreeMap<>();
	static {
		Gson gson = new Gson();
		try (FileReader in = new FileReader("data/cards.json")) {
			RushCard[] cards = gson.fromJson(in, RushCard[].class);
			for (RushCard card : cards) MAPPED_CARDS.put(card.getId(), card);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static RushCard get(int cardId) {
		return MAPPED_CARDS.getOrDefault(cardId, null);
	}
	
	public static Collection<RushCard> values() {
		return Collections.unmodifiableCollection(MAPPED_CARDS.values());
	}
	
	private int id;
	private String name;
	private CardType cardType;
	private EffectType effectType;
	private boolean isLegend;
	private Rarity rarity;
	
	private String inherentSummonCondition;
	private String requirement;
	private String effect;
	
	private MonsterInfo monsterInfo;
	private static class MonsterInfo {
		Integer level;
		Integer atk;
		Integer def;
		Integer maxAtk;
		Attribute attribute;
		Race race;
	}
	
	private String[] cardObtain;
	
	private List<String> searchableText = null;
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public CardType getCardType() {
		return cardType;
	}
	
	public boolean isLegend() {
		return isLegend;
	}
	
	public boolean hasInherentSummonCondition() {
		return inherentSummonCondition != null;
	}
	
	public String getInherentSummonCondition() {
		return inherentSummonCondition;
	}
	
	public String getRequirement() {
		return requirement == null ? "Unknown" : requirement;
	}
	
	public EffectType getEffectType() {
		return effectType;
	}
	
	public String getEffect() {
		return effect == null ? "Unknown" : effect;
	}
	
	public int getLevel() {
		return monsterInfo == null ? -1 : monsterInfo.level;
	}
	
	public int getAtk() {
		return monsterInfo == null ? -1 : monsterInfo.atk;
	}
	
	public int getDef() {
		return monsterInfo == null ? -1 : monsterInfo.def;
	}
	
	public int getMaximumAtk() {
		return monsterInfo == null ? -1 : monsterInfo.maxAtk == null ? -1 : monsterInfo.maxAtk;
	}
	
	public Attribute getAttribute() {
		return monsterInfo == null ? null : monsterInfo.attribute;
	}
	
	public Race getRace() {
		return monsterInfo == null ? null : monsterInfo.race;
	}
	
	public Rarity getRarity() {
		return rarity;
	}
	
	public BanlistStatus getBanlistStatus() {
		return Banlist.getStatus(getId());
	}
	
	public String[] getObtainMethods() {
		return cardObtain;
	}
	
	public String getImage() {
		return String.format("https://raw.githubusercontent.com/QuasarCatfish/ygo-rdl-assets/main/art/%d.png", getId());
	}
	
	public List<String> getSearchableText() {
		if (searchableText == null) {
			searchableText = new ArrayList<>();
			if (name != null) searchableText.add(name);
			if (inherentSummonCondition != null) searchableText.add(inherentSummonCondition);
			if (requirement != null) searchableText.add(requirement);
			if (effect != null) searchableText.add(effect);
		}
		
		return Collections.unmodifiableList(searchableText);
	}
	
	public MessageEmbed asEmbed() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(getName());
		eb.setColor(getCardType().getColor());
		
		if (getRarity() != Rarity.NPC) {
			eb.appendDescription(getRarity().getEmoji() + "\n");
		}
		
		if (isLegend()) {
			eb.appendDescription("LEGEND\n");
		}
		
		if (getCardType().isMonster()) {
			StringJoiner type = new StringJoiner(" / ");
			type.add(getRace() == null ? "???" : getRace().toString());
			if (getCardType() == CardType.MAXIMUM) type.add("Maximum");
			if (getCardType() == CardType.FUSION) type.add("Fusion");
			if (getCardType() == CardType.NORMAL) type.add("Normal");
			else type.add("Effect");
			
			eb.appendDescription(String.format("%s\n%s\nLevel: %d\nATK %d / DEF %d", type.toString(), getAttribute(), getLevel(), getAtk(), getDef()));
			if (getCardType() == CardType.MAXIMUM) eb.appendDescription("\nMAXIMUM ATK: " + getMaximumAtk());
		} else {
			eb.appendDescription(getCardType().toString());
		}
		
		if (getCardType() == CardType.NORMAL) {
			eb.addField(EmbedBuilder.ZERO_WIDTH_SPACE, "*" + MarkdownSanitizer.escape(getEffect()) + "*", false);
		} else {
			if (hasInherentSummonCondition()) eb.addField(EmbedBuilder.ZERO_WIDTH_SPACE, getInherentSummonCondition(), false);
			eb.addField("[REQUIREMENT]", getRequirement(), false);
			eb.addField("[" + getEffectType() + "]", getEffect(), false);
		}
		
		if (getObtainMethods() != null && getObtainMethods().length > 0) {
			StringJoiner sj = new StringJoiner("\n");
			for (String om : getObtainMethods()) {
				sj.add("- " + om);
			}
			eb.addField("How to Obtain", sj.toString(), false);
		}
		
		if (getBanlistStatus() != BanlistStatus.UNLIMITED) {
			eb.setFooter("This card is on the banlist.", getBanlistStatus().getIconUrl());
		} else if (getRarity() == Rarity.NPC) {
			eb.setFooter("This card is currently unobtainable.");
		}
		
		eb.setThumbnail(getImage());
		
		return eb.build();
	}
	
	public static enum EffectType {
		NORMAL("EFFECT"),
		CONTINUOUS("CONTINUOUS EFFECT"),
		MULTICHOICE("MULTI-CHOICE EFFECT");
		
		private String effectTypeName;
		private EffectType(String effectTypeName) {
			this.effectTypeName = effectTypeName;
		}
		
		@Override
		public String toString() {
			return effectTypeName;
		}
	}
	
	public static enum Attribute {
		LIGHT, DARK, FIRE, WATER, EARTH, WIND;
	}
	
	public static enum Race {
		DRAGON("Dragon", ""),
		ZOMBIE("Zombie", ""),
		FIEND("Fiend", ""),
		PYRO("Pyro", ""),
		SEA_SERPENT("Sea Serpent", ""),
		ROCK("Rock", ""),
		MACHINE("Machine", ""),
		FISH("Fish", ""),
		DINOSAUR("Dinosaur", ""),
		INSECT("Insect", ""),
		BEAST("Beast", ""),
		BEAST_WARRIOR("Beast-Warrior", ""),
		PLANT("Plant", ""),
		AQUA("Aqua", ""),
		WARRIOR("Warrior", ""),
		WINGED_BEAST("Winged Beast", ""),
		FAIRY("Fairy", ""),
		SPELLCASTER("Spellcaster", ""),
		THUNDER("Thunder", ""),
		REPTILE("Reptile", ""),
		PSYCHIC("Psychic", ""),
		WYRM("Wyrm", ""),
		CYBERSE("Cyberse", "");
		
		private String name;
		private String emoji;
		private Race(String name, String emoji) {
			this.name = name;
			this.emoji = emoji;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public String getEmoji() {
			return emoji;
		}
	}
	
	public static enum Rarity {
		N("<:dl_n:1214113456001908767>"),
		R("<:dl_r:1214113454957273179>"),
		SR("<:dl_sr:1214113453908955216>"),
		UR("<:dl_ur:1214113452616974336>"),
		NPC("NPC");
		
		private String emoji;
		private Rarity(String emoji) {
			this.emoji = emoji;
		}
		
		public String getEmoji() {
			return emoji;
		}
		
		public boolean isNpc() {
			return this == NPC;
		}
	}
	
	public static enum BanlistStatus {
		FORBIDDEN("Forbidden", "https://cdn.discordapp.com/emojis/1214059682100944917.webp"),
		LIMIT1("Limit 1", "https://cdn.discordapp.com/emojis/1214059683565019157.webp"),
		LIMIT2("Limit 2", "https://cdn.discordapp.com/emojis/1214059685087412264.webp"),
		LIMIT3("Limit 3", "https://cdn.discordapp.com/emojis/1214059715261104190.webp"),
		UNLIMITED("Unlimited", null);
		
		private String name;
		private String url;
		private BanlistStatus(String name, String url) {
			this.name = name;
			this.url = url;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public String getIconUrl() {
			return url;
		}
	}
}
