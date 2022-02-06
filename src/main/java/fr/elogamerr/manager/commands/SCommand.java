package fr.elogamerr.manager.commands;

import fr.elogamerr.manager.files.FileManager;
import fr.elogamerr.manager.messages.MsgManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SCommand
{
	protected CommandsExecutor commandsExecutor;
	protected SubCommand[] subCommands;
	protected SubCommand defaultSubCommand;
	protected MsgManager msg;
	protected String name;
	protected boolean helpEnabled;
	protected SubCommand helpSubCommand;
	protected SubCommand reloadSubCommand;
	protected FileManager fileManager;

	public SCommand(CommandsExecutor commandsExecutor, String name, SubCommand[] subCommands, SubCommand defaultSubCommand, MsgManager msg, boolean helpEnabled, FileManager fileManager)
	{
		this.commandsExecutor = commandsExecutor;
		this.name = name;
		this.subCommands = subCommands;
		this.defaultSubCommand = defaultSubCommand;
		this.msg = msg;
		this.helpEnabled = helpEnabled;
		this.fileManager = fileManager;

		if(this.helpEnabled)
		{
			this.helpSubCommand = new HelpCommand();
			this.helpSubCommand.scommand = this;
		}
		if(this.fileManager != null)
		{
			this.reloadSubCommand = new ReloadCommand();
			this.reloadSubCommand.scommand = this;
		}
	}
	
	public void execute(CommandSender sender, Command cmd, String msg, String[] args)
	{
		if(args.length != 0)
		{
			for(SubCommand subCommand : this.subCommands)
			{
				for(String alias : subCommand.aliases)
				{
					if(alias.equalsIgnoreCase(args[0]))
					{
						List<String> argsList = new ArrayList<String>(Arrays.asList(args));
						argsList.remove(0);
						subCommand.execute(sender, cmd, msg, argsList, alias);
						return;
					}
				}
			}
			if(this.helpSubCommand != null)
			{
				for(String alias : this.helpSubCommand.aliases)
				{
					if(alias.equalsIgnoreCase(args[0]))
					{
						List<String> argsList = new ArrayList<String>(Arrays.asList(args));
						argsList.remove(0);
						this.helpSubCommand.execute(sender, cmd, msg, argsList, alias);
						return;
					}
				}
			}
			if(this.reloadSubCommand != null)
			{
				for(String alias : this.reloadSubCommand.aliases)
				{
					if(alias.equalsIgnoreCase(args[0]))
					{
						List<String> argsList = new ArrayList<String>(Arrays.asList(args));
						argsList.remove(0);
						this.reloadSubCommand.execute(sender, cmd, msg, argsList, alias);
						return;
					}
				}
			}
		}

		if(this.defaultSubCommand != null)
		{
			this.defaultSubCommand.execute(sender, cmd, msg, new ArrayList<String>(Arrays.asList(args)), null);
			return;
		}
		
		sender.sendMessage(this.msg.err("Veuillez essayer comme ceci :"));
		if(!this.helpEnabled)
		sender.sendMessage(this.getUseageTemplate());
		else
		sender.sendMessage(ChatColor.GRAY+"/"+this.name+" help");
	}

	public List<String> completeTab(CommandSender sender, Command cmd, String msg, String[] args)
	{
		if(args.length != 0)
		{
			for(SubCommand subCommand : this.subCommands)
			{
				for(String alias : subCommand.aliases)
				{
					if(alias.equalsIgnoreCase(args[0]))
					{
						return subCommand.onTabComplete(sender, cmd, msg, Arrays.copyOfRange(args, 1, args.length));
					}
				}
			}
		}

		if(this.defaultSubCommand != null)
		{
			return this.defaultSubCommand.onTabComplete(sender, cmd, msg, args);
		}

		return null;
	}

	public String getUseageTemplate()
	{
		StringBuilder message = new StringBuilder(ChatColor.GRAY + "/" + this.name + " ");

		message.append(this.defaultSubCommand != null ? "[" : "<");
		
		for(SubCommand subCommand : this.subCommands)
		{
			if(!subCommand.aliases.isEmpty())
			message.append(subCommand.aliases.get(0)).append(",");
		}

		message = new StringBuilder(message.substring(0, message.length() - 1));

		message.append(this.defaultSubCommand != null ? "]" : ">");
		
		return message.toString();
	}
}
