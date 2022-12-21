package org.apache.cordova.firebase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

public class OnNotificationReceiverFullscreenActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);

            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }

        Log.d(FirebasePlugin.TAG, "OnNotificationReceiverActivity.onCreate()");
        setContentView(getResources().getIdentifier("activity_fullscreen", "layout", getPackageName()));
        //setContentView(R.layout.activity_fullscreen);
        handleNotification(this, getIntent());

        // finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(FirebasePlugin.TAG, "OnNotificationReceiverActivity.onNewIntent()");
        handleNotification(this, intent);

        // finish();
    }

    private static void handleNotification(Context context, Intent intent) {
        try{
            PackageManager pm = context.getPackageManager();

            Intent launchIntent = pm.getLaunchIntentForPackage(context.getPackageName());
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            Bundle data = intent.getExtras();
            if(!data.containsKey("messageType")) data.putString("messageType", "notification");
            data.putString("tap", FirebasePlugin.inBackground() ? "background" : "foreground");

            Log.d(FirebasePlugin.TAG, "OnNotificationReceiverActivity.handleNotification(): "+data.toString());

            FirebasePlugin.sendMessage(data, context);

            launchIntent.putExtras(data);
            context.startActivity(launchIntent);
        }catch (Exception e){
            FirebasePlugin.handleExceptionWithoutContext(e);
        }
    }
}
