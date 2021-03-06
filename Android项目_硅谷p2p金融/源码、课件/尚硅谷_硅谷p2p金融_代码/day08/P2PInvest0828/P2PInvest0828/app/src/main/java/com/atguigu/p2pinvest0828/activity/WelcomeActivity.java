package com.atguigu.p2pinvest0828.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.atguigu.p2pinvest0828.R;
import com.atguigu.p2pinvest0828.bean.UpdateInfo;
import com.atguigu.p2pinvest0828.common.ActivityManager;
import com.atguigu.p2pinvest0828.common.AppNetConfig;
import com.atguigu.p2pinvest0828.util.UIUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WelcomeActivity extends Activity {

    private static final int TO_MAIN = 1;
    private static final int DOWNLOAD_VERSION_SUCCESS = 2;
    private static final int DOWNLOAD_APK_FAIL = 3;
    private static final int DOWNLOAD_APK_SUCCESS = 4;
    @Bind(R.id.iv_welcome_icon)
    ImageView ivWelcomeIcon;
    @Bind(R.id.rl_welcome)
    RelativeLayout rlWelcome;
    @Bind(R.id.tv_welcome_version)
    TextView tvWelcomeVersion;
    private boolean connect;
    private long startTime;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TO_MAIN:
                    finish();
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    break;
                case DOWNLOAD_VERSION_SUCCESS:
                    //?????????????????????????????????
                    String version = getVersion();
                    //?????????????????????????????????
                    tvWelcomeVersion.setText(version);
                    //????????????????????????????????????????????????????????????????????????
                    if(version.equals(updateInfo.version)){
                        UIUtils.toast("?????????????????????????????????",false);
                        toMain();
                    }else{
                        new AlertDialog.Builder(WelcomeActivity.this)
                                    .setTitle("??????????????????")
                                    .setMessage(updateInfo.desc)
                                    .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //????????????????????????????????????
                                            downloadApk();
                                        }
                                    })
                                    .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            toMain();
                                        }
                                    })
                                    .show();
                    }

                    break;
                case DOWNLOAD_APK_FAIL:
                    UIUtils.toast("????????????????????????",false);
                    toMain();
                    break;
                case DOWNLOAD_APK_SUCCESS:
                    UIUtils.toast("????????????????????????",false);
                    dialog.dismiss();
                    installApk();//????????????????????????
                    finish();//???????????????welcomeActivity?????????
                    break;
            }

        }
    };

    private void installApk() {
        Intent intent = new Intent("android.intent.action.INSTALL_PACKAGE");
        intent.setData(Uri.parse("file:" + apkFile.getAbsolutePath()));
        startActivity(intent);
    }

    private ProgressDialog dialog;
    private File apkFile;
    private void downloadApk() {
        //???????????????????????????dialog
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();
        //?????????????????????????????????
        File filesDir;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            filesDir = this.getExternalFilesDir("");
        }else{
            filesDir = this.getFilesDir();
        }
        apkFile = new File(filesDir,"update.apk");

        //??????????????????????????????????????????
        new Thread(){
            public void run(){
                String path = updateInfo.apkUrl;
                InputStream is = null;
                FileOutputStream fos = null;
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(path);
                    conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    conn.connect();

                    if(conn.getResponseCode() == 200){
                        dialog.setMax(conn.getContentLength());//??????dialog????????????
                        is = conn.getInputStream();
                        fos = new FileOutputStream(apkFile);

                        byte[] buffer = new byte[1024];
                        int len;
                        while((len = is.read(buffer)) != -1){
                            //??????dialog?????????
                            dialog.incrementProgressBy(len);
                            fos.write(buffer,0,len);

                            SystemClock.sleep(1);
                        }

                        handler.sendEmptyMessage(DOWNLOAD_APK_SUCCESS);

                    }else{
                        handler.sendEmptyMessage(DOWNLOAD_APK_FAIL);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }finally{
                    if(conn != null){
                        conn.disconnect();
                    }
                    if(is != null){
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if(fos != null){
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }


            }
        }.start();


    }

    private UpdateInfo updateInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ??????????????????
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // ????????????????????????
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);


        //????????????activity?????????ActivityManager???
        ActivityManager.getInstance().add(this);
        //??????????????????
        setAnimation();

        //??????????????????
        updateApkFile();

    }

    /**
     * ???????????????
     *
     * @return
     */
    private String getVersion() {
        String version = "????????????";
        PackageManager manager = getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace(); //???????????????????????????????????????, ?????????"????????????"
        }
        return version;
    }

    private void updateApkFile() {
        //????????????????????????
        startTime = System.currentTimeMillis();

        //1.??????????????????????????????
        boolean connect = isConnect();
        if (!connect) {//??????????????????
            UIUtils.toast("??????????????????????????????", false);
            toMain();
        } else {//???????????????
            //??????????????????????????????????????????
            AsyncHttpClient client = new AsyncHttpClient();
            String url = AppNetConfig.UPDATE;
            client.post(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String content) {
                    //??????json??????
                    updateInfo = JSON.parseObject(content, UpdateInfo.class);
                    handler.sendEmptyMessage(DOWNLOAD_VERSION_SUCCESS);
                }

                @Override
                public void onFailure(Throwable error, String content) {
                    UIUtils.toast("????????????????????????", false);
                    toMain();
                }
            });

        }
    }

    private void toMain() {
        long currentTime = System.currentTimeMillis();
        long delayTime = 3000 - (currentTime - startTime);
        if (delayTime < 0) {
            delayTime = 0;
        }


        handler.sendEmptyMessageDelayed(TO_MAIN, delayTime);
    }


    private void setAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);//0???????????????  1??????????????????
        alphaAnimation.setDuration(3000);
        alphaAnimation.setInterpolator(new AccelerateInterpolator());//????????????????????????

        //????????????
//        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//            //???????????????????????????????????????
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
//                startActivity(intent);
//                finish();//??????????????????
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
        //??????????????????handler
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
//                startActivity(intent);
////                finish();//??????????????????
//                //??????activity????????????????????????????????????
//                ActivityManager.getInstance().remove(WelcomeActivity.this);
//            }
//        }, 3000);

        //????????????
        rlWelcome.startAnimation(alphaAnimation);

    }

    public boolean isConnect() {
        boolean connected = false;

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        if (networkInfo != null) {
            connected = networkInfo.isConnected();
        }
        return connected;
    }
}
