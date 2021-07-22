package org.starcoin.stcpricereporter.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeUtils {

    public static ZonedDateTime toDefaultZonedDateTime(Long dateInMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateInMillis),
                ZoneId.systemDefault());
    }


}
