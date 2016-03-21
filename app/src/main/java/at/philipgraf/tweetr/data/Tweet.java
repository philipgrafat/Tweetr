package at.philipgraf.tweetr.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import at.philipgraf.tweetr.realm.RealmTweet;

/**
 * Created by sznew on 07.02.2016.
 */
public class Tweet implements Parcelable {
    private long mTweetId;
    private String mText;
    private int mLikeCount;
    private long mCreatedAt;
    private User mUser;
    private int mRetweetCount;
    private boolean mRetweeted;
    private boolean mLiked;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        return ((Tweet) o).mTweetId == mTweetId;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public Tweet(com.twitter.sdk.android.core.models.Tweet tweet) {

        SimpleDateFormat formatter = new SimpleDateFormat(TwitterHelper.TWITTER_DATE_FORMAT_STRING, Locale.ENGLISH);
        formatter.setLenient(true);

        try {
            mCreatedAt = formatter.parse(tweet.createdAt).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mTweetId = tweet.id;
        mText = tweet.text;
        mLikeCount = tweet.favoriteCount;
        mUser = new User(tweet.user);
        mRetweetCount = tweet.retweetCount;
        mRetweeted = tweet.retweeted;
        mLiked = tweet.favorited;
    }

    public Tweet(RealmTweet realmTweet) {

        mCreatedAt = realmTweet.getCreatedAt();

        mTweetId = realmTweet.getTweetId();
        mText = realmTweet.getText();
        mLikeCount = realmTweet.getLikeCount();
        mUser = new User(realmTweet.getUser());
        mRetweetCount = realmTweet.getRetweetCount();
        mRetweeted = realmTweet.isRetweeted();
        mLiked = realmTweet.isLiked();
    }

    /*
     * Getters and Setters
     * */

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public int getLikeCount() {
        return mLikeCount;
    }

    public void setLikeCount(int likeCount) {
        mLikeCount = likeCount;
    }

    public User getUser() {
        return mUser;
    }

    public boolean isRetweeted() {
        return mRetweeted;
    }

    public void setRetweeted(boolean retweeted) {
        mRetweeted = retweeted;
    }

    public boolean isLiked() {
        return mLiked;
    }

    public void setLiked(boolean liked) {
        mLiked = liked;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public int getRetweetCount() {
        return mRetweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        mRetweetCount = retweetCount;
    }

    public String getReadableTime() {
        PrettyTime time = new PrettyTime(Locale.GERMAN);
        return time.format(new Date(mCreatedAt));
    }

    public long getTweetId() {
        return mTweetId;
    }

    public void setTweetId(long tweetId) {
        mTweetId = tweetId;
    }


    /*
     * Parcelable
     * */

    @Override
    public int describeContents() {
        return 0; // ignore
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mText);
        dest.writeInt(mLikeCount);
        dest.writeLong(mCreatedAt);
        dest.writeParcelable(mUser, flags);
        dest.writeInt(mRetweetCount);
        dest.writeValue(mRetweeted);
        dest.writeValue(mLiked);
        dest.writeLong(mTweetId);
    }

    private Tweet(Parcel in) {
        mText = in.readString();
        mLikeCount = in.readInt();
        mCreatedAt = in.readLong();
        mUser = in.readParcelable(User.class.getClassLoader());
        mRetweetCount = in.readInt();
        mRetweeted = (Boolean) in.readValue(null);
        mLiked = (Boolean) in.readValue(null);
        mTweetId = in.readLong();
    }

    public static final Creator<Tweet> CREATOR = new Creator<Tweet>() {
        @Override
        public Tweet createFromParcel(Parcel source) {
            return new Tweet(source);
        }

        @Override
        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };

    public RealmTweet getRealmTweet() {
        RealmTweet realmTweet = new RealmTweet();

        realmTweet.setText(mText);
        realmTweet.setLikeCount(mLikeCount);
        realmTweet.setCreatedAt(mCreatedAt);
        realmTweet.setUser(mUser.getRealmUser());
        realmTweet.setRetweetCount(mRetweetCount);
        realmTweet.setRetweeted(mRetweeted);
        realmTweet.setLiked(mLiked);
        realmTweet.setTweetId(mTweetId);

        return realmTweet;
    }
}
