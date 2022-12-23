package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class MakeAnswerActivity extends AppCompatActivity {

    private TextView tvHeader, tvBody, tvNama;
    private EditText etBody;
    private ImageView imgAnswer, imgPost, imgProfile;
    private Button btnUploadImage;
    private Post post;
    private User user;
    private ProgressDialog pd;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();



    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_answer);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Answer");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        tvNama = findViewById(R.id.namaUserAnswer);
        tvHeader = findViewById(R.id.detailTitleAnswer);
        tvBody = findViewById(R.id.detailBodyAnswer);

        etBody = findViewById(R.id.etBodyAnswer);
        imgAnswer = findViewById(R.id.postImageAnswer);
        imgPost = findViewById(R.id.detailImgAnswer);
        imgProfile = findViewById(R.id.userImgProfilAnswer);

        post = getIntent().getParcelableExtra("POST");
        user = getIntent().getParcelableExtra("USER");

        post.setUser(user);

        Glide.with(this)
                .load(user.getProfileImageSource())
                .into(imgProfile);

        setDetailQuestion();

        btnUploadImage = findViewById(R.id.btnUploadAnswer);

        pd = new ProgressDialog(MakeAnswerActivity.this);
        pd.setTitle("Loading");
        pd.setMessage("Please Wait");
        pd.setCancelable(false);

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(MakeAnswerActivity.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1500)			//Final image size will be less than 1 MB(Optional)
                        .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start(101);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == Activity.RESULT_OK){
            uri = data.getData();
            imgAnswer.setVisibility(View.VISIBLE);
            imgAnswer.setImageURI(uri);
        }else{
            Toast.makeText(this, "No image chosen", Toast.LENGTH_SHORT).show();
        }
    }

    private void setDetailQuestion(){
        tvNama.setText(user.getNick());
        tvHeader.setText(post.getHeader());
        tvBody.setText(post.getBody());

        if(post.getPostImageSource().equals("")){
            imgPost.setVisibility(View.GONE);
        }else{
            Glide.with(this)
                    .load(post.getPostImageSource())
                    .into(imgPost);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.post_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.makePost:
                pd.show();
                if(uri == null){
                    savePost("");
                }else{
                    uploadPost(uri);
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePost(String url){

        if(etBody.getText().toString().trim().length()>0){
            db.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentReference userRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());
                    DocumentReference answerRef = db.collection("Answers").document();

                    DocumentSnapshot snap = transaction.get(userRef);
                    transaction.set(answerRef, new Answer(post.getPid(), false, etBody.getText().toString(), url, user.getUid(), snap.toObject(User.class)));
                    if(post.isAnswered()){
                        transaction.update(answerRef, "answered", true);
                    }
                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(MakeAnswerActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    onBackPressed();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("ERROR_ANSWER", e.getLocalizedMessage());
                    pd.dismiss();

                }
            });


        }else{
            pd.dismiss();
            Toast.makeText(MakeAnswerActivity.this, "Fill all field correctly", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadPost(Uri uri){
        if(etBody.getText().length()>0){
            final String randomKey = UUID.randomUUID().toString();
            StorageReference sRef = storage.getReference().child("images/" + randomKey);

            sRef.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Uri getUri = uri;
                                    String url = getUri.toString();
                                    savePost(url);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MakeAnswerActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                                    pd.dismiss();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("ERROR-UPLOAD", e.getLocalizedMessage());
                            pd.dismiss();
                        }
                    });

        }else{
            pd.dismiss();
            Toast.makeText(this, "Fill all field correctly", Toast.LENGTH_SHORT).show();
        }

    }
}