package com.ape.shortcutsUtils;

import android.content.Context;
import android.content.res.Resources;

//import com.android.launcher3.R;

import java.util.regex.Pattern;

/**
 * Created by christopher ney on 13/07/16.
 */
public class StringUtils {

    /*public static String removeAccents(Context context, String value) {

        Resources res = context.getResources();
        String[] arrayAccents = res.getStringArray(R.array.accents);

        StringBuilder builder = new StringBuilder();

        for (char c : value.toCharArray()) {

            String regexAccents = null;

            for (String accents : arrayAccents) {

                if (accents.contains(Character.toString(c))) {
                    regexAccents = accents;
                    break;
                }
            }

            if (regexAccents != null && regexAccents.length() >= 1) {
                builder.append(regexAccents.substring(0, 1));
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }*/

    public static boolean isNumeric(String str) {
         return Pattern.matches("\\-?\\d+", (CharSequence)str);
    }

    public static String capitaliseFirstLetter(String value) {
        if (value != null) {
            if (value.length() > 1) {
                return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
            } else {
                return value.toUpperCase();
            }
        } else {
            return null;
        }
    }
}
