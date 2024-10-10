package com.quas.ygo_rdl_bot.data;

import java.awt.Color;
import java.util.List;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum CardType {
	
	NORMAL("Normal Monster", Constants.COLOR_NORMAL, Constants.EMOJI_NORMAL),
	EFFECT("Effect Monster", Constants.COLOR_EFFECT, Constants.EMOJI_EFFECT),
	MAXIMUM("Maximum Monster", Constants.COLOR_EFFECT, Constants.EMOJI_EFFECT + Constants.EMOJI_EFFECT + Constants.EMOJI_EFFECT),
	FUSION("Fusion Monster", Constants.COLOR_FUSION, Constants.EMOJI_FUSION),
	FUSION_EFFECT("Fusion Effect Monster", Constants.COLOR_FUSION, Constants.EMOJI_FUSION),
	SPELL("Normal Spell Card", Constants.COLOR_SPELL, Constants.EMOJI_SPELL),
	FIELD("Field Spell Card", Constants.COLOR_SPELL, Constants.EMOJI_SPELL),
	EQUIP("Equip Spell Card", Constants.COLOR_SPELL, Constants.EMOJI_SPELL),
	TRAP("Normal Trap Card", Constants.COLOR_TRAP, Constants.EMOJI_TRAP);
	
	private String cardTypeName;
	private String color;
	private String emoji;
	private String singleEmoji;
	private CardType(String cardTypeName, String color, String fullEmoji) {
		this.cardTypeName = cardTypeName;
		this.color = color;
		this.emoji = fullEmoji;
		this.singleEmoji = fullEmoji.substring(0, fullEmoji.indexOf(">") + 1);
	}
	
	@Override
	public String toString() {
		return cardTypeName;
	}
	
	public Color getColor() {
		return Color.decode(color);
	}
	
	public String getEmojiString() {
		return emoji;
	}
	
	public Emoji getEmoji() {
		return Emoji.fromFormatted(singleEmoji);
	}
	
	public boolean isMonster() {
		return List.of(NORMAL, EFFECT, MAXIMUM, FUSION, FUSION_EFFECT).contains(this);
	}
	
	public boolean isSpell() {
		return List.of(SPELL, FIELD, EQUIP).contains(this);
	}
	
	public boolean isTrap() {
		return List.of(TRAP).contains(this);
	}
	
	public boolean isMainDeck() {
		return List.of(NORMAL, EFFECT, MAXIMUM, SPELL, FIELD, EQUIP, TRAP).contains(this);
	}
	
	public boolean isExtraDeck() {
		return List.of(FUSION, FUSION_EFFECT).contains(this);
	}
}
