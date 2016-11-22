package com.miguelbcr.ui.rx_paparazzo.sample.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.miguelbcr.ui.rx_paparazzo.sample.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 * Created by miguel on 16/03/2016.
 */public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder>{
    private List<String> urlImages;

    public ImagesAdapter(List<String> urlImages) {
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

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.iv_image);
        }

        public void bind(String imageUrl) {
            Picasso.with(imageView.getContext()).load(new File(imageUrl)).into(imageView);
        }
    }
}