package com.coin;

import java.io.UnsupportedEncodingException;

public class CryptoInfo {
    private String name;
    private double price;
    private double rate;
    private double volume;

    public CryptoInfo(String line) {
        String[] info = line.split(" : ");
        this.name = info[0];
        this.price = Double.parseDouble(info[1].split(" KRW")[0].replace(",", ""));
        String[] rateAndVolume = info[1].split(" KRW")[1].trim().split("  ");
        this.rate = Double.parseDouble(rateAndVolume[0].replace("%", ""));
        this.volume = Double.parseDouble(rateAndVolume[1].replace("백만", "").replace(",", ""));
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

    private int getLength(String str)  {
        try {
            return str.getBytes("euc-kr").length;
        } catch (UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    @Override
    public String toString() {
        int len1 = getPrintfStrLength(35, name);
        return String.format("%-" + len1 + "s: %,13.2f KRW  %6.2f%%  %,11.2f 백만", name, price, rate, volume);
    }
}