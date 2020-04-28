package com.example.moneyball;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class WagerAdapter extends RecyclerView.Adapter<WagerAdapter.MyViewHolder>{
    private ArrayList<Wager> mDataset;
    private ItemClickListener mClickListener;
    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    Context context;
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public LinearLayout container;
        public TextView header, description;
        public ImageView picture;
        public MyViewHolder(View v) {
            super(v);
            header = v.findViewById(R.id.header);
            picture = v.findViewById(R.id.picture);
            description = v.findViewById(R.id.description);
            context = v.getContext();

            v.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public WagerAdapter(ArrayList<Wager> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public WagerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String UID = "";
        if(user!=null){
            UID = user.getUid();
        }
        holder.header.setText(mDataset.get(position).getHeading());
        holder.description.setText(mDataset.get(position).getDescription());
        Boolean openStatus = mDataset.get(position).getOpenStatus();
        ArrayList<String> usersList = mDataset.get(position).getUsersList();
        Log.d("tag4", openStatus.toString());
        if(openStatus==false){
            holder.header.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRed));
            holder.description.setBackgroundColor(ContextCompat.getColor(context, R.color.colorRed));
        } else {
            if(usersList.contains(UID)) {
                holder.header.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreen));
                holder.description.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreen));
            } else {
                holder.header.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreenLight));
                holder.description.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGreenLight));
            }
        }

        //setting the picture holder with picture url from internet
        Uri picUri = Uri.parse(mDataset.get(position).getPicture());//turn string from Wager object into url
        Picasso.get().load(picUri).into(holder.picture);//use PICASSSOOOO for loading picture urls into imageView or whatever holders u need.
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // convenience method for getting data at click position
    Wager getItem(int id) {
        return mDataset.get(id);
    }
    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

