package kaist.iop.ksh.silentroom;

import android.util.Log;

/**
 * Created by KSH on 2015-11-26.
 */
public class DeviceStatus {

    private static final String TAG = DeviceStatus.class.getSimpleName();

    //Device의 상태를 저장하는 데 필요한 변수들 필요시 추가
    private int audioMode;
    private int volume;
    private float brightness;

    public int SILENTMODE       =   00;
    public int VIBRATIONMODE    =   01;
    public int NORMALMODE       =   02;

    public void setAudioMode(int mode)
    {
        this.audioMode = mode;
        Log.i(TAG, "audio mode is saved in device status");
    }

    public void setVolume(int vol)
    {
        this.volume = vol;
    }

    public void setBrightness(float br)
    {
        this.brightness = br;
    }

    public int getAudioMode(){
        return this.audioMode;
    }

    public int getVolume()
    {
        return this.volume;
    }

    public float getBrightness()
    {
        return this.brightness;
    }
}
