package me.gravityio.easyrename.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.gravityio.easyrename.ModConfig;
import me.gravityio.yaclutils.ConfigScreenBuilder;

public class ModMenuApiImpl implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return p -> ConfigScreenBuilder.getScreen(ModConfig.GSON, p);
    }
}
