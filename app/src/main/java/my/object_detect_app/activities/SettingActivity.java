package my.object_detect_app.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import my.object_detect_app.R;
import my.object_detect_app.entity.Algorithm;
import okhttp3.Call;
import okhttp3.Response;

import static my.object_detect_app.Config.NET_URL;
import static my.object_detect_app.Constant.ALGORITHM;
import static my.object_detect_app.Constant.NET;
import static my.object_detect_app.utils.CommonUtils.removeDuplicate;

/**
 * User: Lizhiguo
 */
public class SettingActivity extends AppCompatActivity {
    public static final String TAG = "SettingActivity";

    private TextView txtAlgorithm;
    private TextView txtNet;

    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;

    private List<String> algorithms = new ArrayList<>();
    private List<String> nets = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initView();

    }

    private void initView() {
        txtAlgorithm = findViewById(R.id.txt_algorithm);
        txtNet = findViewById(R.id.txt_net);

        txtAlgorithm.setText(ALGORITHM);
        txtNet.setText(NET);
    }

    private void singleChoise(String title, List<String> content) {
        mBuilder = new MaterialDialog.Builder(SettingActivity.this);
        mBuilder.title(title);
        mBuilder.titleGravity(GravityEnum.CENTER);
        mBuilder.titleColor(Color.parseColor("#000000"));
        mBuilder.items(content);
        mBuilder.autoDismiss(false);
        mBuilder.widgetColor(Color.RED);
        mBuilder.positiveText("确定");
        mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

            }
        });

        mBuilder.itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
            @Override
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(SettingActivity.this, "请选择" + title, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SettingActivity.this, text, Toast.LENGTH_LONG).show();
                    // 更新View显示
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(title.equals("算法")){
                                ALGORITHM  = (String) text;
                                txtAlgorithm.setText(ALGORITHM);
                            }else if(title.equals("主干网络")){
                                NET  = (String) text;
                                txtNet.setText(NET);
                            }
                        }
                    });
                    dialog.dismiss();
                }
                return false;
            }
        });
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    public void choiceNet(View view){
        // 选择网络
        OkHttpUtils.post()//
                .addParams("name", ALGORITHM)
                .url(NET_URL + "algorithm/selectByName")
                .build()//
                .connTimeOut(200000000)
                .readTimeOut(200000000)
                .writeTimeOut(200000000)
                .execute(new AlgorithmCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(List<Algorithm> response, int id) {
                        Log.d(TAG, "algorithm size  = " + response.size());
                        for (Algorithm algorithm : response){
                            nets.add(algorithm.getNet());
                        }
                        // 去除net重复数据
                        nets = removeDuplicate(nets);
                        singleChoise("主干网络", nets);

                    }
                });
    }

    public void choiceAlgorithm(View view){
        // 选择算法
        OkHttpUtils.post()//
                .url(NET_URL + "algorithm/selectAll")
                .build()//
                .connTimeOut(200000000)
                .readTimeOut(200000000)
                .writeTimeOut(200000000)
                .execute(new AlgorithmCallback() {

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Log.d(TAG, "onError");
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(List<Algorithm> response, int id) {
                        Log.d(TAG, "algorithm size  = " + response.size());
                        for (Algorithm algorithm : response){
                            algorithms.add(algorithm.getAlgorithmName());
                        }
                        //去除algorithms重复数据
                        algorithms = removeDuplicate(algorithms);
                        singleChoise("算法", algorithms);
                    }
                });

    }


   abstract class AlgorithmCallback extends Callback<List<Algorithm>> {
        @Override
        public List<Algorithm> parseNetworkResponse(Response response, int id) throws IOException
        {
            String string = response.body().string();
            Log.d(TAG, "response.body().string() = " + string);
            Algorithm[] array = new Gson().fromJson(string,Algorithm[].class);
            List<Algorithm> algorithmList = Arrays.asList(array);
            Log.d(TAG, "algorithmList.get(0) = " + algorithmList.get(0).toString());
            return algorithmList;
        }
    }

}
