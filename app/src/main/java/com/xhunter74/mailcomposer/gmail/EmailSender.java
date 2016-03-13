package com.xhunter74.mailcomposer.gmail;

import android.content.Context;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.xhunter74.mailcomposer.R;
import com.xhunter74.mailcomposer.models.MessageModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class EmailSender {
    private static final String USER_ID = "me";
    private final MessageModel mMessageModel;
    private Context mContext;
    private GoogleAccountCredential mCredential;

    public EmailSender(Context context, GoogleAccountCredential credential,
                       MessageModel messageModel) {
        mContext = context;
        mCredential = credential;
        mMessageModel = messageModel;
    }

    public void sendEmail() throws MessagingException, IOException {
        MimeMessage mimeMessage = createEmail(mMessageModel);
        //Message email = createMessageWithEmail(mimeMessage);
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Gmail service = new Gmail.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName(mContext.getString(R.string.app_name))
                .build();
        sendMessage(service, mimeMessage);
    }

    private void sendMessage(Gmail service, MimeMessage mimeMessage)
            throws IOException, MessagingException {
        Message message = createMessageWithEmail(mimeMessage);
        service.users().messages().send(USER_ID, message).execute();
    }

    private Message createMessageWithEmail(MimeMessage mimeMessage)
            throws IOException, MessagingException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        mimeMessage.writeTo(bytes);
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }

    private MimeMessage createEmail(MessageModel messageModel) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(messageModel.getFromAddress()));
        if (messageModel.getRecipientAddresses().length > 1) {
            InternetAddress[] addresses = getInternetAddresses(messageModel.getRecipientAddresses());
            email.addRecipients(javax.mail.Message.RecipientType.TO, addresses);
        } else {
            email.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(messageModel.getRecipientAddresses()[0]));
        }
        email.setSubject(messageModel.getSubject());
        email.setText(messageModel.getMessageBody());
        return email;
    }

    private InternetAddress[] getInternetAddresses(String[] addresses) throws AddressException {
        List<InternetAddress> result = new ArrayList<>();
        for (String address : addresses) {
            result.add(new InternetAddress(address));
        }
        return result.toArray(new InternetAddress[addresses.length]);
    }
}
