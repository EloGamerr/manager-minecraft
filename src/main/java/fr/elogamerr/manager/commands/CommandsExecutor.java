package fr.elogamerr.manager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

class CommandsExecutor implements CommandExecutor, TabCompleter
{
	boolean isBungeeLinked;
	JavaPlugin plugin;
	CommandsManager manager;

	public CommandsExecutor(JavaPlugin plugin, CommandsManager manager)
	{
		this.plugin = plugin;
		this.manager = manager;
		this.isBungeeLinked = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args)
	{
		for(SCommand scmd : this.manager.getCommands())
		{
			if(scmd.getName().equalsIgnoreCase(cmd.getName()))
			{
				scmd.execute(sender, cmd, msg, args);
			}
		}
		return false;
	}

	public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String msg, final String[] args)
	{
		for(SCommand scmd : this.manager.getCommands())
		{
			if(scmd.getName().equalsIgnoreCase(cmd.getName()))
			{
				return scmd.completeTab(sender, cmd, msg, args);
			}
		}
		return null;
	}
}
