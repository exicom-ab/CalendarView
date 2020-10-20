package com.haibin.calendarview;

import android.content.Context;

/**
 * The most basic weekly view, because the calendar UI is implemented by hot plugging, it must be implemented here to achieve the same UI.
 * Various views such as：WeekView、RangeWeekView
 */

public abstract class BaseWeekView extends BaseView {

    public BaseWeekView(Context context) {
        super(context);
    }

    /**
     * Initialize the week view control
     *
     * @param calendar calendar
     */
    final void setup(Calendar calendar) {
        mItems = CalendarUtil.initCalendarForWeekView(calendar, mDelegate, mDelegate.getWeekStart());
        addSchemesFromMap();
        invalidate();
    }


    /**
     * Record the selected date
     *
     * @param calendar calendar
     */
    final void setSelectedCalendar(Calendar calendar) {
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_SINGLE &&
                !calendar.equals(mDelegate.mSelectedCalendar)) {
            return;
        }
        mCurrentItem = mItems.indexOf(calendar);
    }


    /**
     * Weekly view switch click the default position
     *
     * @param calendar calendar
     * @param isNotice isNotice
     */
    final void performClickCalendar(Calendar calendar, boolean isNotice) {

        if (mParentLayout == null ||
                mDelegate.mInnerListener == null ||
                mItems == null || mItems.size() == 0) {
            return;
        }

        int week = CalendarUtil.getWeekViewIndexFromCalendar(calendar, mDelegate.getWeekStart());
        if (mItems.contains(mDelegate.getCurrentDay())) {
            week = CalendarUtil.getWeekViewIndexFromCalendar(mDelegate.getCurrentDay(), mDelegate.getWeekStart());
        }

        int curIndex = week;

        Calendar currentCalendar = mItems.get(week);
        if (mDelegate.getSelectMode() != CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            if (mItems.contains(mDelegate.mSelectedCalendar)) {
                currentCalendar = mDelegate.mSelectedCalendar;
            } else {
                mCurrentItem = -1;
            }
        }

        if (!isInRange(currentCalendar)) {
            curIndex = getEdgeIndex(isMinRangeEdge(currentCalendar));
            currentCalendar = mItems.get(curIndex);
        }


        currentCalendar.setCurrentDay(currentCalendar.equals(mDelegate.getCurrentDay()));
        mDelegate.mInnerListener.onWeekDateSelected(currentCalendar, false);
        int i = CalendarUtil.getWeekFromDayInMonth(currentCalendar, mDelegate.getWeekStart());
        mParentLayout.updateSelectWeek(i);


        if (mDelegate.mCalendarSelectListener != null
                && isNotice
                && mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(currentCalendar, false);
        }

        mParentLayout.updateContentViewTranslateY();
        if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
            mCurrentItem = curIndex;
        }

        if (!mDelegate.isShowYearSelectedLayout &&
                mDelegate.mIndexCalendar != null &&
                calendar.getYear() != mDelegate.mIndexCalendar.getYear() &&
                mDelegate.mYearChangeListener != null) {
            mDelegate.mYearChangeListener.onYearChange(mDelegate.mIndexCalendar.getYear());
        }

        mDelegate.mIndexCalendar = currentCalendar;
        invalidate();
    }

    /**
     * Is it the minimum access boundary?
     *
     * @param calendar calendar
     * @return Is it the minimum access boundary?
     */
    final boolean isMinRangeEdge(Calendar calendar) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.set(mDelegate.getMinYear(), mDelegate.getMinYearMonth() - 1, mDelegate.getMinYearDay());
        long minTime = c.getTimeInMillis();
        c.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay());
        long curTime = c.getTimeInMillis();
        return curTime < minTime;
    }

    /**
     * Get the subscript within the boundary
     *
     * @param isMinEdge isMinEdge
     * @return Get the subscript within the boundary
     */
    final int getEdgeIndex(boolean isMinEdge) {
        for (int i = 0; i < mItems.size(); i++) {
            Calendar item = mItems.get(i);
            boolean isInRange = isInRange(item);
            if (isMinEdge && isInRange) {
                return i;
            } else if (!isMinEdge && !isInRange) {
                return i - 1;
            }
        }
        return isMinEdge ? 6 : 0;
    }


    /**
     * Get clicked calendar
     *
     * @return Get clicked calendar
     */
    protected Calendar getIndex() {

        int indexX = (int) (mX - mDelegate.getCalendarPadding()) / mItemWidth;
        if (indexX >= 7) {
            indexX = 6;
        }
        int indexY = (int) mY / mItemHeight;
        int position = indexY * 7 + indexX;// 选择项
        if (position >= 0 && position < mItems.size())
            return mItems.get(position);
        return null;
    }


    /**
     * Update display mode
     */
    final void updateShowMode() {
        invalidate();
    }

    /**
     * Start of update week
     */
    final void updateWeekStart() {

        int position = (int) getTag();
        Calendar calendar = CalendarUtil.getFirstCalendarStartWithMinCalendar(mDelegate.getMinYear(),
                mDelegate.getMinYearMonth(),
                mDelegate.getMinYearDay(),
                position + 1,
                mDelegate.getWeekStart());
        setSelectedCalendar(mDelegate.mSelectedCalendar);
        setup(calendar);
    }

    /**
     * Update election model
     */
    final void updateSingleSelect() {
        if (!mItems.contains(mDelegate.mSelectedCalendar)) {
            mCurrentItem = -1;
            invalidate();
        }
    }

    @Override
    void updateCurrentDate() {
        if (mItems == null)
            return;
        if (mItems.contains(mDelegate.getCurrentDay())) {
            for (Calendar a : mItems) {//Add operation
                a.setCurrentDay(false);
            }
            int index = mItems.indexOf(mDelegate.getCurrentDay());
            mItems.get(index).setCurrentDay(true);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * The hook before starting to draw, here are some initialization operations, and it is only called once for each drawing, and the performance is efficient
     * No need can be ignored and not realized
     * E.g：
     * 1、Need to draw a circular marker event background, you can calculate the radius here
     * 2、Draw rectangle selection effect, you can also calculate rectangle width and height here
     */
    protected void onPreviewHook() {
        // TODO: 2017/11/16
    }


    /**
     * Callback for the start of loop drawing, no need to ignore
     * Draw the cycle of each calendar item, used to calculate baseLine, circle center coordinates, etc. can be implemented here
     *
     * @param x Calendar Card x starting point coordinates
     */
    @SuppressWarnings("unused")
    protected void onLoopStart(int x) {
        // TODO: 2017/11/16
    }

    @Override
    protected void onDestroy() {

    }
}
