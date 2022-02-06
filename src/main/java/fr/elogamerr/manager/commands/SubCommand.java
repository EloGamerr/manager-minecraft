package fr.elogamerr.manager.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.elogamerr.manager.commands.enums.OptionnalArgValue;
import fr.elogamerr.manager.commands.exceptions.ArgTypeException;
import fr.elogamerr.manager.messages.MsgManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

public abstract class SubCommand
{
	protected String commandName;
	protected SCommand scommand;
	protected String msg;
	protected List<String> args;
	protected String subCmdName;
	public Player player;
	public String playerName;
	public List<String> requiredArgs;
	public LinkedHashMap<String, String> optionalArgs;
	public String permission;
	public List<String> aliases;
	public String descHelp;
	public boolean setInHelp;
	public boolean senderIsPlayer;
	public boolean senderMustBePlayer;
	public boolean senderMustBeOp;
	public boolean senderMustBeConsole;
	public boolean errorOnTooManyArgs;
	public CommandSender sender;
	protected Command cmd;
	public boolean isDefaultCmd;

	public SubCommand()
	{
		this.senderMustBePlayer = false;
		this.senderMustBeOp = false;
		this.senderMustBeConsole = false;
		this.setInHelp = true;
		this.errorOnTooManyArgs = false;
		this.aliases = new ArrayList<>();
		this.optionalArgs = new LinkedHashMap<>();
		this.requiredArgs = new ArrayList<>();
		this.args = new ArrayList<>();
		this.permission = null;
		this.isDefaultCmd = false;
		this.subCmdName = null;
	}
	
	public void execute(CommandSender sender, Command cmd, String msg, List<String> args, String subCmdName)
	{
		this.initVariables(sender, cmd, msg, args, subCmdName);
		
		if(!this.checkCommand()) return;
		
		if(this.senderIsPlayer)
		{
			this.player = (Player) sender;
			this.playerName = this.player.getName();
		}

		this.setArgs();

		if(!this.initCustomVariables()) return;

		this.perform();
	}

	public boolean initCustomVariables() { return true; };

	public abstract void perform();

	public List<String> executeTabComplete(final CommandSender sender, final Command cmd, final String msg, final String[] args)
	{
		this.initVariables(sender);

		if(!this.checkTabComplete()) return new ArrayList<>();

		return this.onTabComplete(sender, cmd, msg, args);
	}

	public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String msg, final String[] args)
	{
		return null;
	}

	public void sendToBungee()
	{
		if(!this.scommand.commandsExecutor.isBungeeLinked)
		{
			Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this.scommand.commandsExecutor.plugin, "BungeeCord");
			this.scommand.commandsExecutor.isBungeeLinked = true;
		}

		ByteArrayDataOutput out = ByteStreams.newDataOutput();

		out.writeUTF(this.commandName);

		if(this.senderIsPlayer)
		{
			out.writeUTF(this.player.getName());
		}
		else
		{
			out.writeUTF("&CONSOLE&");
		}

		List<String> argsList = new ArrayList<>(args);

		if(this.subCmdName != null)
		argsList.add(0, this.subCmdName);

		out.writeUTF(""+argsList);

		if(this.senderIsPlayer)
		{
			this.player.sendPluginMessage(this.scommand.commandsExecutor.plugin, "BungeeCord", out.toByteArray());
		}
		else
		{
			if(!Bukkit.getOnlinePlayers().isEmpty())
			{
				Bukkit.getOnlinePlayers().toArray(new Player[] {})[0].sendPluginMessage(this.scommand.commandsExecutor.plugin, "BungeeCord", out.toByteArray());
			}
		}
	}

	public void initVariables(CommandSender sender, Command cmd, String msg, List<String> args, String subCmdName)
	{
		this.sender = sender;
		this.cmd = cmd;
		this.msg = msg;
		this.args = args;
		this.subCmdName = subCmdName;
		this.senderIsPlayer = (sender instanceof Player);
	}

	public void initVariables(CommandSender sender)
	{
		this.sender = sender;
		this.senderIsPlayer = (sender instanceof Player);
	}

	public boolean checkCommand()
	{
		return this.checkMustBe() && this.checkArgs() && this.checkPermissions();
	}

	public boolean checkTabComplete()
	{
		return this.checkMustBe() && this.checkPermissions();
	}

	public void setArgs()
	{
		if(this.args.size() < this.optionalArgs.size() + this.requiredArgs.size())
		{
			int miss = this.args.size() - this.requiredArgs.size();
			int i = 0;
			for(Entry<String, String> arg : this.optionalArgs.entrySet())
			{
				i++;
				if(i > miss)
				{
					this.args.add(this.getOptionnalArg(arg.getValue()));
				}
			}
		}
	}
	
	public String getOptionnalArg(String value)
	{
		if(value == null) return "";

		if(this.sender == null) return value;

		if(value.equalsIgnoreCase(OptionnalArgValue.YOU.getValue()))
		{
			return this.sender.getName();
		}
		
		return value;
	}
	
	public boolean checkPermissions()
	{
		if(this.permission != null && !this.sender.hasPermission(this.permission))
		{
			this.noPerm();
			return false;
		}
		
		return true;
	}
	
	public boolean checkArgs()
	{
		if(this.requiredArgs.size() > this.args.size()) { this.sendNotEnoughArgs(); this.sendUseageTemplate(); return false; }
		
		if(this.requiredArgs.size() + this.optionalArgs.size() < this.args.size() && this.errorOnTooManyArgs) { this.sendTooManyArgs(); this.sendUseageTemplate(); return false; }
		
		
		return true;
	}
	
	public void sendNotEnoughArgs()
	{
		this.err("Votre commande ne contient pas assez d'arguments :");
	}
	
	public void sendTooManyArgs()
	{
		this.err("Votre commande contient trop d'arguments :");
	}
	
	public void sendUseageTemplate()
	{
		this.sender.sendMessage((this.getUseageTemplate()));
	}
	
	public boolean checkMustBe()
	{
		if(this.senderMustBePlayer && !this.senderIsPlayer) { this.sendMustBePlayer(); return false; }
		
		if(this.senderMustBeConsole && this.senderIsPlayer) { this.sendMustBeConsole(); return false; }
		
		if(this.senderMustBeOp && !this.sender.isOp()) { this.sendMustBeOp(); return false; }
		
		return true;
	}
	
	public void sendMustBePlayer()
	{
		this.err("Vous devez être un joueur pour effectuer cette commande !");
	}
	
	public void sendMustBeConsole()
	{
		this.err("Les joueurs ne peuvent pas effectuer cette commande !");
	}
	
	public void sendMustBeOp()
	{
		this.noPerm();
	}
	
	public void info(String msg)
	{
		this.sender.sendMessage(this.scommand.msg.info(msg));
	}
	
	public void err(String msg)
	{
		this.sender.sendMessage(this.scommand.msg.err(msg));
	}
	
	public void noPerm()
	{
		this.sender.sendMessage(this.scommand.msg.noPermCmd());
	}
	
	public String getUseageTemplate()
	{
		StringBuilder message = new StringBuilder(ChatColor.GRAY + "/" + this.scommand.name + " ");

		for(String str : this.aliases)
		{
			message.append(str).append(",");
		}

		message = new StringBuilder(message.substring(0, message.length() - 1));

		for(String str : this.requiredArgs)
		{
			message.append(" <").append(str).append(">");
		}

		for(Entry<String, String> s : this.optionalArgs.entrySet())
		{
			String value = s.getValue();
			if(value == null)
			{
				value = "";
			}
			else
			{
				value = "="+this.getOptionnalArg(value);
			}

			message.append(" [").append(s.getKey()).append(value).append("]");
		}

		return message.toString();
	}

    public String argAsString(int index)
    {
        if(this.args.size() < index + 1)
        {
			this.err("Il y a eu une erreur lors de l'exécution de la commande.");
			throw new NoSuchElementException("Impossible de récupérer l'argument à l'index " + index);
        }

        return this.args.get(index);
    }

	public Player argAsPlayer(int index, boolean notify) throws ArgTypeException
    {
        String arg = this.argAsString(index);

        Player player = Bukkit.getPlayer(arg);

        if(player == null)
        {
        	if(notify)
           		this.err("Le joueur $!$"+arg+"$e$ n'a pas été trouvé.");

			throw new ArgTypeException(index, arg);
        }

        return player;
    }

	public int argAsInt(int index, boolean notify, int min, int max) throws ArgTypeException
	{
		String arg = this.argAsString(index);

		try {
			int number = Integer.parseInt(arg);

			if(number >= min && number <= max) {
				return number;
			}
			else {
				if(notify)
					this.err("L'argument $!$"+arg+"$e$doit être un entier compris entre <!>"+min+" <err>et <!>"+max);

				throw new ArgTypeException(index, arg);
			}
		}
		catch(NumberFormatException ex) {
			if(notify)
				this.err("L'argument $!$"+arg+"$e$doit être un entier compris entre <!>"+min+" <err>et <!>"+max);

			throw new ArgTypeException(index, arg);
		}
	}

	public double argAsDouble(int index, boolean notify, double min, double max) throws ArgTypeException
	{
		String arg = this.argAsString(index);

		try {
			double number = Double.parseDouble(arg);

			if(number >= min && number <= max) {
				return number;
			}
			else {
				if(notify)
					this.err("L'argument $!$"+arg+"$e$doit être un nombre compris entre <!>"+min+" <err>et <!>"+max);

				throw new ArgTypeException(index, arg);
			}
		}
		catch(NumberFormatException ex) {
			if(notify)
				this.err("L'argument $!$"+arg+"$e$doit être un nombre compris entre <!>"+min+" <err>et <!>"+max);

			throw new ArgTypeException(index, arg);
		}
	}

	public boolean argAsBool(int index, boolean notify) throws ArgTypeException
	{
		String arg = this.argAsString(index);

		Boolean bool = null;

		if(arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("t") || arg.equalsIgnoreCase("vrai") || arg.equalsIgnoreCase("v") || arg.equalsIgnoreCase("oui") || arg.equalsIgnoreCase("yes") || arg.equalsIgnoreCase("y") || arg.equalsIgnoreCase("+")) {
			bool = true;
		}

		if(arg.equalsIgnoreCase("false") || arg.equalsIgnoreCase("f") || arg.equalsIgnoreCase("faux") || arg.equalsIgnoreCase("non") || arg.equalsIgnoreCase("no") || arg.equalsIgnoreCase("n") || arg.equalsIgnoreCase("-")) {
			bool = false;
		}

		if(bool == null) {
			if(notify)
				this.err("L'argument $!$"+arg+"$e$doit être un booléen (\"vrai\" ou \"faux\")");

			throw new ArgTypeException(index, arg);
		}

		return bool;
	}

	public MsgManager getMsgManager()
	{
		return this.scommand.msg;
	}
}
