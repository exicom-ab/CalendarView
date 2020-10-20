package com.haibin.calendarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Switch ViewPager by month, customize adaptation height
 */
public final class MonthViewPager extends ViewPager {

    private boolean isUpdateMonthView;

    private int mMonthCount;

    private CalendarViewDelegate mDelegate;

    private int mNextViewHeight, mPreViewHeight, mCurrentViewHeight;

    CalendarLayout mParentLayout;

    WeekViewPager mWeekPager;

    WeekBar mWeekBar;

    /**
     * Whether to use scroll to a certain day
     */
    private boolean isUsingScrollToCalendar = false;

    public MonthViewPager(Context context) {
        this(context, null);
    }

    public MonthViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * initialization
     *
     * @param delegate delegate
     */
    void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;

        updateMonthViewHeight(mDelegate.getCurrentDay().getYear(),
                mDelegate.getCurrentDay().getMonth());

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = mCurrentViewHeight;
        setLayoutParams(params);
        init();
    }

    /**
     * initialization
     */
    private void init() {
        mMonthCount = 12 * (mDelegate.getMaxYear() - mDelegate.getMinYear())
                - mDelegate.getMinYearMonth() + 1 +
                mDelegate.getMaxYearMonth();
        setAdapter(new MonthViewPagerAdapter());
        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ALL_MONTH) {
                    return;
                }
                int height;
                if (position < getCurrentItem()) {//Swipe right-1
                    height = (int) ((mPreViewHeight)
                            * (1 - positionOffset) +
                            mCurrentViewHeight
                                    * positionOffset);
                } else {//Swipe left+!
                    height = (int) ((mCurrentViewHeight)
                            * (1 - positionOffset) +
                            (mNextViewHeight)
                                    * positionOffset);
                }
                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = height;
                setLayoutParams(params);
            }

            @Override
            public void onPageSelected(int position) {
                Calendar calendar = CalendarUtil.getFirstCalendarFromMonthViewPager(position, mDelegate);
                if (getVisibility() == VISIBLE) {
                    if (!mDelegate.isShowYearSelectedLayout &&
                            mDelegate.mIndexCalendar != null &&
                            calendar.getYear() != mDelegate.mIndexCalendar.getYear() &&
                            mDelegate.mYearChangeListener != null) {
                        mDelegate.mYearChangeListener.onYearChange(calendar.getYear());
                    }
                    mDelegate.mIndexCalendar = calendar;
                }
                //Month change event
                if (mDelegate.mMonthChangeListener != null) {
                    mDelegate.mMonthChangeListener.onMonthChange(calendar.getYear(), calendar.getMonth());
                }

                //The height of the month view needs to be dynamically changed when the week view is displayed
                if (mWeekPager.getVisibility() == VISIBLE) {
                    updateMonthViewHeight(calendar.getYear(), calendar.getMonth());
                    return;
                }


                if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
                    if (!calendar.isCurrentMonth()) {
                        mDelegate.mSelectedCalendar = calendar;
                    } else {
                        mDelegate.mSelectedCalendar = CalendarUtil.getRangeEdgeCalendar(calendar, mDelegate);
                    }
                    mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar;
                } else {
                    if (mDelegate.mSelectedStartRangeCalendar != null &&
                            mDelegate.mSelectedStartRangeCalendar.isSameMonth(mDelegate.mIndexCalendar)) {
                        mDelegate.mIndexCalendar = mDelegate.mSelectedStartRangeCalendar;
                    } else {
                        if (calendar.isSameMonth(mDelegate.mSelectedCalendar)) {
                            mDelegate.mIndexCalendar = mDelegate.mSelectedCalendar;
                        }
                    }
                }

                mDelegate.updateSelectCalendarScheme();
                if (!isUsingScrollToCalendar && mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
                    mWeekBar.onDateSelected(mDelegate.mSelectedCalendar, mDelegate.getWeekStart(), false);
                    if (mDelegate.mCalendarSelectListener != null) {
                        mDelegate.mCalendarSelectListener.onCalendarSelect(mDelegate.mSelectedCalendar, false);
                    }
                }

                BaseMonthView view = findViewWithTag(position);
                if (view != null) {
                    int index = view.getSelectedIndex(mDelegate.mIndexCalendar);
                    if (mDelegate.getSelectMode() == CalendarViewDelegate.SELECT_MODE_DEFAULT) {
                        view.mCurrentItem = index;
                    }
                    if (index >= 0 && mParentLayout != null) {
                        mParentLayout.updateSelectPosition(index);
                    }
                    view.invalidate();
                }
                mWeekPager.updateSelected(mDelegate.mIndexCalendar, false);
                updateMonthViewHeight(calendar.getYear(), calendar.getMonth());
                isUsingScrollToCalendar = false;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * Update the height of the month view
     *
     * @param year  year
     * @param month month
     */
    private void updateMonthViewHeight(int year, int month) {
        if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ALL_MONTH) {//Non-dynamic height is not needed
            mCurrentViewHeight = 6 * mDelegate.getCalendarItemHeight();
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = mCurrentViewHeight;
            return;
        }

        if (mParentLayout != null) {
            if (getVisibility() != VISIBLE) {//If the week view is already displayed, you need to dynamically change the height of the month view, otherwise the display will have a bug
                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = CalendarUtil.getMonthViewHeight(year, month,
                        mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                        mDelegate.getMonthViewShowMode());
                setLayoutParams(params);
            }
            mParentLayout.updateContentViewTranslateY();
        }
        mCurrentViewHeight = CalendarUtil.getMonthViewHeight(year, month,
                mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());
        if (month == 1) {
            mPreViewHeight = CalendarUtil.getMonthViewHeight(year - 1, 12,
                    mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                    mDelegate.getMonthViewShowMode());
            mNextViewHeight = CalendarUtil.getMonthViewHeight(year, 2,
                    mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                    mDelegate.getMonthViewShowMode());
        } else {
            mPreViewHeight = CalendarUtil.getMonthViewHeight(year, month - 1,
                    mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                    mDelegate.getMonthViewShowMode());
            if (month == 12) {
                mNextViewHeight = CalendarUtil.getMonthViewHeight(year + 1, 1,
                        mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                        mDelegate.getMonthViewShowMode());
            } else {
                mNextViewHeight = CalendarUtil.getMonthViewHeight(year, month + 1,
                        mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                        mDelegate.getMonthViewShowMode());
            }
        }
    }

    /**
     * Refresh
     */
    void notifyDataSetChanged() {
        mMonthCount = 12 * (mDelegate.getMaxYear() - mDelegate.getMinYear())
                - mDelegate.getMinYearMonth() + 1 +
                mDelegate.getMaxYearMonth();
        notifyAdapterDataSetChanged();
    }

    /**
     * Update month view class
     */
    void updateMonthViewClass() {
        isUpdateMonthView = true;
        notifyAdapterDataSetChanged();
        isUpdateMonthView = false;
    }

    /**
     * Update date range
     */
    final void updateRange() {
        isUpdateMonthView = true;
        notifyDataSetChanged();
        isUpdateMonthView = false;
        if (getVisibility() != VISIBLE) {
            return;
        }
        isUsingScrollToCalendar = false;
        Calendar calendar = mDelegate.mSelectedCalendar;
        int y = calendar.getYear() - mDelegate.getMinYear();
        int position = 12 * y + calendar.getMonth() - mDelegate.getMinYearMonth();
        setCurrentItem(position, false);
        BaseMonthView view = findViewWithTag(position);
        if (view != null) {
            view.setSelectedCalendar(mDelegate.mIndexCalendar);
            view.invalidate();
            if (mParentLayout != null) {
                mParentLayout.updateSelectPosition(view.getSelectedIndex(mDelegate.mIndexCalendar));
            }
        }
        if (mParentLayout != null) {
            int week = CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart());
            mParentLayout.updateSelectWeek(week);
        }


        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onMonthDateSelected(calendar, false);
        }

        if (mDelegate.mCalendarSelectListener != null) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(calendar, false);
        }
        updateSelected();
    }

    /**
     * Scroll to the specified date
     *
     * @param year
     * @param month month
     * @param day
     * @param invokeListener call date event
     */
    void scrollToCalendar(int year, int month, int day, boolean smoothScroll, boolean invokeListener) {
        isUsingScrollToCalendar = true;
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        calendar.setCurrentDay(calendar.equals(mDelegate.getCurrentDay()));
        LunarCalendar.setupLunarCalendar(calendar);
        mDelegate.mIndexCalendar = calendar;
        mDelegate.mSelectedCalendar = calendar;
        mDelegate.updateSelectCalendarScheme();
        int y = calendar.getYear() - mDelegate.getMinYear();
        int position = 12 * y + calendar.getMonth() - mDelegate.getMinYearMonth();
        int curItem = getCurrentItem();
        if (curItem == position) {
            isUsingScrollToCalendar = false;
        }
        setCurrentItem(position, smoothScroll);

        BaseMonthView view = findViewWithTag(position);
        if (view != null) {
            view.setSelectedCalendar(mDelegate.mIndexCalendar);
            view.invalidate();
            if (mParentLayout != null) {
                mParentLayout.updateSelectPosition(view.getSelectedIndex(mDelegate.mIndexCalendar));
            }
        }
        if (mParentLayout != null) {
            int week = CalendarUtil.getWeekFromDayInMonth(calendar, mDelegate.getWeekStart());
            mParentLayout.updateSelectWeek(week);
        }

        if (mDelegate.mCalendarSelectListener != null && invokeListener) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(calendar, false);
        }
        if (mDelegate.mInnerListener != null) {
            mDelegate.mInnerListener.onMonthDateSelected(calendar, false);
        }

        updateSelected();
    }

    /**
     * Scroll to current date
     */
    void scrollToCurrent(boolean smoothScroll) {
        isUsingScrollToCalendar = true;
        int position = 12 * (mDelegate.getCurrentDay().getYear() - mDelegate.getMinYear()) +
                mDelegate.getCurrentDay().getMonth() - mDelegate.getMinYearMonth();
        int curItem = getCurrentItem();
        if (curItem == position) {
            isUsingScrollToCalendar = false;
        }

        setCurrentItem(position, smoothScroll);

        BaseMonthView view = findViewWithTag(position);
        if (view != null) {
            view.setSelectedCalendar(mDelegate.getCurrentDay());
            view.invalidate();
            if (mParentLayout != null) {
                mParentLayout.updateSelectPosition(view.getSelectedIndex(mDelegate.getCurrentDay()));
            }
        }

        if (mDelegate.mCalendarSelectListener != null && getVisibility() == VISIBLE) {
            mDelegate.mCalendarSelectListener.onCalendarSelect(mDelegate.mSelectedCalendar, false);
        }
    }

    /**
     * Get current month data
     *
     * @return Get current month data
     */
    List<Calendar> getCurrentMonthCalendars() {
        BaseMonthView view = findViewWithTag(getCurrentItem());
        if (view == null) {
            return null;
        }
        return view.mItems;
    }

    /**
     * Update to the default selection mode
     */
    void updateDefaultSelect() {
        BaseMonthView view = findViewWithTag(getCurrentItem());
        if (view != null) {
            int index = view.getSelectedIndex(mDelegate.mSelectedCalendar);
            view.mCurrentItem = index;
            if (index >= 0 && mParentLayout != null) {
                mParentLayout.updateSelectPosition(index);
            }
            view.invalidate();
        }
    }


    /**
     * Update selection effect
     */
    void updateSelected() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.setSelectedCalendar(mDelegate.mSelectedCalendar);
            view.invalidate();
        }
    }

    /**
     * Update font color size
     */
    final void updateStyle() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.updateStyle();
            view.invalidate();
        }
    }

    /**
     * Update mark date
     */
    void updateScheme() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.update();
        }
    }

    /**
     * Update the current date, call this function when the night is over, generally do not need to call
     */
    void updateCurrentDate() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.updateCurrentDate();
        }
    }


    /**
     * Update display mode
     */
    void updateShowMode() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.updateShowMode();
            view.requestLayout();
        }
        if (mDelegate.getMonthViewShowMode() == CalendarViewDelegate.MODE_ALL_MONTH) {
            mCurrentViewHeight = 6 * mDelegate.getCalendarItemHeight();
            mNextViewHeight = mCurrentViewHeight;
            mPreViewHeight = mCurrentViewHeight;
        } else {
            updateMonthViewHeight(mDelegate.mSelectedCalendar.getYear(), mDelegate.mSelectedCalendar.getMonth());
        }
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = mCurrentViewHeight;
        setLayoutParams(params);
        if (mParentLayout != null) {
            mParentLayout.updateContentViewTranslateY();
        }
    }

    /**
     * Start of update week
     */
    void updateWeekStart() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.updateWeekStart();
            view.requestLayout();
        }

        updateMonthViewHeight(mDelegate.mSelectedCalendar.getYear(), mDelegate.mSelectedCalendar.getMonth());
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = mCurrentViewHeight;
        setLayoutParams(params);
        if (mParentLayout != null) {
            int i = CalendarUtil.getWeekFromDayInMonth(mDelegate.mSelectedCalendar, mDelegate.getWeekStart());
            mParentLayout.updateSelectWeek(i);
        }
        updateSelected();
    }

    /**
     * Update height
     */
    final void updateItemHeight() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.updateItemHeight();
            view.requestLayout();
        }

        int year = mDelegate.mIndexCalendar.getYear();
        int month = mDelegate.mIndexCalendar.getMonth();
        mCurrentViewHeight = CalendarUtil.getMonthViewHeight(year, month,
                mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                mDelegate.getMonthViewShowMode());
        if (month == 1) {
            mPreViewHeight = CalendarUtil.getMonthViewHeight(year - 1, 12,
                    mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                    mDelegate.getMonthViewShowMode());
            mNextViewHeight = CalendarUtil.getMonthViewHeight(year, 2,
                    mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                    mDelegate.getMonthViewShowMode());
        } else {
            mPreViewHeight = CalendarUtil.getMonthViewHeight(year, month - 1,
                    mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                    mDelegate.getMonthViewShowMode());
            if (month == 12) {
                mNextViewHeight = CalendarUtil.getMonthViewHeight(year + 1, 1,
                        mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                        mDelegate.getMonthViewShowMode());
            } else {
                mNextViewHeight = CalendarUtil.getMonthViewHeight(year, month + 1,
                        mDelegate.getCalendarItemHeight(), mDelegate.getWeekStart(),
                        mDelegate.getMonthViewShowMode());
            }
        }
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = mCurrentViewHeight;
        setLayoutParams(params);
    }

    /**
     * Clear selection
     */
    final void clearSelectRange() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.invalidate();
        }
    }

    /**
     * Clear radio selection
     */
    final void clearSingleSelect() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.mCurrentItem = -1;
            view.invalidate();
        }
    }

    /**
     * Clear radio selection
     */
    final void clearMultiSelect() {
        for (int i = 0; i < getChildCount(); i++) {
            BaseMonthView view = (BaseMonthView) getChildAt(i);
            view.mCurrentItem = -1;
            view.invalidate();
        }
    }

    private void notifyAdapterDataSetChanged() {
        if (getAdapter() == null) {
            return;
        }
        getAdapter().notifyDataSetChanged();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDelegate.isMonthViewScrollable() && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDelegate.isMonthViewScrollable() && super.onInterceptTouchEvent(ev);
    }

    @Override
    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (Math.abs(getCurrentItem() - item) > 1) {
            super.setCurrentItem(item, false);
        } else {
            super.setCurrentItem(item, smoothScroll);
        }
    }

    /**
     * Calendar Card Month Adapter
     */
    private final class MonthViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mMonthCount;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return isUpdateMonthView ? POSITION_NONE : super.getItemPosition(object);
        }

        @Override
        public boolean isViewFromObject(View view, @NonNull Object object) {
            return view.equals(object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            int year = (position + mDelegate.getMinYearMonth() - 1) / 12 + mDelegate.getMinYear();
            int month = (position + mDelegate.getMinYearMonth() - 1) % 12 + 1;
            BaseMonthView view;
            try {
                Constructor constructor = mDelegate.getMonthViewClass().getConstructor(Context.class);
                view = (BaseMonthView) constructor.newInstance(getContext());
            } catch (Exception e) {
                e.printStackTrace();
                return new DefaultMonthView(getContext());
            }
            view.mMonthViewPager = MonthViewPager.this;
            view.mParentLayout = mParentLayout;
            view.setup(mDelegate);
            view.setTag(position);
            view.initMonthWithDate(year, month);
            view.setSelectedCalendar(mDelegate.mSelectedCalendar);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            BaseView view = (BaseView) object;
            view.onDestroy();
            container.removeView(view);
        }
    }


}
