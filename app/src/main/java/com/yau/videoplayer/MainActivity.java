package com.yau.videoplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;

import com.yau.videoplayer.player.VideoPlayer;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private SeekBar mSeekBar;

    private VideoPlayer mVideoPlayer;
    private int mProgress;
    private boolean mIsTouching;
    private boolean mIsSeeking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 保持屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
        checkPermission();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoPlayer.release();
    }

    private void initView() {
        mSeekBar = findViewById(R.id.seek_bar);

        mVideoPlayer = new VideoPlayer();
        SurfaceView svVideoPlayer = findViewById(R.id.sv_video_player);
        mVideoPlayer.setSurfaceView(svVideoPlayer);
    }

    private void initEvent() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsTouching = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mIsTouching = false;
                mIsSeeking = true;

                mProgress = (int) (mVideoPlayer.getDuration() * (1.0 * seekBar.getProgress() / seekBar.getMax()));
                mVideoPlayer.seekTo(mProgress);
            }
        });

        mVideoPlayer.setOnPrepareListener(new VideoPlayer.OnPrepareListener() {
            @Override
            public void onPrepare() {
                mVideoPlayer.start();
                if (mVideoPlayer.getDuration() != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSeekBar.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        mVideoPlayer.setOnProgressListener(new VideoPlayer.OnProgressListener() {
            @Override
            public void onProgress(final int progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int duration = mVideoPlayer.getDuration();
                        if (duration <= 0) {
                            return;
                        }
                        if (mIsTouching) {
                            return;
                        }
                        mSeekBar.setProgress((int) (1.0 * progress  / duration * mSeekBar.getMax()));
                    }
                });
            }
        });
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 102);
            }
        }
    }

    public void play(View v) {
        File file = new File(Environment.getExternalStorageDirectory(), "input.mp4");
        if (!file.exists()) {
            throw new IllegalArgumentException("=======================文件不存在");
        }
        mVideoPlayer.setDataSource(file.getAbsolutePath());
        mVideoPlayer.prepare();
    }

    public void stop(View view) {
        mVideoPlayer.stop();
    }
}
