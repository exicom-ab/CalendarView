package com.haibin.calendarview;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * Month view basic control, freely inheritable
 * Various views such as：MonthView、RangeMonthView、MultiMonthView
 */
public abstract class BaseMonthView extends BaseView {

    MonthViewPager mMonthViewPager;

    /**
     * Current calendar card year
     */
    protected int mYear;

    /**
     * Current calendar card month
     */
    protected int mMonth;


    /**
     * Number of calendar rows
     */
    protected int mLineCount;

    /**
     * Calendar height
     */
    protected int mHeight;


    /**
     * Number of shifts in the next month
     */
    protected int mNextDiff;

    public BaseMonthView(Context context) {
        super(context);
    }

    /**
     * Initialization date
     *
     * @param year  year
     * @param month month
     */
    final void initMonthWithDate(int year, int month) {
        mYear = year;
        mMonth = month;
        initCalendar();
        mHeight = CalendarUtil.getMonthViewHeight(year, month, mItemHeight, mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());

    }

    /**
     * Initialize the calendar
     */
    @SuppressLint("WrongConstant")
    private void initCalendar() {

        mNextDiff = CalendarUtil.getMonthEndDiff(mYear, mMonth, mDelegate.getWeekStart());
        int preDiff = CalendarUtil.getMonthViewStartDiff(mYear, mMonth, mDelegate.getWeekStart());
        int monthDayCount = CalendarUtil.getMonthDaysCount(mYear, mMonth);

        mItems = CalendarUtil.initCalendarForMonthView(mYear, mMonth, mDelegate.getCurrentDay(), mDelegate.getWeekStart());

        if (mItems.contains(mDelegate.getCurrentDay())) {
            mCurrentItem = mItems.indexOf(mDelegate.getCurrentDay());
        } else {
            mCurrentItem = mItems.indexOf(mDelegate.mSelectedCalendar);
        }

        if (mCurrentItem > 0 &&
                mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(mDelegate.mSelectedCalendar)) {
            mCurrentItem = -1;
        }

        if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ALL_MONTH) {
            mLineCount = 6;
        } else {
            mLineCount = (preDiff + monthDayCount + mNextDiff) / 7;
        }
        addSchemesFromMap();
        invalidate();
    }

    /**
     * Get the date selected by click
     *
     * @return return
     */
    protected Calendar getIndex() {
        if (mItemWidth == 0 || mItemHeight == 0) {
            return null;
        }
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
     * Record the selected date
     *
     * @param calendar calendar
     */
    final void setSelectedCalendar(Calendar calendar) {
        mCurrentItem = mItems.indexOf(calendar);
    }


    /**
     * Update display mode
     */
    final void updateShowMode() {
        mLineCount = CalendarUtil.getMonthViewLineCount(mYear, mMonth,
                mDelegate.getWeekStart(), mDelegate.getMonthViewShowMode());
        mHeight = CalendarUtil.getMonthViewHeight(mYear, mMonth, mItemHeight, mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());
        invalidate();
    }

    /**
     * Start of update week
     */
    final void updateWeekStart() {
        initCalendar();
        mHeight = CalendarUtil.getMonthViewHeight(mYear, mMonth, mItemHeight, mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());
    }

    @Override
    void updateItemHeight() {
        super.updateItemHeight();
        mHeight = CalendarUtil.getMonthViewHeight(mYear, mMonth, mItemHeight, mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());
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


    /**
     * Get the selected subscript
     *
     * @param calendar calendar
     * @return Get the selected subscript
     */
    protected final int getSelectedIndex(Calendar calendar) {
        return mItems.indexOf(calendar);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mLineCount != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * The hook before starting to draw, here are some initialization operations, and it is only called once for each drawing, and the performance is efficient
     * No need can be ignored and not realized
     * E.g:：
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
     * @param y Calendar Card y starting point coordinates
     */
    protected void onLoopStart(int x, int y) {
        // TODO: 2017/11/16  
    }

    @Override
    protected void onDestroy() {

    }
}
