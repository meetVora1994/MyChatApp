package app.chat.meet.mychatapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Handler;

/**
 * Created by Meet on 8/6/16.
 */
public class BaseActivity extends AppCompatActivity {

    static AbstractXMPPConnection connection = null;

    BroadcastReceiver myInternetConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("app.chat.meet.INTERNETAVAILABLE")) internetAvailable();
            else if(intent.getAction().equals("app.chat.meet.GOT_MESSAGE")){
                if(intent.hasExtra("messageBean"))
                    gotMessage(new Gson().fromJson(intent.getExtras().get("messageBean").toString(), MessageBean.class));
            }

        }
    };

    /**
     * Override this method to dynamically get internet availability status
     */
    public void internetAvailable() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myInternetConnectivityReceiver, new IntentFilter("app.chat.meet.INTERNETAVAILABLE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(myInternetConnectivityReceiver, new IntentFilter("app.chat.meet.GOT_MESSAGE"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myInternetConnectivityReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myInternetConnectivityReceiver);
    }

    /**
     * Method to connect to XMPP server and setting connection object
     */
    protected void connectToServer() {
        if (connection != null && connection.isConnected()) {
            connected(connection);
            return;
        }
        Log.d("myTAG", "[BaseActivity] Going to create XMPPConnection's Object!");
        final SharedPreferences sp = getSharedPreferences(Config.SharedPrefs, Context.MODE_PRIVATE);
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                // Create a connection
                XMPPTCPConnectionConfiguration connConfig = XMPPTCPConnectionConfiguration.builder()
                        .setHost(Config.HOST)
                        .setUsernameAndPassword(sp.getString(Config.spUsername, ""), sp.getString(Config.spPassword, ""))
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setPort(Integer.parseInt(Config.PORT))
                        .setDebuggerEnabled(true)
                        .setSendPresence(true)      // Yes, we making this user as ONLINE
                        .setServiceName(Config.SERVICE)
                        .build();
                connection = new XMPPTCPConnection(connConfig);

                try {
                    connection.connect();
                    Log.i("myTAG", "[BaseActivity] Connected to " + connection.getHost());
                    connection.login();
                    Log.i("myTAG", "[BaseActivity] Logged in as " + connection.getUser());
                } catch (SmackException | IOException | XMPPException e) {
                    Log.e("myTAG", "[BaseActivity] Failed to connect to " + connection.getHost());
                    Log.e("myTAG", "[BaseActivity] Exception: " + e.getMessage());
                    connection = null;
                    return false;
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if (aBoolean) {
                    connected(connection);
                    MyIntentService.startMessageReceiverService(getApplicationContext(), connection);
                } else {
                    // clearing past user details and going for re-login
                    sp.edit().clear().apply();
                    Toast.makeText(getApplicationContext(), "Something went wrong!\nPlease try to re-login", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }.execute();
    }

    public void connected(AbstractXMPPConnection connection) {
    }

    public void gotMessage(MessageBean messageBean) {
    }
}
