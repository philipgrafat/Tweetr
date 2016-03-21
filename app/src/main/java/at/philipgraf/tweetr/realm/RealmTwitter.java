package at.philipgraf.tweetr.realm;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by sznew on 13.02.2016.
 */
public class RealmTwitter extends RealmObject {
    private RealmList<RealmTweet> homeTimeline;

    public RealmList<RealmTweet> getHomeTimeline() {
        return homeTimeline;
    }

    public void setHomeTimeline(RealmList<RealmTweet> homeTimeline) {
        this.homeTimeline = homeTimeline;
    }
}
