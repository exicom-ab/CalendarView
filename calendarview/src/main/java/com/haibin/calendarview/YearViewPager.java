package com.haibin.calendarview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


/**
 * Year + month selection layout
 * ViewPager + RecyclerView
 */
public final class YearViewPager extends ViewPager {
    private int mYearCount;
    private boolean isUpdateYearView;
    private CalendarViewDelegate mDelegate;
    private YearRecyclerView.OnMonthSelectedListener mListener;

    public YearViewPager(Context context) {
        this(context, null);
    }

    public YearViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        this.mYearCount = mDelegate.getMaxYear() - mDelegate.getMinYear() + 1;
        setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mYearCount;
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return isUpdateYearView ? POSITION_NONE : super.getItemPosition(object);
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                YearRecyclerView view = new YearRecyclerView(getContext());
                container.addView(view);
                view.setup(mDelegate);
                view.setOnMonthSelectedListener(mListener);
                view.init(position + mDelegate.getMinYear());
                return view;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        });
        setCurrentItem(mDelegate.getCurrentDay().getYear() - mDelegate.getMinYear());
    }

    @Override
    public void setCurrentItem(int item) {
        setCurrentItem(item, false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        if (Math.abs(getCurrentItem() - item) > 1) {
            super.setCurrentItem(item, false);
        } else {
            super.setCurrentItem(item, false);
        }
    }

    /**
     * Notification refresh
     */
    void notifyDataSetChanged() {
        this.mYearCount = mDelegate.getMaxYear() - mDelegate.getMinYear() + 1;
        if(getAdapter() != null){
            getAdapter().notifyDataSetChanged();
        }

    }

    /**
     * Scroll to a certain year
     *
     * @param year         year
     * @param smoothScroll smoothScroll
     */
    void scrollToYear(int year, boolean smoothScroll) {
        setCurrentItem(year - mDelegate.getMinYear(), smoothScroll);
    }

    /**
     * Update date range
     */
    final void updateRange() {
        isUpdateYearView = true;
        notifyDataSetChanged();
        isUpdateYearView = false;
    }

    /**
     * Update interface
     */
    final void update() {
        for (int i = 0; i < getChildCount(); i++) {
            YearRecyclerView view = (YearRecyclerView) getChildAt(i);
            view.notifyAdapterDataSetChanged();
        }
    }


    /**
     * Start of update week
     */
    final void updateWeekStart() {
        for (int i = 0; i < getChildCount(); i++) {
            YearRecyclerView view = (YearRecyclerView) getChildAt(i);
            view.updateWeekStart();
            view.notifyAdapterDataSetChanged();
        }
    }

    /**
     * Update font color size
     */
    final void updateStyle(){
        for (int i = 0; i < getChildCount(); i++) {
            YearRecyclerView view = (YearRecyclerView) getChildAt(i);
            view.updateStyle();
        }
    }

    final void setOnMonthSelectedListener(YearRecyclerView.OnMonthSelectedListener listener) {
        this.mListener = listener;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getHeight(getContext(), this), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Calculate relative height
     *
     * @param context context
     * @param view    view
     * @return The most suitable height for the month view selector
     */
    private static int getHeight(Context context, View view) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert manager != null;
        Display display = manager.getDefaultDisplay();
        int h = display.getHeight();
        int[] location = new int[2];
        view.getLocationInWindow(location);
        view.getLocationOnScreen(location);
        return h - location[1];
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mDelegate.isYearViewScrollable() && super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDelegate.isYearViewScrollable() && super.onInterceptTouchEvent(ev);
    }
}
