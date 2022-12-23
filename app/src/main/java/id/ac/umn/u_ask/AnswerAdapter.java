package id.ac.umn.u_ask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AnswerAdapter extends FirestoreRecyclerAdapter<Answer, AnswerAdapter.AnswerHolder>{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference pRef;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private AlertDialog.Builder builder;
    CollectionReference achievementRef = db.collection("Achievements");
    private Post post;

    Context context;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private AnswerAdapter.OnItemClickListener listener;

    public AnswerAdapter(@NonNull FirestoreRecyclerOptions<Answer> options, Context context, Post post) {
        super(options);
        this.context = context;
        this.post = post;

    }

    @Override
    protected void onBindViewHolder(@NonNull AnswerHolder holder, int position, @NonNull Answer model) {
        holder.tvNama.setText(model.getUser().getNick());
        holder.tvDetail.setText(model.getAnswer());
        holder.tvLevel.setText(model.getUser().getLevel());
        Glide.with(context)
                .load(model.getUser().getProfileImageSource())
                .into(holder.imgProfil);

        if(model.isVerified()){
            holder.verified.setVisibility(View.VISIBLE);
        }

        if(!model.getAnswerImageSource().equals("")){
            holder.imgDetail.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(model.getAnswerImageSource())
                    .into(holder.imgDetail);
        }else{
            holder.imgDetail.setVisibility(View.GONE);
        }

        if(!mAuth.getCurrentUser().getUid().equals(model.getAid()) && !mAuth.getCurrentUser().getUid().equals(model.getUser().getUid())){
            holder.popUp.setVisibility(View.GONE);
        }

        holder.popUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu menu = new PopupMenu(context, view);
                menu.getMenuInflater().inflate(R.menu.answer_item_menu, menu.getMenu());



                if(model.isAnswered()){
                    menu.getMenu().findItem(R.id.verifyAnswer).setVisible(false);
                }

                if(!mAuth.getCurrentUser().getUid().equals(model.getAid()) && mAuth.getCurrentUser().getUid().equals(model.getUser().getUid())){
                    menu.getMenu().findItem(R.id.verifyAnswer).setVisible(false);
                }else if(mAuth.getCurrentUser().getUid().equals(model.getAid()) && !mAuth.getCurrentUser().getUid().equals(model.getUser().getUid())) {
                    menu.getMenu().findItem(R.id.editAnswer).setVisible(false);
                    menu.getMenu().findItem(R.id.deleteAnswer).setVisible(false);
                }

                if(!post.isQuestion()){
                    menu.getMenu().findItem(R.id.verifyAnswer).setVisible(false);
                }


                menu.show();
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int position;
                        switch (menuItem.getItemId()){
                            case R.id.verifyAnswer:
                                position= holder.getAbsoluteAdapterPosition();
                                verifyAnswer(model.getPid(), position, menu);
                                break;
                            case R.id.deleteAnswer:
                                position = holder.getAbsoluteAdapterPosition();
                                deleteAnswer(position);
                                break;
                            case R.id.editAnswer:
                                position = holder.getAbsoluteAdapterPosition();
                                updateAnswer(position);
                                break;

                        }
                        return false;
                    }
                });
            }
        });


    }

    @NonNull
    @Override
    public AnswerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.answer_item, parent, false);
        return new AnswerHolder(v);
    }



    class AnswerHolder extends RecyclerView.ViewHolder{
        TextView tvNama;
        TextView tvDetail;
        TextView tvLevel;
        TextView verified;
        ImageView imgDetail, imgProfil;
        ImageView popUp;


        public AnswerHolder(@NonNull View itemView) {
            super(itemView);
            tvNama = itemView.findViewById(R.id.userAnswer);
            tvDetail = itemView.findViewById(R.id.detailAnswer);
            imgDetail = itemView.findViewById(R.id.imgDetailAnswer);
            popUp = itemView.findViewById(R.id.popUpMenu);
            tvLevel = itemView.findViewById(R.id.userLevel);
            imgProfil = itemView.findViewById(R.id.userImgProfil);
            verified = itemView.findViewById(R.id.verifiedAnswer);

            imgDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if(position!=RecyclerView.NO_POSITION && listener!=null){
                        listener.onImageClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    private void verifyAnswer(String pid, int position, PopupMenu menu){
        pRef = db.collection("Posts").document(pid);
        CollectionReference answerRef = db.collection("Answers");
        DocumentReference achRef = achievementRef.document(mAuth.getCurrentUser().getUid());
        WriteBatch batch = db.batch();
        pRef.update("answered", true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                getSnapshots().getSnapshot(position).getReference().update("verified", true);
                answerRef.whereEqualTo("pid", pid).get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                                    batch.update(ds.getReference(),"answered", true);
                                }
                                batch.update(achRef, "verifiedNumber", FieldValue.increment(1));
                                batch.commit();
                                listener.setPostAnswered();
                            }
                        });
            }
        });
    }

    private void deleteAnswer(int position){
        Answer answer = getSnapshots().getSnapshot(position).toObject(Answer.class);
        builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmation")
                .setMessage("Are you sure to delete this answer?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getSnapshots().getSnapshot(position).getReference().delete();
                        if(!answer.getAnswerImageSource().equals("")){
                            StorageReference ref = storage.getReferenceFromUrl(answer.getAnswerImageSource());
                            ref.delete().addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .show();
    }

    private void updateAnswer(int position){
        listener.onItemClick(getSnapshots().getSnapshot(position), position);
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
        void setPostAnswered();
        void onImageClick(DocumentSnapshot documentSnapshot, int position);

    }

    public void setOnItemClickListener(AnswerAdapter.OnItemClickListener listener){
        this.listener = listener;
    }




}
