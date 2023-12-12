package com.coin;

import java.io.UnsupportedEncodingException;

public class CryptoInfo {
    private String name;
    private int price;
    private double rate;
    private int volume;

    public CryptoInfo(String line) {
        String[] info = line.split(" : ");
        this.name = info[0];
        this.price = Integer.parseInt(info[1].split(" KRW")[0].replace(",", ""));
        String[] rateAndVolume = info[1].split(" KRW")[1].trim().split("  ");
        this.rate = Double.parseDouble(rateAndVolume[0].replace("%", ""));
        this.volume = Integer.parseInt(rateAndVolume[1].replace(" 백만", "").replace(",", ""));
    }

    public static int getPrintfStrLength(int formatSize, String str) {
        return formatSize - (getByteLength(str) - str.length());
    }

    public static int getByteLength(String str) {
        int length=0;

        if (str!=null) {
            try {
                length = str.getBytes("euc-kr").length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return length;
    }

    @Override
    public String toString() {
        int len1 = getPrintfStrLength(35, name);
        return String.format("%-" + len1 + "s: %,13d KRW  %6.2f%%  %,11d 백만", name, price, rate, volume);
    }
}