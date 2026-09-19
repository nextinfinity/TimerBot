# TimerBot

A Discord bot for timed reminders and optionally returning users to a voice channel. This may be useful to facilitate a game or other activity that involves users splitting up into separate channels for some period of time, then returning to a main group channel.

Use `/timer` with the following parameters:
- `name`: Friendly name for the timer in text chat
- `text-channel`: Channel to post timer completion and optional updates in
- `length`: Timer duration, in minutes
- `notify-interval` (OPTIONAL): Interval to send updates after, in minutes (i.e. 5 will send an update every 5 minutes during the timer)
- `one-minute-warning` (OPTIONAL): Whether or not to send an additional update with one minute left
- `notify-mention` (OPTIONAL): User or role to tag with each timer update
- `return-voice-channel` (OPTIONAL): Voice channel to move ALL users in voice to at the end of the timer

## Run

Create a Discord application/bot and invite it with the `bot` and `applications.commands` scopes. Grant View Channels and Send Messages in the target text channel. Voice return additionally needs Move Members and access to the destination voice channel. Mentioning roles may require mention permissions. No privileged gateway intents are required. Then start the bot by running:

```sh
docker run -d --name timerbot --restart unless-stopped -e DISCORD_BOT_TOKEN="{YOUR BOT TOKEN}" ghcr.io/nextinfinity/timerbot:latest
```

## Build locally

Requires JDK 25.

```sh
./gradlew clean shadowJar
export DISCORD_BOT_TOKEN='your-token'
java -jar build/libs/TimerBot-*-all.jar
```
