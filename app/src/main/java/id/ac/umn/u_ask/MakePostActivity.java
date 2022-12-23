package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.Toast;


import com.github.dhaval2404.imagepicker.ImagePicker;
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
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class MakePostActivity extends AppCompatActivity {

    private EditText etBody, etHeader;
    private ImageView imgPost;
    private Button btnImage;
    private Uri uri;
    private ProgressDialog pd;
    private final String DISC = "Discussion";

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private String jenisPost;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_post);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Post");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        etBody = findViewById(R.id.etBody);
        etHeader = findViewById(R.id.etHeader);

        imgPost = findViewById(R.id.postImage);
        btnImage = findViewById(R.id.btnUpload);

        jenisPost = getIntent().getStringExtra("JENIS");

        pd = new ProgressDialog(MakePostActivity.this);
        pd.setTitle("Loading");
        pd.setMessage("Please Wait");
        pd.setCancelable(false);



        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(MakePostActivity.this)
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
            imgPost.setVisibility(View.VISIBLE);
            imgPost.setImageURI(uri);
        }else{
            Toast.makeText(this, "No image chosen", Toast.LENGTH_SHORT).show();
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
        if(etHeader.getText().toString().trim().length() > 0 && etBody.getText().toString().trim().length()>0) {
            db.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    Boolean question = true;
                    if(jenisPost.equals(DISC)) {
                        question = false;
                    }
                    DocumentReference userRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());
                    DocumentReference postRef = db.collection("Posts").document();
                    DocumentReference achievementRef = db.collection("Achievements").document(mAuth.getCurrentUser().getUid());
                    DocumentSnapshot snap = transaction.get(userRef);
                    transaction.set(postRef, new Post(etHeader.getText().toString(), etBody.getText().toString(), jenisPost, url ,snap.toObject(User.class), question));
                    transaction.update(achievementRef, "postNumber", FieldValue.increment(1));
                    transaction.update(postRef, "pid", postRef.getId());

                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(MakePostActivity.this, "Post berhasil dikirim", Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                    onBackPressed();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }else{
            pd.dismiss();
            Toast.makeText(this, "Fill all field correctly", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPost(Uri uri){
        if(etHeader.getText().toString().trim().length() > 0 && etBody.getText().toString().trim().length()>0) {
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
                                    Toast.makeText(MakePostActivity.this, "Upload Gagal", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Fill all field correctly", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }

    }


}