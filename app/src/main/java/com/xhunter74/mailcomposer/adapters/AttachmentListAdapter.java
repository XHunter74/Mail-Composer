package com.xhunter74.mailcomposer.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.xhunter74.mailcomposer.R;
import com.xhunter74.mailcomposer.databinding.ItemAttachmentBinding;
import com.xhunter74.mailcomposer.models.AttachmentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class AttachmentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<OnDeleteButtonClick> mOnDeleteButtonClicks;
    private AttachmentModel[] mItems;

    public AttachmentListAdapter(AttachmentModel[] items) {
        mItems = items;
        mOnDeleteButtonClicks = new ArrayList<>();
    }

    public void setOnDeleteButtonClickListeners(OnDeleteButtonClick onDeleteButtonClick) {
        mOnDeleteButtonClicks.add(onDeleteButtonClick);
    }

    public void setItems(AttachmentModel[] items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemAttachmentBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_attachment, parent, false);
        return new AttachmentViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        prepareAttachmentViewHolder(viewHolder, position);
    }

    private void prepareAttachmentViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        AttachmentViewHolder attachmentViewHolder = (AttachmentViewHolder) viewHolder;
        final AttachmentModel attachment = mItems[position];
        attachmentViewHolder.mFileName.setText(attachment.getFileName());
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

        public AttachmentViewHolder(ItemAttachmentBinding viewDataBinding) {
            super(viewDataBinding.getRoot());
            mFileName = viewDataBinding.itemAttachmentFileName;
            mDeleteButton = viewDataBinding.itemAttachmentDeleteButton;
        }
    }
}
