package com.quas.ygo_rdl_bot.data;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.google.gson.Gson;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;

public class RushCard {

	public static int INVALID_STAT = -1;
	
	private static TreeMap<Integer, RushCard> MAPPED_CARDS = new TreeMap<>();
	private static TreeMap<String, ArrayList<RushCard>> NAMED_CARDS = new TreeMap<>();
	static {
		Gson gson = new Gson();
		try (FileReader in = new FileReader("data/cards.json")) {
			RushCard[] cards = gson.fromJson(in, RushCard[].class);
			for (RushCard card : cards) {
				MAPPED_CARDS.put(card.getId(), card);
				
				if (!NAMED_CARDS.containsKey(card.getName())) NAMED_CARDS.put(card.getName(), new ArrayList<>());
				NAMED_CARDS.get(card.getName()).add(card);
			}
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
	
	public static Comparator<RushCard> comparator() {
		return (a, b) -> {
			if (a == null || b == null) return Boolean.compare(a == null, b == null);
			
			// Effect -> Normal -> Spell -> Trap
			if (a.getCardType().isMonster() != b.getCardType().isMonster()) return a.getCardType().isMonster() ? -1 : 1;
			if (a.getCardType().isSpell() != b.getCardType().isSpell()) return a.getCardType().isSpell() ? -1 : 1;
			if (a.getCardType().isMonster() && (a.getCardType() == CardType.NORMAL) != (b.getCardType() == CardType.NORMAL)) return Boolean.compare(a.getCardType() == CardType.NORMAL, b.getCardType() == CardType.NORMAL);
			
			// Rarity
			if (a.getRarity() != b.getRarity()) return -a.getRarity().compareTo(b.getRarity());
			
			// Level
			if (a.getLevel() != b.getLevel()) return -Integer.compare(a.getLevel(), b.getLevel());
			
			// Release Date
			// TODO
			
			// Name
			if (!a.getName().equals(b.getName())) return a.getName().compareTo(b.getName());
			
			// ID
			return Integer.compare(a.getId(), b.getId());
		};
	}
	
	public static Comparator<RushCard> banlistComparator() {
		return (a, b) -> {
			if (a == null || b == null) return Boolean.compare(a == null, b == null);
			if (a.getRarity() != b.getRarity()) return -a.getRarity().compareTo(b.getRarity());
			if (a.getCardType().isMonster() != b.getCardType().isMonster()) return a.getCardType().isMonster() ? -1 : 1;
			if (a.getCardType().isSpell() != b.getCardType().isSpell()) return a.getCardType().isSpell() ? -1 : 1;
			if (a.getCardType().isMonster() && b.getCardType().isMonster() && a.getCardType() != b.getCardType()) return -a.getCardType().compareTo(b.getCardType());
			return a.getName().compareTo(b.getName());
		};
	}
	
	/////////////////////////////////////
	
	private int id;
	private String name;
	private CardType cardType;
	private CardStyle cardStyle;
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
	
	public CardStyle getCardStyle() {
		return cardStyle;
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
		return monsterInfo == null ? INVALID_STAT : monsterInfo.level;
	}
	
	public int getAtk() {
		return monsterInfo == null ? INVALID_STAT : monsterInfo.atk;
	}
	
	public int getDef() {
		return monsterInfo == null ? INVALID_STAT : monsterInfo.def;
	}
	
	public int getMaximumAtk() {
		return monsterInfo == null || monsterInfo.maxAtk == null ? INVALID_STAT : monsterInfo.maxAtk;
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
		return String.format("https://raw.githubusercontent.com/QuasarCatfish/ygo-rdl-assets/refs/heads/main/art/%d.png", getId());
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
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof RushCard && equals((RushCard)obj);
	}
	
	public boolean equals(RushCard other) {
		return this.getName().equals(other.getName());
	}
	
	@Override
	public int hashCode() {
		return getName().hashCode();
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
			if (getCardType() == CardType.FUSION_EFFECT) type.add("Fusion");
			if (getCardType() == CardType.NORMAL) type.add("Normal");
			else type.add("Effect");
			
			eb.appendDescription(String.format("%s\n%s\nLevel: %d\nATK %d / DEF %d", type.toString(), getAttribute(), getLevel(), getAtk(), getDef()));
			if (getCardType() == CardType.MAXIMUM && getMaximumAtk() != INVALID_STAT) eb.appendDescription("\nMAXIMUM ATK: " + getMaximumAtk());
		} else {
			eb.appendDescription(getCardType().toString());
		}
		
		if (getCardType() == CardType.NORMAL) {
			eb.addField(EmbedBuilder.ZERO_WIDTH_SPACE, "*" + MarkdownSanitizer.escape(getEffect()) + "*", false);
		} else if (getCardType() == CardType.FUSION) {
			if (hasInherentSummonCondition()) eb.addField(EmbedBuilder.ZERO_WIDTH_SPACE, getInherentSummonCondition(), false);
		} else {
			if (hasInherentSummonCondition()) eb.addField(EmbedBuilder.ZERO_WIDTH_SPACE, getInherentSummonCondition(), false);
			eb.addField("[REQUIREMENT]", getRequirement(), false);
			eb.addField("[" + getEffectType() + "]", getEffect(), false);
		}
		
		{
			StringJoiner sj = new StringJoiner("\n");
			for (RushCard card : NAMED_CARDS.get(getName())) {
				if (card.getObtainMethods() != null && card.getObtainMethods().length > 0) {
					if (card.getCardStyle() != null) sj.add("**" + card.getCardStyle() + "**");
					
					for (String om : card.getObtainMethods()) {
						sj.add("- " + om);
					}
				}
			}
			
			if (sj.length() > 0) {
				eb.addField("How to Obtain", sj.toString(), false);
			}
		}
		
		if (getBanlistStatus() != BanlistStatus.UNLIMITED) {
			eb.setFooter("This card is " + getBanlistStatus() + " on the banlist.", getBanlistStatus().getIconUrl());
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
		CYBERSE("Cyberse", ""),
		DIVINE_BEAST("Divine-Beast", ""),
		ILLUSION("Illusion", ""),
		CREATOR_GOD("Creator God", ""),
		CYBORG("Cyborg", ""),
		MAGICAL_KNIGHT("Magical Knight", ""),
		HIGH_DRAGON("High Dragon", ""),
		CELESTIAL_WARRIOR("Celestial Warrior", ""),
		OMEGA_PSYCHIC("Omega Psychic", ""),
		GALAXY("Galaxy", "");
		
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
