package net.nextinfinity.timerbot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TimerTest {
    private record Notice(String text, long minutes) {}
    private final List<Notice> notices = new ArrayList<>();
    private Consumer<Message> completion;
    private final TextChannel channel = mock(TextChannel.class);
    private final SlashCommandInteractionEvent event = mock(SlashCommandInteractionEvent.class, RETURNS_DEEP_STUBS);

    private void prepare(long length, Long interval, boolean warning) {
        when(event.isFromGuild()).thenReturn(true);
        when(event.getOption("name").getAsString()).thenReturn("Game");
        when(event.getOption("text-channel").getAsChannel().asTextChannel()).thenReturn(channel);
        when(event.getOption("length").getAsLong()).thenReturn(length);
        if (interval == null) when(event.getOption("notify-interval")).thenReturn(null);
        else when(event.getOption("notify-interval").getAsLong()).thenReturn(interval);
        when(event.getOption("one-minute-warning").getAsBoolean()).thenReturn(warning);
        when(event.getOption("notify-mention")).thenReturn(null);
        when(event.getOption("return-voice-channel")).thenReturn(null);
        when(channel.sendMessage(anyString())).thenAnswer(invocation -> {
            String text = invocation.getArgument(0);
            MessageCreateAction action = mock(MessageCreateAction.class, RETURNS_SELF);
            doAnswer(ignored -> { notices.add(new Notice(text, 0)); return null; }).when(action).queue();
            when(action.queueAfter(anyLong(), eq(TimeUnit.MINUTES))).thenAnswer(call -> {
                notices.add(new Notice(text, call.getArgument(0)));
                return null;
            });
            when(action.onSuccess(any())).thenAnswer(call -> {
                completion = call.getArgument(0);
                return action;
            });
            return action;
        });
    }

    @ParameterizedTest
    @CsvSource(value = {
        "10|null|false|0,10", "10|20|false|0,10",
        "10|3|false|0,3,6,9,10", "10|3|true|0,3,6,9,10",
        "10|4|true|0,4,8,9,10", "10|5|false|0,5,10",
        "1|1|true|0,1"
    }, delimiter = '|', nullValues = "null")
    void schedulesOnlyIntendedNotices(long length, Long interval, boolean warning, String expected) {
        prepare(length, interval, warning);
        new Timer().execute(event);
        assertEquals(expected, String.join(",", notices.stream().map(n -> Long.toString(n.minutes())).toList()));
        for (Notice notice : notices.subList(1, notices.size() - 1)) {
            long remaining = length - notice.minutes();
            assertTrue(notice.text().contains(remaining == 1 ? "***1*** minute" : "**" + remaining + "** minutes"));
        }
        assertTrue(notices.getLast().text().contains("complete"));
        assertNotNull(completion);
        completion.accept(mock(Message.class)); // No destination is a valid completion path.
    }

    @Test
    void omittedOptionalSettingsUseDefaultsAndMentionsApplyToEveryNotice() {
        prepare(3, null, false);
        when(event.getOption("one-minute-warning")).thenReturn(null);
        OptionMapping mention = mock(OptionMapping.class, RETURNS_DEEP_STUBS);
        when(mention.getAsMentionable().getAsMention()).thenReturn("<@123>");
        when(event.getOption("notify-mention")).thenReturn(mention);
        new Timer().execute(event);
        assertEquals(List.of(0L, 3L), notices.stream().map(Notice::minutes).toList());
        assertTrue(notices.stream().allMatch(n -> n.text().startsWith("<@123> ")));
    }

    @Test
    void voiceReturnReadsMembershipAtCompletionAndMovesOnlyConnectedMembers() {
        prepare(5, null, false);
        VoiceChannel destination = mock(VoiceChannel.class);
        Guild guild = mock(Guild.class, RETURNS_DEEP_STUBS);
        when(destination.getGuild()).thenReturn(guild);
        OptionMapping option = mock(OptionMapping.class, RETURNS_DEEP_STUBS);
        when(option.getAsChannel().asVoiceChannel()).thenReturn(destination);
        when(event.getOption("return-voice-channel")).thenReturn(option);
        new Timer().execute(event);
        verify(guild, never()).getMembers();
        Member connected = member(true);
        Member disconnected = member(false);
        when(guild.getMembers()).thenReturn(List.of(connected, disconnected));
        completion.accept(mock(Message.class));
        verify(guild.moveVoiceMember(connected, destination)).queueAfter(3, TimeUnit.SECONDS);
        verify(guild, never()).moveVoiceMember(disconnected, destination);
    }

    @Test
    void rejectsNonGuildInvocationBeforeReadingOptions() {
        when(event.isFromGuild()).thenReturn(false);
        new Timer().execute(event);
        verify(event, never()).getOption(anyString());
        verifyNoInteractions(channel);
    }

    private static Member member(boolean inVoice) {
        Member member = mock(Member.class);
        GuildVoiceState state = mock(GuildVoiceState.class);
        when(member.getVoiceState()).thenReturn(state);
        when(state.inAudioChannel()).thenReturn(inVoice);
        return member;
    }
}
