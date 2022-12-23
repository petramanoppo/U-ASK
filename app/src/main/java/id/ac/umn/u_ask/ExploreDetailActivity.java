package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ExploreDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference fv= db.collection("Favourites");
    private CollectionReference aRef= db.collection("Answers");
    private CollectionReference achievementRef= db.collection("Achievements");

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Post post;
    User user;
    Answer answer;
    private ProgressDialog pd;

    TextView title, body, nama, answerBtn;
    ImageView imageDetail, imageProfil;

    private AnswerAdapter adapter;

    private RecyclerView rv;

    private WrapContentLinearLayoutManager wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_detail);
        title = findViewById(R.id.exploreDetailTitle);
        body = findViewById(R.id.exploreDetailBody);
        nama = findViewById(R.id.NamaUser);
        answerBtn = findViewById(R.id.buttonAnswer);
        imageDetail = findViewById(R.id.imgDetail);
        imageProfil = findViewById(R.id.userImgProfilDetail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Question Detail");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));




        post = getIntent().getParcelableExtra("POST");
        user = getIntent().getParcelableExtra("USER");
        post.setUser(user);

        Glide.with(this)
                .load(user.getProfileImageSource())
                .into(imageProfil);


        pd = new ProgressDialog(ExploreDetailActivity.this);
        pd.setTitle("Loading");
        pd.setMessage("Please Wait");
        pd.setCancelable(false);



        title.setText(post.getHeader());
        body.setText(post.getBody());
        nama.setText(user.getNick());
        if(post.getPostImageSource().equals("")){
            imageDetail.setVisibility(View.GONE);
        }else{
            Glide.with(this)
                    .load(post.getPostImageSource())
                    .into(imageDetail);

            imageDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ExploreDetailActivity.this, ZoomActivity.class);
                    intent.putExtra("IMAGE-REF", post.getPostImageSource());
                    startActivity(intent);


                }
            });
        }

        answerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExploreDetailActivity.this, MakeAnswerActivity.class);
                intent.putExtra("POST", post);
                intent.putExtra("USER", user);
                startActivity(intent);
            }
        });

        setUpRecycler();
    }

    private void setUpRecycler(){
        Query query = aRef.whereEqualTo("pid", post.getPid()).orderBy("createdAt");
        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder<Answer>()
                .setQuery(query, Answer.class)
                .build();
        rv = findViewById(R.id.recyclerAnswer);


        adapter = new AnswerAdapter(options, ExploreDetailActivity.this, post);
        adapter.startListening();

        if(wl == null) {
            wl = new WrapContentLinearLayoutManager(ExploreDetailActivity.this, LinearLayoutManager.VERTICAL, false);
            rv.setHasFixedSize(false);
            rv.setLayoutManager(wl);
        }else{
            adapter.startListening();
        }
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(new AnswerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Answer answer = documentSnapshot.toObject(Answer.class);
                String id = documentSnapshot.getId();
                User user = answer.getUser();

                Intent intent = new Intent(ExploreDetailActivity.this, MakeEditAnswerActivity.class);
                intent.putExtra("POST", post);
                intent.putExtra("USER-ANSWER", user);
                intent.putExtra("USER", post.getUser());
                intent.putExtra("ANSWER", answer);
                intent.putExtra("ID", id);

                startActivity(intent);
            }

            @Override
            public void setPostAnswered() {
                post.setAnswered(true);
            }

            @Override
            public void onImageClick(DocumentSnapshot documentSnapshot, int position) {
                Answer answer = documentSnapshot.toObject(Answer.class);
                Intent intent = new Intent(ExploreDetailActivity.this, ZoomActivity.class);
                intent.putExtra("IMAGE-REF", answer.getAnswerImageSource());
                startActivity(intent);
            }


        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.addFavourite:
                if(item.getTitle().equals("Remove favourite")){
                    pd.show();
                    removeFavourite(item);
                }else{
                    pd.show();
                    addToFavourite(item);
                }

                return true;
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem favoriteItem = menu.findItem(R.id.addFavourite);
        checkLike(favoriteItem);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.explore_detail_menu, menu);


        return super.onCreateOptionsMenu(menu);
    }

    private void addToFavourite(MenuItem item){
        post.setUser(user);
        Favourite favourite = new Favourite(mAuth.getUid(), post);
        DocumentReference doc = fv.document(mAuth.getUid()+post.getPid());
        DocumentReference achRef = achievementRef.document(mAuth.getCurrentUser().getUid());
        doc.set(favourite)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        achRef.update("likesNumber", FieldValue.increment(1))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        pd.dismiss();
                                        checkLike(item);
                                        Toast.makeText(ExploreDetailActivity.this, "Like berhasil ditambahkan", Toast.LENGTH_SHORT).show();

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void checkLike(MenuItem fav){
        DocumentReference doc = fv.document(mAuth.getUid()+post.getPid());
        doc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            fav.setTitle("Remove favourite");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void removeFavourite(MenuItem fav){
        DocumentReference doc = fv.document(mAuth.getCurrentUser().getUid()+post.getPid());
        DocumentReference achRef = achievementRef.document(mAuth.getCurrentUser().getUid());
        doc.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        achRef.update("likesNumber", FieldValue.increment(-1))
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        pd.dismiss();
                                        fav.setTitle("Add to Favourite");
                                        Toast.makeText(ExploreDetailActivity.this, "Favourite berhasil di hapus", Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("REMOVE_FAVOURITE", e.getLocalizedMessage());
                    }
                });
    }

    @Override
    protected void onStart(){
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop(){
        super.onStop();
        adapter.stopListening();
    }
}