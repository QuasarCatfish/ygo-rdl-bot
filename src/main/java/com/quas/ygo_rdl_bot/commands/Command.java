package com.quas.ygo_rdl_bot.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.jetbrains.annotations.NotNull;

import com.quas.ygo_rdl_bot.commands.CommandInfo.DeferType;

import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class Command extends ListenerAdapter {

	public static String ANY_USER = "any";
	
	private List<Subcommand> subcommands = Collections.synchronizedList(new ArrayList<Subcommand>());
	public final void addSubcommand(Subcommand sc) {
		subcommands.add(sc);
	}
	
	public SlashCommandData data() {
		CommandInfo ci = this.getClass().getAnnotation(CommandInfo.class);
		return Commands.slash(ci.name(), ci.desc()).setGuildOnly(true);
	}
	
	@Override
	public final void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		CommandInfo ci = this.getClass().getAnnotation(CommandInfo.class);
		if (!event.getName().equals(ci.name())) return;
		
		if (subcommands.isEmpty()) {
			if (ci.requiresPermissionView() && !event.getGuildChannel().canTalk()) {
				noPermission(event);
			} else {
				if (ci.deferSlash() == DeferType.Reply) event.deferReply(isEphemeral(event)).queue(hook -> handle(event));
				else handle(event);
			}
		} else {
			for (Subcommand sc : subcommands) {
				CommandInfo sci = sc.getClass().getAnnotation(CommandInfo.class);
				if (!event.getSubcommandName().equals(sci.name())) continue;
				
				if (sci.requiresPermissionView() && !event.getGuildChannel().canTalk()) {
					noPermission(event);
				} else {
					if (sci.deferSlash() == DeferType.Reply) event.deferReply(isEphemeral(event)).queue(hook -> sc.handle(event));
					else sc.handle(event);
				}
			}
		}
	}
	
	public void handle(SlashCommandInteractionEvent event) {
		wip(event);
	}
	
	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		CommandInfo ci = this.getClass().getAnnotation(CommandInfo.class);
		if (!event.getName().equals(ci.name())) return;
		
		if (subcommands.isEmpty()) {
			List<Choice> choices = doAutoComplete(event);
			if (choices.size() > OptionData.MAX_CHOICES) event.replyChoices(choices.subList(0, OptionData.MAX_CHOICES)).queue();
			else event.replyChoices(choices).queue();
		} else {
			for (Subcommand sc : subcommands) {
				CommandInfo sci = sc.getClass().getAnnotation(CommandInfo.class);
				if (!event.getSubcommandName().equals(sci.name())) continue;
				
				List<Choice> choices = sc.doAutoComplete(event);
				if (choices.size() > OptionData.MAX_CHOICES) event.replyChoices(choices.subList(0, OptionData.MAX_CHOICES)).queue();
				else event.replyChoices(choices).queue();
			}
		}
	}
	
	public List<Choice> doAutoComplete(CommandAutoCompleteInteractionEvent event) {
		return List.of();
	}
	
	@Override
	public final void onButtonInteraction(ButtonInteractionEvent event) {
		CommandInfo ci = this.getClass().getAnnotation(CommandInfo.class);
		String[] id = event.getComponentId().split(":");
		if (!id[0].equals(ci.name())) return;
		
		if (subcommands.isEmpty()) {
			if (ci.requiresPermissionView() && !event.getGuildChannel().canTalk()) {
				noPermission(event);
			} else if (id[1].equals(ANY_USER) || id[1].equals(event.getUser().getId())) {
				String[] args = Arrays.copyOfRange(id, 2, id.length);
				if (ci.deferButton() == DeferType.Reply) event.deferReply(isEphemeral(event)).queue(hook -> handle(event, args));
				else if (ci.deferButton() == DeferType.Edit) event.deferEdit().queue(hook -> handle(event, args));
				else handle(event, args);
			} else {
				noPermission(event);
			}
		} else {
			for (Subcommand sc : subcommands) {
				CommandInfo sci = sc.getClass().getAnnotation(CommandInfo.class);
				if (!id[1].equals(sci.name())) continue;
				
				if (sci.requiresPermissionView() && !event.getGuildChannel().canTalk()) {
					noPermission(event);
				} else if (id[2].equals(ANY_USER) || id[2].equals(event.getUser().getId())) {
					String[] args = Arrays.copyOfRange(id, 3, id.length);
					if (sci.deferButton() == DeferType.Reply) event.deferReply(isEphemeral(event)).queue(hook -> sc.handle(event, args));
					else if (sci.deferButton() == DeferType.Edit) event.deferEdit().queue(hook -> sc.handle(event, args));
					else sc.handle(event, args);
				} else {
					noPermission(event);
				}
			}
		}
	}
	
	public void handle(ButtonInteractionEvent event, String[] args) {
		wip(event);
	}
	
	@Override
	public final void onModalInteraction(ModalInteractionEvent event) {
		CommandInfo ci = this.getClass().getAnnotation(CommandInfo.class);
		String[] id = event.getModalId().split(":");
		if (!id[0].equals(ci.name())) return;
		
		if (subcommands.isEmpty()) {
			if (ci.requiresPermissionView() && !event.getGuildChannel().canTalk()) {
				noPermission(event);
			} else if (id[1].equals(ANY_USER) || id[1].equals(event.getUser().getId())) {
				String[] args = Arrays.copyOfRange(id, 2, id.length);
				if (ci.deferModal() == DeferType.Reply) event.deferReply(isEphemeral(event)).queue(hook -> handle(event, args));
				else if (ci.deferModal() == DeferType.Edit) event.deferEdit().queue(hook -> handle(event, args));
				else handle(event, args);
			} else {
				noPermission(event);
			}
		} else {
			for (Subcommand sc : subcommands) {
				CommandInfo sci = sc.getClass().getAnnotation(CommandInfo.class);
				if (!id[1].equals(sci.name())) continue;
				
				if (sci.requiresPermissionView() && !event.getGuildChannel().canTalk()) {
					noPermission(event);
				} else if (id[2].equals(ANY_USER) || id[2].equals(event.getUser().getId())) {
					String[] args = Arrays.copyOfRange(id, 3, id.length);
					if (sci.deferModal() == DeferType.Reply) event.deferReply(isEphemeral(event)).queue(hook -> sc.handle(event, args));
					else if (sci.deferModal() == DeferType.Edit) event.deferEdit().queue(hook -> sc.handle(event, args));
					else sc.handle(event, args);
				} else {
					noPermission(event);
				}
			}
		}
	}
	
	public void handle(ModalInteractionEvent event, String[] args) {
		wip(event);
	}
	
	///////////////////////////////////
	
	protected final String componentId(String user, Object...args) {
		StringJoiner sj = new StringJoiner(":");
		
		CommandInfo ci = this.getClass().getAnnotation(CommandInfo.class);
		if (ci.parent() != void.class) sj.add(ci.parent().getAnnotation(CommandInfo.class).name());
		sj.add(ci.name());
		sj.add(user);
		
		for (Object obj : args) sj.add(Objects.toString(obj));
		return sj.toString();
	}
	
	protected final String[] getComponents(String componentId) {
		return componentId.split(":");
	}
	
	protected final void wip(GenericCommandInteractionEvent event) {
		reply(event, "This command is currently a work in progress. Please try again later.");
	}
	
	protected final void wip(GenericComponentInteractionCreateEvent event) {
		reply(event, "This component is currently a work in progress. Please try again later.");
	}
	
	protected final void wip(ModalInteractionEvent event) {
		reply(event, "This modal is currently a work in progress. Please try again later.");
	}
	
	protected final void noPermission(GenericCommandInteractionEvent event) {
		reply(event, "You do not have permission to use this interaction.");
	}
	
	protected final void noPermission(GenericComponentInteractionCreateEvent event) {
		reply(event, "You do not have permission to use this interaction.");
	}
	
	protected final void noPermission(ModalInteractionEvent event) {
		reply(event, "You do not have permission to use this interaction.");
	}
	
	protected final void reply(GenericCommandInteractionEvent event, String format, Object...args) {
		if (event.isAcknowledged()) event.getHook().editOriginalFormat(format, args).queue();
		else event.replyFormat(format, args).setEphemeral(true).queue();
	}
	
	protected final void reply(GenericComponentInteractionCreateEvent event, String format, Object...args) {
		if (event.isAcknowledged()) event.getHook().editOriginalFormat(format, args).queue();
		else event.replyFormat(format, args).setEphemeral(true).queue();
	}
	
	protected final void reply(ModalInteractionEvent event, String format, Object...args) {
		if (event.isAcknowledged()) event.getHook().editOriginalFormat(format, args).queue();
		else event.replyFormat(format, args).queue();
	}
	
	protected final boolean isEphemeral(GenericInteractionCreateEvent event) {
		return false;
	}
}
