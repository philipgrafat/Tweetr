package at.philipgraf.tweetr.realm;

import io.realm.RealmObject;

/**
 * Created by sznew on 13.02.2016.
 */
public class RealmTweet extends RealmObject {
    private long tweetId;
    private String text;
    private long createdAt;
    private RealmUser user;
    private int retweetCount;
    private int likeCount;
    private boolean retweeted;
    private boolean liked;

    public long getTweetId() {
        return tweetId;
    }

    public void setTweetId(long tweetId) {
        this.tweetId = tweetId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public RealmUser getUser() {
        return user;
    }

    public void setUser(RealmUser user) {
        this.user = user;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}

