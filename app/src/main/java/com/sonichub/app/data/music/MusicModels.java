package com.sonichub.app.data.music;

/**
 * 音乐大模型（Python 8001 服务）的请求/响应模型。
 * 字段名与后端 JSON 保持一致（snake_case 直接作为 Java 字段名，省去 @SerializedName）。
 */
public final class MusicModels {
    private MusicModels() {
    }

    /** AI 写词请求：主题 + 曲风/情绪/速度。 */
    public static class LyricsRequest {
        public String theme;
        public String genre;
        public String mood;
        public int bpm;

        public LyricsRequest(String theme, String genre, String mood, int bpm) {
            this.theme = theme;
            this.genre = genre;
            this.mood = mood;
            this.bpm = bpm;
        }
    }

    /** AI 写词响应：{ok, lyrics} 或 {ok:false, error}。 */
    public static class LyricsResponse {
        public boolean ok;
        public String lyrics;
        public String error;
    }

    /** 生成歌曲请求：歌名 + 歌词（可为空→服务端自动写词）+ 参数。 */
    public static class GenerateRequest {
        public String title;
        public String lyrics;
        public String genre;
        public String mood;
        public String vocal;
        public int bpm;
        public int duration;
    }

    /** 提交生成任务响应：{ok, task_id}。 */
    public static class GenerateResponse {
        public boolean ok;
        public String task_id;
        public String error;
    }

    /** 成品信息（进度到达 done 时返回）。 */
    public static class Result {
        public String task_id;
        public String wav_url;
        public String engine;
        public String lyrics_preview;
        public double duration_sec;
        public double elapsed_sec;
    }

    /** 进度轮询响应：{ok, status, progress, stage, msg, error?, result?}。 */
    public static class Progress {
        public boolean ok;
        public String status;
        public int progress;
        public String stage;
        public String msg;
        public String error;
        public Result result;
    }
}
