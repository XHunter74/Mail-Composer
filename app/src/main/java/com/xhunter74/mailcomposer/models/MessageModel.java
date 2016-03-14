package com.xhunter74.mailcomposer.models;

import android.text.TextUtils;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class MessageModel {
    private String mFromAddress;
    private String[] mRecipientAddresses;
    private String mSubject;
    private String mMessageBody;
    private String[] mAttachments;

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
        if (!TextUtils.isEmpty(addresses)) {
            addresses = addresses.replace(',', ';');
            if (addresses.contains(";")) {
                mRecipientAddresses = addresses.split(";");
            } else {
                mRecipientAddresses = new String[]{addresses};
            }
        }
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

    public String[] getAttachments() {
        return mAttachments;
    }

    public void setAttachments(String[] attachments) {
        mAttachments = attachments;
    }
}
