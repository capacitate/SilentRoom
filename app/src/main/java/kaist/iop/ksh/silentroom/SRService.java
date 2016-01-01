package kaist.iop.ksh.silentroom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;

import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import java.util.Collection;

/**
 * Created by KSH on 2015-11-19.
 */
public class SRService extends Service implements IBeaconConsumer {

    private static final String TAG = SRService.class.getSimpleName();
    private DeviceStatusManager deviceStatusManager;
    private IBeaconManager iBeaconManager;
    private AdManager adManager;
    private boolean firstEncounterFlag;

    private String AD_URL;

//    notify
    private NotificationManager mNotificationManager;
    private final String SCAN = "SCAN";
    private final String STOP = "STOP";
    private final String CHANGE = "CHANGE";
    private final String AD = "AD";
    private final String KEY = "contents";
    private int requestCode = 0;
    private final int NOTI_ID=111;


    @Override
    public void onCreate() {
        super.onCreate();
        this.adManager = new AdManager();
        this.deviceStatusManager = new DeviceStatusManager(this);
        Log.d(TAG, "check the BT is on: SRService");
        deviceStatusManager.setBluetoothOn();
        this.firstEncounterFlag = false;
        this.AD_URL = "";

        SystemClock.sleep(5000);
        iBeaconManager = IBeaconManager.getInstanceForApplication(this);
        iBeaconManager.bind(this);
        makeNotification(R.layout.custom_notification, this.deviceStatusManager.audioModeFlag, this.adManager.ad_flag);
    }

    @Override
    //다른 구성요소가 service를 시작할 때 호출 됨
    //한번 시작하면 명시적으로 stop할때까지 무기한으로 동작
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(TAG, "onStartCommand executed");
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                switch (bundle.getString(KEY)) {
                    case ("getMode"): {
                        this.deviceStatusManager.getDeviceStatus().setAudioMode(bundle.getInt("getMode"));
                        Log.i(TAG, "getMode from activity to service:" + bundle.getInt("getMode"));
                        break;
                    }
                    case (SCAN):{
                        Log.i(TAG, "onStartCommand SCAN");
                        makeNotification(R.layout.custom_notification, this.deviceStatusManager.audioModeFlag, this.adManager.ad_flag);
                        try {
                            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
                        } catch (RemoteException e) {}
                        break;
                    }
                    case (STOP):{
                        Log.i(TAG, "onStartCommand STOP");
                        try
                        {
                            iBeaconManager.stopRangingBeaconsInRegion(new Region("myRangingUniqueId",null,null,null));
                        } catch (RemoteException e) {
                        }
                        break;
                    }
                    case(CHANGE):{
                        Log.i(TAG, "onStartCommand CHANGE");
                        if(this.deviceStatusManager.audioModeFlag)
                        {
                            this.deviceStatusManager.audioModeFlag = false;
                            makeNotification(R.layout.custom_notification, this.deviceStatusManager.audioModeFlag, this.adManager.ad_flag);
                        }
                        else
                        {
                            this.deviceStatusManager.audioModeFlag = true;
                            makeNotification(R.layout.custom_notification, this.deviceStatusManager.audioModeFlag, this.adManager.ad_flag);
                        }
                        break;
                    }
                    case(AD):{
                        Log.i(TAG, "AD is clicked");

                        break;
                    }
                }
            }else{
                Log.d(TAG, "Bundle is null");
            }
        }else{
            Log.d(TAG, "Intent is null");
        }


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    //서비스를 소멸할 경우(마지막 호출)
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "SRService is destroyed");
        iBeaconManager.unBind(this);
    }

    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                //범위안에 들어왔을 경우
                Log.i(TAG, "save data");
                //deviceStatusManager.saveDeviceStatus();
//                Log.d(TAG, "set mode");
//                deviceStatusManager.setSilentRoomMode();
            }

            @Override
            public void didExitRegion(Region region) {
                //범위에서 벗어났을 경우
//                Log.d(TAG, "release the silent room mode");
//                deviceStatusManager.releaseSilentRoomMode();
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                //상태가 바뀔 경우
            }
        });

        try {
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
        }

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                //beacon 범위에 속할 경우
                if (iBeacons.size() > 0 && iBeacons.iterator().next().getAccuracy() < 3.0) {
                    //받은 beacon이 SilentRoom Beacon일 경우
                    Log.i(TAG, "UUID: "+iBeacons.iterator().next().getProximityUuid());
                    if (iBeacons.iterator().next().getProximityUuid().equalsIgnoreCase(adManager.beacon)|
                            iBeacons.iterator().next().getProximityUuid().equalsIgnoreCase(adManager.beacon_arduino)) {
                        Log.i(TAG, "SilentRoom beacon is received");
                        //처음 silent room beacon 범위에 속했을 경우
                        if (!firstEncounterFlag) {
                            //silent room mode로 변경한다
                            deviceStatusManager.saveDeviceStatus();
                            Log.i(TAG, "set silent mode : SRService");
                            deviceStatusManager.setSilentRoomMode();
                            firstEncounterFlag = true;
                        } else {
                            //처음 beacon을 받은게 아니라면 아무것도 하지 않는다.
                        }
                        adManager.ad_flag = false;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }
                    //받은 beacon이 ad beacon일 경우
                    else if (iBeacons.iterator().next().getProximityUuid().equalsIgnoreCase(adManager.ad1)) {
                        Log.i(TAG, "Advertisement 1 is occurred");
                        adManager.ad_flag = true;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }
                    else if(iBeacons.iterator().next().getProximityUuid().equalsIgnoreCase(adManager.ad_iphone))
                    {
                        AD_URL = adManager.IPHONE_URL;
                        adManager.ad_flag = true;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }
                    else if(iBeacons.iterator().next().getProximityUuid().equalsIgnoreCase(adManager.ad_kaistcs))
                    {
                        AD_URL = adManager.KAISTCS_URL;
                        adManager.ad_flag = true;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }
                    else if(iBeacons.iterator().next().getProximityUuid().equalsIgnoreCase(adManager.ad_ca))
                    {
                        AD_URL = adManager.CAPTAIN_AMERICA_URL;
                        adManager.ad_flag=true;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }
                    else if(iBeacons.iterator().next().getProximityUuid().equalsIgnoreCase(adManager.ad_proflee))
                    {
                        AD_URL = adManager.PROFSUNGJU_URL;
                        adManager.ad_flag = true;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }
                    else if(iBeacons.iterator().next().getProximityUuid().equalsIgnoreCase(adManager.ad_gglass))
                    {
                        AD_URL = adManager.GOOGLEGLASS_URL;
                        adManager.ad_flag = true;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }
                    //허용하는 beacon이 아닐 경우
                    else {
                        if (firstEncounterFlag) {
                            Log.i(TAG, "release the silent room mode");
                            deviceStatusManager.releaseSilentRoomMode();
                            firstEncounterFlag = false;
                        }
                        adManager.ad_flag = false;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }

                }
                //beacon 범위에 속하지 않을 경우
                else {
                    if (firstEncounterFlag) {
                        Log.i(TAG, "out of range -> recover device status");
                        deviceStatusManager.releaseSilentRoomMode();
                        firstEncounterFlag = false;
                        adManager.ad_flag = false;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }
                    else
                    {
                        Log.i(TAG, "no beacon signal received");
                        adManager.ad_flag = false;
                        makeNotification(R.layout.custom_notification, deviceStatusManager.audioModeFlag, adManager.ad_flag);
                    }
                }

            }
        });

        try {
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    public void makeNotification(int notification_layout, boolean mode_flag, boolean ad_flag) {
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) getSystemService(ns);

        RemoteViews contentView = new RemoteViews(getPackageName(), notification_layout);

        PendingIntent scanStartPendingIntent = makePendingIntent(SCAN);
        PendingIntent scanStopPendingIntent = makePendingIntent(STOP);
        PendingIntent changePendingIntent = makePendingIntent(CHANGE);
        PendingIntent AdPendingIntent = makePendingIntent(AD);

        if(mode_flag){
            contentView.setViewVisibility(R.id.silent_frame, View.INVISIBLE);
            contentView.setViewVisibility(R.id.vibrate_frame, View.VISIBLE);
        }else{
            contentView.setViewVisibility(R.id.vibrate_frame, View.INVISIBLE);
            contentView.setViewVisibility(R.id.silent_frame, View.VISIBLE);
        }

        if(ad_flag){
            contentView.setViewVisibility(R.id.advertisement_btn, View.VISIBLE);
        }else{
            contentView.setViewVisibility(R.id.advertisement_btn, View.INVISIBLE);
        }

        int icon = R.drawable.icon3;

        long when = System.currentTimeMillis();
        contentView.setTextViewText(R.id.current_time, convertDate(String.valueOf(when), "dd/MM/yyyy\nHH:mm:ss"));
        contentView.setOnClickPendingIntent(R.id.scan_start, scanStartPendingIntent);
        contentView.setOnClickPendingIntent(R.id.scan_stop, scanStopPendingIntent);
        contentView.setOnClickPendingIntent(R.id.silent_mode, changePendingIntent);
        contentView.setOnClickPendingIntent(R.id.vibrate_mode, changePendingIntent);
        contentView.setOnClickPendingIntent(R.id.advertisement_btn, AdPendingIntent);

        Notification builder = new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setTicker("Notification")
                .setContent(contentView)
                .setOngoing(true)
                .build();

        mNotificationManager.notify(NOTI_ID, builder);
    }
    public PendingIntent makePendingIntent(String mode){
        PendingIntent pendingIntent = null;
        switch(mode){
            case SCAN:case STOP:case CHANGE:{
                Intent intent = new Intent(this, SRService.class);
                Bundle bundle = new Bundle();
                bundle.putString(KEY, mode);
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                pendingIntent = PendingIntent.getService(this, requestCode++, intent, 0);
                break;
            }
            case AD:{
                Uri uri = Uri.parse(AD_URL);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                Bundle bundle = new Bundle();
                bundle.putString(KEY, mode);
                intent.putExtras(bundle);
                pendingIntent = PendingIntent.getActivity(this, requestCode++, intent, 0);
                break;
            }
            default:
                Log.i(TAG, "PendingIntent is null");
                break;
        }

        return pendingIntent;
    }

    public static String convertDate(String dateInMilliseconds, String dateFormat) {
        return DateFormat.format(dateFormat, Long.parseLong(dateInMilliseconds)).toString();
    }

}
