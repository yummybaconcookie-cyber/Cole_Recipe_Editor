package com.coles.recipeeditor.recipe;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;

public class CRERecipeEntry {
    private String id;
    private String type;
    private boolean enabled;
    private String rawScript;
    private List<RecipeSlot> inputs = new ArrayList<>();
    private List<RecipeSlot> outputs = new ArrayList<>();
    private String heatLevel = "none";
    private boolean shapeless = false;
    private String modOrigin = "cre";
    private JsonObject rawJson;

    public CRERecipeEntry() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getRawScript() { return rawScript; }
    public void setRawScript(String rawScript) { this.rawScript = rawScript; }

    public List<RecipeSlot> getInputs() { return inputs; }
    public void setInputs(List<RecipeSlot> inputs) { this.inputs = inputs; }

    public List<RecipeSlot> getOutputs() { return outputs; }
    public void setOutputs(List<RecipeSlot> outputs) { this.outputs = outputs; }

    public String getHeatLevel() { return heatLevel; }
    public void setHeatLevel(String heatLevel) { this.heatLevel = heatLevel; }

    public boolean isShapeless() { return shapeless; }
    public void setShapeless(boolean shapeless) { this.shapeless = shapeless; }

    public String getModOrigin() { return modOrigin; }
    public void setModOrigin(String modOrigin) { this.modOrigin = modOrigin; }

    public JsonObject getRawJson() { return rawJson; }
    public void setRawJson(JsonObject rawJson) { this.rawJson = rawJson; }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("type", type);
        obj.addProperty("enabled", enabled);
        obj.addProperty("rawScript", rawScript != null ? rawScript : "");
        obj.addProperty("heatLevel", heatLevel);
        obj.addProperty("shapeless", shapeless);
        obj.addProperty("modOrigin", modOrigin);

        JsonArray inputArr = new JsonArray();
        for (RecipeSlot slot : inputs) inputArr.add(slot.toJson());
        obj.add("inputs", inputArr);

        JsonArray outputArr = new JsonArray();
        for (RecipeSlot slot : outputs) outputArr.add(slot.toJson());
        obj.add("outputs", outputArr);

        return obj;
    }

    public static CRERecipeEntry fromJson(JsonObject obj) {
        try {
            CRERecipeEntry entry = new CRERecipeEntry();
            entry.id = obj.get("id").getAsString();
            entry.type = obj.get("type").getAsString();
            entry.enabled = obj.has("enabled") && obj.get("enabled").getAsBoolean();
            entry.rawScript = obj.has("rawScript") ? obj.get("rawScript").getAsString() : "";
            entry.heatLevel = obj.has("heatLevel") ? obj.get("heatLevel").getAsString() : "none";
            entry.shapeless = obj.has("shapeless") && obj.get("shapeless").getAsBoolean();
            entry.modOrigin = obj.has("modOrigin") ? obj.get("modOrigin").getAsString() : "cre";
            entry.rawJson = obj;

            if (obj.has("inputs")) {
                for (JsonElement el : obj.getAsJsonArray("inputs")) {
                    RecipeSlot slot = RecipeSlot.fromJson(el.getAsJsonObject());
                    if (slot != null) entry.inputs.add(slot);
                }
            }
            if (obj.has("outputs")) {
                for (JsonElement el : obj.getAsJsonArray("outputs")) {
                    RecipeSlot slot = RecipeSlot.fromJson(el.getAsJsonObject());
                    if (slot != null) entry.outputs.add(slot);
                }
            }
            return entry;
        } catch (Exception e) {
            return null;
        }
    }
}
