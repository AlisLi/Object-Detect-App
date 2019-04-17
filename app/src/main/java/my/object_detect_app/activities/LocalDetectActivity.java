package my.object_detect_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import my.object_detect_app.R;

/**
 * User: Lizhiguo
 */
public class LocalDetectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_detect);
    }

    public void choicePhoto(View view){
        Intent intent = new Intent();
        intent.setClass(this, ImageSelectorActivity.class);
        startActivity(intent);
    }


}
