package edu.neu.groupassignment.stickittoem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import edu.neu.groupassignment.stickittoem.model.History;

public class HistoryActivity extends AppCompatActivity {
    private ArrayList<History> historyList;

    private RecyclerView recyclerView;
    private RviewAdapter rviewAdapter;
    private RecyclerView.LayoutManager rLayoutManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyList = (ArrayList<History>) getIntent().getSerializableExtra("histories");
        Log.e("historyyyyy", String.valueOf(historyList.size()));
        setTitle("History");
        setContentView(R.layout.activity_history);
        final TextView countTextView = (TextView) findViewById(R.id.count_textview);
        countTextView.setText("Total Stickers Sent: " + String.valueOf(historyList.size()));
        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        createRecyclerView();
    }

    private void createRecyclerView() {
        rLayoutManger = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        rviewAdapter = new RviewAdapter(historyList);

        recyclerView.setAdapter(rviewAdapter);
        recyclerView.setLayoutManager(rLayoutManger);
    }
}