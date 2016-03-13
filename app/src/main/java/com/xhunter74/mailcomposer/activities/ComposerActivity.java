package com.xhunter74.mailcomposer.activities;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.xhunter74.mailcomposer.R;
import com.xhunter74.mailcomposer.gmail.EmailSender;
import com.xhunter74.mailcomposer.models.MessageModel;

import java.io.IOException;
import java.util.Arrays;

import javax.mail.MessagingException;

public class ComposerActivity extends AppCompatActivity {

    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final String[] SCOPES = {GmailScopes.GMAIL_COMPOSE};
    private GoogleAccountCredential mCredential;
    private TextView mFromTextView;
    private EditText mRecipientsEditText;
    private EditText mSubjectEditText;
    private EditText mBodyEditText;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composer);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.composer_activity_progress_dialog_message));
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        prepareControls();
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    private void prepareControls() {
        mFromTextView = (TextView) findViewById(R.id.activity_composer_from);
        mRecipientsEditText = (EditText) findViewById(R.id.activity_composer_to);
        mSubjectEditText = (EditText) findViewById(R.id.activity_composer_subject);
        mBodyEditText = (EditText) findViewById(R.id.activity_composer_body);
        ImageButton sendEmailButton = (ImageButton) findViewById(R.id.activity_composer_send_button);
        assert sendEmailButton != null;
        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyFormAndSendEmail();
            }
        });
    }

    private void verifyFormAndSendEmail() {
        if (isDeviceOnline()) {
            if (isCompleteForm()) {
                sendEmail();
            }
        } else {
            Toast.makeText(ComposerActivity.this,
                    getString(R.string.composer_activity_no_network_connection_message),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendEmail() {
        MessageModel messageModel = new MessageModel();
        messageModel.setFromAddress(mFromTextView.getText().toString());
        messageModel.setRecipientAddresses(mRecipientsEditText.getText().toString());
        messageModel.setSubject(mSubjectEditText.getText().toString());
        messageModel.setMessageBody(mBodyEditText.getText().toString());
        EmailSender emailSender = new EmailSender(ComposerActivity.this, mCredential, messageModel);
        new SendEmailTask().execute(emailSender);
    }

    private boolean isCompleteForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mFromTextView.getText())) {
            result = false;
            mFromTextView.setError(getString(R.string.composer_activity_from_address_empty_error));
        }
        if (TextUtils.isEmpty(mRecipientsEditText.getText())) {
            result = false;
            mRecipientsEditText.setError(getString(R.string.composer_activity_recipient_address_error));
        }
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            initComposer();
        } else {
            Toast.makeText(ComposerActivity.this,
                    getText(R.string.composer_activity_google_play_services_warning),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initComposer() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            mFromTextView.setText(mCredential.getSelectedAccountName());
        }
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        @SuppressWarnings("deprecation")
        final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        //noinspection deprecation
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS) {
            return false;
        }
        return true;
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        @SuppressWarnings("deprecation")
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionStatusCode,
                ComposerActivity.this, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(ComposerActivity.this,
                            getString(R.string.composer_activity_account_unspecified),
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }else{
                    sendEmail();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void chooseAccount() {
        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private void clearForm() {
        mRecipientsEditText.setText("");
        mBodyEditText.setText("");
        mSubjectEditText.setText("");
    }

    private class SendEmailTask extends AsyncTask<EmailSender, Void, Void> {
        private Exception mLastError;

        @Override
        protected Void doInBackground(EmailSender... params) {
            EmailSender emailSender = params[0];
            try {
                emailSender.sendEmail();
            } catch (MessagingException | IOException e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgress.dismiss();
            Toast.makeText(ComposerActivity.this,
                    getString(R.string.composer_activity_email_send_successfully),
                    Toast.LENGTH_LONG).show();
            clearForm();
        }

        @Override
        protected void onCancelled() {
            mProgress.dismiss();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            ComposerActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(ComposerActivity.this,
                            String.format(getString(R.string.composer_activity_exception_message),
                                    mLastError.getMessage()), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
