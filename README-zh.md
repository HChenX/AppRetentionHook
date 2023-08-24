<div align="center">
<h1>AppRetentionHook</h1>

![stars](https://img.shields.io/github/stars/HChenX/AppRetentionHook?style=flat)
![downloads](https://img.shields.io/github/downloads/HChenX/AppRetentionHook/total)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/AppRetentionHook)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/HChenX/AppRetentionHook)](https://github.com/HChenX/AppRetentionHook/releases)
[![GitHub Release Date](https://img.shields.io/github/release-date/HChenX/AppRetentionHook)](https://github.com/HChenX/AppRetentionHook/releases)
![last commit](https://img.shields.io/github/last-commit/HChenX/AppRetentionHook?style=flat)
![language](https://img.shields.io/badge/language-java-purple)

<p><b><a href="README.md">English</a> | <a href="README-zh.md">简体中文</a></b></p>
<p>本模块通过Hook系统kill逻辑来实现后台保活，这是一个Lsp模块。</p>
</div>

# ✨模块介绍

* 因为这是一个Lsp模块，所以请在Lsp中激活并使用他，而不是使用Magisk安装此模块。
* #### 用法：
    * 首先：安装此Lsp模块至你的手机中。
    * 其次：点击Lsp图标进入Lsp界面，选中本模块并激活它。
    * 最后：勾选作用域`Android`，然后重启手机即可正常使用。

- 本模块通过修改系统kill逻辑来实现后台应用保活。
- 本模块专为安卓13的Miui系统设计`Miui14`，但是安卓12和11也可以使用，但是效果未知。
- 但是请注意本模块在非Miui系统的安卓13手机也可以正常使用，且具有几乎相同的效果，除非手机厂商自定义了kill逻辑。

# 🌟模块效果

- 经过个人测试，后台App留存时间大大增长，甚至在较长时间内未出现任何杀后台现象。
- 令我十分惊讶的是甚至经过一个晚上App依然在后台存活没有被杀。
- `其中包括但不限于：QQ B站 抖音 酷安 等国产软件`
- 虽然模块目前只经过小范围测试，但反馈十分良好，后台App保活效果显著。

# 💡模块说明

- 本模块通过Hook修改系统kill逻辑实现后台App保活。
- 但是正因如此本模块可能存在如下问题：

* 比如：
    * 1.系统内存管理失效，在内存较低的情况下不会主动清理内存导致的系统卡死。
    * 2.系统的待机功耗增加，但这是在所难免的不可能鱼和熊掌兼得。我认为造成的待机功耗增加是可以忽略不计的。
    * 3.使用本模块有可能导致系统无法开机卡第二屏，所以请务必做好万全准备。

- 如果你使用的不是安卓13的Miui系统，或者不是安卓13的类原生系统，而是使用例如：Flyme，ColorOs等第三方厂商的系统，他们可能包含厂商自定义的kill逻辑，对模块效果有一定影响。
- 如果你能接受上面所述的可能存在的问题，那么本模块将会为你带来不一样的惊喜。

# 🔍常见问题

- Q：如何使用？
- A：请仔细阅读README-zh.md。
- Q：模块与其他具有相同功能的Lsp模块冲突吗？
- A：那是肯定的，我们不建议你多个重复功能模块一起使用，可能带来未知后果。具有相同功能的其他模块：Don-t-Kill，Cemiuiler中的相同功能，A1内存管理附加Lsp模块等。
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

# 📢项目声明

- 任何对本项目的使用必须注明作者，抄袭是不可接受的！
- 抄袭可能导致本项目的闭源！

# 🌏免责声明

- 使用本模块即代表愿意承担一切后果。
- 任何由本项目衍生出的项目本项目不承担任何责任。

# 🎉结尾

- 感谢您愿意使用本模块！Enjoy your day! ♥️