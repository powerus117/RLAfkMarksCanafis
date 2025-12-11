# Marks of Grace Cooldown

A RuneLite plugin that tracks the Marks of Grace cooldown timer on Agility courses, helping you collect more marks efficiently.

## How It Works

After picking up a Mark of Grace, there's a **3-minute cooldown** before another can spawn. This plugin shows you a timer so you know exactly when to complete your next lap for the best chance at spawning a new mark.

## The Overlay

The plugin displays a simple overlay with three possible states:

| Status | Color | What to do |
|--------|-------|------------|
| **Run** | 🟢 Green | Complete laps — a mark can spawn! |
| **XP** | 🟠 Orange | Safe to run laps for XP (no mark will spawn yet) |
| **Wait** | 🔴 Red | Wait on the last obstacle until the timer runs out |

## Getting Started

1. Start training on any Agility course
2. Pick up a Mark of Grace — the overlay will appear
3. Follow the status indicator:
   - 🟢 **Run** → Finish your lap
   - 🟠 **XP** → Keep running if you want XP
   - 🔴 **Wait** → Pause before the last obstacle
4. When the timer expires, complete a lap for your next mark

## Supported Courses

The plugin works on all 15 courses that spawn Marks of Grace:

- **Rooftop courses**: Draynor, Al Kharid, Varrock, Canifis, Falador, Seers' Village, Pollnivneach, Rellekka, Ardougne
- **Other courses**: Gnome Stronghold, Barbarian Outpost, Ape Atoll, Werewolf, Shayzien Basic, Shayzien Advanced

## Settings

### Notifications
Get a desktop notification when the cooldown expires so you don't miss your chance.

### Swap Mode
Optionally deprioritize the left-click option on the final obstacle while on cooldown, making it harder to accidentally complete a lap too early.

### Diary Support
- **Ardougne Elite Diary**: If you have this diary, the cooldown has a 50% chance of being reduced to 2 minutes. Enable "Use short Ardougne timer" to use the shorter time.
- **Seers' Village + Camelot Teleport**: If you use the bank teleport shortcut, enable "Use Seers bank teleport" for accurate lap time calculations.

### Per-Course Toggles
Enable or disable the plugin for specific courses in the settings.

## Tips

- The timer persists across world hops and logout — just like the actual cooldown
- You can keep training for XP during cooldown; marks simply won't spawn
- Use the lap time buffer setting to give yourself extra time if you're not hitting perfect laps

## License

See [LICENSE](LICENSE) for details.
