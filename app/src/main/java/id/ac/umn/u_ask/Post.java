package id.ac.umn.u_ask;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Post implements Parcelable {
    private String header;
    private String body;
    private String jenis;
    private String postImageSource = "";
    private String pid;
    private boolean answered = false;
    private boolean question;
    @ServerTimestamp
    private Timestamp createdAt;
    private User user;

    public Post(){

    }

    public Post(String header, String body, String jenis, String postImageSource, User user, Boolean question){
        this.header = header;
        this.body = body;
        this.jenis = jenis;
        this.postImageSource = postImageSource;
        this.user = user;
        this.question = question;
    }

    protected Post(Parcel in) {
        header = in.readString();
        body = in.readString();
        jenis = in.readString();
        postImageSource = in.readString();
        pid = in.readString();
        answered = in.readByte() != 0;
        question = in.readByte() != 0;
        createdAt = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public void setHeader(String header) {
        this.header = header;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public void setPostImageSource(String postImageSource) {
        this.postImageSource = postImageSource;
    }

    public void setQuestion(boolean question) {
        this.question = question;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public String getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public String getJenis() {
        return jenis;
    }

    public String getPostImageSource() {
        return postImageSource;
    }

    public String getPid() {
        return pid;
    }

    public boolean isQuestion() {
        return question;
    }

    public boolean isAnswered() {
        return answered;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(header);
        parcel.writeString(body);
        parcel.writeString(jenis);
        parcel.writeString(postImageSource);
        parcel.writeString(pid);
        parcel.writeByte((byte) (answered ? 1 : 0));
        parcel.writeByte((byte) (question ? 1 : 0));
        parcel.writeParcelable(createdAt, i);
    }
}
