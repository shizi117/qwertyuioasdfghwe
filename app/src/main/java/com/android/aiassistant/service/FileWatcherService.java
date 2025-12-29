package com.android.aiassistant.service;

import android.content.Context;
import android.os.FileObserver;
import android.util.Log;

import java.io.File;

public class FileWatcherService extends FileObserver {
    private static final String TAG = "FileWatcherService";
    private String watchPath;
    private OnFileChangeListener listener;

    public interface OnFileChangeListener {
        void onFileChanged(String path);
    }

    public FileWatcherService(Context context) {
        super("", FileObserver.ALL_EVENTS);
    }

    public void startWatching(String path, OnFileChangeListener listener) {
        this.watchPath = path;
        this.listener = listener;
        super.startWatching();
    }

    public void stopWatching() {
        super.stopWatching();
    }

    @Override
    public void onEvent(int event, String path) {
        if (listener != null && path != null) {
            listener.onFileChanged(watchPath + File.separator + path);
        }
    }
}
