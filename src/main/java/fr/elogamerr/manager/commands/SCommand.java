package fr.elogamerr.manager.commands;

import fr.elogamerr.manager.messages.MsgManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SCommand
{
	private CommandsExecutor commandsExecutor;
	private SubCommand[] subCommands;
	private SubCommand defaultSubCommand;
	private MsgManager msg;
	private String name;
	private boolean helpEnabled;
	private SubCommand helpSubCommand;

	public SCommand(CommandsExecutor commandsExecutor, String name, SubCommand[] subCommands, SubCommand defaultSubCommand, MsgManager msg, boolean helpEnabled)
	{
		this.commandsExecutor = commandsExecutor;
		this.name = name;
		this.subCommands = subCommands;
		this.defaultSubCommand = defaultSubCommand;
		this.msg = msg;
		this.helpEnabled = helpEnabled;

		if(this.helpEnabled)
		{
			this.helpSubCommand = new HelpCommand();
			this.helpSubCommand.setScommand(this);
		}
	}
	
	public void execute(CommandSender sender, Command cmd, String msg, String[] args)
	{
		if(args.length != 0)
		{
			for(SubCommand subCommand : this.subCommands)
			{
				for(String alias : subCommand.getAliases())
				{
					if(alias.equalsIgnoreCase(args[0]))
					{
						List<String> argsList = new ArrayList<>(Arrays.asList(args));
						argsList.remove(0);
						subCommand.execute(sender, cmd, msg, argsList, alias);
						return;
					}
				}
			}
			if(this.helpSubCommand != null)
			{
				for(String alias : this.helpSubCommand.getAliases())
				{
					if(alias.equalsIgnoreCase(args[0]))
					{
						List<String> argsList = new ArrayList<>(Arrays.asList(args));
						argsList.remove(0);
						this.helpSubCommand.execute(sender, cmd, msg, argsList, alias);
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
				for(String alias : subCommand.getAliases())
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
			if(!subCommand.getAliases().isEmpty())
				message.append(subCommand.getAliases().get(0)).append(",");
		}

		message = new StringBuilder(message.substring(0, message.length() - 1));

		message.append(this.defaultSubCommand != null ? "]" : ">");
		
		return message.toString();
	}

	public CommandsExecutor getCommandsExecutor() {
		return commandsExecutor;
	}

	public SubCommand[] getSubCommands() {
		return subCommands;
	}

	public SubCommand getDefaultSubCommand() {
		return defaultSubCommand;
	}

	public MsgManager getMsg() {
		return msg;
	}

	public String getName() {
		return name;
	}

	public boolean isHelpEnabled() {
		return helpEnabled;
	}

	public void setSubCommands(SubCommand[] subCommands) {
		this.subCommands = subCommands;
	}
}
