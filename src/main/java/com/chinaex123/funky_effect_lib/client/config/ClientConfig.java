package com.chinaex123.funky_effect_lib.client.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class ClientConfig {

    public static final ForgeConfigSpec.DoubleValue GLOBAL_SCALE;

    public static final ForgeConfigSpec.ConfigValue<String> SOULBURN_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> SOULBURN_COLOR_BACKGROUND;
    public static final ForgeConfigSpec.IntValue SOULBURN_DISPLAY_X;
    public static final ForgeConfigSpec.IntValue SOULBURN_DISPLAY_Y;
    public static final ForgeConfigSpec.IntValue SOULBURN_PADDING;

    public static final ForgeConfigSpec.ConfigValue<String> FROST_ARMOR_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> FROST_ARMOR_COLOR_BACKGROUND;
    public static final ForgeConfigSpec.IntValue FROST_ARMOR_DISPLAY_X;
    public static final ForgeConfigSpec.IntValue FROST_ARMOR_DISPLAY_Y;
    public static final ForgeConfigSpec.IntValue FROST_ARMOR_PADDING;

    public static final ForgeConfigSpec.ConfigValue<String> WOVEN_MAIL_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> WOVEN_MAIL_COLOR_BACKGROUND;
    public static final ForgeConfigSpec.IntValue WOVEN_MAIL_DISPLAY_X;
    public static final ForgeConfigSpec.IntValue WOVEN_MAIL_DISPLAY_Y;
    public static final ForgeConfigSpec.IntValue WOVEN_MAIL_PADDING;

    public static final ForgeConfigSpec.ConfigValue<String> PERVADING_DARKNESS_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> PERVADING_DARKNESS_COLOR_BACKGROUND;
    public static final ForgeConfigSpec.IntValue PERVADING_DARKNESS_DISPLAY_X;
    public static final ForgeConfigSpec.IntValue PERVADING_DARKNESS_DISPLAY_Y;
    public static final ForgeConfigSpec.IntValue PERVADING_DARKNESS_PADDING;

    public static final ForgeConfigSpec.ConfigValue<String> CREEPING_DARKNESS_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> CREEPING_DARKNESS_COLOR_BACKGROUND;
    public static final ForgeConfigSpec.IntValue CREEPING_DARKNESS_DISPLAY_X;
    public static final ForgeConfigSpec.IntValue CREEPING_DARKNESS_DISPLAY_Y;
    public static final ForgeConfigSpec.IntValue CREEPING_DARKNESS_PADDING;

    public static final ForgeConfigSpec.ConfigValue<String> BOLT_CHARGE_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> BOLT_CHARGE_COLOR_BACKGROUND;
    public static final ForgeConfigSpec.IntValue BOLT_CHARGE_DISPLAY_X;
    public static final ForgeConfigSpec.IntValue BOLT_CHARGE_DISPLAY_Y;
    public static final ForgeConfigSpec.IntValue BOLT_CHARGE_PADDING;

    public static final ForgeConfigSpec.ConfigValue<String> SLOW_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> SLOW_COLOR_BACKGROUND;
    public static final ForgeConfigSpec.IntValue SLOW_DISPLAY_X;
    public static final ForgeConfigSpec.IntValue SLOW_DISPLAY_Y;
    public static final ForgeConfigSpec.IntValue SLOW_PADDING;

    public static final ForgeConfigSpec.ConfigValue<String> SCORCH_COLOR_TEXT;
    public static final ForgeConfigSpec.ConfigValue<String> SCORCH_COLOR_BACKGROUND;
    public static final ForgeConfigSpec.IntValue SCORCH_DISPLAY_X;
    public static final ForgeConfigSpec.IntValue SCORCH_DISPLAY_Y;
    public static final ForgeConfigSpec.IntValue SCORCH_PADDING;

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

        builder.push("Client Config");
        builder.comment("客户端配置");

        builder.push("Global Config");
        builder.comment("全局配置");
        GLOBAL_SCALE = builder
                .comment("全局文字缩放比例 (1.0 = 正常大小, 0.5 = 一半大小, 2.0 = 两倍大小)")
                .defineInRange("globalScale", 1.0, 0.1, 3.0);
        builder.pop();

        builder.push("Soulburn Display");
        builder.comment("魂燃显示配置");
        SOULBURN_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式，如 #FFFFFF 或 0xFFFFFF)")
                .define("colorText", "#FFFFFF");
        SOULBURN_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式，如 #88000000 或 0x88000000)")
                .define("colorBackground", "#88000000");
        SOULBURN_DISPLAY_X = builder
                .comment("显示位置 X 坐标")
                .defineInRange("displayX", 4, 0, Integer.MAX_VALUE);
        SOULBURN_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 50, 0, Integer.MAX_VALUE);
        SOULBURN_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 2, 0, 50);
        builder.pop();

        builder.push("FrostArmor Display");
        builder.comment("冰霜护甲显示配置");
        FROST_ARMOR_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式，如 #FFFFFF 或 0xFFFFFF)")
                .define("colorText", "#FFFFFF");
        FROST_ARMOR_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式，如 #88000000 或 0x88000000)")
                .define("colorBackground", "#88000000");
        FROST_ARMOR_DISPLAY_X = builder
                .comment("显示位置 X 坐标")
                .defineInRange("displayX", 4, 0, Integer.MAX_VALUE);
        FROST_ARMOR_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 61, 0, Integer.MAX_VALUE);
        FROST_ARMOR_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 2, 0, 50);
        builder.pop();

        builder.push("WovenMail Display");
        builder.comment("织造铠甲显示配置");
        WOVEN_MAIL_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式，如 #FFFFFF 或 0xFFFFFF)")
                .define("colorText", "#FFFFFF");
        WOVEN_MAIL_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式，如 #88000000 或 0x88000000)")
                .define("colorBackground", "#88000000");
        WOVEN_MAIL_DISPLAY_X = builder
                .comment("显示位置 X 坐标")
                .defineInRange("displayX", 4, 0, Integer.MAX_VALUE);
        WOVEN_MAIL_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 72, 0, Integer.MAX_VALUE);
        WOVEN_MAIL_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 2, 0, 50);
        builder.pop();

        builder.push("PervadingDarkness Display");
        builder.comment("弥漫暗影显示配置");
        PERVADING_DARKNESS_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式，如 #FF5555 或 0xFF5555)")
                .define("colorText", "#FF5555");
        PERVADING_DARKNESS_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式，如 #88000000 或 0x88000000)")
                .define("colorBackground", "#88000000");
        PERVADING_DARKNESS_DISPLAY_X = builder
                .comment("显示位置 X 坐标")
                .defineInRange("displayX", 4, 0, Integer.MAX_VALUE);
        PERVADING_DARKNESS_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 83, 0, Integer.MAX_VALUE);
        PERVADING_DARKNESS_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 2, 0, 50);
        builder.pop();

        builder.push("CreepingDarkness Display");
        builder.comment("蔓延黑暗显示配置");
        CREEPING_DARKNESS_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式，如 #FF5555 或 0xFF5555)")
                .define("colorText", "#FF5555");
        CREEPING_DARKNESS_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式，如 #88000000 或 0x88000000)")
                .define("colorBackground", "#88000000");
        CREEPING_DARKNESS_DISPLAY_X = builder
                .comment("显示位置 X 坐标")
                .defineInRange("displayX", 4, 0, Integer.MAX_VALUE);
        CREEPING_DARKNESS_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 94, 0, Integer.MAX_VALUE);
        CREEPING_DARKNESS_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 2, 0, 50);
        builder.pop();

        builder.push("BoltCharge Display");
        builder.comment("电光充能显示配置");
        BOLT_CHARGE_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式，如 #FFFFFF 或 0xFFFFFF)")
                .define("colorText", "#FFFFFF");
        BOLT_CHARGE_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式，如 #88000000 或 0x88000000)")
                .define("colorBackground", "#88000000");
        BOLT_CHARGE_DISPLAY_X = builder
                .comment("显示位置 X 坐标")
                .defineInRange("displayX", 4, 0, Integer.MAX_VALUE);
        BOLT_CHARGE_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 105, 0, Integer.MAX_VALUE);
        BOLT_CHARGE_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 2, 0, 50);
        builder.pop();

        builder.push("Slow Display");
        builder.comment("减速显示配置");
        SLOW_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式，如 #FFFFFF 或 0xFFFFFF)")
                .define("colorText", "#FFFFFF");
        SLOW_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式，如 #88000000 或 0x88000000)")
                .define("colorBackground", "#88000000");
        SLOW_DISPLAY_X = builder
                .comment("显示位置 X 坐标")
                .defineInRange("displayX", 4, 0, Integer.MAX_VALUE);
        SLOW_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 116, 0, Integer.MAX_VALUE);
        SLOW_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 2, 0, 50);
        builder.pop();

        builder.push("Scorch Display");
        builder.comment("灼烧显示配置");
        SCORCH_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式，如 #FF5555 或 0xFF5555)")
                .define("colorText", "#FF5555");
        SCORCH_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式，如 #88000000 或 0x88000000)")
                .define("colorBackground", "#88000000");
        SCORCH_DISPLAY_X = builder
                .comment("显示位置 X 坐标")
                .defineInRange("displayX", 4, 0, Integer.MAX_VALUE);
        SCORCH_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 127, 0, Integer.MAX_VALUE);
        SCORCH_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 2, 0, 50);
        builder.pop();

        builder.push("Prospector Ore Colors");
        builder.comment("勘探者矿石颜色配置");
        ORE_COLOR_IRON = builder
                .comment("铁矿石颜色 (十六进制格式，如 #C8B4A0)")
                .define("oreColorIron", "#C8B4A0");
        ORE_COLOR_GOLD = builder
                .comment("金矿石颜色 (十六进制格式，如 #FFD700)")
                .define("oreColorGold", "#FFD700");
        ORE_COLOR_DIAMOND = builder
                .comment("钻石矿石颜色 (十六进制格式，如 #00BFFF)")
                .define("oreColorDiamond", "#00BFFF");
        ORE_COLOR_EMERALD = builder
                .comment("绿宝石矿石颜色 (十六进制格式，如 #50FF64)")
                .define("oreColorEmerald", "#50FF64");
        ORE_COLOR_LAPIS = builder
                .comment("青金石矿石颜色 (十六进制格式，如 #0064FF)")
                .define("oreColorLapis", "#0064FF");
        ORE_COLOR_REDSTONE = builder
                .comment("红石矿石颜色 (十六进制格式，如 #FF0000)")
                .define("oreColorRedstone", "#FF0000");
        ORE_COLOR_COAL = builder
                .comment("煤矿石颜色 (十六进制格式，如 #282828)")
                .define("oreColorCoal", "#282828");
        ORE_COLOR_COPPER = builder
                .comment("铜矿石颜色 (十六进制格式，如 #FF8C00)")
                .define("oreColorCopper", "#FF8C00");
        ORE_COLOR_QUARTZ = builder
                .comment("石英矿石颜色 (十六进制格式，如 #FFC8FF)")
                .define("oreColorQuartz", "#FFC8FF");
        ORE_COLOR_DEBRIS = builder
                .comment("远古残骸颜色 (十六进制格式，如 #BA9175)")
                .define("oreColorDebris", "#BA9175");
        CUSTOM_ORE_COLORS = builder
                .comment("自定义模组矿石颜色列表，格式: \"pattern=#RRGGBB\"\n" +
                          "例如: \"tin=#C0C0C0\", \"modid:silver=#C0C0C0\", \"uranium=#00FF00\"")
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