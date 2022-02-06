package fr.elogamerr.manager.messages.enums;

import net.md_5.bungee.api.ChatColor;

public enum MessagesStyles
{
    INFO(""+ ChatColor.GOLD),
    ERROR(""+ ChatColor.RED),
    IMPORTANT_WORD(ChatColor.YELLOW+""+ ChatColor.BOLD),
    COMMAND(""+ ChatColor.GRAY),
    SCOREBOARD_TITLE(ChatColor.DARK_AQUA+""+ ChatColor.BOLD),
    SCOREBOARD_LINE(""+ ChatColor.GOLD);

    private final String style;

    MessagesStyles(String style)
    {
        this.style = style;
    }

    public String getStyle()
    {
        return this.style;
    }
}
