package kaist.iop.ksh.silentroom;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by KSH on 2015-12-01.
 */
public class DummyAudioActivity extends Activity{

    private static final String TAG = DummyAudioActivity.class.getSimpleName();
    private AudioManager audioManager;
    private DeviceStatus deviceStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "The DummyAudioActivity is created");

        //audio manager instance 생성
        audioManager = (AudioManager)this.getSystemService(this.AUDIO_SERVICE);
        deviceStatus = new DeviceStatus();

        Intent audioIntent = this.getIntent();
        String mode = audioIntent.getStringExtra("mode");

        //현재 mode를 얻어올 경우
        if(mode.equalsIgnoreCase("getMode"))
        {
            if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
            {
                Log.i(TAG, "the current mode is slient");
                Bundle bundle = new Bundle();
                bundle.putString("contents","getMode");
                bundle.putInt("getMode", deviceStatus.SILENTMODE);
                Intent serviceIntent = new Intent(this,SRService.class);
                serviceIntent.putExtras(bundle);
                startService(serviceIntent);
                //PendingIntent.getService(this,0,serviceIntent,0);
                finish();
            }
            else if(audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)
            {
                Log.i(TAG, "the current mode is vibrate");
                Bundle bundle = new Bundle();
                bundle.putString("contents","getMode");
                bundle.putInt("getMode", deviceStatus.VIBRATIONMODE);
                Intent serviceIntent = new Intent(this,SRService.class);
                serviceIntent.putExtras(bundle);
                startService(serviceIntent);
                //PendingIntent.getService(this, 0, serviceIntent, 0);
                finish();
            }
            else
            {
                Log.i(TAG, "the current mode is normal");
                Bundle bundle = new Bundle();
                bundle.putString("contents", "getMode");
                bundle.putInt("getMode", deviceStatus.NORMALMODE);
                Intent serviceIntent = new Intent(this,SRService.class);
                serviceIntent.putExtras(bundle);
                startService(serviceIntent);
                //PendingIntent.getService(this, 0, serviceIntent, 0);
                finish();
            }

        }
        //volume을 얻어올 경우
        else if(mode.equalsIgnoreCase("getVolume"))
        {

            finish();
        }
        //silent 모드로 전환할 경우
        else if(mode.equalsIgnoreCase("changeToSilent"))
        {
            Log.i(TAG, "the audio mode is changed to silent: DummyAudioActivity");
            this.audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            finish();
        }
        //vibration 모드로 전환할 경우
        else if(mode.equals("changeToVibrate"))
        {
            Log.i(TAG, "the audio mode is changed to vibrate");
            this.audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
            finish();
        }
        //normal 모드로 전환할 경우
        else
        {
            Log.i(TAG, "the audio mode is changed to normal");
            this.audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            finish();
        }
    }

    //ringtone volume을 return 받는다.
    //Media의 volume을 얻으려면 STREAM_RING 대신 STREAM_MUSIC 사용
    public int getVolumeLevel()
    {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        Log.d(TAG, "get volume level:"+currentVolume);
        return currentVolume;
    }
}
