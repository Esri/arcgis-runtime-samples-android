package com.esri.arcgisruntime.sample.downloadpreplannedmap;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder> {
  private ArrayList<PreplannedAreaPreview> preplannedAreaPreviews;
  public DrawerAdapter(ArrayList<PreplannedAreaPreview> preplannedAreaPreviews) {
    this.preplannedAreaPreviews = preplannedAreaPreviews;
  }
  @Override
  public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view;
    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_item, parent, false);
    return new DrawerViewHolder(view);
  }
  @Override
  public void onBindViewHolder(DrawerViewHolder holder, int position) {
    holder.title.setText(preplannedAreaPreviews.get(position).getTitle());
    byte[] byteStream = preplannedAreaPreviews.get(position).getThumbnailByteStream();
    Bitmap thumbnail = BitmapFactory.decodeByteArray(byteStream, 0, byteStream.length);
    holder.preview.setImageBitmap(thumbnail);
    notifyDataSetChanged();
  }
  @Override
  public int getItemCount() {
    return preplannedAreaPreviews.size();
  }
  class DrawerViewHolder extends RecyclerView.ViewHolder {
    TextView title;
    ImageView preview;
    public DrawerViewHolder(View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.title);
      preview = itemView.findViewById(R.id.areaPreview);
    }
  }
}