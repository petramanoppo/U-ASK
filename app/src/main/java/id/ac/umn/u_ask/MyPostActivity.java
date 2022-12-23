package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.checkerframework.checker.units.qual.A;

public class MyPostActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postRef = db.collection("Posts");
    private ExploreAdapter adapter;
    private WrapContentLinearLayoutManager wl;
    private RecyclerView rv;
    private String UID;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_post);

        UID = getIntent().getStringExtra("UID");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Post");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
        setUpRecycler();
    }

    private void setUpRecycler(){
        Query query = postRef.whereEqualTo("user.uid", UID).orderBy("createdAt", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();
        rv = findViewById(R.id.recyclerMyPost);

        adapter = new ExploreAdapter(options);
        adapter.startListening();

        if(wl == null) {
            wl = new WrapContentLinearLayoutManager(MyPostActivity.this, LinearLayoutManager.VERTICAL, false);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(wl);
        }else{
            adapter.startListening();
        }
        rv.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction == ItemTouchHelper.RIGHT){
                    builder = new AlertDialog.Builder(MyPostActivity.this);
                    builder.setTitle("Confirmation")
                            .setMessage("Are you sure to delete this post?")
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    adapter.deleteItem(viewHolder.getLayoutPosition());
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();

                                }
                            })
                            .show();
                    adapter.notifyDataSetChanged();

                }else if(direction == ItemTouchHelper.LEFT){
                    Post post = adapter.editItem(viewHolder.getLayoutPosition());

                    User user = post.getUser();
                    Intent intent = new Intent(MyPostActivity.this, MakeEditActivity.class);

                    intent.putExtra("POST", post);
                    intent.putExtra("USER", user);

                    startActivity(intent);
                }
            }
        }).attachToRecyclerView(rv);

        adapter.setOnItemClickListener(new ExploreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Post post = documentSnapshot.toObject(Post.class);
                User user = post.getUser();
                Intent intent = new Intent(MyPostActivity.this, ExploreDetailActivity.class);
                intent.putExtra("POST", post);
                intent.putExtra("USER", user);
                startActivity(intent);
            }
        });
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu){
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.explore_menu, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }

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