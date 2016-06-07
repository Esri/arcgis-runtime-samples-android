package com.example.editfeatureattachments;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final ArrayList<String> attachmentName;
    public CustomList(Activity context,
                      ArrayList<String> attachmentList) {
        super(context, R.layout.attachment_entry, attachmentList);
        this.context = context;
        this.attachmentName = attachmentList;



    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.attachment_entry, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.AttachmentName);

        txtTitle.setText(attachmentName.get(position));
        return rowView;
    }

    public void refreshEvents() {
        this.notifyDataSetChanged();
    }
}
