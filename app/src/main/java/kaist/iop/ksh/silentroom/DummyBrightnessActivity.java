package kaist.iop.ksh.silentroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by KSH on 2015-12-01.
 */
public class DummyBrightnessActivity extends Activity{

    private static final String TAG = DummyBrightnessActivity.class.getSimpleName();
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"The DummyBrightnessActivity is created");
        this.window = getWindow();

        Intent brIntent = this.getIntent();
        String mode = brIntent.getStringExtra("mode");

        if(mode.equalsIgnoreCase("changeToDim"))
        {
            //Brightness 줄임
            Log.i(TAG, "the screen is dimmed: DummyBrightnessActivity");
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 10);
            WindowManager.LayoutParams layoutParams = this.window.getAttributes();
            layoutParams.screenBrightness = (float) 10 / (float) 255;
            this.window.setAttributes(layoutParams);
            finish();
        }
        else if(mode.equalsIgnoreCase("changeBrightness"))
        {
            Log.i(TAG, "the brightness of screen is recovered");
            float brightness = brIntent.getFloatExtra("brightness",50);
            Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, (int)brightness);
            WindowManager.LayoutParams layoutParams = this.window.getAttributes();
            layoutParams.screenBrightness = brightness / (float) 255;
            this.window.setAttributes(layoutParams);
            finish();
        }
    }
}
