package com.cyw.myrxjava;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cyw.myrxjava.bean.ProjectBean;
import com.cyw.myrxjava.bean.ProjectItem;
import com.cyw.myrxjava.use.WanandroidApi;
import com.cyw.myrxjava.use.util.DownloadActivity;
import com.cyw.myrxjava.use.util.HttpUtil;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private WanandroidApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = HttpUtil.getOnLineClient().create(WanandroidApi.class);
        getProjectAction(null);
    }
    /**
     * TODO Retrofit+RxJava 查询 项目分类  (总数据查询)
     *
     * @param view
     */
    public void getProjectAction(View view) {
        api.getProject().subscribeOn(Schedulers.io())//上面异步
                .observeOn(AndroidSchedulers.mainThread())//下面主线程
                .subscribe(new Consumer<ProjectBean>() {
                    @Override
                    public void accept(@NonNull ProjectBean projectBean) throws Exception {
                        Log.d(TAG, "accept: " + projectBean); // UI 可以做事情
                    }
                });
    }
    /**
     * TODO Retrofit+RxJava 查询  项目分类的49 去 获取项目列表数据  (Item)
     *
     * @param view
     */
    public void getProjectListAction(View view) {
        // 注意：这里的 294 是项目分类 所查询出来的数据
        // 上面的项目分类会查询出："id": 294,"id": 402,"id": 367,"id": 323,"id": 314, ...

        // id 写死的
        api.getProjectItem(1, 294)
                // .....
                .subscribeOn(Schedulers.io()) // 上面 异步
                .observeOn(AndroidSchedulers.mainThread()) // 下面 主线程
                .subscribe(data->{
                    Log.d(TAG, "getProjectListAction: " + data);
                });

    }
    /**
     * RxJava
     * RxJs
     * Rxxxxx
     * RxBinding  防抖
     *
     * TODO 功能防抖 + 网络嵌套（这种是负面教程，嵌套的太厉害了）
     * 2层嵌套
     * 6层
     */
//    @SuppressLint("CheckResult")
    private void antiShakeActon() {
        // 注意：（项目分类）查询的id，通过此id再去查询(项目列表数据)

        // 对那个控件防抖动？
        Button bt_anti_shake = findViewById(R.id.bt_anti_shake);

        RxView.clicks(bt_anti_shake)
                .throttleFirst(2000, TimeUnit.MILLISECONDS) // 2秒钟之内 响应你一次
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        api.getProject() // 查询主数据
                                .compose(DownloadActivity.rxud())
                                .subscribe(new Consumer<ProjectBean>() {
                                    @Override
                                    public void accept(ProjectBean projectBean) throws Exception {
                                        for (ProjectBean.DataBean dataBean : projectBean.getData()) { // 10
                                            // 查询item数据
                                            api.getProjectItem(1, dataBean.getId())
                                                    .compose(DownloadActivity.rxud())
                                                    .subscribe(new Consumer<ProjectItem>() {
                                                        @Override
                                                        public void accept(ProjectItem projectItem) throws Exception {
                                                            Log.d(TAG, "accept: " + projectItem); // 可以UI操作
                                                        }
                                                    });
                                        }
                                    }
                                });
                    }
                });
    }
        /**
     * TODO 功能防抖 + 网络嵌套 (解决嵌套的问题) flatMap
     */
    @SuppressLint("CheckResult")
   private void antiShakeActonUpdate() {
       Button bt_anti_shake = findViewById(R.id.bt_anti_shake);
       RxView.clicks(bt_anti_shake)
               .throttleFirst(2000,TimeUnit.MILLISECONDS)
               // 我只给下面 切换 异步
               .observeOn(Schedulers.io())
               .flatMap(new Function<Object, ObservableSource<ProjectBean>>() {
                   @NonNull
                   @Override
                   public ObservableSource<ProjectBean> apply(@NonNull Object object) throws Exception {
                       return api.getProject();
                   }
               })
               .flatMap(new Function<ProjectBean, ObservableSource<ProjectBean.DataBean>>() {
                   @NonNull
                   @Override
                   public ObservableSource<ProjectBean.DataBean> apply(@NonNull ProjectBean projectBean) throws Exception {
                       return Observable.fromIterable(projectBean.getData());//自己搞一个发射器，发射多次
                   }
               })
               .flatMap(new Function<ProjectBean.DataBean, ObservableSource<ProjectItem>>() {
                   @NonNull
                   @Override
                   public ObservableSource<ProjectItem> apply(@NonNull ProjectBean.DataBean dataBean) throws Exception {
                       return api.getProjectItem(1,dataBean.getId());
                   }
               })
               .observeOn(AndroidSchedulers.mainThread())
               .subscribe(new Consumer<ProjectItem>() {
                   @Override
                   public void accept(@NonNull ProjectItem projectItem) throws Exception {

                   }
               });
   }
}