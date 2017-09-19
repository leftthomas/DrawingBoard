package com.left.drawingboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.left.drawingboard.fragment.SketchFragment;

public class MainActivity extends AppCompatActivity {
    private final static String FRAGMENT_TAG = "SketchFragmentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.add(R.id.fl_main, new SketchFragment(),FRAGMENT_TAG).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SketchFragment f = (SketchFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        f.onActivityResult(requestCode, resultCode, data);

    }
}
