package com.haibin.calendarview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * The basic calendar View, which derives MonthView and WeekView
 */

public abstract class BaseView extends View implements View.OnClickListener, View.OnLongClickListener {

    CalendarViewDelegate mDelegate;

    /**
     * Pen for current month day
     */
    protected Paint mCurMonthTextPaint = new Paint();

    /**
     * Other month day color
     */
    protected Paint mOtherMonthTextPaint = new Paint();

    /**
     * Lunar text color of current month
     */
    protected Paint mCurMonthLunarTextPaint = new Paint();

    /**
     * Lunar text color of current month
     */
    protected Paint mSelectedLunarTextPaint = new Paint();

    /**
     * Lunar text color for other months
     */
    protected Paint mOtherMonthLunarTextPaint = new Paint();

    /**
     * Lunar text color for other months
     */
    protected Paint mSchemeLunarTextPaint = new Paint();

    /**
     * Marked date background color brush
     */
    protected Paint mSchemePaint = new Paint();

    /**
     * The selected date background color
     */
    protected Paint mSelectedPaint = new Paint();

    /**
     * Marked text brush
     */
    protected Paint mSchemeTextPaint = new Paint();

    /**
     * Selected text brush
     */
    protected Paint mSelectTextPaint = new Paint();

    /**
     * Current date text color pen
     */
    protected Paint mCurDayTextPaint = new Paint();

    /**
     * Current date text color pen
     */
    protected Paint mCurDayLunarTextPaint = new Paint();

    /**
     * Calendar layout, you need to put your own layout below the calendar
     */
    CalendarLayout mParentLayout;

    /**
     * Calendar entry
     */
    List<Calendar> mItems;

    /**
     * Height of each item
     */
    protected int mItemHeight;

    /**
     * The width of each item
     */
    protected int mItemWidth;

    /**
     * Text baseline
     */
    protected float mTextBaseLine;

    /**
     * The x and y coordinates of the click
     */
    float mX, mY;

    /**
     * Whether to click
     */
    boolean isClick = true;

    /**
     * font size
     */
    static final int TEXT_SIZE = 14;

    /**
     * Current click item
     */
    int mCurrentItem = -1;

    public BaseView(Context context) {
        this(context, null);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    /**
     * Initial configuration
     *
     * @param context context
     */
    private void initPaint(Context context) {
        mCurMonthTextPaint.setAntiAlias(true);
        mCurMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurMonthTextPaint.setColor(0xFF111111);
        mCurMonthTextPaint.setFakeBoldText(true);
        mCurMonthTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mOtherMonthTextPaint.setAntiAlias(true);
        mOtherMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mOtherMonthTextPaint.setColor(0xFFe1e1e1);
        mOtherMonthTextPaint.setFakeBoldText(true);
        mOtherMonthTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mCurMonthLunarTextPaint.setAntiAlias(true);
        mCurMonthLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mSelectedLunarTextPaint.setAntiAlias(true);
        mSelectedLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mOtherMonthLunarTextPaint.setAntiAlias(true);
        mOtherMonthLunarTextPaint.setTextAlign(Paint.Align.CENTER);


        mSchemeLunarTextPaint.setAntiAlias(true);
        mSchemeLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mSchemeTextPaint.setAntiAlias(true);
        mSchemeTextPaint.setStyle(Paint.Style.FILL);
        mSchemeTextPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeTextPaint.setColor(0xffed5353);
        mSchemeTextPaint.setFakeBoldText(true);
        mSchemeTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mSelectTextPaint.setAntiAlias(true);
        mSelectTextPaint.setStyle(Paint.Style.FILL);
        mSelectTextPaint.setTextAlign(Paint.Align.CENTER);
        mSelectTextPaint.setColor(0xffed5353);
        mSelectTextPaint.setFakeBoldText(true);
        mSelectTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mSchemePaint.setAntiAlias(true);
        mSchemePaint.setStyle(Paint.Style.FILL);
        mSchemePaint.setStrokeWidth(2);
        mSchemePaint.setColor(0xffefefef);

        mCurDayTextPaint.setAntiAlias(true);
        mCurDayTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurDayTextPaint.setColor(Color.RED);
        mCurDayTextPaint.setFakeBoldText(true);
        mCurDayTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mCurDayLunarTextPaint.setAntiAlias(true);
        mCurDayLunarTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurDayLunarTextPaint.setColor(Color.RED);
        mCurDayLunarTextPaint.setFakeBoldText(true);
        mCurDayLunarTextPaint.setTextSize(CalendarUtil.dipToPx(context, TEXT_SIZE));

        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setStyle(Paint.Style.FILL);
        mSelectedPaint.setStrokeWidth(2);

        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    /**
     * Initialize all UI configuration
     *
     * @param delegate delegate
     */
   final void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        updateStyle();
        updateItemHeight();

        initPaint();
    }


    final void updateStyle(){
        if(mDelegate == null){
            return;
        }
        this.mCurDayTextPaint.setColor(mDelegate.getCurDayTextColor());
        this.mCurDayLunarTextPaint.setColor(mDelegate.getCurDayLunarTextColor());
        this.mCurMonthTextPaint.setColor(mDelegate.getCurrentMonthTextColor());
        this.mOtherMonthTextPaint.setColor(mDelegate.getOtherMonthTextColor());
        this.mCurMonthLunarTextPaint.setColor(mDelegate.getCurrentMonthLunarTextColor());
        this.mSelectedLunarTextPaint.setColor(mDelegate.getSelectedLunarTextColor());
        this.mSelectTextPaint.setColor(mDelegate.getSelectedTextColor());
        this.mOtherMonthLunarTextPaint.setColor(mDelegate.getOtherMonthLunarTextColor());
        this.mSchemeLunarTextPaint.setColor(mDelegate.getSchemeLunarTextColor());
        this.mSchemePaint.setColor(mDelegate.getSchemeThemeColor());
        this.mSchemeTextPaint.setColor(mDelegate.getSchemeTextColor());
        this.mCurMonthTextPaint.setTextSize(mDelegate.getDayTextSize());
        this.mOtherMonthTextPaint.setTextSize(mDelegate.getDayTextSize());
        this.mCurDayTextPaint.setTextSize(mDelegate.getDayTextSize());
        this.mSchemeTextPaint.setTextSize(mDelegate.getDayTextSize());
        this.mSelectTextPaint.setTextSize(mDelegate.getDayTextSize());

        this.mCurMonthLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());
        this.mSelectedLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());
        this.mCurDayLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());
        this.mOtherMonthLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());
        this.mSchemeLunarTextPaint.setTextSize(mDelegate.getLunarTextSize());

        this.mSelectedPaint.setStyle(Paint.Style.FILL);
        this.mSelectedPaint.setColor(mDelegate.getSelectedThemeColor());
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    void updateItemHeight() {
        this.mItemHeight = mDelegate.getCalendarItemHeight();
        Paint.FontMetrics metrics = mCurMonthTextPaint.getFontMetrics();
        mTextBaseLine = mItemHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2;
    }


    /**
     * Remove event
     */
    final void removeSchemes() {
        for (Calendar a : mItems) {
            a.setScheme("");
            a.setSchemeColor(0);
            a.setSchemes(null);
        }
    }

    /**
     * Add event marker, from Map
     */
    final void addSchemesFromMap() {
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap.size() == 0) {
            return;
        }
        for (Calendar a : mItems) {
            if (mDelegate.mSchemeDatesMap.containsKey(a.toString())) {
                Calendar d = mDelegate.mSchemeDatesMap.get(a.toString());
                if(d == null){
                    continue;
                }
                a.setScheme(TextUtils.isEmpty(d.getScheme()) ? mDelegate.getSchemeText() : d.getScheme());
                a.setSchemeColor(d.getSchemeColor());
                a.setSchemes(d.getSchemes());
            } else {
                a.setScheme("");
                a.setSchemeColor(0);
                a.setSchemes(null);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() > 1)
            return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mX = event.getX();
                mY = event.getY();
                isClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float mDY;
                if (isClick) {
                    mDY = event.getY() - mY;
                    isClick = Math.abs(mDY) <= 50;
                }
                break;
            case MotionEvent.ACTION_UP:
                mX = event.getX();
                mY = event.getY();
                break;
        }
        return super.onTouchEvent(event);
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
     * Is it selected
     *
     * @param calendar calendar
     * @return true or false
     */
    protected boolean isSelected(Calendar calendar) {
        return mItems != null && mItems.indexOf(calendar) == mCurrentItem;
    }

    /**
     * Update event
     */
    final void update() {
        if (mDelegate.mSchemeDatesMap == null || mDelegate.mSchemeDatesMap.size() == 0) {//清空操作
            removeSchemes();
            invalidate();
            return;
        }
        addSchemesFromMap();
        invalidate();
    }


    /**
     * Whether to block the date, this setting continues mCalendarInterceptListener
     *
     * @param calendar calendar
     * @return Whether to block date
     */
    protected final boolean onCalendarIntercept(Calendar calendar) {
        return mDelegate.mCalendarInterceptListener != null &&
                mDelegate.mCalendarInterceptListener.onCalendarIntercept(calendar);
    }

    /**
     * Is it within the date range
     *
     * @param calendar calendar
     * @return Is it within the date range
     */
    protected final boolean isInRange(Calendar calendar) {
        return mDelegate != null && CalendarUtil.isCalendarInRange(calendar, mDelegate);
    }

    /**
     * New current date
     */
    abstract void updateCurrentDate();

    /**
     * destroy
     */
    protected abstract void onDestroy();

    /**
     * Initialize brush related
     */
    protected void initPaint() {

    }
}
