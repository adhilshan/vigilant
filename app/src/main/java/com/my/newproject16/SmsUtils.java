package com.my.newproject16;

import android.telephony.SmsManager;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.os.Build;


public class SmsUtils {

    public static void sendSms(Context context, int subscriptionId, String phoneNumber, String message) {
        SmsManager smsManager;

        if (Build.VERSION.SDK_INT >= 31) {
            TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class);
            smsManager = SmsManager.getSmsManagerForSubscriptionId(telephonyManager.getSubscriptionId());
        } else {
            smsManager = SmsManager.getDefault();
        }

        smsManager.sendMultipartTextMessage(phoneNumber, null, smsManager.divideMessage(message), null, null);
    }
}
