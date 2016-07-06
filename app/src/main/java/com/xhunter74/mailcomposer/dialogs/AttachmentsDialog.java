package com.xhunter74.mailcomposer.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xhunter74.mailcomposer.R;
import com.xhunter74.mailcomposer.adapters.AttachmentListAdapter;
import com.xhunter74.mailcomposer.adapters.OnDeleteButtonClick;
import com.xhunter74.mailcomposer.databinding.DialogAttachmentsBinding;
import com.xhunter74.mailcomposer.models.AttachmentModel;
import com.xhunter74.mailcomposer.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class AttachmentsDialog extends DialogFragment {

    public static final String TAG = AttachmentsDialog.class.getName();
    private ViewGroup mParent;
    private List<AttachmentModel> mAttachments;
    private AttachmentListAdapter mAttachmentListAdapter;
    private List<OnDeleteButtonClick> mOnDeleteButtonClicks;

    public static AttachmentsDialog getDialogInstance(@NonNull AttachmentModel[] attachments) {
        AttachmentsDialog dialogFragment = new AttachmentsDialog();
        Bundle bundle = new Bundle();
        bundle.putStringArray(Constants.Extras.ATTACHMENTS, getStringAttachmentsArray(attachments));
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    private static String[] getStringAttachmentsArray(AttachmentModel[] attachments) {
        String[] result = new String[attachments.length];
        for (int i = 0; i < attachments.length; i++) {
            result[i] = attachments[i].path;
        }
        return result;
    }

    public void setOnDeleteButtonClicks(OnDeleteButtonClick onDeleteButtonClick) {
        if (mOnDeleteButtonClicks == null) {
            mOnDeleteButtonClicks = new ArrayList<>();
        }
        mOnDeleteButtonClicks.add(onDeleteButtonClick);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.mParent = container;
        return container;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mAttachments = new ArrayList<>();
        String[] attachments = getArguments().getStringArray(Constants.Extras.ATTACHMENTS);
        for (String filePath : attachments) {
            mAttachments.add(new AttachmentModel(filePath));
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        DialogAttachmentsBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(getActivity()), R.layout.dialog_attachments, mParent, false);
        builder.setView(binding.getRoot());
        prepareDialogAdapter(binding);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @SuppressLint("NewApi")
    private void prepareDialogAdapter(DialogAttachmentsBinding viewBinding) {
        mAttachmentListAdapter = new AttachmentListAdapter(
                mAttachments.toArray(new AttachmentModel[mAttachments.size()]));
        mAttachmentListAdapter.setOnDeleteButtonClickListeners(new OnDeleteButtonClick() {
            @Override
            public void onClick(int position) {
                mAttachments.remove(position);
                mAttachmentListAdapter
                        .setItems(mAttachments.toArray(new AttachmentModel[mAttachments.size()]));
                for (OnDeleteButtonClick onDeleteButtonClick
                        : mOnDeleteButtonClicks) {
                    onDeleteButtonClick.onClick(position);
                }
                if (mAttachments.size() == 0) {
                    dismiss();
                }
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(viewBinding.getRoot().getContext(),
                LinearLayoutManager.VERTICAL, false);
        viewBinding.dialogAttachmentsAttachmentsList.setLayoutManager(layoutManager);
        viewBinding.dialogAttachmentsAttachmentsList.setAdapter(mAttachmentListAdapter);
    }
}
