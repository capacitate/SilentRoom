package kaist.iop.ksh.silentroom;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by KSH on 2015-12-01.
 */
public class DummyActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, SRService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        finish();
    }
}
