package com.xhunter74.mailcomposer.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xhunter74.mailcomposer.R;
import com.xhunter74.mailcomposer.databinding.ItemAttachmentBinding;
import com.xhunter74.mailcomposer.models.AttachmentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class AttachmentListAdapter extends RecyclerView.Adapter<AttachmentViewHolder> {

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
    public AttachmentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemAttachmentBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_attachment, parent, false);
        return new AttachmentViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(AttachmentViewHolder viewHolder, final int position) {
        final AttachmentModel attachment = mItems[position];
        viewHolder.binding.setAttachment(attachment);
        if (mOnDeleteButtonClicks.size() > 0) {
            viewHolder.binding.setDeleteButtonClick(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (OnDeleteButtonClick onDeleteButtonClick : mOnDeleteButtonClicks) {
                        onDeleteButtonClick.onClick(position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.length;
        } else {
            return 0;
        }
    }
}
