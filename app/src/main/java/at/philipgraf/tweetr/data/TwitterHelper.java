package at.philipgraf.tweetr.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.core.services.StatusesService;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import at.philipgraf.tweetr.R;
import at.philipgraf.tweetr.realm.RealmTweet;
import at.philipgraf.tweetr.realm.RealmTwitter;
import io.fabric.sdk.android.Fabric;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

/**
 * Created by sznew on 08.02.2016.
 */
public class TwitterHelper {

    public static final String TWITTER_DATE_FORMAT_STRING = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "vC7uvr75GiwwjFmGNOKAtbmSA";
    private static final String TWITTER_SECRET = "sl94TW6Y6NlhH8HumUBr4pyQSjI8aPojIX7Zr3wnW4an742liU";
    private static final String TAG = "TWEETR/TWITTER(HELPER)";
    public static final String TWEET = "TWEET"; // Used for passing Parcelable
    private final View mView;
    private final Realm mRealm;

    private TwitterApiClient mTwitterApiClient;
    private Context mContext;
    private StatusesService mStatusesService;
    private SearchService mSearchService;
    private FavoriteService mFavoriteService;

    private ArrayList<Tweet> mHomeTimeline;


    /*
     * Several callbacks (also from other Files)
     * */

    public interface HomeTimelineCallback {
        void onHomeTimelineLoaded(ArrayList<Tweet> homeTimeline);

        void onError(TwitterException e);

        void onNetworkUnavailable();

        void onAPIRateLimit();
    }

    public interface ActionCallback {
        void onActionDone(int newCount);

        void onError();
    }

    public interface UpdateItemsCallback {
        void itemsChanged(int numberOfNewItems);
    }

    public interface ActionStatusChangedCallback {
        void onChange(Tweet tweet);
    }

    /*
     * TwitterHelper
     * */

    public TwitterHelper(Context context, View view) {
        mTwitterApiClient = TwitterCore.getInstance().getApiClient();
        mContext = context;
        mView = view;

        mRealm = Realm.getInstance(this.getContext());
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(mContext, new com.twitter.sdk.android.Twitter(authConfig));

        mStatusesService = mTwitterApiClient.getStatusesService();
        mSearchService = mTwitterApiClient.getSearchService();
        mFavoriteService = mTwitterApiClient.getFavoriteService();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public void updateHomeTimeline(final HomeTimelineCallback callback, final boolean showRateLimitWarning) {

        if (isNetworkAvailable()) {

            mStatusesService.homeTimeline(null, null, null, null, null, null, null, new Callback<List<com.twitter.sdk.android.core.models.Tweet>>() {
                @Override
                public void success(Result<List<com.twitter.sdk.android.core.models.Tweet>> result) {

                    ArrayList<Tweet> homeTimeline = new ArrayList<>();

                    mRealm.beginTransaction();

                    if (mRealm.where(RealmTwitter.class).findFirst() == null) {
                        mRealm.copyToRealm(new RealmTwitter());
                    }

                    RealmList<RealmTweet> realmHomeTimeline = mRealm.where(RealmTwitter.class).findFirst().getHomeTimeline();

                    homeTimeline.clear();
                    mRealm.clear(RealmTweet.class);

                    for (com.twitter.sdk.android.core.models.Tweet tweet : result.data) {
                        Tweet tweetrTweet = new Tweet(tweet);
                        homeTimeline.add(tweetrTweet);
                        realmHomeTimeline.add(tweetrTweet.getRealmTweet());
                    }

                    mHomeTimeline = homeTimeline;

                    mRealm.commitTransaction();

                    callback.onHomeTimelineLoaded(homeTimeline);
                }

                @Override
                public void failure(TwitterException e) {
                    if (e.getMessage().equals("429 Too Many Requests")) {
                        callback.onHomeTimelineLoaded(mHomeTimeline = getHomeTimelineFromRealm());

                        callback.onAPIRateLimit();
                    } else {
                        callback.onError(e);
                    }
                }
            });
        } else {
            if(mHomeTimeline == null) {

                mHomeTimeline = getHomeTimelineFromRealm();

            }

            callback.onHomeTimelineLoaded(mHomeTimeline);

            callback.onNetworkUnavailable();
        }
    }

    public ArrayList<Tweet> searchRealm(String query) {
        RealmResults<RealmTweet> results = mRealm.where(RealmTweet.class).contains("text", query, Case.INSENSITIVE).or()
                .contains("user.fullName", query, Case.INSENSITIVE).or().contains("user.screenName", query, Case.INSENSITIVE).findAll();

        return convertRealmListToTweets(results);
    }

    public void searchTwitter(String query, final HomeTimelineCallback callback) {
        if (isNetworkAvailable()) {

            mSearchService.tweets(query, null, null, null, null, null, null, null, null, null, new Callback<Search>() {
                @Override
                public void success(Result<Search> result) {
                    ArrayList<Tweet> homeTimeline = new ArrayList<>();

                    for (com.twitter.sdk.android.core.models.Tweet tweet : result.data.tweets) {
                        homeTimeline.add(new Tweet(tweet));
                    }

                    callback.onHomeTimelineLoaded(homeTimeline);
                }

                @Override
                public void failure(TwitterException e) {
                    if (e.getMessage().equals("429 Too Many Requests")) {
                        callback.onHomeTimelineLoaded(mHomeTimeline = getHomeTimelineFromRealm());

                        callback.onAPIRateLimit();
                    } else {
                        callback.onError(e);
                    }
                }
            });
        } else {
            callback.onNetworkUnavailable();
        }
    }

    public ArrayList<Tweet> discardSearch() {
        return getHomeTimelineFromRealm();
    }

    private ArrayList<Tweet> getHomeTimelineFromRealm() {
        return convertRealmListToTweets(mRealm.where(RealmTwitter.class).findFirst().getHomeTimeline());
    }

    private ArrayList<Tweet> convertRealmListToTweets( AbstractList<RealmTweet> realmList) {
        ArrayList<Tweet> tweetList = new ArrayList<>();

        for (RealmTweet realmTweet : realmList) {
            tweetList.add(new Tweet(realmTweet));
        }

        return tweetList;
    }

    public class ActionButtons {

        private final ActionStatusChangedCallback mCallback;
        private Tweet mTweet;
        private long mTweetId;
        private ImageView mRetweetButton;
        private View mRetweetView;
        private TextView mRetweetCountLabel;
        private ImageView mLikeButton;
        private View mLikeView;
        private TextView mLikeCountLabel;

        public ActionButtons(ImageView retweetButton, View retweetView, TextView retweetCountLabel,
                             ImageView likeButton, View likeView, TextView likeCountLabel, Tweet tweet, ActionStatusChangedCallback callback) {
            mRetweetButton = retweetButton;
            mRetweetView = retweetView;
            mRetweetCountLabel = retweetCountLabel;
            mLikeButton = likeButton;
            mLikeView = likeView;
            mLikeCountLabel = likeCountLabel;
            mTweet = tweet;
            mTweetId = tweet.getTweetId();
            mCallback = callback;

            // UI FIX: WHEN RETWEET BUTTON IS CLICKED

            mRetweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRetweetView.callOnClick();
                }
            });

            // UI FIX: WHEN LIKE BUTTON IS CLICKED

            mLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mLikeView.callOnClick();
                }
            });
        }

        public void setActionStatus(String action) {
            switch (action) {
                case "retweeted":
                    mTweet.setRetweeted(true);

                    mRetweetButton.setImageResource(R.drawable.twitter_retweet_on);
                    mRetweetView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setActionStatus("not_retweeted");
                            unretweet(mTweetId, new TwitterHelper.ActionCallback() {

                                // Callback for unretweet

                                @Override
                                public void onActionDone(int newCount) {
                                    Log.d(TAG, "Successfully unretweeted");
                                    mRetweetCountLabel.setText(newCount + "");
                                    mTweet.setRetweetCount(newCount);
                                }

                                @Override
                                public void onError() {
                                    setActionStatus("retweeted");
                                }
                            }, new TwitterHelper.ActionCallback() {

                                // Callback on Redo

                                @Override
                                public void onActionDone(int newCount) {
                                    setActionStatus("retweeted");
                                    Log.d(TAG, "Successfully undone unretweet");
                                    mRetweetCountLabel.setText(newCount + "");
                                    mTweet.setRetweetCount(newCount);
                                }

                                @Override
                                public void onError() {
                                    Log.d(TAG, "Couldn't undo unretweet");
                                }
                            });
                        }
                    });

                    break;

                case "not_retweeted":
                    mTweet.setRetweeted(false);

                    mRetweetButton.setImageResource(R.drawable.twitter_retweet);
                    mRetweetView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setActionStatus("retweeted");
                            retweet(mTweetId, new TwitterHelper.ActionCallback() {
                                @Override
                                public void onActionDone(int newCount) {
                                    Log.d(TAG, "Successfully retweeted");
                                    mRetweetCountLabel.setText(newCount + "");
                                    mTweet.setRetweetCount(newCount);
                                }

                                @Override
                                public void onError() {
                                    setActionStatus("not_retweeted");
                                }
                            });
                        }
                    });

                    break;

                case "liked":
                    mTweet.setLiked(true);

                    mLikeButton.setImageResource(R.drawable.twitter_like_on);
                    mLikeView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setActionStatus("not_liked");
                            unlike(mTweetId, new TwitterHelper.ActionCallback() {
                                @Override
                                public void onActionDone(int newCount) {
                                    Log.d(TAG, "Successfully unliked");
                                    mLikeCountLabel.setText(newCount + "");
                                    mTweet.setLikeCount(newCount);
                                }

                                @Override
                                public void onError() {
                                    setActionStatus("liked");
                                }
                            });
                        }
                    });

                    break;

                case "not_liked":
                    mTweet.setLiked(false);

                    mLikeButton.setImageResource(R.drawable.twitter_like);
                    mLikeView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setActionStatus("liked");
                            like(mTweetId, new TwitterHelper.ActionCallback() {
                                @Override
                                public void onActionDone(int newCount) {
                                    Log.d(TAG, "Successfully liked");
                                    mLikeCountLabel.setText(newCount + "");
                                    mTweet.setLikeCount(newCount);
                                }

                                @Override
                                public void onError() {
                                    setActionStatus("not_liked");
                                }
                            });
                        }
                    });

                    break;

                default:
                    Log.e(TAG, "Invalid Argument passed to setActionStatus(): " + action);

                    break;
            }

            // Notify TweetAdapter about change
            mCallback.onChange(mTweet);
        }

        /*
         * Actions
         * */

        public void retweet(final long id, final ActionCallback callback) {
            if (isNetworkAvailable()) {
                mStatusesService.retweet(id, null, new Callback<com.twitter.sdk.android.core.models.Tweet>() {
                    @Override
                    public void success(Result<com.twitter.sdk.android.core.models.Tweet> result) {
                        callback.onActionDone(result.data.retweetCount);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        callback.onError();
                        Snackbar.make(mView, "Konnte nicht Retweeted werden.", Snackbar.LENGTH_LONG)
                                .setAction("Erneut Versuchen", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        retweet(id, callback);
                                    }
                                })
                                .show();
                    }
                });
            } else {
                callback.onError();
                Toast.makeText(mContext, "Im Offline-Modus nicht möglich", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        public void unretweet(final long id, final ActionCallback callback, final ActionCallback redoCallback) {
            if (isNetworkAvailable()) {
                mStatusesService.unretweet(id, null, new Callback<com.twitter.sdk.android.core.models.Tweet>() {
                    @Override
                    public void success(Result<com.twitter.sdk.android.core.models.Tweet> result) {
                        callback.onActionDone((result.data.retweetCount) - 1); // -1 because it always returns old count (?!)

                        Snackbar.make(mView, "Unretweeten erfolgreich", Snackbar.LENGTH_LONG)
                                .setAction("Rückgängig", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        retweet(id, redoCallback);
                                    }
                                })
                                .show();
                    }

                    @Override
                    public void failure(TwitterException e) {
                        callback.onError();
                        Snackbar.make(mView, "Konnte nicht Unretweeted werden.", Snackbar.LENGTH_LONG)
                                .setAction("Erneut Versuchen", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unretweet(id, callback, redoCallback);
                                    }
                                })
                                .show();
                    }
                });
            } else {
                callback.onError();
                Toast.makeText(mContext, "Im Offline-Modus nicht möglich", Toast.LENGTH_SHORT)
                        .show();
            }
        }


        public void like(final long id, final ActionCallback callback) {
            if (isNetworkAvailable()) {
                mFavoriteService.create(id, null, new Callback<com.twitter.sdk.android.core.models.Tweet>() {
                    @Override
                    public void success(Result<com.twitter.sdk.android.core.models.Tweet> result) {
                        callback.onActionDone(result.data.favoriteCount);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        callback.onError();
                        Snackbar.make(mView, "Konnte nicht geliked werden", Snackbar.LENGTH_LONG)
                                .setAction("Erneut Versuchen", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        like(id, callback);
                                    }
                                })
                                .show();
                    }
                });
            } else {
                callback.onError();
                Toast.makeText(mContext, "Im Offline-Modus nicht möglich", Toast.LENGTH_SHORT)
                        .show();
            }
        }

        public void unlike(final long id, final ActionCallback callback) {
            if (isNetworkAvailable()) {
                mFavoriteService.destroy(id, null, new Callback<com.twitter.sdk.android.core.models.Tweet>() {
                    @Override
                    public void success(Result<com.twitter.sdk.android.core.models.Tweet> result) {
                        callback.onActionDone(result.data.favoriteCount);
                    }

                    @Override
                    public void failure(TwitterException e) {
                        callback.onError();
                        Snackbar.make(mView, "Konnte nicht entliked werden", Snackbar.LENGTH_LONG)
                                .setAction("Erneut Versuchen", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unlike(id, callback);
                                    }
                                })
                                .show();
                    }
                });
            } else {
                callback.onError();
                Toast.makeText(mContext, "Im Offline-Modus nicht möglich", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    public Context getContext() {
        return mContext;
    }
}

