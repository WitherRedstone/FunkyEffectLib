# 奇趣效果Lib（Funky Effect Lib）

[English](#english) | [中文](#中文)

---

# **English**

## Introduction

Added buffs and debuffs from *Destiny 2*, along with other fun and quirky effects.

## Features

- **60+ Custom Effects**: A wide variety of unique effects to enhance gameplay
- **Destiny 2 Inspired**: Many effects are based on mechanics from Destiny 2
- **Client-Side Rendering**: Custom visual effects for enhanced experience
- **Network Synchronization**: Proper multiplayer support with packet handling

## Effect Categories

### Darkness Effects
- **Darkness**: Gradually slows movement over time
- **Creeping Darkness**: Gradually slows movement over time, kills after 10 layers
- **Pervading Darkness**: Gradually slows movement over time, kills after 10 layers, reduces layer on kill of tagged entity

### Defense Effects
- **Frost Armor**: Collect XP orbs to generate crystals that provide damage reduction
- **Woven Mail**: Collect XP orbs to generate tangles for high damage reduction
- **Crystal Shield**: Protective crystal barrier
- **Hardened Skin**: Increased physical resistance
- **Iron Will**: Unbreakable determination

### Damage Effects
- **Scorch**: Fire-based damage over time
- **Ignite**: Immediate fire ignition
- **Sever**: Cutting damage
- **Vulnerable**: Increased damage taken
- **Radiation**: Radiation damage
- **Bleeding**: Continuous blood loss

### Restoration Effects
- **Cure**: Healing effect
- **Restoration**: Regeneration over time
- **Overheal**: Temporary health beyond maximum
- **Flesh Regrowth**: Natural healing

### Exploration Effects
- **Prospector**: Detect ores nearby
- **Treasure Finder**: Locate hidden treasures
- **Danger Sense**: Warn of nearby threats
- **Lava Vision**: See through lava

### Special Effects
- **Afterimage**: Create clone duplicates
- **Flip Gravity**: Reverse gravity
- **Invisibility**: Become invisible
- **Borrowed Time**: Temporary time extension
- **Dice of Fate**: Randomized outcomes

## Usage for Modders

This is a library mod designed to be used by other mods. You can access effects through the `FELEffects` class:

```java
import com.chinaex123.funky_effect_lib.init.FELEffects;
entity.addEffect(new MobEffectInstance(FELEffects.DARKNESS.get(),600,0));
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

- **Author**: Wither_Redstone
- **Inspiration**: Destiny 2 by Bungie

## Support

For issues, suggestions, or contributions, please visit the project repository.

---

# **中文**

## 简介

主要添加了来自《命运2》的增益与减益效果，同时也加入了一些其他奇趣效果。

## 功能特点

- **60+ 自定义效果**：提供丰富多样的独特效果以增强游戏体验
- **灵感源自命运2**：许多效果基于《命运2》的游戏机制
- **客户端渲染**：自定义视觉效果提供更好的游戏体验
- **网络同步**：完善的多人游戏支持与数据包处理

## 效果分类

### 黑暗类效果
- **黑暗（Darkness）**：随时间逐渐降低移动速度
- **蔓延黑暗（Creeping Darkness）**：随时间逐渐降低移动速度，叠至10层时死亡
- **弥漫暗影（Pervading Darkness）**：随时间逐渐降低移动速度，叠至10层时死亡，击杀tag生物可减少一层

### 防御类效果
- **冰霜护甲（Frost Armor）**：拾取经验球生成冰晶，提供伤害减免
- **织造铠甲（Woven Mail）**：拾取经验球生成缠结，提供高额伤害减免
- **水晶护盾（Crystal Shield）**：保护性水晶屏障
- **硬化皮肤（Hardened Skin）**：增加物理抗性
- **钢铁意志（Iron Will）**：不可动摇的决心

### 伤害类效果
- **灼烧（Scorch）**：火焰持续伤害
- **点燃（Ignite）**：立即点燃目标
- **切割（Sever）**：切割伤害
- **脆弱（Vulnerable）**：增加受到的伤害
- **辐射（Radiation）**：辐射伤害
- **流血（Bleeding）**：持续失血

### 恢复类效果
- **治愈（Cure）**：治疗效果
- **恢复（Restoration）**：持续再生
- **透支治愈（Overheal）**：超出最大生命值的临时生命
- **血肉再生（Flesh Regrowth）**：自然愈合

### 探索类效果
- **探矿者（Prospector）**：探测附近的矿石
- **寻宝者（Treasure Finder）**：定位隐藏的宝藏
- **危险感知（Danger Sense）**：警告附近的威胁
- **岩浆明视（Lava Vision）**：看穿熔岩

### 特殊效果
- **残影（Afterimage）**：创造克隆分身
- **重力翻转（Flip Gravity）**：反转重力
- **隐身（Invisibility）**：变得不可见
- **借贷（Borrowed Time）**：临时延长时间
- **命运骰子（Dice of Fate）**：随机化结果

## 模组开发者使用

这是一个库模组，设计用于被其他模组使用。你可以通过 `FELEffects` 类访问效果：

```java
import com.chinaex123.funky_effect_lib.init.FELEffects;
entity.addEffect(new MobEffectInstance(FELEffects.DARKNESS.get(),600,0));
```

## 许可证

本项目采用 MIT 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。

## 致谢

- **作者**：Wither_Redstone
- **灵感来源**：Bungie 的《命运2》

## 支持

如有问题、建议或贡献，请访问项目仓库。