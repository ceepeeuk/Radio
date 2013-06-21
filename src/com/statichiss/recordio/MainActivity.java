package com.statichiss.recordio;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;

import com.statichiss.R;
import com.statichiss.recordio.fragments.PlayerFragment;
import com.statichiss.recordio.fragments.RecordingsFragment;
import com.statichiss.recordio.fragments.ScheduleFragment;

/**
 * Created by chris on 20/06/2013.
 */
public class MainActivity extends FragmentActivity {
    // Fragment TabHost as mTabHost
    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

//        mTabHost.addTab(mTabHost.newTabSpec("battery").setIndicator("Battery",
//                getResources().getDrawable(R.drawable.ic_battery_tab)),
//                BatteryFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec("player").setIndicator("Player"), PlayerFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("schedule").setIndicator("Schedule"), ScheduleFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("recordings").setIndicator("Recordings"), RecordingsFragment.class, null);

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
}