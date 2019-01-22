package com.example.arcgisruntime.sample.integratedwindowsauthentication;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class PortalItemAdapter extends RecyclerView.Adapter<PortalItemAdapter.PortalItemViewHolder> {

  private String[] mPortalItemNames;

  public static class PortalItemViewHolder extends RecyclerView.ViewHolder {

    public TextView mPortalItemTextView;

    public PortalItemViewHolder(View itemView) {
      super(itemView);
      mPortalItemTextView = itemView.findViewById(R.id.itemTextView);
    }
  }

  public PortalItemAdapter(String[] portalItemNames) {
    mPortalItemNames = portalItemNames;
    Log.d("stuff", "adapter size: " + portalItemNames.length);
  }

  @Override
  public PortalItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View portalItemTextView = LayoutInflater.from(parent.getContext()).inflate(R.layout.portal_item_row, parent, false);
    return new PortalItemViewHolder(portalItemTextView);
  }

  @Override
  public void onBindViewHolder(PortalItemViewHolder holder, int position) {
    holder.mPortalItemTextView.setText(mPortalItemNames[position]);
  }

  @Override
  public int getItemCount() {
    Log.d("stuff", "get item count: " + mPortalItemNames.length);
    return mPortalItemNames.length;
  }
}
