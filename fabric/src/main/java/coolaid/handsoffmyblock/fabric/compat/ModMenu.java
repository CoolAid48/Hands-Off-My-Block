package coolaid.handsoffmyblock.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import coolaid.handsoffmyblock.fabric.client.ConfigScreenFabric;

public class ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigScreenFabric::new;
    }
}