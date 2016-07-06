package com.xhunter74.mailcomposer.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.xhunter74.mailcomposer.databinding.ItemAttachmentBinding;

public class AttachmentViewHolder extends RecyclerView.ViewHolder {

    public ItemAttachmentBinding binding;

    public AttachmentViewHolder(View view) {
        super(view);
        binding = DataBindingUtil.bind(view);
    }
}