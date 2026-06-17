package com.coles.recipeeditor.recipe;

import com.google.gson.JsonObject;

public class RecipeSlot {
    public enum SlotType { ITEM, FLUID }
    public enum SpecialType { NORMAL, CATALYST, TOOL }

    private String id;
    private SlotType slotType = SlotType.ITEM;
    private SpecialType specialType = SpecialType.NORMAL;
    private int count = 1;
    private long fluidAmount = 1000;
    private float chance = 1.0f;
    private int gridX = -1;
    private int gridY = -1;
    private String tag = null;

    public RecipeSlot() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public SlotType getSlotType() { return slotType; }
    public void setSlotType(SlotType slotType) { this.slotType = slotType; }

    public SpecialType getSpecialType() { return specialType; }
    public void setSpecialType(SpecialType specialType) { this.specialType = specialType; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }

    public long getFluidAmount() { return fluidAmount; }
    public void setFluidAmount(long fluidAmount) { this.fluidAmount = fluidAmount; }

    public float getChance() { return chance; }
    public void setChance(float chance) { this.chance = chance; }

    public int getGridX() { return gridX; }
    public void setGridX(int gridX) { this.gridX = gridX; }

    public int getGridY() { return gridY; }
    public void setGridY(int gridY) { this.gridY = gridY; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public boolean isFluid() { return slotType == SlotType.FLUID; }

    public String getDisplayName() {
        if (tag != null) return "#" + tag;
        return id != null ? id : "empty";
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("slotType", slotType.name());
        obj.addProperty("specialType", specialType.name());
        obj.addProperty("count", count);
        obj.addProperty("fluidAmount", fluidAmount);
        obj.addProperty("chance", chance);
        obj.addProperty("gridX", gridX);
        obj.addProperty("gridY", gridY);
        if (tag != null) obj.addProperty("tag", tag);
        return obj;
    }

    public static RecipeSlot fromJson(JsonObject obj) {
        try {
            RecipeSlot slot = new RecipeSlot();
            slot.id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsString() : null;
            slot.slotType = obj.has("slotType") ? SlotType.valueOf(obj.get("slotType").getAsString()) : SlotType.ITEM;
            slot.specialType = obj.has("specialType") ? SpecialType.valueOf(obj.get("specialType").getAsString()) : SpecialType.NORMAL;
            slot.count = obj.has("count") ? obj.get("count").getAsInt() : 1;
            slot.fluidAmount = obj.has("fluidAmount") ? obj.get("fluidAmount").getAsLong() : 1000L;
            slot.chance = obj.has("chance") ? obj.get("chance").getAsFloat() : 1.0f;
            slot.gridX = obj.has("gridX") ? obj.get("gridX").getAsInt() : -1;
            slot.gridY = obj.has("gridY") ? obj.get("gridY").getAsInt() : -1;
            slot.tag = obj.has("tag") && !obj.get("tag").isJsonNull() ? obj.get("tag").getAsString() : null;
            return slot;
        } catch (Exception e) {
            return null;
        }
    }
}
