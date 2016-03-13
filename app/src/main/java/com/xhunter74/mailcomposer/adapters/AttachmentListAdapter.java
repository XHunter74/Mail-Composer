package com.xhunter74.mailcomposer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xhunter74.mailcomposer.R;

import java.io.File;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class AttachmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = AttachmentListAdapter.class.getName();
    private final Context mContext;
    private String[] mItems;

    public AttachmentListAdapter(Context context, String[] items) {
        mContext = context;
        mItems = items;
    }

    public void setItems(String[] items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_attachment, parent, false);
        return new AttachmentViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        prepareAttachmentViewHolder(viewHolder, position);
    }

    private void prepareAttachmentViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        AttachmentViewHolder attachmentViewHolder = (AttachmentViewHolder) viewHolder;
        String fileName = getFileName(mItems[position]);
        attachmentViewHolder.mFileName.setText(fileName);
    }

    private String getFileName(String filePath) {
        File file = new File(filePath);
        return file.getName();
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.length;
        } else {
            return 0;
        }
    }

    private class AttachmentViewHolder extends RecyclerView.ViewHolder {

        public final View mAttachmentContainer;
        public final TextView mFileName;

        public AttachmentViewHolder(View view) {
            super(view);
            mAttachmentContainer = view.findViewById(R.id.item_attachment_container);
            mFileName = (TextView) view.findViewById(R.id.item_attachment_file_name);
        }
    }
}
