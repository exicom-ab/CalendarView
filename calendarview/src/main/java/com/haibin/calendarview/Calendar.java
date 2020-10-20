package com.haibin.calendarview;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Calendar object、
 */
@SuppressWarnings("all")
public final class Calendar implements Serializable, Comparable<Calendar> {
    private static final long serialVersionUID = 141315161718191143L;


    /**
     * year
     */
    private int year;

    /**
     * Month 1-12
     */
    private int month;

    /**
     * If it is a leap month, return to the leap month
     */
    private int leapMonth;

    /**
     * Day 1-31
     */
    private int day;

    /**
     * Is it a leap year
     */
    private boolean isLeapYear;

    /**
     * Whether it is the current month, this corresponds to the current month in the month view, not the current month, please note
     */
    private boolean isCurrentMonth;

    /**
     * Is it today
     */
    private boolean isCurrentDay;

    /**
     * Lunar string, which has no special significance, is used to make simple lunar calendar or holiday markings
     * It is recommended to obtain the complete lunar date through lunarCakendar
     */
    private String lunar;


    /**
     * 24 solar terms
     */
    private String solarTerm;


    /**
     * Gregorian holiday
     */
    private String gregorianFestival;

    /**
     * Traditional lunar festival
     */
    private String traditionFestival;

    /**
     * Plan, can be used to mark whether there are tasks for the day, here is the default, if you use multiple marks, please use the following API
     * using addScheme(int schemeColor,String scheme); multi scheme
     */
    private String scheme;

    /**
     * Various custom marker colors, if not, choose the default color, if you use multiple markers, please use the following API
     * using addScheme(int schemeColor,String scheme); multi scheme
     */
    private int schemeColor;


    /**
     * Multiple markers
     * multi scheme,using addScheme();
     */
    private List<Scheme> schemes;

    /**
     * Is it weekend
     */
    private boolean isWeekend;

    /**
     * Week, 0-6 corresponds to Sunday to Monday
     */
    private int week;

    /**
     * Get the complete lunar date
     */
    private Calendar lunarCalendar;


    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }


    public void setCurrentMonth(boolean currentMonth) {
        this.isCurrentMonth = currentMonth;
    }

    public boolean isCurrentDay() {
        return isCurrentDay;
    }

    public void setCurrentDay(boolean currentDay) {
        isCurrentDay = currentDay;
    }


    public String getLunar() {
        return lunar;
    }

    public void setLunar(String lunar) {
        this.lunar = lunar;
    }


    public String getScheme() {
        return scheme;
    }


    public void setScheme(String scheme) {
        this.scheme = scheme;
    }


    public int getSchemeColor() {
        return schemeColor;
    }

    public void setSchemeColor(int schemeColor) {
        this.schemeColor = schemeColor;
    }


    public List<Scheme> getSchemes() {
        return schemes;
    }

    public void setSchemes(List<Scheme> schemes) {
        this.schemes = schemes;
    }


    public void addScheme(Scheme scheme) {
        if (schemes == null) {
            schemes = new ArrayList<>();
        }
        schemes.add(scheme);
    }

    public void addScheme(int schemeColor, String scheme) {
        if (schemes == null) {
            schemes = new ArrayList<>();
        }
        schemes.add(new Scheme(schemeColor, scheme));
    }

    public void addScheme(int type, int schemeColor, String scheme) {
        if (schemes == null) {
            schemes = new ArrayList<>();
        }
        schemes.add(new Scheme(type, schemeColor, scheme));
    }

    public void addScheme(int type, int schemeColor, String scheme, String other) {
        if (schemes == null) {
            schemes = new ArrayList<>();
        }
        schemes.add(new Scheme(type, schemeColor, scheme, other));
    }

    public void addScheme(int schemeColor, String scheme, String other) {
        if (schemes == null) {
            schemes = new ArrayList<>();
        }
        schemes.add(new Scheme(schemeColor, scheme, other));
    }

    public boolean isWeekend() {
        return isWeekend;
    }

    public void setWeekend(boolean weekend) {
        isWeekend = weekend;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public Calendar getLunarCalendar() {
        return lunarCalendar;
    }

    public void setLunarCalendar(Calendar lunarCakendar) {
        this.lunarCalendar = lunarCakendar;
    }

    public String getSolarTerm() {
        return solarTerm;
    }

    public void setSolarTerm(String solarTerm) {
        this.solarTerm = solarTerm;
    }

    public String getGregorianFestival() {
        return gregorianFestival;
    }

    public void setGregorianFestival(String gregorianFestival) {
        this.gregorianFestival = gregorianFestival;
    }


    public int getLeapMonth() {
        return leapMonth;
    }

    public void setLeapMonth(int leapMonth) {
        this.leapMonth = leapMonth;
    }

    public boolean isLeapYear() {
        return isLeapYear;
    }

    public void setLeapYear(boolean leapYear) {
        isLeapYear = leapYear;
    }

    public String getTraditionFestival() {
        return traditionFestival;
    }

    public void setTraditionFestival(String traditionFestival) {
        this.traditionFestival = traditionFestival;
    }

    public boolean hasScheme() {
        if (schemes != null && schemes.size() != 0) {
            return true;
        }
        if (!TextUtils.isEmpty(scheme)) {
            return true;
        }
        return false;
    }

    /**
     * Is it the same month
     *
     * @param calendar date
     * @return Is it the same month
     */
    public boolean isSameMonth(Calendar calendar) {
        return year == calendar.getYear() && month == calendar.getMonth();
    }

    /**
     * Comparison date
     *
     * @param calendar date
     * @return -1 0 1
     */
    public int compareTo(Calendar calendar) {
        if (calendar == null) {
            return 1;
        }
        return toString().compareTo(calendar.toString());
    }

    /**
     * How many days is the calculation gap
     *
     * @param calendar calendar
     * @return How many days is the calculation gap
     */
    public final int differ(Calendar calendar) {
        return CalendarUtil.differ(this, calendar);
    }

    /**
     * Whether the date is available
     *
     * @return Whether the date is available
     */
    public boolean isAvailable() {
        return year > 0 & month > 0 & day > 0 & day <=31 & month <= 12 & year >= 1900 & year <= 2099;
    }

    /**
     * Get the timestamp corresponding to the current calendar
     *
     * @return getTimeInMillis
     */
    public long getTimeInMillis() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.YEAR, year);
        calendar.set(java.util.Calendar.MONTH, month - 1);
        calendar.set(java.util.Calendar.DAY_OF_MONTH, day);
        return calendar.getTimeInMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Calendar) {
            if (((Calendar) o).getYear() == year && ((Calendar) o).getMonth() == month && ((Calendar) o).getDay() == day)
                return true;
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return year + "" + (month < 10 ? "0" + month : month) + "" + (day < 10 ? "0" + day : day);
    }

//    @Override
//    public int compare(Calendar lhs, Calendar rhs) {
//        if (lhs == null || rhs == null) {
//            return 0;
//        }
//        int result = lhs.compareTo(rhs);
//        return result;
//    }

    final void mergeScheme(Calendar calendar, String defaultScheme) {
        if (calendar == null)
            return;
        setScheme(TextUtils.isEmpty(calendar.getScheme()) ?
                defaultScheme : calendar.getScheme());
        setSchemeColor(calendar.getSchemeColor());
        setSchemes(calendar.getSchemes());
    }

    final void clearScheme() {
        setScheme("");
        setSchemeColor(0);
        setSchemes(null);
    }

    /**
     * Event marking service, it is recommended to use this for multiple types of transaction marking
     */
    public final static class Scheme implements Serializable {
        private int type;
        private int shcemeColor;
        private String scheme;
        private String other;
        private Object obj;

        public Scheme() {
        }

        public Scheme(int type, int shcemeColor, String scheme, String other) {
            this.type = type;
            this.shcemeColor = shcemeColor;
            this.scheme = scheme;
            this.other = other;
        }

        public Scheme(int type, int shcemeColor, String scheme) {
            this.type = type;
            this.shcemeColor = shcemeColor;
            this.scheme = scheme;
        }

        public Scheme(int shcemeColor, String scheme) {
            this.shcemeColor = shcemeColor;
            this.scheme = scheme;
        }

        public Scheme(int shcemeColor, String scheme, String other) {
            this.shcemeColor = shcemeColor;
            this.scheme = scheme;
            this.other = other;
        }

        public int getShcemeColor() {
            return shcemeColor;
        }

        public void setShcemeColor(int shcemeColor) {
            this.shcemeColor = shcemeColor;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getOther() {
            return other;
        }

        public void setOther(String other) {
            this.other = other;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Object getObj() {
            return obj;
        }

        public void setObj(Object obj) {
            this.obj = obj;
        }
    }
}
