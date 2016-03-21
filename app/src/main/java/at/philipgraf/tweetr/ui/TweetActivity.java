package at.philipgraf.tweetr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import at.philipgraf.tweetr.R;
import at.philipgraf.tweetr.data.Tweet;
import at.philipgraf.tweetr.data.TwitterHelper;
import at.philipgraf.tweetr.data.User;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class TweetActivity extends AppCompatActivity {

    private static final String TAG = "TWEETR/TweetActivity";
    public static final String RESULT_KEY = "RESULT";
    public static final String ORIGINAL_TWEET_KEY = "ORIGINAL_TWEET";
    private TwitterHelper mTwitterHelper;
    private Tweet mTweet;
    private Tweet mOriginalTweet;

    @Bind(R.id.profileImageView) CircleImageView mProfileImageView;
    @Bind(R.id.screenNameLabel) TextView mScreenNameLabel;
    @Bind(R.id.fullNameLabel) TextView mFullNameLabel;

    @Bind(R.id.tweetTextLabel) TextView mTweetTextLabel;
    @Bind(R.id.timeLabel) TextView mTimeLabel;

    @Bind(R.id.replyButton) ImageView mReplyButton;
    @Bind(R.id.replyView) View mReplyView;
    @Bind(R.id.retweetButton) ImageView mRetweetButton;
    @Bind(R.id.retweetView) View mRetweetView;
    @Bind(R.id.retweetCountLabel) TextView mRetweetCountLabel;
    @Bind(R.id.likeButton) ImageView mLikeButton;
    @Bind(R.id.likeView) View mLikeView;
    @Bind(R.id.likeCountLabel) TextView mLikeCountLabel;

    @Bind(R.id.tweetLayout) RelativeLayout mTweetLayout;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet);
        ButterKnife.bind(this);

        getSupportActionBar().setHomeButtonEnabled(true);

        mTweet = getIntent().getParcelableExtra(TwitterHelper.TWEET);
        mTwitterHelper = new TwitterHelper(this, mTweetLayout);

        // Initialize Views

        User user = mTweet.getUser();

        mScreenNameLabel.setText(user.getScreenName());
        mFullNameLabel.setText(user.getFullName());

        mTweetTextLabel.setText(mTweet.getText());
        mTimeLabel.setText(mTweet.getReadableTime());

        mRetweetCountLabel.setText(mTweet.getRetweetCount()+"");
        mLikeCountLabel.setText(mTweet.getLikeCount() + "");
        
        /*
         * Action Onclick Listeners
         * */

        //ActionButton Handler

        TwitterHelper.ActionButtons actionButtons = mTwitterHelper.new ActionButtons(mRetweetButton, mRetweetView, mRetweetCountLabel,
                mLikeButton, mLikeView,mLikeCountLabel, mTweet, new TwitterHelper.ActionStatusChangedCallback() {
            @Override
            public void onChange(Tweet newTweet) {
                mOriginalTweet = mTweet;
                mTweet = newTweet;
            }
        });

            /*
             * Retweet Action
             */

            if(mTweet.isRetweeted()) {
                actionButtons.setActionStatus("retweeted");
            } else {
                actionButtons.setActionStatus("not_retweeted");
            }

            /*
             * Like Action
             */

            if(mTweet.isLiked()) {
                actionButtons.setActionStatus("liked");
            } else {
                actionButtons.setActionStatus("not_liked");
            }

            /*
             * Reply Action
             */

            mReplyView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "REPLY ACTION CALL: Not Implemented yet");
                }
            });

        // Offline-Snackbar

        if(!mTwitterHelper.isNetworkAvailable()) {
            final Snackbar snackbar = Snackbar.make(mTweetLayout, "Keine Internetverbindung", Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction("Online gehen", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwitterHelper.isNetworkAvailable()) {
                        snackbar.dismiss();
                    }
                }
            }).show();
        }

        // Load Profile Picture

        Picasso.with(mTwitterHelper.getContext().getApplicationContext())
                .load(user.getProfilePictureUrl())
                .into(mProfileImageView);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            Log.d(TAG, "onKeyDown Called");
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");

        Intent intent = new Intent();

        intent.putExtra(RESULT_KEY, mTweet);
        intent.putExtra(ORIGINAL_TWEET_KEY, mOriginalTweet);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }
}
