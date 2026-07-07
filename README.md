# 📱 YihuanAssistant

**异环游戏助手** — Android 端游戏数据查询与签到工具，为《异环》玩家提供角色信息、资源状态、签到等功能的辅助应用。

[![API](https://img.shields.io/badge/API-28%2B-brightgreen)](https://developer.android.com/studio/releases/platforms)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## ✨ 功能

- **📊 角色主页** — 查看编队角色、资源状态（体力/活力/活跃度）、探索进度数据
- **🔑 多账号管理** — 支持多个游戏角色切换，Token 自动保活
- **🎯 角色评分** — 根据驱动盘副词条权重，对角色装备进行评分（主词条60分+副词条40分）
- **🎴 抽卡记录** — 查看抽卡历史与统计
- **📋 签到系统** — 每日塔吉多签到，奖励邮件领取
- **🏠 房产与载具** — 查看已拥有房产和载具状态
- **🏆 成就进度** — 追踪成就完成率
- **⚙️ 设置** — API Key 绑定、Token 保活配置、主题切换

---

## 📋 前置条件

| 条件 | 说明 |
|------|------|
| **Android 版本** | Android 9.0 (API 28) 或更高 |
| **后端服务** | 需要部署 [tajiduo-server](https://github.com/a2006-dev/tajiduo-server) 作为代理后端 |
| **API Key** | 在管理后台创建 API Key 后填入 App 设置页 |

---

## 🚀 快速开始

### 克隆并构建

```bash
git clone https://github.com/a2006-dev/YihuanAssistant.git
cd YihuanAssistant
./gradlew assembleDebug
```

### 直接安装 APK

从 [Releases](../../releases) 页面下载最新 APK，或自行构建：

```bash
./gradlew installDebug
```

---

## 📖 使用说明

### 第一步：部署后端

参考 [tajiduo-server](https://github.com/a2006-dev/tajiduo-server) 部署后端服务，并创建一个 API Key。

### 第二步：配置 APP

1. 打开 App，进入「设置」页面
2. 在「服务器地址」填写你的后端地址（如 `http://your-server:3000`）
3. 在「API Key」填写管理后台创建的 Key
4. 返回首页，点击「登录」输入手机号接收验证码

### 第三步：使用功能

- **首页** — 查看角色状态、资源、签到
- **角色页** — 查看角色详情、装备评分
- **抽卡页** — 查看抽卡记录
- **设置页** — 账号管理、Token 保活配置

---

## 🏗️ 技术架构

### 项目结构

```
app/src/main/java/com/yh/assistant/
├── MainActivity.kt              # 入口 Activity
├── App.kt                       # Application 类
├── data/
│   ├── api/
│   │   ├── ApiInterfaces.kt     # Retrofit API 接口定义
│   │   ├── RetrofitClient.kt    # Retrofit 网络客户端
│   │   └── ProxyManager.kt     # Token 保活代理管理
│   ├── model/
│   │   └── Models.kt            # 数据模型定义
│   ├── repository/
│   │   └── Repositories.kt      # 数据仓库层
│   ├── EventBus.kt              # 事件总线
│   └── Callbacks.kt             # 回调接口
├── ui/
│   ├── login/
│   │   └── LoginScreen.kt       # 登录界面
│   ├── main/
│   │   └── MainScreen.kt        # 主界面框架
│   ├── home/
│   │   └── HomeScreen.kt        # 首页（角色状态 + 签到）
│   ├── character/
│   │   ├── CharacterListScreen.kt    # 角色列表
│   │   └── CharacterDetailScreen.kt  # 角色详情 + 评分
│   ├── gacha/
│   │   └── GachaScreen.kt       # 抽卡记录
│   ├── settings/
│   │   └── SettingsScreen.kt    # 设置页面
│   └── AppTheme.kt              # Material 3 主题
├── util/
│   ├── AssetUrl.kt              # 资源 URL 构建
│   ├── PreferenceUtil.kt        # SharedPreferences 工具
│   ├── CacheManager.kt          # 缓存管理
│   ├── ImageCacheUtil.kt        # 图片缓存
│   ├── ShareRenderUtil.kt       # 分享渲染工具
│   └── GachaTitleUtil.kt        # 抽卡标题工具
```

### 关键依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| Jetpack Compose | BOM 2024.x | 声明式 UI 框架 |
| Retrofit2 | 2.9.0 | HTTP 网络请求 |
| Coil | 2.x | 异步图片加载 |
| Material 3 | — | Material You 设计语言 |

---

## 🔧 配置说明

### 后端地址

在 `RetrofitClient.kt` 中修改 `SERVER_BASE`：

```kotlin
const val SERVER_BASE = "http://your-server:3000"
```

### 固定 API Key

如使用固定 Key（不通过管理后台创建），需在 APP 代码中修改：

```kotlin
// SettingsScreen.kt 和 MainScreen.kt 中的固定 Key 字符串
```

同时必须在后端通过环境变量 `FIXED_KEY` 设置相同的值。

---

## 🖼️ 截图

> *(待补充)*

---

## ⚠️ 已知问题

1. **签到状态偶发不更新** — 签到请求成功后 APP 端 UI 未及时刷新为「已签到」，点击后转圈但无反馈（已在服务端修复签到 API 路径）
2. **Token 过期后需重新登录** — accessToken 失效后未自动拉起登录流程
3. **名字映射缓存问题** — APP 进程存活时不重新拉取服务端映射数据（已修复：移除全局 `nameCacheLoaded` 标记）

---

## 📄 许可证

本项目基于 MIT 许可证开源 — 详见 [LICENSE](LICENSE) 文件。

---

*Made with ❤️ by a2006-dev*
