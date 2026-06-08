package com.chinaex123.funky_effect_lib.client.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Map;

/** 矿石颜色配置类 - 统一管理所有矿石的高亮颜色 **/
public class OreColorConfig {

    // 存储方块对应的颜色
    private static final Map<Block, OreColor> ORE_COLORS = new HashMap<>();

    // 存储通过名称匹配的颜色（用于模组矿石）
    private static final Map<String, OreColor> NAME_PATTERN_COLORS = new HashMap<>();

    // 默认颜色（白色）
    private static final OreColor DEFAULT_COLOR = new OreColor(255, 255, 255);

    static {
        initVanillaOres();
        initNamePatterns();
    }

    /** 初始化原版矿石颜色 */
    private static void initVanillaOres() {
        // 铁矿石 - 灰色
        OreColor ironColor = new OreColor(200, 180, 160);
        register(Blocks.IRON_ORE, ironColor);
        register(Blocks.DEEPSLATE_IRON_ORE, ironColor);

        // 金矿石 - 金黄色
        OreColor goldColor = new OreColor(255, 215, 0);
        register(Blocks.GOLD_ORE, goldColor);
        register(Blocks.DEEPSLATE_GOLD_ORE, goldColor);
        register(Blocks.NETHER_GOLD_ORE, goldColor);

        // 钻石矿石 - 亮蓝色
        OreColor diamondColor = new OreColor(0, 191, 255);
        register(Blocks.DIAMOND_ORE, diamondColor);
        register(Blocks.DEEPSLATE_DIAMOND_ORE, diamondColor);

        // 绿宝石矿石 - 鲜绿色
        OreColor emeraldColor = new OreColor(80, 255, 100);
        register(Blocks.EMERALD_ORE, emeraldColor);
        register(Blocks.DEEPSLATE_EMERALD_ORE, emeraldColor);

        // 青金石矿石 - 深蓝色
        OreColor lapisColor = new OreColor(0, 100, 255);
        register(Blocks.LAPIS_ORE, lapisColor);
        register(Blocks.DEEPSLATE_LAPIS_ORE, lapisColor);

        // 红石矿石 - 红色
        OreColor redstoneColor = new OreColor(255, 0, 0);
        register(Blocks.REDSTONE_ORE, redstoneColor);
        register(Blocks.DEEPSLATE_REDSTONE_ORE, redstoneColor);

        // 煤矿石 - 深灰色
        OreColor coalColor = new OreColor(40, 40, 40);
        register(Blocks.COAL_ORE, coalColor);
        register(Blocks.DEEPSLATE_COAL_ORE, coalColor);

        // 铜矿石 - 橙色
        OreColor copperColor = new OreColor(255, 140, 0);
        register(Blocks.COPPER_ORE, copperColor);
        register(Blocks.DEEPSLATE_COPPER_ORE, copperColor);

        // 下界石英矿石 - 淡粉色
        OreColor quartzColor = new OreColor(255, 200, 255);
        register(Blocks.NETHER_QUARTZ_ORE, quartzColor);

        // 远古残骸 - 棕色
        OreColor debrisColor = new OreColor(186,145,117);
        register(Blocks.ANCIENT_DEBRIS, debrisColor);
    }

    /** 初始化名称匹配规则（用于模组矿石的自动识别） */
    private static void initNamePatterns() {
        registerNamePattern("iron", new OreColor(200, 180, 160));
        registerNamePattern("gold", new OreColor(255, 215, 0));
        registerNamePattern("diamond", new OreColor(0, 191, 255));
        registerNamePattern("emerald", new OreColor(80, 255, 100));
        registerNamePattern("lapis", new OreColor(0, 100, 255));
        registerNamePattern("redstone", new OreColor(255, 0, 0));
        registerNamePattern("coal", new OreColor(40, 40, 40));
        registerNamePattern("copper", new OreColor(255, 140, 0));
        registerNamePattern("quartz", new OreColor(255, 200, 255));
        registerNamePattern("debris", new OreColor(186,145,117));
        registerNamePattern("ancient", new OreColor(186,145,117));
        registerNamePattern("tin", new OreColor(192, 192, 192)); // 锡 - 银灰色
        registerNamePattern("silver", new OreColor(192, 192, 192)); // 银 - 银灰色
        registerNamePattern("lead", new OreColor(128, 128, 128)); // 铅 - 灰色
        registerNamePattern("uranium", new OreColor(0, 255, 0)); // 铀 - 亮绿色
        registerNamePattern("aluminum", new OreColor(220, 220, 220)); // 铝 - 银白色
        registerNamePattern("nickel", new OreColor(160, 160, 164)); // 镍 - 银灰色
        registerNamePattern("platinum", new OreColor(229, 228, 226)); // 铂 - 铂金色
        registerNamePattern("mithril", new OreColor(0, 255, 255)); // 秘银 - 青色
        registerNamePattern("adamantine", new OreColor(128, 0, 255)); // 精金 - 紫色
        registerNamePattern("cobalt", new OreColor(0, 70, 255)); // 钴 - 钴蓝色
        registerNamePattern("ardite", new OreColor(255, 100, 0)); // 阿迪特 - 橙色
    }

    /** 注册方块颜色 */
    public static void register(Block block, OreColor color) {
        ORE_COLORS.put(block, color);
    }

    /** 注册名称匹配规则 */
    public static void registerNamePattern(String pattern, OreColor color) {
        NAME_PATTERN_COLORS.put(pattern.toLowerCase(), color);
    }

    /** 获取方块颜色 */
    public static OreColor getColor(Block block) {
        // 先检查精确匹配
        if (ORE_COLORS.containsKey(block)) {
            return ORE_COLORS.get(block);
        }

        // 通过方块注册名匹配
        ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(block);
        if (registryName != null) {
            String path = registryName.getPath().toLowerCase();
            for (Map.Entry<String, OreColor> entry : NAME_PATTERN_COLORS.entrySet()) {
                if (path.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }

        // 返回默认颜色
        return DEFAULT_COLOR;
    }

    /** 颜色内部类 */
    public static class OreColor {
        public final int r;
        public final int g;
        public final int b;

        public OreColor(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

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
        public String toString() {
            return String.format("RGB(%d, %d, %d)", r, g, b);
        }
    }
}