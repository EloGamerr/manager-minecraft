package fr.elogamerr.manager.messages.enums;

import fr.elogamerr.manager.messages.MsgManager;
import net.md_5.bungee.api.ChatColor;

public enum MessagesStyles
{
    INFO(ChatColor.GOLD.toString()),
    ERROR(ChatColor.RED.toString()),
    IMPORTANT_WORD(ChatColor.YELLOW.toString() + ChatColor.BOLD),
    COMMAND(ChatColor.GRAY.toString()),
    SCOREBOARD_TITLE(ChatColor.DARK_AQUA.toString() + ChatColor.BOLD),
    SCOREBOARD_LINE(ChatColor.GOLD.toString());

    private final String style;

    MessagesStyles(String style)
    {
        this.style = style;
    }

    public String getStyle(MsgManager msgManager)
    {
        switch(this) {
            case INFO -> {
                return msgManager.getInfoColor();
            }
            case ERROR -> {
                return msgManager.getErrColor();
            }
            case IMPORTANT_WORD -> {
                return msgManager.getWarningColor();
            }
            case COMMAND -> {
                return msgManager.getCmdColor();
            }
            case SCOREBOARD_TITLE -> {
                return msgManager.getScTitleColor();
            }
            case SCOREBOARD_LINE -> {
                return msgManager.getScLineColor();
            }
        }
        return null;
    }

    public String getDefaultStyle() {
        return this.style;
    }
}
