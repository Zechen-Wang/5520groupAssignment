package edu.neu.groupassignment.stickittoem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private DatabaseReference userRef;
    private TextView usernameTextView;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameTextView = findViewById(R.id.usernameTextView);
        loginBtn = findViewById(R.id.loginBtn);
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

    }

    public void loginToMainUI(View v) {
        if (usernameTextView.getText().length() == 0) {
            Toast.makeText(this, "PLease enter a username", Toast.LENGTH_LONG).show();
            return;
        }
        String username = usernameTextView.getText().toString();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }


}