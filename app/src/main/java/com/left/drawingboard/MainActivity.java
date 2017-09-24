package com.left.drawingboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

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

    // 添加右上角的actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 这里是调用menu文件夹中的main.xml，在主界面label右上角的三点里显示其他功能
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        SketchFragment f = (SketchFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        return f.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SketchFragment f = (SketchFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
        f.onActivityResult(requestCode, resultCode, data);
    }
}
