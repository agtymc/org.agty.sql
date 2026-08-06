package org.agty.sql.support;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Internal debug printer used by current production code.
 */
public final class DebugMessages {

    private DebugMessages() {
    }

    public static void print(String type, String message) {
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        System.out.print(formatForDateNow.format(new Date()) + "|" + type + "| " + message + "\n");
    }
}
