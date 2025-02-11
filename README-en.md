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
<p>Hook system kill logic to implement background keep alive</p>
</div>

---

## ✨ Module Introduction

AppRetentionHook is an **LSP module** that implements **background retention** by hooking into the system’s kill logic.
📌 **Version 5.1 is a completely refactored new version!!** Give it a try!

🔹 **Supported Systems** (for version 5.1):

- ✅ **HyperOS V1 / V2**
- ✅ **AOSP 11-15**
- ✅ **Samsung OneUI** (Usage scope is unknown)
- 🚧 **Color OS** (Not fully adapted yet, but usable)

---

## 🛠 Usage Notice

> 📌 **Why might some applications still be killed?**
> **This module only intercepts kills triggered by system scheduling; it cannot address application crashes, self-termination, or other similar behaviors!**

📌 **Intercepted Kill Sources (system behaviors):**

- Idle device cleanup
- Process count limitations
- Maximum background process restrictions
- Restricted application policies
- Scheduled task cleanup
- Doze/empty process restrictions

⚠ **The module does NOT intercept the following kill behaviors:**

- Active killing by lmkd (triggered by memory overload)
- Application ANR (not responding), updates, self-termination, uninstallation, crashes, etc.

💡 **Module Objective:**
**Opened applications will not be killed due to system scheduling, thereby prolonging their background retention as much as possible!**

---

## 🔧 Installation and Usage

📌 **Please enable this module within LSP!**

1. **Installation**: Download and install this module.
2. **Activation**: Open LSP, select this module, and enable it.
3. **Select the applicable scopes** (depending on the system):
    - **MIUI / HyperOS**: `System Framework (system)` and `Battery & Performance (powerkeeper)` [if available]
    - **Color OS**: `Athena`, `Battery`, and `System Framework (system)` (Note: Version 5.1 is not fully adapted for Color OS, but it is usable)
    - **OneUI**: `System Framework (system)`
4. **Restart your device!**

---

## 🌟 Module Effects

✅ Testing shows that the background retention time of apps has **significantly increased**, with no system-initiated app kills observed over extended periods.
✅ **Even after an entire night, apps continue to run in the background.**
✅ **Example Apps** (including but not limited to):

- QQ, Bilibili, Douyin
- GitHub (Android), Twitter (X), Telegram, YouTube, etc.

---

## ⚠ Potential Issues

⚠ **Due to modifications in the system’s operational logic, this module may have the following impacts:**

1. **Failure of system memory management**: When memory is low, automatic cleanup will not occur, which may result in system freezes.
2. **Increased standby power consumption**: Although the impact is minor, battery usage may be slightly higher.
3. **Some devices may experience boot hang issues.**

🚨 **Strongly Recommended**: Please ensure you have a backup before using this module to avoid extreme issues such as failure to boot!

---

## 🔍 Frequently Asked Questions

❓ **Q: How do I use this module?**
💡 A: Please carefully read the README and ensure that the correct LSP scopes are configured.

❓ **Q: Does this module conflict with other retention modules?**
💡 A: Yes, **please do not use multiple modules with the same functionality simultaneously!**
Examples of conflicting modules include:
- **Don-t-Kill**
- **Cemiuiler** (overlapping functionalities)
- **A1 Memory Management LSP Module**

❓ **Q: Why is my system freezing?**
💡 A: Please check your device's **memory usage**. This module does not perform automatic memory cleanup.

❓ **Q: Why has my standby power consumption increased?**
💡 A: With apps staying alive in the background for longer, **increased power consumption is a normal phenomenon**, though the impact is minimal.

❓ **Q: Why does my device hang during boot?**
💡 A: Some devices may be incompatible. If you encounter this issue, please uninstall the module and provide feedback.

---

## 🙏 Acknowledgments

💡 Some parts of this module's code reference the following projects. Special thanks to:

| Project Name   | Project Link                                                                      |
|----------------|-----------------------------------------------------------------------------------|
| Cemiuiler      | [Cemiuiler GitHub](https://github.com/Team-Cemiuiler/Cemiuiler/tree/main)           |
| Don't Kill     | [Don-t-Kill](https://github.com/HChenX/Don-t-Kill)                                 |

📌 **Translation Provided By:**

- **English**: HChen (焕晨HChen), ℓοѕτ οиє ⌕ — 🚫🥄 (Telegram Name)
- **Simplified Chinese**: HChen (焕晨HChen)

---

## 📢 Project Disclaimer

⚠ **By using this module, you agree to assume all risks and consequences!**
⚠ **This project is not responsible for any derivative projects!**
⚠ **Plagiarism will result in the project becoming closed source! Please attribute the author!**

---

## 🎉 Conclusion

💖 **Thank you for your support. Enjoy your day!** 🚀
