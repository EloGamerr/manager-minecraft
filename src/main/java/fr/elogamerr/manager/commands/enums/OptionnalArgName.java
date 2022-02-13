package fr.elogamerr.manager.commands.enums;

public enum OptionnalArgName
{
    PLAYER("player");

    private String argName;

    OptionnalArgName(String argName)
    {
        this.argName = argName;
    }

    public String getArgName()
    {
        return this.argName;
    }
}
