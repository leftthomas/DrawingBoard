package com.zhangyp.higo.drawingboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.zhangyp.higo.drawingboard.fragment.SketchFragment;

public class MainActivity extends AppCompatActivity {
    private final static String FRAGMENT_TAG = "SketchFragmentTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        ft.add(R.id.fl_main, new SketchFragment(),FRAGMENT_TAG).commit();

//        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
//        int width = wm.getDefaultDisplay().getWidth();
//        int height = wm.getDefaultDisplay().getHeight();
//
//        WindowManager wm1 = this.getWindowManager();
//        int width1 = wm1.getDefaultDisplay().getWidth();
//        int height1 = wm1.getDefaultDisplay().getHeight();


//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
//        int mScreenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
//        int mScreenHeight = dm.heightPixels;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        SketchFragment f = (SketchFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        f.onActivityResult(requestCode, resultCode, data);

    }
}
