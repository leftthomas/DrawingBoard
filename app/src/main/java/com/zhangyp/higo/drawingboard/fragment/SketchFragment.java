/*
 * Copyright (C) 2015 Federico Iosue (federico.iosue@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.zhangyp.higo.drawingboard.fragment;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.yancy.imageselector.ImageConfig;
import com.yancy.imageselector.ImageLoader;
import com.yancy.imageselector.ImageSelector;
import com.yancy.imageselector.ImageSelectorActivity;
import com.zhangyp.higo.drawingboard.R;
import com.zhangyp.higo.drawingboard.view.SketchView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;


public class SketchFragment extends Fragment implements SketchView.OnDrawChangedListener {

    @Bind(R.id.sketch_stroke)
    ImageView stroke;
    @Bind(R.id.sketch_eraser)
    ImageView eraser;
    @Bind(R.id.drawing)
    SketchView mSketchView;
    @Bind(R.id.sketch_undo)
    ImageView undo;
    @Bind(R.id.sketch_redo)
    ImageView redo;
    @Bind(R.id.sketch_erase)
    ImageView erase;
    @Bind(R.id.sketch_save)
    ImageView sketchSave;
    @Bind(R.id.sketch_photo)
    ImageView sketchPhoto;
    @Bind(R.id.eraserView)
    LinearLayout eraserView;
    @Bind(R.id.iv_bg)
    ImageView ivBg;
    @Bind(R.id.drawing_question)
    LinearLayout drawingQuestion;
    @Bind(R.id.iv_bg_color)
    ImageView ivBgColor;
    @Bind(R.id.bt_show_bg)
    Button btShowBg;
    @Bind(R.id.bt_show_bg_gray)
    Button btShowBgGray;


    private int seekBarStrokeProgress, seekBarEraserProgress;
    private View popupLayout, popupEraserLayout;
    private ImageView strokeImageView, eraserImageView;
    private int size;
    private ColorPicker mColorPicker;
    private int oldColor;
    private MaterialDialog dialog;
    private Bitmap bitmap;
    private int mScreenWidth;
    private int mScreenHeight;
    private float density;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setHasOptionsMenu(true);
        setRetainInstance(false);
    }


    @Override
    public void onStart() {

        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sketch, container, false);
        ButterKnife.bind(this, view);

        density = getResources().getDisplayMetrics().density;

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        // 获取屏幕分辨率宽度
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) mSketchView.getLayoutParams();
        p.width = mScreenWidth;
        p.height = mScreenWidth;
        mSketchView.setLayoutParams(p);


        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        mSketchView.setOnDrawChangedListener(this);


        stroke.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mSketchView.getMode() == SketchView.STROKE) {
                    showPopup(v, SketchView.STROKE);
                } else {
                    mSketchView.setMode(SketchView.STROKE);
                    setAlpha(eraser, 0.4f);
                    setAlpha(stroke, 1f);
                }
            }
        });

        setAlpha(eraser, 0.4f);
        eraser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSketchView.getMode() == SketchView.ERASER) {
                    showPopup(v, SketchView.ERASER);
                } else {
                    mSketchView.setMode(SketchView.ERASER);
                    setAlpha(stroke, 0.4f);
                    setAlpha(eraser, 1f);
                }
            }
        });

        undo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSketchView.undo();
            }
        });

        redo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSketchView.redo();
            }
        });

        erase.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                askForErase();
            }

            private void askForErase() {
                new MaterialDialog.Builder(getActivity())
                        .content("擦除手绘")
                        .positiveText("确认")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                mSketchView.erase();
                            }
                        })
                        .build().show();
            }
        });

        sketchSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSketchView.getPaths().size() == 0) {
                    Toast.makeText(getActivity(), "你还没有手绘", Toast.LENGTH_SHORT).show();
                    return;
                }
                //保存
                new MaterialDialog.Builder(getActivity())
                        .title("保存")
                        .content("")
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input("手绘名称(.png)", "a.png", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                // Do something
                                Log.i("AAA", input.toString());

                                save(input.toString());
                            }
                        }).show();


            }
        });
        sketchPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //选择图片
                ImageConfig imageConfig
                        = new ImageConfig.Builder(new ImageLoader() {
                    @Override
                    public void displayImage(Context context, String path, ImageView imageView) {
                        Glide.with(context)
                                .load(path)
                                .placeholder(com.yancy.imageselector.R.mipmap.imageselector_photo)
                                .centerCrop()
                                .into(imageView);
                    }
                })
                        .steepToolBarColor(getResources().getColor(R.color.blue))
                        .titleBgColor(getResources().getColor(R.color.blue))
                        .titleSubmitTextColor(getResources().getColor(R.color.white))
                        .titleTextColor(getResources().getColor(R.color.white))
                        //截屏
//                        .crop()
                        // 开启单选   （默认为多选）
                        .singleSelect()
                        // 开启拍照功能 （默认关闭）
                        .showCamera()
                        // 拍照后存放的图片路径（默认 /temp/picture） （会自动创建）
                        .filePath("/DrawingBoard/Pictures")
                        .build();


                ImageSelector.open(getActivity(), imageConfig);   // 开启图片选择器
            }
        });


        // Inflate the popup_layout.xml
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(ActionBarActivity
                .LAYOUT_INFLATER_SERVICE);
        popupLayout = inflater.inflate(R.layout.popup_sketch_stroke, null);
        // And the one for eraser
        LayoutInflater inflaterEraser = (LayoutInflater) getActivity().getSystemService(ActionBarActivity
                .LAYOUT_INFLATER_SERVICE);
        popupEraserLayout = inflaterEraser.inflate(R.layout.popup_sketch_eraser, null);

        // Actual stroke shape size is retrieved
        strokeImageView = (ImageView) popupLayout.findViewById(R.id.stroke_circle);
        final Drawable circleDrawable = getResources().getDrawable(R.drawable.circle);
        size = circleDrawable.getIntrinsicWidth();
        // Actual eraser shape size is retrieved
        eraserImageView = (ImageView) popupEraserLayout.findViewById(R.id.stroke_circle);
        size = circleDrawable.getIntrinsicWidth();

        setSeekbarProgress(SketchView.DEFAULT_STROKE_SIZE, SketchView.STROKE);
        setSeekbarProgress(SketchView.DEFAULT_ERASER_SIZE, SketchView.ERASER);

        // Stroke color picker initialization and event managing
        mColorPicker = (ColorPicker) popupLayout.findViewById(R.id.stroke_color_picker);
        mColorPicker.addSVBar((SVBar) popupLayout.findViewById(R.id.svbar));
        mColorPicker.addOpacityBar((OpacityBar) popupLayout.findViewById(R.id.opacitybar));

        mColorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                mSketchView.setStrokeColor(color);
            }
        });
        mColorPicker.setColor(mSketchView.getStrokeColor());
        mColorPicker.setOldCenterColor(mSketchView.getStrokeColor());
    }

    void setAlpha(View v, float alpha) {
        if (Build.VERSION.SDK_INT < 11) {
            AlphaAnimation animation = new AlphaAnimation(1.0F, alpha);
            animation.setFillAfter(true);
            v.startAnimation(animation);
        } else {
            v.setAlpha(alpha);
        }

    }


    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void save(final String imgName) {
        dialog = new MaterialDialog.Builder(getActivity())
                .title("保存手绘")
                .content("保存中...")
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .show();
        bitmap = mSketchView.getBitmap();

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                if (bitmap != null) {
                    String str = System.currentTimeMillis() + "";

                    try {
                        String filePath = "/mnt/sdcard/DrawingBoard/";
                        File dir = new File(filePath);
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        File f = new File(filePath, imgName);
                        if (!f.exists()) {
                            f.createNewFile();
                        }
                        FileOutputStream out = new FileOutputStream(f);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                        out.close();

                        dialog.dismiss();
                        return "保存手绘成功" + filePath;
                    } catch (Exception e) {

                        dialog.dismiss();
                        Log.i("AAA", e.getMessage());
                        return "保存手绘失败" + e.getMessage();
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);

                Toast.makeText(getActivity(), (String) o, Toast.LENGTH_SHORT).show();

            }
        }.execute("");


    }


    // The method that displays the popup.
    private void showPopup(View anchor, final int eraserOrStroke) {

        boolean isErasing = eraserOrStroke == SketchView.ERASER;

        oldColor = mColorPicker.getColor();

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Creating the PopupWindow
        PopupWindow popup = new PopupWindow(getActivity());
        popup.setContentView(isErasing ? popupEraserLayout : popupLayout);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {


                if (mColorPicker.getColor() != oldColor)
                    mColorPicker.setOldCenterColor(oldColor);
            }
        });

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets (transformed 
        // dp to pixel to support multiple screen sizes)
        popup.showAsDropDown(anchor);

        // Stroke size seekbar initialization and event managing
        SeekBar mSeekBar;
        mSeekBar = (SeekBar) (isErasing ? popupEraserLayout
                .findViewById(R.id.stroke_seekbar) : popupLayout
                .findViewById(R.id.stroke_seekbar));
        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }


            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // When the seekbar is moved a new size is calculated and the new shape
                // is positioned centrally into the ImageView
                setSeekbarProgress(progress, eraserOrStroke);
            }
        });
        int progress = isErasing ? seekBarEraserProgress : seekBarStrokeProgress;
        mSeekBar.setProgress(progress);
    }


    protected void setSeekbarProgress(int progress, int eraserOrStroke) {
        int calcProgress = progress > 1 ? progress : 1;

        int newSize = Math.round((size / 100f) * calcProgress);
        int offset = Math.round((size - newSize) / 2);


        LayoutParams lp = new LayoutParams(newSize, newSize);
        lp.setMargins(offset, offset, offset, offset);
        if (eraserOrStroke == SketchView.STROKE) {
            strokeImageView.setLayoutParams(lp);
            seekBarStrokeProgress = progress;
        } else {
            eraserImageView.setLayoutParams(lp);
            seekBarEraserProgress = progress;
        }

        mSketchView.setSize(newSize, eraserOrStroke);
    }


    @Override
    public void onDrawChanged() {
        // Undo
        if (mSketchView.getPaths().size() > 0)
            setAlpha(undo, 1f);
        else
            setAlpha(undo, 0.4f);
        // Redo
        if (mSketchView.getUndoneCount() > 0)
            setAlpha(redo, 1f);
        else
            setAlpha(redo, 0.4f);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ImageSelector.IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            // Get Image Path List
            List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);

            for (String path : pathList) {
                Log.i("ImagePathList", path);
                Glide.with(this).load(path)
                        .asBitmap()
                        .centerCrop()
                        .into(new SimpleTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                // Do something with bitmap here.

                                init(resource);

                            }

                        });
            }
        }

    }

    private void init(Bitmap bitmap) {

        float scaleRatio = 1;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();


        float screenRatio = 1.0f;
        float imgRatio = (float) height / (float) width;
        if (imgRatio >= screenRatio) {
            //高度大于屏幕，以高为准

            scaleRatio = (float) mScreenWidth / (float) height;

        }

        if (imgRatio < screenRatio) {
            scaleRatio = (float) mScreenWidth / (float) width;

        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleRatio, scaleRatio);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);

        GPUImage gpuImage = new GPUImage(getActivity());
        gpuImage.setFilter(new GPUImageSketchFilter());
        final Bitmap grayBmp = gpuImage.getBitmapWithFilterApplied(dstbmp);

        ivBg.setImageBitmap(grayBmp);
        mSketchView.getBackground().setAlpha(150);

        ivBgColor.setImageBitmap(dstbmp);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(ivBgColor, "alpha", 1.0f, 0.0f);
        alpha.setDuration(2000).start();

        btShowBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator alpha = ObjectAnimator.ofFloat(ivBgColor, "alpha", 0.0f, 1.0f);
                alpha.setDuration(1000).start();

            }
        });
        btShowBgGray.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator alpha = ObjectAnimator.ofFloat(ivBgColor, "alpha", 1.0f, 0.0f);
                alpha.setDuration(1000).start();

            }
        });


    }

}