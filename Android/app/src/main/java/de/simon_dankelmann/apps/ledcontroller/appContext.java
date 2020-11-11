package de.simon_dankelmann.apps.ledcontroller;

import android.app.Application;
import android.content.Context;

/**
 * Created by simon on 20.11.16.
 */

public class appContext extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        appContext.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return appContext.context;
    }
}
