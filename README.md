# Zebak Roar Helper

A RuneLite plugin that provides visual assistance during Zebak's Great Roar attack in the Tombs of Amascut.

<p align="center">
  <!-- GitHub Video Placeholder. Upload your video to a GitHub Issue/PR and paste the URL here: -->
  <video src="https://github.com/user-attachments/assets/2c29a35c-d20d-4518-8d4c-21c029e37dfd" controls="controls" muted="muted" width="800">
    Your browser does not support the video tag.
  </video>
</p>

## Features

- **Dual-Strategy Jug Highlighting**: Intelligently highlights valid jugs on the floor to help you instantly choose the fastest path to safety.
  - **Standard Push Tactic (Green/Yellow/Red)**: Highlights jugs that naturally align with rocks, indicating which ones should be pushed straight into a rock.
  - **Push-to-Hit Tactic (Cyan)**: Highlights jugs that don't directly hit a rock but will roll past the splash radius of a safe spot, telling you to push the jug and attack it mid-roll.
- **Jug Highlighting Modes**:
  - **Optimal Mode**: Calculates and highlights the absolute best single jug to interact with (and additionally shows the best Push-to-Hit jug if one exists).
  - **Nearest Mode**: Highlights the jug closest to the player that has a valid tactic.
  - **All Mode**: Displays all possible paths for all valid jugs.
- **Dynamic Attack Mode**: If a jug is already currently sitting in a safe spot, its 3D model turns purple to indicate it can be attacked immediately.
- **Stance & Path Indication**: Displays the exact stance tile you need to stand on to push the jug in the correct direction, and maps out the exact trajectory of the jug up to the rock.
- **Roar Countdown Timer**: Adds a flashing UI countdown timer tracking exactly how many attacks Zebak will perform before executing the Great Roar damage.
- **Safe Zone Rendering**: Actively evaluates acid pools and perfectly highlights the safe tiles behind rocks so you know exactly where to stand.
- **Rolling True Tile**: Maps the server true tile of a moving jug, making it easier to time your attacks on moving jugs.

## Configuration

All colors, visual elements, and modes are fully customizable via the plugin configuration menu. 

- **Upset Stomach Toggle**: Recalculates the splash radius and valid safe zones based on whether the Upset Stomach invocation is active (3x3 vs 5x5).
- **Flash Threshold**: Customizes exactly when the countdown timer will flash red to warn you to move to a safe zone.
- **Show Push-to-Hit Tactics**: Toggles the secondary Cyan strategy layer.

## Support

Please report any issues or feedback via the GitHub repository issue tracker.