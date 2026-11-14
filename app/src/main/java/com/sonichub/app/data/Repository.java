package com.sonichub.app.data;

import com.google.gson.Gson;
import com.sonichub.app.data.model.Models;

import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class Repository {
    private static final Repository INSTANCE = new Repository();
    private final Gson gson = new Gson();

    private Repository() {
    }

    public static Repository get() {
        return INSTANCE;
    }

    private <T> void enqueue(Call<Models.ApiResponse<T>> call, RepoCallback<T> callback) {
        call.enqueue(new Callback<Models.ApiResponse<T>>() {
            @Override
            public void onResponse(Call<Models.ApiResponse<T>> call, Response<Models.ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Models.ApiResponse<T> body = response.body();
                    if (body.ok()) {
                        callback.onSuccess(body.data);
                    } else {
                        callback.onError(body.message != null && !body.message.isEmpty()
                                ? body.message : "请求失败");
                    }
                    return;
                }
                String message = "请求失败 (" + response.code() + ")";
                try {
                    if (response.errorBody() != null) {
                        Models.ApiResponse<?> err = gson.fromJson(response.errorBody().string(), Models.ApiResponse.class);
                        if (err != null && err.message != null && !err.message.isEmpty()) {
                            message = err.message;
                        }
                    }
                } catch (Exception ignored) {
                }
                callback.onError(message);
            }

            @Override
            public void onFailure(Call<Models.ApiResponse<T>> call, Throwable t) {
                callback.onError("网络连接失败，请确认后端服务已启动");
            }
        });
    }

    public void login(String phone, String code, RepoCallback<Models.LoginData> cb) {
        enqueue(ApiClient.api().login(new Models.LoginRequest(phone, code)), cb);
    }

    /** 请求短信验证码（后端未配置阿里云时返回演示验证码）。 */
    public void sendSms(String phone, RepoCallback<Models.SmsSendData> cb) {
        enqueue(ApiClient.api().sendSms(new Models.SmsSendRequest(phone)), cb);
    }

    /** 本机号码一键登录：用号码认证 SDK 返回的 AccessToken 换取手机号并登录。 */
    public void oneClickLogin(String accessToken, RepoCallback<Models.LoginData> cb) {
        enqueue(ApiClient.api().oneClickLogin(new Models.OneClickRequest(accessToken)), cb);
    }

    /** 短信认证 SDK：换取 BizToken/STS 临时凭证。 */
    public void smsTokens(String sceneCode, String packageName, String signName,
                          RepoCallback<Models.SmsTokens> cb) {
        enqueue(ApiClient.api().smsTokens(
                new Models.SmsTokenRequest(sceneCode, packageName, signName)), cb);
    }

    /** 短信认证 SDK：提交验证码完成登录。 */
    public void smsVerify(String phone, String smsCode, String smsToken,
                          RepoCallback<Models.LoginData> cb) {
        enqueue(ApiClient.api().smsVerify(
                new Models.SmsVerifyRequest(phone, smsCode, smsToken)), cb);
    }

    public void me(RepoCallback<Models.MeData> cb) {
        enqueue(ApiClient.api().me(), cb);
    }

    public void recommend(int limit, int offset, RepoCallback<Models.RecommendData> cb) {
        enqueue(ApiClient.api().recommend(limit, offset), cb);
    }

    public void following(int limit, int offset, RepoCallback<Models.Page<Models.Work>> cb) {
        enqueue(ApiClient.api().following(limit, offset), cb);
    }

    public void plaza(String sort, String genre, int limit, int offset, RepoCallback<Models.Page<Models.Work>> cb) {
        enqueue(ApiClient.api().plaza(sort, genre, limit, offset), cb);
    }

    public void search(String query, RepoCallback<Models.SearchData> cb) {
        enqueue(ApiClient.api().search(query), cb);
    }

    public void work(String id, RepoCallback<Models.Work> cb) {
        enqueue(ApiClient.api().work(id), cb);
    }

    public void like(String id, boolean liked, RepoCallback<Models.LikeData> cb) {
        enqueue(liked ? ApiClient.api().unlike(id) : ApiClient.api().like(id), cb);
    }

    public void collect(String id, boolean collected, RepoCallback<Models.ActionData> cb) {
        enqueue(collected ? ApiClient.api().uncollect(id) : ApiClient.api().collect(id), cb);
    }

    public void comments(String id, RepoCallback<Models.CommentsData> cb) {
        enqueue(ApiClient.api().comments(id), cb);
    }

    public void addComment(String id, String content, RepoCallback<Models.Comment> cb) {
        enqueue(ApiClient.api().addComment(id, new Models.CommentRequest(content)), cb);
    }

    public void follow(String userId, boolean following, RepoCallback<Models.FollowData> cb) {
        enqueue(following ? ApiClient.api().unfollow(userId) : ApiClient.api().follow(userId), cb);
    }

    public void artist(String userId, RepoCallback<Models.ArtistData> cb) {
        enqueue(ApiClient.api().artist(userId), cb);
    }

    public void myApplication(RepoCallback<Models.Musician> cb) {
        enqueue(ApiClient.api().myApplication(), cb);
    }

    public void applyMusician(String stageName, String bio, List<String> genres, RepoCallback<Models.ActionData> cb) {
        enqueue(ApiClient.api().applyMusician(new Models.MusicianApplicationRequest(stageName, bio, genres)), cb);
    }

    public void quote(List<String> styles, int duration, String quality, boolean stems, RepoCallback<Models.Quote> cb) {
        enqueue(ApiClient.api().quote(new Models.QuoteRequest(styles, duration, quality, stems)), cb);
    }

    public void generate(String quoteId, String prompt, String lyrics, List<String> styles,
                         int duration, String vocal, String quality, boolean stems,
                         String title, String visibility, RepoCallback<Models.Generation> cb) {
        enqueue(ApiClient.api().generate(new Models.GenerateRequest(quoteId, prompt, lyrics, styles,
                duration, vocal, quality, stems, title, visibility)), cb);
    }

    public void generation(String id, RepoCallback<Models.Generation> cb) {
        enqueue(ApiClient.api().generation(id), cb);
    }

    /** 手机端 AI 写词（后端内部再调用音乐大模型）。 */
    public void mobileLyrics(String theme, String genre, String mood, int bpm,
                             RepoCallback<Models.MobileLyrics> cb) {
        enqueue(ApiClient.api().mobileLyrics(
                new Models.MobileLyricsRequest(theme, genre, mood, bpm)), cb);
    }

    /** 手机端一步生成音乐：报价 + 冻结积分 + 入队，返回 generation_id 供轮询。 */
    public void mobileMusic(String prompt, String lyrics, String genre, String mood, int duration,
                            String vocal, String title, RepoCallback<Models.MobileMusicData> cb) {
        Models.MobileMusicRequest body = new Models.MobileMusicRequest();
        body.prompt = prompt;
        body.lyrics = lyrics;
        body.genre = genre;
        body.mood = mood;
        body.durationSeconds = duration;
        body.vocal = vocal;
        body.title = title;
        body.visibility = "private";
        body.autoPublish = false;
        enqueue(ApiClient.api().mobileMusic(body), cb);
    }

    public void cancelGeneration(String id, RepoCallback<Models.Generation> cb) {
        enqueue(ApiClient.api().cancelGeneration(id), cb);
    }

    public void publish(String workId, String title, String description, String visibility,
                        List<String> tags, RepoCallback<Models.Work> cb) {
        enqueue(ApiClient.api().publish(workId,
                new Models.PublishRequest(title, description, visibility, tags)), cb);
    }

    public void credits(RepoCallback<Models.CreditAccount> cb) {
        enqueue(ApiClient.api().credits(), cb);
    }

    public void ledger(RepoCallback<Models.LedgerData> cb) {
        enqueue(ApiClient.api().ledger(), cb);
    }

    public void myWorks(String status, RepoCallback<Models.Page<Models.Work>> cb) {
        enqueue(ApiClient.api().myWorks(status), cb);
    }

    /** 把外部音乐引擎生成的音频上传并登记为当前用户的作品。 */
    public void importWork(String title, String genre, String tags, int duration, String description,
                           byte[] audio, RepoCallback<Models.Work> cb) {
        MediaType text = MediaType.parse("text/plain");
        MediaType wav = MediaType.parse("audio/wav");
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", "generated.wav",
                RequestBody.create(audio == null ? new byte[0] : audio, wav));
        enqueue(ApiClient.api().importWork(
                RequestBody.create(title, text),
                RequestBody.create(genre == null ? "" : genre, text),
                RequestBody.create(tags == null ? "" : tags, text),
                RequestBody.create(String.valueOf(duration), text),
                RequestBody.create(description == null ? "" : description, text),
                audioPart), cb);
    }

    public void notifications(String filter, RepoCallback<Models.NotificationsData> cb) {
        enqueue(ApiClient.api().notifications(filter), cb);
    }

    public void readAllNotifications(RepoCallback<Models.ActionData> cb) {
        enqueue(ApiClient.api().readAllNotifications(Collections.emptyMap()), cb);
    }

    public void conversations(RepoCallback<Models.ConversationsData> cb) {
        enqueue(ApiClient.api().conversations(), cb);
    }

    public void messages(String conversationId, RepoCallback<Models.MessagesData> cb) {
        enqueue(ApiClient.api().messages(conversationId), cb);
    }

    public void sendMessage(String conversationId, String text, RepoCallback<Models.Message> cb) {
        enqueue(ApiClient.api().sendMessage(conversationId,
                new Models.SendMessageRequest("text", text, "")), cb);
    }

    public void shareLink(String workId, RepoCallback<Models.ShareData> cb) {
        enqueue(ApiClient.api().shareLink(Collections.singletonMap("work_id", workId)), cb);
    }
}
