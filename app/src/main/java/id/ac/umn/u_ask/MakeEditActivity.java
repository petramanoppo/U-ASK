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

import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import io.grpc.Context;

public class MakeEditActivity extends AppCompatActivity {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private EditText etBody, etHeader;
    private ImageView imgPost, imgProfile;
    private Button btnImage;
    private ProgressDialog pd;
    private Uri uri;



    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Post post;
    User user;
    private CollectionReference postRef =db.collection("Posts");
    private CollectionReference favRef =db.collection("Favourites");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_edit);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Post");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        etBody = findViewById(R.id.editEtBody);
        etHeader = findViewById(R.id.editEtHeader);



        imgPost = findViewById(R.id.editPostImage);
        btnImage = findViewById(R.id.editBtnUpload);
        imgProfile = findViewById(R.id.userImgProfilAnswer);

        post = getIntent().getParcelableExtra("POST");
        user = getIntent().getParcelableExtra("USER");

        post.setUser(user);



        if(!post.getPostImageSource().equals("")){
            imgPost.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(post.getPostImageSource())
                    .into(imgPost);
        }

        etHeader.setText(post.getHeader());
        etBody.setText(post.getBody());

        pd = new ProgressDialog(MakeEditActivity.this);
        pd.setTitle("Loading");
        pd.setMessage("Please Wait");
        pd.setCancelable(false);

        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(MakeEditActivity.this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(512)			//Final image size will be less than 1 MB(Optional)
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
                    updatePost("");
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

    private void updatePost(String url){
        if(etHeader.getText().length()>0 && etBody.getText().length()>0){
            post.setHeader(etHeader.getText().toString());
            post.setBody(etBody.getText().toString());
            if(uri!=null){
                post.setPostImageSource(url);
            }

            WriteBatch batch = db.batch();

            batch.set(postRef.document(post.getPid()), post);

            favRef.whereEqualTo("post.pid", post.getPid()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                                batch.update(ds.getReference(), "post", post);
                            }
                            batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(MakeEditActivity.this, "Update Success", Toast.LENGTH_SHORT).show();
                                    onBackPressed();
                                    pd.dismiss();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(MakeEditActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
        }else{
            Toast.makeText(this, "Fill all field correctly", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPost(Uri uri){
        Log.d("URL-URI", post.getPostImageSource());
        if(!post.getPostImageSource().equals("")){
            StorageReference ref = storage.getReferenceFromUrl(post.getPostImageSource());
            ref.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }

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
                                    updatePost(url);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MakeEditActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
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