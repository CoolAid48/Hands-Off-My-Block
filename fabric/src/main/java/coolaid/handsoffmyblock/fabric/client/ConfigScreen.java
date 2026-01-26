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

public class ConfigScreen extends Screen {
    private final Screen parent;
    private EditBox markerInput;
    private Component statusText;
    private int statusColor;
    private StringWidget titleWidget;

    public ConfigScreen(Screen parent) {
        super(Component.literal("Hands Off My Block Config"));
        this.parent = parent;
        this.statusText = Component.empty();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 2 - 70;

        // Marker item label and EditBox for item ID input
        this.addRenderableWidget(new net.minecraft.client.gui.components.StringWidget(
                centerX - 100, y, 200, 20,
                Component.literal("Input valid item ID"),
                this.font
        ));
        markerInput = new EditBox(this.font, centerX - 100, y + 20, 200, 20,
                Component.literal(""));
        if (HandsOffMyConfigManager.get().markerItem != null) {
            markerInput.setValue(HandsOffMyConfigManager.get().markerItem.toString());
        }
        this.addRenderableWidget(markerInput);

        y += 60;

        int buttonWidth = 130;
        int buttonHeight = 20;
        int buttonSpacing = 10;
        int startX = centerX - buttonWidth - (buttonSpacing / 2);

        Component enabled = Component.literal("Enabled").withStyle(ChatFormatting.GREEN);
        Component disabled = Component.literal("Disabled").withStyle(ChatFormatting.RED);

        // Top row: Workstation Marking (left) and Require Sneaking (right)
        Button workstationToggle = Button.builder(
                Component.literal("Workstation Marking: " +
                        (HandsOffMyConfigManager.get().enableWorkstationMarking ? "§aYes" : "§cNo")),
                btn -> {
                    HandsOffMyConfigManager.get().enableWorkstationMarking =
                            !HandsOffMyConfigManager.get().enableWorkstationMarking;
                    btn.setMessage(Component.literal("Workstation Marking: " +
                            (HandsOffMyConfigManager.get().enableWorkstationMarking ? "§aYes" : "§cNo")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.literal("Workstation Marking: ")
                                        .append(HandsOffMyConfigManager.get().enableWorkstationMarking ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(workstationToggle);

        Button sneakToggle = Button.builder(
                Component.literal("Require Sneaking: " + (HandsOffMyConfigManager.get().requireSneaking ? "§aYes" : "§cNo")),
                btn -> {
                    HandsOffMyConfigManager.get().requireSneaking = !HandsOffMyConfigManager.get().requireSneaking;
                    btn.setMessage(Component.literal("Require Sneaking: " + (HandsOffMyConfigManager.get().requireSneaking ? "§aYes" : "§cNo")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.literal("Require Sneaking: ")
                                        .append(HandsOffMyConfigManager.get().requireSneaking ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX + buttonWidth + buttonSpacing, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(sneakToggle);

        // Second row: Bed Marking (left) and Pathfinding Tweaks (right)
        y += buttonHeight + 10;
        Button bedToggle = Button.builder(
                Component.literal("Bed Marking: " + (HandsOffMyConfigManager.get().enableBedMarking ? "§aYes" : "§cNo")),
                btn -> {
                    HandsOffMyConfigManager.get().enableBedMarking = !HandsOffMyConfigManager.get().enableBedMarking;
                    btn.setMessage(Component.literal("Bed Marking: " + (HandsOffMyConfigManager.get().enableBedMarking ? "§aYes" : "§cNo")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.literal("Bed Marking: ")
                                        .append(HandsOffMyConfigManager.get().enableBedMarking ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(bedToggle);

        Button tweakToggle = Button.builder(
                Component.literal("Pathfinding Tweaks: " + (HandsOffMyConfigManager.get().pathfindingTweaks ? "§aYes" : "§cNo")),
                btn -> {
                    HandsOffMyConfigManager.get().pathfindingTweaks = !HandsOffMyConfigManager.get().pathfindingTweaks;
                    btn.setMessage(Component.literal("Pathfinding Tweaks: " + (HandsOffMyConfigManager.get().pathfindingTweaks ? "§aYes" : "§cNo")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.literal("Pathfinding Tweaks: ")
                                        .append(HandsOffMyConfigManager.get().pathfindingTweaks ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX + buttonWidth + buttonSpacing, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(tweakToggle);

        // Third row: Display Markings (left)
        y += buttonHeight + 10;
        Button actionBarToggle = Button.builder(
                Component.literal("Display Markings: " + (HandsOffMyConfigManager.get().actionBarMessages ? "§aYes" : "§cNo")),
                btn -> {
                    HandsOffMyConfigManager.get().actionBarMessages = !HandsOffMyConfigManager.get().actionBarMessages;
                    btn.setMessage(Component.literal("Display Markings: " + (HandsOffMyConfigManager.get().actionBarMessages ? "§aYes" : "§cNo")));
                    HandsOffMyConfigManager.save();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.literal("Display Markings: ")
                                        .append(HandsOffMyConfigManager.get().actionBarMessages ? enabled : disabled),
                                true
                        );
                    }
                }
        ).bounds(startX, y, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(actionBarToggle);

        // Save and Exit button (centered)
        this.addRenderableWidget(Button.builder(
                Component.literal("Save and Exit"),
                btn -> {
                    saveConfig();
                    minecraft.setScreen(parent);
                }
        ).bounds(centerX - 60, y + buttonHeight + 10, 120, 20).build());

        // Status text widget
        y += 50;
        this.addRenderableWidget(new StringWidget(
                centerX - 100, y, 200, 20,
                statusText,
                this.font
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
            statusText = Component.literal("§cFailed to save config! §fInvalid item ID");
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
                    Component.literal("§aConfig saved! §fMarker item set to: §a" + item.getName().getString()), true
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
        Component title = Component.literal("Hands Off My Block Config").withStyle(ChatFormatting.BOLD);
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