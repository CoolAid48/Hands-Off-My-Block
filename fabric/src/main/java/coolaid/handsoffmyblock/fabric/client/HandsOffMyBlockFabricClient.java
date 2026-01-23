package coolaid.handsoffmyblock.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class HandsOffMyBlockFabricClient implements ClientModInitializer {

    public static KeyMapping openConfig;
    private static final KeyMapping.Category DISABLE_CATEGORY =
            KeyMapping.Category.register(Identifier.parse("handsoffmyblock"));

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        // Config keybind registry
        openConfig = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.handsoffmyblock.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                DISABLE_CATEGORY
        ));
    }
}