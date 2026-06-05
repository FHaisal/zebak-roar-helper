# Zebak Roar Helper

A RuneLite plugin that provides non-intrusive visual assistance during Zebak's Great Roar attack in the Tombs of Amascut.

<p align="center">
  <video src="https://github.com/user-attachments/assets/0c86def4-9bea-4891-a7a6-5b7acbad8093" controls="controls" muted="muted" width="800">
    Your browser does not support the video tag.
  </video>
</p>

## Features

- **Dynamic Jug Highlighting**: Intelligently highlights valid jugs via their 3D hull to help you choose the fastest path to safety without cluttering the floor.
  - **Push Tactic (Purple)**: Highlights jugs that naturally align with rocks, indicating which ones should be pushed straight into a rock.
  - **Push-to-Hit Tactic (Cyan)**: Highlights jugs that don't directly hit a rock but will roll past the splash radius of a safe spot, allowing you to attack it mid-roll.
  - **Hit-Only Tactic (Orange)**: Highlights jugs that are already in a position where hitting them will clear a safe spot.
- **Tactical Priority Engine**: If a jug satisfies multiple conditions, the plugin evaluates your distance to the jug and any acid in the way to recommend the most efficient action.
- **Roar Countdown Timer**: Adds a flashing UI countdown timer tracking exactly how many attacks Zebak will perform before executing the Great Roar damage.
- **Rolling True Tile**: Maps the server true tile of a moving jug, making it easier to time your attacks on moving jugs.

## Configuration

All colors and tactics are fully customizable via the plugin configuration menu.

- **Tactic Toggles**: Individually enable or disable the Hit-Only, Push, and Push-to-Hit highlights, and customize their colors.
- **Upset Stomach Toggle**: Recalculates the splash radius and valid safe zones based on whether the Upset Stomach invocation is active (3x3 vs 5x5).
- **Flash Threshold**: Customizes exactly when the countdown timer will flash red to warn you to move to a safe zone.

## Support

Please report any issues or feedback via the GitHub repository issue tracker.