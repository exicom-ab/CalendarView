package com.haibin.calendarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Lunar calculation related
 */
@SuppressWarnings("all")
public final class LunarCalendar {


    static void init(Context context) {
        if (MONTH_STR != null) {
            return;
        }
        TrunkBranchAnnals.init(context);
        SolarTermUtil.init(context);
        MONTH_STR = context.getResources().getStringArray(R.array.lunar_first_of_month);
        TRADITION_FESTIVAL_STR = context.getResources().getStringArray(R.array.tradition_festival);
        DAY_STR = context.getResources().getStringArray(R.array.lunar_str);
        SPECIAL_FESTIVAL_STR = context.getResources().getStringArray(R.array.special_festivals);
        SOLAR_CALENDAR = context.getResources().getStringArray(R.array.solar_festival);
    }

    /**
     * Transcribed on the first day of the lunar month
     */
    private static String[] MONTH_STR = null;

    /**
     * Traditional lunar festival
     */
    private static String[] TRADITION_FESTIVAL_STR = null;

    /**
     * Chinese lunar capital
     */
    private static String[] DAY_STR = null;

    /**
     * Array of special holidays
     */
    private static String[] SPECIAL_FESTIVAL_STR = null;

    /**
     * Special holidays, Mother's Day and Father's Day, Thanksgiving Day, etc.
     */
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, String[]> SPECIAL_FESTIVAL = new HashMap<>();

    /**
     * Gregorian holiday
     */
    private static String[] SOLAR_CALENDAR = null;

    /**
     * Save 24 solar terms per year
     */
    @SuppressLint("UseSparseArrays")
    private static final Map<Integer, String[]> SOLAR_TERMS = new HashMap<>();

    /**
     * Return to traditional lunar festival
     *
     * @param year Lunar New Year
     * @param month Lunar month
     * @param day Chinese calendar day
     * @return returns to the traditional lunar festival
     */
    private static String getTraditionFestival(int year, int month, int day) {
        if (month == 12) {
            int count = daysInLunarMonth(year, month);
            if (day == count) {
                return TRADITION_FESTIVAL_STR[0];//除夕
            }
        }
        String text = getString(month, day);
        String festivalStr = "";
        for (String festival : TRADITION_FESTIVAL_STR) {
            if (festival.contains(text)) {
                festivalStr = festival.replace(text, "");
                break;
            }
        }
        return festivalStr;
    }


    /**
     * Numbers are converted to Chinese characters month
     *
     * @param month month
     * @param leap 1== leap month
     * @return number is converted to Chinese character month
     */
    private static String numToChineseMonth(int month, int leap) {
        if (leap == 1) {
            return "闰" + MONTH_STR[month - 1];
        }
        return MONTH_STR[month - 1];
    }

    /**
     * Numbers are converted to lunar holidays or dates
     *
     * @param month month
     * @param day
     * @param leap 1== leap month
     * @return numbers are converted to Chinese characters
     */
    private static String numToChinese(int month, int day, int leap) {
        if (day == 1) {
            return numToChineseMonth(month, leap);
        }
        return DAY_STR[day - 1];
    }

    /**
     * Used to represent the relevant information of the lunar calendar year from 1900 to 2099, a total of 24 bits of hexadecimal representation, of which:
     * 1. The first 4 digits indicate which month in the year;
     * 2. The 5-17 digits represent the distribution of large and small months in the 13 months of the lunar calendar year, 0 means small, 1 means large;
     * 3. The last 7 digits represent the Gregorian calendar date corresponding to the first lunar year (first day of the first month).
     * <p/>
     * Take 2014 data 0x955ABF as an example:
     * 1001 0101 0101 1010 1011 1111
     * Leap September, the first day of the first month of the lunar calendar corresponds to January 31 of the Gregorian calendar
     */
    private static final int[] LUNAR_INFO = {
            0x04bd8,0x04ae0,0x0a570,0x054d5,0x0d260,0x0d950,0x16554,0x056a0,0x09ad0,0x055d2,//1900-1909
            0x04ae0,0x0a5b6,0x0a4d0,0x0d250,0x1d255,0x0b540,0x0d6a0,0x0ada2,0x095b0,0x14977,//1910-1919
            0x04970,0x0a4b0,0x0b4b5,0x06a50,0x06d40,0x1ab54,0x02b60,0x09570,0x052f2,0x04970,//1920-1929
            0x06566,0x0d4a0,0x0ea50,0x06e95,0x05ad0,0x02b60,0x186e3,0x092e0,0x1c8d7,0x0c950,//1930-1939
            0x0d4a0,0x1d8a6,0x0b550,0x056a0,0x1a5b4,0x025d0,0x092d0,0x0d2b2,0x0a950,0x0b557,//1940-1949
            0x06ca0,0x0b550,0x15355,0x04da0,0x0a5b0,0x14573,0x052b0,0x0a9a8,0x0e950,0x06aa0,//1950-1959
            0x0aea6,0x0ab50,0x04b60,0x0aae4,0x0a570,0x05260,0x0f263,0x0d950,0x05b57,0x056a0,//1960-1969
            0x096d0,0x04dd5,0x04ad0,0x0a4d0,0x0d4d4,0x0d250,0x0d558,0x0b540,0x0b6a0,0x195a6,//1970-1979
            0x095b0,0x049b0,0x0a974,0x0a4b0,0x0b27a,0x06a50,0x06d40,0x0af46,0x0ab60,0x09570,//1980-1989
            0x04af5,0x04970,0x064b0,0x074a3,0x0ea50,0x06b58,0x055c0,0x0ab60,0x096d5,0x092e0,//1990-1999
            0x0c960,0x0d954,0x0d4a0,0x0da50,0x07552,0x056a0,0x0abb7,0x025d0,0x092d0,0x0cab5,//2000-2009
            0x0a950,0x0b4a0,0x0baa4,0x0ad50,0x055d9,0x04ba0,0x0a5b0,0x15176,0x052b0,0x0a930,//2010-2019
            0x07954,0x06aa0,0x0ad50,0x05b52,0x04b60,0x0a6e6,0x0a4e0,0x0d260,0x0ea65,0x0d530,//2020-2029
            0x05aa0,0x076a3,0x096d0,0x04afb,0x04ad0,0x0a4d0,0x1d0b6,0x0d250,0x0d520,0x0dd45,//2030-2039
            0x0b5a0,0x056d0,0x055b2,0x049b0,0x0a577,0x0a4b0,0x0aa50,0x1b255,0x06d20,0x0ada0,//2040-2049
            0x14b63,0x09370,0x049f8,0x04970,0x064b0,0x168a6,0x0ea50, 0x06b20,0x1a6c4,0x0aae0,//2050-2059
            0x0a2e0,0x0d2e3,0x0c960,0x0d557,0x0d4a0,0x0da50,0x05d55,0x056a0,0x0a6d0,0x055d4,//2060-2069
            0x052d0,0x0a9b8,0x0a950,0x0b4a0,0x0b6a6,0x0ad50,0x055a0,0x0aba4,0x0a5b0,0x052b0,//2070-2079
            0x0b273,0x06930,0x07337,0x06aa0,0x0ad50,0x14b55,0x04b60,0x0a570,0x054e4,0x0d160,//2080-2089
            0x0e968,0x0d520,0x0daa0,0x16aa6,0x056d0,0x04ae0,0x0a9d4,0x0a2d0,0x0d150,0x0f252,//2090-2099
            0x0d520
    };


    /**
     * The total number of days in the year, month and month of the lunar calendar, there are 13 months including leap months
     *
     * @param year The year to be calculated
     * @param month The month to be calculated
     * @return returns the total number of days in the lunar calendar year, month and month
     */
    public static int daysInLunarMonth(int year, int month) {
        if ((LUNAR_INFO[year - CalendarViewDelegate.MIN_YEAR] & (0x10000 >> month)) == 0)
            return 29;
        else
            return 30;
    }

    /**
     * Get Gregorian calendar holiday
     *
     * @param month Gregorian calendar month
     * @param day Gregorian calendar date
     * @return Gregorian calendar holiday
     */
    private static String gregorianFestival(int month, int day) {
        String text = getString(month, day);
        String solar = "";
        for (String aMSolarCalendar : SOLAR_CALENDAR) {
            if (aMSolarCalendar.contains(text)) {
                solar = aMSolarCalendar.replace(text, "");
                break;
            }
        }
        return solar;
    }

    private static String getString(int month, int day) {
        return (month >= 10 ? String.valueOf(month) : "0" + month) + (day >= 10 ? day : "0" + day);
    }


    /**
     * Return 24 solar terms
     *
     * @param year
     * @param month month
     * @param day
     * @return returns 24 solar terms
     */
    private static String getSolarTerm(int year, int month, int day) {
        if (!SOLAR_TERMS.containsKey(year)) {
            SOLAR_TERMS.put(year, SolarTermUtil.getSolarTerms(year));
        }
        String[] solarTerm = SOLAR_TERMS.get(year);
        String text = year + getString(month, day);
        String solar = "";
        assert solarTerm != null;
        for (String solarTermName : solarTerm) {
            if (solarTermName.contains(text)) {
                solar = solarTermName.replace(text, "");
                break;
            }
        }
        return solar;
    }


    /**
     * Get Lunar Festival
     *
     * @param year
     * @param month month
     * @param day
     * @return Lunar Festival
     */
    public static String getLunarText(int year, int month, int day) {
        String termText = LunarCalendar.getSolarTerm(year, month, day);
        String solar = LunarCalendar.gregorianFestival(month, day);
        if (!TextUtils.isEmpty(solar))
            return solar;
        if (!TextUtils.isEmpty(termText))
            return termText;
        int[] lunar = LunarUtil.solarToLunar(year, month, day);
        String festival = getTraditionFestival(lunar[0], lunar[1], lunar[2]);
        if (!TextUtils.isEmpty(festival))
            return festival;
        return LunarCalendar.numToChinese(lunar[1], lunar[2], lunar[3]);
    }


    /**
     * Get special calculation methods for festivals
     * For example: the second Sunday in May is Mother's Day, and the third Sunday in June is Father's Day
     * The fourth Thursday of November is designated as "Thanksgiving Day"
     *
     * @param year year
     * @param month month
     * @param day day
     * @return Get western festivals
     */
    private static String getSpecialFestival(int year, int month, int day) {
        if (!SPECIAL_FESTIVAL.containsKey(year)) {
            SPECIAL_FESTIVAL.put(year, getSpecialFestivals(year));
        }
        String[] specialFestivals = SPECIAL_FESTIVAL.get(year);
        String text = year + getString(month, day);
        String solar = "";
        assert specialFestivals != null;
        for (String special : specialFestivals) {
            if (special.contains(text)) {
                solar = special.replace(text, "");
                break;
            }
        }
        return solar;
    }


    /**
     * Get the annual Mother's Day and Father's Day and Thanksgiving Day
     * Festivals with special calculation methods
     *
     * @param year
     * @return get the annual Mother's Day, Father's Day and Thanksgiving Day
     */
    private static String[] getSpecialFestivals(int year) {
        String[] festivals = new String[3];
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, 4, 1);
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        int startDiff = 7 - week + 1;
        if (startDiff == 7) {
            festivals[0] = dateToString(year, 5, startDiff + 1) + SPECIAL_FESTIVAL_STR[0];
        } else {
            festivals[0] = dateToString(year, 5, startDiff + 7 + 1) + SPECIAL_FESTIVAL_STR[0];
        }
        date.set(year, 5, 1);
        week = date.get(java.util.Calendar.DAY_OF_WEEK);
        startDiff = 7 - week + 1;
        if (startDiff == 7) {
            festivals[1] = dateToString(year, 6, startDiff + 7 + 1) + SPECIAL_FESTIVAL_STR[1];
        } else {
            festivals[1] = dateToString(year, 6, startDiff + 7 + 7 + 1) + SPECIAL_FESTIVAL_STR[1];
        }

        date.set(year, 10, 1);
        week = date.get(java.util.Calendar.DAY_OF_WEEK);
        startDiff = 7 - week + 1;
        if (startDiff <= 2) {
            festivals[2] = dateToString(year, 11, startDiff + 21 + 5) + SPECIAL_FESTIVAL_STR[2];
        } else {
            festivals[2] = dateToString(year, 11, startDiff + 14 + 5) + SPECIAL_FESTIVAL_STR[2];
        }
        return festivals;
    }


    private static String dateToString(int year, int month, int day) {
        return year + getString(month, day);
    }

    /**
     * Initialize various lunar calendars and festivals
     *
     * @param calendar calendar
     */
    public static void setupLunarCalendar(Calendar calendar) {
        int year = calendar.getYear();
        int month = calendar.getMonth();
        int day = calendar.getDay();
        calendar.setWeekend(CalendarUtil.isWeekend(calendar));
        calendar.setWeek(CalendarUtil.getWeekFormCalendar(calendar));

        Calendar lunarCalendar = new Calendar();
        calendar.setLunarCalendar(lunarCalendar);
        int[] lunar = LunarUtil.solarToLunar(year, month, day);
        lunarCalendar.setYear(lunar[0]);
        lunarCalendar.setMonth(lunar[1]);
        lunarCalendar.setDay(lunar[2]);
        calendar.setLeapYear(CalendarUtil.isLeapYear(year));
        if (lunar[3] == 1) {//如果是闰月
            calendar.setLeapMonth(lunar[1]);
            lunarCalendar.setLeapMonth(lunar[1]);
        }
        String solarTerm = LunarCalendar.getSolarTerm(year, month, day);
        String gregorian = LunarCalendar.gregorianFestival(month, day);
        String festival = getTraditionFestival(lunar[0], lunar[1], lunar[2]);
        String lunarText = LunarCalendar.numToChinese(lunar[1], lunar[2], lunar[3]);
        if (TextUtils.isEmpty(gregorian)) {
            gregorian = getSpecialFestival(year, month, day);
        }
        calendar.setSolarTerm(solarTerm);
        calendar.setGregorianFestival(gregorian);
        calendar.setTraditionFestival(festival);
        lunarCalendar.setTraditionFestival(festival);
        lunarCalendar.setSolarTerm(solarTerm);
        if (!TextUtils.isEmpty(solarTerm)) {
            calendar.setLunar(solarTerm);
        } else if (!TextUtils.isEmpty(gregorian)) {
            calendar.setLunar(gregorian);
        } else if (!TextUtils.isEmpty(festival)) {
            calendar.setLunar(festival);
        } else {
            calendar.setLunar(lunarText);
        }
        lunarCalendar.setLunar(lunarText);
    }

    /**
     * Get Lunar Festival
     *
     * @param calendar calendar
     * @return Get the Lunar Festival
     */
    public static String getLunarText(Calendar calendar) {
        return getLunarText(calendar.getYear(), calendar.getMonth(), calendar.getDay());
    }
}
