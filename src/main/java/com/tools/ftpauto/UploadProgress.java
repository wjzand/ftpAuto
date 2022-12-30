package com.tools.ftpauto;

public interface UploadProgress {
    void onProgress(String percent);
    void onComplete();
}
