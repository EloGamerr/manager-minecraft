package fr.elogamerr.manager.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.elogamerr.manager.commands.enums.OptionnalArgName;
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
	private String commandName;
	private SCommand scommand;
	private String msg;
	private List<String> args;
	private String subCmdName;
	private Player player;
	private String playerName;
	private List<String> requiredArgs;
	private LinkedHashMap<String, String> optionalArgs;
	private String permission;
	private List<String> aliases;
	private String helpDescription;
	private boolean displayInHelp;
	private boolean senderIsPlayer;
	private boolean senderMustBePlayer;
	private boolean senderMustBeOp;
	private boolean senderMustBeConsole;
	private boolean errorOnTooManyArgs;
	private CommandSender sender;
	private Command cmd;
	private boolean isDefaultCmd;

	public SubCommand()
	{
		this.senderMustBePlayer = false;
		this.senderMustBeOp = false;
		this.senderMustBeConsole = false;
		this.displayInHelp = true;
		this.errorOnTooManyArgs = false;
		this.aliases = new ArrayList<>();
		this.optionalArgs = new LinkedHashMap<>();
		this.requiredArgs = new ArrayList<>();
		this.args = new ArrayList<>();
		this.permission = null;
		this.isDefaultCmd = false;
		this.subCmdName = null;
		this.commandName = null;

		this.init();
	}

	// Accessible for children

	protected void setCommandName(String commandName) {
		this.commandName = commandName;
	}

	protected void addRequiredArg(String requiredArg) {
		this.requiredArgs.add(requiredArg);
	}

	protected void putOptionalArg(String argName, String defaultArg) {
		this.optionalArgs.put(argName, defaultArg);
	}

	protected void setPermission(String permission) {
		this.permission = permission;
	}

	protected void addAlias(String alias) {
		this.aliases.add(alias);
	}

	protected void setHelpDescription(String helpDescription) {
		this.helpDescription = helpDescription;
	}

	protected void setDisplayInHelp(boolean displayInHelp) {
		this.displayInHelp = displayInHelp;
	}

	protected void setSenderMustBePlayer(boolean senderMustBePlayer) {
		this.senderMustBePlayer = senderMustBePlayer;
	}

	protected void setSenderMustBeOp(boolean senderMustBeOp) {
		this.senderMustBeOp = senderMustBeOp;
	}

	protected void setSenderMustBeConsole(boolean senderMustBeConsole) {
		this.senderMustBeConsole = senderMustBeConsole;
	}

	protected void setErrorOnTooManyArgs(boolean errorOnTooManyArgs) {
		this.errorOnTooManyArgs = errorOnTooManyArgs;
	}

	protected void setDefaultCmd(boolean defaultCmd) {
		isDefaultCmd = defaultCmd;
	}

	protected abstract void init();

	protected abstract void perform();

	protected String getDefaultOptionalArg(String argName) {
		return null;
	}

	protected void sendToBungee()
	{
		if(!this.scommand.getCommandsExecutor().isBungeeLinked)
		{
			Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this.scommand.getCommandsExecutor().plugin, "BungeeCord");
			this.scommand.getCommandsExecutor().isBungeeLinked = true;
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
			this.player.sendPluginMessage(this.scommand.getCommandsExecutor().plugin, "BungeeCord", out.toByteArray());
		}
		else
		{
			if(!Bukkit.getOnlinePlayers().isEmpty())
			{
				Bukkit.getOnlinePlayers().toArray(new Player[] {})[0].sendPluginMessage(this.scommand.getCommandsExecutor().plugin, "BungeeCord", out.toByteArray());
			}
		}
	}

	protected String argAsString(int index)
	{
		if(this.args.size() < index + 1)
		{
			this.err("Il y a eu une erreur lors de l'exécution de la commande.");
			throw new NoSuchElementException("Impossible de récupérer l'argument à l'index " + index);
		}

		return this.args.get(index);
	}

	protected Player argAsPlayer(int index, boolean notify) throws ArgTypeException
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

	protected int argAsInt(int index, boolean notify) throws ArgTypeException {
		return this.argAsInt(index, notify, Integer.MIN_VALUE, false);
	}

	protected int argAsInt(int index, boolean notify, int min) throws ArgTypeException {
		return this.argAsInt(index, notify, min, true);
	}

	protected int argAsInt(int index, boolean notify, int min, boolean hasMin) throws ArgTypeException {
		return this.argAsInt(index, notify, min, Integer.MAX_VALUE, hasMin, false);
	}

	protected int argAsInt(int index, boolean notify, int min, int max) throws ArgTypeException {
		return this.argAsNumber(index, notify, Integer.class, min, max, true, true);
	}

	protected int argAsInt(int index, boolean notify, int min, int max, boolean hasMin, boolean hasMax) throws ArgTypeException {
		return this.argAsNumber(index, notify, Integer.class, min, max, hasMin, hasMax);
	}

	protected double argAsDouble(int index, boolean notify) throws ArgTypeException {
		return this.argAsDouble(index, notify, Double.NEGATIVE_INFINITY, false);
	}

	protected double argAsDouble(int index, boolean notify, double min) throws ArgTypeException {
		return this.argAsDouble(index, notify, min, true);
	}

	protected double argAsDouble(int index, boolean notify, double min, boolean hasMin) throws ArgTypeException {
		return this.argAsDouble(index, notify, min, Double.POSITIVE_INFINITY, hasMin, false);
	}

	protected double argAsDouble(int index, boolean notify, double min, double max) throws ArgTypeException {
		return this.argAsNumber(index, notify, Double.class, min, max, true, true);
	}

	protected double argAsDouble(int index, boolean notify, double min, double max, boolean hasMin, boolean hasMax) throws ArgTypeException {
		return this.argAsNumber(index, notify, Double.class, min, max, hasMin, hasMax);
	}

	protected byte argAsByte(int index, boolean notify) throws ArgTypeException {
		return this.argAsByte(index, notify, Byte.MIN_VALUE, false);
	}

	protected byte argAsByte(int index, boolean notify, byte min) throws ArgTypeException {
		return this.argAsByte(index, notify, min, true);
	}

	protected byte argAsByte(int index, boolean notify, byte min, boolean hasMin) throws ArgTypeException {
		return this.argAsByte(index, notify, min, Byte.MAX_VALUE, hasMin, false);
	}

	protected byte argAsByte(int index, boolean notify, byte min, byte max) throws ArgTypeException {
		return this.argAsNumber(index, notify, Byte.class, min, max, true, true);
	}

	protected byte argAsByte(int index, boolean notify, byte min, byte max, boolean hasMin, boolean hasMax) throws ArgTypeException {
		return this.argAsNumber(index, notify, Byte.class, min, max, hasMin, hasMax);
	}

	protected float argAsFloat(int index, boolean notify) throws ArgTypeException {
		return this.argAsFloat(index, notify, Float.NEGATIVE_INFINITY, false);
	}

	protected float argAsFloat(int index, boolean notify, float min) throws ArgTypeException {
		return this.argAsFloat(index, notify, min, true);
	}

	protected float argAsFloat(int index, boolean notify, float min, boolean hasMin) throws ArgTypeException {
		return this.argAsFloat(index, notify, min, Float.POSITIVE_INFINITY, hasMin, false);
	}

	protected float argAsFloat(int index, boolean notify, float min, float max) throws ArgTypeException {
		return this.argAsNumber(index, notify, Float.class, min, max, true, true);
	}

	protected float argAsFloat(int index, boolean notify, float min, float max, boolean hasMin, boolean hasMax) throws ArgTypeException {
		return this.argAsNumber(index, notify, Float.class, min, max, hasMin, hasMax);
	}

	protected short argAsShort(int index, boolean notify) throws ArgTypeException {
		return this.argAsShort(index, notify, Short.MIN_VALUE, false);
	}

	protected short argAsShort(int index, boolean notify, short min) throws ArgTypeException {
		return this.argAsShort(index, notify, min, true);
	}

	protected short argAsShort(int index, boolean notify, short min, boolean hasMin) throws ArgTypeException {
		return this.argAsShort(index, notify, min, Short.MAX_VALUE, hasMin, false);
	}

	protected short argAsShort(int index, boolean notify, short min, short max) throws ArgTypeException {
		return this.argAsNumber(index, notify, Short.class, min, max, true, true);
	}

	protected short argAsShort(int index, boolean notify, short min, short max, boolean hasMin, boolean hasMax) throws ArgTypeException {
		return this.argAsNumber(index, notify, Short.class, min, max, hasMin, hasMax);
	}

	protected long argAsLong(int index, boolean notify) throws ArgTypeException {
		return this.argAsLong(index, notify, Long.MIN_VALUE, false);
	}

	protected long argAsLong(int index, boolean notify, long min) throws ArgTypeException {
		return this.argAsLong(index, notify, min, true);
	}

	protected long argAsLong(int index, boolean notify, long min, boolean hasMin) throws ArgTypeException {
		return this.argAsLong(index, notify, min, Long.MAX_VALUE, hasMin, false);
	}

	protected long argAsLong(int index, boolean notify, long min, long max) throws ArgTypeException {
		return this.argAsNumber(index, notify, Long.class, min, max, true, true);
	}

	protected long argAsLong(int index, boolean notify, long min, long max, boolean hasMin, boolean hasMax) throws ArgTypeException {
		return this.argAsNumber(index, notify, Long.class, min, max, hasMin, hasMax);
	}

	private <T extends Number> T argAsNumber(int index, boolean notify, Class<T> clazz, T min, T max, boolean hasMin, boolean hasMax) throws ArgTypeException {
		String arg = this.argAsString(index);

		Comparable<T> value;

		if (clazz == Byte.class) {
			value = (Comparable<T>) Byte.valueOf(arg);
		} else if (clazz == Double.class) {
			value = (Comparable<T>) Double.valueOf(arg);
		} else if (clazz == Float.class) {
			value = (Comparable<T>) Float.valueOf(arg);
		} else if (clazz == Integer.class) {
			value = (Comparable<T>) Integer.valueOf(arg);
		} else if (clazz == Long.class) {
			value = (Comparable<T>) Long.valueOf(arg);
		} else if (clazz == Short.class) {
			value = (Comparable<T>) Short.valueOf(arg);
		} else {
			throw new RuntimeException();
		}

		if(value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
			return (T) value;
		}
		else {
			if(notify) {
				if (hasMin && hasMin) {
					this.err("L'argument $!$"+arg+"$e$doit être un nombre compris entre <!>"+min+" <err>et <!>"+max);
				} else if (hasMin) {
					this.err("L'argument $!$"+arg+"$e$doit être un nombre supérieur ou égal à <!>"+min);
				} else if (hasMax) {
					this.err("L'argument $!$"+arg+"$e$doit être un nombre inférieur ou égal à <!>"+max);
				} else {
					this.err("L'argument $!$"+arg+"$e$doit être un nombre");
				}
			}

			throw new ArgTypeException(index, arg);
		}
	}

	protected boolean argAsBool(int index, boolean notify) throws ArgTypeException
	{
		String arg = this.argAsString(index);

		Boolean bool = null;

		if(arg.equalsIgnoreCase("true") || arg.equalsIgnoreCase("t") || arg.equalsIgnoreCase("vrai") || arg.equalsIgnoreCase("v") || arg.equalsIgnoreCase("oui") || arg.equalsIgnoreCase("yes") || arg.equalsIgnoreCase("y") || arg.equalsIgnoreCase("+")) {
			bool = true;
		} else if(arg.equalsIgnoreCase("false") || arg.equalsIgnoreCase("f") || arg.equalsIgnoreCase("faux") || arg.equalsIgnoreCase("non") || arg.equalsIgnoreCase("no") || arg.equalsIgnoreCase("n") || arg.equalsIgnoreCase("-")) {
			bool = false;
		}

		if(bool == null) {
			if(notify)
				this.err("L'argument $!$"+arg+"$e$doit être un booléen (\"vrai\" ou \"faux\")");

			throw new ArgTypeException(index, arg);
		}

		return bool;
	}

	protected MsgManager getMsgManager()
	{
		return this.scommand.getMsg();
	}

	protected void info(String msg)
	{
		this.sender.sendMessage(this.getMsgManager().info(msg));
	}

	protected void err(String msg)
	{
		this.sender.sendMessage(this.getMsgManager().err(msg));
	}

	protected void noPerm()
	{
		this.sender.sendMessage(this.getMsgManager().noPermCmd());
	}

	protected List<String> onTabComplete(final CommandSender sender, final Command cmd, final String msg, final String[] args)
	{
		return null;
	}

	protected Player getPlayer() {
		return player;
	}

	protected String getPlayerName() {
		return playerName;
	}

	protected boolean isSenderIsPlayer() {
		return senderIsPlayer;
	}

	protected CommandSender getSender() {
		return sender;
	}

	protected boolean initCustomVariables() {
		return true;
	}

	//Accessible only for package

	String getCommandName() {
		return commandName;
	}

	boolean isDefaultCmd() {
		return isDefaultCmd;
	}

	void setScommand(SCommand scommand) {
		this.scommand = scommand;
	}

	List<String> getAliases() {
		return aliases;
	}

	boolean isDisplayInHelp() {
		return displayInHelp;
	}

	String getHelpDescription() {
		return helpDescription;
	}

	boolean isSenderMustBeOp() {
		return senderMustBeOp;
	}

	String getPermission() {
		return permission;
	}

	String getUseageTemplate()
	{
		StringBuilder message = new StringBuilder(ChatColor.GRAY + "/" + this.scommand.getName() + " ");

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
				value = "="+this.getOptionnalArg(s.getKey(), value);
			}

			message.append(" [").append(s.getKey()).append(value).append("]");
		}

		return message.toString();
	}

	void execute(CommandSender sender, Command cmd, String msg, List<String> args, String subCmdName)
	{
		this.initVariables(sender, cmd, msg, args, subCmdName);

		if(!this.checkCommand()) return;

		if(this.senderIsPlayer)
		{
			this.player = (Player) sender;
			this.playerName = this.player.getName();
		}

		if(!this.initCustomVariables()) return;

		this.setArgs();

		this.perform();
	}

	int getRequiredArgsAmount() {
		return requiredArgs.size();
	}

	// Private

	/*private List<String> executeTabComplete(final CommandSender sender, final Command cmd, final String msg, final String[] args)
	{
		this.initVariables(sender);

		if(!this.checkTabComplete()) return new ArrayList<>();

		return this.onTabComplete(sender, cmd, msg, args);
	}*/

	private void initVariables(CommandSender sender, Command cmd, String msg, List<String> args, String subCmdName)
	{
		this.sender = sender;
		this.cmd = cmd;
		this.msg = msg;
		this.args = args;
		this.subCmdName = subCmdName;
		this.senderIsPlayer = (sender instanceof Player);
	}

	/*private void initVariables(CommandSender sender)
	{
		this.sender = sender;
		this.senderIsPlayer = (sender instanceof Player);
	}*/

	private boolean checkCommand()
	{
		return this.checkMustBe() && this.checkArgs() && this.checkPermissions();
	}

	/*private boolean checkTabComplete()
	{
		return this.checkMustBe() && this.checkPermissions();
	}*/

	private void setArgs()
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
					this.args.add(this.getOptionnalArg(arg.getKey(), null));
				}
			}
		}
	}

	private String getOptionnalArg(String argName, String value)
	{
		String defaultArg = getDefaultOptionalArg(argName);

		if (defaultArg != null) {
			return defaultArg;
		}

		if(this.sender != null && argName.equalsIgnoreCase(OptionnalArgName.PLAYER.getArgName()))
		{
			return this.sender.getName();
		}

		if(value == null)
			value = "";

		return value;
	}

	private boolean checkPermissions()
	{
		if(this.permission != null && !this.sender.hasPermission(this.permission))
		{
			this.noPerm();
			return false;
		}
		
		return true;
	}

	private boolean checkArgs()
	{
		if(this.requiredArgs.size() > this.args.size()) { this.sendNotEnoughArgs(); this.sendUseageTemplate(); return false; }
		
		if(this.requiredArgs.size() + this.optionalArgs.size() < this.args.size() && this.errorOnTooManyArgs) { this.sendTooManyArgs(); this.sendUseageTemplate(); return false; }
		
		
		return true;
	}

	private void sendNotEnoughArgs()
	{
		this.err("Votre commande ne contient pas assez d'arguments :");
	}

	private void sendTooManyArgs()
	{
		this.err("Votre commande contient trop d'arguments :");
	}

	private void sendUseageTemplate()
	{
		this.sender.sendMessage((this.getUseageTemplate()));
	}

	private boolean checkMustBe()
	{
		if(this.senderMustBePlayer && !this.senderIsPlayer) { this.sendMustBePlayer(); return false; }
		
		if(this.senderMustBeConsole && this.senderIsPlayer) { this.sendMustBeConsole(); return false; }
		
		if(this.senderMustBeOp && !this.sender.isOp()) { this.sendMustBeOp(); return false; }
		
		return true;
	}

	private void sendMustBePlayer()
	{
		this.err("Vous devez être un joueur pour effectuer cette commande !");
	}

	private void sendMustBeConsole()
	{
		this.err("Les joueurs ne peuvent pas effectuer cette commande !");
	}

	private void sendMustBeOp()
	{
		this.noPerm();
	}
}
