package app.chat.meet.mychatapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
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

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A login screen that offers login via email/password.
 */
public class RegistrationActivity extends AppCompatActivity {

    // UI references.
    private EditText mFirstNameView, mLastNameView, mUsernameView, mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Set up the login form.
        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mLastNameView = (EditText) findViewById(R.id.last_name);
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    register(null);
                    return true;
                }
                return false;
            }
        });

    }

    /**
     * Registration Button's onClick
     */
    public void register(View view) {
        if (!validateRegistration())
            return;

        if(!Config.isOnline(getApplicationContext()))
            return;

        // Store values at the time of the login attempt.
        String firstName = mFirstNameView.getText().toString().trim();
        String lastName = mLastNameView.getText().toString().trim();
        String username = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        // perform the user login attempt.
        new RegisterTask(firstName, lastName, username, password).execute();
    }

    /**
     * Validates the data and returns boolean value
     */
    private boolean validateRegistration() {

        // Reset errors.
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String firstName = mFirstNameView.getText().toString().trim();
        String lastName = mLastNameView.getText().toString().trim();
        String username = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        // Check for a valid username address.
        if (TextUtils.isEmpty(firstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            mFirstNameView.requestFocus();
            return false;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(lastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            mLastNameView.requestFocus();
            return false;
        }

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

    private class RegisterTask extends AsyncTask<Void, Void, Integer> {

        private String firstName = "";
        private String lastName = "";
        private String username = "";
        private String password = "";

        public RegisterTask(String firstName, String lastName, String username, String password) {
            this.firstName = firstName;
            this.lastName = lastName;
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
        protected Integer doInBackground(Void... params) {
            // Create a connection
            XMPPTCPConnectionConfiguration connConfig =
                    XMPPTCPConnectionConfiguration.builder()
                            .setHost(Config.HOST)
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .setPort(Integer.parseInt(Config.PORT))
                            .setDebuggerEnabled(true)
                            .setServiceName(Config.SERVICE)
                            .build();
            AbstractXMPPConnection connection = new XMPPTCPConnection(connConfig);

            try {
                connection.connect();
                Log.i("myTAG", "[RegistrationActivity] Connected to " + connection.getHost());

                // Registering the user
                AccountManager accountManager = AccountManager.getInstance(connection);
                accountManager.sensitiveOperationOverInsecureConnection(true);
                if (accountManager.supportsAccountCreation()) {
                    Map<String, String> attributes = new HashMap<>();
                    attributes.put("name", firstName + " " + lastName);
                    accountManager.createAccount(username, password, attributes);
                } else {
                    return 1;
                }
            } catch (SmackException | IOException | XMPPException e) {
                Log.e("myTAG", "[RegistrationActivity] Failed to connect to " + connection.getHost());
                Log.e("myTAG", "[RegistrationActivity] Exception: " + e.getMessage());
                if (connection.isConnected())
                    connection.disconnect();
                return 2;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            showProgress(false);
            switch (integer) {
                case 0:
                    Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), "Sorry, You don't have rights to create a new User account.\nContact server administrator!", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), "Oops! Something went wrong.\nRegistration failed!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}

