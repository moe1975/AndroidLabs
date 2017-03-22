package com.example.moe.androidlabs;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class MessageDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        Bundle data = this.getIntent().getExtras();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        MessageFragment f = new MessageFragment();
        f.setArguments(data);

        ft.replace(R.id.emptyFrame, f);
        ft.commit();
    }


}
