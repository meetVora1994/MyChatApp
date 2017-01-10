package app.chat.meet.mychatapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // For Google Play Services
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    FirebaseAnalytics mFirebaseAnalytics;
    // UI references.
    private EditText mUsernameView, mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private SharedPreferences sp;
    private AbstractXMPPConnection connection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Getting previous login details
        sp = getSharedPreferences(Config.SharedPrefs, Context.MODE_PRIVATE);
        String username = sp.getString(Config.spUsername, "");
        String password = sp.getString(Config.spPassword, "");
        if (!(TextUtils.isEmpty(username)) && !(TextUtils.isEmpty(password))) {
            // skipping Login screen
            Intent i = new Intent(getApplicationContext(), RosterActivity.class);
            startActivity(i);
            finish();
        }

//        getActionBar().setTitle("SignIn");

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    signIn(null);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Attempts to sign in.
     */
    public void signIn(View v) {

        if (!validateSignIn())
            return;

        if(!Config.isOnline(getApplicationContext()))
            return;

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        // perform the user login attempt.
        new LoginTask(username, password).execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("myTAG", "[checkPlayServices] onResume() returned: " + checkPlayServices());
    }

    /**
     * Validates the data and returns boolean value
     */
    private boolean validateSignIn() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return false;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            mPasswordView.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Registration Button's onClick
     */
    public void signUp(View v) {
        Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
        startActivity(i);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("myTAG", "This device is not supported.");
                finish();
            }
            return false;
        }
        // subscribing to a topic, for example "foo-bar"
        FirebaseMessaging.getInstance().subscribeToTopic("foo-bar");
        Log.d("myTAG", "SUBSCRIBED to 'foo-bar' topic!");
        return true;
    }

    private class LoginTask extends AsyncTask<Void, Void, Boolean> {

        private String username = "";
        private String password = "";

        public LoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show a progress spinner
            showProgress(true);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // Create a connection
            XMPPTCPConnectionConfiguration connConfig =
                    XMPPTCPConnectionConfiguration.builder()
                            .setHost(Config.HOST)
                            .setUsernameAndPassword(username, password)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .setPort(Integer.parseInt(Config.PORT))
                            .setDebuggerEnabled(true)
                            .setSendPresence(false)         // no need to make user online, we'll do it in next screen
                            .setServiceName(Config.SERVICE)
                            .build();
            connection = new XMPPTCPConnection(connConfig);

            try {
                connection.connect();
                Log.i("myTAG", "[LoginActivity] Connected to " + connection.getHost());
                connection.login();
                Log.i("myTAG", "[LoginActivity] Logged in as " + connection.getUser());
            } catch (SmackException | IOException | XMPPException e) {
                Log.e("myTAG", "[LoginActivity] Failed to connect to " + connection.getHost());
                Log.e("myTAG", "[LoginActivity] Exception: " + e.getMessage());
                return false;
            }
            if (connection.isConnected()) {
                connection.disconnect();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            showProgress(false);
            if (bool) {
                Toast.makeText(getApplicationContext(), "LoggedIn as: " + connection.getUser(), Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Config.spUsername, username);
                editor.putString(Config.spPassword, password);
                editor.apply();

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "user_id");
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, username);
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "imageABCD_From: Login screen");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                Intent i = new Intent(getApplicationContext(), RosterActivity.class);
                startActivity(i);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Oops! Something went wrong.\nLogin Failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

