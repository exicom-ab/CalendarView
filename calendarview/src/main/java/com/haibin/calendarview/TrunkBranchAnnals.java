package com.haibin.calendarview;

import android.content.Context;

/**
 * Ganzhi chronology algorithm
 */
@SuppressWarnings("unused")
public final class TrunkBranchAnnals {

    /**
     * Zodiac string
     */
    private static String[] TRUNK_STR = null;

    /**
     * Earthly Branch String
     */
    private static String[] BRANCH_STR = null;

    /**
     * Please call this method for single use
     * @param context context
     */
    public static void init(Context context) {
        if (TRUNK_STR != null) {
            return;
        }
        TRUNK_STR = context.getResources().getStringArray(R.array.trunk_string_array);
        BRANCH_STR = context.getResources().getStringArray(R.array.branch_string_array);

    }

    /**
     * Get the corresponding Chinese characters of a certain year
     *
     * @param year years
     * @return Heavenly stem from Jia to Kui, every 10 cycles
     */
    @SuppressWarnings("all")
    public static String getTrunkString(int year) {
        return TRUNK_STR[getTrunkInt(year)];
    }

    /**
     * Get the corresponding heavenly stems of a certain year，
     *
     * @param year years
     * @return 4 5 6 7 8 9 10 1 2 3
     */
    @SuppressWarnings("all")
    public static int getTrunkInt(int year) {
        int trunk = year % 10;
        return trunk == 0 ? 9 : trunk - 1;
    }

    /**
     * Get the corresponding earthly branch text for a certain year
     *
     * @param year years
     * @return Earthly Branch from Zi to Hai, every 12 cycles
     */
    @SuppressWarnings("all")
    public static String getBranchString(int year) {
        return BRANCH_STR[getBranchInt(year)];
    }

    /**
     * Obtain the corresponding earthly branch of a certain year
     *
     * @param year years
     * @return 4 5 6 7 8 9 10 11 12 1 2 3
     */
    @SuppressWarnings("all")
    public static int getBranchInt(int year) {
        int branch = year % 12;
        return branch == 0 ? 11 : branch - 1;
    }

    /**
     * Obtain the chronology
     *
     * @param year years
     * @return Ganzhi Chronicle
     */
    public static String getTrunkBranchYear(int year) {
        return String.format("%s%s", getTrunkString(year), getBranchString(year));
    }
}
