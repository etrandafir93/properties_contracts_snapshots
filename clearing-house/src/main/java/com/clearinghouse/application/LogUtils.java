package com.clearinghouse;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LogUtils {

    private final String ESC = String.valueOf((char) 27);

    public final String YELLOW = ESC + "[33m";
    public final String GREEN = ESC + "[32m";
    public final String BLUE = ESC + "[34m";
    public final String INDIGO = ESC + "[38;5;57m";
    public final String VIOLET = ESC + "[38;5;129m";
    public final String RESET = ESC + "[0m";
}
