package com.iteye.weimingtom.hbksuger;

import android.app.Activity;
import android.os.Bundle;

public class HBKSugarActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        new WebDowner().start();
    }
}
