package com.esri.arcgisruntime.sample.downloadpreplannedmap;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder> {
  private ArrayList<DrawerItem> drawerMenuList;
  public DrawerAdapter(ArrayList<DrawerItem> drawerMenuList) {
    this.drawerMenuList = drawerMenuList;
  }
  @Override
  public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view;
    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
    return new DrawerViewHolder(view);
  }
  @Override
  public void onBindViewHolder(DrawerViewHolder holder, int position) {
    holder.title.setText(drawerMenuList.get(position).getTitle());
    holder.icon.setImageResource(drawerMenuList.get(position).getIcon());
  }
  @Override
  public int getItemCount() {
    return drawerMenuList.size();
  }
  class DrawerViewHolder extends RecyclerView.ViewHolder {
    TextView title;
    ImageView icon;
    public DrawerViewHolder(View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.title);
      icon = itemView.findViewById(R.id.icon);
    }
  }
}