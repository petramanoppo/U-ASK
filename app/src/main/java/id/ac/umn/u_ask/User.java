package id.ac.umn.u_ask;






import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class User implements Parcelable {
    private String name;
    private String email;
    private String profileImageSource;
    private String nick;
    private String uid;
    private String level;


    public User(){

    }

    public User(String name, String email, String profileImageSource, String nick, String uid, String level){
        this.name = name;
        this.email = email;
        this.profileImageSource = profileImageSource;
        this.nick = nick;
        this.uid = uid;
        this.level = level;
    }

    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();
        profileImageSource = in.readString();
        nick = in.readString();
        uid = in.readString();
        level = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileImageSource(String profileImageSource) {
        this.profileImageSource = profileImageSource;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public String getProfileImageSource() {
        return profileImageSource;
    }

    public String getNick() {
        return nick;
    }

    public String getUid() {
        return uid;
    }

    public String getLevel() {
        return level;
    }

    public String getEmail() {
        return email;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeString(profileImageSource);
        parcel.writeString(nick);
        parcel.writeString(uid);
        parcel.writeString(level);
    }
}
