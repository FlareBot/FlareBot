# Self Hosting
By following this guide you agree to:
* Not use our logos
* Only use the bot for private use
* Do not hog the support server
* Claiming the source code of the bot

Self hosting is actually pretty easy. The software currently only supports UNIX-Like due to others not having sane methods of obtaining CPU stats through Java.  
The following requirements must be met:
* Cassandra (with `cassandra.username`, `cassandra.password`, set and `cassandra.nodes` set optionally to an array of IPs as strings
* UNIX-Like OS

## Build instructions:

1. Clone the repository and switch to dev (`git clone <fill this in>` and `git checkout dev`)
2. Replace all occurances of `158310004187725824` with your Discord ID
3. Replace all occurances of `226786557862871040` with your error channel ID
4. Replace everything in ` public String postToApi(String action, String property, JsonElement data)`  with `return "";` in `src/main/java/stream/flarebot/flarebot/FlareBot.java`
5. Empty the entire `private void postToBotlist(String auth, String url)` function in `src/main/java/stream/flarebot/flarebot/FlareBot.java`
6. Run `mvn clean package`
7. Copy target/\*-dependencies.jar into .

The following is an example config file (`config.json`):
```json
{
    "cassandra": {
        "username": "<username>",
        "password": "<password>",
        "nodes": [
            "<IPs>"
        ]
    }
}
```

The following systemd service file should be used:
```ini
[Unit]
Description=Flare
After=syslog.target network.target

[Service]
User=YOUR USER
Group=YOUR GROUP

Type=simple
WorkingDirectory=YOUR WORKING DIRECTORY
ExecStart=/usr/bin/java -Djna.nosys=true -jar FlareBot-jar-with-dependencies.jar -t <Token> -s <GitHub Webhook Secret> -db something -sql <SQL Password> -yt <YouTube API Token> -websecret whatever -bl whatever -sh <status webhook>
TimeoutStopSec=120
KillMode=process
Restart=on-success

[Install]
WantedBy=multi-user.target
```
