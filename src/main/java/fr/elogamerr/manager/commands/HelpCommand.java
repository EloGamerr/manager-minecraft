package fr.elogamerr.manager.commands;

class HelpCommand extends SubCommand
{
    @Override
    protected void init() {
        this.addAlias("help");
        this.setDisplayInHelp(false);
    }

    public void perform()
    {
        this.getMsgManager().help(this.getSender());
    }
}
