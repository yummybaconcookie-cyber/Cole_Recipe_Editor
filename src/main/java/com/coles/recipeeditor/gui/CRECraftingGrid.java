package com.coles.recipeeditor.gui;

import com.coles.recipeeditor.recipe.RecipeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class CRECraftingGrid {

    private final int x, y;
    private final int cols, rows;
    private final RecipeSlot[][] grid;
    private final List<SlotEditRow> slotRows;

    private int selectedSlotX = -1;
    private int selectedSlotY = -1;
    private boolean itemPickerOpen = false;
    private String itemPickerQuery = "";

    private static final int SLOT_SIZE = 18;
    private static final int SLOT_BG = 0xFF2A2A3E;
    private static final int SLOT_BORDER = 0xFF445566;
    private static final int SLOT_SELECTED = 0xFFE94560;
    private static final int TEXT_COLOR = 0xFFE0E0E0;

    public CRECraftingGrid(int x, int y, List<SlotEditRow> slotRows, int cols, int rows) {
        this.x = x;
        this.y = y;
        this.slotRows = slotRows;
        this.cols = cols;
        this.rows = rows;
        this.grid = new RecipeSlot[rows][cols];

        // Pre-populate from existing slot rows
        for (SlotEditRow row : slotRows) {
            RecipeSlot slot = row.slot;
            if (slot.getGridX() >= 0 && slot.getGridY() >= 0
                && slot.getGridY() < rows && slot.getGridX() < cols) {
                grid[slot.getGridY()][slot.getGridX()] = slot;
            }
        }
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();

        // Grid label
        gfx.drawString(mc.font, "§eCrafting Grid (9x9 - click a slot to set item):", x, y - 10, TEXT_COLOR);

        // Grid hint
        gfx.drawString(mc.font, "§72x2=Inventory 3x3=Crafting Table 9x9=Mech.Crafter", x, y - 2, 0xFF888888);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int sx = x + col * SLOT_SIZE;
                int sy = y + 10 + row * SLOT_SIZE;

                boolean selected = selectedSlotX == col && selectedSlotY == row;
                boolean hovered = mouseX >= sx && mouseX <= sx + SLOT_SIZE - 1
                    && mouseY >= sy && mouseY <= sy + SLOT_SIZE - 1;

                int border = selected ? SLOT_SELECTED : (hovered ? 0xFF6688AA : SLOT_BORDER);
                gfx.fill(sx - 1, sy - 1, sx + SLOT_SIZE, sy + SLOT_SIZE, border);
                gfx.fill(sx, sy, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, SLOT_BG);

                RecipeSlot slot = grid[row][col];
                if (slot != null && slot.getId() != null) {
                    // Show abbreviated ID
                    String id = slot.getId();
                    String shortId = id.contains(":") ? id.split(":")[1] : id;
                    if (shortId.length() > 2) shortId = shortId.substring(0, 2);
                    gfx.drawString(mc.font, shortId, sx + 2, sy + 5, 0xFFFFFFFF);

                    // Special type indicator
                    if (slot.getSpecialType() == RecipeSlot.SpecialType.CATALYST) {
                        gfx.fill(sx + SLOT_SIZE - 5, sy, sx + SLOT_SIZE - 1, sy + 4, 0xFFFFAA00);
                    } else if (slot.getSpecialType() == RecipeSlot.SpecialType.TOOL) {
                        gfx.fill(sx + SLOT_SIZE - 5, sy, sx + SLOT_SIZE - 1, sy + 4, 0xFFAA00FF);
                    }
                }

                // Tooltip on hover
                if (hovered && slot != null && slot.getId() != null) {
                    gfx.renderTooltip(mc.font,
                        net.minecraft.network.chat.Component.literal(slot.getId()
                            + (slot.getSpecialType() != RecipeSlot.SpecialType.NORMAL ? " [" + slot.getSpecialType().name() + "]" : "")),
                        mouseX, mouseY);
                }
            }
        }

        // Item picker popup
        if (itemPickerOpen) {
            renderItemPicker(gfx, mouseX, mouseY);
        }
    }

    private void renderItemPicker(GuiGraphics gfx, int mouseX, int mouseY) {
        int px = x + cols * SLOT_SIZE + 5;
        int py = y + 10;
        int pw = 180;
        int ph = 140;

        gfx.fill(px, py, px + pw, py + ph, 0xFF050510);
        gfx.fill(px, py, px + pw, py + 1, 0xFF334455);
        gfx.fill(px, py + 14, px + pw, py + 15, 0xFF334455);

        Minecraft mc = Minecraft.getInstance();
        gfx.drawString(mc.font, "§eSearch item/fluid:", px + 3, py + 3, TEXT_COLOR);

        // Search box visual
        gfx.fill(px + 2, py + 16, px + pw - 2, py + 27, 0xFF0A0A1A);
        gfx.drawString(mc.font, itemPickerQuery + "|", px + 4, py + 18, 0xFF00FF88);

        // Results from game registry (filtered)
        List<String> results = getFilteredItems(itemPickerQuery);
        int resultY = py + 30;
        for (String result : results) {
            if (resultY > py + ph - 12) break;
            boolean hov = mouseX >= px && mouseX <= px + pw && mouseY >= resultY && mouseY <= resultY + 10;
            if (hov) gfx.fill(px, resultY, px + pw, resultY + 10, 0xFF0F3460);
            String display = result.length() > 26 ? result.substring(0, 24) + ".." : result;
            gfx.drawString(mc.font, display, px + 3, resultY + 1, hov ? 0xFFFFFFFF : 0xFFAAAAAA);
            resultY += 11;
        }
    }

    private List<String> getFilteredItems(String query) {
        List<String> results = new ArrayList<>();
        String q = query.toLowerCase(Locale.ROOT);
        // Pull from game registry
        try {
            net.minecraft.core.registries.BuiltInRegistries.ITEM.keySet().stream()
                .map(rl -> rl.toString())
                .filter(s -> q.isEmpty() || s.contains(q))
                .limit(10)
                .forEach(results::add);
            // Also add fluids
            net.minecraft.core.registries.BuiltInRegistries.FLUID.keySet().stream()
                .map(rl -> "fluid:" + rl.toString())
                .filter(s -> q.isEmpty() || s.contains(q))
                .limit(5)
                .forEach(results::add);
        } catch (Exception ignored) {}
        return results;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Item picker result click
        if (itemPickerOpen) {
            int px = x + cols * SLOT_SIZE + 5;
            int py = y + 10;
            int pw = 180;

            List<String> results = getFilteredItems(itemPickerQuery);
            int resultY = py + 30;
            for (String result : results) {
                if (mouseX >= px && mouseX <= px + pw && mouseY >= resultY && mouseY <= resultY + 10) {
                    applyItemToSelectedSlot(result);
                    itemPickerOpen = false;
                    return true;
                }
                resultY += 11;
            }
            // Click outside picker closes it
            if (mouseX < px || mouseX > px + pw || mouseY < py || mouseY > py + 140) {
                itemPickerOpen = false;
                return true;
            }
            return true;
        }

        // Slot click
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int sx = x + col * SLOT_SIZE;
                int sy = y + 10 + row * SLOT_SIZE;
                if (mouseX >= sx && mouseX <= sx + SLOT_SIZE - 1
                    && mouseY >= sy && mouseY <= sy + SLOT_SIZE - 1) {
                    if (button == 1) {
                        // Right click = clear slot
                        grid[row][col] = null;
                    } else {
                        selectedSlotX = col;
                        selectedSlotY = row;
                        itemPickerOpen = true;
                        itemPickerQuery = "";
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean charTyped(char c) {
        if (itemPickerOpen) {
            itemPickerQuery += c;
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode) {
        if (itemPickerOpen && keyCode == 259) { // Backspace
            if (!itemPickerQuery.isEmpty()) {
                itemPickerQuery = itemPickerQuery.substring(0, itemPickerQuery.length() - 1);
            }
            return true;
        }
        return false;
    }

    private void applyItemToSelectedSlot(String itemId) {
        if (selectedSlotX < 0 || selectedSlotY < 0) return;
        RecipeSlot slot = grid[selectedSlotY][selectedSlotX];
        if (slot == null) {
            slot = new RecipeSlot();
            slot.setGridX(selectedSlotX);
            slot.setGridY(selectedSlotY);
            slot.setCount(1);
            slot.setChance(1.0f);
            grid[selectedSlotY][selectedSlotX] = slot;
        }
        boolean isFluid = itemId.startsWith("fluid:");
        if (isFluid) {
            slot.setId(itemId.substring(6));
            slot.setSlotType(RecipeSlot.SlotType.FLUID);
        } else {
            slot.setId(itemId);
            slot.setSlotType(RecipeSlot.SlotType.ITEM);
        }
    }

    public List<RecipeSlot> collectSlots() {
        List<RecipeSlot> result = new ArrayList<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (grid[row][col] != null && grid[row][col].getId() != null) {
                    result.add(grid[row][col]);
                }
            }
        }
        return result;
    }
}
