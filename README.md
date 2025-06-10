# 电脑性能监视器 (Hardware Performance Monitor)

![品牌识别](https://img.shields.io/badge/功能-品牌识别-green)
![资源缓存](https://img.shields.io/badge/优化-图片缓存-blue)

## 功能特性更新
- 🏷️ **智能品牌识别**：
  - 根据硬件型号关键字自动匹配品牌
  - 支持Intel/AMD/Samsung/WD/Seagate/Kingston等主流品牌
- 🖼️ **动态Logo加载**：
  - 自动加载匹配的品牌Logo
  - 内置缓存机制提升性能
  - 优雅的失败处理（默认Logo）

## 资源管理系统
### 品牌识别逻辑
```java
// 品牌关键字映射
private static final Map<String, String> BRAND_MAPPING = new HashMap<>();
static {
    BRAND_MAPPING.put("intel", "intel");
    BRAND_MAPPING.put("amd", "amd");
    // 更多品牌...
}
