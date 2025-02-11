<div align="center">
<h1>AppRetentionHook</h1>

![stars](https://img.shields.io/github/stars/HChenX/AppRetentionHook?style=flat)
![downloads](https://img.shields.io/github/downloads/HChenX/AppRetentionHook/total)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/AppRetentionHook)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/HChenX/AppRetentionHook)](https://github.com/HChenX/AppRetentionHook/releases)
[![GitHub Release Date](https://img.shields.io/github/release-date/HChenX/AppRetentionHook)](https://github.com/HChenX/AppRetentionHook/releases)
![last commit](https://img.shields.io/github/last-commit/HChenX/AppRetentionHook?style=flat)
![language](https://img.shields.io/badge/language-java-purple)

<p><b><a href="README-en.md">English</a> | <a href="README.md">简体中文</a></b></p>
<p>Hook 系统 kill 逻辑来实现后台保活</p>
</div>

---

## ✨ 模块介绍

AppRetentionHook 是一款 **Lsp 模块**，通过 **Hook 系统 kill 逻辑** 来实现 **后台保活**。
📌 **5.1 版本是重构后的全新版本！！** 快来体验吧！

🔹 **适配系统**（5.1 版本支持范围）：

- ✅ **HyperOS V1 / V2**
- ✅ **AOSP 11-15**
- ✅ **三星 OneUI**（可用范围未知）
- 🚧 **Color OS（暂不完全适配，但可用）**

---

## 🛠 使用须知

> 📌 **为什么还会有应用被杀？**
> **本模块只拦截因系统计划导致的 kill，不能解决应用自身崩溃、自杀等行为！**

📌 **拦截的 kill 来源**（系统行为）：

- 设备空闲清理
- 进程数量限制
- 后台进程最大数量限制
- 受限应用限制
- 计划任务清理
- 待机/空进程限制

⚠ **模块不会拦截以下 kill 行为**：

- lmkd 主动 kill（内存爆满触发）
- 应用 ANR（无响应）、更新、自杀、卸载、崩溃等

💡 **模块目标**：
**已打开的应用不会因系统计划而被 kill，尽可能延长后台存活时间！**

---

## 🔧 安装与使用

📌 **请在 Lsp 中启用本模块！**

1. **安装**：下载并安装本模块
2. **激活**：进入 Lsp 选择本模块并启用
3. **勾选作用域**（不同系统）：
    - **MIUI / HyperOS**：`系统框架(system)` 和 `电量与性能(powerkeeper) [如果有]`
    - **Color OS**：`雅典娜(Athena)`、`电池`、`系统框架(system)`（5.1 版本未适配，仅可用）
    - **OneUi**：`系统框架(system)`
4. **重启设备即可！**

---

## 🌟 模块效果

✅ 经过测试，后台 App 留存时间 **大幅增长**，长时间未出现系统杀后台的现象
✅ **即使经过整晚，应用仍保持后台运行**
✅ **示例 App**（包括但不限于）：

- QQ、B 站、抖音
- GitHub（安卓）、Twitter（X）、Telegram、YouTube 等

---

## ⚠ 可能存在的问题

⚠ **由于本模块修改了系统运行逻辑，可能会带来以下影响**：

1. **系统内存管理失效**：内存不足时不会主动清理，可能导致系统卡死
2. **待机功耗增加**：影响较小，但可能稍微增加耗电量
3. **部分设备可能出现卡开机**

🚨 **强烈建议** 在使用前做好备份，以防止无法开机等极端情况！

---

## 🔍 常见问题

❓ **Q：如何使用？**
💡 A：请仔细阅读 README 说明，并确保正确设置 Lsp 作用域。

❓ **Q：与其他保活模块是否冲突？**
💡 A：是的，**请勿同时使用多个具有相同功能的模块！**
冲突模块示例：

- **Don-t-Kill**
- **Cemiuiler**（部分功能重叠）
- **A1 内存管理附加 Lsp 模块**

❓ **Q：为什么系统卡死了？**
💡 A：请检查设备的 **内存占用情况**，本模块不会主动清理内存。

❓ **Q：为什么待机功耗增加了？**
💡 A：后台应用存活时间更长，**功耗增加是正常现象**，但影响较小。

❓ **Q：为什么卡开机了？**
💡 A：部分设备可能不兼容，若出现此问题，请卸载模块并反馈！

---

## 🙏 致谢名单

💡 本模块部分代码参考以下项目，特此致谢：

| 项目名称       | 项目链接                                                                      |
|------------|---------------------------------------------------------------------------|
| Cemiuiler  | [Cemiuiler GitHub](https://github.com/Team-Cemiuiler/Cemiuiler/tree/main) |
| Don't Kill | [Don-t-Kill](https://github.com/HChenX/Don-t-Kill)                        |

📌 **翻译提供**

- **English**：焕晨HChen, ℓοѕτ οиє ⌕ — 🚫🥄(Telegram Name)
- **简体中文**：焕晨HChen

---

## 📢 项目声明

⚠ **使用本模块即代表愿意承担一切后果**！
⚠ **任何衍生项目，本项目不承担任何责任**！
⚠ **抄袭将导致项目闭源！请注明作者！**

## 🎉结尾
💖 **感谢你的支持，Enjoy your day!** 🚀
