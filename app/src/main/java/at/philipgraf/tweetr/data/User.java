package at.philipgraf.tweetr.data;

import android.os.Parcel;
import android.os.Parcelable;

import at.philipgraf.tweetr.realm.RealmUser;

/**
 * Created by sznew on 08.02.2016.
 */
public class User implements Parcelable {
    private String mFullName;
    private String mScreenName;
    private String mProfilePictureUrl;

    public User(com.twitter.sdk.android.core.models.User user) {
        mFullName = user.name;
        mScreenName = "@"+user.screenName;
        mProfilePictureUrl = user.profileImageUrlHttps.replace("_normal", "_bigger");
    }

    public User(RealmUser realmUser) {
        mFullName = realmUser.getFullName();
        if(realmUser.getScreenName().toCharArray()[0] == '@') {
            mScreenName = realmUser.getScreenName();
        } else {
            mScreenName = "@" + realmUser.getScreenName();
        }
        mProfilePictureUrl = realmUser.getProfilePictureUrl().replace("_normal", "_bigger");
    }

    /*
     * Getters and Setters
     * */

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String name) {
        mFullName = name;
    }

    public String getScreenName() {
        return mScreenName;
    }

    public void setScreenName(String screenName) {
        mScreenName = screenName;
    }

    public String getProfilePictureUrl() {
        return mProfilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        mProfilePictureUrl = profilePictureUrl;
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
        dest.writeString(mFullName);
        dest.writeString(mScreenName);
        dest.writeString(mProfilePictureUrl);
    }

    private User(Parcel in) {
        mFullName = in.readString();
        mScreenName = in.readString();
        mProfilePictureUrl = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public RealmUser getRealmUser() {
        RealmUser realmUser = new RealmUser();

        realmUser.setFullName(mFullName);
        realmUser.setScreenName(mScreenName);
        realmUser.setProfilePictureUrl(mProfilePictureUrl);

        return realmUser;
    }
}
