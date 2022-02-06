package fr.elogamerr.manager.commands;

class ReloadCommand extends SubCommand
{
    public ReloadCommand()
    {
        this.aliases.add("reload");
        this.aliases.add("rl");
        this.senderMustBeOp = true;
    }

    public void perform()
    {
        this.scommand.fileManager.reloadStaticFiles();
        this.info("La config a bien été rechargée.");
    }
}