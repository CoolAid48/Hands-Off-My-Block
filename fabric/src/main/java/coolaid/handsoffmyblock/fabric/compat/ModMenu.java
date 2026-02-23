package coolaid.handsoffmyblock.fabric.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import coolaid.handsoffmyblock.fabric.client.ConfigScreenFabric;
import net.minecraft.client.Minecraft;

public class ModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Minecraft client = Minecraft.getInstance();
            if (client.getCurrentServer() != null && !client.hasSingleplayerServer()) {
                return parent;
            }
            return new ConfigScreenFabric(parent);
        };
    }
}