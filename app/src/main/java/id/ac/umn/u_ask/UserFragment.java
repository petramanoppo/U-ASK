package id.ac.umn.u_ask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.lang.ref.Reference;
import java.util.UUID;

public class UserFragment extends Fragment {
    AppCompatActivity act;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference docRef;
    private DocumentReference achievementRef;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private ImageView fotoPp, arrow1, arrow2, arrow3;
    private ProgressDialog pd;
    private TextView namaTextview;
    private TextView emailTextview;
    private TextView nickTextview;
    private ListenerRegistration userListener, achievementListener;

    private TextView tvPost, tvLikes, tvVerified;
    private Uri uri;

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

    public UserFragment() {
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
        View v =  inflater.inflate(R.layout.fragment_user, container, false);
        act.getSupportActionBar().setTitle("User Profile");
        act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        docRef = db.collection("Users").document(mAuth.getCurrentUser().getUid());
        achievementRef = db.collection("Achievements").document(mAuth.getCurrentUser().getUid());

        namaTextview = v.findViewById(R.id.tvUser);
        emailTextview = v.findViewById(R.id.tvUserEmail);
        nickTextview = v.findViewById(R.id.tvUserNick);

        tvLikes = v.findViewById(R.id.totalLikes);
        tvPost = v.findViewById(R.id.totalPost);
        tvVerified = v.findViewById(R.id.totalVerified);

        arrow1 = v.findViewById(R.id.arrow1);
        arrow2 = v.findViewById(R.id.arrow2);
        arrow3 = v.findViewById(R.id.arrow3);
        fotoPp = v.findViewById(R.id.fotoProfil);

        auth = FirebaseAuth.getInstance();
        database= FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        pd = new ProgressDialog(act);
        pd.setTitle("Loading");
        pd.setMessage("Please Wait");
        pd.setCancelable(false);



        return v;
    }

    @Override
    public void onStart(){
        super.onStart();
        userListener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(act, "Error while loading", Toast.LENGTH_SHORT).show();
                }else {
                    if (value.exists()) {
                        User user = value.toObject(User.class);
                        namaTextview.setText(user.getName());
                        emailTextview.setText(user.getEmail());
                        nickTextview.setText(user.getNick());

                        if (act != null) {
                            Glide.with(act)
                                    .load(user.getProfileImageSource())
                                    .into(fotoPp);
                        }

                        fotoPp.setOnClickListener((View view) -> {
                            ImagePicker.with(act)
                                    .crop()                    //Crop image(Optional), Check Customization for more option
                                    .compress(1000)            //Final image size will be less than 1 MB(Optional)
                                    .maxResultSize(1080, 1080)    //Final image resolution will be less than 1080 x 1080(Optional)
                                    .createIntent(intent -> {
                                        startForMediaPickerResult.launch(intent);
                                        return null;
                                    });
                        });

                        arrow2.setOnClickListener((View view) -> {
                            Intent intent = new Intent(act, ChangeNick.class);
                            intent.putExtra("USER", user);
                            startActivity(intent);
                        });

                        arrow1.setOnClickListener((View view) -> {
                            Intent intent = new Intent(act, ChangeEmail.class);
                            intent.putExtra("USER", user);
                            startActivity(intent);
                        });

                        arrow3.setOnClickListener((View view) -> {
                            Intent intent = new Intent(act, ChangePass.class);
                            intent.putExtra("USER", user);
                            startActivity(intent);
                        });
                    }
                }
            }
        });

        achievementListener = achievementRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(act, "Error While Loading", Toast.LENGTH_SHORT).show();
                }else {
                    if (value.exists()) {
                        Achievement achievement = value.toObject(Achievement.class);
                        tvLikes.setText(String.valueOf(achievement.getLikesNumber()));
                        tvPost.setText(String.valueOf(achievement.getPostNumber()));
                        tvVerified.setText(String.valueOf(achievement.getVerifiedNumber()));
                    }
                }
            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
        userListener.remove();
        achievementListener.remove();
    }



    private final ActivityResultLauncher<Intent> startForMediaPickerResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();
                if (data != null && result.getResultCode() == Activity.RESULT_OK) {
                    uri = data.getData();
                    uploadPost(uri);
                }
                else {
                        Toast.makeText(requireActivity(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
                    }
                });


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.user_menu, menu);
//        MenuItem log = menu.findItem(R.id.logOut);
//
//        log.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.logOut:
                mAuth.signOut();
                Intent intent = new Intent(act, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateImage(String url){
        CollectionReference answers = db.collection("Answers");
        CollectionReference posts = db.collection("Posts");
        CollectionReference favourites = db.collection("Favourites");
        DocumentReference user = db.collection("Users").document(mAuth.getCurrentUser().getUid());

        WriteBatch batch = db.batch();

        batch.update(user, "profileImageSource", url);
        posts.whereEqualTo("user.uid", mAuth.getCurrentUser().getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                            batch.update(ds.getReference(), "user.profileImageSource", url);
                        }
                        answers.whereEqualTo("user.uid", mAuth.getCurrentUser().getUid()).get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                                            batch.update(ds.getReference(), "user.profileImageSource", url);
                                        }
                                        favourites.whereEqualTo("post.user.uid", mAuth.getCurrentUser().getUid()).get()
                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                        for(QueryDocumentSnapshot ds : queryDocumentSnapshots){
                                                            batch.update(ds.getReference(), "post.user.profileImageSource", url);
                                                        }
                                                        batch.commit();
                                                        pd.dismiss();
                                                    }
                                                });

                                    }
                                });
                    }
                });
    }

    private void uploadPost(Uri uri){
        final String randomKey = UUID.randomUUID().toString();
        StorageReference sRef = storage.getReference().child("imagesProfile/" + randomKey);
        pd.show();
        sRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri getUri = uri;
                                String url = getUri.toString();
                                updateImage(url);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(act, "Upload Gagal", Toast.LENGTH_SHORT).show();
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
    }


}