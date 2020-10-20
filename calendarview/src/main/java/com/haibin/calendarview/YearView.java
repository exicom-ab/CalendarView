package com.haibin.calendarview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * Year view
 * Created by huanghaibin on 2018/10/9.
 */
@SuppressWarnings("unused")
public abstract class YearView extends View {

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
     * Month brush
     */
    protected Paint mMonthTextPaint = new Paint();

    /**
     * Weekly brush
     */
    protected Paint mWeekTextPaint = new Paint();

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
     * Text baseline
     */
    protected float mMonthTextBaseLine;

    /**
     * Text baseline
     */
    protected float mWeekTextBaseLine;

    /**
     * Current calendar card year
     */
    protected int mYear;

    /**
     * Current calendar card month
     */
    protected int mMonth;

    /**
     * Number of shifts in the next month
     */
    protected int mNextDiff;

    /**
     * Week start
     */
    protected int mWeekStart;

    /**
     * Number of calendar rows
     */
    protected int mLineCount;

    public YearView(Context context) {
        this(context, null);
    }

    public YearView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }


    /**
     * Initial configuration
     */
    private void initPaint() {
        mCurMonthTextPaint.setAntiAlias(true);
        mCurMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurMonthTextPaint.setColor(0xFF111111);
        mCurMonthTextPaint.setFakeBoldText(true);

        mOtherMonthTextPaint.setAntiAlias(true);
        mOtherMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mOtherMonthTextPaint.setColor(0xFFe1e1e1);
        mOtherMonthTextPaint.setFakeBoldText(true);

        mCurMonthLunarTextPaint.setAntiAlias(true);
        mCurMonthLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mSelectedLunarTextPaint.setAntiAlias(true);
        mSelectedLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mOtherMonthLunarTextPaint.setAntiAlias(true);
        mOtherMonthLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mMonthTextPaint.setAntiAlias(true);
        mMonthTextPaint.setFakeBoldText(true);

        mWeekTextPaint.setAntiAlias(true);
        mWeekTextPaint.setFakeBoldText(true);
        mWeekTextPaint.setTextAlign(Paint.Align.CENTER);

        mSchemeLunarTextPaint.setAntiAlias(true);
        mSchemeLunarTextPaint.setTextAlign(Paint.Align.CENTER);

        mSchemeTextPaint.setAntiAlias(true);
        mSchemeTextPaint.setStyle(Paint.Style.FILL);
        mSchemeTextPaint.setTextAlign(Paint.Align.CENTER);
        mSchemeTextPaint.setColor(0xffed5353);
        mSchemeTextPaint.setFakeBoldText(true);

        mSelectTextPaint.setAntiAlias(true);
        mSelectTextPaint.setStyle(Paint.Style.FILL);
        mSelectTextPaint.setTextAlign(Paint.Align.CENTER);
        mSelectTextPaint.setColor(0xffed5353);
        mSelectTextPaint.setFakeBoldText(true);

        mSchemePaint.setAntiAlias(true);
        mSchemePaint.setStyle(Paint.Style.FILL);
        mSchemePaint.setStrokeWidth(2);
        mSchemePaint.setColor(0xffefefef);

        mCurDayTextPaint.setAntiAlias(true);
        mCurDayTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurDayTextPaint.setColor(Color.RED);
        mCurDayTextPaint.setFakeBoldText(true);

        mCurDayLunarTextPaint.setAntiAlias(true);
        mCurDayLunarTextPaint.setTextAlign(Paint.Align.CENTER);
        mCurDayLunarTextPaint.setColor(Color.RED);
        mCurDayLunarTextPaint.setFakeBoldText(true);

        mSelectedPaint.setAntiAlias(true);
        mSelectedPaint.setStyle(Paint.Style.FILL);
        mSelectedPaint.setStrokeWidth(2);
    }

    /**
     * Set up
     *
     * @param delegate delegate
     */
    final void setup(CalendarViewDelegate delegate) {
        this.mDelegate = delegate;
        updateStyle();
    }

    final void updateStyle(){
        if(mDelegate == null){
            return;
        }
        this.mCurMonthTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());
        this.mSchemeTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());
        this.mOtherMonthTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());
        this.mCurDayTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());
        this.mSelectTextPaint.setTextSize(mDelegate.getYearViewDayTextSize());

        this.mSchemeTextPaint.setColor(mDelegate.getYearViewSchemeTextColor());
        this.mCurMonthTextPaint.setColor(mDelegate.getYearViewDayTextColor());
        this.mOtherMonthTextPaint.setColor(mDelegate.getYearViewDayTextColor());
        this.mCurDayTextPaint.setColor(mDelegate.getYearViewCurDayTextColor());
        this.mSelectTextPaint.setColor(mDelegate.getYearViewSelectTextColor());
        this.mMonthTextPaint.setTextSize(mDelegate.getYearViewMonthTextSize());
        this.mMonthTextPaint.setColor(mDelegate.getYearViewMonthTextColor());
        this.mWeekTextPaint.setColor(mDelegate.getYearViewWeekTextColor());
        this.mWeekTextPaint.setTextSize(mDelegate.getYearViewWeekTextSize());
    }

    /**
     * Initialize year view
     *
     * @param year  year
     * @param month month
     */
    final void init(int year, int month) {
        mYear = year;
        mMonth = month;
        mNextDiff = CalendarUtil.getMonthEndDiff(mYear, mMonth, mDelegate.getWeekStart());
        int preDiff = CalendarUtil.getMonthViewStartDiff(mYear, mMonth, mDelegate.getWeekStart());

        mItems = CalendarUtil.initCalendarForMonthView(mYear, mMonth, mDelegate.getCurrentDay(), mDelegate.getWeekStart());

        mLineCount = 6;
        addSchemesFromMap();

    }

    /**
     * Measure size
     *
     * @param width  width
     * @param height height
     */
    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    final void measureSize(int width, int height) {

        Rect rect = new Rect();
        mCurMonthTextPaint.getTextBounds("1", 0, 1, rect);
        int textHeight = rect.height();
        int mMinHeight = 12 * textHeight + getMonthViewTop();

        int h = height >= mMinHeight ? height : mMinHeight;

        getLayoutParams().width = width;
        getLayoutParams().height = h;
        mItemHeight = (h - getMonthViewTop()) / 6;

        Paint.FontMetrics metrics = mCurMonthTextPaint.getFontMetrics();
        mTextBaseLine = mItemHeight / 2 - metrics.descent + (metrics.bottom - metrics.top) / 2;

        Paint.FontMetrics monthMetrics = mMonthTextPaint.getFontMetrics();
        mMonthTextBaseLine = mDelegate.getYearViewMonthHeight() / 2 - monthMetrics.descent +
                (monthMetrics.bottom - monthMetrics.top) / 2;

        Paint.FontMetrics weekMetrics = mWeekTextPaint.getFontMetrics();
        mWeekTextBaseLine = mDelegate.getYearViewWeekHeight() / 2 - weekMetrics.descent +
                (weekMetrics.bottom - weekMetrics.top) / 2;

        invalidate();
    }

    /**
     * Add event marker, from Map
     */
    private void addSchemesFromMap() {
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
    protected void onDraw(Canvas canvas) {
        mItemWidth = (getWidth() - 2 * mDelegate.getYearViewPadding()) / 7;
        onPreviewHook();
        onDrawMonth(canvas);
        onDrawWeek(canvas);
        onDrawMonthView(canvas);
    }

    /**
     * draw
     *
     * @param canvas canvas
     */
    private void onDrawMonth(Canvas canvas) {
        onDrawMonth(canvas,
                mYear, mMonth,
                mDelegate.getYearViewPadding(),
                mDelegate.getYearViewMonthMarginTop(),
                getWidth() - 2 * mDelegate.getYearViewPadding(),
                mDelegate.getYearViewMonthHeight() +
                        mDelegate.getYearViewMonthMarginTop());
    }

    private int getMonthViewTop() {
        return mDelegate.getYearViewMonthMarginTop() +
                mDelegate.getYearViewMonthHeight() +
                mDelegate.getYearViewMonthMarginBottom() +
                mDelegate.getYearViewWeekHeight();
    }

    /**
     * draw
     *
     * @param canvas canvas
     */
    private void onDrawWeek(Canvas canvas) {
        if (mDelegate.getYearViewWeekHeight() <= 0) {
            return;
        }
        int week = mDelegate.getWeekStart();
        if (week > 0) {
            week -= 1;
        }
        int width = (getWidth() - 2 * mDelegate.getYearViewPadding()) / 7;
        for (int i = 0; i < 7; i++) {
            onDrawWeek(canvas,
                    week,
                    mDelegate.getYearViewPadding() + i * width,
                    mDelegate.getYearViewMonthHeight() +
                            mDelegate.getYearViewMonthMarginTop() +
                            mDelegate.getYearViewMonthMarginBottom(),
                    width,
                    mDelegate.getYearViewWeekHeight());
            week += 1;
            if (week >= 7) {
                week = 0;
            }

        }
    }

    /**
     * Plot monthly data
     *
     * @param canvas canvas
     */
    private void onDrawMonthView(Canvas canvas) {

        int count = mLineCount * 7;
        int d = 0;
        for (int i = 0; i < mLineCount; i++) {
            for (int j = 0; j < 7; j++) {
                Calendar calendar = mItems.get(d);
                if (d > mItems.size() - mNextDiff) {
                    return;
                }
                if (!calendar.isCurrentMonth()) {
                    ++d;
                    continue;
                }
                draw(canvas, calendar, i, j, d);
                ++d;
            }
        }
    }


    /**
     * Start drawing
     *
     * @param canvas   canvas
     * @param calendar Corresponding calendar
     * @param i        i
     * @param j        j
     * @param d        d
     */
    private void draw(Canvas canvas, Calendar calendar, int i, int j, int d) {
        int x = j * mItemWidth + mDelegate.getYearViewPadding();
        int y = i * mItemHeight + getMonthViewTop();

        boolean isSelected = calendar.equals(mDelegate.mSelectedCalendar);
        boolean hasScheme = calendar.hasScheme();

        if (hasScheme) {
            //Marked day
            boolean isDrawSelected = false;//Whether to continue drawing the selected onDrawScheme
            if (isSelected) {
                isDrawSelected = onDrawSelected(canvas, calendar, x, y, true);
            }
            if (isDrawSelected || !isSelected) {
                //Set the brush as the marker color
                mSchemePaint.setColor(calendar.getSchemeColor() != 0 ? calendar.getSchemeColor() : mDelegate.getSchemeThemeColor());
                onDrawScheme(canvas, calendar, x, y);
            }
        } else {
            if (isSelected) {
                onDrawSelected(canvas, calendar, x, y, false);
            }
        }
        onDrawText(canvas, calendar, x, y, hasScheme, isSelected);
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
     * Plot month
     *
     * @param canvas canvas
     * @param year   year
     * @param month  month
     * @param x      x
     * @param y      y
     * @param width  width
     * @param height height
     */
    protected abstract void onDrawMonth(Canvas canvas, int year, int month, int x, int y, int width, int height);


    /**
     * Plot the week column of the year view
     *
     * @param canvas canvas
     * @param week   week
     * @param x      x
     * @param y      y
     * @param width  width
     * @param height height
     */
    protected abstract void onDrawWeek(Canvas canvas, int week, int x, int y, int width, int height);


    /**
     * Draw the selected date
     *
     * @param canvas    canvas
     * @param calendar  Calendar calendar
     * @param x         Calendar Card x starting point coordinates
     * @param y         Calendar Card y starting point coordinates
     * @param hasScheme hasScheme Unmarked date
     * @return Whether to draw onDrawScheme，true or false
     */
    protected abstract boolean onDrawSelected(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme);

    /**
     * The date when the marker is drawn, here can be the background color, marker color or something
     *
     * @param canvas   canvas
     * @param calendar calendar
     * @param x        Calendar Card x starting point coordinates
     * @param y        Calendar Card y starting point coordinates
     */
    protected abstract void onDrawScheme(Canvas canvas, Calendar calendar, int x, int y);


    /**
     * Draw calendar text
     *
     * @param canvas     canvas
     * @param calendar   calendar
     * @param x          Calendar Card x starting point coordinates
     * @param y          Calendar Card y starting point coordinates
     * @param hasScheme  Is it a marked date
     * @param isSelected Whether selected
     */
    protected abstract void onDrawText(Canvas canvas, Calendar calendar, int x, int y, boolean hasScheme, boolean isSelected);
}
