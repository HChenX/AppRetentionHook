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
<p>本模块通过Hook系统kill逻辑来实现后台保活，这是一个Lsp模块。</p>
</div>

# ✨模块介绍

* 因为这是一个Lsp模块，所以请在Lsp中激活并使用他，而不是使用Magisk安装此模块。
* #### 用法：
    * 首先：安装此Lsp模块至你的手机中。
    * 其次：点击Lsp图标进入Lsp界面，选中本模块并激活它。
    * 最后：勾选作用域:
    * [MIUI] `系统框架(system)`和`电量和性能(powerkeeper)[如果有]`
    * [OPPO] `雅典娜(Athena)`和`电池`和`系统框架(system)`
    * 然后重启手机即可正常使用。

- 本模块通过Hook系统kill逻辑来实现后台应用保活。
- 本模块适用于: `HyperOS(A14)` `MIUI14(A13,A12,A11)` `OPPO系统(A14,A13,A12)` `AOSP(A14,A13,A12,A11)`
  等。

# 🌟模块效果

- 经过个人测试，后台App留存时间大大增长在较长时间内未出现任何杀后台现象。
- 令我十分惊讶的是甚至经过一个晚上App依然在后台存活没有被杀死。
- `其中包括但不限于：QQ(腾讯) B站 抖音 GitHub(安卓) Twitter(X) Telegram(电报) YouTobe 等软件`

# 💡模块说明

- 由于修改了系统运行逻辑本模块可能存在如下问题：

* 比如：
    * 1.系统内存管理失效，在内存较低的情况下不会主动清理内存导致的系统卡死。
    * 2.系统的待机功耗增加，但造成的待机功耗增加是可以忽略不计的。
    * 3.使用本模块有可能导致系统无法开机卡第二屏，所以请务必做好万全准备。

- 提醒:使用例如：`OPPO` `VIVO` `华为(Huawei)`等第三方厂商的系统，他们可能包含厂商自定义的kill逻辑，对模块效果有一定影响。
- 如果你能接受上面所述的可能存在的问题，那么本模块将会为你带来不一样的惊喜。

# 🔍常见问题

- Q：如何使用？
- A：请仔细阅读README-zh.md。
- Q：模块与其他具有相同功能的Lsp模块冲突吗？
- A：那是肯定的，我们不建议你多个重复功能模块一起使用，否则可能带来未知后果。
- 具有相同功能的其他模块：Don-t-Kill，Cemiuiler中的相同功能，A1内存管理附加Lsp模块等。
- Q；为什么我的系统卡死了？
- A：因为更改了系统kill逻辑，所以请注意你手机的内存状态，不要让它爆满。
- Q：为什么我手机功耗增加了？
- A；这是不可避免的，鱼与熊掌不能兼得，我认为在合理范围内是无伤大雅的。
- Q；为什么卡开机了？
- A；可能是模块中的某个功能的原因，请卸载模块并向我反馈。

# 🙏致谢名单

- 本模块代码参考了如下项目，对此表示由衷的感谢：

|    项目名称    |                                项目链接                                |
|:----------:|:------------------------------------------------------------------:|
| Cemiuiler  | [Cemiuiler](https://github.com/Team-Cemiuiler/Cemiuiler/tree/main) |
| Don't Kill |         [Don-t-Kill](https://github.com/HChenX/Don-t-Kill)         |
|    翻译提供    |                                提供者                                 |
|  English   |  焕晨HChen , ℓοѕτ οиє ⌕ ➹ • #𝙣𝙤𝙋𝙈𝙨𝙥𝙡𝙨 • 𝕏 (Telegram Name)   |
|    简体中文    |                              焕晨HChen                               |

# 📢项目声明

- 任何对本项目的使用必须注明作者，抄袭是不可接受的！
- 抄袭可能导致本项目的闭源！

# 🌏免责声明

- 使用本模块即代表愿意承担一切后果。
- 任何由本项目衍生出的项目本项目不承担任何责任。

# 🎉结尾

- 感谢您愿意使用本模块！Enjoy your day! ♥️
