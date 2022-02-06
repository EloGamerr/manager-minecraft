package fr.elogamerr.manager.messages;

import fr.elogamerr.manager.messages.enums.DefaultFontInfo;
import fr.elogamerr.manager.messages.enums.MessagesStyles;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class MsgManager
{

    String pluginName;
    LinkedHashMap<String, String> helpNameAndDesc = new LinkedHashMap<>();

    public MsgManager(String pluginName, LinkedHashMap<String, String> helpNameAndDesc)
    {
        this.pluginName = pluginName;
        this.helpNameAndDesc = helpNameAndDesc;
    }

    public MsgManager(String pluginName)
    {
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public LinkedHashMap<String, String> getHelpNameAndDesc() {
        return helpNameAndDesc;
    }

    private String parseMsg(String str, String messageStyle, boolean sc)
    {
        if(!str.endsWith(".") && !str.endsWith("!") && !str.endsWith("?") && !str.endsWith(":") && !str.endsWith(";"))
        {
            String[] strSplit = str.split(" ");
            if(!isUrl(strSplit[strSplit.length-1]))
            {
                str += messageStyle+".";
            }
        }


        return this.parse(str, sc);
    }

    public String parse(String str, boolean sc)
    {
        str = parseColor(str);

        if(!sc)
            str = parseTags(str);
        else
            str = parseTagsSc(str);

        return str;
    }

    // -------------------------------------------- //
    // Color parsing
    // -------------------------------------------- //

    private String parseTagsSc(String string)
    {
        return string.replace("<err>", MessagesStyles.ERROR.getStyle())
                .replace("<!>", MessagesStyles.IMPORTANT_WORD.getStyle())
                .replace("<cmd>", MessagesStyles.COMMAND.getStyle())
                .replace("<info>", MessagesStyles.SCOREBOARD_LINE.getStyle())
                .replace("$e$", MessagesStyles.ERROR.getStyle())
                .replace("$!$", MessagesStyles.IMPORTANT_WORD.getStyle())
                .replace("$c$", MessagesStyles.COMMAND.getStyle())
                .replace("$i$", MessagesStyles.SCOREBOARD_LINE.getStyle());
    }

    private String parseTags(String string)
    {
        return string.replace("<err>", MessagesStyles.ERROR.getStyle())
                .replace("<!>", MessagesStyles.IMPORTANT_WORD.getStyle())
                .replace("<cmd>", MessagesStyles.COMMAND.getStyle())
                .replace("<info>", MessagesStyles.INFO.getStyle())
                .replace("$e$", MessagesStyles.ERROR.getStyle())
                .replace("$!$", MessagesStyles.IMPORTANT_WORD.getStyle())
                .replace("$c$", MessagesStyles.COMMAND.getStyle())
                .replace("$i$", MessagesStyles.INFO.getStyle());
    }

    private String parseColor(String string)
    {
        string = parseColorAmp(string);
        return string;
    }

    public String parseColorAmp(String string)
    {
        string = string.replaceAll("(§([a-z0-9]))", "\u00A7$2");
        string = string.replaceAll("(&([a-z0-9]))", "\u00A7$2");
        string = string.replace("&&", "&");
        return string;
    }
    // -------------------------------------------- //
    // Message Sending Helpers
    // -------------------------------------------- //

    private String format(String msg, String pluginName, String messageStyle)
    {
        return this.formatWithoutPluginName(ChatColor.DARK_AQUA+"["+ ChatColor.BOLD+pluginName+ ChatColor.DARK_AQUA+"] "+messageStyle+msg, messageStyle);
    }

    public String formatWithoutPluginName(String msg, String messageStyle)
    {
        return this.parseMsg(messageStyle+msg, messageStyle, false);
    }

    public String info(String prefix, String msg)
    {
        return this.format(msg, prefix,  MessagesStyles.INFO.getStyle());
    }

    public String err(String prefix, String msg)
    {
        return this.format(msg, prefix,  MessagesStyles.ERROR.getStyle());
    }

    public String info(String msg)
    {
        return this.format(msg, this.pluginName, MessagesStyles.INFO.getStyle());
    }

    public String err(String msg)
    {
        return this.format(msg, this.pluginName, MessagesStyles.ERROR.getStyle());
    }

    private String getFormattedRemainingTime(long time, int type, boolean days, boolean hours, boolean minutes, boolean seconds) {
        final int secs = Math.abs((int)(time / 1000L - System.currentTimeMillis() / 1000L));
        String ret = "";
        if (secs >= 86400 && days) {
            if(type == 0)
                ret = String.valueOf(ret) + (secs / 86400 < 10 ? "0" : "") + secs / 86400 + " jour" + ((secs / 86400 > 1) ? "s" : "");
            else
                ret = String.valueOf(ret) + (secs / 86400 < 10 ? "0" : "") + secs / 86400 + "j";
        }
        if (secs % 86400 >= 3600 && hours) {
            if(type == 0)
                ret = String.valueOf(ret) + ((secs >= 86400 && days) ? ((secs % 3600 == 0) ? " et " : ", ") : "") + (secs % 86400 / 3600 < 10 ? "0" : "") + secs % 86400 / 3600 + " heure" + ((secs % 86400 / 3600 > 1) ? "s" : "");
            else
                ret = String.valueOf(ret) + (secs % 86400 / 3600 < 10 ? "0" : "") + secs % 86400 / 3600 + "h";
        }
        if (secs % 86400 % 3600 >= 60 && minutes) {
            if(type == 0)
                ret = String.valueOf(ret) + ((secs >= 3600 && hours) ? ((secs % 60 == 0) ? " et " : ", ") : "") + (secs % 86400 % 3600 / 60 < 10 ? "0" : "") + secs % 86400 % 3600 / 60 + " minute" + ((secs % 86400 % 3600 / 60 > 1) ? "s" : "");
            else
                ret = String.valueOf(ret) + (secs % 86400 % 3600 / 60 < 10 ? "0" : "") + secs % 86400 % 3600 / 60 + "m";
        }
        if (secs % 86400 % 3600 % 60 > 0 && seconds) {
            if(type == 0)
                ret = String.valueOf(ret) + ((secs > 60 && minutes) ? " et " : "") + (secs % 86400 % 3600 % 60 < 10 ? "0" : "") + secs % 86400 % 3600 % 60 + " seconde" + ((secs % 86400 % 3600 % 60 > 1) ? "s" : "");
            else
                ret = String.valueOf(ret) + (secs % 86400 % 3600 % 60 < 10 ? "0" : "") + secs % 86400 % 3600 % 60 + "s";
        }
        return ret;
    }

    public String getFormattedRemainingTime(long time, boolean days, boolean hours, boolean minutes, boolean seconds) {
        return this.getFormattedRemainingTime(time, 0, days, hours, minutes, seconds);
    }

    public String getShortFormattedRemainingTime(long time, boolean days, boolean hours, boolean minutes, boolean seconds) {
        return this.getFormattedRemainingTime(time, 1, days, hours, minutes, seconds);
    }

    public String getFormattedRemainingTime(long time) {
        return this.getFormattedRemainingTime(time, 0, true, true, true, true);
    }

    public String getShortFormattedRemainingTime(long time) {
        return this.getFormattedRemainingTime(time, 1, true, true, true, true);
    }

    public String wrongCmd(String cmd)
    {
        if(!cmd.replaceAll("(Â§([a-z0-9]))", "").replaceAll("(&([a-z0-9]))", "").replaceAll("&&", "").replaceAll("§([a-z0-9])", "").startsWith("/"))
            cmd = "/"+cmd;

        return this.err("Utilisation: <cmd>"+cmd);
    }

    public String noPermCmd()
    {
        return this.err("Vous n'avez pas la permission d'effectuer cette commande !");
    }

    public String noExistCmd()
    {
        return this.err("Cette commande n'existe pas !");
    }

    public void help(CommandSender sender)
    {
        if(this.helpNameAndDesc != null && !this.helpNameAndDesc.isEmpty())
        {
            List<String> cmds = new ArrayList<>();
            for(Entry<String, String> set : this.helpNameAndDesc.entrySet())
            {
                String cmd = set.getKey();

                if(!cmd.replaceAll("(§([a-z0-9]))", "").replaceAll("(&([a-z0-9]))", "").replaceAll("&&", "").replaceAll("§([a-z0-9])", "").startsWith("/"))
                    cmd = "/"+cmd;

                String[] descAndPerm = set.getValue().split(";");

                String desc = descAndPerm[0];
                String perm = null;
                if(descAndPerm.length == 2)
                    perm = descAndPerm[1];

                desc = this.parseMsg(desc, "", false);

                if(perm == null || perm.equalsIgnoreCase("") || sender.hasPermission(perm))
                    cmds.add(MessagesStyles.COMMAND.getStyle()+cmd + ChatColor.RED+" - " + MessagesStyles.INFO.getStyle()+desc);
            }
            if(!cmds.isEmpty())
            {
                sender.sendMessage("----------------- " + ChatColor.GOLD + "Aide " + this.pluginName + ChatColor.WHITE + " -----------------");
                for(String cmd : cmds)
                {
                    sender.sendMessage(cmd);
                }
                sender.sendMessage("---------------------------------------------");
            }
            else
            {
                sender.sendMessage(this.noPermCmd());
            }
        }
        else
        {
            sender.sendMessage(this.noExistCmd());
        }
    }

    private final int CENTER_PX = 130;

    public void sendStartMsgCenteredInChat(CommandSender sender, String message)
    {
        message = "§r§6 "+message+" ";
        if(message == null || message.equals("")) if(sender != null)sender.sendMessage("");;
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;


        for(char c : message.toCharArray()){
            if(c == '\u00a7'){
                previousCode = true;
                continue;
            }else if(previousCode == true){
                previousCode = false;
                if(c == 'l' || c == 'L'){
                    isBold = true;
                    continue;
                }else isBold = false;
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }
        double pair = 0;
        while(messagePxSize < 258)
        {
            messagePxSize = 0;
            if(((int)(pair/2)) == pair/2)
            {
                message = "§r§l§3§m "+message;
            }
            else
            {
                message = message+"§r§l§3§m ";
            }
            pair++;

            previousCode = false;
            isBold = false;


            for(char c : message.toCharArray())
            {
                if(c == '\u00a7')
                {
                    previousCode = true;
                    continue;
                }
                else if(previousCode == true){
                    previousCode = false;
                    if(c == 'l' || c == 'L'){
                        isBold = true;
                        continue;
                    }else isBold = false;
                }
                else
                {
                    DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                    messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                    messagePxSize++;
                }
            }

        }
        if(sender != null)
        {
            sender.sendMessage("§l§6»"+message+"§r§l§6«");
            sender.sendMessage("");
        }
        else
        {
            Bukkit.broadcastMessage("§l§6»"+message+"§r§l§6«");
            Bukkit.broadcastMessage("");
        }
    }

    public void sendMsgCenteredInChat(CommandSender sender, String message)
    {
        if(message == null || message.equals("")) if(sender != null)sender.sendMessage("");
        message = this.parseColor(message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '\u00a7'){
                previousCode = true;
                continue;
            }else if(previousCode == true){
                previousCode = false;
                if(c == 'l' || c == 'L'){
                    isBold = true;
                    continue;
                }else isBold = false;
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        if(messagePxSize > 258)
        {
            int rank = 35;

            while(message.toCharArray()[rank] != ' ')
            {
                rank--;
                if(rank == 25)
                {
                    rank = 35;
                    break;
                }
            }

            String color1 = "";
            int i = 0;
            for(char c : message.toCharArray())
            {
                if(c == '\u00a7')
                {
                    previousCode = true;
                    continue;
                }
                else if(previousCode == true)
                {
                    previousCode = false;
                    if(!color1.contains("§"+c))
                        color1 += "§"+c;
                }
                i++;
                if(i == rank)
                    break;
            }

            sendMsgCenteredInChat(sender, message.substring(0, rank));
            sendMsgCenteredInChat(sender, color1+message.substring(rank, message.length()));
            return;
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        if(sender != null)
            sender.sendMessage(sb.toString() + message);
        else
            Bukkit.broadcastMessage(sb.toString() + message);
    }

    public void sendEndMsgCenteredInChat(CommandSender sender)
    {
        if(sender != null)
            sender.sendMessage("§l§6»§r§l§o§3§m                                                               §r§l§6«");
        else
            Bukkit.broadcastMessage("§l§6»§r§l§o§3§m                                                               §r§l§6«");
    }

    public String msgCenteredInScreen(String msg)
    {
        msg = " ~ "+msg;
        return msg;
    }

    private static boolean isUrl(String msg) {
        try
        {
            URL url = new URL(msg);
            return true;
        }
        catch(MalformedURLException e)
        {
            return false;
        }
    }

    public String scTitle(String title)
    {
        title = MessagesStyles.SCOREBOARD_TITLE.getStyle()+title;

        return this.parse(title, true);
    }

    public String scLine(String line)
    {
        line = MessagesStyles.SCOREBOARD_LINE.getStyle()+line;

        return this.parse(line, true);
    }

    public void setHelp(LinkedHashMap<String, String> helpNameAndDesc)
    {
        this.helpNameAndDesc = helpNameAndDesc;
    }

    public void putAllHelp(LinkedHashMap<String, String> helpNameAndDesc)
    {
        if(this.helpNameAndDesc == null)
        {
            this.helpNameAndDesc = helpNameAndDesc;
        }
        else
        {
            this.helpNameAndDesc.putAll(helpNameAndDesc);
        }
    }
}
