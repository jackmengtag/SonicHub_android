package com.sonichub.app.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public final class Models {
    private Models() {
    }

    public static class ApiResponse<T> {
        public String code;
        public String message;
        public T data;

        public boolean ok() {
            return "OK".equals(code) && data != null;
        }
    }

    public static class Page<T> {
        public List<T> items;
        @SerializedName("next_cursor")
        public String nextCursor;
        @SerializedName("has_more")
        public boolean hasMore;
    }

    public static class RecommendData extends Page<Work> {
        @SerializedName("policy_version")
        public String policyVersion;
    }

    public static class User {
        public String id;
        public String phone;
        @SerializedName("display_name")
        public String displayName;
        public String avatar;
        public String bio;
        public String role;
        public String status;
        public List<String> genres;
        @SerializedName("created_at")
        public String createdAt;
    }

    public static class UserCard {
        public String id;
        public String name;
        public String avatar;
        public String bio;
        public boolean verified;
        @SerializedName("stage_name")
        public String stageName;
        public boolean following;
        public boolean friend;
        public boolean follower;
        public boolean blocked;
    }

    public static class Musician {
        @SerializedName("user_id")
        public String userId;
        @SerializedName("stage_name")
        public String stageName;
        public String status;
        public String bio;
        public List<String> genres;
        @SerializedName("verified_at")
        public String verifiedAt;
    }

    public static class Work {
        public String id;
        public String title;
        public String cover;
        public String description;
        public String visibility;
        public String status;
        @SerializedName("reject_reason")
        public String rejectReason;
        public String genre;
        public List<String> tags;
        @SerializedName("audio_url")
        public String audioUrl;
        @SerializedName("duration_seconds")
        public int duration;
        @SerializedName("play_count")
        public int playCount;
        @SerializedName("like_count")
        public int likeCount;
        @SerializedName("comment_count")
        public int commentCount;
        @SerializedName("share_count")
        public int shareCount;
        @SerializedName("is_ai")
        public boolean isAi;
        @SerializedName("owner_id")
        public String ownerId;
        @SerializedName("owner_name")
        public String ownerName;
        @SerializedName("owner_avatar")
        public String ownerAvatar;
        @SerializedName("owner_verified")
        public boolean ownerVerified;
        public boolean liked;
        public boolean collected;
        public String reason;
        @SerializedName("published_at")
        public String publishedAt;
        @SerializedName("created_at")
        public String createdAt;

        public String tagsText() {
            if (tags == null || tags.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tags.size(); i++) {
                if (i > 0) sb.append(" / ");
                sb.append(tags.get(i));
            }
            return sb.toString();
        }

        public String durationText() {
            int s = duration > 0 ? duration : 30;
            return String.format(java.util.Locale.US, "%02d:%02d", s / 60, s % 60);
        }
    }

    public static class Comment {
        public String id;
        @SerializedName("work_id")
        public String workId;
        @SerializedName("user_id")
        public String userId;
        public String name;
        public String avatar;
        public String content;
        @SerializedName("created_at")
        public String createdAt;
    }

    public static class Notification {
        public String id;
        public String type;
        @SerializedName("actor_id")
        public String actorId;
        @SerializedName("actor_name")
        public String actorName;
        @SerializedName("actor_avatar")
        public String actorAvatar;
        public String title;
        public String body;
        @SerializedName("target_type")
        public String targetType;
        @SerializedName("target_id")
        public String targetId;
        @SerializedName("read_at")
        public String readAt;
        @SerializedName("created_at")
        public String createdAt;

        public boolean unread() {
            return readAt == null || readAt.isEmpty();
        }
    }

    public static class Conversation {
        public String id;
        @SerializedName("peer_id")
        public String peerId;
        @SerializedName("peer_name")
        public String peerName;
        @SerializedName("peer_avatar")
        public String peerAvatar;
        @SerializedName("last_message")
        public String lastMessage;
        public int unread;
        public boolean pinned;
        @SerializedName("updated_at")
        public String updatedAt;
    }

    public static class Message {
        public String id;
        @SerializedName("sender_id")
        public String senderId;
        public String type;
        public String text;
        @SerializedName("work_id")
        public String workId;
        public Work work;
        @SerializedName("created_at")
        public String createdAt;
    }

    public static class CreditAccount {
        public long available;
        public long reserved;
    }

    public static class Quote {
        @SerializedName("quote_id")
        public String quoteId;
        public int base;
        @SerializedName("quality_fee")
        public int qualityFee;
        @SerializedName("stems_fee")
        public int stemsFee;
        @SerializedName("duration_fee")
        public int durationFee;
        public int total;
        public String status;
        @SerializedName("expires_at")
        public String expiresAt;
    }

    public static class Generation {
        public String id;
        public String status;
        public String stage;
        public int progress;
        public String error;
        @SerializedName("work_id")
        public String workId;
        public String title;
    }

    public static class PackageItem {
        public String id;
        public int credits;
        @SerializedName("price_cents")
        public int priceCents;
        public String tag;
    }

    public static class LedgerEntry {
        public String id;
        @SerializedName("entry_type")
        public String entryType;
        public long amount;
        @SerializedName("balance_after")
        public long balanceAfter;
        @SerializedName("ref_type")
        public String refType;
        @SerializedName("ref_id")
        public String refId;
        @SerializedName("created_at")
        public String createdAt;
    }

    public static class LoginData {
        @SerializedName("access_token")
        public String accessToken;
        @SerializedName("refresh_token")
        public String refreshToken;
        @SerializedName("expires_in")
        public int expiresIn;
        public User user;
        @SerializedName("tenant_id")
        public String tenantId;
    }

    public static class Counts {
        public int works;
        public int following;
        public int followers;
        public int friends;
    }

    public static class MeData {
        public User user;
        public Musician musician;
        public CreditAccount credits;
        public Counts counts;
        @SerializedName("unread_notifications")
        public int unreadNotifications;
    }

    public static class ArtistData {
        public UserCard user;
        public Musician musician;
        public List<Work> works;
    }

    public static class NotificationsData {
        public List<Notification> items;
        public int unread;
    }

    public static class ConversationsData {
        public List<Conversation> items;
    }

    public static class MessagesData {
        public List<Message> items;
    }

    public static class CommentsData {
        public List<Comment> items;
    }

    public static class PackagesData {
        public List<PackageItem> items;
    }

    public static class LedgerData {
        public List<LedgerEntry> items;
    }

    public static class SearchData {
        public List<Work> works;
        public List<UserCard> artists;
    }

    public static class LikeData {
        public boolean liked;
        @SerializedName("like_count")
        public int likeCount;
    }

    public static class FollowData {
        public boolean following;
        public boolean friend;
    }

    public static class ShareData {
        public String code;
        public String url;
    }

    public static class ActionData {
        public boolean ok;
        public boolean submitted;
        public boolean deleted;
        public String status;
    }

    public static class LoginRequest {
        public String phone;
        public String code;

        public LoginRequest(String phone, String code) {
            this.phone = phone;
            this.code = code;
        }
    }

    public static class SmsSendRequest {
        public String phone;

        public SmsSendRequest(String phone) {
            this.phone = phone;
        }
    }

    public static class SmsSendData {
        public boolean sent;
        public boolean demo;
        @SerializedName("demo_code")
        public String demoCode;
        public String message;
        @SerializedName("biz_id")
        public String bizId;
        @SerializedName("interval_seconds")
        public int intervalSeconds;
        @SerializedName("valid_seconds")
        public int validSeconds;
    }

    public static class OneClickRequest {
        @SerializedName("access_token")
        public String accessToken;

        public OneClickRequest(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    /** 短信认证 SDK 换取 BizToken/STS 的请求。 */
    public static class SmsTokenRequest {
        @SerializedName("scene_code")
        public String sceneCode;
        @SerializedName("package_name")
        public String packageName;
        @SerializedName("sign_name")
        public String signName;

        public SmsTokenRequest(String sceneCode, String packageName, String signName) {
            this.sceneCode = sceneCode;
            this.packageName = packageName;
            this.signName = signName;
        }
    }

    public static class SmsTokens {
        @SerializedName("biz_token")
        public String bizToken;
        @SerializedName("expire_time")
        public long expireTime;
        @SerializedName("sts_access_key_id")
        public String stsAccessKeyId;
        @SerializedName("sts_access_key_secret")
        public String stsAccessKeySecret;
        @SerializedName("sts_token")
        public String stsToken;
        @SerializedName("scene_code")
        public String sceneCode;
        @SerializedName("sms_code_expire")
        public int smsCodeExpire;
    }

    /** 短信认证 SDK 发送后，提交验证码完成登录。 */
    public static class SmsVerifyRequest {
        public String phone;
        @SerializedName("sms_code")
        public String smsCode;
        @SerializedName("sms_token")
        public String smsToken;

        public SmsVerifyRequest(String phone, String smsCode, String smsToken) {
            this.phone = phone;
            this.smsCode = smsCode;
            this.smsToken = smsToken;
        }
    }

    public static class RefreshRequest {
        @SerializedName("refresh_token")
        public String refreshToken;

        public RefreshRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }

    public static class QuoteRequest {
        public List<String> styles;
        @SerializedName("duration_seconds")
        public int duration;
        public String quality;
        public boolean stems;

        public QuoteRequest(List<String> styles, int duration, String quality, boolean stems) {
            this.styles = styles;
            this.duration = duration;
            this.quality = quality;
            this.stems = stems;
        }
    }

    public static class GenerateRequest {
        @SerializedName("quote_id")
        public String quoteId;
        public String prompt;
        public String lyrics;
        public List<String> styles;
        public String mood;
        @SerializedName("duration_seconds")
        public int duration;
        public String vocal;
        public String quality;
        public boolean stems;
        public String title;
        public String visibility;
        @SerializedName("auto_publish")
        public boolean autoPublish;

        public GenerateRequest(String quoteId, String prompt, String lyrics, List<String> styles,
                               int duration, String vocal, String quality, boolean stems,
                               String title, String visibility) {
            this.quoteId = quoteId;
            this.prompt = prompt;
            this.lyrics = lyrics;
            this.styles = styles;
            this.duration = duration;
            this.vocal = vocal;
            this.quality = quality;
            this.stems = stems;
            this.title = title;
            this.visibility = visibility;
        }
    }

    public static class PublishRequest {
        public String title;
        public String description;
        public String visibility;
        public List<String> tags;

        public PublishRequest(String title, String description, String visibility, List<String> tags) {
            this.title = title;
            this.description = description;
            this.visibility = visibility;
            this.tags = tags;
        }
    }

    public static class CommentRequest {
        public String content;

        public CommentRequest(String content) {
            this.content = content;
        }
    }

    public static class SendMessageRequest {
        public String type;
        public String text;
        @SerializedName("work_id")
        public String workId;

        public SendMessageRequest(String type, String text, String workId) {
            this.type = type;
            this.text = text;
            this.workId = workId;
        }
    }

    public static class RechargeRequest {
        @SerializedName("package_id")
        public String packageId;

        public RechargeRequest(String packageId) {
            this.packageId = packageId;
        }
    }

    public static class MusicianApplicationRequest {
        @SerializedName("stage_name")
        public String stageName;
        public String bio;
        public List<String> genres;

        public MusicianApplicationRequest(String stageName, String bio, List<String> genres) {
            this.stageName = stageName;
            this.bio = bio;
            this.genres = genres;
        }
    }
}
