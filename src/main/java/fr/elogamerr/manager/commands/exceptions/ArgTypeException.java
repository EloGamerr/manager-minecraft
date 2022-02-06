package fr.elogamerr.manager.commands.exceptions;

public class ArgTypeException extends Exception {
    private int argNumber;
    private String arg;

    public ArgTypeException(int argNumber, String arg) {
        super();

        this.argNumber = argNumber;
        this.arg = arg;
    }

    public int getArgNumber() {
        return argNumber;
    }

    public String getArg() {
        return arg;
    }
}
