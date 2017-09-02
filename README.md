# FlareBot [![Discord](https://discordapp.com/api/guilds/226785954537406464/widget.png)](https://discord.gg/TTAUGvZ)
Growing Discord music bot made with JDA (Orginally D4J) in Java

# Commands
Commands are moved [here](https://flarebot.stream/#commands)!

# Support
If you need any help with the bot or you think you have found a bug please join our official Discord server and report it there!  
[![](https://discordapp.com/api/guilds/226785954537406464/embed.png?style=banner1)](https://discord.gg/TTAUGvZ)

# Self Hosting
Self hosting is actually pretty easy. The software currently only supports UNIX-Like due to others not having sane methods of obtaining CPU stats through Java.  
The following requirements must be met:
* Cassandra (with `cassandra.username`, `cassandra.password`, set and `cassandra.nodes` set optionally to an array of IPs as strings
* MariaDB/MySQL (database name `flarebot` user name `user` on `localhost:3306`
* UNIX-Like OS

## Build instructions:

1. Clone the repository and switch to dev (`git clone <fill this in>` and `git checkout dev`)
2. Replace all occurances of `158310004187725824` with your Discord ID
3. Replace all occurances of `226786557862871040` with your error channel ID
4. Replace everything in ` public String postToApi(String action, String property, JsonElement data)`  with `return "";` in `src/main/java/stream/flarebot/flarebot/FlareBot.java`
5. Empty the entire `private void postToBotlist(String auth, String url)` function in `src/main/java/stream/flarebot/flarebot/FlareBot.java`
6. Run `mvn clean package`
7. Copy target/*-dependencies.jar into .

The following systemd service file should be used:
```ini
[Unit]
Description=Flare
After=syslog.target network.target

[Service]
User=<<YOU>>
Group=<<YOU>>

Type=simple
WorkingDirectory=YOUR WORKING DIRECTORY
ExecStart=/usr/bin/java -Djna.nosys=true -jar FlareBot-jar-with-dependencies.jar -t <Token> -s <GitHub Webhook Secret> -db something -sql <SQL Password> -yt <YouTube API Token> -websecret whatever -bl whatever -sh <status webhook>
TimeoutStopSec=120
KillMode=process
Restart=on-success

[Install]
WantedBy=multi-user.target
```

# Donation
If you want to help FlareBot's hosting and development please use [this link](https://www.paypal.me/FlareBot). Thank you!

## Credits
* Initially started by [WalshyDev(@bwfcwalshy#1284)](https://github.com/WalshyDev/) and [ArsenArsen(@Arsen#7525)](https://github.com/ArsenArsen/)
* FlareBot's Avatar is made by [CaptainBaconz](https://www.twitch.tv/captainbaconz)
* Big thanks to EJ Technologies for providing us with open source licenses for [their Java profiler](https://www.ej-technologies.com/products/jprofiler/overview.html). 
