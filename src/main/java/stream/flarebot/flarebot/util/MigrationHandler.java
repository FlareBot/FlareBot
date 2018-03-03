package stream.flarebot.flarebot.util;

import net.dv8tion.jda.core.entities.Guild;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.FlareBotManager;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Group;

import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * For the 100 times we make breaking changes and need to do migration.
 * //TODO: Make this async but without causing any possible issues to loaded guilds.
 */
public class MigrationHandler {

    public int migratePermissionsForGuild(String[] oldPermissions, String[] newPermissions, Guild guild) {
        if (oldPermissions.length != newPermissions.length) {
            throw new IllegalArgumentException("Err... I haven't made a way to do this yet. So go make that!");
        }
        int migrated = 0;
        for (int i = 0; i < newPermissions.length; i++) {
            migrated += migrateSinglePermissionForGuild(oldPermissions[i], newPermissions[i], guild);
        }
        return migrated;
    }

    public int migratePermissionsForAllGuilds(String[] oldPermissions, String[] newPermissions) {
        return FlareBot.getInstance().getGuilds().stream().mapToInt(g -> migratePermissionsForGuild(oldPermissions, newPermissions, g)).sum();
    }

    public int migrateSinglePermissionForGuild(String oldPermission, String newPermission, Guild guild) {
        int i = 0;
        GuildWrapper wrapper = null;
        try {
            wrapper = FlareBotManager.getInstance().getGuildNoCache(guild.getId());
            Pattern oldPerm = Pattern.compile("\\b" + oldPermission.replaceAll("\\.", "\\.") + "\\b"); // Make sure it is exact permission
            for (Group g : wrapper.getPermissions().getListGroups()) {
                for (final Iterator<String> it = g.getPermissions().iterator(); it.hasNext(); ) {
                    String perm = it.next();
                    if (oldPerm.matcher(perm).find()) {
                        it.remove();
                        g.getPermissions().add(perm.replace(oldPermission, newPermission));
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            FlareBot.LOGGER.error("Migration failed", e);
        }
        FlareBotManager.getInstance().saveGuild(guild.getId(), wrapper, System.currentTimeMillis());
        return i;
    }

    public int migrateSinglePermissionForAllGuilds(String oldPermission, String newPermission) {
        return FlareBot.getInstance().getGuilds().stream().mapToInt(g -> migrateSinglePermissionForGuild(oldPermission, newPermission, g)).sum();
    }
}
