package com.haibin.calendarview;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Some date calculation tools
 */
final class CalendarUtil {

    private static final long ONE_DAY = 1000 * 3600 * 24;

    @SuppressLint("SimpleDateFormat")
    static int getDate(String formatStr, Date date) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return Integer.parseInt(format.format(date));
    }

    /**
     * Determine whether a date is a weekend, that is, Saturday and Sunday
     *
     * @param calendar calendar
     * @return judge whether a date is a weekend, that is, Saturday and Sunday
     */
    static boolean isWeekend(Calendar calendar) {
        int week = getWeekFormCalendar(calendar);
        return week == 0 || week == 6;
    }

    /**
     * Get the number of days in a month
     *
     * @param year
     * @param month month
     * @return the number of days in a month
     */
    static int getMonthDaysCount(int year, int month) {
        int count = 0;
        //Judging the big month
        if (month == 1 || month == 3 || month == 5 || month == 7
                || month == 8 || month == 10 || month == 12) {
            count = 31;
        }

        //Judging Satsuki
        if (month == 4 || month == 6 || month == 9 || month == 11) {
            count = 30;
        }

        //Judging the average year and leap year
        if (month == 2) {
            if (isLeapYear(year)) {
                count = 29;
            } else {
                count = 28;
            }
        }
        return count;
    }


    /**
     * Is it a leap year
     *
     * @param year year
     * @return is it a leap year
     */
    static boolean isLeapYear(int year) {
        return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
    }


    static int getMonthViewLineCount(int year, int month, int weekStartWith, int mode) {
        if (mode == CalendarViewDelegate.MODE_ALL_MONTH) {
            return 6;
        }
        int nextDiff = CalendarUtil.getMonthEndDiff(year, month, weekStartWith);
        int preDiff = CalendarUtil.getMonthViewStartDiff(year, month, weekStartWith);
        int monthDayCount = CalendarUtil.getMonthDaysCount(year, month);
        return (preDiff + monthDayCount + nextDiff) / 7;
    }

    /**
     * Get the exact height of the month view
     * Test pass
     *
     * @param year
     * @param month month
     * @param itemHeight The height of each item
     * @return does not require the height of extra rows
     */
    static int getMonthViewHeight(int year, int month, int itemHeight, int weekStartWith) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, 1,12,0,0);
        int preDiff = getMonthViewStartDiff(year, month, weekStartWith);
        int monthDaysCount = getMonthDaysCount(year, month);
        int nextDiff = getMonthEndDiff(year, month, monthDaysCount, weekStartWith);
        return (preDiff + monthDaysCount + nextDiff) / 7 * itemHeight;
    }

    /**
     * Get the exact height of the month view
     * Test pass
     *
     * @param year
     * @param month month
     * @param itemHeight The height of each item
     * @return does not require the height of extra rows
     */
    static int getMonthViewHeight(int year, int month, int itemHeight, int weekStartWith, int mode) {
        if (mode == CalendarViewDelegate.MODE_ALL_MONTH) {
            return itemHeight * 6;
        }
        return getMonthViewHeight(year, month, itemHeight, weekStartWith);
    }

    /**
     * Get the week of a certain day in the month, in other words, get the row and week of the month view of the day, and get it dynamically according to the start of the week
     * Test pass, unit test passed
     *
     * @param calendar calendar
     * @param weekStart What day is the week actually?
     * @return Get the week line in MonthView of a certain day in the week of the month
     */
    static int getWeekFromDayInMonth(Calendar calendar, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(calendar.getYear(), calendar.getMonth() - 1, 1,12,0,0);
        //The first day of the month is the day of the week, and Sunday == 0
        int diff = getMonthViewStartDiff(calendar, weekStart);
        return (calendar.getDay() + diff - 1) / 7 + 1;
    }

    /**
     * Get the previous day
     *
     * @param calendar calendar
     * @return Get the previous day
     */
    static Calendar getPreCalendar(Calendar calendar) {
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay(),12,0,0);//

        long timeMills = date.getTimeInMillis();//Get the start timestamp

        date.setTimeInMillis(timeMills - ONE_DAY);

        Calendar preCalendar = new Calendar();
        preCalendar.setYear(date.get(java.util.Calendar.YEAR));
        preCalendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
        preCalendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));

        return preCalendar;
    }

    static Calendar getNextCalendar(Calendar calendar) {
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay(),12,0,0);//

        long timeMills = date.getTimeInMillis();//获得起始时间戳

        date.setTimeInMillis(timeMills + ONE_DAY);

        Calendar nextCalendar = new Calendar();
        nextCalendar.setYear(date.get(java.util.Calendar.YEAR));
        nextCalendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
        nextCalendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));

        return nextCalendar;
    }

    /**
     * DAY_OF_WEEK return 1 2 3 4 5 6 7, offset by one
     * Get the starting offset corresponding to the month view where the date is located
     * Test pass
     *
     * @param calendar calendar
     * @param weekStart weekStart week start
     * @return Get the start diff with MonthView corresponding to the month view where the date is located
     */
    static int getMonthViewStartDiff(Calendar calendar, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(calendar.getYear(), calendar.getMonth() - 1, 1,12,0,0);
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return week - 1;
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return week == 1 ? 6 : week - weekStart;
        }
        return week == CalendarViewDelegate.WEEK_START_WITH_SAT ? 0 : week;
    }


    /**
     * DAY_OF_WEEK return 1 2 3 4 5 6 7, offset by one
     * Get the starting offset corresponding to the month view where the date is located
     * Test pass
     *
     * @param year
     * @param month month
     * @param weekStart week start
     * @return Get the start diff with MonthView corresponding to the month view where the date is located
     */
    static int getMonthViewStartDiff(int year, int month, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, 1,12,0,0);
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return week - 1;
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return week == 1 ? 6 : week - weekStart;
        }
        return week == CalendarViewDelegate.WEEK_START_WITH_SAT ? 0 : week;
    }


    /**
     * DAY_OF_WEEK return 1 2 3 4 5 6 7, offset by one
     * Get the end offset corresponding to the date and month, used to calculate the total number of weeks between the two years, not used in MonthView
     * Test pass
     *
     * @param year
     * @param month month
     * @param weekStart week start
     * @return Get the end diff corresponding to the date month the end diff in Month not MonthView
     */
    static int getMonthEndDiff(int year, int month, int weekStart) {
        return getMonthEndDiff(year, month, getMonthDaysCount(year, month), weekStart);
    }


    /**
     * DAY_OF_WEEK return 1 2 3 4 5 6 7, offset by one
     * Get the end offset corresponding to the date and month, used to calculate the total number of weeks between the two years, not used in MonthView
     * Test pass
     *
     * @param year
     * @param month month
     * @param weekStart week start
     * @return Get the end diff corresponding to the date month the end diff in Month not MonthView
     */
    private static int getMonthEndDiff(int year, int month, int day, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, day);
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return 7 - week;
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return week == 1 ? 0 : 7 - week + 1;
        }
        return week == 7 ? 6 : 7 - week - 1;
    }

    /**
     * Get the day of the week for a certain date
     * Test passed
     *
     * @param calendar a certain date
     * @return returns the day of the week for a certain date
     */
    static int getWeekFormCalendar(Calendar calendar) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay());
        return date.get(java.util.Calendar.DAY_OF_WEEK) - 1;
    }


    /**
     * Get the switch default option position of week view WeekView index
     * Test pass
     *
     * @param calendar calendar
     * @param weekStart weekStart
     * @return Get the switch default option position of the week view
     */
    static int getWeekViewIndexFromCalendar(Calendar calendar, int weekStart) {
        return getWeekViewStartDiff(calendar.getYear(), calendar.getMonth(), calendar.getDay(), weekStart);
    }

    /**
     * Is it within the date range
     * Test pass
     *
     * @param calendar calendar
     * @param minYear minYear
     * @param minYearDay minimum year day
     * @param minYearMonth minYearMonth
     * @param maxYear maxYear
     * @param maxYearMonth maxYearMonth
     * @param maxYearDay maximum year day
     * @return is within the date range
     */
    static boolean isCalendarInRange(Calendar calendar,
                                     int minYear, int minYearMonth, int minYearDay,
                                     int maxYear, int maxYearMonth, int maxYearDay) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(minYear, minYearMonth - 1, minYearDay);
        long minTime = c.getTimeInMillis();
        c.set(maxYear, maxYearMonth - 1, maxYearDay);
        long maxTime = c.getTimeInMillis();
        c.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay());
        long curTime = c.getTimeInMillis();
        return curTime >= minTime && curTime <= maxTime;
    }

    /**
     * Get the total number of weeks between two dates,
     * Note the beginning of the week on Monday, Sunday, and Saturday
     * Test pass
     *
     * @param minYear minYear minimum year
     * @param minYearMonth maxYear minimum year month
     * @param minYearDay minimum year day
     * @param maxYear maxYear maximum year
     * @param maxYearMonth maxYear maximum year month
     * @param maxYearDay maximum year day
     * @param weekStart week start
     * @return week number is used for WeekViewPager itemCount
     */
    static int getWeekCountBetweenBothCalendar(int minYear, int minYearMonth, int minYearDay,
                                               int maxYear, int maxYearMonth, int maxYearDay,
                                               int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(minYear, minYearMonth - 1, minYearDay);
        long minTimeMills = date.getTimeInMillis();//Given timestamp
        int preDiff = getWeekViewStartDiff(minYear, minYearMonth, minYearDay, weekStart);

        date.set(maxYear, maxYearMonth - 1, maxYearDay);

        long maxTimeMills = date.getTimeInMillis();//Given timestamp

        int nextDiff = getWeekViewEndDiff(maxYear, maxYearMonth, maxYearDay, weekStart);

        int count = preDiff + nextDiff;

        int c = (int) ((maxTimeMills - minTimeMills) / ONE_DAY) + 1;
        count += c;
        return count / 7;
    }


    /**
     * According to the date to get the smallest date in the first few weeks
     * Used to set WeekView currentItem
     * Test pass
     *
     * @param calendar calendar
     * @param minYear minYear minimum year
     * @param minYearMonth maxYear minimum year month
     * @param minYearDay minimum year day
     * @param weekStart week start
     * @return returns the WeekView currentItem in the week of the two years
     */
    static int getWeekFromCalendarStartWithMinCalendar(Calendar calendar,
                                                       int minYear, int minYearMonth, int minYearDay,
                                                       int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(minYear, minYearMonth - 1, minYearDay);//Start date
        long firstTimeMill = date.getTimeInMillis();//Get range start timestamp

        int preDiff = getWeekViewStartDiff(minYear, minYearMonth, minYearDay, weekStart);//Week offset from the start of the range

        int weekStartDiff = getWeekViewStartDiff(calendar.getYear(),
                calendar.getMonth(),
                calendar.getDay(),
                weekStart);//Get the clicked day at the start of the week view. In order to be compatible with the global time zone, the maximum daily difference is one day. If the week start deviation weekStartDiff=0, the date is increased by 1

        date.set(calendar.getYear(),
                calendar.getMonth() - 1,
                weekStartDiff == 0 ? calendar.getDay() + 1 : calendar.getDay());

        long curTimeMills = date.getTimeInMillis();//Given timestamp

        int c = (int) ((curTimeMills - firstTimeMill) / ONE_DAY);

        int count = preDiff + c;

        return count / 7 + 1;
    }

    /**
     * Calculate the first day of the week based on the number of weeks and the minimum date,
     * In order to prevent the daylight saving time, the time will be advanced and delayed by 1-2 hours, resulting in an error of 1 day in the date, so hourOfDay = 12
     * //Test pass Test pass
     *
     * @param minYear minimum year such as 2017
     * @param minYearMonth maxYear minimum year and month, like: 2017-07
     * @param minYearDay minimum year day
     * @param week Weeks starting from minYear month minYearMonth day 1 of the minimum year week> 0
     * @return the first day of the week
     */
    static Calendar getFirstCalendarStartWithMinCalendar(int minYear, int minYearMonth, int minYearDay, int week, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(minYear, minYearMonth - 1, minYearDay, 12, 0);//

        long firstTimeMills = date.getTimeInMillis();//Get the start timestamp


        long weekTimeMills = (week - 1) * 7 * ONE_DAY;

        long timeCountMills = weekTimeMills + firstTimeMills;

        date.setTimeInMillis(timeCountMills);

        int startDiff = getWeekViewStartDiff(date.get(java.util.Calendar.YEAR),
                date.get(java.util.Calendar.MONTH) + 1,
                date.get(java.util.Calendar.DAY_OF_MONTH), weekStart);

        timeCountMills -= startDiff * ONE_DAY;
        date.setTimeInMillis(timeCountMills);

        Calendar calendar = new Calendar();
        calendar.setYear(date.get(java.util.Calendar.YEAR));
        calendar.setMonth(date.get(java.util.Calendar.MONTH) + 1);
        calendar.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));

        return calendar;
    }


    /**
     * Is it within the date range
     *
     * @param calendar calendar
     * @param delegate delegate
     * @return is within the date range
     */
    static boolean isCalendarInRange(Calendar calendar, CalendarViewDelegate delegate) {
        return isCalendarInRange(calendar,
                delegate.getMinYear(), delegate.getMinYearMonth(), delegate.getMinYearDay(),
                delegate.getMaxYear(), delegate.getMaxYearMonth(), delegate.getMaxYearDay());
    }

    /**
     * Is it within the date range
     *
     * @param year year
     * @param month month
     * @param minYear minYear
     * @param minYearMonth minYearMonth
     * @param maxYear maxYear
     * @param maxYearMonth maxYearMonth
     * @return is within the date range
     */
    static boolean isMonthInRange(int year, int month, int minYear, int minYearMonth, int maxYear, int maxYearMonth) {
        return !(year < minYear || year > maxYear) &&
                !(year == minYear && month < minYearMonth) &&
                !(year == maxYear && month > maxYearMonth);
    }

    /**
     * Operation calendar1-calendar2
     * test Pass
     *
     * @param calendar1 calendar1
     * @param calendar2 calendar2
     * @return calendar1-calendar2
     */
    static int differ(Calendar calendar1, Calendar calendar2) {
        if (calendar1 == null) {
            return Integer.MIN_VALUE;
        }
        if (calendar2 == null) {
            return Integer.MAX_VALUE;
        }
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(calendar1.getYear(), calendar1.getMonth() - 1, calendar1.getDay(), 12, 0, 0);//

        long startTimeMills = date.getTimeInMillis();//获得起始时间戳

        date.set(calendar2.getYear(), calendar2.getMonth() - 1, calendar2.getDay(), 12, 0, 0);//

        long endTimeMills = date.getTimeInMillis();//获得结束时间戳

        return (int) ((startTimeMills - endTimeMills) / ONE_DAY);
    }

    /**
     * Compare date size
     *
     * @param minYear minYear
     * @param minYearMonth minYearMonth
     * @param minYearDay minYearDay
     * @param maxYear maxYear
     * @param maxYearMonth maxYearMonth
     * @param maxYearDay maxYearDay
     * @return -1 0 1
     */
    static int compareTo(int minYear, int minYearMonth, int minYearDay,
                         int maxYear, int maxYearMonth, int maxYearDay) {
        Calendar first = new Calendar();
        first.setYear(minYear);
        first.setMonth(minYearMonth);
        first.setDay(minYearDay);

        Calendar second = new Calendar();
        second.setYear(maxYear);
        second.setMonth(maxYearMonth);
        second.setDay(maxYearDay);
        return first.compareTo(second);
    }

    /**
     * Initialize calendar for month view
     *
     * @param year year
     * @param month month
     * @param currentDate currentDate
     * @param weekStar weekStar
     * @return initialize calendar entry for month view
     */
    static List<Calendar> initCalendarForMonthView(int year, int month, Calendar currentDate, int weekStar) {
        java.util.Calendar date = java.util.Calendar.getInstance();

        date.set(year, month - 1, 1);

        int mPreDiff = getMonthViewStartDiff(year, month, weekStar);//Get the actual offset of the month view

        int monthDayCount = getMonthDaysCount(year, month);//Get the true number of days in the month

        int preYear, preMonth;
        int nextYear, nextMonth;

        int size = 42;

        List<Calendar> mItems = new ArrayList<>();

        int preMonthDaysCount;
        if (month == 1) {//If it's January
            preYear = year - 1;
            preMonth = 12;
            nextYear = year;
            nextMonth = month + 1;
            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
        } else if (month == 12) {//If it's December
            preYear = year;
            preMonth = month - 1;
            nextYear = year + 1;
            nextMonth = 1;
            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
        } else {//usually
            preYear = year;
            preMonth = month - 1;
            nextYear = year;
            nextMonth = month + 1;
            preMonthDaysCount = mPreDiff == 0 ? 0 : CalendarUtil.getMonthDaysCount(preYear, preMonth);
        }
        int nextDay = 1;
        for (int i = 0; i < size; i++) {
            Calendar calendarDate = new Calendar();
            if (i < mPreDiff) {
                calendarDate.setYear(preYear);
                calendarDate.setMonth(preMonth);
                calendarDate.setDay(preMonthDaysCount - mPreDiff + i + 1);
            } else if (i >= monthDayCount + mPreDiff) {
                calendarDate.setYear(nextYear);
                calendarDate.setMonth(nextMonth);
                calendarDate.setDay(nextDay);
                ++nextDay;
            } else {
                calendarDate.setYear(year);
                calendarDate.setMonth(month);
                calendarDate.setCurrentMonth(true);
                calendarDate.setDay(i - mPreDiff + 1);
            }
            if (calendarDate.equals(currentDate)) {
                calendarDate.setCurrentDay(true);
            }
            LunarCalendar.setupLunarCalendar(calendarDate);
            mItems.add(calendarDate);
        }
        return mItems;
    }

    static List<Calendar> getWeekCalendars(Calendar calendar, CalendarViewDelegate mDelegate) {
        long curTime = calendar.getTimeInMillis();

        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(calendar.getYear(),
                calendar.getMonth() - 1,
                calendar.getDay(), 12, 0);//
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        int startDiff;
        if (mDelegate.getWeekStart() == 1) {
            startDiff = week - 1;
        } else if (mDelegate.getWeekStart() == 2) {
            startDiff = week == 1 ? 6 : week - mDelegate.getWeekStart();
        } else {
            startDiff = week == 7 ? 0 : week;
        }

        curTime -= startDiff * ONE_DAY;
        java.util.Calendar minCalendar = java.util.Calendar.getInstance();
        minCalendar.setTimeInMillis(curTime);
        Calendar startCalendar = new Calendar();
        startCalendar.setYear(minCalendar.get(java.util.Calendar.YEAR));
        startCalendar.setMonth(minCalendar.get(java.util.Calendar.MONTH) + 1);
        startCalendar.setDay(minCalendar.get(java.util.Calendar.DAY_OF_MONTH));
        return initCalendarForWeekView(startCalendar, mDelegate, mDelegate.getWeekStart());
    }

    /**
     * 7 items to generate weekly view
     *
     * @param calendar The first day of the week view calendar, so it will be postponed for 6 days and the week view will be generated
     * @param mDelegate mDelegate
     * @param weekStart weekStart
     * @return generates 7 items of week view
     */
    @SuppressWarnings("unused")
    static List<Calendar> initCalendarForWeekView(Calendar calendar, CalendarViewDelegate mDelegate, int weekStart) {

        java.util.Calendar date = java.util.Calendar.getInstance();//Time of day
        date.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay(), 12, 0);
        long curDateMills = date.getTimeInMillis();//Generate selected date and time stamp

        //int weekEndDiff = getWeekViewEndDiff(calendar.getYear(), calendar.getMonth(), calendar.getDay(), weekStart);
        //weekEndDiff For example, the start of the week is Sunday 1, and the current is 2020-04-01. On Wednesday, weekEndDiff is three days after the end of the week, weekEndDiff=3
        int weekEndDiff = 6;
        List<Calendar> mItems = new ArrayList<>();

        date.setTimeInMillis(curDateMills);
        Calendar selectCalendar = new Calendar();
        selectCalendar.setYear(calendar.getYear());
        selectCalendar.setMonth(calendar.getMonth());
        selectCalendar.setDay(calendar.getDay());
        if (selectCalendar.equals(mDelegate.getCurrentDay())) {
            selectCalendar.setCurrentDay(true);
        }
        LunarCalendar.setupLunarCalendar(selectCalendar);
        selectCalendar.setCurrentMonth(true);
        mItems.add(selectCalendar);


        for (int i = 1; i <= weekEndDiff; i++) {
            date.setTimeInMillis(curDateMills + i * ONE_DAY);
            Calendar calendarDate = new Calendar();
            calendarDate.setYear(date.get(java.util.Calendar.YEAR));
            calendarDate.setMonth(date.get(java.util.Calendar.MONTH) + 1);
            calendarDate.setDay(date.get(java.util.Calendar.DAY_OF_MONTH));
            if (calendarDate.equals(mDelegate.getCurrentDay())) {
                calendarDate.setCurrentDay(true);
            }
            LunarCalendar.setupLunarCalendar(calendarDate);
            calendarDate.setCurrentMonth(true);
            mItems.add(calendarDate);
        }
        return mItems;
    }

    /**
     * Unit test passed
     * Get the starting offset of the week view from the selected date to generate the week view layout
     *
     * @param year year
     * @param month month
     * @param day day
     * @param weekStart Week start, 1, 2, 7 Monday, Saturday
     * @return Get the starting offset of the week view, used to generate the week view layout
     */
    private static int getWeekViewStartDiff(int year, int month, int day, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, day, 12, 0);//
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == 1) {
            return week - 1;
        }
        if (weekStart == 2) {
            return week == 1 ? 6 : week - weekStart;
        }
        return week == 7 ? 0 : week;
    }


    /**
     * Unit test passed
     * Get the end offset of the week view from the selected date to generate the week view layout
     * In order to be compatible with DST, the DST time zone may have a time offset of 1-2 hours, which causes the actual date obtained in the early morning to move forward or backward by one day.
     * The calendar does not have the concept of hours and minutes, so the date and time are forced to 12:00 to avoid DST compatibility issues
     * @param year year
     * @param month month
     * @param day day
     * @param weekStart Week start, 1, 2, 7 Monday, Saturday
     * @return Get the end offset of the week view, used to generate the week view layout
     */
    public static int getWeekViewEndDiff(int year, int month, int day, int weekStart) {
        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(year, month - 1, day, 12, 0);
        int week = date.get(java.util.Calendar.DAY_OF_WEEK);
        if (weekStart == 1) {
            return 7 - week;
        }
        if (weekStart == 2) {
            return week == 1 ? 0 : 7 - week + 1;
        }
        return week == 7 ? 6 : 7 - week - 1;
    }


    /**
     * Switch to get the date of the first day from the month view
     * Test Pass is 100% correct
     * @param position position
     * @param delegate position
     * @return Switch to get the date of the first day from the month view
     */
    static Calendar getFirstCalendarFromMonthViewPager(int position, CalendarViewDelegate delegate) {
        Calendar calendar = new Calendar();
        calendar.setYear((position + delegate.getMinYearMonth() - 1) / 12 + delegate.getMinYear());
        calendar.setMonth((position + delegate.getMinYearMonth() - 1) % 12 + 1);
        if (delegate.getDefaultCalendarSelectDay() != CalendarViewDelegate.FIRST_DAY_OF_MONTH) {
            int monthDays = getMonthDaysCount(calendar.getYear(), calendar.getMonth());
            Calendar indexCalendar = delegate.mIndexCalendar;
            calendar.setDay(indexCalendar == null || indexCalendar.getDay() == 0 ? 1 :
                    monthDays < indexCalendar.getDay() ? monthDays : indexCalendar.getDay());
        } else {
            calendar.setDay(1);
        }
        if (!isCalendarInRange(calendar, delegate)) {
            if (isMinRangeEdge(calendar, delegate)) {
                calendar = delegate.getMinRangeCalendar();
            } else {
                calendar = delegate.getMaxRangeCalendar();
            }
        }
        calendar.setCurrentMonth(calendar.getYear() == delegate.getCurrentDay().getYear() &&
                calendar.getMonth() == delegate.getCurrentDay().getMonth());
        calendar.setCurrentDay(calendar.equals(delegate.getCurrentDay()));
        LunarCalendar.setupLunarCalendar(calendar);
        return calendar;
    }


    /**
     * Get the boundary access date according to the incoming date, either the largest or the smallest
     *
     * @param calendar calendar
     * @param delegate delegate
     * @return Get boundary access date
     */
    static Calendar getRangeEdgeCalendar(Calendar calendar, CalendarViewDelegate delegate) {
        if (CalendarUtil.isCalendarInRange(delegate.getCurrentDay(), delegate)
                && delegate.getDefaultCalendarSelectDay() != CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY_IGNORE_CURRENT) {
            return delegate.createCurrentDate();
        }
        if (isCalendarInRange(calendar, delegate)) {
            return calendar;
        }
        Calendar minRangeCalendar = delegate.getMinRangeCalendar();
        if (minRangeCalendar.isSameMonth(calendar)) {
            return delegate.getMinRangeCalendar();
        }
        return delegate.getMaxRangeCalendar();
    }

    /**
     * Is it the minimum access boundary?
     *
     * @param calendar calendar
     * @return is the minimum access boundary
     */
    private static boolean isMinRangeEdge(Calendar calendar, CalendarViewDelegate delegate) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(delegate.getMinYear(), delegate.getMinYearMonth() - 1, delegate.getMinYearDay(), 12, 0);
        long minTime = c.getTimeInMillis();
        c.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay(), 12, 0);
        long curTime = c.getTimeInMillis();
        return curTime < minTime;
    }

    /**
     * dp to px
     *
     * @param context context
     * @param dpValue dp
     * @return px
     */
    static int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
