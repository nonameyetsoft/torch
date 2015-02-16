package nonameyetsoft.com.torch;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(Flashlight.LOG_TAG, "Widget tapped.");

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.neon_widget);
        Intent serviceIntent = new Intent(context, FlashlightService.class);
        Flashlight.isWidgetContext = true;

        if (Flashlight.isOn()) {
            Log.i(Flashlight.LOG_TAG, "turn off code.");
            views.setImageViewResource(R.id.NeonWidget, R.drawable.button_widget_on);
            context.stopService(serviceIntent);
            Flashlight.setInUseByWidget(false);
            Flashlight.setIsOn(false);
        } else {
            Log.i(Flashlight.LOG_TAG, "turn on code.");
            if (isCameraUsebyApp() && !Flashlight.isOn()) {
                Toast.makeText(context, "Camera is in use by another app, " +
                                "you cannot use torch till that app is running.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (!FlashlightService.isRunning()) {
                Log.i(Flashlight.LOG_TAG, "Starting service from the widget");
                serviceIntent.putExtra("command", "turnOn");
                context.startService(serviceIntent);
            }
            views.setImageViewResource(R.id.NeonWidget, R.drawable.button_widget_off);
            Flashlight.setInUseByWidget(true);
            Flashlight.setIsOn(true);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(new ComponentName(context, WidgetProvider.class),
                views);
    }

    public boolean isCameraUsebyApp() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            return true;
        } finally {
            if (camera != null) camera.release();
        }
        return false;
    }
}