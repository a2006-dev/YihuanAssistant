# YihuanAssistant - 海特洛档案室

一个基于 Android Kotlin + Jetpack Compose 的游戏辅助工具，提供角色查看、抽卡分析、自动签到等功能。

## 功能特性

- **登录系统** — 手机号短信验证码登录，支持多账号管理
- **首页仪表盘** — 角色信息、资源状态、成就进度、探索进度一目了然
- **自动签到** — 一键签到社区，奖励直接发至邮箱
- **角色图鉴** — 网格列表查看所有角色，详情页展示属性、弧盘、驱动套装、技能
- **抽卡分析** — 统计抽卡数据，展示抽数、欧非标签、超过玩家百分比
- **Token 保活** — 与服务器绑定，自动保持登录状态
- **系统通知** — 服务器推送通知自动弹窗提醒
- **公告系统** — 服务端发布的公告在首页直接展示
- **问题反馈** — 内置反馈功能，提交到服务端后台
- **深色/浅色主题** — 支持跟随系统、手动切换
- **图片 CDN** — 角色头像、弧盘图片均从 CDN 加载

## 快速开始

### 环境要求

- Android 8.0+ (API 26+)
- Android Studio 或 AndroidIDE
- JDK 11

### 编译运行

```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本
./gradlew assembleRelease
```

APK 输出路径: `app/build/outputs/apk/`

### 首次配置

1. 安装 APP
2. 输入服务器管理员提供的 API Key
3. 输入手机号获取验证码登录
4. 进入设置页绑定 API Key 到当前游戏账号

## 架构说明

```
app/
├── src/main/java/com/yh/assistant/
│   ├── data/
│   │   ├── api/           # 网络接口定义 (Retrofit)
│   │   ├── model/         # 数据模型
│   │   └── repository/    # 仓库层
│   ├── ui/
│   │   ├── home/          # 首页仪表盘
│   │   ├── character/     # 角色图鉴 + 详情
│   │   ├── gacha/         # 抽卡分析
│   │   ├── settings/      # 设置 + 反馈 + 绑定
│   │   ├── login/         # 登录
│   │   └── main/          # 主框架导航
│   └── util/              # 工具类
└── src/main/res/           # 资源文件
```

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **网络**: Retrofit + OkHttp
- **图片**: Coil (Compose)
- **架构**: MVVM (ViewModel + Repository)
- **最低支持**: Android 8.0 (API 26)

## 依赖项

- [compose-bom:2022.12.00](https://developer.android.com/jetpack/androidx/releases/compose)
- [retrofit2:2.9.0](https://square.github.io/retrofit/)
- [okhttp3:4.10.0](https://square.github.io/okhttp/)
- [coil:2.2.2](https://coil-kt.github.io/coil/) — Compose 图片加载
- [gson:2.10.1](https://github.com/google/gson)

## 注意事项

- 此 APP 需配合 [tajiduo-server](https://github.com/a2006-dev/tajiduo-server) 后端使用
- APP 不直接与游戏服务器通信，所有请求通过后端中转
- 请遵守游戏服务条款

## License

MIT
