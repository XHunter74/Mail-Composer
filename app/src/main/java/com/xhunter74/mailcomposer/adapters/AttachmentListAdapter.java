package com.xhunter74.mailcomposer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.xhunter74.mailcomposer.R;
import com.xhunter74.mailcomposer.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class AttachmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private final List<OnDeleteButtonClick> mOnDeleteButtonClicks;
    private String[] mItems;

    public AttachmentListAdapter(Context context, String[] items) {
        mContext = context;
        mItems = items;
        mOnDeleteButtonClicks = new ArrayList<>();
    }

    public void setOnDeleteButtonClickListeners(OnDeleteButtonClick onDeleteButtonClick) {
        mOnDeleteButtonClicks.add(onDeleteButtonClick);
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

    private void prepareAttachmentViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        AttachmentViewHolder attachmentViewHolder = (AttachmentViewHolder) viewHolder;
        final String fileName = FileUtils.getFileName(mItems[position]);
        attachmentViewHolder.mFileName.setText(fileName);
        attachmentViewHolder.mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (OnDeleteButtonClick onDeleteButtonClick : mOnDeleteButtonClicks) {
                    onDeleteButtonClick.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.length;
        } else {
            return 0;
        }
    }

    public interface OnDeleteButtonClick {
        void onClick(int position);
    }

    private class AttachmentViewHolder extends RecyclerView.ViewHolder {

        public final TextView mFileName;
        public final ImageButton mDeleteButton;

        public AttachmentViewHolder(View view) {
            super(view);
            mFileName = (TextView) view.findViewById(R.id.item_attachment_file_name);
            mDeleteButton = (ImageButton) view.findViewById(R.id.item_attachment_delete_button);
        }
    }
}
