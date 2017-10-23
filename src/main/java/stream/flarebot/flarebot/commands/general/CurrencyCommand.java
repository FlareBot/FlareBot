package stream.flarebot.flarebot.commands.general;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import stream.flarebot.flarebot.FlareBot;
import stream.flarebot.flarebot.commands.Command;
import stream.flarebot.flarebot.commands.CommandType;
import stream.flarebot.flarebot.objects.GuildWrapper;
import stream.flarebot.flarebot.util.MessageUtils;
import stream.flarebot.flarebot.util.currency.CurrencyComparison;
import stream.flarebot.flarebot.util.currency.CurrencyConversionUtil;

import java.awt.Color;
import java.io.IOException;
import java.util.Random;

public class CurrencyCommand implements Command {

    private Random random = new Random();

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

                    CurrencyComparison comparison;
                    if (from.equalsIgnoreCase(to)) {
                        if ((random.nextInt(100) + 1) == 100) {
                            channel.sendMessage("I had hoped you didn't need me for that...").complete();
                            FlareBot.getInstance().logEG("Convert a currency to itself...", this, guild.getGuild(), sender);
                        }
                        comparison = new CurrencyComparison(from, to, (double) 1);
                    } else {
                        comparison = CurrencyConversionUtil.getCurrencyComparison(from, to);
                    }

                    channel.sendMessage(getCurrencyRatesEmbed(sender, comparison)).queue();

                    return;
                } catch (IOException e) {
                    MessageUtils.sendException("There was an error completing your request! \n" +
                            "Please join the support guild: " + FlareBot.INVITE_URL, e, channel);
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
        return "`{%}currency <from> <to>` - Displays the currency conversion rates for two currencies";
    }

    @Override
    public CommandType getType() {
        return CommandType.GENERAL;
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
