package com.exodus.survival.client.screen;

import com.exodus.core.api.player.inventory.PlayerInventoryData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class ExodusInventoryScreen extends Screen {
    private final int SLOT_WIDTH = 32;
    private final int SLOT_HEIGHT = 32;
    private final int screenWidth;
    private final int screenHeight;
    private final PlayerInventoryData playerInventoryData;

    public ExodusInventoryScreen(PlayerInventoryData inventoryData) {
        super(Component.literal("Exodus Inventory"));
        this.screenWidth = Math.max(1080, Math.min(1536, (int)(this.width * 0.8f)));
        this.screenHeight = Math.max(608, Math.min(864, (int)(this.height * 0.8f)));
        this.playerInventoryData = inventoryData;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int x = (this.width - screenWidth) / 2;
        int y = (this.height - screenHeight) / 2;

        graphics.fill(x, y, x + screenWidth, y + screenHeight, 0xFF606060);

        drawSlots(graphics, x, y);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_I){
            System.out.println("=== I PRESSED IN SCREEN ===");
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawSlots(GuiGraphics graphics, int screenX, int screenY){
        int inventorySize = PlayerInventoryData.INVENTORY_SIZE;
        int col = (int) Math.sqrt(inventorySize);
        int row = (int) Math.sqrt(inventorySize);

        int offsetX = 10;
        int offsetY = 10;
        int spacing = 4;
        int slotIndex = 0;

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int x = screenX + offsetX + (j * (SLOT_WIDTH + spacing));
                int y = screenY + offsetY + (i * (SLOT_HEIGHT + spacing));

                drawSlot(graphics, x, y, SLOT_WIDTH, SLOT_HEIGHT, 0xFF6B6B6B, slotIndex);
                slotIndex++;
            }
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y, int width, int height, int color, int slotIndex){
        graphics.fill(x, y, x + width, y + height, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, color);

        ItemStack stack = playerInventoryData.slots.get(slotIndex).getItemStack();
        drawItem(graphics, x + 4, y + 4, stack);
    }

    private void drawItem(GuiGraphics graphics, int x, int y, ItemStack itemStack) {
        if (itemStack.isEmpty()) return;
        float scale = 1.5f;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, scale);
        graphics.renderItem(itemStack, 0, 0);
        graphics.renderItemDecorations(this.font, itemStack, 0, 0);
        graphics.pose().popPose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}