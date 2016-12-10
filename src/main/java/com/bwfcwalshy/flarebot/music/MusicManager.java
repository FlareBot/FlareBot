package com.bwfcwalshy.flarebot.music;

@Deprecated
public class MusicManager {
/*
    // Guild ID | Queue
    private Map<String, Player> players;
    private AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    private FlareBot bot;
    private List<Class<? extends AudioSourceManager>> registered = new CopyOnWriteArrayList<>();

    public MusicManager(FlareBot flareBot) {
        players = new ConcurrentHashMap<>();
        this.bot = flareBot;
    }

    public void pause(String guildId) {
        if (players.containsKey(guildId))
            players.get(guildId).setPaused(true);
    }

    public void play(String guildId) {
        if (players.containsKey(guildId))
            players.get(guildId).play();
    }

    public void skip(String guildId) {
        if (players.containsKey(guildId))
            players.get(guildId).skip();
    }

    public void skip(String guildId, int songs) {
        if (players.containsKey(guildId))
            for (int i = 0; i < songs; i++)
                players.get(guildId).skip();
    }

    public void stop(String guildId) {
        if (players.containsKey(guildId)) {
            players.get(guildId).stop();
        }
    }

    public void shuffle(String guildId) {
        if (players.containsKey(guildId)) {
            players.get(guildId).shuffle();
        }
    }

    public Map<String, Player> getPlayers() {
        return players;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public Player getPlayer(String id) {
        return players.computeIfAbsent(id, gid -> {
            Player ply = new Player();
            AudioPlayer.getAudioPlayerForGuild(bot.getClient().getGuildByID(gid)).queue(ply);
            return ply;
        });
    }

    public Player getPlayer(String id, Extractor extractor) throws IllegalAccessException, InstantiationException {
        if (!registered.contains(extractor.getSourceManagerClass())) {
            playerManager.registerSourceManager(extractor.getSourceManagerClass().newInstance());
            registered.add(extractor.getSourceManagerClass());
        }
        return getPlayer(id);
    }

    public boolean hasPlayer(String id) {
        return players.containsKey(id);
    }*/
}
