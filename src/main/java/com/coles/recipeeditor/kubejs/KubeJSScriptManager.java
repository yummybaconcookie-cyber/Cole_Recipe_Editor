package com.coles.recipeeditor.kubejs;

import com.coles.recipeeditor.CREMod;
import com.coles.recipeeditor.recipe.CRERecipeEntry;
import com.coles.recipeeditor.recipe.RecipeSlot;
import com.coles.recipeeditor.recipe.RecipeStateManager;
import com.coles.recipeeditor.recipe.RecipeType;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class KubeJSScriptManager {

    private static final String CRE_SCRIPT_NAME = "cre_generated.js";

    public static Path getKubeJSServerScriptsDir() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        Path base = server != null
            ? server.getServerDirectory()
            : Path.of(".");
        return base.resolve("kubejs/server_scripts");
    }

    public static Path getCREScriptPath() {
        return getKubeJSServerScriptsDir().resolve(CRE_SCRIPT_NAME);
    }

    public static void writeScripts() {
        RecipeStateManager state = RecipeStateManager.getInstance();
        Set<String> disabled = state.getDisabledRecipes();
        List<CRERecipeEntry> custom = state.getCustomRecipes();

        StringBuilder sb = new StringBuilder();
        sb.append("// ===========================================\n");
        sb.append("// Cole's Recipe Editor - Auto-Generated Script\n");
        sb.append("// DO NOT EDIT MANUALLY — use the CRE GUI in-game\n");
        sb.append("// ===========================================\n\n");

        sb.append("ServerEvents.recipes(event => {\n\n");

        // Disable recipes
        if (!disabled.isEmpty()) {
            sb.append("    // === DISABLED RECIPES ===\n");
            for (String id : disabled) {
                sb.append("    event.remove({ id: '").append(escapeJs(id)).append("' });\n");
            }
            sb.append("\n");
        }

        // Custom recipes
        List<CRERecipeEntry> activeCustom = custom.stream()
            .filter(CRERecipeEntry::isEnabled)
            .toList();

        if (!activeCustom.isEmpty()) {
            sb.append("    // === CUSTOM CRE RECIPES ===\n");
            for (CRERecipeEntry entry : activeCustom) {
                String script = buildRecipeScript(entry);
                if (script != null) {
                    sb.append("    // Recipe: ").append(entry.getId()).append("\n");
                    sb.append("    ").append(script).append("\n\n");
                }
            }
        }

        sb.append("});\n");

        try {
            Path dir = getKubeJSServerScriptsDir();
            Files.createDirectories(dir);
            Files.writeString(getCREScriptPath(), sb.toString());
            CREMod.LOGGER.info("[CRE] Wrote KubeJS script to {}", getCREScriptPath());
        } catch (IOException e) {
            CREMod.LOGGER.error("[CRE] Failed to write KubeJS script: {}", e.getMessage());
        }
    }

    private static String buildRecipeScript(CRERecipeEntry entry) {
        try {
            RecipeType type = RecipeType.valueOf(entry.getType());
            List<RecipeSlot> inputs = entry.getInputs();
            List<RecipeSlot> outputs = entry.getOutputs();

            if (outputs.isEmpty()) return null;
            RecipeSlot primaryOutput = outputs.get(0);

            return switch (type) {
                case CRAFTING -> buildCraftingScript(entry, inputs, primaryOutput);
                case SMELTING, BLASTING, SMOKING, CAMPFIRE -> buildSimpleCookingScript(type, entry, inputs, primaryOutput);
                case STONECUTTING -> buildStonecuttingScript(entry, inputs, primaryOutput);
                case SMITHING -> buildSmithingScript(entry, inputs, primaryOutput);
                case CREATE_MIXING, CREATE_COMPACTING, CREATE_PRESSING_BASIN -> buildCreateMultiIOScript(type, entry, inputs, outputs);
                case CREATE_CRUSHING, CREATE_PRESSING, CREATE_MILLING, CREATE_SANDPAPER, CREATE_CUTTING -> buildCreateSingleInMultiOut(type, entry, inputs, outputs);
                case CREATE_FAN_BLASTING, CREATE_FAN_WASHING, CREATE_FAN_SMOKING, CREATE_FAN_HAUNTING -> buildFanScript(type, entry, inputs, outputs);
                case CREATE_DEPLOYING -> buildDeployingScript(entry, inputs, primaryOutput);
                case CREATE_EMPTYING -> buildEmptyingScript(entry, inputs, outputs);
                case CREATE_FILLING -> buildFillingScript(entry, inputs, outputs);
            };
        } catch (Exception e) {
            CREMod.LOGGER.warn("[CRE] Could not build script for {}: {}", entry.getId(), e.getMessage());
            return "// ERROR: could not generate script for " + entry.getId();
        }
    }

    private static String buildCraftingScript(CRERecipeEntry entry, List<RecipeSlot> inputs, RecipeSlot output) {
        String outputStr = slotToItem(output);
        if (entry.isShapeless()) {
            StringBuilder sb = new StringBuilder();
            sb.append("event.shapeless(").append(outputStr).append(", [");
            for (int i = 0; i < inputs.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(slotToIngredient(inputs.get(i)));
            }
            sb.append("]).id('").append(entry.getId()).append("')");
            return sb.toString();
        } else {
            int maxX = inputs.stream().mapToInt(s -> s.getGridX()).max().orElse(2);
            int maxY = inputs.stream().mapToInt(s -> s.getGridY()).max().orElse(2);
            int cols = Math.max(maxX + 1, 1);
            int rows = Math.max(maxY + 1, 1);

            Map<String, RecipeSlot> grid = new HashMap<>();
            for (RecipeSlot slot : inputs) {
                if (slot.getGridX() >= 0 && slot.getGridY() >= 0) {
                    grid.put(slot.getGridY() + "," + slot.getGridX(), slot);
                }
            }

            StringBuilder pattern = new StringBuilder();
            pattern.append("[");
            for (int row = 0; row < rows; row++) {
                if (row > 0) pattern.append(", ");
                StringBuilder rowStr = new StringBuilder("'");
                for (int col = 0; col < cols; col++) {
                    RecipeSlot slot = grid.get(row + "," + col);
                    if (slot != null && slot.getId() != null) {
                        char key = (char)('A' + (row * cols + col));
                        rowStr.append(key);
                    } else {
                        rowStr.append(' ');
                    }
                }
                rowStr.append("'");
                pattern.append(rowStr);
            }
            pattern.append("]");

            StringBuilder keys = new StringBuilder("{");
            boolean first = true;
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    RecipeSlot slot = grid.get(row + "," + col);
                    if (slot != null && slot.getId() != null) {
                        char key = (char)('A' + (row * cols + col));
                        if (!first) keys.append(", ");
                        keys.append(key).append(": ").append(slotToIngredient(slot));
                        first = false;
                    }
                }
            }
            keys.append("}");

            return "event.shaped(" + outputStr + ", " + pattern + ", " + keys + ").id('" + entry.getId() + "')";
        }
    }

    private static String buildSimpleCookingScript(RecipeType type, CRERecipeEntry entry, List<RecipeSlot> inputs, RecipeSlot output) {
        if (inputs.isEmpty()) return null;
        String ns = type.getKubeJSNamespace();
        return "event." + ns + "(" + slotToItem(output) + ", " + slotToIngredient(inputs.get(0)) + ").id('" + entry.getId() + "')";
    }

    private static String buildStonecuttingScript(CRERecipeEntry entry, List<RecipeSlot> inputs, RecipeSlot output) {
        if (inputs.isEmpty()) return null;
        return "event.stonecutting(" + slotToItem(output) + ", " + slotToIngredient(inputs.get(0)) + ").id('" + entry.getId() + "')";
    }

    private static String buildSmithingScript(CRERecipeEntry entry, List<RecipeSlot> inputs, RecipeSlot output) {
        if (inputs.size() < 2) return null;
        return "event.smithing(" + slotToItem(output) + ", " + slotToIngredient(inputs.get(0)) + ", " + slotToIngredient(inputs.get(1)) + ").id('" + entry.getId() + "')";
    }

    private static String buildCreateMultiIOScript(RecipeType type, CRERecipeEntry entry, List<RecipeSlot> inputs, List<RecipeSlot> outputs) {
        StringBuilder sb = new StringBuilder();
        sb.append("event.recipes.").append(type.getKubeJSNamespace()).append("(");

        // outputs array
        sb.append("[");
        for (int i = 0; i < outputs.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(slotToItemWithChance(outputs.get(i)));
        }
        sb.append("], [");

        // inputs array
        for (int i = 0; i < inputs.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(slotToIngredient(inputs.get(i)));
        }
        sb.append("])");

        // heat
        String heat = entry.getHeatLevel();
        if ("heated".equals(heat)) sb.append(".heated()");
        else if ("superheated".equals(heat)) sb.append(".superHeated()");

        sb.append(".id('").append(entry.getId()).append("')");
        return sb.toString();
    }

    private static String buildCreateSingleInMultiOut(RecipeType type, CRERecipeEntry entry, List<RecipeSlot> inputs, List<RecipeSlot> outputs) {
        if (inputs.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("event.recipes.").append(type.getKubeJSNamespace()).append("([");
        for (int i = 0; i < outputs.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(slotToItemWithChance(outputs.get(i)));
        }
        sb.append("], ").append(slotToIngredient(inputs.get(0))).append(").id('").append(entry.getId()).append("')");
        return sb.toString();
    }

    private static String buildFanScript(RecipeType type, CRERecipeEntry entry, List<RecipeSlot> inputs, List<RecipeSlot> outputs) {
        if (inputs.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        sb.append("event.recipes.").append(type.getKubeJSNamespace()).append("([");
        for (int i = 0; i < outputs.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(slotToItemWithChance(outputs.get(i)));
        }
        sb.append("], ").append(slotToIngredient(inputs.get(0))).append(").id('").append(entry.getId()).append("')");
        return sb.toString();
    }

    private static String buildDeployingScript(CRERecipeEntry entry, List<RecipeSlot> inputs, RecipeSlot output) {
        if (inputs.size() < 2) return null;
        return "event.recipes.create.deploying(" + slotToItem(output) + ", [" +
            slotToIngredient(inputs.get(0)) + ", " + slotToIngredient(inputs.get(1)) + "]).id('" + entry.getId() + "')";
    }

    private static String buildEmptyingScript(CRERecipeEntry entry, List<RecipeSlot> inputs, List<RecipeSlot> outputs) {
        if (inputs.isEmpty() || outputs.isEmpty()) return null;
        List<RecipeSlot> items = outputs.stream().filter(s -> !s.isFluid()).toList();
        List<RecipeSlot> fluids = outputs.stream().filter(RecipeSlot::isFluid).toList();
        String itemOut = items.isEmpty() ? "Item.of('minecraft:air')" : slotToItem(items.get(0));
        String fluidOut = fluids.isEmpty() ? "Fluid.of('minecraft:water', 1000)" : slotToFluid(fluids.get(0));
        return "event.recipes.create.emptying([" + itemOut + ", " + fluidOut + "], " + slotToIngredient(inputs.get(0)) + ").id('" + entry.getId() + "')";
    }

    private static String buildFillingScript(CRERecipeEntry entry, List<RecipeSlot> inputs, List<RecipeSlot> outputs) {
        if (outputs.isEmpty()) return null;
        List<RecipeSlot> itemInputs = inputs.stream().filter(s -> !s.isFluid()).toList();
        List<RecipeSlot> fluidInputs = inputs.stream().filter(RecipeSlot::isFluid).toList();
        String itemIn = itemInputs.isEmpty() ? "Item.of('minecraft:air')" : slotToIngredient(itemInputs.get(0));
        String fluidIn = fluidInputs.isEmpty() ? "Fluid.of('minecraft:water', 1000)" : slotToFluid(fluidInputs.get(0));
        return "event.recipes.create.filling(" + slotToItem(outputs.get(0)) + ", [" + itemIn + ", " + fluidIn + "]).id('" + entry.getId() + "')";
    }

    private static String slotToItem(RecipeSlot slot) {
        if (slot == null) return "Item.of('minecraft:air')";
        if (slot.isFluid()) return slotToFluid(slot);
        int count = slot.getCount();
        String id = slot.getId() != null ? slot.getId() : "minecraft:air";
        if (count <= 1) return "Item.of('" + id + "')";
        return "Item.of('" + id + "', " + count + ")";
    }

    private static String slotToItemWithChance(RecipeSlot slot) {
        if (slot == null) return "Item.of('minecraft:air')";
        if (slot.isFluid()) return slotToFluid(slot);
        String base = slotToItem(slot);
        if (slot.getChance() < 1.0f && slot.getChance() >= 0f) {
            return base + ".withChance(" + slot.getChance() + ")";
        }
        return base;
    }

    private static String slotToIngredient(RecipeSlot slot) {
        if (slot == null) return "Item.of('minecraft:air')";
        if (slot.isFluid()) return slotToFluid(slot);
        if (slot.getTag() != null) return "'" + slot.getTag() + "'";
        String id = slot.getId() != null ? slot.getId() : "minecraft:air";
        return "'" + id + "'";
    }

    private static String slotToFluid(RecipeSlot slot) {
        String id = slot.getId() != null ? slot.getId() : "minecraft:water";
        long amount = slot.getFluidAmount();
        return "Fluid.of('" + id + "', " + amount + ")";
    }

    private static String escapeJs(String s) {
        return s.replace("'", "\\'").replace("\\", "\\\\");
    }
}
