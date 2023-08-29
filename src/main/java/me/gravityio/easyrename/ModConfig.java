package me.gravityio.easyrename;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.GsonConfigInstance;
import me.gravityio.yaclutils.annotations.Config;
import me.gravityio.yaclutils.annotations.elements.ScreenOption;
import me.gravityio.yaclutils.api.ConfigFrame;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

@Config(namespace = RenameMod.MOD_ID)
public class ModConfig implements ConfigFrame<ModConfig> {
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder()
            .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .serializeNulls();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(RenameMod.MOD_ID + ".json");
    public static GsonConfigInstance<ModConfig> GSON = GsonConfigInstance.createBuilder(ModConfig.class)
            .overrideGsonBuilder(GSON_BUILDER)
            .setPath(CONFIG_PATH)
            .build();
    public static ModConfig INSTANCE;

    /**
     * Whether to also rename item frames nearby a container when it is renamed. <br><br>
     *
     * Useful for use with the <a href="https://modrinth.com/mod/inventory-tabs-updated">Inventory Tabs</a> mod
     */
    @ConfigEntry
    @ScreenOption(index = 0)
    public boolean syncItemFrame = false;

}
