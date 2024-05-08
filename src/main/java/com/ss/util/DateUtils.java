package com.ss.util;

import lombok.extern.log4j.Log4j2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

import static com.ss.enums.Const.TIME_ZONE;

@Log4j2
public final class DateUtils {

    public static Long stringToTimestamp(String format, String value) {
        if (value == null)
            return null;
        DateFormat df = new SimpleDateFormat(format);
        try {
            Date date = df.parse(value);
            return date.getTime();
        } catch (Exception ex) {
            log.error("**********can not parse datetime value {}", value);
            return null;
        }
    }

    public static Long stringDateToLong(String date) {
        if (date == null)
            return null;
        try {
            return Long.valueOf(date);
        } catch (Exception ex) {
            log.error("**********can not parse datetime value {}", date);
            return null;
        }
    }

    public static String timestampToString(String format, Long timestamp) {
        if (timestamp == null)
            return "";
        DateFormat df = new SimpleDateFormat(format);
        try {
            Date date = new Date(timestamp);
            return df.format(date);
        } catch (Exception ex) {
            log.error("**********can not parse datetime value {}", timestamp);
            return "";
        }
    }

    public static String instantToString(String format, Instant instant) {
        if (instant == null)
            return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        try {
            return formatter.format(instant.atOffset(ZoneOffset.UTC));
        } catch (Exception ex) {
            log.error("**********can not parse datetime value {}", instant);
            return "";
        }
    }

    public static Long getFilterDate(Long timestamp, Boolean isStart) {
        if (timestamp == null)
            return null;

        try {

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);
            if (isStart) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 1);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
            }
            return cal.getTimeInMillis();
        } catch (Exception ex) {
            log.error("**********can not parse datetime value {}", timestamp);
            return null;
        }
    }

    public static Long getStartEndTimeOfDate(Long date, boolean isStart) {
        LocalDate localDate = Instant.ofEpochMilli(date).atZone(ZoneId.of(TIME_ZONE)).toLocalDate();
        LocalDateTime localDateTime = localDate.atStartOfDay();
        if (!isStart) {
            localDate = localDate.plusDays(1);
            localDateTime = localDate.atStartOfDay();
            ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.of(TIME_ZONE));
            return zdt.toInstant().toEpochMilli() - 1;
        }
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.of(TIME_ZONE));
        return zdt.toInstant().toEpochMilli();
    }

    public static Long getDaysNumber(Long startDateTimestamp, Long endDateTimestamp) {
        try {
            LocalDate startDate = Instant.ofEpochMilli(startDateTimestamp).atZone(ZoneId.of(TIME_ZONE)).toLocalDate();
            LocalDate endDate = Instant.ofEpochMilli(endDateTimestamp).atZone(ZoneId.of(TIME_ZONE)).toLocalDate();
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
        } catch (Exception ex) {
            log.error("can not parse time!!! " + ex);
            return null;
        }
    }

    public static String formatDate(Instant dateInstant) {
        if (dateInstant == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.systemDefault()).format(dateInstant);
    }

}
