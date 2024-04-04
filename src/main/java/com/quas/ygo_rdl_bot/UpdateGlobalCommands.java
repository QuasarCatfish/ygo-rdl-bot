package com.quas.ygo_rdl_bot;

import com.quas.ygo_rdl_bot.commands.CommandManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class UpdateGlobalCommands {
	
	public static void main(String[] args) throws InterruptedException {
		
		JDA bot = JDABuilder.createDefault(BotProperties.getToken()).build().awaitReady();
		CommandManager.load(bot, true);
		
		System.exit(0);
	}
}
