# OxeVy

<div align="center">
  
**!!!!WARNING THIS PROJECT IS IN BETA!!!!**

**A AI based hackclient on OyVey-Ported**

Built with: ChatGPT • DeepSeek • OpenCode • Gemini • Cursor

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/daneq1/oxevy)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11+-brightgreen.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-1.21.11+-orange.svg)](https://fabricmc.net/)

</div>

## Preview

### OyVey-Ported
![Original UI](images/ui.png)

### OxeVy UI
![OxeVy UI](images/ui1.png)
![OxeVy UI2](images/ui2.png)

## Features

### Combat
- **KillAura** - Automatic attack with customizable CPS, cooldown, rotation sync
- **AimBot** - Auto-aim at entities
- **Criticals** - Force critical hits
- **Strafe** - Combat strafe
- **KeyPearl** - Auto-throw ender pearls

### Movement
- **Flight** - Fly mode
- **Speed** - Speed hack
- **Timer** - Modify game tick speed
- **Step** - Step up blocks
- **ReverseStep** - Reverse step

### Player
- **NoFall** - Prevent fall damage
- **FastPlace** - Faster item placement
- **Velocity** - Modify knockback
- **AutoTotem** - Auto-switch to totem
- **AutoEat** - Auto-eat when hungry
- **AirPlace** - Place blocks faster
- **FastBreak** - Break blocks faster
- **Reach** - Increase attack reach

### Render
- **ESP** - EntityESP
- **Tracers** - Line to entities with customizable target/source positions
- **Nametags** - Custom nametags
- **Fullbright** - Brightness control
- **BlockHighlight** - Highlight targeted block
- **ChestESP** - Visualize containers
- **HealthBar** - Display health bars

### HUD
- **ArrayList** - Enabled modules list with sliding animations
- **Watermark** - Client watermark
- **MenuWatermark** - Shows on main menu
- **Coordinates** - Player coordinates
- **TargetHUD** - Target information with armor/head display
- **ServerInfo** - Server details
- **FPS** - Display FPS

### Client
- **ClickGui** - Module configuration GUI with Ctrl+F search
- **HudEditor** - Edit HUD positions
- **Notifications** - Module notifications

## Multi-Config Support

Save and load multiple configurations:
- `.config save <name>` - Save config
- `.config load <name>` - Load config
- `.config list` - List configs
- `.config delete <name>` - Delete config

## Technologies Used

| AI Tool | Purpose |
|---------|---------|
| ChatGPT | Code optimization and feature implementation |
| DeepSeek | Prompt making |
| OpenCode | Open-source best practices |
| Gemini | UI/UX improvements |
| Cursor | Development assistance |

## Requirements

- Java 21
- Minecraft 1.21.11
- Fabric Loader 0.18.4+
- Fabric API

## Installation

1. Clone the repository
   ```bash
   git clone https://github.com/daneq1/oxevy.git
   ```
2. Build the client
   ```bash
   ./gradlew build
   ```
3. Find the built JAR in `build/libs/`
4. Drop the JAR into your `mods` folder

## Commands

| Command | Description |
|---------|-------------|
| `.` | Command prefix |
| `.help` | Show all commands |
| `.toggle <module>` | Toggle a module |
| `.bind <module> <key>` | Bind a module |
| `.friend add <name>` | Add friend |
| `.friend remove <name>` | Remove friend |
| `.config save` | Save config |
| `.config load` | Load config |

## Credits

- OyVey - Base client
- Fabric Team - Fabric API
- Mixin Team - Mixin

---

Fun fact: This page was made by AI too
