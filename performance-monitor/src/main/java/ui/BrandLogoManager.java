package ui;

import javafx.scene.image.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BrandLogoManager {
    private static final Map<String, String> BRAND_MAPPING = new HashMap<>();
    private static final Map<String, Image> LOGO_CACHE = new HashMap<>();
    static {
        BRAND_MAPPING.put("intel", "intel");
        BRAND_MAPPING.put("amd", "amd");
        BRAND_MAPPING.put("samsung", "samsung");
        BRAND_MAPPING.put("western digital", "wd");
        BRAND_MAPPING.put("seagate", "seagate");
        BRAND_MAPPING.put("kingston", "kingston");
    }

    public String detectBrandFromModel(String model) {
        if (model == null)
            return "default";
        String lowerModel = model.toLowerCase();
        for (Map.Entry<String, String> entry : BRAND_MAPPING.entrySet()) {
            if (lowerModel.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "default";
    }

    public Image getBrandLogo(String brand) {
        if (LOGO_CACHE.containsKey(brand))
            return LOGO_CACHE.get(brand);
        
        try {
            // 使用URL加载资源，确保在打包后也能正确访问
            URL resourceUrl = getClass().getResource("/img/" + brand + "_logo.png");
            if (resourceUrl == null) {
                throw new Exception("品牌Logo资源未找到: " + brand);
            }
            
            Image logo = new Image(resourceUrl.toExternalForm());
            LOGO_CACHE.put(brand, logo);
            return logo;
        } catch (Exception e) {
            System.err.println("无法加载品牌Logo: " + brand + ", 使用默认Logo. 错误: " + e.getMessage());
            
            // 加载默认logo
            URL defaultUrl = getClass().getResource("/img/default_logo.png");
            if (defaultUrl != null) {
                Image defaultLogo = new Image(defaultUrl.toExternalForm());
                LOGO_CACHE.put("default", defaultLogo);
                return defaultLogo;
            } else {
                System.err.println("严重错误: 默认Logo也未找到!");
                return null;
            }
        }
    }
}