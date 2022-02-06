package fr.elogamerr.manager.commands;

import fr.elogamerr.manager.files.FileManager;
import fr.elogamerr.manager.messages.MsgManager;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CommandsManager
{
	public List<SCommand> commands = new ArrayList<SCommand>();

	public CommandsManager(JavaPlugin plugin, String commandsPackage, MsgManager msg, boolean helpEnabled)
	{
		this(plugin, commandsPackage, msg, helpEnabled, null);
	}

	//Package of commands : plugin/commands/[command]/[subcommand]
	public CommandsManager(JavaPlugin plugin, String commandsPackage, MsgManager msg, boolean helpEnabled, FileManager fileManager)
	{
		try
		{
			CommandsExecutor commandsExecutor = new CommandsExecutor(plugin, this);

			ArrayList<String> packageFiles = getPackageContentJar(plugin, commandsPackage);
			HashMap<String, List<SubCommand>> subCommands = new HashMap<>();

			for(String packageFile : packageFiles)
			{
				if(packageFile != null && packageFile.endsWith(".class"))
				{
					Class<?> commandClass = Class.forName(packageFile.replaceAll("\\.class", "").replaceAll("/", "."), true, plugin.getClass().getClassLoader());

					Class<?> superClass = commandClass.getSuperclass();
					while(superClass != null && !Object.class.equals(superClass.getSuperclass())) {
						superClass = superClass.getSuperclass();
					}

					if(!Modifier.isAbstract(commandClass.getModifiers()) && SubCommand.class.equals(superClass))
					{
						Object object = commandClass.newInstance();
						if(object instanceof SubCommand)
						{
							SubCommand subCommand = (SubCommand)object;

							if(subCommand.commandName == null)
							{
								String[] splitPackage = packageFile.split("/");
								if(splitPackage.length >= 2)
								{
									subCommand.commandName = splitPackage[splitPackage.length-2];
								}
							}

							if(subCommand.commandName != null)
							{
								List<SubCommand> subCmd = subCommands.get(subCommand.commandName);
								if(subCmd == null)
								{
									subCmd = new ArrayList<>();
								}
								subCmd.add(subCommand);
								subCommands.put(subCommand.commandName, subCmd);
							}
						}
					}
				}
			}

			List<Command> commands = PluginCommandYamlParser.parse(plugin);
			List<SCommand> scommands = new ArrayList<SCommand>();
			Iterator<SCommand> iterator = this.commands.iterator();
			for(Command command : commands)
			{
				while (iterator.hasNext())
				{
					SCommand scommand = iterator.next();

					if (scommand.name.equalsIgnoreCase(command.getName()))
					{
						iterator.remove(); //We can't have two identical commands
					}
				}

				List<SubCommand> subCmds = subCommands.get(command.getName());
				if(subCmds != null && !subCmds.isEmpty())
				{
					SubCommand defaultSubCommand = null;
					for(SubCommand subCmd : subCmds)
					{
						if(subCmd.isDefaultCmd)
						{
							defaultSubCommand = subCmd;
						}
					}

					SCommand scmd = new SCommand(commandsExecutor, command.getName(), subCmds.toArray(new SubCommand[]{}), defaultSubCommand, msg, helpEnabled, fileManager);
					scommands.add(scmd);

					LinkedHashMap<String, String> helpNameAndDesc = new LinkedHashMap<String, String>();
					plugin.getCommand(scmd.name).setExecutor(commandsExecutor);

					if (scmd.subCommands == null) scmd.subCommands = new SubCommand[]{};

					for (SubCommand subCommand : scmd.subCommands) {
						subCommand.scommand = scmd;
						if (scmd.helpEnabled) {
							if (!subCommand.aliases.isEmpty() && subCommand.setInHelp && (scmd.defaultSubCommand == null || !scmd.defaultSubCommand.equals(subCommand))) {
								String cmd = subCommand.getUseageTemplate();

								if (subCommand.descHelp != null) {
									helpNameAndDesc.put(cmd, subCommand.descHelp + (subCommand.senderMustBeOp ? ";op" : (subCommand.permission == null ? "" : ";" + subCommand.permission)));
								} else {
									helpNameAndDesc.put(cmd, "Aucune description" + (subCommand.senderMustBeOp ? ";op" : (subCommand.permission == null ? "" : ";" + subCommand.permission)));
								}
							}
						}
					}

					if (scmd.defaultSubCommand != null) {
						scmd.defaultSubCommand.scommand = scmd;

						if (scmd.helpEnabled) {
							SubCommand subCommand = scmd.defaultSubCommand;
							if (subCommand.setInHelp) {
								String cmd = subCommand.getUseageTemplate();
								if (subCommand.descHelp != null) {
									helpNameAndDesc.put(cmd, subCommand.descHelp + (subCommand.senderMustBeOp ? ";op" : (subCommand.permission == null ? "" : ";" + subCommand.permission)));
								} else {
									helpNameAndDesc.put(cmd, "Aucune description" + (subCommand.senderMustBeOp ? ";op" : (subCommand.permission == null ? "" : ";" + subCommand.permission)));
								}
							}
						}
					}
					if (scmd.helpEnabled) {
						scmd.msg.putAllHelp(helpNameAndDesc);
					}
				}
			}

			this.commands.addAll(scommands);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public static ArrayList<String> getPackageContentJar(JavaPlugin plugin, String commandsPackage) throws IOException
	{
		ArrayList<String> content = new ArrayList<String>();
		String commandPackagePath = commandsPackage;
		commandPackagePath = commandPackagePath.replaceAll("\\.", "/").replaceAll("\\\\", "/");
		CodeSource src = plugin.getClass().getProtectionDomain().getCodeSource();
		if (src != null) {
			URL jar = src.getLocation();
			ZipInputStream zip = new ZipInputStream(jar.openStream());
			while(true) {
				ZipEntry e = zip.getNextEntry();
				if (e == null)
					break;
				String name = e.getName();
				if (name.startsWith(commandPackagePath)) {
					content.add(name);
				}
			}
		}
		return content;
	}
}
