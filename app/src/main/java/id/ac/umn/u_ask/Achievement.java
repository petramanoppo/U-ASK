package id.ac.umn.u_ask;

public class Achievement {
    int postNumber;
    int verifiedNumber;
    int likesNumber;

    public Achievement(){

    }

    public Achievement(int postNumber, int verifiedNumber, int likesNumber){
        this.postNumber = postNumber;
        this.verifiedNumber = verifiedNumber;
        this.likesNumber = likesNumber;
    }

    public void setPostNumber(int postNumber) {
        this.postNumber = postNumber;
    }

    public void setVerifiedNumber(int verifiedNumber) {
        this.verifiedNumber = verifiedNumber;
    }

    public void setLikesNumber(int likesNumber) {
        this.likesNumber = likesNumber;
    }

    public int getPostNumber() {
        return postNumber;
    }

    public int getVerifiedNumber() {
        return verifiedNumber;
    }

    public int getLikesNumber() {
        return likesNumber;
    }
}
