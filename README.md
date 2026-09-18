# TimerBot

A Discord bot for timed reminders and optionally returning users to a voice channel.

Use `/timer` with required `name`, `text-channel`, and `length` (minutes). Optional settings: `notify-interval`, `one-minute-warning`, `notify-mention`, and `return-voice-channel`.

**Limits:** Duration: 0–1440 minutes (0 completes immediately). Notification interval: 1–1440 minutes; omitted means no interval reminders. At most 60 reminders per timer, including the optional one-minute warning; 3 active timers per user across servers and 10 per server. Invalid requests are rejected before scheduling.

**Known caveats:** Timers are in memory and are lost on restart. The return-channel option moves **all cached members currently in voice**, not just the caller's channel. Restrict command access to trusted users in Discord's integration settings.

## Run

Create a Discord application/bot and invite it with the `bot` and `applications.commands` scopes. Grant View Channels and Send Messages in the target text channel. Voice return additionally needs Move Members and access to the destination voice channel. Mentioning roles may require mention permissions. No privileged gateway intents are required.

```sh
cp .env.example .env
# Edit .env and set DISCORD_BOT_TOKEN.
docker build -t timerbot .
docker run -d --name timerbot --restart unless-stopped --env-file .env timerbot
```

After the workflow publishes an image, replace `timerbot` in the run command with `ghcr.io/nextinfinity/timerbot:master`. Published tags include `master`, release tags, `sha-<commit>` (short SHA), and `latest` (most recent non-prerelease publication). CI builds Linux amd64 images. Initial GHCR packages may need to be made public in package settings for anonymous pulls.

`DISCORD_BOT_TOKEN` is the only environment variable. Keep it secret; `.env` files are ignored and excluded from Docker builds.

## Build locally

Requires JDK 25.

```sh
./gradlew clean shadowJar
export DISCORD_BOT_TOKEN='your-token'
java -jar build/libs/TimerBot-*-all.jar
```

## CI

Pull requests build without publishing. Pushes to `master`, published releases, and manual runs build and publish to GHCR using `GITHUB_TOKEN`, including provenance attestations. Docker Hub secrets are no longer needed. Manual runs publish tags for the selected ref, not `latest`.
