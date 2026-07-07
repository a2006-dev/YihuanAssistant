# 📱 YihuanAssistant

**异环游戏助手** — Android 端游戏数据查询与签到工具，为《异环》玩家提供角色信息、资源状态、签到等功能的辅助应用。

[![API](https://img.shields.io/badge/API-28%2B-brightgreen)](https://developer.android.com/studio/releases/platforms)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## ✨ 功能

- **📊 角色主页** — 查看编队角色、资源状态（体力/活力/活跃度）、探索进度
- **🔑 多账号管理** — 支持多个游戏角色切换，Token 自动保活
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

```bash
git clone https://github.com/a2006-dev/YihuanAssistant.git
cd YihuanAssistant
./gradlew assembleDebug
```

从 [Releases](../../releases) 页面下载最新 APK，或自行构建：

```bash
./gradlew installDebug
```

---

## 📖 使用说明

1. 参考 [tajiduo-server](https://github.com/a2006-dev/tajiduo-server) 部署后端服务并创建 API Key
2. 打开 App 进入「设置」页，填写服务器地址和 API Key
3. 返回首页点击「登录」，输入手机号接收验证码
4. 登录后即可查看角色状态、签到、抽卡记录等功能

---

## 🏗️ 项目结构

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
│   │   └── Models.kt            # 数据模型
│   ├── repository/
│   │   └── Repositories.kt      # 数据仓库层
│   ├── EventBus.kt              # 事件总线
│   └── Callbacks.kt             # 回调接口
├── ui/
│   ├── login/LoginScreen.kt     # 登录界面
│   ├── main/MainScreen.kt       # 主界面框架
│   ├── home/HomeScreen.kt       # 首页（角色状态 + 签到）
│   ├── character/
│   │   ├── CharacterListScreen.kt    # 角色列表
│   │   └── CharacterDetailScreen.kt  # 角色详情
│   ├── gacha/GachaScreen.kt     # 抽卡记录
│   ├── settings/SettingsScreen.kt # 设置页面
│   └── AppTheme.kt              # Material 3 主题
├── util/
│   ├── AssetUrl.kt              # 资源 URL 构建
│   ├── PreferenceUtil.kt        # SharedPreferences 工具
│   ├── CacheManager.kt          # 缓存管理
│   └── ...
```

---

## 🔧 配置说明

后端地址在 `RetrofitClient.kt` 中修改：

```kotlin
const val SERVER_BASE = "http://your-server:3000"
```

固定 API Key 需在 APP 代码和后端环境变量 `FIXED_KEY` 中设置为相同值。

---

## 📄 许可证

本项目基于 MIT 许可证开源 — 详见 [LICENSE](LICENSE) 文件。

---

*Made with ❤️ by a2006-dev*
