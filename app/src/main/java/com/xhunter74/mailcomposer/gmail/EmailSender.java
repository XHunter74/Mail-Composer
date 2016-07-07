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
import com.xhunter74.mailcomposer.models.AttachmentModel;
import com.xhunter74.mailcomposer.models.MessageModel;
import com.xhunter74.mailcomposer.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by Serhiy.Krasovskyy on 13.03.2016.
 */
public class EmailSender {
    private static final String USER_ID = "me";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String MESSAGE_CONTENT_TYPE = "text/plain; charset=\"UTF-8\"";
    private static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    private static final String BASE64 = "base64";
    private static final String UTF8 = "UTF-8";
    private final MessageModel mMessageModel;
    private final Context mContext;
    private final GoogleAccountCredential mCredential;

    public EmailSender(Context context, GoogleAccountCredential credential,
                       MessageModel messageModel) {
        mContext = context;
        mCredential = credential;
        mMessageModel = messageModel;
    }

    public void sendEmail() throws MessagingException, IOException {
        MimeMessage mimeMessage;
        if (mMessageModel.getAttachments().size() > 0) {
            mimeMessage = createEmailWithAttachment(mMessageModel);
        } else {
            mimeMessage = createEmail(mMessageModel);
        }
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
        if (messageModel.getRecipientAddressesArray().length > 1) {
            InternetAddress[] addresses = getInternetAddresses(messageModel.getRecipientAddressesArray());
            email.addRecipients(javax.mail.Message.RecipientType.TO, addresses);
        } else {
            email.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(messageModel.getRecipientAddressesArray()[0]));
        }
        email.setSubject(messageModel.getSubject(), UTF8);
        email.setText(messageModel.getMessageBody(), UTF8);
        return email;
    }

    private MimeMessage createEmailWithAttachment(MessageModel messageModel)
            throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(messageModel.getFromAddress()));
        if (messageModel.getRecipientAddressesArray().length > 1) {
            InternetAddress[] addresses = getInternetAddresses(messageModel.getRecipientAddressesArray());
            email.addRecipients(javax.mail.Message.RecipientType.TO, addresses);
        } else {
            email.addRecipient(javax.mail.Message.RecipientType.TO,
                    new InternetAddress(messageModel.getRecipientAddressesArray()[0]));
        }

        email.setSubject(messageModel.getSubject(), UTF8);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(messageModel.getMessageBody(), MESSAGE_CONTENT_TYPE);
        mimeBodyPart.setHeader(CONTENT_TYPE, MESSAGE_CONTENT_TYPE);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        multipart = addMessageAttachments(multipart, messageModel.getAttachments());
        email.setContent(multipart);

        return email;
    }

    private Multipart addMessageAttachments(Multipart multipart, List<AttachmentModel> attachments)
            throws MessagingException {

        for (AttachmentModel attachment : attachments) {
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachment.path);
            mimeBodyPart.setDataHandler(new DataHandler(source));
            String fileName = FileUtils.getFileName(attachment.path);
            mimeBodyPart.setFileName(fileName);
            String contentType = FileUtils.getContentType(attachment.path);
            mimeBodyPart.setHeader(CONTENT_TYPE, contentType + "; name=\"" + fileName + "\"");
            mimeBodyPart.setHeader(CONTENT_TRANSFER_ENCODING, BASE64);
            multipart.addBodyPart(mimeBodyPart);
        }
        return multipart;
    }


    private InternetAddress[] getInternetAddresses(String[] addresses) throws AddressException {
        List<InternetAddress> result = new ArrayList<>();
        for (String address : addresses) {
            result.add(new InternetAddress(address));
        }
        return result.toArray(new InternetAddress[addresses.length]);
    }
}
