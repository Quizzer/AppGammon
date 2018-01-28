package com.doneu.backgammoncounter.utils;


public class Constants {

    public static final String TAG = "bc";
    public static final int MAX_SCORE_LENGTH = 4;
    public static final int POINTS_AT_START = 1;
    public static final int MAXIMUM_POINTS = 999999;
    public static final int MULTIPLY_POINTS_WITH = 2;
    public static final String log_challenge_string = "Challenge[id-%s] vs. %s - %d : %d";
    public static final String log_game_string = "Game[id-%s] with %d points closed(%s) at %s";

    // Enum Increase
    public static final int NONE = 0;
    public static final int MY_TURN_TO_INCREASE = 1;
    public static final int OPP_TURN_TO_INCREASE = 2;
    public static final int MY_INCREASE_PENDING = 3;
    public static final int OPP_INCREASE_PENDING = 4;

}
