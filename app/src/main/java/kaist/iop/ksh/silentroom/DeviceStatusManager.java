package kaist.iop.ksh.silentroom;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by KSH on 2015-11-23.
 */
public class DeviceStatusManager{
    private static final String TAG = DeviceStatusManager.class.getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private DeviceStatus mDeviceStatus;
    private Service parentService;

    public boolean audioModeFlag;

    public DeviceStatusManager(Service service)
    {
        //parent의 context를 가져옴
        this.parentService = service;

        //BluetoothAdapter 인스턴스를 받아옴
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //device가 BT를 지원하지 않을 경우
        if (mBluetoothAdapter == null)
        {
            Log.i(TAG, "This devices doesn't support BT");
        }
        //Device의 상태를 저장할 구조체 생성
        this.mDeviceStatus = new DeviceStatus();

        //기본 오디오 모드를 설정
        this.audioModeFlag = true; //true일경우 기본적으로 silent 모드로 변경
    }

    public DeviceStatus getDeviceStatus()
    {
        return this.mDeviceStatus;
    }

    /*Bluetooth가 켜져 있는지 확인한다.
    *defaul로는 user에게 dialog를 띄우고 BT를 켜도록 한다.
    * 추후 자동으로 켜지는 방법 고려해야 함.
     */
    public boolean checkBluetoothIsOn()
    {
        //BT가 disabled 상태이면 false를 return
        if(mBluetoothAdapter.isEnabled())
        {
            return true;
        }

        return false;
    }

    public boolean setBluetoothOn()
    {
        //RESULT_OK = -1, RESULT_CANCELED = 0
        //Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //일단 여기에 activity를 만들지 않아서 함수 자체를 사용할 수 없음
        //startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);

        //위의 것은 가능하면 하는 것으로 하고, 일단 자동으로 바꿔주는 것으로 시도
        if(checkBluetoothIsOn())
        {
            //BT가 이미 켜져있을 경우
            Log.i(TAG, "BT is already enabled");
            return false;
        }
        else
        {
            //BT가 꺼져있을 경우 켠다.
            this.mBluetoothAdapter.enable();
            Log.i(TAG, "BT is enabled");
            return true;
        }
    }

    public void saveDeviceStatus()
    {
        getCurrentAudioMode();
        getCurrentVolume();
        this.mDeviceStatus.setBrightness(getBrightness());
        Log.i(TAG, "device status is saved");
    }

    //만약 자동 밝기 설정을 해놨다면? 그것도 처리해야하나..
    public float getBrightness()
    {
        try{
            float curBrightnessValue = android.provider.Settings.System.getInt(
                    this.parentService.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            Log.i(TAG, "get brightness:"+curBrightnessValue);
            return curBrightnessValue;
        } catch(Settings.SettingNotFoundException e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public void getCurrentAudioMode()
    {
        Intent audioIntent = new Intent(this.parentService,DummyAudioActivity.class);
        audioIntent.putExtra("mode", "getMode");
        audioIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        this.parentService.startActivity(audioIntent);
    }

    public void getCurrentVolume()
    {
        Intent audioIntent = new Intent(this.parentService,DummyAudioActivity.class);
        audioIntent.putExtra("mode", "getVolume");
        audioIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        this.parentService.startActivity(audioIntent);
    }

    //SilentRoom 모드로 변경한다.
    public void setSilentRoomMode()
    {
        //Brightness 줄임
        Intent brightnessIntent = new Intent(this.parentService,DummyBrightnessActivity.class);
        brightnessIntent.putExtra("mode", "changeToDim");
        brightnessIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        this.parentService.startActivity(brightnessIntent);

        //SilentRoom모드로 변경
        Intent audioIntent = new Intent(this.parentService,DummyAudioActivity.class);
        if(this.audioModeFlag)
        {
            audioIntent.putExtra("mode", "changeToSilent");
        }
        else
        {
            audioIntent.putExtra("mode","changeToVibrate");
        }
        audioIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        this.parentService.startActivity(audioIntent);

        Toast toast = Toast.makeText(this.parentService, "SilentRoom mode is turned on", Toast.LENGTH_SHORT);
        toast.show();

    }

    //SilentRoom 모드를 해제한다.
    public void releaseSilentRoomMode()
    {
        //Audio mode를 원상태로 복구한다.
        if(mDeviceStatus.getAudioMode()==mDeviceStatus.SILENTMODE)
        {
            Intent audioIntent = new Intent(this.parentService,DummyAudioActivity.class);
            audioIntent.putExtra("mode", "changeToSilent");
            audioIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            this.parentService.startActivity(audioIntent);
        }
        else if(mDeviceStatus.getAudioMode()==mDeviceStatus.VIBRATIONMODE)
        {
            Intent audioIntent = new Intent(this.parentService,DummyAudioActivity.class);
            audioIntent.putExtra("mode", "changeToVibrate");
            audioIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            this.parentService.startActivity(audioIntent);
        }
        else
        {
            Intent audioIntent = new Intent(this.parentService,DummyAudioActivity.class);
            audioIntent.putExtra("mode", "changeToNormal");
            audioIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            this.parentService.startActivity(audioIntent);
        }


        //Brightness 원래대로
        Intent brightnessIntent = new Intent(this.parentService,DummyBrightnessActivity.class);
        brightnessIntent.putExtra("mode", "changeBrightness");
        brightnessIntent.putExtra("brightness", mDeviceStatus.getBrightness());
        brightnessIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        this.parentService.startActivity(brightnessIntent);

        Toast toast = Toast.makeText(this.parentService, "SilentRoom mode is turned off", Toast.LENGTH_SHORT);
        toast.show();
    }

}
