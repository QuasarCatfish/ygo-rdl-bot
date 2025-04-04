package com.quas.ygo_rdl_bot;

import com.quas.ygo_rdl_bot.commands.CommandManager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class RushDuelLinksBot {
	
	public static void main(String[] args) throws InterruptedException {
		
		JDA bot = JDABuilder.createDefault(BotProperties.getToken()).build().awaitReady();
		bot.getPresence().setActivity(Activity.playing("Yu-Gi-Oh! Duel Links"));
		CommandManager.load(bot, false);
	}
}
