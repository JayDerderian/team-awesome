package teamawesome;

import org.apache.commons.io.filefilter.TrueFileFilter;

public interface FlagConstants {

    // Error
    static final int ERROR = 11404;
    static final int NONE = 0;

    // Password
    static final int PASSWORD = 11;

    // Alerts
    static final int NEUTRAL_ENLIGHTENMENT_CENTER_FLAG = 1;
    static final int NEED_HELP = 2;
    static final int GO_HERE = 3;
    static final int ENEMY_INFO = 4;
    static final int OUT_OF_RANGE = 5;
    static final int NEUTRAL = 6;
    static final int SEND_LOCATION = 7;
    static final int LOCATION_INFO = 8;


    // Detected Enemy Robot Flags
    static final int ENEMY_POLITICIAN_FLAG = 100;
    static final int ENEMY_SLANDERER_NEARBY_FLAG = 200;
    static final int ENEMY_MUCKRAKER_NEARBY_FLAG = 300;
    static final int ENEMY_ENLIGHTENMENT_CENTER_FLAG = 400;

}
