package net.nextinfinity.timerbot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.nextinfinity.timerbot.Command;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Timer extends Command {

	public Timer() {
		super(
			"timer",
			"Sets a timer for the specified time. Will pull users back to channel at end if specified.",
			List.of(
				new OptionData(OptionType.STRING, "name", "Name for the timer")
					.setRequired(true),
				new OptionData(OptionType.CHANNEL, "text-channel", "Text channel to post timer in")
					.setRequired(true).setChannelTypes(ChannelType.TEXT),
				new OptionData(OptionType.INTEGER, "length", "The length of the timer, in minute")
					.setRequired(true).setMinValue(0),
				new OptionData(OptionType.INTEGER, "notify-interval", "How often to post separate update notifications, in minutes")
					.setRequired(false).setMinValue(0),
				new OptionData(OptionType.BOOLEAN, "one-minute-warning", "Whether or not to notify with one minute remaining")
					.setRequired(false),
				new OptionData(OptionType.MENTIONABLE, "notify-mention", "Who to mention for updates")
					.setRequired(false),
				new OptionData(OptionType.CHANNEL, "return-voice-channel", "Channel to send all voice users to when the timer completes")
					.setRequired(false).setChannelTypes(ChannelType.VOICE)
			)
		);
	}

	public void execute(SlashCommandInteractionEvent event) {
		// Required parameters

		final String timerName = Objects.requireNonNull(event.getOption("name")).getAsString();
		final TextChannel textChannel = Objects.requireNonNull(event.getOption("text-channel")).getAsChannel().asTextChannel();
		final long timerLength = Objects.requireNonNull(event.getOption("length")).getAsLong();

		// Optional parameters

		final OptionMapping notifyIntervalOption = event.getOption("notify-interval");
		final long notifyInterval = notifyIntervalOption != null ? notifyIntervalOption.getAsLong() : timerLength;

		final OptionMapping oneMinuteWarningOption = event.getOption("one-minute-warning");
		boolean sendOneMinuteWarning = oneMinuteWarningOption != null ? oneMinuteWarningOption.getAsBoolean() : false;

		final OptionMapping mentionOption = event.getOption("notify-mention");
		final String mention = mentionOption != null ? mentionOption.getAsMentionable().getAsMention() + " " : "";

		final OptionMapping voiceChannelOption = event.getOption("return-voice-channel");
		final VoiceChannel voiceChannel = voiceChannelOption != null ? voiceChannelOption.getAsChannel().asVoiceChannel() : null;

		// Reply to the actual slash command

		event.getHook().editOriginal("Creating timer...").queue();

		// Send start of timer message

		textChannel.sendMessage(mention + "Timer for *" + timerName + "* set for **" + timerLength + "** minutes").queue();

		// Schedule interval messages

		for (long notifyTime = notifyInterval; notifyTime < timerLength; notifyTime += notifyInterval) {
			if (notifyTime == timerLength - 1) {
				sendOneMinuteWarning = true;
				break;
			}

			textChannel.sendMessage(mention + "**" + (timerLength - notifyTime) + "** minutes remaining for *" + timerName + "*").queueAfter(notifyTime, TimeUnit.MINUTES);
		}

		// Schedule one-minute warning

		if (sendOneMinuteWarning && timerLength > 1) {
			textChannel.sendMessage(mention + "Only ***1*** minute remaining for *" + timerName + "*").queueAfter(timerLength - 1, TimeUnit.MINUTES);
		}

		// Schedule final message and voice channel movement

		textChannel.sendMessage(mention + "*" + timerName + "* complete!").onSuccess(a -> {
			if (voiceChannel != null) {
				final Guild guild = voiceChannel.getGuild();
				for (Member member : voiceChannel.getGuild().getMembers()) {
					GuildVoiceState memberVoiceState = Objects.requireNonNull(member.getVoiceState());
					if (memberVoiceState.inAudioChannel()) {
						guild.moveVoiceMember(member, voiceChannel).queueAfter(3, TimeUnit.SECONDS);
					}
				}
			}
		}).queueAfter(timerLength, TimeUnit.MINUTES);
	}

}
