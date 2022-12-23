package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class ChangeEmail extends AppCompatActivity {

    private EditText etPassword, etEmail;
    private Button updateEmail;
    private String UID;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ImageView imgProfil;
    private TextView namaProfil;
    private User user;

    ProgressDialog pd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Change Email");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        UID = mAuth.getCurrentUser().getUid();

        etPassword = findViewById(R.id.etEmailPassword);
        etEmail = findViewById(R.id.etChangeEmail);

        imgProfil = findViewById(R.id.fotoProfil);
        namaProfil = findViewById(R.id.tvUser);

        updateEmail = findViewById(R.id.updateEmail);

        user = getIntent().getParcelableExtra("USER");
        setUpView();

        updateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.show();
                updateNewEmail();
            }
        });

        pd = new ProgressDialog(ChangeEmail.this);
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

    private void updateNewEmail(){
        if(etPassword.getText().length()>0 && etEmail.getText().length()>0){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), etPassword.getText().toString());

            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.getException() != null){
                                pd.dismiss();
                                Toast.makeText(ChangeEmail.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }else{
                                user.updateEmail(etEmail.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    updateDatabase(etEmail.getText().toString());
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("ERROR", e.getLocalizedMessage());
                                                pd.dismiss();
                                                Toast.makeText(ChangeEmail.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    });

        }else{
            pd.dismiss();
            Toast.makeText(this, "Isi field dengan lengkap", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDatabase(String email){
        DocumentReference user = db.collection("Users").document(UID);

        user.update("email", email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(ChangeEmail.this, "Email Updated, plase login again!", Toast.LENGTH_LONG).show();
                mAuth.signOut();
                Intent intent = new Intent(ChangeEmail.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }
}