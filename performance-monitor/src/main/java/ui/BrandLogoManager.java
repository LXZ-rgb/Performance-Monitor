package ui; // 指定包名为ui，负责界面相关的管理类

import javafx.scene.image.Image; // 导入JavaFX的Image类，用于显示品牌Logo
import java.util.HashMap; // 导入HashMap用于存储品牌与Logo的映射
import java.util.Map; // 导入Map接口

public class BrandLogoManager { // 品牌Logo管理器，根据硬件型号推断品牌并提供Logo图片

    private final Map<String, Image> logoMap; // 品牌名称到Logo图片的映射表

    public BrandLogoManager() {
        logoMap = new HashMap<>(); // 初始化映射表
        // 预加载常见品牌Logo
        logoMap.put("Intel", loadLogo("/ui/logo/intel.png"));   // Intel logo
        logoMap.put("AMD", loadLogo("/ui/logo/amd.png"));       // AMD logo
        logoMap.put("Samsung", loadLogo("/ui/logo/samsung.png"));// Samsung logo
        logoMap.put("Kingston", loadLogo("/ui/logo/kingston.png"));// Kingston logo
        // ... 可以根据实际需求继续扩展品牌和logo
    }

    // 根据型号字符串猜测品牌（简单字符串包含判断，可按需拓展更复杂判断）
    public String detectBrandFromModel(String model) {
        if (model == null) return "";
        model = model.toLowerCase(); // 转为小写便于判断
        if (model.contains("intel")) return "Intel";
        if (model.contains("amd")) return "AMD";
        if (model.contains("samsung")) return "Samsung";
        if (model.contains("kingston")) return "Kingston";
        // ... 可添加更多品牌判断
        return ""; // 未知品牌
    }

    // 获取品牌Logo图片，若找不到返回null
    public Image getBrandLogo(String brand) {
        return logoMap.getOrDefault(brand, null);
    }

    // 加载Logo图片资源
    private Image loadLogo(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Logo加载失败: " + path);
            return null;
        }
    }
}
