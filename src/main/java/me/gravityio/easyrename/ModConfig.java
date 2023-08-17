package me.gravityio.easyrename;

import dev.isxander.yacl3.config.ConfigEntry;
import dev.isxander.yacl3.config.ConfigInstance;
import dev.isxander.yacl3.config.GsonConfigInstance;
import me.gravityio.yaclutils.ConfigScreenFrame;
import me.gravityio.yaclutils.annotations.Config;
import me.gravityio.yaclutils.annotations.elements.ScreenOption;

import java.nio.file.Path;

@Config(namespace = RenameMod.MOD_ID)
public class ModConfig implements ConfigScreenFrame {
    public static ConfigInstance<ModConfig> GSON = GsonConfigInstance.createBuilder(ModConfig.class)
            .setPath(Path.of("config", RenameMod.MOD_ID))
            .build();

    public static ModConfig INSTANCE;

    @ConfigEntry
    @ScreenOption(index = 0)
    public boolean syncItemFrame = false;
}
