package com.chinaex123.funky_effect_lib.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static final ModConfigSpec.ConfigValue<String> FROST_ARMOR_COLOR_TEXT;
    public static final ModConfigSpec.ConfigValue<String> FROST_ARMOR_COLOR_BACKGROUND;
    public static final ModConfigSpec.IntValue FROST_ARMOR_DISPLAY_X;
    public static final ModConfigSpec.IntValue FROST_ARMOR_DISPLAY_Y;
    public static final ModConfigSpec.IntValue FROST_ARMOR_PADDING;

    public static final ModConfigSpec.ConfigValue<String> WOVEN_MAIL_COLOR_TEXT;
    public static final ModConfigSpec.ConfigValue<String> WOVEN_MAIL_COLOR_BACKGROUND;
    public static final ModConfigSpec.IntValue WOVEN_MAIL_DISPLAY_X;
    public static final ModConfigSpec.IntValue WOVEN_MAIL_DISPLAY_Y;
    public static final ModConfigSpec.IntValue WOVEN_MAIL_PADDING;

    public static final ModConfigSpec.ConfigValue<String> SOULBURN_COLOR_TEXT;
    public static final ModConfigSpec.ConfigValue<String> SOULBURN_COLOR_BACKGROUND;
    public static final ModConfigSpec.IntValue SOULBURN_DISPLAY_X;
    public static final ModConfigSpec.IntValue SOULBURN_DISPLAY_Y;
    public static final ModConfigSpec.IntValue SOULBURN_PADDING;

    public static final ModConfigSpec.ConfigValue<String> PERVADING_DARKNESS_COLOR_TEXT;
    public static final ModConfigSpec.ConfigValue<String> PERVADING_DARKNESS_COLOR_BACKGROUND;
    public static final ModConfigSpec.IntValue PERVADING_DARKNESS_DISPLAY_X;
    public static final ModConfigSpec.IntValue PERVADING_DARKNESS_DISPLAY_Y;
    public static final ModConfigSpec.IntValue PERVADING_DARKNESS_PADDING;

    public static final ModConfigSpec.ConfigValue<String> CREEPING_DARKNESS_COLOR_TEXT;
    public static final ModConfigSpec.ConfigValue<String> CREEPING_DARKNESS_COLOR_BACKGROUND;
    public static final ModConfigSpec.IntValue CREEPING_DARKNESS_DISPLAY_X;
    public static final ModConfigSpec.IntValue CREEPING_DARKNESS_DISPLAY_Y;
    public static final ModConfigSpec.IntValue CREEPING_DARKNESS_PADDING;

    public static final ModConfigSpec.ConfigValue<String> BOLT_CHARGE_COLOR_TEXT;
    public static final ModConfigSpec.ConfigValue<String> BOLT_CHARGE_COLOR_BACKGROUND;
    public static final ModConfigSpec.IntValue BOLT_CHARGE_DISPLAY_X;
    public static final ModConfigSpec.IntValue BOLT_CHARGE_DISPLAY_Y;
    public static final ModConfigSpec.IntValue BOLT_CHARGE_PADDING;

    public static final ModConfigSpec.ConfigValue<String> SLOW_COLOR_TEXT;
    public static final ModConfigSpec.ConfigValue<String> SLOW_COLOR_BACKGROUND;
    public static final ModConfigSpec.IntValue SLOW_DISPLAY_X;
    public static final ModConfigSpec.IntValue SLOW_DISPLAY_Y;
    public static final ModConfigSpec.IntValue SLOW_PADDING;

    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("Client Config");
        builder.comment("客户端配置");

        builder.push("FrostArmor Display");
        builder.comment("冰冻铠甲显示配置");
        FROST_ARMOR_COLOR_TEXT = builder
                .comment("文本颜色 (十六进制格式，如 #FFFFFF 或 0xFFFFFF)")
                .define("colorText", "#FFFFFF");
        FROST_ARMOR_COLOR_BACKGROUND = builder
                .comment("背景颜色 (十六进制格式，如 #88000000 或 0x88000000)")
                .define("colorBackground", "#88000000");
        FROST_ARMOR_DISPLAY_X = builder
                .comment("显示位置 X 坐标")
                .defineInRange("displayX", 10, 0, Integer.MAX_VALUE);
        FROST_ARMOR_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 50, 0, Integer.MAX_VALUE);
        FROST_ARMOR_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 4, 0, 50);
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
                .defineInRange("displayX", 10, 0, Integer.MAX_VALUE);
        WOVEN_MAIL_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 70, 0, Integer.MAX_VALUE);
        WOVEN_MAIL_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 4, 0, 50);
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
                .defineInRange("displayX", 10, 0, Integer.MAX_VALUE);
        SOULBURN_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 90, 0, Integer.MAX_VALUE);
        SOULBURN_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 4, 0, 50);
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
                .defineInRange("displayX", 10, 0, Integer.MAX_VALUE);
        PERVADING_DARKNESS_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 100, 0, Integer.MAX_VALUE);
        PERVADING_DARKNESS_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 4, 0, 50);
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
                .defineInRange("displayX", 10, 0, Integer.MAX_VALUE);
        CREEPING_DARKNESS_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 110, 0, Integer.MAX_VALUE);
        CREEPING_DARKNESS_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 4, 0, 50);
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
                .defineInRange("displayX", 10, 0, Integer.MAX_VALUE);
        BOLT_CHARGE_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 120, 0, Integer.MAX_VALUE);
        BOLT_CHARGE_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 4, 0, 50);
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
                .defineInRange("displayX", 10, 0, Integer.MAX_VALUE);
        SLOW_DISPLAY_Y = builder
                .comment("显示位置 Y 坐标")
                .defineInRange("displayY", 130, 0, Integer.MAX_VALUE);
        SLOW_PADDING = builder
                .comment("背景内边距")
                .defineInRange("padding", 4, 0, 50);
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