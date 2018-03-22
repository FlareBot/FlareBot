package stream.flarebot.flarebot.util.objects;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.Getters;
import stream.flarebot.flarebot.util.buttons.ButtonRunnable;

import java.util.ArrayList;
import java.util.List;

public class ButtonGroup {

    private List<Button> buttons;

    public ButtonGroup() {
        buttons = new ArrayList<>();
    }

    /**
     * Adds a button to the button group.
     *
     * @param btn The button which you would like to add.
     */
    public void addButton(Button btn) {
        // Note I don't check if it already exists... so just don't fuck up :blobthumbsup:
        this.buttons.add(btn);
    }

    public List<Button> getButtons() {
        return buttons;
    }

    public static class Button {

        private String unicode;
        private long emoteId;
        private ButtonRunnable runnable;
        private Message message;

        public Button(String unicode, ButtonRunnable runnable) {
            this.unicode = unicode;
            this.runnable = runnable;
        }

        public Button(long emoteId, ButtonRunnable runnable) {
            this.emoteId = emoteId;
            this.runnable = runnable;
        }

        public long getEmoteId() {
            return emoteId;
        }

        public String getUnicode() {
            return unicode;
        }

        public void addReaction(Message message) {
            if (!(message.getChannel().getType() == ChannelType.TEXT && message.getGuild().getSelfMember()
                    .hasPermission(message.getTextChannel(), Permission.MESSAGE_HISTORY))) {
                message.getChannel().sendMessage("I can't add buttons due to not having the `Message History` permission!").queue();
                return;
            }

            this.message = message;
            if (unicode != null)
                message.addReaction(unicode).queue();
            else
                message.addReaction(Getters.getEmoteById(emoteId)).queue();
        }

        public void onClick(User user) {
            runnable.run(user, message);
        }
    }
}
