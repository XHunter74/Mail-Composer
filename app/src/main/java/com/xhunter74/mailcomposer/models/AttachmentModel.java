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
}
