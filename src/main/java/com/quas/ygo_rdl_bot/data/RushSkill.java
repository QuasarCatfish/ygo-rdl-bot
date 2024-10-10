package com.quas.ygo_rdl_bot.data;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.quas.ygo_rdl_bot.data.RushCard.EffectType;

public class RushSkill {

	private static TreeMap<Integer, RushSkill> MAPPED_SKILLS = new TreeMap<>();
	static {
		Gson gson = new Gson();
		try (FileReader in = new FileReader("data/skills.json")) {
			RushSkill[] skills = gson.fromJson(in, RushSkill[].class);
			for (RushSkill skill : skills) MAPPED_SKILLS.put(skill.getSkillId(), skill);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static RushSkill get(int skillId) {
		return MAPPED_SKILLS.getOrDefault(skillId, null);
	}
	
	public static Collection<RushSkill> values() {
		return Collections.unmodifiableCollection(MAPPED_SKILLS.values());
	}
	
	private int id;
	private String name;
	private String condition;
	private String requirement;
	private String effect;
	private EffectType effectType;
	
	private boolean isArchive;
	private int[] characters;
	private int[] mentionedCards;
	
	private List<String> searchableText = null;
	
	public int getSkillId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean hasCondition() {
		return condition != null;
	}
	
	public String getCondition() {
		return condition;
	}
	
	public String getRequirement() {
		return requirement;
	}
	
	public String getEffect() {
		return effect;
	}
	
	public EffectType getEffectType() {
		return effectType;
	}
	
	public boolean isArchive() {
		return isArchive;
	}
	
	public int[] getCharacters() {
		return characters;
	}
	
	public int[] getMentionedCards() {
		return mentionedCards;
	}
	
	public List<String> getSearchableText() {
		if (searchableText == null) {
			searchableText = new ArrayList<>();
			if (name != null) searchableText.add(name);
			if (condition != null) searchableText.add(condition);
			if (requirement != null) searchableText.add(requirement);
			if (effect != null) searchableText.add(effect);
		}
		
		return Collections.unmodifiableList(searchableText);
	}
}
