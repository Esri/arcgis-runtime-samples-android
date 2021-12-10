/*
 * Copyright 2019 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.integratedwindowsauthentication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.esri.arcgisruntime.portal.PortalItem
import com.esri.arcgisruntime.sample.integratedwindowsauthentication.databinding.PortalItemRowBinding

class PortalItemAdapter(private val onItemClickListener: OnItemClickListener) :
  RecyclerView.Adapter<PortalItemAdapter.PortalItemViewHolder>() {

  // list of PortalItems to display
  private var portalItems: MutableList<PortalItem>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortalItemViewHolder {
      val binding = PortalItemRowBinding.inflate(LayoutInflater.from(parent.context),parent, false)
      return PortalItemViewHolder(binding)
  }

  override fun onBindViewHolder(holder: PortalItemViewHolder, position: Int) {
    holder.bind(portalItems?.get(position), onItemClickListener)
  }

  override fun getItemCount() = portalItems?.size ?: 0

  class PortalItemViewHolder(binding: PortalItemRowBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val itemTextView = binding.itemTextView

    fun bind(portalItem: PortalItem?, onItemClickListener: OnItemClickListener) {
      portalItem?.let {
        itemTextView.text = it.title
        itemView.setOnClickListener { _ ->
          onItemClickListener.onPortalItemClick(it)
        }
      }
    }
  }

  fun updatePortalItems(portalItems: List<PortalItem>) {
    if (this.portalItems == null) {
      this.portalItems = ArrayList()
    }

    DiffUtil.calculateDiff(PortalItemsDiffUtilCallback(this.portalItems, portalItems)).let {
      this.portalItems?.clear()
      this.portalItems?.addAll(portalItems)
      it.dispatchUpdatesTo(this)
    }
  }

  interface OnItemClickListener {
    fun onPortalItemClick(portalItem: PortalItem)
  }
}

class PortalItemsDiffUtilCallback(
  private val oldPortalItems: List<PortalItem>?,
  private val newPortalItems: List<PortalItem>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
    oldPortalItems?.get(oldItemPosition)?.itemId == newPortalItems[newItemPosition].itemId

  override fun getOldListSize() = oldPortalItems?.size ?: 0

  override fun getNewListSize() = newPortalItems.size

  override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
    oldPortalItems?.get(oldItemPosition) == newPortalItems[newItemPosition]

}
