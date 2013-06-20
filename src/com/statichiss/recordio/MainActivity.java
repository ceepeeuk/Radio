package com.statichiss.recordio;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by chris on 20/06/2013.
 */
public class MainActivity extends Activity {
    // Fragment TabHost as mTabHost
    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        mTabHost.addTab(mTabHost.newTabSpec("battery").setIndicator("Battery",
                getResources().getDrawable(R.drawable.ic_battery_tab)),
                BatteryFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("network").setIndicator("Network",
                getResources().getDrawable(R.drawable.ic_network_tab)),
                NetworkFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("device").setIndicator("Device",
                getResources().getDrawable(R.drawable.ic_device_tab)),
                DeviceFragment.class, null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}