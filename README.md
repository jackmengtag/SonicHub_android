# SonicHub Android

基于 `http://127.0.0.1:5173/index.html` 原型实现的**原生 Android（Java）MVP** 客户端，并对接 `SonicHub_api` 后端。

- 语言：Java 17
- 架构：MVP（Contract + Presenter + Activity/Fragment + Repository）
- 网络：Retrofit + OkHttp + Gson
- 最低系统：Android 7.0（API 24）
- 编译 SDK：37（AGP 9.4.0 / Gradle 9.7.1）
- UI：深色音乐社区风格，自定义 `CoverView` 绘制渐变封面与黑胶纹理

## 已实现页面

| 页面 | 说明 |
|---|---|
| 登录 / 注册 | 手机号 + 验证码，自动注册，演示验证码 `1234` |
| 推荐首页 | 推荐/关注切换、作品播放、点赞、音乐人卡片 |
| 广场 | 最新/热门/风格筛选、双列作品网格 |
| 写歌 | 4 步：灵感 → 风格参数 → 报价 → 生成/发布 |
| 播放页 | 音频播放、进度、点赞、收藏、评论、分享短链 |
| 消息 | 通知 / 私信切换，全部已读，未读角标 |
| 私信会话 | 文本发送、歌曲消息展示 |
| 我的 | 用户资料、积分、作品、认证入口、退出登录 |
| 音乐人主页 | 关注/取关、作品列表 |
| 搜索 | 作品与音乐人搜索 |
| 我的作品 | 状态筛选、审核中/未通过/草稿展示 |
| 积分中心 | 余额、套餐、充值（演示）、积分流水 |
| 音乐人认证 | 申请表单、审核状态 |

## 后端地址

默认：`http://10.0.2.2:8080/`（Android 模拟器访问宿主机）

- **模拟器**：保持默认即可。
- **真机**：在登录页点击底部「服务器：...」修改为电脑局域网地址，例如 `http://192.168.1.10:8080/`。
- 也可以修改 [app/build.gradle](file:///D:/githubProject/MusicProduct/SonicHub_android/app/build.gradle) 中的 `API_BASE_URL` 后重新构建。

后端需要先启动（详见 [SonicHub_api README](file:///D:/githubProject/MusicProduct/SonicHub_api/README.md)）：

```bash
cd d:/githubProject/MusicProduct/SonicHub_api
go run ./cmd/server
```

演示账号：手机号 `13800138000`，验证码 `1234`。

## 构建

```bash
cd d:/githubProject/MusicProduct/SonicHub_android
./gradlew.bat :app:assembleDebug
```

产物：

```text
app/build/outputs/apk/debug/app-debug.apk
```

安装：

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 架构说明

```text
com.sonichub.app
├── data
│   ├── ApiClient / ApiService        Retrofit、JWT 拦截器
│   ├── Repository                    统一响应解析与错误处理
│   ├── Session                       Token、用户、服务器地址
│   └── model/Models.java             与后端一致的 DTO
├── ui
│   ├── login       LoginContract / LoginPresenter / LoginActivity
│   ├── main        MainActivity（底部导航 + Fragment）
│   ├── discover    DiscoverContract / Presenter / Fragment / Adapter
│   ├── plaza       PlazaContract / Presenter / Fragment / Adapter
│   ├── create      CreateContract / Presenter / CreateActivity
│   ├── player      PlayerContract / Presenter / Activity / CommentAdapter
│   ├── messages    MessagesContract / Presenter / Fragment / Adapters
│   ├── chat        ChatContract / Presenter / Activity / MessageAdapter
│   ├── profile     ProfileContract / Presenter / Fragment
│   ├── artist      ArtistContract / Presenter / Activity
│   ├── works       WorksContract / Presenter / Adapter
│   ├── credits     CreditsContract / Presenter / Adapters
│   ├── verify      VerifyContract / Presenter / Activity
│   └── search      SearchContract / Presenter / Activity
├── widget/CoverView                  渐变封面 / 头像
└── util/Ui                           尺寸、Toast、时间、媒体 URL
```

## 对接的接口

| 功能 | 接口 |
|---|---|
| 登录 | `POST /api/v1/auth/login` |
| 当前用户 | `GET /api/v1/me` |
| 推荐 / 关注流 | `GET /api/v1/feed/recommend`、`/feed/following` |
| 广场 | `GET /api/v1/plaza` |
| 搜索 | `GET /api/v1/search` |
| 作品详情 | `GET /api/v1/works/{id}` |
| 点赞 / 收藏 | `POST/DELETE /api/v1/works/{id}/like`、`/collect` |
| 评论 | `GET/POST /api/v1/works/{id}/comments` |
| 关注 | `POST/DELETE /api/v1/users/{id}/follow` |
| 音乐人主页 | `GET /api/v1/musicians/{id}` |
| 报价 | `POST /api/v1/credit/quotes` |
| 生成 | `POST /api/v1/generations`、`GET /generations/{id}`、`POST /cancel` |
| 发布 | `POST /api/v1/works/{id}/publish` |
| 通知 | `GET /api/v1/notifications`、`POST /notifications/read` |
| 私信 | `GET /api/v1/conversations`、`GET/POST /conversations/{id}/messages` |
| 积分 | `GET /api/v1/me/credits`、`/me/credits/ledger`、`/credit/packages`、`/credit/recharge` |
| 认证 | `POST /api/v1/musicians/applications` |
| 分享 | `POST /api/v1/share-links` |

## 验证状态

- `./gradlew.bat :app:assembleDebug` 构建通过，已生成 debug APK。
- 已在 API 34 x86_64 模拟器安装并完成端到端验证：登录（`13800138000` / `1234`）、推荐与关注流、广场筛选、搜索、播放页点赞/评论、写歌四步（报价 → 生成 → 发布）、消息通知与私信发送、个人主页、我的作品、积分中心与账单、音乐人主页。
- 生成作品通过后端 Worker 完成 WAV 落盘，`/media/generated/*.wav` 返回 `200 audio/wav`，App 播放页可正常加载。
- 访问令牌过期时自动调用 `POST /api/v1/auth/refresh` 刷新并重放原请求；刷新失败则清理登录态。
- 联调修复：可见性字段改为后端枚举（`public`/`friends`/`private`）；后端私信写入不再对 `work_id` 使用 `NULL`。
- 后端联调路径已按 `SonicHub_api` 的实际路由实现；启动后端并配置正确地址后即可登录使用。

## 未覆盖

- 后台审核工作台、推荐策略配置（仅 App 端）
- 站外 H5 落地页、分享海报（原型中的 Web 页面）
- 推送（APNs/FCM）、支付渠道回调
- 离线缓存与弱网重试策略
