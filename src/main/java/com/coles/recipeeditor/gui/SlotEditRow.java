package com.coles.recipeeditor.gui;

import com.coles.recipeeditor.recipe.RecipeSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SlotEditRow {
    public RecipeSlot slot;

    // Live text fields (basic text input since EditBox requires Screen context)
    private String idText;
    private String countText;
    private String chanceText;
    private String fluidAmountText;

    private boolean editingId = false;
    private boolean editingCount = false;
    private boolean editingChance = false;
    private boolean editingFluid = false;

    private int renderX, renderY;
    private int fieldWidth = 140;

    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int BOX_BG = 0xFF0A0A1A;
    private static final int BOX_BORDER = 0xFF334455;
    private static final int ACTIVE_BORDER = 0xFFE94560;

    public SlotEditRow(RecipeSlot slot) {
        this.slot = slot;
        this.idText = slot.getId() != null ? slot.getId() : "";
        this.countText = String.valueOf(slot.getCount());
        this.chanceText = String.format("%.2f", slot.getChance());
        this.fluidAmountText = String.valueOf(slot.getFluidAmount());
    }

    public int render(GuiGraphics gfx, int x, int y, int mouseX, int mouseY, boolean showFluid, boolean isOutput) {
        renderX = x;
        renderY = y;
        Minecraft mc = Minecraft.getInstance();

        // ID field label
        gfx.drawString(mc.font, "§7ID:", x, y + 2, TEXT_COLOR);
        drawTextField(gfx, x + 22, y, fieldWidth, idText, editingId, mouseX, mouseY);

        // Count field
        int cx = x + 22 + fieldWidth + 5;
        gfx.drawString(mc.font, "§7x", cx, y + 2, TEXT_COLOR);
        drawTextField(gfx, cx + 8, y, 30, countText, editingCount, mouseX, mouseY);

        // Chance field (outputs only)
        if (isOutput) {
            int chx = cx + 8 + 30 + 5;
            gfx.drawString(mc.font, "§7%:", chx, y + 2, TEXT_COLOR);
            drawTextField(gfx, chx + 14, y, 35, chanceText, editingChance, mouseX, mouseY);
        }

        // Fluid amount
        if (showFluid && slot.isFluid()) {
            int fx = x + 22 + fieldWidth + 5 + 8 + 30 + 5 + 14 + 35 + 5;
            gfx.drawString(mc.font, "§bmB:", fx, y + 2, TEXT_COLOR);
            drawTextField(gfx, fx + 22, y, 45, fluidAmountText, editingFluid, mouseX, mouseY);
        }

        // Special type toggle (cycle: NORMAL → CATALYST → TOOL)
        String specialLabel = switch (slot.getSpecialType()) {
            case CATALYST -> "§6[CATALYST]";
            case TOOL -> "§d[TOOL]";
            default -> "§7[NORMAL]";
        };
        gfx.drawString(mc.font, specialLabel, x + width() - 70, y + 2, TEXT_COLOR);

        return y + 14;
    }

    private void drawTextField(GuiGraphics gfx, int x, int y, int w, String text, boolean active, int mouseX, int mouseY) {
        int border = active ? ACTIVE_BORDER : BOX_BORDER;
        gfx.fill(x - 1, y - 1, x + w + 1, y + 10, border);
        gfx.fill(x, y, x + w, y + 9, BOX_BG);
        String display = text.length() > w / 5 ? text.substring(text.length() - w / 5) : text;
        gfx.drawString(Minecraft.getInstance().font, display, x + 2, y + 1, TEXT_COLOR);
    }

    private int width() { return 400; }

    public boolean handleClick(double mouseX, double mouseY) {
        // Simple click-to-focus logic (basic implementation)
        int y = renderY;
        int x = renderX;

        int idX = x + 22;
        if (inBox(mouseX, mouseY, idX, y, fieldWidth)) {
            editingId = true; editingCount = false; editingChance = false; editingFluid = false;
            return true;
        }
        int cx = idX + fieldWidth + 5 + 8;
        if (inBox(mouseX, mouseY, cx, y, 30)) {
            editingCount = true; editingId = false; editingChance = false; editingFluid = false;
            return true;
        }
        int chx = cx + 30 + 5 + 14;
        if (inBox(mouseX, mouseY, chx, y, 35)) {
            editingChance = true; editingId = false; editingCount = false; editingFluid = false;
            return true;
        }
        // Special type toggle
        int spX = x + width() - 70;
        if (inBox(mouseX, mouseY, spX, y, 65)) {
            cycleSpecialType();
            return true;
        }
        return false;
    }

    private boolean inBox(double mx, double my, int bx, int by, int bw) {
        return mx >= bx && mx <= bx + bw && my >= by && my <= by + 12;
    }

    private void cycleSpecialType() {
        RecipeSlot.SpecialType current = slot.getSpecialType();
        slot.setSpecialType(switch (current) {
            case NORMAL -> RecipeSlot.SpecialType.CATALYST;
            case CATALYST -> RecipeSlot.SpecialType.TOOL;
            case TOOL -> RecipeSlot.SpecialType.NORMAL;
        });
    }

    public void charTyped(char c) {
        if (editingId) { idText += c; slot.setId(idText); }
        else if (editingCount) { if (Character.isDigit(c)) { countText += c; try { slot.setCount(Integer.parseInt(countText)); } catch (Exception ignored) {} } }
        else if (editingChance) { if (Character.isDigit(c) || c == '.') { chanceText += c; try { slot.setChance(Float.parseFloat(chanceText)); } catch (Exception ignored) {} } }
        else if (editingFluid) { if (Character.isDigit(c)) { fluidAmountText += c; try { slot.setFluidAmount(Long.parseLong(fluidAmountText)); } catch (Exception ignored) {} } }
    }

    public void backspace() {
        if (editingId && !idText.isEmpty()) { idText = idText.substring(0, idText.length() - 1); slot.setId(idText.isEmpty() ? null : idText); }
        else if (editingCount && countText.length() > 1) { countText = countText.substring(0, countText.length() - 1); }
        else if (editingChance && chanceText.length() > 1) { chanceText = chanceText.substring(0, chanceText.length() - 1); }
        else if (editingFluid && fluidAmountText.length() > 1) { fluidAmountText = fluidAmountText.substring(0, fluidAmountText.length() - 1); }
    }
}
