# Changelog for FlareBot v3

## Added
 - Command usages that look nicer and more consistent.
 - Ability to create a pin without a previous message existing.
 - Ability for commands to have discord permissions associated with them. **Not yet implemented**
 - The `report` and `reports` command to allow users in a guild to report users which moderators can then view and take action against the person who was reported.
 - Added `summon` as an alias to the `join` command.
 - Added `gtfo`, `getout` and `banish` as aliases for the `leave` command.
 - Added `playing` as an alias to the `song` command.
 - The `shardinfo` command to show information on all FlareBot's shards.
 - Welcome messages can now be used in user's DMs as well as in a welcome channel.
 - `prune` command to prune guild members for being in-active for a certain amount of days. This command asks twice to make sure this isn't run by accident.
 - Auto-spam detection for guilds. The number of messages allowed per second scales dynamically with the size of a guild. The equation for this can be found [here](https://github.com/FlareBot/FlareBot/blob/dev/src/main/java/stream/flarebot/flarebot/Events.java#L394). If a guild exceeds their limit then they will be blocked for 5 minutes from the bot.
 - `serverinfo` command which shows information about a guild. Example [here](https://user-images.githubusercontent.com/10491247/28494542-870addf0-6ef7-11e7-825f-b117984d5fbf.PNG)
 - `fix` command to fix issues that are usually caused by downtime or other issues. **The scope of this is limited and does not remove the need to contact staff about issues!**
 - Feature for us to block your guild from using FlareBot. This will happen if you spam or abuse our system and can/will happen at Walshy's discretion.
 - Added a beta test pin feature - if a post in the official guild gets 5 pin reactions that post will be pinned. This is a test and will be adapted on before we released it as a feature to everyone.
 - Added `repeat` command, this will play the current song again.
 - Added multiple modertion commands - `ban`, `forceban`, `kick` and `mute`. 
 
 - Easter eggs! Gotta find 'em all!

## Changed
 - All commands *should* now accept names and mentions for users instead of just mentions.
 - Changed ugly progress text in `song` command into a pretty progress bar.
 - The `songnick` command now toggles instead of having a argument to change it.
 - The GitHub webhook (FlareBot's guild only) is now more compact. See also `Fixed`
 - The poll command actually works now.
 - We now use OkHTTP instead of UniRest which should give us better performance overall.
 - HUGE change to the permissions command to allow per-user permissions and also sem-validation permissions that are entered. The user experience for the command has been improved a lot.
 - The permissions command now supports mass adding roles and `@everyone` and `@here` to permission groups.
 - The `play` command now makes the bot join the voice channel if it isn't in any other channel.
 - Our entire backend data management, this doesn't mean much to you but means a lot to us!
 - Enabled livestreams! You can now queue a livestream normally and we have improved how it is displayed in the song command
 - Internal logging

## Fixed
 - Stopped Auto assign throwing an error if a role is deleted.
 - Stoppped people purging ridulous amounts of messages *cough cough*. This will relieve peoples tendencies to purge whole channels and make our error feed less full.
 - `invite` command no longer throws an error if it can't PM the user. It will instead send the message where the command was sent.
 - Duplicate spaces in commands are now ignored.
 - GitHub webhook (FlareBot's guild only) now doesn't error on huge merges by only showing the last 5 commits.
 - YouTube errors are now handled a lot better (and don't spam us anymore) by trying 3 times and on fail sending an error message to the user.
 - Escape out \` to stop people breaking stuff ;)

## Deprecated
 - The search command is being phased out in favour of the `play` command. The `search` command will be repurposed in a future update.
