package stream.flarebot.flarebot.commands.currency;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.permissions.Permission;
import stream.flarebot.flarebot.util.Constants;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.currency.CurrencyComparison;
import stream.flarebot.flarebot.util.currency.CurrencyConversionUtil;

import java.awt.Color;
import java.io.IOException;

public class CurrencyCommand implements Command {

    @Override
    public void onCommand(User sender, GuildWrapper guild, TextChannel channel, Message message, String[] args, Member member) {
        if (args.length >= 1) {
            if (args.length == 1) {
                MessageUtils.sendErrorMessage("You must specify a target currency!", channel);
            } else if (args.length == 2) {
                if (!CurrencyConversionUtil.normalEndpointAvailable()) {
                    MessageUtils.sendWarningMessage("The normal currencies API is not available! Results may not be complete!", channel);
                }
                if (!CurrencyConversionUtil.cryptoEndpintAvailable()) {
                    MessageUtils.sendWarningMessage("The crypto currencies API is not available! Results may not be complete!", channel);
                }
                String from = args[0];
                String to = args[1];
                try {
                    if (!CurrencyConversionUtil.isValidCurrency(from)) {
                        MessageUtils.sendErrorMessage("The currency `" + from + "` is not valid!", channel);
                        return;
                    }
                    if (!CurrencyConversionUtil.isValidCurrency(to)) {
                        MessageUtils.sendErrorMessage("The currency `" + to + "` is not valid!", channel);
                        return;
                    }

                    channel.sendMessage(getCurrencyRatesEmbed(sender,
                            CurrencyConversionUtil.getCurrencyComparison(channel, sender, this, from, to))).queue();
                    return;
                } catch (IOException e) {
                    MessageUtils.sendException("There was an error completing your request! \n" +
                            "Please join the support guild: " + Constants.INVITE_URL, e, channel);
                }
            }
        }
        MessageUtils.sendUsage(this, channel, sender, args);
    }

    @Override
    public String getCommand() {
        return "currency";
    }

    @Override
    public String getDescription() {
        return "Allows viewing of currency conversion rates.";
    }

    @Override
    public String getUsage() {
        return "`{%}currency <from> <to>` - Displays the currency conversion rates for two currencies.";
    }

    @Override
    public Permission getPermission() {
        return Permission.CURRENCY_COMMAND;
    }

    @Override
    public CommandType getType() {
        return CommandType.CURRENCY;
    }

    @Override
    public boolean isBetaTesterCommand() {
        return false;
    }

    private MessageEmbed getCurrencyRatesEmbed(User sender, CurrencyComparison c) {
        EmbedBuilder builder = MessageUtils.getEmbed(sender);
        builder.setColor(Color.CYAN)
                .setDescription("Currency Conversion Rates")
                .addField("From", c.getBase(), true)
                .addField("To", c.getTo(), true)
                .addField("Rate", ConvertCommand.DECIMAL_FORMAT.format(c.getRate()), false);
        return builder.build();
    }


}
