package com.quas.ygo_rdl_bot.commands.banlist;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import com.quas.ygo_rdl_bot.commands.Command;
import com.quas.ygo_rdl_bot.commands.CommandInfo;
import com.quas.ygo_rdl_bot.data.RushCard;
import com.quas.ygo_rdl_bot.data.Util;
import com.quas.ygo_rdl_bot.data.RushCard.BanlistStatus;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@CommandInfo(name = "banlist", desc = "Lists the current Rush Duel Links banlist.")
public class BanlistCommand extends Command {

	private static final String LIMIT_TYPE = "limit";
	
	@Override
	public SlashCommandData data() {
		OptionData limitType = new OptionData(OptionType.STRING, LIMIT_TYPE, "Banlist status.", false, true);
		return super.data().addOptions(limitType);
	}
	
	@Override
	public void handle(SlashCommandInteractionEvent event) {
		BanlistStatus[] display = BanlistStatus.values();
		OptionMapping option = event.getOption(LIMIT_TYPE);
		if (option != null) {
			try {
				BanlistStatus value = BanlistStatus.valueOf(option.getAsString());
				display = Util.arr(value);
			} catch (IllegalArgumentException e) {}
		}
		
		Collection<RushCard> cards = RushCard.values();
		ArrayList<MessageEmbed> embeds = new ArrayList<>();
		for (BanlistStatus limit : display) {
			if (limit == BanlistStatus.UNLIMITED) continue;
			
			ArrayList<RushCard> banned = new ArrayList<>();
			TreeSet<String> bannedNames = new TreeSet<>();
			for (RushCard card : cards) {
				if (card.getBanlistStatus() == limit) {
					if (!bannedNames.contains(card.getName())) {
						banned.add(card);
						bannedNames.add(card.getName());
					}
				}
			}
			
			if (!banned.isEmpty()) {
				banned.sort(RushCard.banlistComparator());
				
				EmbedBuilder eb = setupEmbed(limit);
				for (RushCard card : banned) {
					eb.appendDescription(String.format("%s %s %s\n", card.getRarity().getEmoji(), card.getCardType().getEmojiString(), card.getName()));
				}
				embeds.add(eb.build());
			} else if (display.length == 1) {
				EmbedBuilder eb = setupEmbed(limit);
				eb.appendDescription("None.");
				embeds.add(eb.build());
			}
		}
		
		if (embeds.isEmpty()) {
			event.getHook().editOriginal("There are no cards currently on the banlist.").queue();
		} else {
			event.getHook().editOriginalEmbeds(embeds).queue();
		}
	}
	
	private static EmbedBuilder setupEmbed(BanlistStatus limit) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle(limit.toString());
		eb.setColor(Color.red);
		eb.setThumbnail(limit.getIconUrl());
		return eb;
	}
	
	@Override
	public List<Choice> doAutoComplete(CommandAutoCompleteInteractionEvent event) {
		if (event.getFocusedOption().getName().equals(LIMIT_TYPE)) {
			return Stream.of(BanlistStatus.values())
					.filter(s -> s != BanlistStatus.UNLIMITED)
					.map(s -> new Choice(s.toString(), s.name()))
					.toList();
		}
		
		return super.doAutoComplete(event);
	}
}
