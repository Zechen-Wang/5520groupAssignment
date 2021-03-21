package edu.neu.groupassignment.stickittoem.fcm;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import edu.neu.groupassignment.stickittoem.R;
import edu.neu.groupassignment.stickittoem.model.User;

public class FCMActivity extends AppCompatActivity {

    public static final String SERVER_KEY = "key=AAAALzse8gE:APA91bHsJPnCQ_E-eCFmdIOrLAZz0B11jxgVoUkt62Kfb-ZvubuxGTTAx0rWpavmr1ZRg6N6-zlx0Eg0P1t0tzwDsWdq7WahoXceZJsNhbmDiNjMldtd7So4Cj0l874UddKYmy31u0BY";
    public static final String TAG = "fcm";

    private DatabaseReference userRef;
    private ArrayList<String> users;
    private String username;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_c_m);

        username = getIntent().getStringExtra("username");
        setTitle(username);

        // database reference: users
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        setToken(); // 在createUser之前设定好token
        createUser();

        //实时sync database 数据？？
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users = new ArrayList<>();
                for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                    //key应该是username名字
                    String user = userSnapshot.getKey();
                    users.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /*
    * 新建一个user存在database里（有可能覆盖，username一样的话）
    * TODO： 数据库有username的话，就把这个user数据取出来（名字，token，发送sticker个数）
    * */
    private void createUser() {
        String userId = userRef.push().getKey(); //生成id
        User user = new User(userId, username, token);

        //userRef.setValue(currentUsername);
        userRef.child(username).setValue(user).addOnCompleteListener(task ->{
            if  (task.isSuccessful()) {
                Toast.makeText(this, "Add new user success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(FCMActivity.this, "add new user fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //取得token 并且设置到全局变量token中
    private void setToken() {
        //newToken等于empty，如果token没有更新的话
        String newToken = MyFirebaseMessageService.getToken(getApplicationContext());
        if (!newToken.equals("empty") && !newToken.equals(token)) {
            token = newToken;
            // change token in the database!
            Log.d(TAG, "New Token generated :" + token);
        }
        Log.d(TAG, "No Change to the Token !!!!!!!!!!!!!!!!!");
        Log.d(TAG, "Current Token is :" + token);
    }

    /*
    * 接受 news 下的提醒
    * */
    public void subscribeNews(View view) {
        FirebaseMessaging.getInstance().subscribeToTopic("news")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "subscribe success";
                        if (!task.isSuccessful()) {
                            msg = "subscribe fail";
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(FCMActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //bind到btn onclick上
    public void showToken(View view) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("main", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token1 = task.getResult();

                        // Log and toast
                        Log.d("main", "Instance Token: " + token1);
                        Toast.makeText(FCMActivity.this, "Instance Token: " + token1, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //bind到onclick上了
    public void sendMessageToNews(View type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendMessageToNews();
            }
        }).start();
    }

    //上面sendMessageToNews的helper function
    private void sendMessageToNews() {
        JSONObject jPayload = new JSONObject();
        JSONObject jNotification = new JSONObject();

        try{
            // 创建一个notification object
            jNotification.put("message", "This is a Firebase Cloud Messaging topic \"news\" message!");
            jNotification.put("body", "Hello there!!!");
            jNotification.put("sound", "default");
            jNotification.put("badge", "1");
            jNotification.put("click_action", "OPEN_ACTIVITY_1");

            // 填充Payload object.
            // Note that "to" is a topic, not a token representing an app instance
            //TODO： 填充图像数据
            jPayload.put("to", "/topics/news");
            jPayload.put("priority", "high");
            jPayload.put("notification", jNotification);

            // 打开一个HTTP连接 and send the payload
            URL url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", SERVER_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send FCM message content.
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(jPayload.toString().getBytes());
            outputStream.close();

            // Read FCM response.
            InputStream inputStream = conn.getInputStream();
            final String resp = convertStreamToString(inputStream);

            Handler h = new Handler(Looper.getMainLooper());
            h.post(new Runnable() {
                @Override
                public void run() {
                    Log.e(TAG, "run: " + resp);
                    Toast.makeText(FCMActivity.this, resp,Toast.LENGTH_LONG).show();
                }
            });


        } catch (JSONException | IOException e) {
            Log.e(TAG,"sendMessageToNews threw error",e);
        }
    }

    //方便log的方法
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }
}