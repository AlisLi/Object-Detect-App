package my.object_detect_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import my.object_detect_app.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toRealTimeVideo(View view){
        Intent intent = new Intent();
        intent.setClass(this, ClassifierActivity.class);
        startActivity(intent);
    }

    public void toDetectPhoto(View view){
        Intent intent = new Intent();
        intent.setClass(this, ImageDetectActivity.class);
        startActivity(intent);
    }

    public void toDetectVideo(View view){
        Intent intent = new Intent();
        intent.setClass(this, VideoDetectActivity.class);
        startActivity(intent);
    }

    public void toNetPhoto(View view){

    }

    public void toNetVideo(View view){

    }

    public void toDetectResult(View view){

    }

}
