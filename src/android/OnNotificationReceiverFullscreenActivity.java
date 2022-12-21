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
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.media.MediaPlayer;

public class OnNotificationReceiverFullscreenActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(FirebasePlugin.TAG, "OnNotificationReceiverFullscreenActivity.onCreate() Start!");
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

        MediaPlayer mediaPlayer = MediaPlayer.create(this, getResources().getIdentifier("ding", "raw", getPackageName()));
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        Activity thisActivity = this;
        Intent thisIntent = getIntent();

        Log.d(FirebasePlugin.TAG, "OnNotificationReceiverFullscreenActivity.onCreate()");
        setContentView(getResources().getIdentifier("activity_fullscreen", "layout", getPackageName()));

        Button clickButton = (Button) findViewById(getResources().getIdentifier("notification_button", "id", getPackageName()));
        clickButton.setOnClickListener( new OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();

                handleNotification(thisActivity, thisIntent);
            }
        });

        // With this uncommented, it brings the activity up, but haphazardly
        //handleNotification(this, getIntent());

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
