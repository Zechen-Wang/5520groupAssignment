package edu.neu.groupassignment.stickittoem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import edu.neu.groupassignment.stickittoem.model.History;

public class RviewAdapter extends RecyclerView.Adapter<RviewHolder> {

    private final ArrayList<History> historyList;

    //Constructor
    public RviewAdapter(ArrayList<History> historyList) {
        this.historyList = historyList;
    }

    @Override
    public RviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new RviewHolder(view);
    }

    @Override
    public void onBindViewHolder(RviewHolder holder, int position) {
        History currentItem = historyList.get(position);

        holder.receiver.setText(currentItem.getTo());
        holder.time.setText(currentItem.getTime());
        Uri uri = Uri.parse(currentItem.getImage());
        Picasso.get().load(uri).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }
}
