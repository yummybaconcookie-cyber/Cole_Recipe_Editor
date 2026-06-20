package com.coles.recipeeditor.gui;

import com.coles.recipeeditor.CREMod;
import com.coles.recipeeditor.network.CRENetworkHandler;
import com.coles.recipeeditor.recipe.CRERecipeEntry;
import com.coles.recipeeditor.recipe.RecipeStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class CREScreen extends Screen {

    private static final int BG_COLOR = 0xFF1A1A2E;
    private static final int PANEL_COLOR = 0xFF16213E;
    private static final int ACCENT_COLOR = 0xFF0F3460;
    private static final int HIGHLIGHT_COLOR = 0xFFE94560;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int TEXT_DIM = 0xFF888888;
    private static final int GREEN = 0xFF4CAF50;
    private static final int RED = 0xFFF44336;
    private static final int YELLOW = 0xFFFFEB3B;

    private EditBox searchBox;
    private final List<RecipeRowData> displayedRows = new ArrayList<>();
    private int scrollOffset = 0;
    private int currentPage = 0;
    private static final int ROWS_PER_PAGE = 12;

    private String selectedRecipeId = null;
    private boolean inDetailView = false;
    private boolean inCreateView = false;
    private boolean inEditView = false;

    private Button createNewBtn;
    private Button saveBtn;
    private Button reloadBtn;
    private Button backBtn;

    private CREDetailPanel detailPanel;
    private CRECreatePanel createPanel;

    // All recipes shown in the list (from server data + local state)
    private List<RecipeRowData> allRecipes = new ArrayList<>();

    public CREScreen() {
        super(Component.literal("Cole's Recipe Editor"));
    }

    public <T extends AbstractWidget> T addWidget(T widget) {
        return addRenderableWidget(widget);
    }

    @Override
    protected void init() {
        super.init();
        loadRecipeList();
        buildBrowseUI();
    }

    private void loadRecipeList() {
        allRecipes.clear();
        // Custom CRE recipes
        RecipeStateManager state = RecipeStateManager.getInstance();
        for (CRERecipeEntry entry : state.getCustomRecipes()) {
            allRecipes.add(new RecipeRowData(
                entry.getId(),
                entry.getType(),
                "CRE",
                entry.isEnabled()
            ));
        }
        // We'll also request the full recipe list from server via network
        // For now, show what we have locally
        filterRecipes("");
    }

    private void filterRecipes(String query) {
        displayedRows.clear();
        String q = query.toLowerCase(Locale.ROOT);
        for (RecipeRowData row : allRecipes) {
            if (q.isEmpty() || row.id.toLowerCase(Locale.ROOT).contains(q)) {
                displayedRows.add(row);
            }
        }
        currentPage = 0;
        scrollOffset = 0;
    }

    private void buildBrowseUI() {
        clearWidgets();
        inDetailView = false;
        inCreateView = false;
        inEditView = false;

        int w = this.width;
        int h = this.height;
        int panelX = w / 2 - 300;
        int panelW = 600;

        // Search bar
        searchBox = new EditBox(this.font, panelX + 10, 38, panelW - 110, 16, Component.literal("Search recipe ID..."));
        searchBox.setHint(Component.literal("Search recipe ID..."));
        searchBox.setResponder(this::filterRecipes);
        this.addRenderableWidget(searchBox);

        // Create New button
        createNewBtn = Button.builder(Component.literal("+ New Recipe"), b -> openCreateView())
            .pos(panelX + panelW - 95, 36)
            .size(90, 20)
            .build();
        this.addRenderableWidget(createNewBtn);

        // Page nav
        Button prevPage = Button.builder(Component.literal("< Prev"), b -> {
            if (currentPage > 0) { currentPage--; }
        }).pos(panelX + 10, h - 55).size(60, 16).build();
        this.addRenderableWidget(prevPage);

        Button nextPage = Button.builder(Component.literal("Next >"), b -> {
            int maxPage = Math.max(0, (displayedRows.size() - 1) / ROWS_PER_PAGE);
            if (currentPage < maxPage) { currentPage++; }
        }).pos(panelX + 80, h - 55).size(60, 16).build();
        this.addRenderableWidget(nextPage);

        // Save button
        saveBtn = Button.builder(Component.literal("Save Changes"), b -> {
            CRENetworkHandler.sendSaveRequest();
            minecraft.player.displayClientMessage(Component.literal("[CRE] Changes saved! Run /CRE reload to apply."), false);
        }).pos(panelX + panelW - 200, h - 55).size(95, 18).build();
        this.addRenderableWidget(saveBtn);

        // Reload button
        reloadBtn = Button.builder(Component.literal("/CRE reload"), b -> {
            CRENetworkHandler.sendReloadRequest();
            minecraft.player.displayClientMessage(Component.literal("[CRE] Reload sent to server..."), false);
        }).pos(panelX + panelW - 100, h - 55).size(90, 18).build();
        this.addRenderableWidget(reloadBtn);
    }

    private void openDetailView(String recipeId) {
        selectedRecipeId = recipeId;
        inDetailView = true;
        clearWidgets();

        backBtn = Button.builder(Component.literal("< Back"), b -> buildBrowseUI())
            .pos(this.width / 2 - 300, 36).size(60, 18).build();
        this.addRenderableWidget(backBtn);

        detailPanel = new CREDetailPanel(this, recipeId, this.width / 2 - 290, 62, 580, this.height - 90);
        detailPanel.init();
    }

    private void openCreateView() {
        openCreateViewWithClone(null);
    }

    private void openCreateViewWithClone(CRERecipeEntry cloneFrom) {
        inCreateView = true;
        inDetailView = false;
        clearWidgets();

        backBtn = Button.builder(Component.literal("< Back"), b -> buildBrowseUI())
            .pos(this.width / 2 - 300, 36).size(60, 18).build();
        this.addRenderableWidget(backBtn);

        createPanel = new CRECreatePanel(this, cloneFrom, this.width / 2 - 290, 62, 580, this.height - 90);
        createPanel.init();
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        // Background
        gfx.fill(0, 0, this.width, this.height, BG_COLOR);

        // Title bar
        gfx.fill(0, 0, this.width, 30, ACCENT_COLOR);
        gfx.drawCenteredString(this.font, "§6Cole's Recipe Editor §7(CRE)", this.width / 2, 10, TEXT_COLOR);

        int panelX = this.width / 2 - 300;
        int panelW = 600;

        if (inDetailView && detailPanel != null) {
            detailPanel.render(gfx, mouseX, mouseY, partialTick);
        } else if (inCreateView && createPanel != null) {
            createPanel.render(gfx, mouseX, mouseY, partialTick);
        } else {
            renderBrowseView(gfx, mouseX, mouseY, panelX, panelW);
        }

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private void renderBrowseView(GuiGraphics gfx, int mouseX, int mouseY, int panelX, int panelW) {
        int h = this.height;

        // Panel background
        gfx.fill(panelX, 32, panelX + panelW, h - 30, PANEL_COLOR);

        // Header row
        gfx.fill(panelX, 58, panelX + panelW, 72, ACCENT_COLOR);
        gfx.drawString(this.font, "Recipe ID", panelX + 10, 62, YELLOW);
        gfx.drawString(this.font, "Type", panelX + 310, 62, YELLOW);
        gfx.drawString(this.font, "Origin", panelX + 430, 62, YELLOW);
        gfx.drawString(this.font, "Status", panelX + 510, 62, YELLOW);

        // Recipe rows
        int startIdx = currentPage * ROWS_PER_PAGE;
        int endIdx = Math.min(startIdx + ROWS_PER_PAGE, displayedRows.size());
        int rowY = 74;

        for (int i = startIdx; i < endIdx; i++) {
            RecipeRowData row = displayedRows.get(i);
            boolean hovered = mouseX >= panelX && mouseX <= panelX + panelW - 100
                && mouseY >= rowY && mouseY <= rowY + 13;
            int rowBg = hovered ? 0xFF0F3460 : (i % 2 == 0 ? 0xFF111126 : 0xFF0D0D20);
            gfx.fill(panelX, rowY, panelX + panelW, rowY + 14, rowBg);

            // ID (truncated with scroll hint)
            String displayId = row.id.length() > 38 ? row.id.substring(0, 36) + ".." : row.id;
            gfx.drawString(this.font, displayId, panelX + 10, rowY + 3, TEXT_COLOR);

            // Type
            String displayType = row.type != null && row.type.length() > 16
                ? row.type.substring(0, 14) + ".." : (row.type != null ? row.type : "?");
            gfx.drawString(this.font, displayType, panelX + 310, rowY + 3, TEXT_DIM);

            // Origin
            String origin = row.origin != null ? row.origin : "?";
            gfx.drawString(this.font, origin, panelX + 430, rowY + 3, 0xFFAAAAAA);

            // Status
            int statusColor = row.enabled ? GREEN : RED;
            gfx.drawString(this.font, row.enabled ? "ON" : "OFF", panelX + 515, rowY + 3, statusColor);

            rowY += 14;
        }

        // Page info
        int maxPage = displayedRows.isEmpty() ? 0 : (displayedRows.size() - 1) / ROWS_PER_PAGE;
        String pageInfo = "Page " + (currentPage + 1) + "/" + (maxPage + 1) + "  |  " + displayedRows.size() + " recipes";
        gfx.drawCenteredString(this.font, pageInfo, this.width / 2, h - 48, TEXT_DIM);

        // Hover tooltip for full recipe ID
        int rowY2 = 74;
        for (int i = startIdx; i < endIdx; i++) {
            RecipeRowData row = displayedRows.get(i);
            if (mouseX >= panelX && mouseX <= panelX + panelW - 100
                && mouseY >= rowY2 && mouseY <= rowY2 + 13
                && row.id.length() > 38) {
                gfx.renderTooltip(this.font, Component.literal(row.id), mouseX, mouseY);
            }
            rowY2 += 14;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (inDetailView && detailPanel != null) {
            if (detailPanel.mouseClicked(mouseX, mouseY, button)) return true;
        } else if (inCreateView && createPanel != null) {
            if (createPanel.mouseClicked(mouseX, mouseY, button)) return true;
        } else {
            handleBrowseClick(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleBrowseClick(double mouseX, double mouseY, int button) {
        int panelX = this.width / 2 - 300;
        int panelW = 600;
        int startIdx = currentPage * ROWS_PER_PAGE;
        int endIdx = Math.min(startIdx + ROWS_PER_PAGE, displayedRows.size());
        int rowY = 74;

        for (int i = startIdx; i < endIdx; i++) {
            RecipeRowData row = displayedRows.get(i);
            // Click on row (not toggle) -> detail view
            if (mouseX >= panelX && mouseX <= panelX + panelW - 60
                && mouseY >= rowY && mouseY <= rowY + 13) {
                openDetailView(row.id);
                return;
            }
            // Toggle button area (right side)
            if (mouseX >= panelX + panelW - 58 && mouseX <= panelX + panelW - 5
                && mouseY >= rowY && mouseY <= rowY + 13) {
                toggleRecipeEnabled(row);
                return;
            }
            rowY += 14;
        }
    }

    private void toggleRecipeEnabled(RecipeRowData row) {
        RecipeStateManager state = RecipeStateManager.getInstance();
        if (row.enabled) {
            state.disableRecipe(row.id);
            row.enabled = false;
        } else {
            state.enableRecipe(row.id);
            row.enabled = true;
        }
        CRENetworkHandler.sendToggleRecipe(row.id, row.enabled);
    }

    public void onRecipeCreated(CRERecipeEntry entry) {
        allRecipes.add(new RecipeRowData(entry.getId(), entry.getType(), "CRE", entry.isEnabled()));
        filterRecipes(searchBox != null ? searchBox.getValue() : "");
        buildBrowseUI();
    }

    public void openCloneView(CRERecipeEntry entry) {
        openCreateViewWithClone(entry);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (inDetailView && detailPanel != null) {
            if (detailPanel.keyPressed(keyCode, scanCode, modifiers)) return true;
        } else if (inCreateView && createPanel != null) {
            if (createPanel.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Data class for a row in the recipe list
    public static class RecipeRowData {
        public String id;
        public String type;
        public String origin;
        public boolean enabled;

        public RecipeRowData(String id, String type, String origin, boolean enabled) {
            this.id = id;
            this.type = type;
            this.origin = origin;
            this.enabled = enabled;
        }
    }
}
