package com.haibin.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * Range selection week view
 */
public abstract class RangeWeekView extends BaseWeekView {

    public RangeWeekView(Context context) {
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
        if (mDelegate.mSelectedStartRangeCalendar == null) {
            return false;
        }
        if (onCalendarIntercept(calendar)) {
            return false;
        }
        if (mDelegate.mSelectedEndRangeCalendar == null) {
            return calendar.compareTo(mDelegate.mSelectedStartRangeCalendar) == 0;
        }
        return calendar.compareTo(mDelegate.mSelectedStartRangeCalendar) >= 0 &&
                calendar.compareTo(mDelegate.mSelectedEndRangeCalendar) <= 0;
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
            if (mDelegate.mCalendarRangeSelectListener != null) {
                mDelegate.mCalendarRangeSelectListener.onCalendarSelectOutOfRange(calendar);
            }
            return;
        }

        //Prioritize various direct return situations to reduce code depth
        if (mDelegate.mSelectedStartRangeCalendar != null && mDelegate.mSelectedEndRangeCalendar == null) {
            int minDiffer = CalendarUtil.differ(calendar, mDelegate.mSelectedStartRangeCalendar);
            if (minDiffer >= 0 && mDelegate.getMinSelectRange() != -1 && mDelegate.getMinSelectRange() > minDiffer + 1) {
                if (mDelegate.mCalendarRangeSelectListener != null) {
                    mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(calendar, true);
                }
                return;
            } else if (mDelegate.getMaxSelectRange() != -1 && mDelegate.getMaxSelectRange() <
                    CalendarUtil.differ(calendar, mDelegate.mSelectedStartRangeCalendar) + 1) {
                if (mDelegate.mCalendarRangeSelectListener != null) {
                    mDelegate.mCalendarRangeSelectListener.onSelectOutOfRange(calendar, false);
                }
                return;
            }
        }

        if (mDelegate.mSelectedStartRangeCalendar == null || mDelegate.mSelectedEndRangeCalendar != null) {
            mDelegate.mSelectedStartRangeCalendar = calendar;
            mDelegate.mSelectedEndRangeCalendar = null;
        } else {
            int compare = calendar.compareTo(mDelegate.mSelectedStartRangeCalendar);
            if (mDelegate.getMinSelectRange() == -1 && compare <= 0) {
                mDelegate.mSelectedStartRangeCalendar = calendar;
                mDelegate.mSelectedEndRangeCalendar = null;
            } else if (compare < 0) {
                mDelegate.mSelectedStartRangeCalendar = calendar;
                mDelegate.mSelectedEndRangeCalendar = null;
            } else if (compare == 0 &&
                    mDelegate.getMinSelectRange() == 1) {
                mDelegate.mSelectedEndRangeCalendar = calendar;
            } else {
                mDelegate.mSelectedEndRangeCalendar = calendar;
            }

        }

        mCurrentItem = mItems.indexOf(calendar);

        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onWeekDateSelected(calendar, true);
        }
        if (mParentLayout != null) {
            int i = CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart());
            mParentLayout.updateSelectWeek(i);
        }

        if (mDelegate.mCalendarRangeSelectListener != null) {
            mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(calendar,
                    mDelegate.mSelectedEndRangeCalendar != null);
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
        return mDelegate.mSelectedStartRangeCalendar != null &&
                isCalendarSelected(preCalendar);
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
        return mDelegate.mSelectedStartRangeCalendar != null &&
                isCalendarSelected(nextCalendar);
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
