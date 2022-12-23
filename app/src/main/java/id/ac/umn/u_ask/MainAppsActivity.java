package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Bundle;

import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainAppsActivity extends AppCompatActivity {
    User user;

    final Fragment hm = new HomeFragment();
    final Fragment qm = new QuestionFragment();
    final Fragment dm = new DiscussionFragment();
    final Fragment um = new UserFragment();
    Fragment active = hm;

    FragmentManager fm = getSupportFragmentManager();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_apps);

//        fm.beginTransaction().add(R.id.frag_container, hm).commit();
//        fm.beginTransaction().add(R.id.frag_container, qm).hide(qm).commit();
//        fm.beginTransaction().add(R.id.frag_container, dm).hide(dm).commit();
//        fm.beginTransaction().add(R.id.frag_container, um).hide(um).commit();


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_container,
                    new HomeFragment()).commit();
        }
        //getData();
        setFragment();
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));


    }

    private void setFragment(){
        BottomNavigationView bn = findViewById(R.id.bottom_nav);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frag_container, new HomeFragment())
                .commit();

        bn.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch(item.getItemId()){
                case R.id.ic_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.ic_question:
                    selectedFragment = new QuestionFragment();
                    break;
                case R.id.ic_discussion:
                    selectedFragment = new DiscussionFragment();
                    break;
                case R.id.ic_user:
                    selectedFragment = new UserFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frag_container, selectedFragment)
                    .commit();

            return true;
        });
    }

    private void getData(){
        DocumentReference ref = db.collection("Users").document(mAuth.getCurrentUser().getUid());
        ref.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            user = documentSnapshot.toObject(User.class);
                            Toast.makeText(MainAppsActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainAppsActivity.this, "load fail", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }




}