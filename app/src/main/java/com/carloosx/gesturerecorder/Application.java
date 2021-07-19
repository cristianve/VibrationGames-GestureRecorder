package com.carloosx.gesturerecorder;


import android.os.Environment;
import android.preference.PreferenceManager;

import com.carloosx.gesturerecorder.settings.AppSettings;

import java.io.File;


public class Application extends android.app.Application {
    private AppSettings settings;

    public AppSettings getSettings()
    {
        return settings;
    }

    @Override
    public void onCreate() {
        settings = new AppSettings(PreferenceManager.getDefaultSharedPreferences(this));
        settings.load();

        if (settings.getWorkingDir() == null) {
            File extDir = Environment.getExternalStorageDirectory();
            if (extDir != null) {
                settings.setWorkingDir(extDir.getAbsolutePath());
                settings.saveDeferred();
            }
        }

        super.onCreate();
    }
}
