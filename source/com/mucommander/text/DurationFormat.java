/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (c) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with muCommander; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.mucommander.text;

/**
 * DurationFormat formats duration in milliseconds into localized string representations.
 *
 * @author Maxence Bernard
 */
public class DurationFormat {

    private final static String SECONDS_KEY = "duration.seconds";
    private final static String MINUTES_KEY = "duration.minutes";
    private final static String HOURS_KEY = "duration.hours";
    private final static String DAYS_KEY = "duration.days";
    private final static String MONTHS_KEY = "duration.months";
    private final static String YEARS_KEY = "duration.years";

    private final static String INFINITE = Translator.get("duration.infinite");

    private final static int SECONDS_IN_MINUTE = 60;
    private final static int SECONDS_IN_HOUR = 3600;
    private final static int SECONDS_IN_DAY = 86400;
    private final static int SECONDS_IN_MONTH = 2592000;
    private final static int SECONDS_IN_YEAR = 31104000;


    public static String format(long durationMs) {
        if(durationMs/1000>Integer.MAX_VALUE)
            return INFINITE;

        int remainderSec = (int)Math.round(((float)durationMs)/1000);
        String s = "";

        String keys[] = new String[]{YEARS_KEY, MONTHS_KEY, DAYS_KEY, HOURS_KEY, MINUTES_KEY};
        int seconds[] = new int[]{SECONDS_IN_YEAR, SECONDS_IN_MONTH, SECONDS_IN_DAY, SECONDS_IN_HOUR, SECONDS_IN_MINUTE};

        for(int i=0; i<5; i++) {
            int n = remainderSec/seconds[i];
            if(n>0) {
                if(!s.equals(""))
                    s += " ";

                s += Translator.get(keys[i], ""+n);
                remainderSec = remainderSec%seconds[i];
            }
        }

        // Don't add second part if equal to 0, unless this is the only part
        if(remainderSec>0 || s.equals("")) {
            if(remainderSec==0)
                s = "<"+Translator.get(SECONDS_KEY, "1");
            else
                s += (s.equals("")?"":" ")+Translator.get(SECONDS_KEY, ""+remainderSec);
        }

        return s;
    }


    /**
     * Returns the infinite symbol string.
     */
    public static String getInfiniteSymbol() {
        return INFINITE;
    }
    

//    public static void main(String args[]) {
//        // 0s
//        System.out.println(com.mucommander.text.DurationFormat.format(0));
//        // 1s
//        System.out.println(com.mucommander.text.DurationFormat.format(1000));
//        // 1m 1s
//        System.out.println(com.mucommander.text.DurationFormat.format(61*1000));
//        // 1h 1m 1s
//        System.out.println(com.mucommander.text.DurationFormat.format(3661*1000));
//        // 1d 1h 1m 1s
//        System.out.println(com.mucommander.text.DurationFormat.format(90061*1000));
//        // 1m 1h 1m 1s
//        System.out.println(com.mucommander.text.DurationFormat.format((2592000+90061)*(long)1000));
//        // 1y 1m 1h 1m 1s
//        System.out.println(com.mucommander.text.DurationFormat.format((31104000+2592000+90061)*(long)1000));
//        // Infinite
//        System.out.println(com.mucommander.text.DurationFormat.format(1001l*Integer.MAX_VALUE));
//    }
}
