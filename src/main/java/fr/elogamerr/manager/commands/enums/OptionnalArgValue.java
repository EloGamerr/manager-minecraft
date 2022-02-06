package fr.elogamerr.manager.commands.enums;

public enum OptionnalArgValue
{
    YOU("vous");

    private String value;

    OptionnalArgValue(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return this.value;
    }
}
