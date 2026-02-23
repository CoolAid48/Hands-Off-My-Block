package coolaid.handsoffmyblock.fabric.client;

import com.mojang.blaze3d.platform.InputConstants;
import coolaid.handsoffmyblock.config.HandsOffMyConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class HandsOffMyBlockFabricClient implements ClientModInitializer {

    public static KeyMapping openConfig;
    public static KeyMapping showMarkings;
    public static KeyMapping requireSneaking;

    private static final KeyMapping.Category DISABLE_CATEGORY =
            KeyMapping.Category.register(Identifier.parse("handsoffmyblock"));

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        // Open Config, Show Markings, and Require Sneaking keybind registries

        openConfig = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.handsoffmyblock.open_config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                DISABLE_CATEGORY
        ));

        showMarkings = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.handsoffmyblock.show_markings",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                DISABLE_CATEGORY
        ));

        requireSneaking = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.handsoffmyblock.require_sneaking",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                DISABLE_CATEGORY
        ));

        // Show Markings Logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (showMarkings.consumeClick()) {
                Component enabled = Component.translatable("component.actionbar.enabled").withStyle(ChatFormatting.GREEN);
                Component disabled = Component.translatable("component.actionbar.disabled").withStyle(ChatFormatting.RED);

                HandsOffMyConfigManager.get().actionBarMessages = !HandsOffMyConfigManager.get().actionBarMessages;
                HandsOffMyConfigManager.save();

                if (client.player != null) {
                    client.player.displayClientMessage(
                            Component.translatable("message.actionbar.showMarkings")
                                    .append(HandsOffMyConfigManager.get().actionBarMessages ? enabled : disabled),
                            true
                    );
                }
            }
        });
        // Require Sneaking Logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getCurrentServer() != null && !client.hasSingleplayerServer()) {
                while (true) {
                    if (!requireSneaking.consumeClick()) break;
                    // Consume clicks while connected to a remote server.
                }
                return;
            }

            while (requireSneaking.consumeClick()) {
                Component enabled = Component.translatable("component.actionbar.enabled").withStyle(ChatFormatting.GREEN);
                Component disabled = Component.translatable("component.actionbar.disabled").withStyle(ChatFormatting.RED);

                HandsOffMyConfigManager.get().requireSneaking = !HandsOffMyConfigManager.get().requireSneaking;
                HandsOffMyConfigManager.save();

                if (client.player != null) {
                    client.player.displayClientMessage(
                            Component.translatable("message.actionbar.sneakToggle")
                                    .append(HandsOffMyConfigManager.get().requireSneaking ? enabled : disabled),
                            true
                    );
                }
            }
        });

        // Open Config Logic
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.screen != null) {
                return;
            }

            if (openConfig.consumeClick()) {
                if (client.getCurrentServer() != null && !client.hasSingleplayerServer()) {
                    return;
                }
                Minecraft.getInstance().setScreen(new ConfigScreenFabric(Minecraft.getInstance().screen));
            }
        });
    }
}