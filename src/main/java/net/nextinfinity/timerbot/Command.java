package net.nextinfinity.timerbot;

import lombok.Data;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

@Data
public abstract class Command {
	private final String name;
	private final String description;
	private final List<OptionData> options;

	public abstract void execute(SlashCommandInteractionEvent event);
}
