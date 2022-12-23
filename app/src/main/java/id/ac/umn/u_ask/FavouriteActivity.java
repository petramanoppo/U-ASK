package id.ac.umn.u_ask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class FavouriteActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference favRef = db.collection("Favourites");
    private FavouriteAdapter adapter;
    private WrapContentLinearLayoutManager wl;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private RecyclerView rv;
    private String UID;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        UID = getIntent().getStringExtra("UID");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Favourites");
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));

        setUpRecycler();
    }

    private void setUpRecycler(){
        Query query = favRef.whereEqualTo("uid_fav", UID).orderBy("addedAt", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder<Favourite>()
                .setQuery(query, Favourite.class)
                .build();
        rv = findViewById(R.id.recyclerMyFavourite);

        adapter = new FavouriteAdapter(options);
        adapter.startListening();

        if(wl == null) {
            wl = new WrapContentLinearLayoutManager(FavouriteActivity.this, LinearLayoutManager.VERTICAL, false);
            rv.setHasFixedSize(true);
            rv.setLayoutManager(wl);
        }else{
            adapter.startListening();
        }
        rv.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction == ItemTouchHelper.RIGHT){
                    builder = new AlertDialog.Builder(FavouriteActivity.this);
                    builder.setTitle("Confirmation")
                            .setMessage("Are you sure to delete this post from your favourite?")
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
                }
            }
        }).attachToRecyclerView(rv);

        adapter.setOnItemClickListener(new FavouriteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Favourite favourite= documentSnapshot.toObject(Favourite.class);
                Post post = favourite.getPost();
                User user = post.getUser();
                Intent intent = new Intent(FavouriteActivity.this, ExploreDetailActivity.class);

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

}