package id.ac.umn.u_ask;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

public class FavouriteAdapter extends FirestoreRecyclerAdapter<Favourite, FavouriteAdapter.FavouriteHolder> {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference fv= db.collection("Favourites");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private FavouriteAdapter.OnItemClickListener listener;


    public FavouriteAdapter(@NonNull FirestoreRecyclerOptions<Favourite> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FavouriteHolder holder, int position, @NonNull Favourite model) {
        holder.tvNick.setText(model.getPost().getUser().getNick());
        holder.tvTitle.setText(model.getPost().getHeader());
        holder.tvJenis.setText(model.getPost().getJenis());
        if(model.getPost().getJenis().equals("Programming")){
            holder.imgExplore.setImageResource(R.drawable.ic_code_50);
        }else if(model.getPost().getJenis().equals("Engineer")){
            holder.imgExplore.setImageResource(R.drawable.ic_baseline_engineering);
        }else if(model.getPost().getJenis().equals("Business")){
            holder.imgExplore.setImageResource(R.drawable.ic_baseline_business_24);
        }else if(model.getPost().getJenis().equals("Common")){
            holder.imgExplore.setImageResource(R.drawable.ic_baseline_school);
        }else{
            holder.imgExplore.setImageResource(R.drawable.ic_baseline_discussion_blue);
        }
    }

    @NonNull
    @Override
    public FavouriteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.explore_item, parent, false);
        return new FavouriteHolder(v);
    }

    class FavouriteHolder extends RecyclerView.ViewHolder{
        TextView tvNick, tvTitle, tvJenis;
        ImageView imgExplore;


        public FavouriteHolder(@NonNull View itemView) {
            super(itemView);
            tvNick = itemView.findViewById(R.id.itemNick);
            tvTitle = itemView.findViewById(R.id.itemTitle);
            tvJenis = itemView.findViewById(R.id.itemTipe);
            imgExplore = itemView.findViewById(R.id.exploreImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if(position!=RecyclerView.NO_POSITION && listener!=null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public void deleteItem(int position){
        getSnapshots().getSnapshot(position).getReference().delete();
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(FavouriteAdapter.OnItemClickListener listener){
        this.listener = listener;
    }
}
