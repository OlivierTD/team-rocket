package de.danoeh.antennapod.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.annotation.Nullable;

import de.danoeh.antennapod.R;

/**
 * Created by David on 2018-03-07.
 *
 * Empty activity container called during unit tests
 */

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
    }
}
