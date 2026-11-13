package pl.sofron.terralab;

import net.fabricmc.api.ModInitializer;

/**
 * Fresh clean-room entry point. Terrain generation deliberately remains vanilla
 * until the owner approves the full design specification.
 */
public final class TerraLabMod implements ModInitializer {
    public static final String MOD_ID = "sofron_terra_lab";

    @Override
    public void onInitialize() {
        System.out.println("[Sofron Terra Lab] Design baseline started (vanilla worldgen).");
    }
}
