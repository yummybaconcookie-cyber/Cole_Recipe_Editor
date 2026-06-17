package com.coles.recipeeditor.recipe;

public enum RecipeType {
    // Vanilla
    CRAFTING("Crafting Table / Mechanical Crafter", "minecraft", true, false),
    SMELTING("Furnace Smelting", "minecraft", false, false),
    BLASTING("Blast Furnace", "minecraft", false, false),
    SMOKING("Smoker", "minecraft", false, false),
    CAMPFIRE("Campfire Cooking", "minecraft", false, false),
    STONECUTTING("Stonecutter", "minecraft", false, false),
    SMITHING("Smithing Table", "minecraft", false, false),

    // Create
    CREATE_MIXING("Create: Mixing", "create", false, true),
    CREATE_CRUSHING("Create: Crushing Wheels", "create", false, false),
    CREATE_PRESSING("Create: Pressing (Depot)", "create", false, false),
    CREATE_PRESSING_BASIN("Create: Pressing (Basin)", "create", false, true),
    CREATE_FAN_BLASTING("Create: Fan Blasting", "create", false, false),
    CREATE_FAN_WASHING("Create: Fan Washing", "create", false, false),
    CREATE_FAN_SMOKING("Create: Fan Smoking", "create", false, false),
    CREATE_FAN_HAUNTING("Create: Fan Haunting", "create", false, false),
    CREATE_DEPLOYING("Create: Deployer", "create", false, false),
    CREATE_COMPACTING("Create: Compacting (Basin)", "create", false, true),
    CREATE_CUTTING("Create: Mechanical Saw Cutting", "create", false, false),
    CREATE_MILLING("Create: Millstone", "create", false, false),
    CREATE_EMPTYING("Create: Emptying (Spout)", "create", false, true),
    CREATE_FILLING("Create: Filling (Spout)", "create", false, true),
    CREATE_SANDPAPER("Create: Sandpaper Polishing", "create", false, false);

    private final String displayName;
    private final String modId;
    private final boolean isCraftingGrid;
    private final boolean supportsFluid;

    RecipeType(String displayName, String modId, boolean isCraftingGrid, boolean supportsFluid) {
        this.displayName = displayName;
        this.modId = modId;
        this.isCraftingGrid = isCraftingGrid;
        this.supportsFluid = supportsFluid;
    }

    public String getDisplayName() { return displayName; }
    public String getModId() { return modId; }
    public boolean isCraftingGrid() { return isCraftingGrid; }
    public boolean supportsFluid() { return supportsFluid; }
    public boolean supportsHeat() {
        return this == CREATE_MIXING || this == CREATE_COMPACTING || this == CREATE_PRESSING_BASIN;
    }
    public boolean supportsMultipleOutputs() {
        return this != CRAFTING && this != SMELTING && this != BLASTING
            && this != SMOKING && this != CAMPFIRE && this != STONECUTTING
            && this != SMITHING;
    }

    public String getKubeJSNamespace() {
        return switch (this) {
            case CRAFTING -> "shaped";
            case SMELTING -> "smelting";
            case BLASTING -> "blasting";
            case SMOKING -> "smoking";
            case CAMPFIRE -> "campfireCooking";
            case STONECUTTING -> "stonecutting";
            case SMITHING -> "smithing";
            case CREATE_MIXING -> "create.mixing";
            case CREATE_CRUSHING -> "create.crushing";
            case CREATE_PRESSING -> "create.pressing";
            case CREATE_PRESSING_BASIN -> "create.pressing";
            case CREATE_FAN_BLASTING -> "create.fan_blasting";
            case CREATE_FAN_WASHING -> "create.fan_washing";
            case CREATE_FAN_SMOKING -> "create.fan_smoking";
            case CREATE_FAN_HAUNTING -> "create.fan_haunting";
            case CREATE_DEPLOYING -> "create.deploying";
            case CREATE_COMPACTING -> "create.compacting";
            case CREATE_CUTTING -> "create.cutting";
            case CREATE_MILLING -> "create.milling";
            case CREATE_EMPTYING -> "create.emptying";
            case CREATE_FILLING -> "create.filling";
            case CREATE_SANDPAPER -> "create.sandpaper_polishing";
        };
    }
}
