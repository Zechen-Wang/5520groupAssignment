package edu.neu.groupassignment.stickittoem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button send;
    private String friendlyName;
    private DatabaseReference userRef;
    private ArrayList<String> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send = findViewById(R.id.btn_send);
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users = new ArrayList<>();
                for (DataSnapshot userSnapshot: snapshot.getChildren()) {
                    String user = userSnapshot.getKey();
                    users.add(user);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendStick(View v) {
        friendlyName = ((EditText) findViewById(R.id.editText)).getText().toString();
        if (users.contains(friendlyName)) {
            Toast.makeText(this, "Success", Toast.LENGTH_LONG).show();
            return;
        } else {
            Toast.makeText(this, "No such user", Toast.LENGTH_LONG).show();
            return;
        }
    }
}