package com.haibin.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * Range selection month view
 */
public abstract class RangeMonthView extends BaseMonthView {

    public RangeMonthView(Context context) {
        super(context);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (mLineCount == 0)
            return;
        mItemWidth = (getWidth() - 2 * mDelegate.getCalendarPadding()) / 7;
        onPreviewHook();
        int count = mLineCount * 7;
        int d = 0;
        for (int i = 0; i < mLineCount; i++) {
            for (int j = 0; j < 7; j++) {
                Calendar calendar = mItems.get(d);
                if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH) {
                    if (d > mItems.size() - mNextDiff) {
                        return;
                    }
                    if (!calendar.isCurrentMonth()) {
                        ++d;
                        continue;
                    }
                } else if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_FIT_MONTH) {
                    if (d >= count) {
                        return;
                    }
                }
                draw(canvas, calendar, i, j);
                ++d;
            }
        }
    }

    /**
     * Start drawing
     *
     * @param canvas canvas
     * @param calendar corresponds to the calendar
     * @param i i
     * @param j j
     */
    private void draw(Canvas canvas, Calendar calendar, int i, int j) {
        int x = j * mItemWidth + mDelegate.getCalendarPadding();
        int y = i * mItemHeight;
        onLoopStart(x, y);
        boolean isSelected = isCalendarSelected(calendar);
        boolean hasScheme = calendar.hasScheme();
        boolean isPreSelected = isSelectPreCalendar(calendar);
        boolean isNextSelected = isSelectNextCalendar(calendar);

        if (hasScheme) {
            //Marked day
            boolean isDrawSelected = false;//Whether to continue drawing the selected onDrawScheme
            if (isSelected) {
                isDrawSelected = onDrawSelected(canvas, calendar, x, y, true, isPreSelected, isNextSelected);
            }
            if (isDrawSelected || !isSelected) {
                //Set the brush as the marker color
                mSchemePaint.setColor(calendar.getSchemeColor() != 0 ? calendar.getSchemeColor() : mDelegate.getSchemeThemeColor());
                onDrawScheme(canvas, calendar, x, y, true);
            }
        } else {
            if (isSelected) {
                onDrawSelected(canvas, calendar, x, y, false, isPreSelected, isNextSelected);
            }
        }
        onDrawText(canvas, calendar, x, y, hasScheme, isSelected);
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

        if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ONLY_CURRENT_MONTH
                && !calendar.isCurrentMonth()) {
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

        if (!calendar.isCurrentMonth() && mMonthViewPager != null) {
            int cur = mMonthViewPager.getCurrentItem();
            int position = mCurrentItem < 7 ? cur - 1 : cur + 1;
            mMonthViewPager.setCurrentItem(position);
        }

        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onMonthDateSelected(calendar, true);
        }

        if (mParentLayout != null) {
            if (calendar.isCurrentMonth()) {
                mParentLayout.updateSelectPosition(mItems.indexOf(calendar));
            } else {
                mParentLayout.updateSelectWeek(CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart()));
            }
        }
        if (mDelegate.mCalendarRangeSelectListener != null) {
            mDelegate.mCalendarRangeSelectListener.onCalendarRangeSelect(calendar,
                    mDelegate.mSelectedEndRangeCalendar != null);
        }
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
     * @param y Calendar Card y starting point coordinates
     * @param hasScheme hasScheme unmarked date
     * @param isSelectedPre Whether the previous date is selected
     * @param isSelectedNext Whether the next date is selected
     * @return Whether to continue drawing onDrawScheme, true or false
     */
    protected abstract boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme,
                                              boolean isSelectedPre, boolean isSelectedNext);

    /**
     * The date of drawing the mark, here can be the background color, mark color or something
     *
     * @param canvas canvas
     * @param calendar calendar
     * @param x Calendar Card x starting point coordinates
     * @param y Calendar Card y starting point coordinates
     * @param isSelected is selected
     */
    protected abstract void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y, boolean isSelected);


    /**
     * Draw calendar text
     *
     * @param canvas canvas
     * @param calendar calendar
     * @param x Calendar Card x starting point coordinates
     * @param y Calendar Card y starting point coordinates
     * @param hasScheme is the date marked
     * @param isSelected is selected
     */
    protected abstract void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected);
}
