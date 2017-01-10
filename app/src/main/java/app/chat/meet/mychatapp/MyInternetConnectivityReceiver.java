package app.chat.meet.mychatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * The Broadcast receiver to monitor changes in Network Connectivity
 */
public class MyInternetConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            Log.d("myTAG", "Connectivity Changed!");
            if (Config.isOnline(context))
                context.sendBroadcast(new Intent("app.chat.meet.INTERNETAVAILABLE"));
        }
    }
}