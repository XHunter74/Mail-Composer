package com.xhunter74.mailcomposer.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.text.TextUtils;

import com.xhunter74.mailcomposer.BR;
import com.xhunter74.mailcomposer.utils.Utils;

import java.util.List;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class MessageModel extends BaseObservable {
    private String mFromAddress;
    private String mRecipientAddresses;
    private String mSubject;
    private String mMessageBody;
    private ObservableArrayList<AttachmentModel> mAttachments;

    public MessageModel() {
        mAttachments = new ObservableArrayList<>();
        mAttachments.addOnListChangedCallback(new ObservableList.OnListChangedCallback<ObservableList<AttachmentModel>>() {
            @Override
            public void onChanged(ObservableList<AttachmentModel> attachmentModels) {
                notifyPropertyChanged(BR.attachments);
            }

            @Override
            public void onItemRangeChanged(ObservableList<AttachmentModel> attachmentModels, int i, int i1) {
                notifyPropertyChanged(BR.attachments);
            }

            @Override
            public void onItemRangeInserted(ObservableList<AttachmentModel> attachmentModels, int i, int i1) {
                notifyPropertyChanged(BR.attachments);
            }

            @Override
            public void onItemRangeMoved(ObservableList<AttachmentModel> attachmentModels, int i, int i1, int i2) {
                notifyPropertyChanged(BR.attachments);
            }

            @Override
            public void onItemRangeRemoved(ObservableList<AttachmentModel> attachmentModels, int i, int i1) {
                notifyPropertyChanged(BR.attachments);
            }
        });
    }

    @Bindable
    public String getFromAddress() {
        return mFromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.mFromAddress = fromAddress;
        notifyPropertyChanged(BR.fromAddress);
    }

    @Bindable
    public String getRecipientAddresses() {
        return mRecipientAddresses;
    }

    public void setRecipientAddresses(String addresses) {
        mRecipientAddresses = addresses;
        notifyPropertyChanged(BR.recipientAddresses);
    }

    public String[] getRecipientAddressesArray() {
        String[] result = null;
        if (!TextUtils.isEmpty(mRecipientAddresses)) {
            result = Utils.getEmailsFromString(mRecipientAddresses);
        }
        return result;
    }

    @Bindable
    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
        notifyPropertyChanged(BR.subject);
    }

    @Bindable
    public String getMessageBody() {
        return mMessageBody;
    }

    public void setMessageBody(String messageBody) {
        this.mMessageBody = messageBody;
        notifyPropertyChanged(BR.messageBody);
    }

    @Bindable
    public List<AttachmentModel> getAttachments() {
        return mAttachments;
    }
}
