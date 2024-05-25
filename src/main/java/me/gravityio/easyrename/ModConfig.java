package me.gravityio.easyrename;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.gui.controllers.BooleanController;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.file.Path;

public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(RenameMod.MOD_ID + ".json");
    public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(new Identifier(RenameMod.MOD_ID, "config"))
            .serializer(handler -> GsonConfigSerializerBuilder
                    .create(handler)
                    .setPath(CONFIG_PATH)
                    .setJson5(true)
                    .build()
            )
            .build();

    public static ModConfig INSTANCE;

    /**
     * Whether to also rename item frames nearby a container when it is renamed. <br><br>
     *
     * Useful for use with the <a href="https://modrinth.com/mod/inventory-tabs-updated">Inventory Tabs</a> mod
     */
    @SerialEntry
    public boolean syncItemFrame = false;

    public Screen getScreen(Screen p) {
        return YetAnotherConfigLib.create(HANDLER, (defaults, config, builder) -> {
            ConfigCategory.Builder main = ConfigCategory.createBuilder();

            Option.Builder<Boolean> itemFrameOpt = Option.createBuilder();

            itemFrameOpt.name(Text.translatable("yacl.renamemod.syncItemFrame.label"))
                    .description(OptionDescription.of(Text.translatable("yacl.renamemod.syncItemFrame.description")))
                    .binding(defaults.syncItemFrame, () -> config.syncItemFrame, v -> config.syncItemFrame = v)
                    .customController(opt -> BooleanControllerBuilder.create(opt).coloured(true).yesNoFormatter().build());

            main.name(Text.translatable("yacl.renamemod.title"))
                    .option(itemFrameOpt.build());
            builder.title(Text.translatable("yacl.renamemod.title"))
                    .category(main.build());
            return builder;
        }).generateScreen(p);
    }
}
