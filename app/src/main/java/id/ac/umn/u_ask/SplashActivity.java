package id.ac.umn.u_ask;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.protobuf.Value;

public class SplashActivity extends AppCompatActivity {
    ImageView ivTop, ivLogo, ivBeat, ivBottom;
    TextView tv;
    CharSequence charSequence;
    int index;
    long delay = 200;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        ivTop = findViewById(R.id.ivTop);
        ivLogo = findViewById(R.id.ivLogo);
        ivBeat = findViewById(R.id.ivBeat);
        ivBottom = findViewById(R.id.ivBottom);
        tv = findViewById(R.id.tvSplash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Animation animation1 = AnimationUtils.loadAnimation(this,R.anim.top_wave);
        ivTop.setAnimation(animation1);
        ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(ivLogo, PropertyValuesHolder.ofFloat("scaleX",1.2f),PropertyValuesHolder.ofFloat("scaleY", 1.2f));
        objectAnimator.setDuration(500);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.start();
        animatText("Welcome to U-Ask");
        Animation animation2 = AnimationUtils.loadAnimation(this,R.anim.bottom_wave);
        ivBottom.setAnimation(animation2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                onDestroy();
            }
        },4000);

    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            tv.setText(charSequence.subSequence(0,index++));
            if(index <= charSequence.length()){
                handler.postDelayed(runnable,delay);
            }
        }
    };
    public void animatText(CharSequence cs){
        charSequence = cs;
        index = 0;
        tv.setText("");
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable,delay);
    }
}