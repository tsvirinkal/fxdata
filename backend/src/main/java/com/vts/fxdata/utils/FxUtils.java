package com.vts.fxdata.utils;

public class FxUtils {

    /**
     * Calculates the difference between two price points in pips.
     * @param price1 if greater than price2, the result will be positive.
     * @param price2 if greater than price1, the result will be negative.
     * @param point the smalest price increment for the pair. (e.g. 0.00001 for EURUSD or 0.001 for USDJPY.)
     * @return the number of pips between two prices.
     */
    public static int getPips(double price1, double price2, double point)
    {
        return (int)((price1-price2)/point/10);
    }

    /**
     * Calculates percentage of the estimated distance in pips for the current price of the pair.
     * @param price1 the current price of the pair (for bullish actions) or
     *               the start price where the price movement began on a particular timeframe (for bearish actions).
     * @param price2 the start price where the price movement began on a particular timeframe (for bullish actions) or
     *               the current price of the pair (for bearish actions).
     * @param point  the smalest price increment for the pair. (e.g. 0.00001 for EURUSD or 0.001 for USDJPY)
     * @param targetPips estimated distance in pips for the pair to move from the start price.
     * @return a percentage of the distance the current price has moved from the start price.
     */
    public static int getProgress(double price1, double price2, double point, int targetPips)
    {
        return (int)((price1-price2)/point/10 * 100.0 / targetPips);
    }
}
