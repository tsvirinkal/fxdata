package com.vts.fxdata.utils;

public class FxUtils {

    public static int getPips(double price1, double price2, double point)
    {
        return (int)((price1-price2)/point/10);
    }
}
