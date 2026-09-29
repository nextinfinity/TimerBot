# Testing

Run `./gradlew test` (or `sh ./gradlew test`) with JDK 25. `./gradlew check`
includes the same suite. Reports are in `build/reports/tests/test/index.html`.
CI runs `check` in a separate Test job; container builds/publication depend on it.

## Scope

JUnit Jupiter and Mockito exercise the live `/timer` command without starting
Discord or waiting for real time. Tests capture scheduled notices and manually
invoke the completion callback. They cover:

- Reminder delays and remaining-minute calculations, including non-divisible
  intervals, defaults, and intervals longer than the timer.
- One-minute warning deduplication and one-minute timers. An interval reminder
  at one minute remaining uses the warning wording even if the flag is false.
- Optional mention propagation and non-server rejection.
- Voice membership read at completion, moving only members currently in voice,
  with the existing three-second delay.

Voice return currently runs only after a successful completion-message send.
The tests preserve that callback model; they do not introduce an independent
completion scheduler. There are no active-timer quotas or duration/reminder
caps beyond the command's existing option constraints. The unused TimerLimits
proposal has been removed rather than promoted into live behavior.

## Keeping this lightweight

- Add a case for a plausible application bug, not to increase coverage numbers.
- Prefer another row in the schedule table over another fixture.
- Assert intended delays/recipients, not exact message prose or JDA internals.
- No sleeps, credentials, network calls, bot startup, or tests of JDA's scheduler,
  REST delivery, command registration, reflection, or generated accessors.
- Fixes to substantive bugs should include a focused regression test.

After deployment or a JDA update, manually create a short timer in a test server
and check a reminder, completion, and optional voice return. This operational
smoke check is intentionally outside Gradle.
