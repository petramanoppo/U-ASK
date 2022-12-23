package id.ac.umn.u_ask;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.ortiz.touchview.TouchImageView;

public class ZoomActivity extends AppCompatActivity {

    private TouchImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        image = findViewById(R.id.zoomImage);
        getSupportActionBar().hide();

        String ref = getIntent().getStringExtra("IMAGE-REF");
        if(ref!=null){
            Glide.with(this)
                    .load(ref)
                    .into(image);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}