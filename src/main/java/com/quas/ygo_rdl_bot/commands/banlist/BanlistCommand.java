package com.quas.ygo_rdl_bot.commands.banlist;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import com.quas.ygo_rdl_bot.commands.Command;
import com.quas.ygo_rdl_bot.commands.CommandInfo;
import com.quas.ygo_rdl_bot.data.RushCard;
import com.quas.ygo_rdl_bot.data.RushCard.BanlistStatus;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@CommandInfo(name = "banlist", desc = "Lists the current Rush Duel Links banlist.")
public class BanlistCommand extends Command {

	@Override
	public void handle(SlashCommandInteractionEvent event) {
		Collection<RushCard> cards = RushCard.values();
		
		ArrayList<MessageEmbed> embeds = new ArrayList<>();
		for (BanlistStatus limit : BanlistStatus.values()) {
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
}
