package fr.elogamerr.manager.commands;

class HelpCommand extends SubCommand
{
    public HelpCommand()
    {
        this.aliases.add("help");
        this.setInHelp = false;
    }

    public void perform()
    {
        this.scommand.msg.help(this.sender);
    }
}
