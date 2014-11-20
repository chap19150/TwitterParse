package com.applicoinc.twitterparse;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import io.fabric.sdk.android.Fabric;


public class LoginActivity extends Activity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "";
    private static final String TWITTER_SECRET = "";

    public static final String PARSE_APP_ID = "";
    public static final String PARSE_CLIENT_ID = "";

    private TwitterLoginButton loginButton;

    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);

        Fabric.with(this, new Twitter(authConfig));
        Parse.initialize(this, PARSE_APP_ID, PARSE_CLIENT_ID);
        ParseTwitterUtils.initialize(TWITTER_KEY, TWITTER_SECRET);

        setContentView(R.layout.activity_login);
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                String twitterId = String.valueOf(twitterSessionResult.data.getUserId());
                String screenName = twitterSessionResult.data.getUserName();
                String authToken = twitterSessionResult.data.getAuthToken().token;
                String authSecret = twitterSessionResult.data.getAuthToken().secret;
                LoginActivity.this.progressDialog = ProgressDialog.show(
                        LoginActivity.this, "", "Logging in...", true);
                ParseTwitterUtils.logIn(twitterId, screenName, authToken, authSecret, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        LoginActivity.this.progressDialog.dismiss();
                        if (parseUser == null) {
                            Log.d(TAG, "user is null");
                        } else if (parseUser.isNew()){
                            Log.d(TAG, "user is signed and logged in proceed into app");
                        } else{
                            Log.d(TAG, "user is signed and logged in finish profile");
                            //if I don't do this it only saves as an anon user not a twitter user
                            parseUser.saveInBackground();
                        }
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
