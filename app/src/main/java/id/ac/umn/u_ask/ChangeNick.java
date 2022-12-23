package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Locale;

public class ChangeNick extends AppCompatActivity {
    private EditText changeNick;
    private Button updateNick;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressDialog pd;
    private ImageView imgProfil;
    private TextView namaProfil;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_nick);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Change Nickname");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        imgProfil = findViewById(R.id.fotoProfil);
        namaProfil = findViewById(R.id.tvUser);

        changeNick = findViewById(R.id.etChangeNick);
        updateNick = findViewById(R.id.editNick);
        user = getIntent().getParcelableExtra("USER");
        setUpView();


        updateNick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.show();
                checkNick();
            }
        });



        pd = new ProgressDialog(ChangeNick.this);
        pd.setTitle("Loading");
        pd.setMessage("Please Wait");
        pd.setCancelable(false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpView(){
        Glide.with(this)
                .load(user.getProfileImageSource())
                .into(imgProfil);

        namaProfil.setText(user.getName());

    }

    private void checkNick(){
        if(changeNick.getText().length()>0) {
            DocumentReference nick = db.collection("Nicknames").document(changeNick.getText().toString().trim());
            nick.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        pd.dismiss();
                        Toast.makeText(ChangeNick.this, "This nick has been used by other user", Toast.LENGTH_SHORT).show();
                    }else{
                        updateNickDatabase();
                    }
                }
            });
        }else{
            pd.dismiss();
            Toast.makeText(this, "Insert field correctly", Toast.LENGTH_SHORT).show();
        }


    }

    private void updateNickDatabase(){
        String newNick = changeNick.getText().toString().trim();
        CollectionReference answers = db.collection("Answers");
        CollectionReference posts = db.collection("Posts");
        CollectionReference favourites = db.collection("Favourites");
        DocumentReference user = db.collection("Users").document(mAuth.getCurrentUser().getUid());

        WriteBatch batch = db.batch();

        batch.update(user, "nick", newNick);
        posts.whereEqualTo("user.uid", mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                            batch.update(ds.getReference(), "user.nick", newNick);
                        }
                        answers.whereEqualTo("user.uid", mAuth.getCurrentUser().getUid()).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                                            batch.update(ds.getReference(), "user.nick", newNick);
                                        }
                                        favourites.whereEqualTo("post.user.uid", mAuth.getCurrentUser().getUid()).get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                                                            batch.update(ds.getReference(), "post.user.nick", newNick);
                                                        }
                                                        batch.commit();
                                                        Toast.makeText(ChangeNick.this, "Nick updated", Toast.LENGTH_SHORT).show();
                                                        pd.dismiss();
                                                    }
                                                });

                                    }
                                });
                    }
                });

    }
}