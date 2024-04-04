package com.quas.ygo_rdl_bot.commands;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Subcommand extends Command {
	
	public SubcommandData subdata() {
		CommandInfo ci = this.getClass().getAnnotation(CommandInfo.class);
		return new SubcommandData(ci.name(), ci.desc());
	}
}
