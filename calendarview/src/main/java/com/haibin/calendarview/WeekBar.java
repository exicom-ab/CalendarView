package com.haibin.calendarview;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Week column, if you want to use the week column to customize, remember to use merge for XML instead of LinearLayout
 */
public class WeekBar extends LinearLayout {
    private CalendarViewDelegate mDelegate;

    public WeekBar(Context context) {
        super(context);
        if ("com.haibin.calendarview.WeekBar".equals(getClass().getName())) {
            LayoutInflater.from(context).inflate(R.layout.cv_week_bar, this, true);
        }
    }

    /**
     * Transfer attributes
     *
     * @param delegate delegate
     */
    void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        if ("com.haibin.calendarview.WeekBar".equalsIgnoreCase(getClass().getName())) {
            setTextSize(mDelegate.getWeekTextSize());
            setTextColor(delegate.getWeekTextColor());
            setBackgroundColor(delegate.getWeekBackground());
            setPadding(delegate.getCalendarPadding(), 0, delegate.getCalendarPadding(), 0);
        }
    }

    /**
     * Setting the text color, using a custom layout needs to rewrite this method to avoid problems
     * If an error is reported here, please make sure your custom XML file and layout use merge instead of LinearLayout
     *
     * @param color color
     */
    protected void setTextColor(int color) {
        for (int i = 0; i < getChildCount(); i++) {
            ((TextView) getChildAt(i)).setTextColor(color);
        }
    }


    /**
     * Set text size
     *
     * @param size size
     */
    protected void setTextSize(int size) {
        for (int i = 0; i < getChildCount(); i++) {
            ((TextView) getChildAt(i)).setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
    }

    /**
     * Date selection event, this callback is provided here, you can easily customize WeekBar needs
     *
     * @param calendar  calendar Selected date
     * @param weekStart Week start
     * @param isClick   isClick Click on
     */
    protected void onDateSelected(Calendar calendar, int weekStart, boolean isClick) {

    }

    /**
     * When the beginning of the week changes, using a custom layout needs to rewrite this method to avoid problems
     *
     * @param weekStart Week start
     */
    protected void onWeekStartChange(int weekStart) {
        if (!"com.haibin.calendarview.WeekBar".equalsIgnoreCase(getClass().getName())) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            ((TextView) getChildAt(i)).setText(getWeekString(i, weekStart));
        }
    }


    /**
     * Get the corresponding coordinates of the week through the position of the View and the start of the week
     *
     * @param calendar  calendar
     * @param weekStart weekStart
     * @return Get the corresponding coordinates of the week through the position of the View and the start of the week
     */
    protected int getViewIndexByCalendar(Calendar calendar, int weekStart) {
        int week = calendar.getWeek() + 1;
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return week - 1;
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return week == CalendarViewDelegate.WEEK_START_WITH_SUN ? 6 : week - 2;
        }
        return week == CalendarViewDelegate.WEEK_START_WITH_SAT ? 0 : week;
    }

    /**
     * Or week text, this method is only for the parent class
     *
     * @param index     index
     * @param weekStart weekStart
     * @return Or week text
     */
    private String getWeekString(int index, int weekStart) {
        String[] weeks = getContext().getResources().getStringArray(R.array.week_string_array);

        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_SUN) {
            return weeks[index];
        }
        if (weekStart == CalendarViewDelegate.WEEK_START_WITH_MON) {
            return weeks[index == 6 ? 0 : index + 1];
        }
        return weeks[index == 0 ? 6 : index - 1];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mDelegate != null) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mDelegate.getWeekBarHeight(), MeasureSpec.EXACTLY);
        } else {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(CalendarUtil.dipToPx(getContext(), 40), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
