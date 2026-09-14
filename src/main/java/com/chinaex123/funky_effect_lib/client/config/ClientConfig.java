package com.chinaex123.funky_effect_lib.client.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ClientConfig {

    // 通用HUD配置
    public static final ForgeConfigSpec.DoubleValue GLOBAL_SCALE;
    public static final ForgeConfigSpec.IntValue HUD_PADDING;
    public static final ForgeConfigSpec.IntValue HUD_DYNAMIC_MAX_DISPLAY_COUNT;
    public static final ForgeConfigSpec.DoubleValue HUD_LAYOUT_RELATIVE_X;
    public static final ForgeConfigSpec.DoubleValue HUD_LAYOUT_RELATIVE_Y;
    public static final ForgeConfigSpec.IntValue HUD_LAYOUT_SPACING;
    public static final ForgeConfigSpec.DoubleValue HUD_LAYOUT_MAX_SCREEN_HEIGHT_RATIO;

    // 魂燃
    public static final ForgeConfigSpec.ConfigValue<String> SOULBURN_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> SOULBURN_COLOR_BACKGROUND;

    // 冰霜护甲
    public static final ForgeConfigSpec.ConfigValue<String> FROST_ARMOR_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> FROST_ARMOR_COLOR_BACKGROUND;

    // 织造铠甲
    public static final ForgeConfigSpec.ConfigValue<String> WOVEN_MAIL_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> WOVEN_MAIL_COLOR_BACKGROUND;

    // 弥漫暗影
    public static final ForgeConfigSpec.ConfigValue<String> PERVADING_DARKNESS_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> PERVADING_DARKNESS_COLOR_BACKGROUND;

    // 蔓延黑暗
    public static final ForgeConfigSpec.ConfigValue<String> CREEPING_DARKNESS_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> CREEPING_DARKNESS_COLOR_BACKGROUND;

    // 电光充能
    public static final ForgeConfigSpec.ConfigValue<String> BOLT_CHARGE_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> BOLT_CHARGE_COLOR_BACKGROUND;

    // 减速
    public static final ForgeConfigSpec.ConfigValue<String> SLOW_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> SLOW_COLOR_BACKGROUND;

    // 灼烧
    public static final ForgeConfigSpec.ConfigValue<String> SCORCH_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> SCORCH_COLOR_BACKGROUND;

    // 舒张
    public static final ForgeConfigSpec.ConfigValue<String> DIASTOLE_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> DIASTOLE_COLOR_BACKGROUND;

    // 矿石颜色配置
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_IRON;
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_GOLD;
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_DIAMOND;
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_EMERALD;
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_LAPIS;
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_REDSTONE;
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_COAL;
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_COPPER;
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_QUARTZ;
    public static final ForgeConfigSpec.ConfigValue<String> ORE_COLOR_DEBRIS;
    public static final ForgeConfigSpec.ConfigValue<java.util.List<? extends String>> CUSTOM_ORE_COLORS;

    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("通用HUD配置").push("Common HUD Config");
        GLOBAL_SCALE = builder
                .comment("通用HUD缩放比例")
                .comment("Common HUD scaling")
                .defineInRange("globalScale", 1.0, 0.1, 3.0);
        HUD_PADDING = builder
                .comment("通用HUD背景内边距")
                .comment("Common HUD background padding")
                .defineInRange("padding", 2, 0, 50);
        HUD_DYNAMIC_MAX_DISPLAY_COUNT = builder
                .comment("动态HUD最大显示数量")
                .comment("Common HUD dynamic display count")
                .defineInRange("dynamicMaxDisplayCount", 5, 1, 50);
        HUD_LAYOUT_RELATIVE_X = builder
                .comment("相对X位置")
                .comment("Common HUD relative X position")
                .defineInRange("relativeX", 0.05, 0.0, 1.0);
        HUD_LAYOUT_RELATIVE_Y = builder
                .comment("相对Y位置")
                .comment("Common HUD relative Y position")
                .defineInRange("relativeY", 0.05, 0.0, 1.0);
        HUD_LAYOUT_SPACING = builder
                .comment("通用HUD元素间距")
                .comment("Common HUD spacing spacing between elements")
                .defineInRange("spacing", 4, 0, 50);
        HUD_LAYOUT_MAX_SCREEN_HEIGHT_RATIO = builder
                .comment("通用HUD最大高度占屏幕高度的比例")
                .comment("Common HUD max screen height ratio")
                .defineInRange("maxScreenHeightRatio", 0.5, 0.1, 1.0);
        builder.pop();

        builder.comment("魂燃").push("Soulburn");
        SOULBURN_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式)")
                .comment("Text Color (Hexadecimal format)")
                .define("colorText", "#FFFFFF");
        SOULBURN_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式带透明度)")
                .comment("Background Color (Hexadecimal format with alpha)")
                .define("colorBackground", "#88000000");
        builder.pop();

        builder.comment("冰霜护甲").push("FrostArmor");
        FROST_ARMOR_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式)")
                .comment("Text Color (Hexadecimal format)")
                .define("colorText", "#FFFFFF");
        FROST_ARMOR_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式带透明度)")
                .comment("Background Color (Hexadecimal format with alpha)")
                .define("colorBackground", "#88000000");
        builder.pop();

        builder.comment("织造铠甲").push("WovenMail");
        WOVEN_MAIL_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式)")
                .comment("Text Color (Hexadecimal format)")
                .define("colorText", "#FFFFFF");
        WOVEN_MAIL_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式带透明度)")
                .comment("Background Color (Hexadecimal format with alpha)")
                .define("colorBackground", "#88000000");
        builder.pop();

        builder.comment("弥漫暗影").push("PervadingDarkness");
        PERVADING_DARKNESS_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式)")
                .comment("Text Color (Hexadecimal format)")
                .define("colorText", "#FF5555");
        PERVADING_DARKNESS_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式带透明度)")
                .comment("Background Color (Hexadecimal format with alpha)")
                .define("colorBackground", "#88000000");
        builder.pop();

        builder.comment("蔓延黑暗").push("CreepingDarkness");
        CREEPING_DARKNESS_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式)")
                .comment("Text Color (Hexadecimal format)")
                .define("colorText", "#FF5555");
        CREEPING_DARKNESS_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式带透明度)")
                .comment("Background Color (Hexadecimal format with alpha)")
                .define("colorBackground", "#88000000");
        builder.pop();

        builder.comment("电光充能").push("BoltCharge");
        BOLT_CHARGE_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式)")
                .comment("Text Color (Hexadecimal format)")
                .define("colorText", "#FFFFFF");
        BOLT_CHARGE_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式带透明度)")
                .comment("Background Color (Hexadecimal format with alpha)")
                .define("colorBackground", "#88000000");
        builder.pop();

        builder.comment("减速").push("Slow");
        SLOW_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式)")
                .comment("Text Color (Hexadecimal format)")
                .define("colorText", "#FFFFFF");
        SLOW_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式带透明度)")
                .comment("Background Color (Hexadecimal format with alpha)")
                .define("colorBackground", "#88000000");
        builder.pop();

        builder.comment("灼烧").push("Scorch");
        SCORCH_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式)")
                .comment("Text Color (Hexadecimal format)")
                .define("colorText", "#FF5555");
        SCORCH_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式带透明度)")
                .comment("Background Color (Hexadecimal format with alpha)")
                .define("colorBackground", "#88000000");
        builder.pop();

        builder.comment("舒张").push("Diastole");
        DIASTOLE_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式)")
                .comment("Text Color (Hexadecimal format)")
                .define("colorText", "#FFFFFF");
        DIASTOLE_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式带透明度)")
                .comment("Background Color (Hexadecimal format with alpha)")
                .define("colorBackground", "#88000000");
        builder.pop();

        builder.comment("勘探者矿石颜色").push("ProspectorOreColors");
        ORE_COLOR_IRON = builder
                .comment("铁矿石颜色 (十六进制格式)")
                .comment("Iron Ore Color (Hexadecimal format)")
                .define("oreColorIron", "#C8B4A0");
        ORE_COLOR_GOLD = builder
                .comment("金矿石颜色 (十六进制格式)")
                .comment("Gold Ore Color (Hexadecimal format)")
                .define("oreColorGold", "#FFD700");
        ORE_COLOR_DIAMOND = builder
                .comment("钻石矿石颜色 (十六进制格式)")
                .comment("Diamond Ore Color (Hexadecimal format)")
                .define("oreColorDiamond", "#00BFFF");
        ORE_COLOR_EMERALD = builder
                .comment("绿宝石矿石颜色 (十六进制格式)")
                .comment("Emerald Ore Color (Hexadecimal format)")
                .define("oreColorEmerald", "#50FF64");
        ORE_COLOR_LAPIS = builder
                .comment("青金石矿石颜色 (十六进制格式)")
                .comment("Lapis Lazuli Ore Color (Hexadecimal format)")
                .define("oreColorLapis", "#0064FF");
        ORE_COLOR_REDSTONE = builder
                .comment("红石矿石颜色 (十六进制格式)")
                .comment("Redstone Ore Color (Hexadecimal format)")
                .define("oreColorRedstone", "#FF0000");
        ORE_COLOR_COAL = builder
                .comment("煤矿石颜色 (十六进制格式)")
                .comment("Coal Ore Color (Hexadecimal format)")
                .define("oreColorCoal", "#282828");
        ORE_COLOR_COPPER = builder
                .comment("铜矿石颜色 (十六进制格式)")
                .comment("Copper Ore Color (Hexadecimal format)")
                .define("oreColorCopper", "#FF8C00");
        ORE_COLOR_QUARTZ = builder
                .comment("石英矿石颜色 (十六进制格式)")
                .comment("Quartz Ore Color (Hexadecimal format)")
                .define("oreColorQuartz", "#FFC8FF");
        ORE_COLOR_DEBRIS = builder
                .comment("远古残骸颜色 (十六进制格式)")
                .comment("Ancient Debris Color (Hexadecimal format)")
                .define("oreColorDebris", "#BA9175");
        CUSTOM_ORE_COLORS = builder
                .comment("自定义模组矿石颜色列表，格式: \"pattern=#RRGGBB\"")
                .comment("Custom Ore Colors Color, Format: \"pattern=#RRGGBB\"")
                .defineList("customOreColors", List.of(
                    "tin=#C0C0C0",
                    "silver=#C0C0C0", 
                    "lead=#808080",
                    "uranium=#00FF00",
                    "aluminum=#DCDCDC",
                    "nickel=#A0A0A4",
                    "platinum=#E5E4E2",
                    "mithril=#00FFFF",
                    "adamantine=#8000FF",
                    "cobalt=#0046FF",
                    "ardite=#FF6400"
                ), obj -> true);
        builder.pop();

        SPEC = builder.build();
    }

    /** 解析颜色字符串为整数值 **/
    public static int parseColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            return 0xFFFFFF;
        }

        try {
            String hex = colorStr.trim();
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            } else if (hex.startsWith("0x") || hex.startsWith("0X")) {
                hex = hex.substring(2);
            }

            if (hex.length() == 6) {
                return Integer.parseInt(hex, 16);
            } else if (hex.length() == 8) {
                return (int) Long.parseLong(hex, 16);
            } else {
                return 0xFFFFFF;
            }
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }
}