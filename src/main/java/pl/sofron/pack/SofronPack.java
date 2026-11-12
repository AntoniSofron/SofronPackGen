package pl.sofron.pack;

import net.fabricmc.api.ModInitializer;

/** The generator is defined entirely in 26.3 worldgen registries. */
public final class SofronPack implements ModInitializer {
    public static final String MOD_ID = "sofron_pack";
    @Override public void onInitialize() {
        System.out.println("[Sofron Pack Gen 2] Experimental clean-room worldgen loaded.");
    }
}
