package stream.flarebot.flarebot.commands.currency;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.currency.CurrencyComparison;
import stream.flarebot.flarebot.util.currency.CurrencyConversionUtil;

import java.awt.Color;
import java.io.IOException;
import java.text.DecimalFormat;

public class ConvertCommand implements Command {

    static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.#################");

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length == 3) {
            if (!CurrencyConversionUtil.normalEndpointAvailable()) {
                MessageUtils.sendWarningMessage("The normal currencies API is not available! Results may not be complete!", channel);
            }
            if (!CurrencyConversionUtil.cryptoEndpintAvailable()) {
                MessageUtils.sendWarningMessage("The crypto currencies API is not available! Results may not be complete!", channel);
            }
            String from = args[1];
            String to = args[2];
            try {
                if (!CurrencyConversionUtil.isValidCurrency(from)) {
                    MessageUtils.sendErrorMessage("The currency `" + from + "` is not valid!", channel);
                    return;
                }
                if (!CurrencyConversionUtil.isValidCurrency(to)) {
                    MessageUtils.sendErrorMessage("The currency `" + to + "` is not valid!", channel);
                    return;
                }

                Double amount;
                try {
                    amount = Double.parseDouble(args[0]);
                } catch (NumberFormatException e) {
                    MessageUtils.sendErrorMessage("That is not a valid amount!", channel);
                    return;
                }
                channel.sendMessage(getCurrencyConversionEmbed(sender,
                        CurrencyConversionUtil.getCurrencyComparison(channel, sender, this, from, to), amount)).queue();

                return;
            } catch (IOException e) {
                MessageUtils.sendException("There was an error completing your request! \n" +
                        "Please join the support guild: " + Constants.INVITE_URL, e, channel);
            }
        }
        MessageUtils.sendUsage(this, channel, sender, args);
    }

    @Override
    public String getCommand() {
        return "convert";
    }

    @Override
    public String getDescription() {
        return "Allows conversion of currencies";
    }

    @Override
    public String getUsage() {
        return "`{%}convert <amount> <from> <to>` - Converts an amount of one currency to another.";
    }

    @Override
    public CommandType getType() {
        return CommandType.CURRENCY;
    }

    @Override
    public boolean isBetaTesterCommand() {
        return false;
    }

    private MessageEmbed getCurrencyConversionEmbed(User sender, CurrencyComparison c, Double from) {
        EmbedBuilder builder = MessageUtils.getEmbed(sender);
        builder.setColor(Color.CYAN)
                .setDescription("Currency Conversion")
                .addField(c.getBase(), DECIMAL_FORMAT.format(from), true)
                .addField(c.getTo(), DECIMAL_FORMAT.format(from * c.getRate()), true);
        return builder.build();
    }


}
