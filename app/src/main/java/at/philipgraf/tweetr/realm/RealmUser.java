package at.philipgraf.tweetr.realm;

import io.realm.RealmObject;

/**
 * Created by sznew on 13.02.2016.
 */
public class RealmUser extends RealmObject {
    private String fullName;
    private String screenName;
    private String profilePictureUrl;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
