package com.example.arcgisruntime.sample.integratedwindowsauthentication;

import java.util.List;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.esri.arcgisruntime.portal.PortalItem;

public class PortalItemAdapter extends RecyclerView.Adapter<PortalItemAdapter.PortalItemViewHolder> {

  public interface OnItemClickListener {
    void onItemClick(PortalItem portalItem);
  }

  private List<PortalItem> mPortalItemList;
  private OnItemClickListener mOnItemClickListener;

  public static class PortalItemViewHolder extends RecyclerView.ViewHolder {

    public TextView mPortalItemTextView;

    public PortalItemViewHolder(View itemView) {
      super(itemView);
      mPortalItemTextView = itemView.findViewById(R.id.itemTextView);
    }

    public void bind(final PortalItem portalItem, final OnItemClickListener listener) {
      mPortalItemTextView.setText(portalItem.getTitle());
      // return the portal item object's itemID, rather than its title
      itemView.setOnClickListener(v -> listener.onItemClick(portalItem));
    }
  }

  public PortalItemAdapter(List<PortalItem> portalItemNames, OnItemClickListener onItemClickListener) {
    mPortalItemList = portalItemNames;
    mOnItemClickListener = onItemClickListener;
  }

  @Override
  public PortalItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View portalItemTextView = LayoutInflater.from(parent.getContext()).inflate(R.layout.portal_item_row, parent, false);
    return new PortalItemViewHolder(portalItemTextView);
  }

  @Override
  public void onBindViewHolder(PortalItemViewHolder holder, int position) {
    holder.bind(mPortalItemList.get(position), mOnItemClickListener);
    Log.d("stuff", mPortalItemList.get(position).getTitle());
  }

  @Override
  public int getItemCount() {
    return mPortalItemList.size();
  }
}
