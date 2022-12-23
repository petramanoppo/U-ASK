package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


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

public class ExploreActivity extends AppCompatActivity{

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postRef = db.collection("Posts");
    private String jenisPost;
    private ExploreAdapter adapter;
    private WrapContentLinearLayoutManager wl;
    private RecyclerView rv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        jenisPost = getIntent().getStringExtra("JENIS");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Explore " + jenisPost);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        setUpRecycler();

    }

    private void setUpRecycler(){
        Query query = postRef.whereEqualTo("jenis", jenisPost).orderBy("createdAt", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();
        rv = findViewById(R.id.recyclerExplore);

        adapter = new ExploreAdapter(options);

        if(wl == null) {
            wl = new WrapContentLinearLayoutManager(ExploreActivity.this, LinearLayoutManager.VERTICAL, false);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(wl);
        }else{
            adapter.startListening();
        }
        rv.setAdapter(adapter);

        adapter.setOnItemClickListener(new ExploreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Post post = documentSnapshot.toObject(Post.class);
                User user = post.getUser();
                Intent intent = new Intent(ExploreActivity.this, ExploreDetailActivity.class);

                intent.putExtra("POST", post);
                intent.putExtra("USER", user);

                startActivity(intent);

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.ic_addPost:
                Intent intent = new Intent(ExploreActivity.this, MakePostActivity.class);
                intent.putExtra("JENIS", jenisPost);
                startActivity(intent);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.explore_menu, menu);

        MenuItem item = menu.findItem(R.id.exploreSeacrh);
        SearchView searchView = (SearchView) item.getActionView();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchList(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchList(s);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private void searchList(String s){
        if(s.equals(null) || s.equals("")){
            setUpRecycler();
            return;
        }
        Query query = postRef.whereEqualTo("jenis", jenisPost).orderBy("header").startAt(s).endAt(s+"\uf8ff");
        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();
        adapter = new ExploreAdapter(options);
        rv.setAdapter(adapter);

        adapter.startListening();

        adapter.setOnItemClickListener(new ExploreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Post post = documentSnapshot.toObject(Post.class);
                User user = post.getUser();
                Intent intent = new Intent(ExploreActivity.this, ExploreDetailActivity.class);

                intent.putExtra("POST", post);
                intent.putExtra("USER", user);
                startActivity(intent);
            }
        });
    }

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