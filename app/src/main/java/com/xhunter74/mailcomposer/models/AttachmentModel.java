package com.xhunter74.mailcomposer.models;

import android.text.TextUtils;

import com.xhunter74.mailcomposer.utils.FileUtils;

public class AttachmentModel {

    public String path;

    public AttachmentModel(String path) {
        this.path = path;
    }

    public String getFileName() {
        if (!TextUtils.isEmpty(path)) {
            return FileUtils.getFileName(path);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o != null && o instanceof AttachmentModel) {
            AttachmentModel attachment = (AttachmentModel) o;
            if (!TextUtils.isEmpty(attachment.path) && attachment.path.equals(this.path)) {
                result = true;
            } else if (attachment.path == null && this.path == null) {
                result = true;
            }
        }
        return result;
    }
}
