package id.ac.umn.u_ask;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class DiscussionFragment extends Fragment {
    private AppCompatActivity act;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference postRef = db.collection("Posts");
    private ExploreAdapter adapter;
    private WrapContentLinearLayoutManager wl;
    private RecyclerView rv;
    private SearchView searchView;
    private View vGlobal;
    private final String DISC = "Discussion";

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
        adapter.stopListening();
    }

    @Override
    public void onStart(){
        super.onStart();
        adapter.startListening();
    }


    public DiscussionFragment() {
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
        View v = inflater.inflate(R.layout.fragment_discussion, container, false);
        act.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        act.getSupportActionBar().setTitle("Discussion");
        act.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3F51B5")));
        vGlobal = v;
        setUpRecycler();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.discussion_menu, menu);

        MenuItem item = menu.findItem(R.id.searchDisc);
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.ic_addDisc:
                Intent intent = new Intent(act, MakePostActivity.class);
                intent.putExtra("JENIS", DISC);
                startActivity(intent);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    private void setUpRecycler(){
        Query query = postRef.whereEqualTo("question", false).orderBy("createdAt", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder<Post>()
                .setQuery(query, Post.class)
                .build();
        rv = vGlobal.findViewById(R.id.recyclerDiscussion);

        adapter = new ExploreAdapter(options);

        if(wl == null) {
            wl = new WrapContentLinearLayoutManager(act, LinearLayoutManager.VERTICAL, false);
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
                Intent intent = new Intent(act, ExploreDetailActivity.class);

                intent.putExtra("POST", post);
                intent.putExtra("USER", user);

                startActivity(intent);

            }
        });
    }

    private void searchList(String s){
        if(s.equals(null) || s.equals("")){
            setUpRecycler();
            return;
        }
        Query query = postRef.whereEqualTo("question", false).orderBy("header").startAt(s).endAt(s+"\uf8ff");


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
                Intent intent = new Intent(act, ExploreDetailActivity.class);

                intent.putExtra("POST", post);
                intent.putExtra("USER", user);
                startActivity(intent);
            }
        });
    }



}