package com.miguelbcr.ui.rx_paparazzo2.sample.adapters;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.miguelbcr.ui.rx_paparazzo2.entities.FileData;
import com.miguelbcr.ui.rx_paparazzo2.sample.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
    private List<FileData> urlImages;

    public ImagesAdapter(List<FileData> urlImages) {
        this.urlImages = urlImages;
    }

    @Override
    public ImagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_images, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ImagesAdapter.ViewHolder holder, int position) {
        holder.bind(urlImages.get(position));
    }

    @Override
    public int getItemCount() {
        return urlImages == null ? 0 : urlImages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView filenameView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.iv_image);
            filenameView = (TextView)itemView.findViewById(R.id.iv_filename);
        }

        void bind(FileData fileData) {
            filenameView.setText(fileData.describe());
            File file = fileData.getFile();
            if (file != null && file.exists()) {
                Picasso.with(imageView.getContext())
                        .load(file)
                        .error(R.drawable.ic_description_black_48px)
                        .into(imageView);
            } else {
                if (fileData.isExceededMaximumFileSize()) {
                    filenameView.setText("MAXIMUM FILESIZE EXCEEDED");
                }

                Drawable drawable = AppCompatDrawableManager.get().getDrawable(imageView.getContext(), R.drawable.ic_description_black_48px);
                imageView.setImageDrawable(drawable);
            }
        }
    }
}