package com.coles.recipeeditor.gui;

import com.coles.recipeeditor.network.CRENetworkHandler;
import com.coles.recipeeditor.recipe.CRERecipeEntry;
import com.coles.recipeeditor.recipe.RecipeSlot;
import com.coles.recipeeditor.recipe.RecipeStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CREDetailPanel {

    private final CREScreen parent;
    private final String recipeId;
    private final int x, y, width, height;

    private CRERecipeEntry entry;
    private boolean isCustomRecipe;

    private Button toggleBtn;
    private Button editBtn;
    private Button cloneBtn;
    private Button deleteBtn;
    private Button viewStringBtn;
    private boolean showingRawString = false;

    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int GREEN = 0xFF4CAF50;
    private static final int RED = 0xFFF44336;
    private static final int YELLOW = 0xFFFFEB3B;
    private static final int PANEL_COLOR = 0xFF16213E;
    private static final int ACCENT_COLOR = 0xFF0F3460;

    public CREDetailPanel(CREScreen parent, String recipeId, int x, int y, int width, int height) {
        this.parent = parent;
        this.recipeId = recipeId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void init() {
        // Try to find in custom recipes first
        entry = RecipeStateManager.getInstance().getCustomRecipe(recipeId).orElse(null);
        isCustomRecipe = (entry != null);

        // If not custom, create a display-only entry from server data
        if (entry == null) {
            entry = new CRERecipeEntry();
            entry.setId(recipeId);
            entry.setType("UNKNOWN");
            entry.setEnabled(!RecipeStateManager.getInstance().isDisabled(recipeId));
            entry.setModOrigin("?");
        }

        int btnY = y + height - 25;
        int btnX = x;

        // Toggle enable/disable
        boolean enabled = entry.isEnabled() && !RecipeStateManager.getInstance().isDisabled(recipeId);
        toggleBtn = Button.builder(
            Component.literal(enabled ? "§cDisable" : "§aEnable"),
            b -> handleToggle()
        ).pos(btnX, btnY).size(70, 18).build();
        parent.addWidget(toggleBtn);

        // Edit (custom recipes only)
        if (isCustomRecipe) {
            editBtn = Button.builder(Component.literal("§eEdit"), b -> handleEdit())
                .pos(btnX + 75, btnY).size(55, 18).build();
            parent.addWidget(editBtn);
        }

        // Clone
        cloneBtn = Button.builder(Component.literal("§bClone"), b -> handleClone())
            .pos(btnX + (isCustomRecipe ? 135 : 75), btnY).size(55, 18).build();
        parent.addWidget(cloneBtn);

        // View/Hide recipe string
        viewStringBtn = Button.builder(Component.literal("§dString"), b -> showingRawString = !showingRawString)
            .pos(btnX + (isCustomRecipe ? 195 : 135), btnY).size(55, 18).build();
        parent.addWidget(viewStringBtn);

        // Delete (only for disabled custom recipes)
        if (isCustomRecipe) {
            deleteBtn = Button.builder(Component.literal("§4Delete"), b -> handleDelete())
                .pos(x + width - 70, btnY).size(65, 18).build();
            parent.addWidget(deleteBtn);
        }
    }

    private void handleToggle() {
        boolean currentlyEnabled = entry.isEnabled() && !RecipeStateManager.getInstance().isDisabled(recipeId);
        if (currentlyEnabled) {
            RecipeStateManager.getInstance().disableRecipe(recipeId);
            if (isCustomRecipe) entry.setEnabled(false);
        } else {
            RecipeStateManager.getInstance().enableRecipe(recipeId);
            if (isCustomRecipe) entry.setEnabled(true);
        }
        CRENetworkHandler.sendToggleRecipe(recipeId, !currentlyEnabled);
        boolean newState = !currentlyEnabled;
        toggleBtn.setMessage(Component.literal(newState ? "§cDisable" : "§aEnable"));
        if (deleteBtn != null) {
            deleteBtn.active = !newState;
        }
    }

    private void handleEdit() {
        if (entry != null && isCustomRecipe) {
            parent.openCloneView(entry);
        }
    }

    private void handleClone() {
        if (entry != null) {
            CRERecipeEntry clone = CRERecipeEntry.fromJson(entry.toJson());
            if (clone != null) {
                clone.setId("cre:copy_of_" + entry.getId().replace(":", "_").replace("/", "_"));
                clone.setModOrigin("cre");
                parent.openCloneView(clone);
            }
        }
    }

    private void handleDelete() {
        if (isCustomRecipe && !entry.isEnabled()) {
            RecipeStateManager.getInstance().removeCustomRecipe(recipeId);
            CRENetworkHandler.sendDeleteRecipe(recipeId);
            parent.onRecipeCreated(new CRERecipeEntry()); // refresh list
        } else {
            Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§c[CRE] You must disable this recipe before deleting it."), false
            );
        }
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        if (entry == null) return;

        gfx.fill(x - 10, y - 5, x + width + 10, y + height + 5, PANEL_COLOR);
        gfx.fill(x - 10, y - 5, x + width + 10, y + 14, ACCENT_COLOR);

        // Title
        gfx.drawString(Minecraft.getInstance().font, "§6Recipe Detail", x, y, TEXT_COLOR);

        int infoY = y + 20;

        // Recipe ID (full, wrapping)
        gfx.drawString(Minecraft.getInstance().font, "§7ID: §f" + entry.getId(), x, infoY, TEXT_COLOR);
        infoY += 12;

        // Type
        gfx.drawString(Minecraft.getInstance().font, "§7Type: §e" + entry.getType(), x, infoY, TEXT_COLOR);
        infoY += 12;

        // Mod origin
        String originColor = "cre".equals(entry.getModOrigin()) ? "§b" : "§a";
        String originDisplay = "cre".equals(entry.getModOrigin()) ? "Cole's Recipe Editor (CRE)" : entry.getModOrigin();
        gfx.drawString(Minecraft.getInstance().font, "§7Origin: " + originColor + originDisplay, x, infoY, TEXT_COLOR);
        infoY += 12;

        // Status
        boolean isDisabled = RecipeStateManager.getInstance().isDisabled(recipeId);
        boolean enabled = entry.isEnabled() && !isDisabled;
        int statusColor = enabled ? GREEN : RED;
        gfx.drawString(Minecraft.getInstance().font, "§7Status: ", x, infoY, TEXT_COLOR);
        gfx.drawString(Minecraft.getInstance().font, enabled ? "Enabled" : "Disabled", x + 46, infoY, statusColor);
        infoY += 12;

        // Heat level (for Create recipes)
        if (entry.getHeatLevel() != null && !"none".equals(entry.getHeatLevel())) {
            String heatColor = "superheated".equals(entry.getHeatLevel()) ? "§c" : "§6";
            gfx.drawString(Minecraft.getInstance().font, "§7Heat: " + heatColor + entry.getHeatLevel(), x, infoY, TEXT_COLOR);
            infoY += 12;
        }

        infoY += 5;
        gfx.fill(x, infoY, x + width, infoY + 1, ACCENT_COLOR);
        infoY += 6;

        if (showingRawString) {
            // Show raw KubeJS script string
            gfx.drawString(Minecraft.getInstance().font, "§dRaw Script String:", x, infoY, TEXT_COLOR);
            infoY += 12;
            String script = entry.getRawScript() != null ? entry.getRawScript() : "(no script available)";
            gfx.fill(x, infoY, x + width, infoY + 60, 0xFF0A0A1A);
            // Word-wrap the script
            List<String> lines = wrapText(script, width - 4, 60);
            int lineY = infoY + 2;
            for (String line : lines) {
                gfx.drawString(Minecraft.getInstance().font, line, x + 2, lineY, 0xFF00FF88);
                lineY += 10;
                if (lineY > infoY + 58) break;
            }
        } else {
            // Visual recipe display
            gfx.drawString(Minecraft.getInstance().font, "§eIngredients:", x, infoY, TEXT_COLOR);
            infoY += 12;

            List<RecipeSlot> inputs = entry.getInputs();
            if (inputs.isEmpty()) {
                gfx.drawString(Minecraft.getInstance().font, "§7  (no ingredients data)", x, infoY, TEXT_DIM);
            } else {
                for (RecipeSlot slot : inputs) {
                    String special = slot.getSpecialType() != null && slot.getSpecialType() != RecipeSlot.SpecialType.NORMAL
                        ? " §7[" + slot.getSpecialType().name() + "]" : "";
                    String fluidSuffix = slot.isFluid() ? " §b(" + slot.getFluidAmount() + " mB)" : "";
                    gfx.drawString(Minecraft.getInstance().font,
                        "  §7- §f" + slot.getDisplayName() + " x" + slot.getCount() + special + fluidSuffix,
                        x, infoY, TEXT_COLOR);
                    infoY += 10;
                }
            }

            infoY += 5;
            gfx.drawString(Minecraft.getInstance().font, "§aOutputs:", x, infoY, TEXT_COLOR);
            infoY += 12;

            List<RecipeSlot> outputs = entry.getOutputs();
            if (outputs.isEmpty()) {
                gfx.drawString(Minecraft.getInstance().font, "§7  (no output data)", x, infoY, TEXT_DIM);
            } else {
                for (RecipeSlot slot : outputs) {
                    String chanceStr = slot.getChance() < 1.0f
                        ? " §e(" + (int)(slot.getChance() * 100) + "%)" : "";
                    String fluidSuffix = slot.isFluid() ? " §b(" + slot.getFluidAmount() + " mB)" : "";
                    gfx.drawString(Minecraft.getInstance().font,
                        "  §7- §f" + slot.getDisplayName() + " x" + slot.getCount() + chanceStr + fluidSuffix,
                        x, infoY, TEXT_COLOR);
                    infoY += 10;
                }
            }
        }
    }

    private List<String> wrapText(String text, int maxWidth, int maxHeight) {
        List<String> lines = new ArrayList<>();
        int maxChars = maxWidth / 6;
        while (text.length() > maxChars) {
            lines.add(text.substring(0, maxChars));
            text = text.substring(maxChars);
        }
        if (!text.isEmpty()) lines.add(text);
        return lines;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) { return false; }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }

    private static final int TEXT_DIM = 0xFF888888;
}
