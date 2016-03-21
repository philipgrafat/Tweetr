package at.philipgraf.tweetr.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import at.philipgraf.tweetr.R;
import io.fabric.sdk.android.Fabric;

/**
 * The Activity is never displayed. It only checks if the user is logged in
 **/

public class LauncherActivity extends Activity {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "vC7uvr75GiwwjFmGNOKAtbmSA";
    private static final String TWITTER_SECRET = "sl94TW6Y6NlhH8HumUBr4pyQSjI8aPojIX7Zr3wnW4an742liU";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_login);

        if(TwitterCore.getInstance().getSessionManager().getActiveSession() != null) {
            //Switch to main activity

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Switch to login activity

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
