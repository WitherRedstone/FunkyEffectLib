package com.chinaex123.funky_effect_lib.client.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 矿石颜色配置类 - 统一管理所有矿石的高亮颜色
 * <p>
 * 支持原版矿石和模组矿石的颜色配置
 * 颜色可通过配置文件自定义，格式支持十六进制（#RRGGBB）
 * 模组矿石支持通过方块名称关键词匹配颜色
 */
public class OreColorConfig {

    /** 存储方块对应的颜色 */
    private static final Map<Block, OreColor> ORE_COLORS = new HashMap<>();
    /** 存储通过名称匹配的颜色 */
    private static final Map<String, OreColor> NAME_PATTERN_COLORS = new HashMap<>();
    /** 默认颜色（白色） */
    private static final OreColor DEFAULT_COLOR = new OreColor(255, 255, 255);

    static {
        initVanillaOres();
        loadCustomOreColors();
    }

    /**
     * 初始化原版矿石颜色
     * 从客户端配置读取颜色值，若未配置则使用默认颜色
     */
    private static void initVanillaOres() {
        // 铁矿石 - 从配置读取，默认灰色
        OreColor ironColor = parseColor(ClientConfig.ORE_COLOR_IRON.get(), new OreColor(200, 180, 160));
        register(Blocks.IRON_ORE, ironColor);
        register(Blocks.DEEPSLATE_IRON_ORE, ironColor);

        // 金矿石 - 从配置读取，默认金黄色
        OreColor goldColor = parseColor(ClientConfig.ORE_COLOR_GOLD.get(), new OreColor(255, 215, 0));
        register(Blocks.GOLD_ORE, goldColor);
        register(Blocks.DEEPSLATE_GOLD_ORE, goldColor);
        register(Blocks.NETHER_GOLD_ORE, goldColor);

        // 钻石矿石 - 从配置读取，默认亮蓝色
        OreColor diamondColor = parseColor(ClientConfig.ORE_COLOR_DIAMOND.get(), new OreColor(0, 191, 255));
        register(Blocks.DIAMOND_ORE, diamondColor);
        register(Blocks.DEEPSLATE_DIAMOND_ORE, diamondColor);

        // 绿宝石矿石 - 从配置读取，默认鲜绿色
        OreColor emeraldColor = parseColor(ClientConfig.ORE_COLOR_EMERALD.get(), new OreColor(80, 255, 100));
        register(Blocks.EMERALD_ORE, emeraldColor);
        register(Blocks.DEEPSLATE_EMERALD_ORE, emeraldColor);

        // 青金石矿石 - 从配置读取，默认深蓝色
        OreColor lapisColor = parseColor(ClientConfig.ORE_COLOR_LAPIS.get(), new OreColor(0, 100, 255));
        register(Blocks.LAPIS_ORE, lapisColor);
        register(Blocks.DEEPSLATE_LAPIS_ORE, lapisColor);

        // 红石矿石 - 从配置读取，默认红色
        OreColor redstoneColor = parseColor(ClientConfig.ORE_COLOR_REDSTONE.get(), new OreColor(255, 0, 0));
        register(Blocks.REDSTONE_ORE, redstoneColor);
        register(Blocks.DEEPSLATE_REDSTONE_ORE, redstoneColor);

        // 煤矿石 - 从配置读取，默认深灰色
        OreColor coalColor = parseColor(ClientConfig.ORE_COLOR_COAL.get(), new OreColor(40, 40, 40));
        register(Blocks.COAL_ORE, coalColor);
        register(Blocks.DEEPSLATE_COAL_ORE, coalColor);

        // 铜矿石 - 从配置读取，默认橙色
        OreColor copperColor = parseColor(ClientConfig.ORE_COLOR_COPPER.get(), new OreColor(255, 140, 0));
        register(Blocks.COPPER_ORE, copperColor);
        register(Blocks.DEEPSLATE_COPPER_ORE, copperColor);

        // 下界石英矿石 - 从配置读取，默认淡粉色
        OreColor quartzColor = parseColor(ClientConfig.ORE_COLOR_QUARTZ.get(), new OreColor(255, 200, 255));
        register(Blocks.NETHER_QUARTZ_ORE, quartzColor);

        // 远古残骸 - 从配置读取，默认棕色
        OreColor debrisColor = parseColor(ClientConfig.ORE_COLOR_DEBRIS.get(), new OreColor(186, 145, 117));
        register(Blocks.ANCIENT_DEBRIS, debrisColor);
    }

    /**
     * 加载自定义模组矿石颜色配置
     * 从配置文件中读取格式为 "modid:ore_name=R,G,B" 或 "ore_name=R,G,B" 的条目
     */
    private static void loadCustomOreColors() {
        for (Object configEntryObj : ClientConfig.CUSTOM_ORE_COLORS.get()) {
            try {
                String configEntry = configEntryObj.toString();
                // 格式: "modid:ore_name=#RRGGBB" 或 "ore_name=#RRGGBB"
                String[] parts = configEntry.split("=");
                if (parts.length != 2) continue;

                String orePattern = parts[0].trim().toLowerCase();
                OreColor color = parseColor(parts[1].trim(), null);

                if (color != null) {
                    registerNamePattern(orePattern, color);
                }
            } catch (Exception e) {
                // 忽略格式错误的配置项，不中断加载
            }
        }
    }

    /**
     * 解析颜色字符串
     * 支持十六进制格式：如 "#RRGGBB" 或 "0xRRGGBB"
     *
     * @param colorStr 颜色字符串
     * @param defaultColor 解析失败时返回的默认颜色
     * @return 解析后的颜色对象
     */
    private static OreColor parseColor(String colorStr, OreColor defaultColor) {
        if (colorStr == null || colorStr.isEmpty()) {
            return defaultColor;
        }

        try {
            // 支持十六进制格式 "#RRGGBB" 或 "0xRRGGBB"
            String hex = colorStr.trim();
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            } else if (hex.startsWith("0x") || hex.startsWith("0X")) {
                hex = hex.substring(2);
            } else {
                return defaultColor;
            }

            if (hex.length() == 6) {
                int color = Integer.parseInt(hex, 16);
                return new OreColor((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
            }
        } catch (Exception e) {
            // 解析失败，返回默认颜色
        }

        return defaultColor;
    }

    /**
     * 注册方块颜色（精确匹配）
     *
     * @param block 方块
     * @param color 颜色
     */
    public static void register(Block block, OreColor color) {
        ORE_COLORS.put(block, color);
    }

    /**
     * 注册名称匹配规则（关键词匹配）
     *
     * @param pattern 匹配关键词（不区分大小写）
     * @param color 颜色
     */
    public static void registerNamePattern(String pattern, OreColor color) {
        NAME_PATTERN_COLORS.put(pattern.toLowerCase(), color);
    }

    /**
     * 获取方块颜色
     * 优先精确匹配，其次关键词匹配，最后返回默认颜色
     *
     * @param block 目标方块
     * @return 对应的颜色
     */
    public static OreColor getColor(Block block) {
        // 先检查精确匹配
        if (ORE_COLORS.containsKey(block)) {
            return ORE_COLORS.get(block);
        }

        // 通过方块注册名进行关键词匹配
        ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(block);
        String path = registryName.getPath().toLowerCase();
        for (Map.Entry<String, OreColor> entry : NAME_PATTERN_COLORS.entrySet()) {
            if (path.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // 返回默认颜色
        return DEFAULT_COLOR;
    }

    /**
     * 颜色记录类
     * 存储RGB颜色值，并提供转换为浮点值的方法
     */
    public record OreColor(int r, int g, int b) {
        public float getRedFloat() {
            return r / 255.0f;
        }

        public float getGreenFloat() {
            return g / 255.0f;
        }

        public float getBlueFloat() {
            return b / 255.0f;
        }

        @Override
        public @NotNull String toString() {
            return String.format("RGB(%d, %d, %d)", r, g, b);
        }
    }
}