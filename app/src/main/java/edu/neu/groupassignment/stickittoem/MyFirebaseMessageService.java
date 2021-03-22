package edu.neu.groupassignment.stickittoem;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

public class MyFirebaseMessageService extends FirebaseMessagingService {

    public static final String TAG = "message";
    private static final String CHANNEL_ID  = "CHANNEL_ID";
    private static final String CHANNEL_NAME  = "CHANNEL_NAME";
    private static final String CHANNEL_DESCRIPTION  = "CHANNEL_DESCRIPTION";

    public MyFirebaseMessageService() {
        super();
        Log.d(TAG, "service running");
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "notification received");


        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(remoteMessage);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("message", "Refreshed token: " + token);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fb", token).apply();
    }

    //保证在FCMActivity中调用，可以获得最新的token（如果token换了的话）
    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty");
    }

    /*
    * 设置notification的内容
    * */
    private void showNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Notification notification;
        NotificationCompat.Builder builder;
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
            // Configure the notification channel
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this,CHANNEL_ID);

        }
        else {
            builder = new NotificationCompat.Builder(this);
        }

        Bitmap image = null;

        try{
            URL url = new URL(remoteMessage.getNotification().getImageUrl().toString());
            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch(IOException e) {
            System.out.println(e);
        }

        notification = builder.setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(image)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();


        notificationManager.notify(0,notification);
    }

}