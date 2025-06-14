# 💻 电脑性能监视器 (Hardware Performance Monitor)

<div align="center">
  <img src="https://img.shields.io/badge/功能-品牌识别-green?style=for-the-badge" alt="品牌识别"/>
  <img src="https://img.shields.io/badge/优化-图片缓存-blue?style=for-the-badge" alt="资源缓存"/>
  <img src="https://img.shields.io/badge/平台-Windows%20/%20macOS%20/%20Linux-1976d2?style=for-the-badge" alt="平台"/>
</div>

---

<div align="center">

<img src="https://img.shields.io/github/stars/LXZ-rgb/Performance-Monitor?style=social" alt="GitHub stars" />  
<br/>
<b>一款面向开发者与团队的跨平台性能监视和数据可视化工具</b>
</div>

---

## 🚀 功能特性

- 🏷️ <b>智能品牌识别：</b>
  - 根据硬件型号关键字自动匹配品牌
  - 支持 Intel / AMD / Samsung / WD / Seagate / Kingston 等主流品牌
- 🖼️ <b>动态 Logo 加载：</b>
  - 自动加载匹配的品牌 Logo
  - 内置缓存机制提升性能
  - 优雅的失败处理（默认 Logo）
- 📈 <b>数据可视化与异常监控：</b>
  - 实时折线图展示 CPU / 内存 / 磁盘 / 温度等信息
  - 一键导出异常数据报表（Excel）
- 🔒 <b>本地数据存储：</b>
  - 自动在用户目录建立数据库，无需手动配置
  - 所有数据私有化安全存储

---

## 🏗️ 技术栈

- Java 8+
- JavaFX（桌面 UI）
- OSHI（硬件信息采集）
- SQLite（本地数据库）
- Apache POI（Excel 导出）

---

## 🗂️ 资源管理系统

### 品牌识别逻辑

```java
// 品牌关键字映射
private static final Map<String, String> BRAND_MAPPING = new HashMap<>();
static {
    BRAND_MAPPING.put("intel", "intel");
    BRAND_MAPPING.put("amd", "amd");
    BRAND_MAPPING.put("samsung", "samsung");
    BRAND_MAPPING.put("western digital", "wd");
    BRAND_MAPPING.put("seagate", "seagate");
    BRAND_MAPPING.put("kingston", "kingston");
    // 更多品牌...
}
```

---

## 🌟 快速开始

1. **克隆仓库**
   ```bash
   git clone https://github.com/LXZ-rgb/Performance-Monitor.git
   ```
2. **导入到 IDE**（如 IntelliJ IDEA、Eclipse）
3. **运行主类**  
   `ui.MainApp`
4. **体验高效性能监视与可视化！**

---

## 📚 项目文档与扩展

- [团队博客](https://lxz-rgb.github.io/Performance-Monitor/)
- [项目源码与结构介绍](docs/code.md)
- [团队介绍](docs/about.md)

---

## 💬 反馈与交流

- [提交 Issue](https://github.com/LXZ-rgb/Performance-Monitor/issues)
- 邮箱：lxz-rgb@proton.me

---

<div align="center" style="margin-top:2em;">
  <img src="https://img.shields.io/badge/欢迎Star和贡献-blueviolet?style=for-the-badge" height="32" alt="欢迎Star" />  
  <br/>
  <em>让性能可见，让成长同行。</em>
</div>
