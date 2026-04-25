# 🕵️ PlayerHider

A simple but powerful Minecraft (Spigot/Paper) plugin that lets you **hide players** from others while giving trusted/whitelisted players special visibility.  
Supports **Minecraft 1.21+**.

---

## ✨ Features
- 🔒 Plugin name disguised in console and plugin list. (Disguised to InternalSystem)
- 🔒 Hide any player from the server.
- 👀 Whitelisted players can still see hidden players.
- 🌟 Hidden players see themselves normally.
- ✨ Whitelisted players + hidden players see each other **with a glowing effect**.
- 🚫 Prevents hidden players from broadcasting **advancements/achievements**.
- 🔄 Easy reload with `/internalreload`.

---

## 📦 Installation
1. Download the latest release (or build from source).
2. Drop the `PlayerHider.jar` file into your server’s `plugins/` folder.
3. Restart your server.
4. Done ✅

---

## ⚙️ Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/hideplayer <player>` | Hide a player from everyone except whitelisted players. | `internalsystem.hide` |
| `/unhideplayer <player>` | Unhide a previously hidden player. | `internalsystem.unhide` |
| `/whitelistplayer <player>` | Allow a player to see hidden players. | `internalsystem.whitelist` |
| `/unwhitelistplayer <player>` | Remove a player from the whitelist. | `internalsystem.unwhitelist` |
| `/internalreload` | Reload the plugin. | `internalsystem.reload` |

---

## 🔑 Permissions
- `internalsystem.hide` → Default: OP  
- `internalsystem.unhide` → Default: OP  
- `internalsystem.whitelist` → Default: OP  
- `internalsystem.unwhitelist` → Default: OP  
- `internalsystem.reload` → Default: OP  

---

## 🛠️ Building from Source
This project uses **Gradle**.

```sh
# Clone the repository
git clone https://github.com/ignJoro/playerhider.git
cd playerhider

# Build the plugin
./gradlew build
