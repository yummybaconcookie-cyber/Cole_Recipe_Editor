=========================================================
  Cole's Recipe Editor (CRE) - NeoForge 1.21.1 Mod
=========================================================

WHAT THIS MOD DOES
------------------
CRE is an in-game GUI that lets you create, disable, enable,
and manage recipes without touching any code or scripts.
It automatically writes KubeJS scripts behind the scenes.

REQUIREMENTS (you must have ALL of these installed):
  - Minecraft 1.21.1
  - NeoForge 21.1.x
  - KubeJS (kubejs-neoforge-2101.x)
  - KubeJS Create (kubejs-create-neoforge-2101.x)
  - Create (create-1.21.1-6.0.x)

HOW TO USE
----------
1. Load into a world (singleplayer or server)
2. Press F8 to open the Recipe Editor GUI
   (You must be OP level 2 or higher on a server)
3. Browse all loaded recipes in the list
4. Use the search bar to find recipes by ID
5. Click a recipe row to see details
6. Use the toggle button to enable/disable recipes
7. Click "+ New Recipe" to create a custom recipe
8. When done, click "Save Changes" then run /CRE reload

COMMANDS
--------
  /CRE reload   - Save all changes AND reload recipes for everyone
  /CRE save     - Save changes without reloading (apply later)
  /CRE list     - Show all disabled/custom recipes in chat
  /CRE help     - Show command help

HOW RECIPE CHANGES ARE SAVED
-----------------------------
- All changes are saved to: [world/server]/config/cre/recipe_state.json
- A KubeJS script is written to: kubejs/server_scripts/cre_generated.js
- Changes persist between restarts
- Run /CRE reload to apply changes to all players immediately

JEI INTEGRATION
---------------
- Disabled recipes will disappear from JEI
- Custom CRE recipes will appear in JEI
- Changes take effect after /CRE reload

GITHUB ACTIONS BUILD (see GITHUB_SETUP.txt for full guide)
-----------------------------------------------------------
If you need to rebuild this mod after changes:
1. Upload this folder to a GitHub repository
2. GitHub will auto-build a .jar file
3. Download the .jar from the Actions tab

=========================================================
