package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePass extends AppCompatActivity {

    private EditText etOldPass, etNewPass;
    private Button updatePass;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ImageView imgProfil;
    private TextView namaProfil;
    private User user;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Change Password");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        etOldPass = findViewById(R.id.etChangePassOld);
        etNewPass = findViewById(R.id.etChangePassNew);

        imgProfil = findViewById(R.id.fotoProfil);
        namaProfil = findViewById(R.id.tvUser);


        updatePass = findViewById(R.id.updatePassword);

        user = getIntent().getParcelableExtra("USER");
        setUpView();

        updatePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.show();
                changePass();
            }
        });

        pd = new ProgressDialog(ChangePass.this);
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

    private void changePass(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), etOldPass.getText().toString());

        if(etOldPass.getText().length()>0 && etNewPass.getText().length()>0){
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.getException() != null){
                                pd.dismiss();
                                Toast.makeText(ChangePass.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }else{
                                user.updatePassword(etNewPass.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    pd.dismiss();
                                                    Toast.makeText(ChangePass.this, "Password Updated, plase login again!", Toast.LENGTH_LONG).show();
                                                    mAuth.signOut();
                                                    Intent intent = new Intent(ChangePass.this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(ChangePass.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
}