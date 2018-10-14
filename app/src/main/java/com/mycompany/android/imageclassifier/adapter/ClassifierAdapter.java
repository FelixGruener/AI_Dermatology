package com.mycompany.android.imageclassifier.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mycompany.android.imageclassifier.R;
import com.mycompany.android.imageclassifier.database.ImageEntry;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by delaroy on 9/5/18.
 */

public class ClassifierAdapter extends RecyclerView.Adapter<ClassifierAdapter.ClassifierViewHolder> {

    // Member variable to handle item clicks
    final private ItemClickListener mItemClickListener;
    private List<ImageEntry> mImageEntries;
    private Context mContext;

    public ClassifierAdapter(Context context, ItemClickListener listener) {
        mContext = context;
        mItemClickListener = listener;
    }

    @Override
    public ClassifierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.classifier_item, parent, false);

        return new ClassifierViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassifierViewHolder holder, int position) {
        // Determine the values of the wanted data
        ImageEntry imageEntry = mImageEntries.get(position);
        String labeldesc = imageEntry.getPredclass();
        String images = imageEntry.getImage();

        //Set values
        holder.label.setText(labeldesc);
        if (labeldesc.equals("melanoma")) {
            holder.location.setText("Cancerous");
        } else{
            holder.location.setText("No Cancer");
        }

        Glide.with(mContext)
                .load(images)
                .into(holder.imageView);
    }

    /**
     * Returns the number of items to display.
     */
    @Override
    public int getItemCount() {
        if (mImageEntries == null) {
            return 0;
        }
        return mImageEntries.size();
    }

    public List<ImageEntry> getClassifier() {
        return mImageEntries;
    }


    public void setTasks(List<ImageEntry> imageEntries) {
        mImageEntries = imageEntries;
        notifyDataSetChanged();
    }

    public interface ItemClickListener {
        void onItemClickListener(int itemId);
    }

    // Inner class for creating ViewHolders
    public class ClassifierViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView label;
        TextView location;
        CircleImageView imageView;
        public RelativeLayout viewBackground, viewForeground;

        public ClassifierViewHolder(View itemView) {
            super(itemView);

            label = itemView.findViewById(R.id.label);
            location = itemView.findViewById(R.id.location);
            imageView = itemView.findViewById(R.id.image);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int elementId = mImageEntries.get(getAdapterPosition()).getId();
            mItemClickListener.onItemClickListener(elementId);
        }
    }
}
