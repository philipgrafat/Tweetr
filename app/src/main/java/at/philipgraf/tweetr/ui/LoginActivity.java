package at.philipgraf.tweetr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import at.philipgraf.tweetr.R;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    TwitterAuthClient mTwitterAuthClient;

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "vC7uvr75GiwwjFmGNOKAtbmSA";
    private static final String TWITTER_SECRET = "sl94TW6Y6NlhH8HumUBr4pyQSjI8aPojIX7Zr3wnW4an742liU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        ButterKnife.bind(this);

        mTwitterAuthClient = new TwitterAuthClient();
    }

    @OnClick(R.id.loginButton)
    public void onClick(View v) {
        mTwitterAuthClient.authorize(this, new com.twitter.sdk.android.core.Callback<TwitterSession>() {

            @Override
            public void success(Result<TwitterSession> twitterSessionResult) {
                // Success

                //Switch to main activity

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        super.onActivityResult(requestCode, responseCode, intent);
        mTwitterAuthClient.onActivityResult(requestCode, responseCode, intent);
    }

}