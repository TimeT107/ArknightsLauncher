# Arknights Launcher

一个仿明日方舟风格的 Android 桌面启动器。

## 功能

- 🎮 明日方舟风格 UI（ArkTerminal / 编队 / 干员 / 采购中心等）
- 🌗 白天/夜晚自动切换配色
- 🎡 陀螺仪视差效果
- 📱 Dock 栏 4 页轮换（应用 / 媒体控制 / 系统面板 / 游戏入口）
- 🎵 内置媒体播放控制
- 🔋 实时系统状态（CPU / 内存 / 存储 / 电量）
- 📊 应用列表（横向网格 + 长按快捷菜单）
- 🎨 自定义快捷入口（可绑定任意应用）
- 🔔 无障碍服务支持下滑呼出通知中心/控制中心
- 🔒 双击空白处锁屏

## 截图

（这里放几张手机截图）

## 环境要求

- Android 8.0 (API 26) 及以上
- 编译：Android Studio + AGP 8.x
- Kotlin 2.0+

## 编译

```bash
git clone https://github.com/TimeT107/ArknightsLauncher.git
cd ArknightsLauncher
./gradlew assembleDebug
```

## 使用

1. 安装后进入系统设置，把默认桌面改成 **Arknights Launcher**
2. 长按 Dock 图标可绑定应用
3. 首次下滑呼出面板需要在无障碍里开启服务

## License

代码部分基于 [MIT License](LICENSE) 开源。

---

**素材版权声明**

本项目使用了《明日方舟》（Arknights）相关的角色立绘、UI 素材、字体等，
相关版权归 **鹰角网络（Hypergryph）** 所有。

- 代码：MIT License，可自由使用、修改、分发
- 素材：仅供学习交流，**不得用于商业用途**
- 本项目与鹰角网络无任何关联，不代表官方立场

## 缺失的素材

克隆后你会发现 `res/drawable/` 和 `res/font/` 里缺少部分文件
（明日方舟素材、字体等）。这些素材版权归鹰角网络所有，
请自行准备，或使用占位图替代：

- `assistant.png` —— 助理立绘
- `bg_day.png` / `bg_night.png` —— 背景图
- `power_logo.png` / `terminal_deco.png` —— 装饰图
- `team_castle.png` / `operator_deco.png` / ... —— 各板块图标
- `hkstw12.ttf` —— 华康少女文字
- `breaking_news.ttf` —— 新闻条字体
