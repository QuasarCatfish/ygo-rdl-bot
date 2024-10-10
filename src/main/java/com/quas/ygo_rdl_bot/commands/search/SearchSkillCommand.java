package com.quas.ygo_rdl_bot.commands.search;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import com.quas.ygo_rdl_bot.commands.Command;
import com.quas.ygo_rdl_bot.commands.CommandInfo;
import com.quas.ygo_rdl_bot.commands.CommandInfo.DeferType;
import com.quas.ygo_rdl_bot.data.RushCard;
import com.quas.ygo_rdl_bot.data.RushSkill;
import com.quas.ygo_rdl_bot.data.Util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

@CommandInfo(name = "skill", desc = "Searches a Rush Duel Links skill.", deferButton = DeferType.Reply)
public class SearchSkillCommand extends Command {

	private static final String SKILL_NAME = "name";
	
	@Override
	public SlashCommandData data() {
		OptionData skillName = new OptionData(OptionType.INTEGER, SKILL_NAME, "Skill name.", true, true);
		return super.data().addOptions(skillName);
	}
	
	@Override
	public void handle(SlashCommandInteractionEvent event) {
		int id = event.getOption(SKILL_NAME).getAsInt();
		RushSkill skill = RushSkill.get(id);
		
		if (skill == null) {
			event.getHook().editOriginal("I could not find the skill you were looking for.").queue();
		} else {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(0xFFCC00);
			
			eb.setTitle(skill.getName());
			if (skill.hasCondition()) eb.setDescription(skill.getCondition());
			eb.addField("[REQUIREMENT]", skill.getRequirement(), false);
			eb.addField("[" + skill.getEffectType().toString() + "]", skill.getEffect(), false);
			
			List<ActionRow> mentionedCards = ActionRow.partitionOf(IntStream.of(skill.getMentionedCards()).boxed().map(i -> {
					RushCard card = RushCard.get(i);
					return Button.primary(componentId(ANY_USER, i), card.getName()).withEmoji(card.getCardType().getEmoji());
			}).toList());
			
			if (skill.isArchive()) {
				eb.setThumbnail("https://raw.githubusercontent.com/QuasarCatfish/ygo-rdl-assets/main/ui/archive_skill.png");
				event.getHook().editOriginalEmbeds(eb.build()).setComponents(mentionedCards).queue();
			} else {
				File image = generateCharacterImage(skill.getCharacters());
				eb.setThumbnail("https://raw.githubusercontent.com/QuasarCatfish/ygo-rdl-assets/main/ui/legendary_skill.png");
				
				if (image == null) {
					event.getHook().editOriginalEmbeds(eb.build()).setComponents(mentionedCards).queue();
				} else {
					eb.setImage("attachment://" + image.getName());
					event.getHook().editOriginalEmbeds(eb.build()).setComponents(mentionedCards).setAttachments(FileUpload.fromData(image)).queue(m -> image.delete());
				}
			}
		}
	}
	
	@Override
	public List<Choice> doAutoComplete(CommandAutoCompleteInteractionEvent event) {
		if (event.getFocusedOption().getName().equals(SKILL_NAME)) {
			return RushSkill.values().stream()
					.filter(s -> {
						String input = event.getFocusedOption().getValue().toLowerCase();
						for (String search : s.getSearchableText()) {
							if (search.toLowerCase().contains(input)) return true;
						}
						return false;
					})
					.limit(OptionData.MAX_CHOICES)
					.map(s -> new Choice(s.getName(), s.getSkillId()))
					.toList();
		}
		
		return super.doAutoComplete(event);
	}
	
	@Override
	public void handle(ButtonInteractionEvent event, String[] args) {
		RushCard card = RushCard.get(Integer.parseInt(args[0]));
		
		if (card == null) {
			event.getHook().editOriginal("I could not find the card you were looking for.").queue();
		} else {
			event.getHook().editOriginalEmbeds(card.asEmbed()).queue();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	private static final int NUM = 8;
	
	private String[][] format(int[] characters) {
		if (characters == null) {
			return new String[][] {
				new String[] {"9998"}
			};
		}
		
		ArrayDeque<Integer> list = new ArrayDeque<>(IntStream.of(characters).boxed().toList());
		ArrayList<String[]> rows = new ArrayList<>();
		
		while (!list.isEmpty()) {
			String[] row = new String[Math.min(NUM, list.size())];
			for (int q = 0; q < row.length; q++) row[q] = String.format("%04d", list.poll());
			rows.add(row);
		}
		
		return rows.toArray(String[][]::new);
	}
	
	///////////////////////////////////////////////////////////////////////
	
	private static final int CHARACTER_WIDTH = 124;
	private static final int CHARACTER_HEIGHT = 146;
	
	private File generateCharacterImage(int[] characters) {
		String[][] arr = format(characters);
		
		BufferedImage image = new BufferedImage(NUM * CHARACTER_WIDTH, arr.length * CHARACTER_HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics g = image.getGraphics();
		
		for (int row = 0; row < arr.length; row++) {
			for (int col = 0; col < arr[row].length; col++) {
				File f = new File("data/characters/" + arr[row][col] + ".png");
				if (!f.exists()) f = new File("data/characters/9998.png");
				
				try {
					BufferedImage chara = ImageIO.read(f);
					g.drawImage(chara, col * CHARACTER_WIDTH, row * CHARACTER_HEIGHT, null);
				} catch (IOException e) {}
			}
		}
		
		File f = new File("data/tmp/" + Util.nextDigitString() + ".png");
		f.mkdirs();
		try {
			ImageIO.write(image, "PNG", f);
			return f;
		} catch (IOException e) {}
		
		return null;
	}
}
