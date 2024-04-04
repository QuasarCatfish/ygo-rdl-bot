package com.quas.ygo_rdl_bot.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.reflections.Reflections;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class CommandManager {

	private static boolean isLoaded = false;
	private static final List<Command> COMMANDS = Collections.synchronizedList(new ArrayList<Command>());
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public static synchronized void load(JDA bot, boolean pushCommands) {
		if (isLoaded) return;
		
		Reflections ref = new Reflections("com.quas.ygo_rdl_bot");
		Map<Class<?>, List<Subcommand>> subcommands = new HashMap<>();
		List<SlashCommandData> commands = new ArrayList<>();
		
		// Load subcommands
		for (Class<?> c : ref.getSubTypesOf(Subcommand.class)) {
			if (!c.isAnnotationPresent(CommandInfo.class)) continue;
			
			CommandInfo ci = c.getAnnotation(CommandInfo.class);
			if (ci.parent() == void.class) continue;
			if (!Command.class.isAssignableFrom(ci.parent())) continue;
			
			LOGGER.info(String.format("Loading sub-command from class %s.", c.getSimpleName()));
			try {
				Subcommand sc = (Subcommand) c.getConstructor().newInstance();
				
				if (!subcommands.containsKey(ci.parent())) subcommands.put(ci.parent(), new ArrayList<Subcommand>());
				subcommands.get(ci.parent()).add(sc);
			} catch (Exception e) {}
		}
		
		// Load commands
		for (Class<?> c : ref.getSubTypesOf(Command.class)) {
			if (!c.isAnnotationPresent(CommandInfo.class)) continue;
			
			CommandInfo ci = c.getAnnotation(CommandInfo.class);
			if (ci.parent() != void.class) continue;
			
			LOGGER.info(String.format("Loading command from class %s.", c.getSimpleName()));
			try {
				Command cmd = (Command) c.getConstructor().newInstance();
				COMMANDS.add(cmd);
				bot.addEventListener(cmd);
				
				SlashCommandData cd = cmd.data();
				commands.add(cd);
				if (subcommands.containsKey(c)) {
					for (Subcommand sc : subcommands.get(c)) {
						cd.addSubcommands(sc.subdata());
						cmd.addSubcommand(sc);
					}
				}
			} catch (Exception e) {}
		}
		
		// Push commands
		if (pushCommands) {
			bot.updateCommands().addCommands(commands).queue();
		}
		
		LOGGER.info(String.format("Finished loading %,d commands.", COMMANDS.size()));
		isLoaded = true;
	}
}
