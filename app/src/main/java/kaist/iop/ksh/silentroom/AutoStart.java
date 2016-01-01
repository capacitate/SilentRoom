package kaist.iop.ksh.silentroom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by KSH on 2015-11-19.
 */
public class AutoStart extends BroadcastReceiver{

    public void onReceive(Context context, Intent intent)
    {
        Intent startServiceIntent = new Intent(context, SRService.class);
        context.startService(startServiceIntent);
    }

}
