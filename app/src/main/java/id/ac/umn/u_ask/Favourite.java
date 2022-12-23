package id.ac.umn.u_ask;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class Favourite {


    private String uid_fav;
    private Post post;
    @ServerTimestamp
    private Timestamp addedAt;


    public Favourite(){

    }

    public Favourite(String uid_fav, Post post){
        this.uid_fav = uid_fav;
        this.post = post;
    }

    public Post getPost() {
        return post;
    }

    public String getUid_fav() {
        return uid_fav;
    }

    public Timestamp getAddedAt() {
        return addedAt;
    }
}
