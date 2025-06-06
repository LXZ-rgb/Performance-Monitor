package ui;

import javafx.scene.image.Image;
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
        String imagePath = "/img/" + brand + "_logo.png";
        Image logo = null;
        try {
            logo = new Image(getClass().getResourceAsStream(imagePath));
            if (logo.isError() || logo.getWidth() <= 0)
                throw new Exception();
        } catch (Exception e) {
            System.err.println("无法加载品牌Logo: " + brand + ", 使用默认Logo");
        }
        if (logo == null || logo.isError() || logo.getWidth() <= 0) {
            logo = new Image(getClass().getResourceAsStream("/img/default_logo.png"));
            LOGO_CACHE.put("default", logo);
            return logo;
        }
        LOGO_CACHE.put(brand, logo);
        return logo;
    }
}