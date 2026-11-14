package pl.sofron.terralab;

import net.fabricmc.api.ModInitializer;

/**
 * Clean-room 26.3 Fabric entry point. Terrain is data-driven from the approved spec;
 * generated worldgen resources and Vanilla biome/structure data are the only inputs.
 */
public final class TerraLabMod implements ModInitializer {
    public static final String MOD_ID = "sofron_terra_lab";

    @Override
    public void onInitialize() {
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.FEATURE_TYPE,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "birch_variants"), BirchVariants.CODEC);
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.FEATURE_TYPE,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "oak_variants"), OakVariants.CODEC);
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.FEATURE_TYPE,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "grounded_willow"), GroundedWillow.CODEC);
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.FEATURE_TYPE,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "spruce_variants"), SpruceVariants.CODEC);
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.FEATURE_TYPE,
                net.minecraft.resources.Identifier.fromNamespaceAndPath(MOD_ID, "giant_spruce_variants"), GiantSpruceVariants.CODEC);
        System.out.println("[Sofron Pack Gen] Approved 84-parameter worldgen enabled; Vanilla biomes and structures preserved.");
    }
}
