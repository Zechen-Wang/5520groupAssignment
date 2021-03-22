package edu.neu.groupassignment.stickittoem;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class RviewHolder extends RecyclerView.ViewHolder {
    public TextView receiver;
    public TextView time;
    public ImageView image;

    public RviewHolder(View itemView) {
        super(itemView);
        receiver = itemView.findViewById(R.id.receiver);
        time = itemView.findViewById(R.id.time);
        image = itemView.findViewById(R.id.image);
    }
}