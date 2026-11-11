package pl.sofron.pack;

import net.fabricmc.api.ModInitializer;

/** Worldgen JSON resources are loaded through Minecraft's data pack pipeline. */
public final class SofronPack implements ModInitializer {
    public static final String MOD_ID = "sofron_pack";
    @Override public void onInitialize() {
        System.out.println("[Sofron Pack] Worldgen resources registered (legacy v0.9.3 baseline).");
    }
}
