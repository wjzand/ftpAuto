package com.tools.ftpauto;

import com.jcraft.jsch.SftpProgressMonitor;

public class UploadProgressMonitor implements SftpProgressMonitor {
    private long max = 0;
    private long count =0;

    private UploadProgress uploadProgress;

    public UploadProgressMonitor(UploadProgress uploadProgress,long length) {
        this.uploadProgress = uploadProgress;
        this.max = length;
    }

    @Override
    public void init(int op, String src, String dest, long max) {

    }

    @Override
    public boolean count(long count) {
        this.count += count;
        if(uploadProgress != null && max != 0){
            uploadProgress.onProgress(String.valueOf(this.count * 100/max));
        }
        return true;
    }

    @Override
    public void end() {
        if(uploadProgress != null){
            uploadProgress.onComplete();
        }
    }
}
