package id.ac.umn.u_ask;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Answer implements Parcelable {
    private String pid;
    private String aid;
    private String answer;
    private String answerImageSource;
    private boolean verified;
    private boolean answered;
    @ServerTimestamp
    private Timestamp createdAt;
    private User user;

    public Answer(){

    }

    public Answer(String pid, boolean verified, String answer, String answerImageSource, String aid, User user){
        this.pid = pid;
        this.verified = verified;
        this.answer = answer;
        this.answerImageSource = answerImageSource;
        this.aid = aid;
        this.user = user;
    }

    protected Answer(Parcel in) {
        pid = in.readString();
        aid = in.readString();
        answer = in.readString();
        answerImageSource = in.readString();
        verified = in.readByte() != 0;
        answered = in.readByte() != 0;
        createdAt = in.readParcelable(Timestamp.class.getClassLoader());
        user = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<Answer> CREATOR = new Creator<Answer>() {
        @Override
        public Answer createFromParcel(Parcel in) {
            return new Answer(in);
        }

        @Override
        public Answer[] newArray(int size) {
            return new Answer[size];
        }
    };

    public void setPid(String pid) {
        this.pid = pid;
    }


    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setAnswerImageSource(String answerImageSource) {
        this.answerImageSource = answerImageSource;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }



    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPid() {
        return pid;
    }


    public boolean isVerified() {
        return verified;
    }

    public boolean isAnswered() {
        return answered;
    }



    public String getAnswer() {
        return answer;
    }

    public String getAnswerImageSource() {
        return answerImageSource;
    }

    public String getAid() {
        return aid;
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
        parcel.writeString(pid);
        parcel.writeString(aid);
        parcel.writeString(answer);
        parcel.writeString(answerImageSource);
        parcel.writeByte((byte) (verified ? 1 : 0));
        parcel.writeByte((byte) (answered ? 1 : 0));
        parcel.writeParcelable(createdAt, i);
        parcel.writeParcelable(user, i);
    }
}
