package com.quas.ygo_rdl_bot.commands.search;

import java.util.List;

import com.quas.ygo_rdl_bot.commands.Command;
import com.quas.ygo_rdl_bot.commands.CommandInfo;
import com.quas.ygo_rdl_bot.data.RushCard;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@CommandInfo(name = "card", desc = "Searches a Rush Duel Links card.")
public class SearchCardCommand extends Command {

	private static final String CARD_NAME = "name";
	
	@Override
	public SlashCommandData data() {
		OptionData cardName = new OptionData(OptionType.INTEGER, CARD_NAME, "Card name.", true, true);
		return super.data().addOptions(cardName);
	}
	
	@Override
	public void handle(SlashCommandInteractionEvent event) {
		int id = event.getOption(CARD_NAME).getAsInt();
		
		RushCard card = RushCard.get(id);
		if (card == null) {
			event.getHook().editOriginal("I could not find the card you were looking for.").queue();
		} else {
			event.getHook().editOriginalEmbeds(card.asEmbed()).queue();
		}
	}
	
	@Override
	public List<Choice> doAutoComplete(CommandAutoCompleteInteractionEvent event) {
		if (event.getFocusedOption().getName().equals(CARD_NAME)) {
			return RushCard.values().stream()
					.filter(c -> {
						String input = event.getFocusedOption().getValue().toLowerCase();
						for (String search : c.getSearchableText()) {
							if (search.toLowerCase().contains(input)) return true;
						}
						return false;
					})
					.sorted((a, b) -> {
						if (a.getRarity().isNpc() != b.getRarity().isNpc()) return Boolean.compare(a.getRarity().isNpc(), b.getRarity().isNpc());
						return Integer.compare(a.getId(), b.getId());
					})
					.limit(OptionData.MAX_CHOICES)
					.map(s -> new Choice(s.getName(), s.getId()))
					.toList();
		}
		
		return super.doAutoComplete(event);
	}
}
