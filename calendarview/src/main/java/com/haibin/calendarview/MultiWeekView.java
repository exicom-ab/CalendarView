package com.haibin.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * Multiple selection week view
 */
public abstract class MultiWeekView extends BaseWeekView {

    public MultiWeekView(Context context) {
        super(context);
    }

    /**
     * Draw calendar text
     *
     * @param canvas canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (mItems.size() == 0)
            return;
        mItemWidth = (getWidth() - 2 * mDelegate.getCalendarPadding()) / 7;
        onPreviewHook();

        for (int i = 0; i < 7; i++) {
            int x = i * mItemWidth + mDelegate.getCalendarPadding();
            onLoopStart(x);
            Calendar calendar = mItems.get(i);
            boolean isSelected = isCalendarSelected(calendar);
            boolean isPreSelected = isSelectPreCalendar(calendar);
            boolean isNextSelected = isSelectNextCalendar(calendar);
            boolean hasScheme = calendar.hasScheme();
            if (hasScheme) {
                boolean isDrawSelected = false;//Whether to continue drawing the selected onDrawScheme
                if (isSelected) {
                    isDrawSelected = onDrawSelected(canvas, calendar, x, true, isPreSelected, isNextSelected);
                }
                if (isDrawSelected || !isSelected) {
                    //Set the brush as the marker color
                    mSchemePaint.setColor(calendar.getSchemeColor() != 0 ? calendar.getSchemeColor() : mDelegate.getSchemeThemeColor());
                    onDrawScheme(canvas, calendar, x, isSelected);
                }
            } else {
                if (isSelected) {
                    onDrawSelected(canvas, calendar, x, false, isPreSelected, isNextSelected);
                }
            }
            onDrawText(canvas, calendar, x, hasScheme, isSelected);
        }
    }


    /**
     * Whether the calendar is selected
     *
     * @param calendar calendar
     * @return whether the calendar is selected
     */
    protected boolean isCalendarSelected(Calendar calendar) {
        return !onCalendarIntercept(calendar) && mDelegate.mSelectedCalendars.containsKey(calendar.toString());
    }

    @Override
    public void onClick(View v) {
        if (!isClick) {
            return;
        }
        Calendar calendar = getIndex();
        if (calendar == null) {
            return;
        }
        if (onCalendarIntercept(calendar)) {
            mDelegate.mCalendarInterceptListener.onCalendarInterceptClick(calendar, true);
            return;
        }
        if (!isInRange(calendar)) {
            if (mDelegate.mCalendarMultiSelectListener != null) {
                mDelegate.mCalendarMultiSelectListener.onCalendarMultiSelectOutOfRange(calendar);
            }
            return;
        }


        String key = calendar.toString();

        if (mDelegate.mSelectedCalendars.containsKey(key)) {
            mDelegate.mSelectedCalendars.remove(key);
        } else {
            if (mDelegate.mSelectedCalendars.size() >= mDelegate.getMaxMultiSelectSize()) {
                if (mDelegate.mCalendarMultiSelectListener != null) {
                    mDelegate.mCalendarMultiSelectListener.onMultiSelectOutOfSize(calendar,
                            mDelegate.getMaxMultiSelectSize());
                }
                return;
            }
            mDelegate.mSelectedCalendars.put(key, calendar);
        }

        mCurrentItem = mItems.indexOf(calendar);

        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(calendar, true);
        }
        if (mParentLayout != null) {
            int i = CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart());
            mParentLayout.updateSelectWeek(i);
        }

        if (mDelegate.mCalendarMultiSelectListener != null) {
            mDelegate.mCalendarMultiSelectListener.onCalendarMultiSelect(
                    calendar,
                    mDelegate.mSelectedCalendars.size(),
                    mDelegate.getMaxMultiSelectSize());
        }

        invalidate();
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    /**
     * Whether the previous date is selected
     *
     * @param calendar current date
     * @return Whether the previous date is selected
     */
    protected final boolean isSelectPreCalendar(Calendar calendar) {
        Calendar preCalendar = CalendarUtil.getPreCalendar(calendar);
        mDelegate.updateCalendarScheme(preCalendar);
        return isCalendarSelected(preCalendar);
    }

    /**
     * Whether the next date is selected
     *
     * @param calendar current date
     * @return whether the next date is selected
     */
    protected final boolean isSelectNextCalendar(Calendar calendar) {
        Calendar nextCalendar = CalendarUtil.getNextCalendar(calendar);
        mDelegate.updateCalendarScheme(nextCalendar);
        return isCalendarSelected(nextCalendar);
    }

    /**
     * Draw the selected date
     *
     * @param canvas canvas
     * @param calendar calendar
     * @param x Calendar Card x starting point coordinates
     * @param hasScheme hasScheme unmarked date
     * @param isSelectedPre Whether the previous date is selected
     * @param isSelectedNext Whether the next date is selected
     * @return whether to draw onDrawScheme
     */
    protected abstract boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, boolean hasScheme,
                                              boolean isSelectedPre, boolean isSelectedNext);

    /**
     * Date the mark was drawn
     *
     * @param canvas canvas
     * @param calendar calendar
     * @param x Calendar Card x starting point coordinates
     * @param isSelected is selected
     */
    protected abstract void onDrawScheme(Canvas canvas, Calendar calendar, int x, boolean isSelected);


    /**
     * Draw calendar text
     *
     * @param canvas canvas
     * @param calendar calendar
     * @param x Calendar Card x starting point coordinates
     * @param hasScheme is the date marked
     * @param isSelected is selected
     */
    protected abstract void onDrawText(Canvas canvas, Calendar calendar, int x, boolean hasScheme, boolean isSelected);
}
