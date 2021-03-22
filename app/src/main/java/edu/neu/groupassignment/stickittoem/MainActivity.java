package edu.neu.groupassignment.stickittoem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import edu.neu.groupassignment.stickittoem.R;
import edu.neu.groupassignment.stickittoem.model.User;

import com.squareup.picasso.Picasso;



public class MainActivity extends AppCompatActivity {
    private static final String SERVER_KEY = "key=AAAALzse8gE:APA91bHsJPnCQ_E-eCFmdIOrLAZz0B11jxgVoUkt62Kfb-ZvubuxGTTAx0rWpavmr1ZRg6N6-zlx0Eg0P1t0tzwDsWdq7WahoXceZJsNhbmDiNjMldtd7So4Cj0l874UddKYmy31u0BY";
    private static final String TAG = "fcm";
    private Button send;
    private String username, friendName, token;
    private DatabaseReference userRef;
    private Map<String, User> users;
    private ArrayList<RadioButton> radioButtons;
    private RadioButton button1, button2, button3, button4, button5, button6;
    private ImageView image1, image2, image3, image4, image5, image6;
    private String selected;
    private Map<RadioButton, String> buttonImageMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initImages();
        send = findViewById(R.id.btn_send);
        username = getIntent().getStringExtra("username");
        setTitle(username);

        // database reference: users
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Error getting data", task.getException());
                }
                else {
                    updateUsersMap(task.getResult().getChildren());
                    setToken();
                    createUser();
                }
            }
        });

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateUsersMap(snapshot.getChildren());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initImages() {
        buttonImageMap = new HashMap<>();
        image1 = findViewById(R.id.image_1);
        image2 = findViewById(R.id.image_2);
        image3 = findViewById(R.id.image_3);
        image4 = findViewById(R.id.image_4);
        image5 = findViewById(R.id.image_5);
        image6 = findViewById(R.id.image_6);
        radioButtons = new ArrayList<>();
        button1 = findViewById(R.id.radioButton_1);
        radioButtons.add(button1);
        button2 = findViewById(R.id.radioButton_2);
        radioButtons.add(button2);
        button3 = findViewById(R.id.radioButton_3);
        radioButtons.add(button3);
        button4 = findViewById(R.id.radioButton_4);
        radioButtons.add(button4);
        button5 = findViewById(R.id.radioButton_5);
        radioButtons.add(button5);
        button6 = findViewById(R.id.radioButton_6);
        radioButtons.add(button6);
        displayImage(button1, image1, "gs://stick-it-to-em-5be57.appspot.com/Grinning Emoji with Smiling Eyes.png");
        displayImage(button2, image2, "gs://stick-it-to-em-5be57.appspot.com/Smiling Devil Emoji.png");
        displayImage(button3, image3, "gs://stick-it-to-em-5be57.appspot.com/Smiling Emoji with Eyes Opened.png");
        displayImage(button4, image4, "gs://stick-it-to-em-5be57.appspot.com/Smiling Face Emoji with Blushed Cheeks.png");
        displayImage(button5, image5, "gs://stick-it-to-em-5be57.appspot.com/Sunglasses Emoji.png");
        displayImage(button6, image6, "gs://stick-it-to-em-5be57.appspot.com/Tongue Out Emoji with Winking Eye.png");


    }

    public void selectImage(View view) {
        for (RadioButton radioButton: radioButtons){
            radioButton.setChecked(false);
        }
        RadioButton thisRadioButton = findViewById(view.getId());
        thisRadioButton.setChecked(true);
        selected = buttonImageMap.get(thisRadioButton);
    }

    private void displayImage(RadioButton button, ImageView image, String url) {
        StorageReference gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                buttonImageMap.put(button, uri.toString());
                Picasso.get().load(uri).into(image);
            }
        });
    }

    private void updateUsersMap(Iterable <DataSnapshot> it) {
        users = new HashMap<>();
        for (DataSnapshot userSnapshot: it) {
            String user = userSnapshot.getKey();
            int sentCount = ((Long) userSnapshot.child("sentCount").getValue()).intValue();
            String token = (String) userSnapshot.child("token").getValue();
            String userId = (String) userSnapshot.child("userId").getValue();
            users.put(user, new User(userId, user, token, sentCount));
        }
    }

    /*
     * 新建一个user存在database里（有可能覆盖，username一样的话）
     * TODO： 数据库有username的话，就把这个user数据取出来（名字，token，发送sticker个数）
     * */
    private void createUser() {
        if (users.containsKey(username)) {
            // update token only
            User user = users.get(username);
            user.setToken(token);
            userRef.child(username).setValue(user).addOnCompleteListener(task ->{
                if  (task.isSuccessful()) {
                    Toast.makeText(this, "Load existing user success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Load existing user fail", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // create a new user
            String userId = userRef.push().getKey(); //生成id
            User user = new User(userId, username, token);
            userRef.child(username).setValue(user).addOnCompleteListener(task ->{
                if  (task.isSuccessful()) {
                    Toast.makeText(this, "Add new user success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "add new user fail", Toast.LENGTH_SHORT).show();
                }
            });
        }
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

    private void accumateSentStickers() {
        User user = users.get(username);
        user.accumulateSentCount();
        userRef.child(username).setValue(user).addOnCompleteListener(task ->{
            if  (task.isSuccessful()) {
                Toast.makeText(this, "Save history success", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Save history fail", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //bind to onclick of sendBtn
    public void sendMessage(View type) {
        friendName = ((EditText) findViewById(R.id.editText)).getText().toString();
        if (users.containsKey(friendName)) {
            if (selected != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendMessage(users.get(friendName).getToken(), selected);
                    }
                }).start();
                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
                accumateSentStickers();
            } else {
                Toast.makeText(this, "Please select a sticker", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "No such user", Toast.LENGTH_LONG).show();
        }
    }

    // helper function of above
    private void sendMessage(String targetToken, String selected) {
        JSONObject jPayload = new JSONObject();
        JSONObject jNotification = new JSONObject();

        try{
            // 创建一个notification object
            jNotification.put("title", "Stick It To 'Em");
            jNotification.put("body", "Sticker Sent From " + username);
            jNotification.put("sound", "default");
            jNotification.put("badge", "1");
            jNotification.put("image", selected);
            jNotification.put("click_action", "OPEN_ACTIVITY_1");

            // If sending to a single client
            jPayload.put("to", targetToken);
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
                    Log.d(TAG, "run: " + resp);
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