package com.xhunter74.mailcomposer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xhunter74.mailcomposer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class AttachmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = AttachmentListAdapter.class.getName();
    private final Context mContext;
    private final List<OnAttachmentLongClick> mOnAttachmentLongClicks;
    private String[] mItems;

    public AttachmentListAdapter(Context context, String[] items) {
        mContext = context;
        mItems = items;
        mOnAttachmentLongClicks = new ArrayList<>();
    }

    public void setOnAttachmentLongClickListeners(OnAttachmentLongClick onAttachmentLongClick) {
        mOnAttachmentLongClicks.add(onAttachmentLongClick);
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
        final String fileName = getFileName(mItems[position]);
        attachmentViewHolder.mFileName.setText(fileName);
        attachmentViewHolder.mAttachmentContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                for (OnAttachmentLongClick onAttachmentLongClick : mOnAttachmentLongClicks) {
                    onAttachmentLongClick.onLongClick(position);
                }
                return false;
            }
        });
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

    public interface OnAttachmentLongClick {
        void onLongClick(int position);
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
