package com.bitwisehero.takmoport;

public class TakmoCommand {


    public enum ClickType {
        TELEPORTER,
        WAYPOINT,
        FOCUS,
        INFO
    }


    private String[] args;
    private ClickType type;


    public String[] getArguments() {
        return args;
    }


    public ClickType getType() {
        return type;
    }


    public TakmoCommand(ClickType t, String[] a) {
        type = t;
        args = a;
    }


}
