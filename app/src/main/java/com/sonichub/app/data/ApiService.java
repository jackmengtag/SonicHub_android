package com.sonichub.app.data;

import com.sonichub.app.data.model.Models;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/v1/auth/login")
    Call<Models.ApiResponse<Models.LoginData>> login(@Body Models.LoginRequest body);

    @POST("api/v1/auth/sms/send")
    Call<Models.ApiResponse<Models.SmsSendData>> sendSms(@Body Models.SmsSendRequest body);

    @POST("api/v1/auth/one-click/login")
    Call<Models.ApiResponse<Models.LoginData>> oneClickLogin(@Body Models.OneClickRequest body);

    @POST("api/v1/auth/sms/tokens")
    Call<Models.ApiResponse<Models.SmsTokens>> smsTokens(@Body Models.SmsTokenRequest body);

    @POST("api/v1/auth/sms/verify")
    Call<Models.ApiResponse<Models.LoginData>> smsVerify(@Body Models.SmsVerifyRequest body);

    @POST("api/v1/auth/refresh")
    Call<Models.ApiResponse<Models.LoginData>> refresh(@Body Models.RefreshRequest body);

    @GET("api/v1/me")
    Call<Models.ApiResponse<Models.MeData>> me();

    @GET("api/v1/feed/recommend")
    Call<Models.ApiResponse<Models.RecommendData>> recommend(@Query("limit") int limit, @Query("offset") int offset);

    @GET("api/v1/feed/following")
    Call<Models.ApiResponse<Models.Page<Models.Work>>> following(@Query("limit") int limit, @Query("offset") int offset);

    @GET("api/v1/plaza")
    Call<Models.ApiResponse<Models.Page<Models.Work>>> plaza(@Query("sort") String sort, @Query("genre") String genre,
                                                            @Query("limit") int limit, @Query("offset") int offset);

    @GET("api/v1/search")
    Call<Models.ApiResponse<Models.SearchData>> search(@Query("q") String query);

    @GET("api/v1/works/{id}")
    Call<Models.ApiResponse<Models.Work>> work(@Path("id") String id);

    @POST("api/v1/works/{id}/like")
    Call<Models.ApiResponse<Models.LikeData>> like(@Path("id") String id);

    @DELETE("api/v1/works/{id}/like")
    Call<Models.ApiResponse<Models.LikeData>> unlike(@Path("id") String id);

    @POST("api/v1/works/{id}/collect")
    Call<Models.ApiResponse<Models.ActionData>> collect(@Path("id") String id);

    @DELETE("api/v1/works/{id}/collect")
    Call<Models.ApiResponse<Models.ActionData>> uncollect(@Path("id") String id);

    @GET("api/v1/works/{id}/comments")
    Call<Models.ApiResponse<Models.CommentsData>> comments(@Path("id") String id);

    @POST("api/v1/works/{id}/comments")
    Call<Models.ApiResponse<Models.Comment>> addComment(@Path("id") String id, @Body Models.CommentRequest body);

    @POST("api/v1/users/{id}/follow")
    Call<Models.ApiResponse<Models.FollowData>> follow(@Path("id") String id);

    @DELETE("api/v1/users/{id}/follow")
    Call<Models.ApiResponse<Models.FollowData>> unfollow(@Path("id") String id);

    @GET("api/v1/musicians/{id}")
    Call<Models.ApiResponse<Models.ArtistData>> artist(@Path("id") String id);

    @GET("api/v1/musicians/applications/me")
    Call<Models.ApiResponse<Models.Musician>> myApplication();

    @POST("api/v1/musicians/applications")
    Call<Models.ApiResponse<Models.ActionData>> applyMusician(@Body Models.MusicianApplicationRequest body);

    @POST("api/v1/credit/quotes")
    Call<Models.ApiResponse<Models.Quote>> quote(@Body Models.QuoteRequest body);

    @POST("api/v1/generations")
    Call<Models.ApiResponse<Models.Generation>> generate(@Body Models.GenerateRequest body);

    @POST("api/v1/mobile/lyrics")
    Call<Models.ApiResponse<Models.MobileLyrics>> mobileLyrics(@Body Models.MobileLyricsRequest body);

    @POST("api/v1/mobile/music")
    Call<Models.ApiResponse<Models.MobileMusicData>> mobileMusic(@Body Models.MobileMusicRequest body);

    @GET("api/v1/generations/{id}")
    Call<Models.ApiResponse<Models.Generation>> generation(@Path("id") String id);

    @POST("api/v1/generations/{id}/cancel")
    Call<Models.ApiResponse<Models.Generation>> cancelGeneration(@Path("id") String id);

    @POST("api/v1/works/{id}/publish")
    Call<Models.ApiResponse<Models.Work>> publish(@Path("id") String id, @Body Models.PublishRequest body);

    @GET("api/v1/me/credits")
    Call<Models.ApiResponse<Models.CreditAccount>> credits();

    @GET("api/v1/me/credits/ledger")
    Call<Models.ApiResponse<Models.LedgerData>> ledger();

    @GET("api/v1/me/works")
    Call<Models.ApiResponse<Models.Page<Models.Work>>> myWorks(@Query("status") String status);

    @Multipart
    @POST("api/v1/works/import")
    Call<Models.ApiResponse<Models.Work>> importWork(@Part("title") RequestBody title,
                                                     @Part("genre") RequestBody genre,
                                                     @Part("tags") RequestBody tags,
                                                     @Part("duration_seconds") RequestBody duration,
                                                     @Part("description") RequestBody description,
                                                     @Part MultipartBody.Part audio);

    @GET("api/v1/notifications")
    Call<Models.ApiResponse<Models.NotificationsData>> notifications(@Query("filter") String filter);

    @POST("api/v1/notifications/read")
    Call<Models.ApiResponse<Models.ActionData>> readAllNotifications(@Body Map<String, Object> body);

    @GET("api/v1/conversations")
    Call<Models.ApiResponse<Models.ConversationsData>> conversations();

    @GET("api/v1/conversations/{id}/messages")
    Call<Models.ApiResponse<Models.MessagesData>> messages(@Path("id") String id);

    @POST("api/v1/conversations/{id}/messages")
    Call<Models.ApiResponse<Models.Message>> sendMessage(@Path("id") String id, @Body Models.SendMessageRequest body);

    @POST("api/v1/share-links")
    Call<Models.ApiResponse<Models.ShareData>> shareLink(@Body Map<String, Object> body);
}
