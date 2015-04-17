package com.test.helloworld;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Created by blin on 2015/4/17.
 */
public class PowerImageView extends ImageView implements View.OnClickListener {
private String  TAGSTR="PowerImgView";
    /**
     * 播放GIF??的???
     */
    private Movie mMovie;

    /**
     * ?始播放按??片
     */
    private Bitmap mStartButton;

    /**
     * ?????始的??
     */
    private long mMovieStart;

    /**
     * GIF?片的?度
     */
    private int mImageWidth;

    /**
     * GIF?片的高度
     */
    private int mImageHeight;

    /**
     * ?片是否正在播放
     */
    private boolean isPlaying;

    /**
     * 是否允?自?播放
     */
    private boolean isAutoPlay;

    /**
     * PowerImageView构造函?。
     *
     * @param context
     */
    public PowerImageView(Context context) {
        super(context);
    }

    /**
     * PowerImageView构造函?。
     *
     * @param context
     */
    public PowerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * PowerImageView构造函?，在?里完成所有必要的初始化操作。
     *
     * @param context
     */
    public PowerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PowerImageView);
        int resourceId = getResourceId(a, context, attrs);
        if (resourceId != 0) {
            // ??源id不等于0?，就去?取??源的流
            InputStream is = getResources().openRawResource(resourceId);
            // 使用Movie??流?行解?
            mMovie = Movie.decodeStream(is);
            if (mMovie != null) {
                // 如果返回值不等于null，就?明?是一?GIF?片，下面?取是否自?播放的?性
                Log.i(TAGSTR, "?是一?GIF?片");
                isAutoPlay = a.getBoolean(R.styleable.PowerImageView_auto_play, false);
                Log.i(TAGSTR, "isAutoplay:"+Boolean.toString(isAutoPlay));
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                mImageWidth = bitmap.getWidth();
                mImageHeight = bitmap.getHeight();
                bitmap.recycle();
                if (!isAutoPlay) {
                    // ?不允?自?播放的?候，得到?始播放按?的?片，并注???事件
                    mStartButton = BitmapFactory.decodeResource(getResources(),
                            R.drawable.start_play);
                    setOnClickListener(this);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == getId()) {
            // ?用????片?，?始播放GIF??
            isPlaying = true;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mMovie == null) {
            // mMovie等于null，?明是?普通的?片，?直接?用父?的onDraw()方法
            super.onDraw(canvas);
        } else {
            // mMovie不等于null，?明是?GIF?片
            if (isAutoPlay) {
                // 如果允?自?播放，就?用playMovie()方法播放GIF??
                playMovie(canvas);
                invalidate();
            } else {
                // 不允?自?播放?，判??前?片是否正在播放
                if (isPlaying) {
                    // 正在播放就???用playMovie()方法，一直到??播放?束?止
                    if (playMovie(canvas)) {
                        isPlaying = false;
                    }
                    invalidate();
                } else {
                    // ???始播放就只?制GIF?片的第一?，并?制一??始按?
                    mMovie.setTime(0);
                    mMovie.draw(canvas, 0, 0);
                    int offsetW = (mImageWidth - mStartButton.getWidth()) / 2;
                    int offsetH = (mImageHeight - mStartButton.getHeight()) / 2;
                    canvas.drawBitmap(mStartButton, offsetW, offsetH, null);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mMovie != null) {
            // 如果是GIF?片?重??定PowerImageView的大小
            setMeasuredDimension(mImageWidth, mImageHeight);
        }
    }

    /**
     * ?始播放GIF??，播放完成返回true，未完成返回false。
     *
     * @param canvas
     * @return 播放完成返回true，未完成返回false。
     */
    private boolean playMovie(Canvas canvas) {
        long now = SystemClock.uptimeMillis();
//        Log.i(TAGSTR, "playMovie");
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        int duration = mMovie.duration();
        if (duration == 0) {
            duration = 1000;
        }
        int relTime = (int) ((now - mMovieStart) % duration);
        mMovie.setTime(relTime);
        mMovie.draw(canvas, 0, 0);
        if ((now - mMovieStart) >= duration) {
            mMovieStart = 0;
            return true;
        }
        return false;
    }

    /**
     * 通?Java反射，?取到src指定?片?源所??的id。
     *
     * @param a
     * @param context
     * @param attrs
     * @return 返回布局文件中指定?片?源所??的id，?有指定任何?片?源就返回0。
     */
    private int getResourceId(TypedArray a, Context context, AttributeSet attrs) {
        try {
            Field field = TypedArray.class.getDeclaredField("mValue");
            field.setAccessible(true);
            TypedValue typedValueObject = (TypedValue) field.get(a);
            return typedValueObject.resourceId;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
        return 0;
    }

}