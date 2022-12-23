package id.ac.umn.u_ask;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Field;

public class ExploreAdapter extends FirestoreRecyclerAdapter<Post, ExploreAdapter.ExploreHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference fv= db.collection("Favourites");
    private CollectionReference av= db.collection("Answers");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    CollectionReference achievementRef = db.collection("Achievements");

    private OnItemClickListener listener;
    public ExploreAdapter(@NonNull FirestoreRecyclerOptions<Post> options) {

        super(options);

    }

    @Override
    protected void onBindViewHolder(@NonNull ExploreHolder holder, int position, @NonNull Post model) {
        holder.tvNick.setText(model.getUser().getNick());
        holder.tvTitle.setText(model.getHeader());
        holder.tvJenis.setText(model.getJenis());
        if(model.getJenis().equals("Programming")){
            holder.imgExplore.setImageResource(R.drawable.ic_code_50);
        }else if(model.getJenis().equals("Engineer")){
            holder.imgExplore.setImageResource(R.drawable.ic_baseline_engineering);
        }else if(model.getJenis().equals("Business")){
            holder.imgExplore.setImageResource(R.drawable.ic_baseline_business_24);
        }else if(model.getJenis().equals("Common")){
            holder.imgExplore.setImageResource(R.drawable.ic_baseline_school);
        }else{
            holder.imgExplore.setImageResource(R.drawable.ic_baseline_discussion_blue);
        }
    }

    @NonNull
    @Override
    public ExploreHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.explore_item, parent, false);

        return new ExploreHolder(v);
    }

    class ExploreHolder extends RecyclerView.ViewHolder{
        TextView tvNick, tvTitle, tvJenis, tvViewmore;
        ImageView imgExplore;

        public ExploreHolder(@NonNull View itemView) {
            super(itemView);
            tvViewmore = itemView.findViewById(R.id.itemViewmore);
            tvNick = itemView.findViewById(R.id.itemNick);
            tvTitle = itemView.findViewById(R.id.itemTitle);
            tvJenis = itemView.findViewById(R.id.itemTipe);
            imgExplore = itemView.findViewById(R.id.exploreImage);

            tvViewmore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if(position!=RecyclerView.NO_POSITION && listener!=null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });


        }
    }

    public void deleteItem(int position){
        DocumentReference achRef = achievementRef.document(mAuth.getCurrentUser().getUid());
        Post post = getSnapshots().getSnapshot(position).toObject(Post.class);
        getSnapshots().getSnapshot(position).getReference().delete();
        WriteBatch batch = db.batch();
        fv.whereEqualTo("post.pid", post.getPid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                            batch.delete(ds.getReference());
                        }
                        av.whereEqualTo("pid", post.getPid()).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                                            batch.delete((ds.getReference()));
                                            Answer answer = ds.toObject(Answer.class);
                                            if(!answer.getAnswerImageSource().equals("")){
                                                StorageReference ref = storage.getReferenceFromUrl(answer.getAnswerImageSource());
                                                ref.delete();
                                            }
                                        }
                                        batch.update(achRef, "postNumber", FieldValue.increment(-1));
                                        batch.commit();

                                    }
                                });
                    }
                });

        if(!post.getPostImageSource().equals("")){
            StorageReference ref = storage.getReferenceFromUrl(post.getPostImageSource());
            ref.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }

    public Post editItem(int position){
        Post post = getSnapshots().getSnapshot(position).toObject(Post.class);
        return post;
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
}
