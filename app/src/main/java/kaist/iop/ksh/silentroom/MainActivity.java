package kaist.iop.ksh.silentroom;

import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import java.util.Collection;

public class MainActivity extends AppCompatActivity implements IBeaconConsumer{
    private static final String TAG = MainActivity.class.getSimpleName();
    private IBeaconManager iBeaconManager;
    private DeviceStatusManager deviceStatusManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        iBeaconManager = IBeaconManager.getInstanceForApplication(this);
//        deviceStatusManager = new DeviceStatusManager(this);
//
//        Log.d(TAG, "BT is on");
//        deviceStatusManager.setBluetoothOn();
//
//        iBeaconManager.bind(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iBeaconManager.unBind(this);
    }

    @Override
    public void onIBeaconServiceConnect() {
        iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                //범위안에 들어왔을 경우
                Log.d(TAG, "save data");
                deviceStatusManager.saveDeviceStatus();
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

        try{
            iBeaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId",null,null,null));
        } catch(RemoteException e){}

        iBeaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0) {
                    Log.i(TAG, "The first iBeacon I see is about " + iBeacons.iterator().next().getAccuracy() + " meters away.");
                }
            }
        });

        try{
            iBeaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        }catch(RemoteException e){}
    }
}
