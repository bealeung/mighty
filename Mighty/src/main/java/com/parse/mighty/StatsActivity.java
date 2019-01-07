package com.parse.mighty;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class StatsActivity extends AppCompatActivity {
    View bottomNavLayout;





    public void setNav(String type) {

        ImageView logImageView = (ImageView) bottomNavLayout.findViewById(R.id.bottomNavLayout).findViewById(R.id.logLayout).findViewById(R.id.logImageView);
        ImageView statsImageView = (ImageView) bottomNavLayout.findViewById(R.id.bottomNavLayout).findViewById(R.id.statsLayout).findViewById(R.id.statsImageView);
        ImageView programsImageView = (ImageView) bottomNavLayout.findViewById(R.id.bottomNavLayout).findViewById(R.id.programsLayout).findViewById(R.id.programsImageView);
        ImageView userImageView = (ImageView) bottomNavLayout.findViewById(R.id.bottomNavLayout).findViewById(R.id.userLayout).findViewById(R.id.userImageView);

        TextView logTextView = (TextView) bottomNavLayout.findViewById(R.id.bottomNavLayout).findViewById(R.id.logLayout).findViewById(R.id.logTextView);
        TextView statsTextView = (TextView) bottomNavLayout.findViewById(R.id.bottomNavLayout).findViewById(R.id.statsLayout).findViewById(R.id.statsTextView);
        TextView programsTextView = (TextView) bottomNavLayout.findViewById(R.id.bottomNavLayout).findViewById(R.id.programsLayout).findViewById(R.id.programsTextView);
        TextView userTextView = (TextView) bottomNavLayout.findViewById(R.id.bottomNavLayout).findViewById(R.id.userLayout).findViewById(R.id.userTextView);


        logImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        statsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StatsActivity.class);
                startActivity(intent);
            }
        });

        logImageView.setAlpha((float)0.4);
        statsImageView.setAlpha((float)0.4);
        programsImageView.setAlpha((float)0.4);
        userImageView.setAlpha((float)0.4);

        logTextView.setVisibility(View.GONE);
        statsTextView.setVisibility(View.GONE);
        programsTextView.setVisibility(View.GONE);
        userTextView.setVisibility(View.GONE);

        switch (type) {
            case "log":
                logImageView.setAlpha((float) 1);
                logTextView.setVisibility(View.VISIBLE);
                logImageView.setOnClickListener(null);
                return;
            case "stats":
                statsImageView.setAlpha((float) 1);
                statsTextView.setVisibility(View.VISIBLE);
                statsImageView.setOnClickListener(null);
                return;
            default:

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        bottomNavLayout = findViewById(R.id.bottomNavLayout);
        setNav("stats");
    }
}
