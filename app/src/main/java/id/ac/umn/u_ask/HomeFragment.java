package id.ac.umn.u_ask;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

public class HomeFragment extends Fragment {

    private AppCompatActivity act;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CardView cProgram, cEngineer, cBusiness, cCommon, cMypost, cFavPost;
    private TextView tvPost, tvLikes, tvVerified, tvNama;
    private DocumentReference achievementRef, docRef;
    private ListenerRegistration docListener, achievementListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof AppCompatActivity){
            act =(AppCompatActivity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        act = null;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        cProgram = v.findViewById(R.id.cardProgramming);
        cEngineer = v.findViewById(R.id.cardEngineer);
        cBusiness=v.findViewById(R.id.cardBusiness);
        cCommon = v.findViewById(R.id.cardCommon);
        cMypost = v.findViewById(R.id.cardPost);
        cFavPost = v.findViewById(R.id.cardFavourite);
        tvLikes = v.findViewById(R.id.totalLikesHome);
        tvPost = v.findViewById(R.id.totalPostHome);
        tvVerified = v.findViewById(R.id.totalVerifiedHome);
        tvNama = v.findViewById(R.id.tvHome);
        docRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());
        achievementRef = db.collection("Achievements").document(mAuth.getCurrentUser().getUid());


        cProgram.setOnClickListener((View view)->{
            Intent intent = new Intent(act, ExploreActivity.class);
            intent.putExtra("JENIS", "Programming");
            startActivity(intent);
        });

        cEngineer.setOnClickListener((View view)->{
            Intent intent = new Intent(act, ExploreActivity.class);
            intent.putExtra("JENIS", "Engineer");
            startActivity(intent);
        });

        cCommon.setOnClickListener((View view)->{
            Intent intent = new Intent(act, ExploreActivity.class);
            intent.putExtra("JENIS", "Common");
            startActivity(intent);
        });

        cBusiness.setOnClickListener((View view)->{
            Intent intent = new Intent(act, ExploreActivity.class);
            intent.putExtra("JENIS", "Business");
            startActivity(intent);
        });

        cMypost.setOnClickListener((View view)->{
            Intent intent = new Intent(act, MyPostActivity.class);
            intent.putExtra("UID", mAuth.getCurrentUser().getUid());
            startActivity(intent);
        });

        cFavPost.setOnClickListener((View view)->{
            Intent intent = new Intent(act, FavouriteActivity.class);
            intent.putExtra("UID", mAuth.getCurrentUser().getUid());
            startActivity(intent);
        });

        act.getSupportActionBar().setTitle("Home");
        act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return v;
    }

    @Override
    public void onStart(){
        super.onStart();

        achievementListener = achievementRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(act, "Error while loading", Toast.LENGTH_SHORT).show();
                }else{
                    if(value.exists()){
                        Achievement achievement = value.toObject(Achievement.class);
                        tvLikes.setText(String.valueOf(achievement.getLikesNumber()));
                        tvPost.setText(String.valueOf(achievement.getPostNumber()));
                        tvVerified.setText(String.valueOf(achievement.getVerifiedNumber()));
                        docListener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                if(error != null){
                                    Toast.makeText(act, "Error while loading", Toast.LENGTH_SHORT).show();
                                }else{
                                    if(value.exists()){
                                        User user = value.toObject(User.class);
                                        tvNama.setText("Welcome "+ user.getName());
                                        if(achievement.verifiedNumber > 50 && !user.getLevel().equals("Expert")){
                                            updateLevel("Expert");
                                        }else if(achievement.verifiedNumber > 40 && !user.getLevel().equals("Proficient")){
                                            updateLevel("Proficient");
                                        }else if(achievement.verifiedNumber > 30 && !user.getLevel().equals("Competent")){
                                            updateLevel("Competent");
                                        }else if(achievement.verifiedNumber > 20 && !user.getLevel().equals("Intermediate")){
                                            updateLevel("Intermediate");
                                        }else if(achievement.verifiedNumber <= 20 && !user.getLevel().equals("Beginner")){
                                            updateLevel("Beginner");
                                        }
                                    }
                                }
                            }
                        });

                    }
                }
            }
        });



    }

    @Override
    public void onStop(){
        super.onStop();
        achievementListener.remove();
        docListener.remove();
    }

    private void updateLevel(String level){
        CollectionReference answers = db.collection("Answers");
        CollectionReference posts = db.collection("Posts");
        CollectionReference favourites = db.collection("Favourites");
        DocumentReference user = db.collection("Users").document(mAuth.getCurrentUser().getUid());

        WriteBatch batch = db.batch();
        batch.update(user, "level", level);
        posts.whereEqualTo("user.uid", mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                            batch.update(ds.getReference(), "user.level", level);
                        }
                        answers.whereEqualTo("user.uid", mAuth.getCurrentUser().getUid()).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                                            batch.update(ds.getReference(), "user.level", level);
                                        }
                                        favourites.whereEqualTo("post.user.uid", mAuth.getCurrentUser().getUid()).get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                                                            batch.update(ds.getReference(), "post.user.level", level);
                                                        }
                                                        batch.commit();
                                                    }
                                                });

                                    }
                                });
                    }
                });

    }





}