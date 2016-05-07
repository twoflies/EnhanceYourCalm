package org.twoflies.enhanceyourcalm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(MainActivity.LOG_TAG, "Received alarm intent.");

        Intent activityIntent = new Intent(context, MainActivity.class);

        activityIntent.putExtra(MainActivity.INTENT_EXTRA_ALARM_EXPIRED, true);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        context.startActivity(activityIntent);
    }
}
