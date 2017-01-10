package app.chat.meet.mychatapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by Meet on 26/5/16.
 */
public final class Config {

    public static final String HOST = "192.168.3.62";
    public static final String PORT = "5222";
    public static final String SERVICE = "meet-desktop";

    // Preferences
    public static final String SharedPrefs = "mySharedPrefs";
    public static final String spUsername = "mySharedPrefs";
    public static final String spPassword = "mySharedPrefs";

    // Roster details
    public static final String roster_jid = "roster_jid";
    public static final String roster_name= "roster_name";


    /**
     * Checks whether device is online or offline
     *
     * @param context Activity's Context
     * @return true if user is online otherwise false
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if ((netInfo != null && netInfo.isConnectedOrConnecting())) return true;
        Toast.makeText(context, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
        return false;
    }

}
