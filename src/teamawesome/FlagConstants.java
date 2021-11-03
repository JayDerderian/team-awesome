package teamawesome;

public interface FlagConstants {

    // Error
    static final int ERROR = 0;

    // Password
    static final int PASSWORD = 11;

    // Alerts
    static final int ALERT = 0; // used to determine which type of flag to make
    static final int NEUTRAL_ENLIGHTENMENT_CENTER_FLAG = 1;
    static final int NEED_HELP = 2;

    // Detected Enemy Robot Flags
    static final int ENEMY_INFO = 0;  // used to determine which type of flag to make
    static final int ENEMY_POLITICIAN_FLAG = 100;
    static final int ENEMY_SLANDERER_NEARBY_FLAG = 200;
    static final int ENEMY_MUCKRAKER_NEARBY_FLAG = 300;
    static final int ENEMY_ENLIGHTENMENT_CENTER_FLAG = 400;

    // Direction Ints
    static final int LOCATION = 9;
    static final int NORTH_EAST = 1;
    static final int NORTH_WEST = 2;
    static final int SOUTH_WEST = 3;
    static final int SOUTH_EAST = 4;
}
