# UltimateBackpack

Welcome to UltimateBackpack, the ultimate plugin for customizing and enhancing your Minecraft backpack experience! With a wide range of features, UltimateBackpack is designed to meet all your needs for inventory management and customization.

## 🚀 Features

### 🔧 **Customizable Items**
- **Fully Customizable:** UltimateBackpack supports completely customizable items, including modded and custom items.
- **Custom Heads:** Configure backpacks to use custom Minecraft heads as items.

### 🖼️ **Customizable GUI**
- **Fully Customizable GUI:** Design your GUI with complete freedom to match your server’s theme.

### 🌐 **Customizable Messages**
- **All Messages Customizable:** Tailor all messages displayed by the plugin to fit your server’s style.

### 🔒 **Blacklist & Restrictions**
- **Item Blacklist:** Prevent specific items from being added to backpacks.
- **WorldGuard & World Blacklist:** Restrict backpack usage in certain WorldGuard regions or worlds.

### ⏲️ **Cooldown System**
- **Hit Cooldown:** Prevent backpack usage for a configurable time after being hit by another player or after hitting another player (similar to combat tag plugins).

### 🔑 **Permissions System**
- **Comprehensive Permissions:** Grant or restrict backpack functionalities with a detailed permissions system.
- **Bypass Restrictions:** Assign permissions to bypass item, region, or world restrictions.

### 🔄 **Dynamic Backpack Upgrades**
- **Upgrade System:** Use commands or automated processes to upgrade backpacks with varying costs and benefits.
- **Row System:** Customize the number of rows available in backpacks based on permissions.

### 🗃️ **Efficient Player Data Management**
- **Lightweight Data Files:** Efficiently store player data while maintaining detailed item information.

### 🔄 **Version Compatibility**
- **Compatible with Minecraft Versions 1.8 to 1.21+:** Ensure support for a broad range of Minecraft versions.

### 📜 **Developer Events**
- **BackpackItemAddEvent & BackpackItemRemoveEvent:** Track items added or removed from backpacks with these events for developers.

### 🔧 **Administration System**
- **Complete & Secure Admin Tools:** Manage and configure backpacks with a robust administrative system.

## 📜 Commands

| Command            | Permission                    | Description                                    |
|--------------------|-------------------------------|------------------------------------------------|
| `/backpack help` | `none` | Shows the help menu for backpack commands. |
| `/backpack open`        | `backpack.open.himself`       | Opens the player's own backpack.               |
| `/backpack <player>` | `backpack.open.everyone`      | Opens another player's backpack (requires target player name). |
| `/backpack upgrade` | `backpack.upgrade`            | Upgrades the player's backpack (if applicable). |
| `/backpack giveitem` | `backpack.give-item`            | Give the backpack item. |

## 🔑 Permissions

| Permission                             | Description                                           |
|----------------------------------------|-------------------------------------------------------|
| `backpack.open.himself`                | Allows the player to open their own backpack.        |
| `backpack.open.everyone`               | Allows the player to open backpacks of other players. |
| `backpack.bypass.blacklist_items`      | Bypasses the item blacklist for the player.          |
| `backpack.bypass.hit_cooldown`         | Bypasses the hit cooldown for the player.            |
| `backpack.bypass.region_limitation`    | Bypasses region restrictions for the player.         |
| `backpack.bypass.world_limitation`     | Bypasses world restrictions for the player.          |
| `backpack.size.1`                     | Grants access to a backpack with 1 row.              |
| `backpack.size.2`                     | Grants access to a backpack with 2 rows.             |
| `backpack.size.3`                     | Grants access to a backpack with 3 rows.             |
| `backpack.size.4`                     | Grants access to a backpack with 4 rows.             |
| `backpack.size.5`                     | Grants access to a backpack with 5 rows.             |
| `backpack.size.6`                     | Grants access to a backpack with 6 rows.             |
| `backpack.give-item` |  Give the backpack item. |

Feel free to adjust the commands and permissions based on the exact functionalities of your plugin and add or remove any that are not applicable.

## 📊 bStats

UltimateBackpack uses [bStats](https://bstats.org) to gather anonymous usage data, which helps us improve the plugin. You can view the plugin's statistics here:

[![bStats Graph](https://bstats.org/signatures/bukkit/UltimateBackpack.svg)](https://bstats.org/plugin/bukkit/UltimateBackpack)

## 💬 Support

For support, questions, or feedback, join our [Discord server](https://discord.gg/rsZCQumQ5N) where you can connect with our community and get help from the developers.

Thank you for using UltimateBackpack! If you enjoy the plugin, please consider leaving a review or contributing to its development.

---

*UltimateBackpack is developed by Boulldogo.*
