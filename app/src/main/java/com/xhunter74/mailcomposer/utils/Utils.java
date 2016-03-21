package com.xhunter74.mailcomposer.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Patterns;

public class Utils {

    public static String[] getEmailsFromString(String addresses) {
        String[] emails = null;
        if (!TextUtils.isEmpty(addresses)) {
            addresses = addresses.replace(',', ';');
            if (addresses.contains(";")) {
                emails = addresses.split(";");
            } else {
                emails = new String[]{addresses};
            }
        }
        return emails;
    }

    public static boolean isValidEmails(String emailString) {
        boolean result = true;
        String[] emails = Utils.getEmailsFromString(emailString);
        for (String email : emails) {
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                result = false;
                break;
            }
        }
        return result;
    }

    public static boolean isDeviceOnline(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
