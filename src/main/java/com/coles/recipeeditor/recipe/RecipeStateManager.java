package com.coles.recipeeditor.recipe;

import com.coles.recipeeditor.CREMod;
import com.google.gson.*;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RecipeStateManager {
    private static final RecipeStateManager INSTANCE = new RecipeStateManager();

    private final Set<String> disabledRecipes = new LinkedHashSet<>();
    private final List<CRERecipeEntry> customRecipes = new ArrayList<>();
    private Path savePath = null;

    private RecipeStateManager() {}

    public static RecipeStateManager getInstance() {
        return INSTANCE;
    }

    public void setSavePath(Path path) {
        this.savePath = path;
    }

    public Path getSavePath() {
        if (savePath != null) return savePath;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            return server.getServerDirectory().resolve("config/cre");
        }
        return Path.of("config/cre");
    }

    public void loadFromDisk() {
        Path dir = getSavePath();
        try {
            Files.createDirectories(dir);
            Path stateFile = dir.resolve("recipe_state.json");
            if (Files.exists(stateFile)) {
                String content = Files.readString(stateFile);
                JsonObject root = JsonParser.parseString(content).getAsJsonObject();

                disabledRecipes.clear();
                if (root.has("disabled")) {
                    for (JsonElement el : root.getAsJsonArray("disabled")) {
                        disabledRecipes.add(el.getAsString());
                    }
                }

                customRecipes.clear();
                if (root.has("custom")) {
                    for (JsonElement el : root.getAsJsonArray("custom")) {
                        JsonObject obj = el.getAsJsonObject();
                        CRERecipeEntry entry = CRERecipeEntry.fromJson(obj);
                        if (entry != null) customRecipes.add(entry);
                    }
                }
                CREMod.LOGGER.info("[CRE] Loaded {} disabled recipes and {} custom recipes.", disabledRecipes.size(), customRecipes.size());
            }
        } catch (Exception e) {
            CREMod.LOGGER.error("[CRE] Failed to load recipe state: {}", e.getMessage());
        }
    }

    public void saveToDisk() {
        Path dir = getSavePath();
        try {
            Files.createDirectories(dir);
            JsonObject root = new JsonObject();

            JsonArray disabledArr = new JsonArray();
            for (String id : disabledRecipes) disabledArr.add(id);
            root.add("disabled", disabledArr);

            JsonArray customArr = new JsonArray();
            for (CRERecipeEntry entry : customRecipes) customArr.add(entry.toJson());
            root.add("custom", customArr);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Files.writeString(dir.resolve("recipe_state.json"), gson.toJson(root));
            CREMod.LOGGER.info("[CRE] Saved recipe state to disk.");
        } catch (Exception e) {
            CREMod.LOGGER.error("[CRE] Failed to save recipe state: {}", e.getMessage());
        }
    }

    public boolean isDisabled(String recipeId) {
        return disabledRecipes.contains(recipeId);
    }

    public void disableRecipe(String recipeId) {
        disabledRecipes.add(recipeId);
    }

    public void enableRecipe(String recipeId) {
        disabledRecipes.remove(recipeId);
    }

    public Set<String> getDisabledRecipes() {
        return Collections.unmodifiableSet(disabledRecipes);
    }

    public List<CRERecipeEntry> getCustomRecipes() {
        return Collections.unmodifiableList(customRecipes);
    }

    public void addCustomRecipe(CRERecipeEntry entry) {
        customRecipes.removeIf(e -> e.getId().equals(entry.getId()));
        customRecipes.add(entry);
    }

    public void removeCustomRecipe(String id) {
        customRecipes.removeIf(e -> e.getId().equals(id));
    }

    public Optional<CRERecipeEntry> getCustomRecipe(String id) {
        return customRecipes.stream().filter(e -> e.getId().equals(id)).findFirst();
    }
}
