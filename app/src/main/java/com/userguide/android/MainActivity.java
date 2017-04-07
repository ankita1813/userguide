package com.userguide.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.userguide.android.lib.widget.UserGuideView;

public class MainActivity extends AppCompatActivity {

    private TextView mClickMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClickMe = (TextView) findViewById(R.id.click_me);

    }

    private void showUserGuideView() {
        new UserGuideView.Builder(this)
                .setTarget(mClickMe)
                .setTitleText("Save to watch offline")
                .setDismissText("GOT IT")
                .setDiscriptionText("Watch your saved videos even when you aren't connected.")
                .show();
    }

    void clickMe(View view){
        switch (view.getId()){
            case R.id.click_me:
                showUserGuideView();
                break;

        }
    }
}
