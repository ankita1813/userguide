package com.userguide.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.userguide.android.lib.widget.UserGuideView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mClickMe;
    UserGuideView.Builder mUserGuide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClickMe = (TextView) findViewById(R.id.click_me);
        mClickMe.setOnClickListener(this);
        mUserGuide =  new UserGuideView.Builder(this).setTarget(mClickMe)
                .setTitleText("Save to watch offline")
                .setDismissText("GOT IT")
                .setDiscriptionText("Watch your saved videos even when you aren't connected.");
    }

    private void showUserGuideView() {
        mUserGuide.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.click_me:
                showUserGuideView();
                break;

        }
    }
}
