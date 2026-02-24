package com.dskroba.vpn.type;

public record SelectOption(String value, int priority, boolean isFullRow, boolean isHidden) {
    public static int DEFAULT_PRIORITY = 10;

    public static SelectOption SAVE_OPTION = SelectOption.fullRowOption("Save", 1);
    public static SelectOption CANCEL_OPTION = SelectOption.fullRowOption("Cancel", 0);

    public static SelectOption fullRowOption(String value, int priority) {
        return new SelectOption(value, priority, true, false);
    }

    public static SelectOption fullRowOption(String value) {
        return new SelectOption(value, DEFAULT_PRIORITY, true, false);
    }

    public static SelectOption base(String value, int priority) {
        return new SelectOption(value, priority, false, false);
    }

    public static SelectOption base(String value) {
        return base(value, DEFAULT_PRIORITY);
    }
}
