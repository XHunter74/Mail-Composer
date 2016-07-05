package com.xhunter74.mailcomposer.activities;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.GmailScopes;
import com.xhunter74.mailcomposer.R;
import com.xhunter74.mailcomposer.adapters.AttachmentListAdapter;
import com.xhunter74.mailcomposer.databinding.ActivityComposerBinding;
import com.xhunter74.mailcomposer.dialogs.AttachmentsDialog;
import com.xhunter74.mailcomposer.gmail.EmailSender;
import com.xhunter74.mailcomposer.models.MessageModel;
import com.xhunter74.mailcomposer.utils.FileUtils;
import com.xhunter74.mailcomposer.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

public class ComposerActivity extends AppCompatActivity {

    private static final int REQUEST_ACCOUNT_PICKER = 1000;
    private static final int REQUEST_AUTHORIZATION = 1001;
    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final int FILE_SELECT_CODE = 1003;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {GmailScopes.GMAIL_COMPOSE};
    private GoogleAccountCredential mCredential;
    private ProgressDialog mProgress;
    private List<String> mAttachmentsList;
    private ActivityComposerBinding mBinding;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_composer);
        mAttachmentsList = new ArrayList<>();
        prepareControls();
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    private void prepareControls() {
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.composer_activity_progress_dialog_message));
        mBinding.activityComposerFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseAccount();
            }
        });
        mBinding.activityComposerAttachments.setText(
                String.format(getString(R.string.activity_composer_attachments),
                        mAttachmentsList.size()));
        mBinding.activityComposerAttachments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAttachmentsList.size() > 0) {
                    showAttachmentsDialog();
                }
            }
        });
    }

    private void showAttachmentsDialog() {
        final AttachmentsDialog attachmentsDialog = AttachmentsDialog
                .getDialogInstance(mAttachmentsList.toArray(new String[mAttachmentsList.size()]));
        attachmentsDialog.setOnDeleteButtonClicks(new AttachmentListAdapter.OnDeleteButtonClick() {
            @Override
            public void onClick(int position) {
                mAttachmentsList.remove(position);
                mBinding.activityComposerAttachments.setText(
                        String.format(getString(R.string.activity_composer_attachments),
                                mAttachmentsList.size()));
            }
        });
        attachmentsDialog.show(getFragmentManager(), AttachmentsDialog.TAG);
    }

    private void verifyFormAndSendEmail() {
        if (Utils.isDeviceOnline(ComposerActivity.this)) {
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
        messageModel.setFromAddress(mBinding.activityComposerFrom.getText().toString());
        messageModel.setRecipientAddresses(mBinding.activityComposerTo.getText().toString());
        messageModel.setSubject(mBinding.activityComposerSubject.getText().toString());
        messageModel.setMessageBody(mBinding.activityComposerBody.getText().toString());
        messageModel.setAttachments(mAttachmentsList.toArray(new String[mAttachmentsList.size()]));
        EmailSender emailSender = new EmailSender(ComposerActivity.this, mCredential, messageModel);
        new SendEmailTask().execute(emailSender);
    }

    private boolean isCompleteForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mBinding.activityComposerFrom.getText())) {
            result = false;
            mBinding.activityComposerFrom.setError(getString(R.string.composer_activity_from_address_empty_error));
        }
        String emails = mBinding.activityComposerTo.getText().toString();
        if (TextUtils.isEmpty(emails)) {
            result = false;
            mBinding.activityComposerTo.setError(getString(R.string.composer_activity_recipient_address_error));
        } else if (!Utils.isValidEmails(emails)) {
            result = false;
            mBinding.activityComposerTo.setError(getString(R.string.composer_activity_recipient_address_invalid_email));
        }
        return result;
    }

    private void initComposer() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            mBinding.activityComposerFrom.setText(mCredential.getSelectedAccountName());
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                String oldAccountName = mBinding.activityComposerFrom.getText().toString();
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED && TextUtils.isEmpty(oldAccountName)) {
                    Toast.makeText(ComposerActivity.this,
                            getString(R.string.composer_activity_account_unspecified),
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                } else {
                    sendEmail();
                }
                break;
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    String filePath = FileUtils.getPath(ComposerActivity.this, data.getData());
                    if (!TextUtils.isEmpty(filePath) && new File(filePath).exists()) {
                        addAttachments(filePath);
                    } else {
                        Toast.makeText(ComposerActivity.this,
                                String.format(getString(
                                        R.string.composer_activity_file_doesnt_exist_message),
                                        filePath),
                                Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void addAttachments(String path) {
        if (!mAttachmentsList.contains(path)) {
            mAttachmentsList.add(path);
            mBinding.activityComposerAttachments
                    .setText(String.format(getString(R.string.activity_composer_attachments),
                            mAttachmentsList.size()));
        } else {
            Toast.makeText(ComposerActivity.this,
                    getString(R.string.composer_activity_existed_attachment_warning),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void chooseAccount() {
        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private void clearForm() {
        mBinding.activityComposerTo.setText("");
        mBinding.activityComposerBody.setText("");
        mBinding.activityComposerSubject.setText("");
        mAttachmentsList = new ArrayList<>();
        mBinding.activityComposerAttachments.setText(
                String.format(getString(R.string.activity_composer_attachments),
                        mAttachmentsList.size()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mail_composer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_send_email:
                verifyFormAndSendEmail();
                return true;
            case R.id.action_add_attachment:
                showChooseFileDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showChooseFileDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, FILE_SELECT_CODE);

        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, FILE_SELECT_CODE);
        }
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
