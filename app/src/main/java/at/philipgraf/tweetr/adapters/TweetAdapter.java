package at.philipgraf.tweetr.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import at.philipgraf.tweetr.R;
import at.philipgraf.tweetr.data.Tweet;
import at.philipgraf.tweetr.data.TwitterHelper;
import at.philipgraf.tweetr.data.User;
import at.philipgraf.tweetr.ui.TweetActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by sznew on 08.02.2016.
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.TweetViewHolder> {

    public static final String TAG = "TWEETR/TWEET_ADAPTER";
    private final TwitterHelper mTwitterHelper;
    private ArrayList<Tweet> mTweets;

    private Picasso mPicasso;

    public TweetAdapter(ArrayList<Tweet> tweets, TwitterHelper twitterHelper) {
        mTweets = tweets;
        mTwitterHelper = twitterHelper;
        mPicasso = Picasso.with(mTwitterHelper.getContext().getApplicationContext());
    }

    @Override
    public TweetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tweet_list_item, parent, false);
        return new TweetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TweetViewHolder holder, int position) {
        holder.bindTweet(mTweets.get(position));
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    public ArrayList<Tweet> getTweets() {
        return mTweets;
    }

    public void swap(Tweet tweet, Tweet newTweet) {
        mTweets.set(mTweets.indexOf(tweet), newTweet);
        notifyItemChanged(mTweets.indexOf(tweet));
    }

    public void swap(List<Tweet> newTweets, TwitterHelper.UpdateItemsCallback callback) {
        for(int i = 0; i<mTweets.size() && i<newTweets.size(); i++) {

            Tweet t = newTweets.get(i);

            if(t.equals(mTweets.get(i))) {
                if(i != 0){
                    if(callback != null) {
                        callback.itemsChanged(i);
                    }
                    notifyItemRangeInserted(0, i);
                }

                break;
            }

            mTweets.add(i, t);
        }
    }

    public void replace(ArrayList<Tweet> newTweets) {
        mTweets.clear();

        mTweets = newTweets;

        notifyDataSetChanged();
    }

    public class TweetViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.profileImageView)
        CircleImageView mProfileImageView;
        @Bind(R.id.screenNameLabel)
        TextView mScreenNameLabel;
        @Bind(R.id.fullNameLabel)
        TextView mFullNameLabel;

        @Bind(R.id.tweetTextLabel)
        TextView mTweetTextLabel;
        @Bind(R.id.timeLabel)
        TextView mTimeLabel;

        @Bind(R.id.replyButton)
        ImageView mReplyButton;
        @Bind(R.id.replyView)
        View mReplyView;
        @Bind(R.id.retweetButton)
        ImageView mRetweetButton;
        @Bind(R.id.retweetView)
        View mRetweetView;
        @Bind(R.id.retweetCountLabel)
        TextView mRetweetCountLabel;
        @Bind(R.id.likeButton)
        ImageView mLikeButton;
        @Bind(R.id.likeView)
        View mLikeView;
        @Bind(R.id.likeCountLabel)
        TextView mLikeCountLabel;

        @Bind(R.id.listItemLayout)
        RelativeLayout mListItemLayout;


        public TweetViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }

        public void bindTweet(final Tweet tweet) {
            User user = tweet.getUser();

            mScreenNameLabel.setText(user.getScreenName());
            mFullNameLabel.setText(user.getFullName());

            mTweetTextLabel.setText(tweet.getText());
            mTimeLabel.setText(tweet.getReadableTime());

            mRetweetCountLabel.setText(tweet.getRetweetCount() + "");
            mLikeCountLabel.setText(tweet.getLikeCount() + "");

            //ActionButton Handler

            TwitterHelper.ActionButtons actionButtons = mTwitterHelper.new ActionButtons(mRetweetButton, mRetweetView, mRetweetCountLabel,
                    mLikeButton, mLikeView, mLikeCountLabel, tweet, new TwitterHelper.ActionStatusChangedCallback() {
                @Override
                public void onChange(Tweet newTweet) {
                    mTweets.set(mTweets.indexOf(tweet), newTweet);
                }
            });

            /*
             * Retweet Action
             */

            if (tweet.isRetweeted()) {
                actionButtons.setActionStatus("retweeted");
            } else {
                actionButtons.setActionStatus("not_retweeted");
            }

            /*
             * Like Action
             */

            if (tweet.isLiked()) {
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

            // Load Profile Picture

            mPicasso.load(user.getProfilePictureUrl())
                    .into(mProfileImageView);

            // Pass Tweet to TweetActivity when clicking on the Tweet

            mListItemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mTwitterHelper.getContext(), TweetActivity.class);
                    intent.putExtra(TwitterHelper.TWEET, tweet);
                    ((Activity) mTwitterHelper.getContext()).startActivityForResult(intent, 0);
                }
            });

        }

    }
}