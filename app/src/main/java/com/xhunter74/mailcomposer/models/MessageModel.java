package com.xhunter74.mailcomposer.models;

import android.text.TextUtils;

import com.xhunter74.mailcomposer.utils.Utils;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class MessageModel {
    private String mFromAddress;
    private String[] mRecipientAddresses;
    private String mSubject;
    private String mMessageBody;
    private AttachmentModel[] mAttachments;

    public String getFromAddress() {
        return mFromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.mFromAddress = fromAddress;
    }

    public String[] getRecipientAddresses() {
        return mRecipientAddresses;
    }

    public void setRecipientAddresses(String addresses) {
        mRecipientAddresses = Utils.getEmailsFromString(addresses);
    }

    public String getSubject() {
        return mSubject;
    }

    public void setSubject(String subject) {
        this.mSubject = subject;
    }

    public String getMessageBody() {
        return mMessageBody;
    }

    public void setMessageBody(String messageBody) {
        this.mMessageBody = messageBody;
    }

    public AttachmentModel[] getAttachments() {
        return mAttachments;
    }

    public void setAttachments(AttachmentModel[] attachments) {
        mAttachments = attachments;
    }
}
