package net.nextinfinity.timerbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;

public class CommandListener extends ListenerAdapter {

	private final Map<String, Command> commands = new HashMap<>();

	/**
	 * Load all commands into a map.
	 */
	public void registerCommands(JDA jda) {
		Reflections reflections = new Reflections("net.nextinfinity.timerbot");
		Set<Class<?>> commandClasses = reflections.get(SubTypes.of(Command.class).asClass());

		CommandListUpdateAction commandListUpdateAction = jda.updateCommands();

		for (Class<?> commandClass : commandClasses) {
            try {
				Command command = commandClass.asSubclass(Command.class).getConstructor().newInstance();
				commandListUpdateAction = commandListUpdateAction.addCommands(Commands.slash(command.getName(), command.getDescription()).addOptions(command.getOptions()));
				commands.put(command.getName(), command);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
		}

		commandListUpdateAction.queue();
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		Command command = commands.get(event.getName());
		if (command != null) {
			event.deferReply().queue();
			command.execute(event);
		}
	}

}
