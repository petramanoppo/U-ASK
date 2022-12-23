package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private Button register;
    private EditText name, email, password, nickname;
    private ProgressDialog pd;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference nickName = db.collection("Nicknames");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name = findViewById(R.id.etNamer);
        email = findViewById(R.id.etEmailr);
        password = findViewById(R.id.etPasswordr);
        nickname = findViewById(R.id.etNickr);
        getSupportActionBar().hide();

        register = findViewById(R.id.btnRegister);
        pd = new ProgressDialog(RegisterActivity.this);
        pd.setTitle("Loading");
        pd.setMessage("Please Wait");
        pd.setCancelable(false);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(name.getText().toString().trim().length() > 0 && email.getText().toString().trim().length()>0 && password.getText().toString().trim().length()>0
                        && nickname.getText().toString().trim().length()>0){
                    pd.show();
                    checkNickname(nickname.getText().toString().trim());
                }else{
                    Toast.makeText(RegisterActivity.this, "Isi field lengkap", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkNickname(String nickname){
        DocumentReference nick = nickName.document(nickname);
        nick.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    pd.dismiss();
                    Toast.makeText(RegisterActivity.this, "Nick sudah terpakai", Toast.LENGTH_SHORT).show();
                }else{
                    registerUser(email.getText().toString().trim(), password.getText().toString().trim(), name.getText().toString().trim()
                            , nickname.trim());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("CHECK-GAGAL", e.getLocalizedMessage());
            }
        });


    }

    private void registerUser(String email, String password,String name, String nickname) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && task.getResult()!=null) {
                            FirebaseUser user = task.getResult().getUser();
                            if(user!= null){
                                registerDatabase(email, name, nickname, user.getUid());
                                Log.d("REG-SUKSES", "createUserWithEmail:success");
                            }else{
                                Toast.makeText(RegisterActivity.this, "Register Gagal", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }
                        } else {
                            Log.w("REG-GAGAL", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            pd.dismiss();

                        }
                    }
                });
    }

    private void registerDatabase(String email, String name, String nickname, String uid){
        Uri uri = Uri.parse("android.resource://id.ac.umn.u_ask/drawable/defaultpicture");
        User user = new User(name, email, uri.toString(), nickname, uid, "Beginner");
        Achievement achievement = new Achievement(0 ,0, 0);
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference nickRef = nickName.document(nickname);
                DocumentReference userRef = db.collection("Users").document(uid);
                DocumentReference achievementRef = db.collection("Achievements").document(uid);
                transaction.set(userRef, user);
                transaction.set(nickRef, new HashMap<String, Object>());
                transaction.set(achievementRef, achievement);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(RegisterActivity.this, "Berhasil", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainAppsActivity.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("REG-GAGAL", e.getLocalizedMessage());
            }
        });
    }





}