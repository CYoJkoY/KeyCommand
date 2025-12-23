package com.keycommand.keycommandmod.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextComponentString;

public class LoopCountInputGui extends GuiScreen {
    private final GuiInventory parent;
    private String inputText = "";
    private GuiTextField numberField;

    public LoopCountInputGui(GuiInventory parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();

        // 创建输入框
        numberField = new GuiTextField(0, fontRenderer, this.width / 2 - 100, this.height / 2 - 25, 200, 20);
        numberField.setFocused(true);
        numberField.setCanLoseFocus(false);
        numberField.setMaxStringLength(10);
        numberField.setText(inputText);

        // 添加按钮
        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 2, 90, 20, "确认"));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 10, this.height / 2, 90, 20, "取消"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 2 + 30, 200, 20, "设置为无限循环"));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if (numberField.textboxKeyTyped(typedChar, keyCode)) {
            inputText = numberField.getText();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        numberField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawCenteredString(fontRenderer, "设置循环次数", width / 2, height / 2 - 50, 0xFFFFFF);
        drawString(fontRenderer, "输入数字（0=不循环，-1=无限循环）:", width / 2 - 100, height / 2 - 40, 0xA0A0A0);
        numberField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) { // 确认
            setLoopCount();
            mc.displayGuiScreen(parent);
        } else if (button.id == 1) { // 取消
            mc.displayGuiScreen(parent);
        } else if (button.id == 2) { // 无限循环
            GuiInventory.loopCount = -1;
            mc.displayGuiScreen(parent);
        }
    }

    private void setLoopCount() {
        try {
            GuiInventory.loopCount = Integer.parseInt(inputText.trim());
            GuiInventory.loopCounter = 0;
        } catch (NumberFormatException e) {
            GuiInventory.loopCount = 1;
            mc.player.sendMessage(new TextComponentString("§c无效输入! 已重置为单次循环"));
        }
    }
}