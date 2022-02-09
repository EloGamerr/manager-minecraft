package fr.elogamerr.manager.commands;

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
	private List<SCommand> commands = new ArrayList<>();

	/**
	 * Package of commands : plugin/commands/[command]/[subcommand]
	 */
	public CommandsManager(JavaPlugin plugin, String commandsPackage, MsgManager msg, boolean helpEnabled)
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

							if(subCommand.getCommandName() == null)
							{
								String[] splitPackage = packageFile.split("/");
								if(splitPackage.length >= 2)
								{
									subCommand.setCommandName(splitPackage[splitPackage.length-2]);;
								}
							}

							if(subCommand.getCommandName() != null)
							{
								List<SubCommand> subCmd = subCommands.get(subCommand.getCommandName());
								if(subCmd == null)
								{
									subCmd = new ArrayList<>();
								}
								subCmd.add(subCommand);
								subCommands.put(subCommand.getCommandName(), subCmd);
							}
						}
					}
				}
			}

			List<Command> commands = PluginCommandYamlParser.parse(plugin);
			List<SCommand> scommands = new ArrayList<>();
			Iterator<SCommand> iterator = this.commands.iterator();
			for(Command command : commands)
			{
				while (iterator.hasNext())
				{
					SCommand scommand = iterator.next();

					if (scommand.getName().equalsIgnoreCase(command.getName()))
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
						if(subCmd.isDefaultCmd())
						{
							defaultSubCommand = subCmd;
						}
					}

					SCommand scmd = new SCommand(commandsExecutor, command.getName(), subCmds.toArray(new SubCommand[]{}), defaultSubCommand, msg, helpEnabled);
					scommands.add(scmd);

					LinkedHashMap<String, String> helpNameAndDesc = new LinkedHashMap<String, String>();
					plugin.getCommand(scmd.getName()).setExecutor(commandsExecutor);

					if (scmd.getSubCommands() == null) scmd.setSubCommands(new SubCommand[]{});

					for (SubCommand subCommand : scmd.getSubCommands()) {
						subCommand.setScommand(scmd);
						if (scmd.isHelpEnabled()) {
							if (!subCommand.getAliases().isEmpty() && subCommand.isDisplayInHelp() && (scmd.getDefaultSubCommand() == null || !scmd.getDefaultSubCommand().equals(subCommand))) {
								String cmd = subCommand.getUseageTemplate();

								if (subCommand.getHelpDescription() != null) {
									helpNameAndDesc.put(cmd, subCommand.getHelpDescription() + (subCommand.isSenderMustBeOp() ? ";op" : (subCommand.getPermission() == null ? "" : ";" + subCommand.getPermission())));
								} else {
									helpNameAndDesc.put(cmd, "Aucune description" + (subCommand.isSenderMustBeOp() ? ";op" : (subCommand.getPermission() == null ? "" : ";" + subCommand.getPermission())));
								}
							}
						}
					}

					if (scmd.getDefaultSubCommand() != null) {
						scmd.getDefaultSubCommand().setScommand(scmd);

						if (scmd.isHelpEnabled()) {
							SubCommand subCommand = scmd.getDefaultSubCommand();
							if (subCommand.isDisplayInHelp()) {
								String cmd = subCommand.getUseageTemplate();
								if (subCommand.getHelpDescription() != null) {
									helpNameAndDesc.put(cmd, subCommand.getHelpDescription() + (subCommand.isSenderMustBeOp() ? ";op" : (subCommand.getPermission() == null ? "" : ";" + subCommand.getPermission())));
								} else {
									helpNameAndDesc.put(cmd, "Aucune description" + (subCommand.isSenderMustBeOp() ? ";op" : (subCommand.getPermission() == null ? "" : ";" + subCommand.getPermission())));
								}
							}
						}
					}
					if (scmd.isHelpEnabled()) {
						scmd.getMsg().putAllHelp(helpNameAndDesc);
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

	public ArrayList<String> getPackageContentJar(JavaPlugin plugin, String commandsPackage) throws IOException
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

	public List<SCommand> getCommands() {
		return commands;
	}
}
