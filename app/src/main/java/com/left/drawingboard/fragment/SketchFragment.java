package com.left.drawingboard.fragment;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.left.drawingboard.R;
import com.left.drawingboard.SketchView;
import com.yancy.imageselector.ImageConfig;
import com.yancy.imageselector.ImageLoader;
import com.yancy.imageselector.ImageSelector;
import com.yancy.imageselector.ImageSelectorActivity;

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
    @Bind(R.id.iv_bg)
    ImageView ivBg;
    @Bind(R.id.iv_bg_color)
    ImageView ivBgColor;

    private int seekBarStrokeProgress, seekBarEraserProgress;
    private View popupStrokeLayout, popupEraserLayout;
    private ImageView strokeImageView, eraserImageView;
    //    调色板中黑色小圆球的size
    private int size;
    private ColorPicker mColorPicker;
    //    记录弹出调色板中ColorPicker的颜色，用以弹出时的颜色中心初始化
    private int oldColor;
    private MaterialDialog dialog;
    private Bitmap bitmap;
    private int mScreenWidth;
    private int mScreenHeight;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sketch, container, false);
        ButterKnife.bind(this, view);

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        // 获取屏幕分辨率宽度
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        // 给mSketchView设置宽高
        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) mSketchView.getLayoutParams();
        p.width = mScreenWidth;
        p.height = mScreenHeight;
        mSketchView.setLayoutParams(p);

        return view;
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
        // 默认初始化时橡皮擦为未选中状态
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
        // 擦除画图
        erase.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(getActivity()).content("擦除所有画图？").positiveText("确认")
                        .negativeText("取消").callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                mSketchView.erase();
                            }
                }).build().show();
            }
        });
        // 保存画图
        sketchSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSketchView.getPaths().size() == 0) {
                    Toast.makeText(getActivity(), "你还没有画图", Toast.LENGTH_SHORT).show();
                    return;
                }
                //保存
                new MaterialDialog.Builder(getActivity()).title("保存").negativeText("取消").inputType(InputType
                        .TYPE_CLASS_TEXT).input("画图名称(.png)", "a.png", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                if (input == null || input.length() == 0) {
                                    Toast.makeText(getActivity(), "请输入画图名称", Toast.LENGTH_SHORT).show();
                                } else if (input.length() <= 4 || !input.subSequence(input.length() - 4, input.length()).toString().equals(".png")) {
                                    Toast.makeText(getActivity(), "请输入正确的画图名称(.png)", Toast.LENGTH_SHORT).show();
                                } else
                                    save(input.toString());
                            }
                        }).show();
            }
        });
        sketchPhoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //选择图片
                ImageConfig imageConfig = new ImageConfig.Builder(new ImageLoader() {
                    @Override
                    public void displayImage(Context context, String path, ImageView imageView) {
                        Glide.with(context).load(path).placeholder(com.yancy.imageselector.R.mipmap.imageselector_photo).centerCrop().into(imageView);
                    }
                }).steepToolBarColor(getResources().getColor(R.color.colorPrimary)).titleBgColor(getResources().getColor(R.color.colorPrimary))
                        .titleSubmitTextColor(getResources().getColor(R.color.white)).titleTextColor(getResources().getColor(R.color.white))
                        // 开启单选   （默认为多选）
                        .singleSelect()
                        // 开启拍照功能 （默认关闭）
                        .showCamera()
                        // 拍照后存放的图片路径（默认 /temp/picture） （会自动创建）
                        .filePath("/DrawingBoard/Pictures")
                        .build();
                // 开启图片选择器
                ImageSelector.open(getActivity(), imageConfig);
            }
        });

        // Inflate布局文件
        LayoutInflater inflaterStroke = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        popupStrokeLayout = inflaterStroke.inflate(R.layout.popup_sketch_stroke, null);
        LayoutInflater inflaterEraser = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        popupEraserLayout = inflaterEraser.inflate(R.layout.popup_sketch_eraser, null);

        // 实例化stroke、eraser弹出调色板黑色小圆球控件
        strokeImageView = popupStrokeLayout.findViewById(R.id.stroke_circle);
        eraserImageView = popupEraserLayout.findViewById(R.id.stroke_circle);

        final Drawable circleDrawable = getResources().getDrawable(R.drawable.circle);
        size = circleDrawable.getIntrinsicWidth();

        setSeekBarProgress(SketchView.DEFAULT_STROKE_SIZE, SketchView.STROKE);
        setSeekBarProgress(SketchView.DEFAULT_ERASER_SIZE, SketchView.ERASER);

        // stroke color picker初始化
        mColorPicker = popupStrokeLayout.findViewById(R.id.stroke_color_picker);
        mColorPicker.addSVBar((SVBar) popupStrokeLayout.findViewById(R.id.sv_bar));
        mColorPicker.addOpacityBar((OpacityBar) popupStrokeLayout.findViewById(R.id.opacity_bar));

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
        v.setAlpha(alpha);
    }

    public void save(final String imgName) {
        dialog = new MaterialDialog.Builder(getActivity()).title("保存画图").content("保存中...").progress(true, 0).progressIndeterminateStyle(true).show();
        bitmap = mSketchView.getBitmap();

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {

                if (bitmap != null) {
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
                        return "保存手绘成功" + filePath + imgName;
                    } catch (Exception e) {

                        dialog.dismiss();
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

    // 显示弹出调色板
    private void showPopup(View anchor, final int eraserOrStroke) {

        boolean isErasing = eraserOrStroke == SketchView.ERASER;

        oldColor = mColorPicker.getColor();

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // 创建弹出调色板
        PopupWindow popup = new PopupWindow(getActivity());
        popup.setContentView(isErasing ? popupEraserLayout : popupStrokeLayout);
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

        // 清除默认的半透明背景
        popup.setBackgroundDrawable(new BitmapDrawable());

        popup.showAsDropDown(anchor);

        // seekbar初始化
        SeekBar mSeekBar;
        mSeekBar = (SeekBar) (isErasing ? popupEraserLayout
                .findViewById(R.id.stroke_seekbar) : popupStrokeLayout
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
                setSeekBarProgress(progress, eraserOrStroke);
            }
        });
        int progress = isErasing ? seekBarEraserProgress : seekBarStrokeProgress;
        mSeekBar.setProgress(progress);
    }

    protected void setSeekBarProgress(int progress, int eraserOrStroke) {
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

    //    设置redo、undo的显示状态
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
            // 获取Image Path List
            List<String> pathList = data.getStringArrayListExtra(ImageSelectorActivity.EXTRA_RESULT);
            for (String path : pathList) {
                Glide.with(this).load(path).asBitmap().centerCrop().into(new SimpleTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                                initBitmap(resource);
                            }
                        });
            }
        }
    }

    //    选择图片后初始化图片相关控件
    private void initBitmap(Bitmap bitmap) {

        float scaleRatio = 1;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float screenRatio = 1.0f;
        float imgRatio = (float) height / (float) width;
        if (imgRatio >= screenRatio) {
            //高度大于屏幕，以高为准
            scaleRatio = (float) mScreenHeight / (float) height;
        }

        if (imgRatio < screenRatio) {
            scaleRatio = (float) mScreenWidth / (float) width;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scaleRatio, scaleRatio);
        final Bitmap dstBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);

        GPUImage gpuImage = new GPUImage(getActivity());
//        这是手绘效果的filter
        gpuImage.setFilter(new GPUImageSketchFilter());
        final Bitmap grayBmp = gpuImage.getBitmapWithFilterApplied(dstBmp);

//        设置下透明度，不然原图会看不见
        mSketchView.getBackground().setAlpha(150);
        ivBg.setImageBitmap(grayBmp);
        ivBgColor.setImageBitmap(dstBmp);
//        默认初始时显示手绘效果
        ObjectAnimator alpha = ObjectAnimator.ofFloat(ivBgColor, "alpha", 1.0f, 0.0f);
        alpha.setDuration(2000).start();
    }

    //    切换显示手绘图和原图
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.show_original:
                ObjectAnimator alpha = ObjectAnimator.ofFloat(ivBgColor, "alpha", 0.0f, 1.0f);
                alpha.setDuration(1000).start();
                return true;
            case R.id.show_painted:
                ObjectAnimator alpha2 = ObjectAnimator.ofFloat(ivBgColor, "alpha", 1.0f, 0.0f);
                alpha2.setDuration(1000).start();
                return true;
        }
        return true;
    }
}