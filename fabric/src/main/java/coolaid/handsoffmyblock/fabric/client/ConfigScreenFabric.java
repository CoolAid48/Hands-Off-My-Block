package coolaid.handsoffmyblock.fabric.client;

import coolaid.handsoffmyblock.config.HandsOffMyConfigManager;
import coolaid.handsoffmyblock.fabric.HandsOffMyBlockFabric;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class ConfigScreenFabric extends Screen {
    private final Screen parent;
    private EditBox markerInput;
    private Component statusText;
    private int statusColor;
    private StringWidget titleWidget;

    public ConfigScreenFabric(Screen parent) {
        super(Component.translatable("text.configScreen.title"));
        this.parent = parent;
        this.statusText = Component.empty();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 100;

        // Marker item label and EditBox for item ID input
        this.addRenderableWidget(new net.minecraft.client.gui.components.StringWidget(
                centerX - 100, y, 200, 20,
                Component.translatable("text.configScreen.label"),
                this.font
        ));
        markerInput = new EditBox(this.font, centerX - 100, y + 15, 200, 20,
                Component.literal(""));
        if (HandsOffMyConfigManager.get().markerItem != null) {
            markerInput.setValue(HandsOffMyConfigManager.get().markerItem.toString());
        }
        this.addRenderableWidget(markerInput);

        y += 45;

        int buttonWidth = 130;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        int startX = centerX - buttonWidth - (buttonSpacing / 2);

        Component enabled = Component.translatable("component.actionbar.enabled").withStyle(ChatFormatting.GREEN);
        Component disabled = Component.translatable("component.actionbar.disabled").withStyle(ChatFormatting.RED);

        // Top row: Block Marking (left) and Require Sneaking (right)
        Button workstationToggle = Button.builder(
                Component.translatable("text.configButton.workstationToggle",
                        Component.translatable(HandsOffMyConfigManager.get().enableWorkstationMarking ? "component.configButton.yes" : "component.configButton.no")),
                btn -> {
                    HandsOffMyConfigManager.get().enableWorkstationMarking =
                            !HandsOffMyConfigManager.get().enableWorkstationMarking;
                    btn.setMessage(Component.translatable("text.configButton.workstationToggle",
                            Component.translatable(HandsOffMyConfigManager.get().enableWorkstationMarking ? "component.configButton.yes" : "component.configButton.no")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.translatable("message.actionbar.workstationToggle")
                                        .append(HandsOffMyConfigManager.get().enableWorkstationMarking ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(workstationToggle);

        Button sneakToggle = Button.builder(
                Component.translatable("text.configButton.sneakToggle", Component.translatable(HandsOffMyConfigManager.get().requireSneaking ? "component.configButton.yes" : "component.configButton.no")),
                btn -> {
                    HandsOffMyConfigManager.get().requireSneaking = !HandsOffMyConfigManager.get().requireSneaking;
                    btn.setMessage(Component.translatable("text.configButton.sneakToggle", Component.translatable(HandsOffMyConfigManager.get().requireSneaking ? "component.configButton.yes" : "component.configButton.no")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.translatable("message.actionbar.sneakToggle")
                                        .append(HandsOffMyConfigManager.get().requireSneaking ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX + buttonWidth + buttonSpacing, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(sneakToggle);

        // Second row: Bed Marking (left) and Pathfinding Tweaks (right)
        y += buttonHeight + 5;
        Button bedToggle = Button.builder(
                Component.translatable("text.configButton.bedToggle", Component.translatable(HandsOffMyConfigManager.get().enableBedMarking ? "component.configButton.yes" : "component.configButton.no")),
                btn -> {
                    HandsOffMyConfigManager.get().enableBedMarking = !HandsOffMyConfigManager.get().enableBedMarking;
                    btn.setMessage(Component.translatable("text.configButton.bedToggle", Component.translatable(HandsOffMyConfigManager.get().enableBedMarking ? "component.configButton.yes" : "component.configButton.no")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.translatable("message.actionbar.bedToggle")
                                        .append(HandsOffMyConfigManager.get().enableBedMarking ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(bedToggle);

        Button tweakToggle = Button.builder(
                Component.translatable("text.configButton.tweakToggle", Component.translatable(HandsOffMyConfigManager.get().pathfindingTweaks ? "component.configButton.yes" : "component.configButton.no")),
                btn -> {
                    HandsOffMyConfigManager.get().pathfindingTweaks = !HandsOffMyConfigManager.get().pathfindingTweaks;
                    btn.setMessage(Component.translatable("text.configButton.tweakToggle", Component.translatable(HandsOffMyConfigManager.get().pathfindingTweaks ? "component.configButton.yes" : "component.configButton.no")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.translatable("message.actionbar.tweakToggle")
                                        .append(HandsOffMyConfigManager.get().pathfindingTweaks ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX + buttonWidth + buttonSpacing, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(tweakToggle);

        // Third row: Show Markings (left)
        y += buttonHeight + 5;
        Button actionBarToggle = Button.builder(
                Component.translatable("text.configButton.showMarkings", Component.translatable(HandsOffMyConfigManager.get().actionBarMessages ? "component.configButton.yes" : "component.configButton.no")),
                btn -> {
                    HandsOffMyConfigManager.get().actionBarMessages = !HandsOffMyConfigManager.get().actionBarMessages;
                    btn.setMessage(Component.translatable("text.configButton.showMarkings", Component.translatable(HandsOffMyConfigManager.get().actionBarMessages ? "component.configButton.yes" : "component.configButton.no")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.translatable("message.actionbar.showMarkings")
                                        .append(HandsOffMyConfigManager.get().actionBarMessages ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(actionBarToggle);

        // Save and Exit button (centered)
        this.addRenderableWidget(Button.builder(
                Component.translatable("text.configButton.save_and_exit"),
                btn -> {
                    saveConfig();
                    minecraft.setScreen(parent);
                }
        ).bounds(centerX - 60, y + buttonHeight + 4, 120, 20).build());

        // Status text widget
        this.addRenderableWidget(new StringWidget(
                centerX - 100, y, 200, 20, statusText, this.font
        ) {
            @Override
            public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                if (!statusText.getString().isEmpty()) {
                    graphics.drawCenteredString(font, statusText, getX() + getWidth() / 2, getY(), statusColor);
                }
            }
        });
    }

    private void saveConfig() {
        String input = markerInput.getValue().trim();

        Identifier id = Identifier.tryParse(input);
        Item item = (id != null) ? BuiltInRegistries.ITEM.getOptional(id).orElse(null) : null;

        // Do not save and display error message when invalid
        if (id == null || item == null || item == Items.AIR) {
            statusText = Component.translatable("message.actionbar.configFailed");
            statusColor = 0xFF5555;

            if (minecraft.player != null) {
                minecraft.player.displayClientMessage(statusText, true);
            }
            return;
        }

        // No change
        if (HandsOffMyConfigManager.get().markerItem.equals(id)) {
            statusText = Component.literal("");
            return;
        }

        // Save everything when valid and changed, then send message
        HandsOffMyConfigManager.get().markerItem = id;
        HandsOffMyConfigManager.save();
        HandsOffMyBlockFabric.MARKER_ITEM = item;

        if (minecraft.player != null) {
            minecraft.player.displayClientMessage(
                    Component.translatable("message.actionbar.configSaved", item.getName()
                    ).withStyle(ChatFormatting.GREEN), true
            );
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Draws background and widgets
        graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        super.render(graphics, mouseX, mouseY, delta);

        // Screen title component
        int textWidth = this.font.width(title) + 25; // Include +25 to fit bold formatting
        Component title = Component.translatable("text.configScreen.title").withStyle(ChatFormatting.BOLD);
        titleWidget = new StringWidget(
                (this.width - textWidth) / 2, 10, textWidth, 9, title, this.font
        );
        this.addRenderableWidget(titleWidget);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}