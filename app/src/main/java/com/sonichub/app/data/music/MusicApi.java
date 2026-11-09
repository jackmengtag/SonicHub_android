package com.sonichub.app.data.music;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/** 音乐大模型服务（默认 http://10.0.2.2:8001/）的接口定义。 */
public interface MusicApi {
    /** AI 写词：按歌名/创意生成歌词。 */
    @POST("api/lyrics")
    Call<MusicModels.LyricsResponse> lyrics(@Body MusicModels.LyricsRequest body);

    /** 提交生成任务，立即返回 task_id（异步生成）。 */
    @POST("api/generate")
    Call<MusicModels.GenerateResponse> generate(@Body MusicModels.GenerateRequest body);

    /** 轮询任务进度，done 时带 result.wav_url。 */
    @GET("api/progress/{id}")
    Call<MusicModels.Progress> progress(@Path("id") String taskId);
}
