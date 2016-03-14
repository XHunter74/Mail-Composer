package com.xhunter74.mailcomposer.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xhunter74.mailcomposer.R;
import com.xhunter74.mailcomposer.adapters.AttachmentListAdapter;
import com.xhunter74.mailcomposer.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class AttachmentsDialog extends DialogFragment {

    public static final String TAG = AttachmentsDialog.class.getName();
    private ViewGroup mParent;
    private List<String> mAttachments;
    private AttachmentListAdapter mAttachmentListAdapter;
    private List<AttachmentListAdapter.OnAttachmentLongClick> mOnAttachmentLongClicks;

    public static AttachmentsDialog getDialogInstance(@NonNull String[] attachments) {
        AttachmentsDialog dialogFragment = new AttachmentsDialog();
        Bundle bundle = new Bundle();
        bundle.putStringArray(Constants.Extras.ATTACHMENTS, attachments);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    public void setOnAttachmentLongClicks(
            AttachmentListAdapter.OnAttachmentLongClick onAttachmentLongClick) {
        if (mOnAttachmentLongClicks == null) {
            mOnAttachmentLongClicks = new ArrayList<>();
        }
        mOnAttachmentLongClicks.add(onAttachmentLongClick);
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
        assert attachments != null;
        mAttachments.addAll(Arrays.asList(attachments));
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_attachments, mParent);
        builder.setView(view);
        prepareDialogAdapter(view);
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @SuppressLint("NewApi")
    private void prepareDialogAdapter(View view) {
        mAttachmentListAdapter = new AttachmentListAdapter(
                getActivity(), mAttachments.toArray(new String[mAttachments.size()]));
        mAttachmentListAdapter.setOnAttachmentLongClickListeners(
                new AttachmentListAdapter.OnAttachmentLongClick() {
                    @Override
                    public void onLongClick(int position) {
                        mAttachments.remove(position);
                        mAttachmentListAdapter
                                .setItems(mAttachments.toArray(new String[mAttachments.size()]));
                        for (AttachmentListAdapter.OnAttachmentLongClick onAttachmentLongClick
                                : mOnAttachmentLongClicks) {
                            onAttachmentLongClick.onLongClick(position);
                        }
                        if (mAttachments.size()==0){
                            dismiss();
                        }
                    }
                });
        RecyclerView attachmentsList = (RecyclerView)
                view.findViewById(R.id.dialog_attachments_attachments_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                view.getContext(), LinearLayoutManager.VERTICAL, false);
        attachmentsList.setLayoutManager(layoutManager);
        attachmentsList.setAdapter(mAttachmentListAdapter);
    }
}
