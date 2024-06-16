package me.gravityio.easyrename;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(RenameMod.MOD_ID + ".json");
    public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(Identifier.of(RenameMod.MOD_ID, "config"))
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
     * <p>
     * Useful for use with the <a href="https://modrinth.com/mod/inventory-tabs-updated">Inventory Tabs</a> mod
     */
    @SerialEntry
    public boolean syncItemFrame = false;
    @SerialEntry
    public boolean useXP = false;
    @SerialEntry
    public boolean useLevels = false;
    @SerialEntry
    public int cost = 50;

    public boolean getUseLevels() {
        return useLevels;
    }

    public void setUseLevels(boolean useLevels) {
        this.useLevels = useLevels;
    }

    public boolean getSyncItemFrame() {
        return syncItemFrame;
    }

    public void setSyncItemFrame(boolean syncItemFrame) {
        this.syncItemFrame = syncItemFrame;
    }

    public boolean getUseXP() {
        return useXP;
    }

    public void setUseXP(boolean useXP) {
        this.useXP = useXP;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int xpCost) {
        this.cost = xpCost;
    }

    public Screen getScreen(Screen p) {
        return YetAnotherConfigLib.create(HANDLER, (defaults, config, builder) -> {
            ConfigCategory.Builder main = ConfigCategory.createBuilder();

            var itemFrameOpt = opt(
                    RenameMod.MOD_ID, "syncItemFrame",
                    defaults.syncItemFrame, config::getSyncItemFrame, config::setSyncItemFrame,
                    opt -> BooleanControllerBuilder.create(opt).coloured(true).yesNoFormatter()
            ).build();

            var useLevelOpt = opt(
                    RenameMod.MOD_ID, "useLevels",
                    defaults.useLevels, config::getUseLevels, config::setUseLevels,
                    opt -> BooleanControllerBuilder.create(opt).coloured(true).yesNoFormatter()
            ).build();

            var xpCostOpt = opt(
                    RenameMod.MOD_ID, "xpCost",
                    defaults.cost, config::getCost, config::setCost,
                    opt -> IntegerFieldControllerBuilder.create(opt).min(0).max(500).formatValue(s -> useLevelOpt.pendingValue() ? Text.literal(s + "lvl") : Text.literal(s + "xp"))
            ).build();

            var useXPOpt = opt(
                    RenameMod.MOD_ID, "useXP",
                    defaults.useXP, config::getUseXP, config::setUseXP,
                    opt -> BooleanControllerBuilder.create(opt).coloured(true).yesNoFormatter()
            ).listener((opt, v) -> {
                xpCostOpt.setAvailable(v);
                useLevelOpt.setAvailable(v);
            }).build();

            main.name(Text.translatable("yacl.renamemod.title"))
                    .option(itemFrameOpt)
                    .option(useXPOpt)
                    .option(useLevelOpt)
                    .option(xpCostOpt);
            builder.title(Text.translatable("yacl.renamemod.title"))
                    .category(main.build());
            return builder;
        }).generateScreen(p);
    }

    private static <T> Option.Builder<T> opt(String modid, String name, T def, Supplier<T> getter, Consumer<T> setter, Function<Option<T>, ControllerBuilder<T>> controllerBuilder) {
        var label = "yacl.%s.%s.label".formatted(modid, name);
        var description = "yacl.%s.%s.description".formatted(modid, name);

        return Option.<T>createBuilder()
                .name(Text.translatable(label))
                .description(OptionDescription.of(Text.translatable(description)))
                .binding(def, getter, setter)
                .controller(controllerBuilder);
    }
}
