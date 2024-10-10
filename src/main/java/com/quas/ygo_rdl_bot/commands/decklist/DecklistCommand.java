package com.quas.ygo_rdl_bot.commands.decklist;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.quas.ygo_rdl_bot.commands.Command;
import com.quas.ygo_rdl_bot.commands.CommandInfo;
import com.quas.ygo_rdl_bot.commands.CommandInfo.DeferType;
import com.quas.ygo_rdl_bot.data.Approximate;
import com.quas.ygo_rdl_bot.data.RushCard;
import com.quas.ygo_rdl_bot.data.RushSkill;
import com.quas.ygo_rdl_bot.data.Util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;

@CommandInfo(name = "decklist", desc = "Creates a decklist image from a list of cards.", deferSlash = DeferType.None, deferButton = DeferType.None, deferModal = DeferType.Reply)
public class DecklistCommand extends Command {

	private final String SKILL = "skill";
	private final String DECKLIST = "decklist";
	
	@Override
	public void handle(SlashCommandInteractionEvent event) {
		if (event.getUser().getIdLong() == 563661703124877322L) {
			return;
		}
		
		if (event.getUser().getIdLong() != 563661703124877322L) {
			event.reply("This command is currently WIP and cannot be used in this server yet. Sorry!").setEphemeral(true).queue();
			return;
		}
		
		TextInput skill = TextInput.create(SKILL, "Skill", TextInputStyle.SHORT)
				.setPlaceholder("Skill")
				.setRequired(false)
				.build();
		
		TextInput decklist = TextInput.create(DECKLIST, "Decklist", TextInputStyle.PARAGRAPH)
				.setPlaceholder("Decklist")
				.build();
		
		Modal modal = Modal.create(componentId(ANY_USER), "Decklist Submission")
				.addActionRow(skill)
				.addActionRow(decklist)
				.build();
		
		event.replyModal(modal).queue();
	}

	@Override
	public void handle(ButtonInteractionEvent event, String[] args) {
		if (event.getUser().getIdLong() == 563661703124877322L) {
			return;
		}
		
		if (event.getUser().getIdLong() != 563661703124877322L) {
			event.reply("This command is currently WIP and cannot be used in this server yet. Sorry!").setEphemeral(true).queue();
			return;
		}
		
		if (event.getMessage().getEmbeds().isEmpty()) {
			event.reply("An error has occured.").setEphemeral(true).queue();
			return;
		}
		
		String footer = event.getMessage().getEmbeds().get(0).getFooter().getText();
		String[] arr = footer.split(";");
		
		int skillId = Integer.parseInt(arr[0], Character.MAX_RADIX);
		
		StringJoiner sj = new StringJoiner("\n");
		for (int q = 1; q < arr.length; q++) {
			if (arr[q].contains(":")) {
				String[] split = arr[q].split(":");
				int count = Integer.parseInt(split[0]);
				int cardId = Integer.parseInt(split[1], Character.MAX_RADIX);
				sj.add(String.format("%dx %s", count, cardId == 0 ? "Unknown Card" : RushCard.get(cardId).getName()));
			} else {
				int cardId = Integer.parseInt(arr[q], Character.MAX_RADIX);
				sj.add(String.format("%dx %s", 1, cardId == 0 ? "Unknown Card" : RushCard.get(cardId).getName()));
			}
		}
		
		TextInput skill = TextInput.create(SKILL, "Skill", TextInputStyle.SHORT)
				.setPlaceholder("Skill")
				.setRequired(false)
				.setValue(skillId == 0 ? null : RushSkill.get(skillId) == null ? null : RushSkill.get(skillId).getName())
				.build();
		
		TextInput decklist = TextInput.create(DECKLIST, "Decklist", TextInputStyle.PARAGRAPH)
				.setPlaceholder("Decklist")
				.setValue(sj.toString())
				.build();
		
		Modal modal = Modal.create(componentId(ANY_USER), "Decklist Submission")
				.addActionRow(skill)
				.addActionRow(decklist)
				.build();
		
		event.replyModal(modal).queue();
	}
	
	@Override
	public void handle(ModalInteractionEvent event, String[] args) {
		if (event.getUser().getIdLong() == 563661703124877322L) {
			return;
		}
		
		if (event.getUser().getIdLong() != 563661703124877322L) {
			event.reply("This command is currently WIP and cannot be used in this server yet. Sorry!").setEphemeral(true).queue();
			return;
		}
		
		String skillText = event.getValue(SKILL).getAsString();
		RushSkill skill = Approximate.skill(skillText);
		
		String[] deckText = event.getValue(DECKLIST).getAsString().split("(\r|\n)+");
		TreeMap<RushCard, Integer> deck = new TreeMap<>(RushCard.comparator());
		int mainCount = 0;
		int extraCount = 0;
		int sideCount = 0;
		
		// Build deck
		for (String cardText : deckText) {
			int count = 1;
			
			Pattern p = Pattern.compile("(\\d{1,2})x?\\s+(.+)");
			Matcher m = p.matcher(cardText);
			if (m.matches()) {
				count = Integer.parseInt(m.group(1));
				cardText = m.group(2);
			}
			
			RushCard card = Approximate.card(cardText);
			if (card == null) continue;
			
			System.out.println(card.getName() + " " + count);
			deck.put(card, deck.getOrDefault(card, 0) + count);
			if (card.getCardType().isMainDeck()) mainCount += count;
			else extraCount += count;
		}
		
		// Build embed
		StringJoiner footer = new StringJoiner(";");
		footer.add(skill == null ? "0" : Integer.toString(skill.getSkillId(), Character.MAX_RADIX).toUpperCase());
		
		final int CARDS_PER_ROW = 10;
		final int MAX_MAIN_CARDS = 60;
		final int MAX_EXTRA_CARDS = 15;
		final int MAX_SIDE_CARDS = 15;
		
		int mainRows = (Math.min(mainCount, MAX_MAIN_CARDS) + CARDS_PER_ROW - 1) / CARDS_PER_ROW;
		int extraRows = (Math.min(extraCount, MAX_EXTRA_CARDS) + CARDS_PER_ROW - 1) / CARDS_PER_ROW;
		int sideRows = (Math.min(sideCount, MAX_SIDE_CARDS) + CARDS_PER_ROW - 1) / CARDS_PER_ROW;
		int rows = mainRows + extraRows + sideRows;
		
		final int WIDTH = 704 / 2;
		final int HEIGHT = 1024 / 2;
		
		BufferedImage image = new BufferedImage(CARDS_PER_ROW * WIDTH, rows * HEIGHT, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics g = image.getGraphics();
		
		int mainIndex = 0;
		int extraIndex = 0;
		int sideIndex = 0;
		for (RushCard card : deck.keySet()) {
			if (card != null) {
				try {
					BufferedImage img = ImageIO.read(new File("C:\\Users\\cstephens\\OneDrive\\Pictures\\Yu-Gi-Oh! Rush Duel Links Bot\\ygo-rdl-assets\\cards\\" + card.getId() + ".png"));
//					BufferedImage img = ImageIO.read(new File("C:\\Users\\cstephens\\Downloads\\DL Extractions\\large cards\\assets\\resources\\card\\en-us\\l\\" + card.getId() + ".png"));
					
					for (int q = 0; q < deck.get(card); q++) {
						int x = 0, y = 0;
						
						if (card.getCardType().isMainDeck()) {
							if (mainIndex >= MAX_MAIN_CARDS) continue;
							x = mainIndex % CARDS_PER_ROW;
							y = mainIndex / CARDS_PER_ROW;
							mainIndex++;
						} else {
							if (extraIndex >= MAX_EXTRA_CARDS) continue;
							x = extraIndex % CARDS_PER_ROW;
							y = extraIndex / CARDS_PER_ROW + mainRows;
							extraIndex++;
						}
						
						g.drawImage(img, x * WIDTH, y * HEIGHT, WIDTH, HEIGHT, null);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (deck.get(card) > 1) footer.add(String.format("%d:%s", deck.get(card), Integer.toString(card == null ? 0 : card.getId(), Character.MAX_RADIX).toUpperCase()));
			else footer.add(Integer.toString(card == null ? 0 : card.getId(), Character.MAX_RADIX).toUpperCase());
		}

		File f = new File("data/tmp/" + Util.nextDigitString() + ".png");
		try {
			ImageIO.write(image, "PNG", f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setDescription(String.format("**Skill:**\n- %s", skill == null ? "None" : skill.getName() + (skill.isArchive() ? " (Archive)" : " (Legendary)")));
		eb.setFooter(footer.toString());
		eb.setImage("attachment://" + f.getName());
		
		event.getHook().editOriginalEmbeds(eb.build())
				.setActionRow(Button.secondary(componentId(ANY_USER), "Edit Decklist").withEmoji(Emoji.fromUnicode("U+1F4DD")))
				.setFiles(FileUpload.fromData(f))
				.queue(m -> f.delete());
	}
}
