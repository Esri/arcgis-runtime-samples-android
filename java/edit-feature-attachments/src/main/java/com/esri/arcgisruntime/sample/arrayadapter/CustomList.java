/* Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.esri.arcgisruntime.sample.arrayadapter;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.esri.arcgisruntime.sample.editfeatureattachments.R;

public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final ArrayList<String> attachmentName;
    public CustomList(Activity context,
                      ArrayList<String> attachmentList) {
        super(context, R.layout.attachment_entry, attachmentList);
        this.context = context;
        attachmentName = attachmentList;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        CustomList.ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.attachment_entry, null, true);

            holder = new CustomList.ViewHolder();
            holder.textTitle = convertView.findViewById(R.id.AttachmentName);

            convertView.setTag(holder);
        } else {
            holder = (CustomList.ViewHolder) convertView.getTag();
        }

        holder.textTitle.setText(attachmentName.get(position));

        return convertView;
    }

    private static class ViewHolder {
        TextView textTitle;
    }
}
