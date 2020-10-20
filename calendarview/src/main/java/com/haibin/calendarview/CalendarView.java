package com.haibin.calendarview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calendar layout
 * Use package permissions for each class to avoid unnecessary public
 */
@SuppressWarnings({"unused"})
public class CalendarView extends FrameLayout {

    /**
     * Extract custom attributes
     */
    private final CalendarViewDelegate mDelegate;

    /**
     * ViewPager with custom adaptive height
     */
    private MonthViewPager mMonthPager;

    /**
     * Calendar week view
     */
    private WeekViewPager mWeekPager;

    /**
     * Line of the day of the week
     */
    private View mWeekLine;

    /**
     * Quick selection of month
     */
    private YearViewPager mYearViewPager;

    /**
     * Day of the week
     */
    private WeekBar mWeekBar;

    /**
     * Calendar external shrink layout
     */
    CalendarLayout mParentLayout;


    public CalendarView(@NonNull Context context) {
        this(context, null);
    }

    public CalendarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mDelegate = new CalendarViewDelegate(context, attrs);
        init(context);
    }

    /**
     * initialization
     *
     * @param context context
     */
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cv_layout_calendar_view, this, true);
        FrameLayout frameContent = findViewById(R.id.frameContent);
        this.mWeekPager = findViewById(R.id.vp_week);
        this.mWeekPager.setup(mDelegate);

        try {
            Constructor constructor = mDelegate.getWeekBarClass().getConstructor(Context.class);
            mWeekBar = (WeekBar) constructor.newInstance(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frameContent.addView(mWeekBar, 2);
        mWeekBar.setup(mDelegate);
        mWeekBar.onWeekStartChange(mDelegate.getWeekStart());

        this.mWeekLine = findViewById(R.id.line);
        this.mWeekLine.setBackgroundColor(mDelegate.getWeekLineBackground());
        LayoutParams lineParams = (LayoutParams) this.mWeekLine.getLayoutParams();
        lineParams.setMargins(mDelegate.getWeekLineMargin(),
                mDelegate.getWeekBarHeight(),
                mDelegate.getWeekLineMargin(),
                0);
        this.mWeekLine.setLayoutParams(lineParams);

        this.mMonthPager = findViewById(R.id.vp_month);
        this.mMonthPager.mWeekPager = mWeekPager;
        this.mMonthPager.mWeekBar = mWeekBar;
        LayoutParams params = (LayoutParams) this.mMonthPager.getLayoutParams();
        params.setMargins(0, mDelegate.getWeekBarHeight() + CalendarUtil.dipToPx(context, 1), 0, 0);
        mWeekPager.setLayoutParams(params);


        mYearViewPager = findViewById(R.id.selectLayout);
        mYearViewPager.setBackgroundColor(mDelegate.getYearViewBackground());
        mYearViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mWeekPager.getVisibility() == VISIBLE) {
                    return;
                }
                if (mDelegate.mYearChangeListener != null) {
                    mDelegate.mYearChangeListener.onYearChange(position + mDelegate.getMinYear());
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mDelegate.mInnerListener = new OnInnerDateSelectedListener() {
            /**
             * Month view selection event
             * @param calendar calendar
             * @param isClick is it a click
             */
            @Override
            public void onMonthDateSelected(Calendar calendar, boolean isClick) {

                if (calendar.getYear() == mDelegate.getCurrentDay().getYear() &&
                        calendar.getMonth() == mDelegate.getCurrentDay().getMonth()
                        && mMonthPager.getCurrentItem() != mDelegate.mCurrentMonthViewItem) {
                    return;
                }
                mDelegate.mIndexCalendar = calendar;
                if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick) {
                    mDelegate.mSelectedCalendar = calendar;
                }
                mWeekPager.updateSelected(mDelegate.mIndexCalendar, false);
                mMonthPager.updateSelected();
                if (mWeekBar != null &&
                        (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick)) {
                    mWeekBar.onDateSelected(calendar, mDelegate.getWeekStart(), isClick);
                }
            }

            /**
             * Week view selection event
             * @param calendar calendar
             * @param isClick is it a click
             */
            @Override
            public void onWeekDateSelected(Calendar calendar, boolean isClick) {
                mDelegate.mIndexCalendar = calendar;
                if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT || isClick
                        || mDelegate.mIndexCalendar.equals(mDelegate.mSelectedCalendar)) {
                    mDelegate.mSelectedCalendar = calendar;
                }
                int y = calendar.getYear() - mDelegate.getMinYear();
                int position = 12 * y + mDelegate.mIndexCalendar.getMonth() - mDelegate.getMinYearMonth();
                mWeekPager.updateSingleSelect();
                mMonthPager.setCurrentItem(position, false);
                mMonthPager.updateSelected();
                if (mWeekBar != null &&
                        (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT
                                || isClick
                                || mDelegate.mIndexCalendar.equals(mDelegate.mSelectedCalendar))) {
                    mWeekBar.onDateSelected(calendar, mDelegate.getWeekStart(), isClick);
                }
            }
        };


        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            if (isInRange(mDelegate.getCurrentDay())) {
                mDelegate.mSelectedCalendar = mDelegate.createCurrentDate();
            } else {
                mDelegate.mSelectedCalendar = mDelegate.getMinRangeCalendar();
            }
        } else {
            mDelegate.mSelectedCalendar = new Calendar();
        }

        mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar;

        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.getWeekStart(), false);

        mMonthPager.setup(mDelegate);
        mMonthPager.setCurrentItem(mDelegate.mCurrentMonthViewItem);
        mYearViewPager.setOnMonthSelectedListener(new YearRecyclerView.OnMonthSelectedListener() {
            @Override
            public void onMonthSelected(int year, int month) {
                int position = 12 * (year - mDelegate.getMinYear()) + month - mDelegate.getMinYearMonth();
                closeSelectLayout(position);
                mDelegate.isShowYearSelectedLayout = false;
            }
        });
        mYearViewPager.setup(mDelegate);
        mWeekPager.updateSelected(mDelegate.createCurrentDate(), false);
    }

    /**
     * Set date range
     *
     * @param minYear minimum year
     * @param minYearMonth minimum year corresponds to month
     * @param minYearDay The minimum year corresponds to the day
     * @param maxYear maximum month
     * @param maxYearMonth The largest month corresponds to the month
     * @param maxYearDay The corresponding day of the largest month
     */
    public void setRange(int minYear, int minYearMonth, int minYearDay,
                         int maxYear, int maxYearMonth, int maxYearDay) {
        if (CalendarUtil.compareTo(minYear, minYearMonth, minYearDay,
                maxYear, maxYearMonth, maxYearDay) > 0) {
            return;
        }
        mDelegate.setRange(minYear, minYearMonth, minYearDay,
                maxYear, maxYearMonth, maxYearDay);
        mWeekPager.notifyDataSetChanged();
        mYearViewPager.notifyDataSetChanged();
        mMonthPager.notifyDataSetChanged();
        if (!isInRange(mDelegate.mSelectedCalendar)) {
            mDelegate.mSelectedCalendar = mDelegate.getMinRangeCalendar();
            mDelegate.updateSelectCalendarScheme();
            mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar;
        }
        mWeekPager.updateRange();
        mMonthPager.updateRange();
        mYearViewPager.updateRange();
    }

    /**
     * Get the day
     *
     * @return return today
     */
    public int getCurDay() {
        return mDelegate.getCurrentDay().getDay();
    }

    /**
     * Get this month
     *
     * @return return this month
     */
    public int getCurMonth() {
        return mDelegate.getCurrentDay().getMonth();
    }

    /**
     * Get this year
     *
     * @return returns this year
     */
    public int getCurYear() {
        return mDelegate.getCurrentDay().getYear();
    }


    /**
     * Open calendar year and month quick selection
     *
     * @param year
     */
    public void showYearSelectLayout(final int year) {
        showSelectLayout(year);
    }

    /**
     * Open calendar year and month quick selection
     * Please use showYearSelectLayout(final int year) instead, this is nothing, more and more standardized
     *
     * @param year
     */
    private void showSelectLayout(final int year) {
        if (mParentLayout != null && mParentLayout.mContentView != null) {
            if (!mParentLayout.isExpand()) {
                mParentLayout.expand();
                //return;
            }
        }
        mWeekPager.setVisibility(GONE);
        mDelegate.isShowYearSelectedLayout = true;
        if (mParentLayout != null) {
            mParentLayout.hideContentView();
        }
        mWeekBar.animate()
                .translationY(-mWeekBar.getHeight())
                .setInterpolator(new LinearInterpolator())
                .setDuration(260)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mWeekBar.setVisibility(GONE);
                        mYearViewPager.setVisibility(VISIBLE);
                        mYearViewPager.scrollToYear(year, false);
                        if (mParentLayout != null && mParentLayout.mContentView != null) {
                            mParentLayout.expand();
                        }
                    }
                });

        mMonthPager.animate()
                .scaleX(0)
                .scaleY(0)
                .setDuration(260)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mDelegate.mYearViewChangeListener != null) {
                            mDelegate.mYearViewChangeListener.onYearViewChange(false);
                        }
                    }
                });
    }


    /**
     * Year and month selection view is open
     *
     * @return true or false
     */
    public boolean isYearSelectLayoutVisible() {
        return mYearViewPager.getVisibility() == VISIBLE;
    }

    /**
     * Close the year and month view and select the layout
     */
    public void closeYearSelectLayout() {
        if (mYearViewPager.getVisibility() == GONE) {
            return;
        }
        int position = 12 * (mDelegate.mSelectedCalendar.getYear() - mDelegate.getMinYear()) +
                mDelegate.mSelectedCalendar.getMonth() - mDelegate.getMinYearMonth();
        closeSelectLayout(position);
        mDelegate.isShowYearSelectedLayout = false;
    }

    /**
     * Close the calendar layout and scroll to the specified position
     *
     * @param position A certain year
     */
    private void closeSelectLayout(final int position) {
        mYearViewPager.setVisibility(GONE);
        mWeekBar.setVisibility(VISIBLE);
        if (position == mMonthPager.getCurrentItem()) {
            if (mDelegate.mCalendarSelectListener != null &&
                    mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_SINGLE) {
                mDelegate.mCalendarSelectListener.onCalendarSelect(mDelegate.mSelectedCalendar, false);
            }
        } else {
            mMonthPager.setCurrentItem(position, false);
        }
        mWeekBar.animate()
                .translationY(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(280)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mWeekBar.setVisibility(VISIBLE);
                    }
                });
        mMonthPager.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(180)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mDelegate.mYearViewChangeListener != null) {
                            mDelegate.mYearViewChangeListener.onYearViewChange(true);
                        }
                        if (mParentLayout != null) {
                            mParentLayout.showContentView();
                            if (mParentLayout.isExpand()) {
                                mMonthPager.setVisibility(VISIBLE);
                            } else {
                                mWeekPager.setVisibility(VISIBLE);
                                mParentLayout.shrink();
                            }
                        } else {
                            mMonthPager.setVisibility(VISIBLE);
                        }
                        mMonthPager.clearAnimation();
                    }
                });
    }

    /**
     * Scroll to current
     */
    public void scrollToCurrent() {
        scrollToCurrent(false);
    }

    /**
     * Scroll to current
     *
     * @param smoothScroll smoothScroll
     */
    public void scrollToCurrent(boolean smoothScroll) {
        if (!isInRange(mDelegate.getCurrentDay())) {
            return;
        }
        Calendar calendar = mDelegate.createCurrentDate();
        if (mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, false);
            return;
        }
        mDelegate.mSelectedCalendar = mDelegate.createCurrentDate();
        mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar;
        mDelegate.updateSelectCalendarScheme();
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.getWeekStart(), false);
        if (mMonthPager.getVisibility() == VISIBLE) {
            mMonthPager.scrollToCurrent(smoothScroll);
            mWeekPager.updateSelected(mDelegate.mIndexCalendar, false);
        } else {
            mWeekPager.scrollToCurrent(smoothScroll);
        }
        mYearViewPager.scrollToYear(mDelegate.getCurrentDay().getYear(), smoothScroll);
    }


    /**
     * Scroll to next month
     */
    public void scrollToNext() {
        scrollToNext(false);
    }

    /**
     * Scroll to next month
     *
     * @param smoothScroll smoothScroll
     */
    public void scrollToNext(boolean smoothScroll) {
        if (isYearSelectLayoutVisible()) {
            mYearViewPager.setCurrentItem(mYearViewPager.getCurrentItem() + 1, smoothScroll);
        } else if (mWeekPager.getVisibility() == VISIBLE) {
            mWeekPager.setCurrentItem(mWeekPager.getCurrentItem() + 1, smoothScroll);
        } else {
            mMonthPager.setCurrentItem(mMonthPager.getCurrentItem() + 1, smoothScroll);
        }

    }

    /**
     * Scroll to the previous month
     */
    public void scrollToPre() {
        scrollToPre(false);
    }

    /**
     * Scroll to the previous month
     *
     * @param smoothScroll smoothScroll
     */
    public void scrollToPre(boolean smoothScroll) {
        if (isYearSelectLayoutVisible()) {
            mYearViewPager.setCurrentItem(mYearViewPager.getCurrentItem() - 1, smoothScroll);
        } else if (mWeekPager.getVisibility() == VISIBLE) {
            mWeekPager.setCurrentItem(mWeekPager.getCurrentItem() - 1, smoothScroll);
        } else {
            mMonthPager.setCurrentItem(mMonthPager.getCurrentItem() - 1, smoothScroll);
        }
    }

    /**
     * Scroll to selected calendar
     */
    public void scrollToSelectCalendar() {
        if (!mDelegate.mSelectedCalendar.isAvailable()) {
            return;
        }
        scrollToCalendar(mDelegate.mSelectedCalendar.getYear(),
                mDelegate.mSelectedCalendar.getMonth(),
                mDelegate.mSelectedCalendar.getDay(),
                false,
                true);
    }

    /**
     * Scroll to specified date
     *
     * @param year  year
     * @param month month
     * @param day   day
     */
    public void scrollToCalendar(int year, int month, int day) {
        scrollToCalendar(year, month, day, false, true);
    }

    /**
     * Scroll to specified date
     *
     * @param year         year
     * @param month        month
     * @param day          day
     * @param smoothScroll smoothScroll
     */
    public void scrollToCalendar(int year, int month, int day, boolean smoothScroll) {
        scrollToCalendar(year, month, day, smoothScroll, true);
    }

    /**
     * Scroll to specified date
     *
     * @param year           year
     * @param month          month
     * @param day            day
     * @param smoothScroll   smoothScroll
     * @param invokeListener Call date event
     */
    public void scrollToCalendar(int year, int month, int day, boolean smoothScroll, boolean invokeListener) {

        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        if (!calendar.isAvailable()) {
            return;
        }
        if (!isInRange(calendar)) {
            return;
        }
        if (mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, false);
            return;
        }

        if (mWeekPager.getVisibility() == VISIBLE) {
            mWeekPager.scrollToCalendar(year, month, day, smoothScroll, invokeListener);
        } else {
            mMonthPager.scrollToCalendar(year, month, day, smoothScroll, invokeListener);
        }
    }

    /**
     * Scroll to a certain year
     *
     * @param year Fast scrolling year
     */
    public void scrollToYear(int year) {
        scrollToYear(year, false);
    }

    /**
     * Scroll to a certain year
     *
     * @param year Fast scrolling year
     * @param smoothScroll smoothScroll
     */
    public void scrollToYear(int year, boolean smoothScroll) {
        if (mYearViewPager.getVisibility() != VISIBLE) {
            return;
        }
        mYearViewPager.scrollToYear(year, smoothScroll);
    }

    /**
     * Set whether the month view is scrollable
     *
     * @param monthViewScrollable sets whether the month view is scrollable
     */
    public final void setMonthViewScrollable(boolean monthViewScrollable) {
        mDelegate.setMonthViewScrollable(monthViewScrollable);
    }


    /**
     * Set whether the week view is scrollable
     *
     * @param weekViewScrollable sets whether the week view is scrollable
     */
    public final void setWeekViewScrollable(boolean weekViewScrollable) {
        mDelegate.setWeekViewScrollable(weekViewScrollable);
    }

    /**
     * Set whether the year view is scrollable
     *
     * @param yearViewScrollable sets whether the year view is scrollable
     */
    public final void setYearViewScrollable(boolean yearViewScrollable) {
        mDelegate.setYearViewScrollable(yearViewScrollable);
    }


    public final void setDefaultMonthViewSelectDay() {
        mDelegate.setDefaultCalendarSelectDay(CalendarViewDelegate.FIRST_DAY_OF_MONTH);
    }

    public final void setLastMonthViewSelectDay() {
        mDelegate.setDefaultCalendarSelectDay(CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY);
    }

    public final void setLastMonthViewSelectDayIgnoreCurrent() {
        mDelegate.setDefaultCalendarSelectDay(CalendarViewDelegate.LAST_MONTH_VIEW_SELECT_DAY_IGNORE_CURRENT);
    }

    /**
     * Clear selection
     */
    public final void clearSelectRange() {
        mDelegate.clearSelectRange();
        mMonthPager.clearSelectRange();
        mWeekPager.clearSelectRange();
    }

    /**
     * Clear single selection
     */
    public final void clearSingleSelect() {
        mDelegate.mSelectedCalendar = new Calendar();
        mMonthPager.clearSingleSelect();
        mWeekPager.clearSingleSelect();
    }

    /**
     * Clear multiple selection
     */
    public final void clearMultiSelect() {
        mDelegate.mSelectedCalendars.clear();
        mMonthPager.clearMultiSelect();
        mWeekPager.clearMultiSelect();
    }

    /**
     * Add selection
     *
     * @param calendars calendars
     */
    public final void putMultiSelect(Calendar... calendars) {
        if (calendars == null || calendars.length == 0) {
            return;
        }
        for (Calendar calendar : calendars) {
            if (calendar == null || mDelegate.mSelectedCalendars.containsKey(calendar.toString())) {
                continue;
            }
            mDelegate.mSelectedCalendars.put(calendar.toString(), calendar);
        }
        update();
    }

    /**
     * Clear some multiple choice dates
     *
     * @param calendars calendars
     */
    @SuppressWarnings("RedundantCollectionOperation")
    public final void removeMultiSelect(Calendar... calendars) {
        if (calendars == null || calendars.length == 0) {
            return;
        }
        for (Calendar calendar : calendars) {
            if (calendar == null) {
                continue;
            }
            if (mDelegate.mSelectedCalendars.containsKey(calendar.toString())) {
                mDelegate.mSelectedCalendars.remove(calendar.toString());
            }
        }
        update();
    }


    public final List<Calendar> getMultiSelectCalendars() {
        List<Calendar> calendars = new ArrayList<>();
        if (mDelegate.mSelectedCalendars.size() == 0) {
            return calendars;
        }
        calendars.addAll(mDelegate.mSelectedCalendars.values());
        Collections.sort(calendars);
        return calendars;
    }

    /**
     * Get selected range
     *
     * @return return
     */
    public final List<Calendar> getSelectCalendarRange() {
        return mDelegate.getSelectCalendarRange();
    }

    /**
     * Set the height of the month view item
     *
     * @param calendarItemHeight MonthView item height
     */
    public final void setCalendarItemHeight(int calendarItemHeight) {
        if (mDelegate.getCalendarItemHeight() == calendarItemHeight) {
            return;
        }
        mDelegate.setCalendarItemHeight(calendarItemHeight);
        mMonthPager.updateItemHeight();
        mWeekPager.updateItemHeight();
        if (mParentLayout == null) {
            return;
        }
        mParentLayout.updateCalendarItemHeight();
    }


    /**
     * Set month view
     *
     * @param cls MonthView.class
     */
    public final void setMonthView(Class<?> cls) {
        if (cls == null) {
            return;
        }
        if (mDelegate.getMonthViewClass().equals(cls)) {
            return;
        }
        mDelegate.setMonthViewClass(cls);
        mMonthPager.updateMonthViewClass();
    }

    /**
     * Set up week view
     *
     * @param cls WeekView.class
     */
    public final void setWeekView(Class<?> cls) {
        if (cls == null) {
            return;
        }
        if (mDelegate.getWeekBarClass().equals(cls)) {
            return;
        }
        mDelegate.setWeekViewClass(cls);
        mWeekPager.updateWeekViewClass();
    }

    /**
     * Set the week bar view
     *
     * @param cls WeekBar.class
     */
    public final void setWeekBar(Class<?> cls) {
        if (cls == null) {
            return;
        }
        if (mDelegate.getWeekBarClass().equals(cls)) {
            return;
        }
        mDelegate.setWeekBarClass(cls);
        FrameLayout frameContent = findViewById(R.id.frameContent);
        frameContent.removeView(mWeekBar);

        try {
            Constructor constructor = cls.getConstructor(Context.class);
            mWeekBar = (WeekBar) constructor.newInstance(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        frameContent.addView(mWeekBar, 2);
        mWeekBar.setup(mDelegate);
        mWeekBar.onWeekStartChange(mDelegate.getWeekStart());
        this.mMonthPager.mWeekBar = mWeekBar;
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.getWeekStart(), false);
    }


    /**
     * Added date interception event
     * Use this method, only based on select_mode = single_mode
     * Otherwise, it is meaningless to mark all dates as non-clickable.
     * It is impossible for the frame itself to judge the clickability of each date during the sliding process
     *
     * @param listener listener
     */
    public final void setOnCalendarInterceptListener(OnCalendarInterceptListener listener) {
        if (listener == null) {
            mDelegate.mCalendarInterceptListener = null;
        }
        if (listener == null || mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return;
        }
        mDelegate.mCalendarInterceptListener = listener;
        if (!listener.onCalendarIntercept(mDelegate.mSelectedCalendar)) {
            return;
        }
        mDelegate.mSelectedCalendar = new Calendar();
    }

    /**
     * Year change event
     *
     * @param listener listener
     */
    public void setOnYearChangeListener(OnYearChangeListener listener) {
        this.mDelegate.mYearChangeListener = listener;
    }

    /**
     * Month change event
     *
     * @param listener listener
     */
    public void setOnMonthChangeListener(OnMonthChangeListener listener) {
        this.mDelegate.mMonthChangeListener = listener;
    }


    /**
     * Weekly view switching monitoring
     *
     * @param listener listener
     */
    public void setOnWeekChangeListener(OnWeekChangeListener listener) {
        this.mDelegate.mWeekChangeListener = listener;
    }

    /**
     * Date selection event
     *
     * @param listener listener
     */
    public void setOnCalendarSelectListener(OnCalendarSelectListener listener) {
        this.mDelegate.mCalendarSelectListener = listener;
        if (mDelegate.mCalendarSelectListener == null) {
            return;
        }
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return;
        }
        if (!isInRange(mDelegate.mSelectedCalendar)) {
            return;
        }
        mDelegate.updateSelectCalendarScheme();
    }


    /**
     * Date selection event
     *
     * @param listener listener
     */
    public final void setOnCalendarRangeSelectListener(OnCalendarRangeSelectListener listener) {
        this.mDelegate.mCalendarRangeSelectListener = listener;
    }

    /**
     * Date multiple selection event
     *
     * @param listener listener
     */
    public final void setOnCalendarMultiSelectListener(OnCalendarMultiSelectListener listener) {
        this.mDelegate.mCalendarMultiSelectListener = listener;
    }

    /**
     * Set the minimum range and maximum access, default: minRange = -1, maxRange = -1 no limit
     *
     * @param minRange minRange
     * @param maxRange maxRange
     */
    public final void setSelectRange(int minRange, int maxRange) {
        if (minRange > maxRange) {
            return;
        }
        mDelegate.setSelectRange(minRange, maxRange);
    }


    public final void setSelectStartCalendar(int startYear, int startMonth, int startDay) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        Calendar startCalendar = new Calendar();
        startCalendar.setYear(startYear);
        startCalendar.setMonth(startMonth);
        startCalendar.setDay(startDay);
        setSelectStartCalendar(startCalendar);
    }

    public final void setSelectStartCalendar(Calendar startCalendar) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        if (startCalendar == null) {
            return;
        }
        if (!isInRange(startCalendar)) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(startCalendar, true);
            }
            return;
        }
        if (onCalendarIntercept(startCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(startCalendar, false);
            }
            return;
        }
        mDelegate.mSelectedEndRangeCalendar = null;
        mDelegate.mSelectedStartRangeCalendar = startCalendar;
        scrollToCalendar(startCalendar.getYear(), startCalendar.getMonth(), startCalendar.getDay());
    }

    public final void setSelectEndCalendar(int endYear, int endMonth, int endDay) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        if (mDelegate.mSelectedStartRangeCalendar == null) {
            return;
        }
        Calendar endCalendar = new Calendar();
        endCalendar.setYear(endYear);
        endCalendar.setMonth(endMonth);
        endCalendar.setDay(endDay);
        setSelectEndCalendar(endCalendar);
    }

    public final void setSelectEndCalendar(Calendar endCalendar) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        if (mDelegate.mSelectedStartRangeCalendar == null) {
            return;
        }
        setSelectCalendarRange(mDelegate.mSelectedStartRangeCalendar, endCalendar);
    }

    /**
     * Specify the selection range directly，set select calendar range
     *
     * @param startYear  startYear
     * @param startMonth startMonth
     * @param startDay   startDay
     * @param endYear    endYear
     * @param endMonth   endMonth
     * @param endDay     endDay
     */
    public final void setSelectCalendarRange(int startYear, int startMonth, int startDay,
                                             int endYear, int endMonth, int endDay) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        Calendar startCalendar = new Calendar();
        startCalendar.setYear(startYear);
        startCalendar.setMonth(startMonth);
        startCalendar.setDay(startDay);

        Calendar endCalendar = new Calendar();
        endCalendar.setYear(endYear);
        endCalendar.setMonth(endMonth);
        endCalendar.setDay(endDay);
        setSelectCalendarRange(startCalendar, endCalendar);
    }

    /**
     * Set selection date range
     *
     * @param startCalendar startCalendar
     * @param endCalendar   endCalendar
     */
    public final void setSelectCalendarRange(Calendar startCalendar, Calendar endCalendar) {
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        if (startCalendar == null || endCalendar == null) {
            return;
        }
        if (onCalendarIntercept(startCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(startCalendar, false);
            }
            return;
        }
        if (onCalendarIntercept(endCalendar)) {
            if (mDelegate.mCalendarInterceptListener != null) {
                mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(endCalendar, false);
            }
            return;
        }
        int minDiffer = endCalendar.differ(startCalendar);
        if (minDiffer < 0) {
            return;
        }
        if (!isInRange(startCalendar) || !isInRange(endCalendar)) {
            return;
        }


        //Prioritize various direct return situations to reduce code depth
        if (mDelegate.getMinSelectRange() != -1 && mDelegate.getMinSelectRange() > minDiffer + 1) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(endCalendar, true);
            }
            return;
        } else if (mDelegate.getMaxSelectRange() != -1 && mDelegate.getMaxSelectRange() <
                minDiffer + 1) {
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(endCalendar, false);
            }
            return;
        }
        if (mDelegate.getMinSelectRange() == -1 && minDiffer == 0) {
            mDelegate.mSelectedStartRangeCalendar = startCalendar;
            mDelegate.mSelectedEndRangeCalendar = null;
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(startCalendar, false);
            }
            scrollToCalendar(startCalendar.getYear(), startCalendar.getMonth(), startCalendar.getDay());
            return;
        }

        mDelegate.mSelectedStartRangeCalendar = startCalendar;
        mDelegate.mSelectedEndRangeCalendar = endCalendar;
        if (mDelegate.mCalendarRangeSelectListener != null) {
            mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(startCalendar, false);
            mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(endCalendar, true);
        }
        scrollToCalendar(startCalendar.getYear(), startCalendar.getMonth(), startCalendar.getDay());
    }

    /**
     * Whether to intercept the date, this setting continues to set mCalendarInterceptListener
     *
     * @param calendar calendar
     * @return Whether to block date
     */
    protected final boolean onCalendarIntercept(Calendar calendar) {
        return mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar);
    }


    /**
     * Get the maximum number of multiple choices
     *
     * @return Get the maximum number of multiple choices
     */
    public final int getMaxMultiSelectSize() {
        return mDelegate.getMaxMultiSelectSize();
    }

    /**
     * Set the maximum number of multiple selections
     *
     * @param maxMultiSelectSize Maximum number of multiple choices
     */
    public final void setMaxMultiSelectSize(int maxMultiSelectSize) {
        mDelegate.setMaxMultiSelectSize(maxMultiSelectSize);
    }

    /**
     * Minimum selection range
     *
     * @return Minimum selection range
     */
    public final int getMinSelectRange() {
        return mDelegate.getMinSelectRange();
    }

    /**
     * Maximum selection range
     *
     * @return Maximum selection range
     */
    public final int getMaxSelectRange() {
        return mDelegate.getMaxSelectRange();
    }

    /**
     * Date long press event
     *
     * @param listener listener
     */
    public void setOnCalendarLongClickListener(OnCalendarLongClickListener listener) {
        this.mDelegate.mCalendarLongClickListener = listener;
    }

    /**
     * Date long press event
     *
     * @param preventLongPressedSelect Prevent long press to select date
     * @param listener                 listener
     */
    public void setOnCalendarLongClickListener(OnCalendarLongClickListener listener, boolean preventLongPressedSelect) {
        this.mDelegate.mCalendarLongClickListener = listener;
        this.mDelegate.setPreventLongPressedSelected(preventLongPressedSelect);
    }

    /**
     * View change event
     *
     * @param listener listener
     */
    public void setOnViewChangeListener(OnViewChangeListener listener) {
        this.mDelegate.mViewChangeListener = listener;
    }


    public void setOnYearViewChangeListener(OnYearViewChangeListener listener) {
        this.mDelegate.mYearViewChangeListener = listener;
    }

    /**
     * On hold
     *
     * @return status
     */
    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        if (mDelegate == null) {
            return super.onSaveInstanceState();
        }
        Bundle bundle = new Bundle();
        Parcelable parcelable = super.onSaveInstanceState();
        bundle.putParcelable("super", parcelable);
        bundle.putSerializable("selected_calendar", mDelegate.mSelectedCalendar);
        bundle.putSerializable("index_calendar", mDelegate.mIndexCalendar);
        return bundle;
    }

    /**
     * Recovery state
     *
     * @param state status
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superData = bundle.getParcelable("super");
        mDelegate.mSelectedCalendar = (Calendar) bundle.getSerializable("selected_calendar");
        mDelegate.mIndexCalendar = (Calendar) bundle.getSerializable("index_calendar");
        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(mDelegate.mSelectedCalendar, false);
        }
        if (mDelegate.mIndexCalendar != null) {
            scrollToCalendar(mDelegate.mIndexCalendar.getYear(),
                    mDelegate.mIndexCalendar.getMonth(),
                    mDelegate.mIndexCalendar.getDay());
        }
        update();
        super.onRestoreInstanceState(superData);
    }


    /**
     * Initialize the calendar card default selection position during initialization
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getParent() != null && getParent() instanceof CalendarLayout) {
            mParentLayout = (CalendarLayout) getParent();
            mMonthPager.mParentLayout = mParentLayout;
            mWeekPager.mParentLayout = mParentLayout;
            mParentLayout.mWeekBar = mWeekBar;
            mParentLayout.setup(mDelegate);
            mParentLayout.initStatus();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mDelegate == null ||
                !mDelegate.isFullScreenCalendar()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        setCalendarItemHeight((height -
                mDelegate.getWeekBarHeight()) / 6);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Mark which dates have events
     *
     * @param mSchemeDates mSchemeDatesMap can be converted according to your own needs
     */
    public final void setSchemeDate(Map<String, Calendar> mSchemeDates) {
        this.mDelegate.mSchemeDatesMap = mSchemeDates;
        this.mDelegate.updateSelectCalendarScheme();
        this.mYearViewPager.update();
        this.mMonthPager.updateScheme();
        this.mWeekPager.updateScheme();
    }

    /**
     * Clear date stamp
     */
    public final void clearSchemeDate() {
        this.mDelegate.mSchemeDatesMap = null;
        this.mDelegate.clearSelectedScheme();
        mYearViewPager.update();
        mMonthPager.updateScheme();
        mWeekPager.updateScheme();
    }

    /**
     * Add thing tag
     *
     * @param calendar calendar
     */
    public final void addSchemeDate(Calendar calendar) {
        if (calendar == null || !calendar.isAvailable()) {
            return;
        }
        if (mDelegate.mSchemeDatesMap == null) {
            mDelegate.mSchemeDatesMap = new HashMap<>();
        }
        mDelegate.mSchemeDatesMap.remove(calendar.toString());
        mDelegate.mSchemeDatesMap.put(calendar.toString(), calendar);
        this.mDelegate.updateSelectCalendarScheme();
        this.mYearViewPager.update();
        this.mMonthPager.updateScheme();
        this.mWeekPager.updateScheme();
    }

    /**
     * Add thing tag
     *
     * @param mSchemeDates mSchemeDates
     */
    public final void addSchemeDate(Map<String, Calendar> mSchemeDates) {
        if (this.mDelegate == null || mSchemeDates == null || mSchemeDates.size() == 0) {
            return;
        }
        if (this.mDelegate.mSchemeDatesMap == null) {
            this.mDelegate.mSchemeDatesMap = new HashMap<>();
        }
        this.mDelegate.addSchemes(mSchemeDates);
        this.mDelegate.updateSelectCalendarScheme();
        this.mYearViewPager.update();
        this.mMonthPager.updateScheme();
        this.mWeekPager.updateScheme();
    }

    /**
     * Remove the mark of a certain day
     * This API is safe
     *
     * @param calendar calendar
     */
    public final void removeSchemeDate(Calendar calendar) {
        if (calendar == null) {
            return;
        }
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap.size() == 0) {
            return;
        }
        mDelegate.mSchemeDatesMap.remove(calendar.toString());
        if (mDelegate.mSelectedCalendar.equals(calendar)) {
            mDelegate.clearSelectedScheme();
        }

        mYearViewPager.update();
        mMonthPager.updateScheme();
        mWeekPager.updateScheme();
    }

    /**
     * Set background color
     *
     * @param yearViewBackground the background color of the year card
     * @param weekBackground week bar background color
     * @param lineBg line color
     */
    public void setBackground(int yearViewBackground, int weekBackground, int lineBg) {
        mWeekBar.setBackgroundColor(weekBackground);
        mYearViewPager.setBackgroundColor(yearViewBackground);
        mWeekLine.setBackgroundColor(lineBg);
    }


    /**
     * Set text color
     *
     * @param currentDayTextColor today's font color
     * @param curMonthTextColor current month font color
     * @param otherMonthColor Other month font color
     * @param curMonthLunarTextColor current month lunar calendar font color
     * @param otherMonthLunarTextColor Other lunar font color
     */
    public void setTextColor(
            int currentDayTextColor,
            int curMonthTextColor,
            int otherMonthColor,
            int curMonthLunarTextColor,
            int otherMonthLunarTextColor) {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        mDelegate.setTextColor(currentDayTextColor, curMonthTextColor,
                otherMonthColor, curMonthLunarTextColor, otherMonthLunarTextColor);
        mMonthPager.updateStyle();
        mWeekPager.updateStyle();
    }

    /**
     * Set the selected effect
     *
     * @param selectedThemeColor selected marker color
     * @param selectedTextColor selected font color
     * @param selectedLunarTextColor selected lunar font color
     */
    public void setSelectedColor(int selectedThemeColor, int selectedTextColor, int selectedLunarTextColor) {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        mDelegate.setSelectColor(selectedThemeColor, selectedTextColor, selectedLunarTextColor);
        mMonthPager.updateStyle();
        mWeekPager.updateStyle();
    }

    /**
     * Custom colors
     *
     * @param selectedThemeColor selected marker color
     * @param schemeColor mark the background color
     */
    public void setThemeColor(int selectedThemeColor, int schemeColor) {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        mDelegate.setThemeColor(selectedThemeColor, schemeColor);
        mMonthPager.updateStyle();
        mWeekPager.updateStyle();
    }

    /**
     * Set the color of the mark
     *
     * @param schemeLunarTextColor mark the lunar color
     * @param schemeColor mark the background color
     * @param schemeTextColor mark font color
     */
    public void setSchemeColor(int schemeColor, int schemeTextColor, int schemeLunarTextColor) {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        mDelegate.setSchemeColor(schemeColor, schemeTextColor, schemeLunarTextColor);
        mMonthPager.updateStyle();
        mWeekPager.updateStyle();
    }

    /**
     * Set the color of the year view
     *
     * @param yearViewMonthTextColor year view month color
     * @param yearViewDayTextColor year view day color
     * @param yarViewSchemeTextColor year view mark color
     */
    public void setYearViewTextColor(int yearViewMonthTextColor, int yearViewDayTextColor, int yarViewSchemeTextColor) {
        if (mDelegate == null || mYearViewPager == null) {
            return;
        }
        mDelegate.setYearViewTextColor(yearViewMonthTextColor, yearViewDayTextColor, yarViewSchemeTextColor);
        mYearViewPager.updateStyle();
    }

    /**
     * Set the background and font color of the weekday bar
     *
     * @param weekBackground background color
     * @param weekTextColor font color
     */
    public void setWeeColor(int weekBackground, int weekTextColor) {
        if (mWeekBar == null) {
            return;
        }
        mWeekBar.setBackgroundColor(weekBackground);
        mWeekBar.setTextColor(weekTextColor);
    }

    /**
     * Default selection mode
     */
    public final void setSelectDefaultMode() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            return;
        }
        mDelegate.mSelectedCalendar = mDelegate.mIndexCalendar;
        mDelegate.setSelectMode(CalendarViewDelegate.SELECT_MODE_DEFAULT);
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.getWeekStart(), false);
        mMonthPager.updateDefaultSelect();
        mWeekPager.updateDefaultSelect();

    }

    /**
     * Range mode
     */
    public void setSelectRangeMode() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_RANGE) {
            return;
        }
        mDelegate.setSelectMode(CalendarViewDelegate.SELECT_MODE_RANGE);
        clearSelectRange();
    }

    /**
     * Multiple selection mode
     */
    public void setSelectMultiMode() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_MULTI) {
            return;
        }
        mDelegate.setSelectMode(CalendarViewDelegate.SELECT_MODE_MULTI);
        clearMultiSelect();
    }

    /**
     * Single selection mode
     */
    public void setSelectSingleMode() {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_SINGLE) {
            return;
        }
        mDelegate.setSelectMode(CalendarViewDelegate.SELECT_MODE_SINGLE);
        mWeekPager.updateSelected();
        mMonthPager.updateSelected();
    }

    /**
     * Set the start of the week on Sunday
     */
    public void setWeekStarWithSun() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SUN);
    }

    /**
     * Set the start of the week on Monday
     */
    public void setWeekStarWithMon() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_MON);
    }

    /**
     * Set the start of the week on Saturday
     */
    public void setWeekStarWithSat() {
        setWeekStart(CalendarViewDelegate.WEEK_START_WITH_SAT);
    }

    /**
     * Set start of week
     * CalendarViewDelegate.WEEK_START_WITH_SUN
     * CalendarViewDelegate.WEEK_START_WITH_MON
     * CalendarViewDelegate.WEEK_START_WITH_SAT
     *
     * @param weekStart Week start
     */
    private void setWeekStart(int weekStart) {
        if (weekStart != CalendarViewDelegate.WEEK_START_WITH_SUN &&
                weekStart != CalendarViewDelegate.WEEK_START_WITH_MON &&
                weekStart != CalendarViewDelegate.WEEK_START_WITH_SAT)
            return;
        if (weekStart == mDelegate.getWeekStart())
            return;
        mDelegate.setWeekStart(weekStart);
        mWeekBar.onWeekStartChange(weekStart);
        mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, weekStart, false);
        mWeekPager.updateWeekStart();
        mMonthPager.updateWeekStart();
        mYearViewPager.updateWeekStart();
    }

    /**
     * Whether it is single-select mode
     *
     * @return isSingleSelectMode
     */
    public boolean isSingleSelectMode() {
        return mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_SINGLE;
    }

    /**
     * Set the display mode to all
     */
    public void setAllMode() {
        setShowMode(CalendarViewDelegate.MODE_ALL_MONTH);
    }

    /**
     * Set display mode to current month only
     */
    public void setOnlyCurrentMode() {
        setShowMode(CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH);
    }

    /**
     * Set the display mode to fill
     */
    public void setFixMode() {
        setShowMode(CalendarViewDelegate.MODE_FIT_MONTH);
    }

    /**
     * Set display mode
     * CalendarViewDelegate.MODE_ALL_MONTH
     * CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH
     * CalendarViewDelegate.MODE_FIT_MONTH
     *
     * @param mode Month view display mode
     */
    private void setShowMode(int mode) {
        if (mode != CalendarViewDelegate.MODE_ALL_MONTH &&
                mode != CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH &&
                mode != CalendarViewDelegate.MODE_FIT_MONTH)
            return;
        if (mDelegate.getMonthViewShowMode() == mode)
            return;
        mDelegate.setMonthViewShowMode(mode);
        mWeekPager.updateShowMode();
        mMonthPager.updateShowMode();
        mWeekPager.notifyDataSetChanged();
    }

    /**
     * Update the interface,
     * You need to call this method to reset the color, etc.
     */
    public final void update() {
        mWeekBar.onWeekStartChange(mDelegate.getWeekStart());
        mYearViewPager.update();
        mMonthPager.updateScheme();
        mWeekPager.updateScheme();
    }

    /**
     * Update week view
     */
    public void updateWeekBar() {
        mWeekBar.onWeekStartChange(mDelegate.getWeekStart());
    }


    /**
     * Update current date
     */
    public final void updateCurrentDate() {
        if (mDelegate == null || mMonthPager == null || mWeekPager == null) {
            return;
        }
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        if (getCurDay() == day) {
            return;
        }
        mDelegate.updateCurrentDay();
        mMonthPager.updateCurrentDate();
        mWeekPager.updateCurrentDate();
    }

    /**
     * Get current week data
     *
     * @return Get current week data
     */
    public List<Calendar> getCurrentWeekCalendars() {
        return mWeekPager.getCurrentWeekCalendars();
    }


    /**
     * Get the current month date
     *
     * @return return
     */
    public List<Calendar> getCurrentMonthCalendars() {
        return mMonthPager.getCurrentMonthCalendars();
    }

    /**
     * Get the selected date
     *
     * @return Get the selected date
     */
    public Calendar getSelectedCalendar() {
        return mDelegate.mSelectedCalendar;
    }

    /**
     * Get the minimum range date
     *
     * @return minimum range date
     */
    public Calendar getMinRangeCalendar() {
        return mDelegate.getMinRangeCalendar();
    }


    /**
     * Get the maximum range date
     *
     * @return Maximum range date
     */
    public Calendar getMaxRangeCalendar() {
        return mDelegate.getMaxRangeCalendar();
    }

    /**
     * MonthViewPager
     *
     * @return Get month view
     */
    public MonthViewPager getMonthViewPager() {
        return mMonthPager;
    }

    /**
     * Get weekly view
     *
     * @return get the week view
     */
    public WeekViewPager getWeekViewPager() {
        return mWeekPager;
    }

    /**
     * Is it within the date range
     *
     * @param calendar calendar
     * @return is within the date range
     */
    protected final boolean isInRange(Calendar calendar) {
        return mDelegate != null && CalendarUtil.isCalendarInRange(calendar, mDelegate);
    }


    /**
     * Year view switching event, fast year switching
     */
    public interface OnYearChangeListener {
        void onYearChange(int year);
    }

    /**
     * Month switching event
     */
    public interface OnMonthChangeListener {
        void onMonthChange(int year, int month);
    }


    /**
     * Weekly view switching event
     */
    public interface OnWeekChangeListener {
        void onWeekChange(List<Calendar> weekCalendars);
    }

    /**
     * Internal date selection, does not expose external use
     * Mainly used to update the CalendarLayout position
     */
    interface OnInnerDateSelectedListener {
        /**
         * Click on month view
         *
         * @param calendar calendar
         * @param isClick is it a click
         */
        void onMonthDateSelected(Calendar calendar, boolean isClick);

        /**
         * Weekly view click
         *
         * @param calendar calendar
         * @param isClick is it a click
         */
        void onWeekDateSelected(Calendar calendar, boolean isClick);
    }


    /**
     * Calendar range selection event
     */
    public interface OnCalendarRangeSelectListener {

        /**
         * Range selection is out of range
         *
         * @param calendar calendar
         */
        void onCalendarSelectOutOfRange(Calendar calendar);

        /**
         * Selection range is out of range
         *
         * @param calendar calendar
         * @param isOutOfMinRange is it smaller than the minimum range, otherwise the maximum range
         */
        void onSelectOutOfRange(Calendar calendar, boolean isOutOfMinRange);

        /**
         * Date selection event
         *
         * @param calendar calendar
         * @param isEnd Whether to end
         */
        void onCalendarRangeSelect(Calendar calendar, boolean isEnd);
    }


    /**
     * Calendar multi-select events
     */
    public interface OnCalendarMultiSelectListener {

        /**
         * Multiple selection out of range
         *
         * @param calendar calendar
         */
        void onCalendarMultiSelectOutOfRange(Calendar calendar);

        /**
         * Multiple selection exceeds size
         *
         * @param maxSize maximum size
         * @param calendar calendar
         */
        void onMultiSelectOutOfSize(Calendar calendar, int maxSize);

        /**
         * Multiple choice event
         *
         * @param calendar calendar
         * @param curSize  curSize
         * @param maxSize  maxSize
         */
        void onCalendarMultiSelect(Calendar calendar, int curSize, int maxSize);
    }

    /**
     * Calendar selection event
     */
    public interface OnCalendarSelectListener {

        /**
         * Out of range
         *
         * @param calendar calendar
         */
        void onCalendarOutOfRange(Calendar calendar);

        /**
         * Date selection event
         *
         * @param calendar calendar
         * @param isClick  isClick
         */
        void onCalendarSelect(Calendar calendar, boolean isClick);
    }

    public interface OnCalendarLongClickListener {

        /**
         * Out of range
         *
         * @param calendar calendar
         */
        void onCalendarLongClickOutOfRange(Calendar calendar);

        /**
         * Date long press event
         *
         * @param calendar calendar
         */
        void onCalendarLongClick(Calendar calendar);
    }

    /**
     * View change event
     */
    public interface OnViewChangeListener {
        /**
         * View change event
         *
         * @param isMonthView isMonthView is a month view
         */
        void onViewChange(boolean isMonthView);
    }

    /**
     * Year view change event
     */
    public interface OnYearViewChangeListener {
        /**
         * Yearly view changes
         *
         * @param isClose whether to close
         */
        void onYearViewChange(boolean isClose);
    }

    /**
     * Is the intercept date available?
     */
    public interface OnCalendarInterceptListener {
        boolean onCalendarIntercept(Calendar calendar);

        void onCalendarInterceptClick(Calendar calendar, boolean isClick);
    }
}
