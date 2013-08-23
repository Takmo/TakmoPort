package com.bitwisehero.takmoport;

public class TakmoCommand {


    public enum Type {
        TELEPORTER,
        WAYPOINT,
        FOCUS,
        INFO
    }


    private String[] args;
    private Type type;


    public String[] getArguments() {
        return args;
    }


    public Type getType() {
        return type;
    }


    public TakmoCommand(Type t, String[] a) {
        type = t;
        args = a;
    }


}
