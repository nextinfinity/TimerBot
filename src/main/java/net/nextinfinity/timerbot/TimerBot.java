package net.nextinfinity.timerbot;

import lombok.Value;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.Collections;

@Value
public class TimerBot {

	public static void main(String[] args) {
		try {
			CommandListener listener = new CommandListener();
			JDA bot = JDABuilder.createDefault(System.getenv("DISCORD_BOT_TOKEN"),
							Collections.singletonList(GatewayIntent.GUILD_VOICE_STATES))
					.addEventListeners(listener)
					.build();
			listener.registerCommands(bot);
		} catch (Exception exception) {
			InstantiationError error = new InstantiationError("Failed to load TimerBot.");
			error.setStackTrace(exception.getStackTrace());
			throw error;
		}
	}

}
