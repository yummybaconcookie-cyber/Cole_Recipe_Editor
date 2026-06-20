package com.coles.recipeeditor.gui;

import com.coles.recipeeditor.network.CRENetworkHandler;
import com.coles.recipeeditor.recipe.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class CRECreatePanel {

    private final CREScreen parent;
    private CRERecipeEntry workingEntry;
    private final int x, y, width, height;

    private EditBox recipeIdBox;
    private EditBox rawStringBox;

    private RecipeType selectedType;
    private boolean typePickerOpen = false;
    private boolean showRawMode = false;

    private Button typePickerBtn;
    private Button shapelessBtn;
    private Button heatNoneBtn, heatHotBtn, heatSuperBtn;
    private Button addInputBtn, addOutputBtn;
    private Button saveRecipeBtn;
    private Button toggleRawModeBtn;

    private List<SlotEditRow> inputRows = new ArrayList<>();
    private List<SlotEditRow> outputRows = new ArrayList<>();

    // For crafting grid
    private CRECraftingGrid craftingGrid;

    // Hovering slot ID display
    private String hoverSlotId = null;

    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int PANEL_COLOR = 0xFF16213E;
    private static final int ACCENT_COLOR = 0xFF0F3460;
    private static final int YELLOW = 0xFFFFEB3B;
    private static final int GREEN = 0xFF4CAF50;

    public CRECreatePanel(CREScreen parent, CRERecipeEntry cloneFrom, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (cloneFrom != null) {
            workingEntry = cloneFrom;
            try {
                selectedType = RecipeType.valueOf(cloneFrom.getType());
            } catch (Exception e) {
                selectedType = RecipeType.CRAFTING;
            }
            for (RecipeSlot s : cloneFrom.getInputs()) inputRows.add(new SlotEditRow(s));
            for (RecipeSlot s : cloneFrom.getOutputs()) outputRows.add(new SlotEditRow(s));
        } else {
            workingEntry = new CRERecipeEntry();
            workingEntry.setId("cre:new_recipe_" + System.currentTimeMillis());
            workingEntry.setType("CRAFTING");
            workingEntry.setEnabled(true);
            workingEntry.setModOrigin("cre");
            workingEntry.setHeatLevel("none");
            selectedType = RecipeType.CRAFTING;
        }
    }

    public void init() {
        int controlY = y;

        // Recipe ID field
        recipeIdBox = new EditBox(Minecraft.getInstance().font, x, controlY, 300, 16,
            Component.literal("Recipe ID (e.g. cre:my_recipe)"));
        recipeIdBox.setValue(workingEntry.getId());
        recipeIdBox.setResponder(id -> workingEntry.setId(id));
        parent.addWidget(recipeIdBox);

        // Recipe type picker
        typePickerBtn = Button.builder(
            Component.literal("Type: " + selectedType.getDisplayName()),
            b -> typePickerOpen = !typePickerOpen
        ).pos(x + 310, controlY).size(200, 16).build();
        parent.addWidget(typePickerBtn);

        controlY += 22;

        // Shapeless toggle (crafting only)
        if (selectedType == RecipeType.CRAFTING) {
            shapelessBtn = Button.builder(
                Component.literal(workingEntry.isShapeless() ? "§aShapeless: ON" : "§7Shapeless: OFF"),
                b -> {
                    workingEntry.setShapeless(!workingEntry.isShapeless());
                    shapelessBtn.setMessage(Component.literal(workingEntry.isShapeless() ? "§aShapeless: ON" : "§7Shapeless: OFF"));
                    rebuildCraftingGrid();
                }
            ).pos(x, controlY).size(110, 14).build();
            parent.addWidget(shapelessBtn);
        }

        // Heat level (for applicable types)
        if (selectedType != null && selectedType.supportsHeat()) {
            heatNoneBtn = Button.builder(Component.literal("No Heat"), b -> setHeat("none"))
                .pos(x + 120, controlY).size(60, 14).build();
            heatHotBtn = Button.builder(Component.literal("§6Heated"), b -> setHeat("heated"))
                .pos(x + 184, controlY).size(60, 14).build();
            heatSuperBtn = Button.builder(Component.literal("§cSuper"), b -> setHeat("superheated"))
                .pos(x + 248, controlY).size(70, 14).build();
            parent.addWidget(heatNoneBtn);
            parent.addWidget(heatHotBtn);
            parent.addWidget(heatSuperBtn);
        }

        controlY += 20;

        // Crafting grid (for crafting type)
        if (selectedType == RecipeType.CRAFTING && !workingEntry.isShapeless()) {
            craftingGrid = new CRECraftingGrid(x, controlY, inputRows, 9, 9);
        } else {
            // Slot list mode
            addInputBtn = Button.builder(Component.literal("+ Add Input"), b -> {
                RecipeSlot slot = new RecipeSlot();
                slot.setCount(1);
                slot.setChance(1.0f);
                inputRows.add(new SlotEditRow(slot));
            }).pos(x, controlY).size(90, 14).build();
            parent.addWidget(addInputBtn);

            addOutputBtn = Button.builder(Component.literal("+ Add Output"), b -> {
                RecipeSlot slot = new RecipeSlot();
                slot.setCount(1);
                slot.setChance(1.0f);
                outputRows.add(new SlotEditRow(slot));
            }).pos(x + 100, controlY).size(90, 14).build();
            parent.addWidget(addOutputBtn);
        }

        // Raw string toggle
        toggleRawModeBtn = Button.builder(
            Component.literal(showRawMode ? "§dSwitch to Visual" : "§dView/Edit String"),
            b -> showRawMode = !showRawMode
        ).pos(x + width - 130, y).size(125, 16).build();
        parent.addWidget(toggleRawModeBtn);

        // Raw string box (hidden unless showRawMode)
        rawStringBox = new EditBox(Minecraft.getInstance().font, x, y + height - 50, width, 40,
            Component.literal("Paste raw recipe string here..."));
        rawStringBox.setMaxLength(4096);
        if (workingEntry.getRawScript() != null) rawStringBox.setValue(workingEntry.getRawScript());
        rawStringBox.setResponder(s -> workingEntry.setRawScript(s));

        // Save button
        saveRecipeBtn = Button.builder(Component.literal("§aSave Recipe"), b -> handleSave())
            .pos(x + width / 2 - 50, y + height - 22).size(100, 18).build();
        parent.addWidget(saveRecipeBtn);
    }

    private void setHeat(String level) {
        workingEntry.setHeatLevel(level);
    }

    private void rebuildCraftingGrid() {
        // Rebuild when shapeless toggle changes
    }

    private void handleSave() {
        // Collect inputs/outputs from rows
        List<RecipeSlot> inputs = new ArrayList<>();
        for (SlotEditRow row : inputRows) {
            if (row.slot.getId() != null && !row.slot.getId().isEmpty()) {
                inputs.add(row.slot);
            }
        }
        if (craftingGrid != null) {
            inputs = craftingGrid.collectSlots();
        }
        workingEntry.setInputs(inputs);

        List<RecipeSlot> outputs = new ArrayList<>();
        for (SlotEditRow row : outputRows) {
            if (row.slot.getId() != null && !row.slot.getId().isEmpty()) {
                outputs.add(row.slot);
            }
        }
        workingEntry.setOutputs(outputs);
        workingEntry.setType(selectedType.name());

        // Generate script
        com.coles.recipeeditor.kubejs.KubeJSScriptManager.writeScripts();
        RecipeStateManager.getInstance().addCustomRecipe(workingEntry);
        CRENetworkHandler.sendAddRecipe(workingEntry);

        parent.onRecipeCreated(workingEntry);
        Minecraft.getInstance().player.displayClientMessage(
            Component.literal("§a[CRE] Recipe saved! Run /CRE reload to apply."), false
        );
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        gfx.fill(x - 10, y - 5, x + width + 10, y + height + 5, PANEL_COLOR);
        gfx.fill(x - 10, y - 5, x + width + 10, y + 14, ACCENT_COLOR);

        gfx.drawString(Minecraft.getInstance().font, "§6Create / Edit Recipe", x, y, TEXT_COLOR);

        if (typePickerOpen) {
            renderTypePicker(gfx, mouseX, mouseY);
        }

        if (showRawMode && rawStringBox != null) {
            gfx.fill(x, y + height - 55, x + width, y + height - 5, 0xFF0A0A1A);
            gfx.drawString(Minecraft.getInstance().font, "§dRaw Script String (paste here to import):", x, y + height - 58, TEXT_COLOR);
            rawStringBox.render(gfx, mouseX, mouseY, partialTick);
        } else {
            // Render slot rows
            int rowY = y + 75;

            if (craftingGrid != null && selectedType == RecipeType.CRAFTING && !workingEntry.isShapeless()) {
                craftingGrid.render(gfx, mouseX, mouseY, partialTick);
            } else {
                gfx.drawString(Minecraft.getInstance().font, "§eInputs:", x, rowY, YELLOW);
                rowY += 12;
                for (int i = 0; i < inputRows.size(); i++) {
                    rowY = inputRows.get(i).render(gfx, x, rowY, mouseX, mouseY, selectedType.supportsFluid(), false);
                }

                rowY += 8;
                gfx.drawString(Minecraft.getInstance().font, "§aOutputs:", x, rowY, 0xFF4CAF50);
                rowY += 12;
                for (int i = 0; i < outputRows.size(); i++) {
                    rowY = outputRows.get(i).render(gfx, x, rowY, mouseX, mouseY, selectedType.supportsFluid(), true);
                }
            }
        }
    }

    private void renderTypePicker(GuiGraphics gfx, int mouseX, int mouseY) {
        RecipeType[] types = RecipeType.values();
        int pickerX = x + 310;
        int pickerY = y + 18;
        int pickerW = 220;
        int lineH = 11;
        gfx.fill(pickerX, pickerY, pickerX + pickerW, pickerY + types.length * lineH + 4, 0xFF050510);
        gfx.fill(pickerX, pickerY, pickerX + pickerW, pickerY + 1, ACCENT_COLOR);

        for (int i = 0; i < types.length; i++) {
            int ty = pickerY + 2 + i * lineH;
            boolean hovered = mouseX >= pickerX && mouseX <= pickerX + pickerW
                && mouseY >= ty && mouseY <= ty + lineH;
            if (hovered) gfx.fill(pickerX, ty, pickerX + pickerW, ty + lineH, ACCENT_COLOR);
            boolean isSelected = types[i] == selectedType;
            int col = isSelected ? YELLOW : TEXT_COLOR;
            String prefix = isSelected ? "§a> " : "  ";
            String name = types[i].getDisplayName();
            if (name.length() > 27) name = name.substring(0, 25) + "..";
            gfx.drawString(Minecraft.getInstance().font, prefix + name, pickerX + 3, ty + 1, col);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (typePickerOpen) {
            RecipeType[] types = RecipeType.values();
            int pickerX = x + 310;
            int pickerY = y + 18;
            int lineH = 11;
            for (int i = 0; i < types.length; i++) {
                int ty = pickerY + 2 + i * lineH;
                if (mouseX >= pickerX && mouseX <= pickerX + 220
                    && mouseY >= ty && mouseY <= ty + lineH) {
                    selectedType = types[i];
                    workingEntry.setType(selectedType.name());
                    typePickerBtn.setMessage(Component.literal("Type: " + selectedType.getDisplayName()));
                    typePickerOpen = false;
                    return true;
                }
            }
            typePickerOpen = false;
        }

        // Handle slot row clicks
        for (SlotEditRow row : inputRows) {
            if (row.handleClick(mouseX, mouseY)) return true;
        }
        for (SlotEditRow row : outputRows) {
            if (row.handleClick(mouseX, mouseY)) return true;
        }

        if (craftingGrid != null) {
            return craftingGrid.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
}
