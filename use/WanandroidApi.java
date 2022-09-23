package com.cyw.myrxjava.use;

import com.cyw.myrxjava.bean.ProjectBean;
import com.cyw.myrxjava.bean.ProjectItem;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * author： cyw
 */
public interface WanandroidApi {
    @GET("project/tree/json")
    Observable<ProjectBean> getProject();
    @GET("project/list/{pageIndex}/json")
    Observable<ProjectItem> getProjectItem(@Path("pageIndex") int pageIndex,@Query("cid") int cid);

}
