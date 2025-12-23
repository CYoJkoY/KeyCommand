package com.keycommand.keycommandmod.gui;

import com.keycommand.keycommandmod.KeyCommandMod;
import com.keycommand.keycommandmod.handler.LocationTriggerHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextComponentString;

import java.io.IOException;

/**
 * 位置触发参数配置GUI
 * 用于在游戏中修改自动返回的各项参数
 */
public class LocationConfigGui extends GuiScreen {
    private final GuiScreen parentScreen; // 父界面（返回用）
    private final LocationTriggerHandler handler = LocationTriggerHandler.getInstance();

    // 输入框组件
    private GuiTextField txtX;
    private GuiTextField txtY;
    private GuiTextField txtZ;
    private GuiTextField txtRadius;
    private GuiTextField txtInitialDelay;
    private GuiTextField txtRepeatInterval;
    private GuiButton btnToggleEnabled;
    private GuiButton btnUseCurrentPos; // 使用当前玩家位置按钮

    public LocationConfigGui(GuiScreen parent) {
        this.parentScreen = parent;
    }

    /**
     * 初始化GUI组件
     */
    @Override
    public void initGui() {
        // 按钮ID
        int id = 0;
        // 添加启用/禁用按钮
        btnToggleEnabled = new GuiButton(id++, this.width / 2 - 100, this.height / 2 - 100, getEnabledButtonText());
        this.buttonList.add(btnToggleEnabled);

        // 添加"使用当前位置"按钮
        btnUseCurrentPos = new GuiButton(id++, this.width / 2 - 100, this.height / 2 - 75, I18n.format("keycommand.use_current_pos"));
        this.buttonList.add(btnUseCurrentPos);

        // 输入框坐标和尺寸
        int inputX = this.width / 2 - 70;
        int inputY = this.height / 2 - 40;
        int inputWidth = 140;
        int inputHeight = 20;
        int lineHeight = 25;

        // 初始化输入框（带默认值）
        txtX = new GuiTextField(0, this.fontRenderer, inputX, inputY, inputWidth, inputHeight);
        txtX.setText(String.valueOf(handler.getTriggerX()));
        txtX.setMaxStringLength(10);

        txtY = new GuiTextField(1, this.fontRenderer, inputX, inputY + lineHeight, inputWidth, inputHeight);
        txtY.setText(String.valueOf(handler.getTriggerY()));
        txtY.setMaxStringLength(10);

        txtZ = new GuiTextField(2, this.fontRenderer, inputX, inputY + lineHeight * 2, inputWidth, inputHeight);
        txtZ.setText(String.valueOf(handler.getTriggerZ()));
        txtZ.setMaxStringLength(10);

        txtRadius = new GuiTextField(3, this.fontRenderer, inputX, inputY + lineHeight * 3, inputWidth, inputHeight);
        txtRadius.setText(String.valueOf(handler.getTriggerRadius()));
        txtRadius.setMaxStringLength(5);

        txtInitialDelay = new GuiTextField(4, this.fontRenderer, inputX, inputY + lineHeight * 4, inputWidth, inputHeight);
        txtInitialDelay.setText(String.valueOf(handler.getInitialDelayTicks()));
        txtInitialDelay.setMaxStringLength(5);

        txtRepeatInterval = new GuiTextField(5, this.fontRenderer, inputX, inputY + lineHeight * 5, inputWidth, inputHeight);
        txtRepeatInterval.setText(String.valueOf(handler.getRepeatIntervalTicks()));
        txtRepeatInterval.setMaxStringLength(5);

        // 添加保存和返回按钮
        this.buttonList.add(new GuiButton(id++, this.width / 2 - 100, this.height / 2 + 120, I18n.format("keycommand.save")));
        this.buttonList.add(new GuiButton(id++, this.width / 2 - 100, this.height / 2 + 145, I18n.format("gui.back")));
    }

    /**
     * 获取启用/禁用按钮的文本
     */
    private String getEnabledButtonText() {
        return handler.isEnabled() ? "禁用自动返回" : "启用自动返回";
    }

    /**
     * 绘制GUI界面
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 绘制背景
        this.drawDefaultBackground();
        // 绘制标题
        this.drawCenteredString(this.fontRenderer, "自动返回参数配置", this.width / 2, this.height / 2 - 130, 0xFFFFFF);

        // 绘制输入框标签
        int labelX = this.width / 2 - 120;
        int labelY = this.height / 2 - 35;
        int lineHeight = 25;
        this.drawString(this.fontRenderer, "触发X坐标:", labelX, labelY, 0xFFFFFF);
        this.drawString(this.fontRenderer, "触发Y坐标:", labelX, labelY + lineHeight, 0xFFFFFF);
        this.drawString(this.fontRenderer, "触发Z坐标:", labelX, labelY + lineHeight * 2, 0xFFFFFF);
        this.drawString(this.fontRenderer, "触发半径:", labelX, labelY + lineHeight * 3, 0xFFFFFF);
        this.drawString(this.fontRenderer, "初始延迟(ticks):", labelX, labelY + lineHeight * 4, 0xFFFFFF);
        this.drawString(this.fontRenderer, "重复间隔(ticks):", labelX, labelY + lineHeight * 5, 0xFFFFFF);

        // 绘制输入框
        txtX.drawTextBox();
        txtY.drawTextBox();
        txtZ.drawTextBox();
        txtRadius.drawTextBox();
        txtInitialDelay.drawTextBox();
        txtRepeatInterval.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * 处理按钮点击事件
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == btnToggleEnabled) {
            // 切换启用/禁用状态
            handler.setEnabled(!handler.isEnabled());
            btnToggleEnabled.displayString = getEnabledButtonText();
            mc.player.sendMessage(new TextComponentString(handler.isEnabled() ? "已启用自动返回" : "已禁用自动返回"));
        } else if (button == btnUseCurrentPos) {
            // 使用当前玩家位置
            if (mc.player != null) {
                txtX.setText(String.valueOf(mc.player.posX));
                txtY.setText(String.valueOf(mc.player.posY));
                txtZ.setText(String.valueOf(mc.player.posZ));
                mc.player.sendMessage(new TextComponentString("已使用当前位置"));
            }
        } else if (button.id == 8) { // 保存按钮（ID需根据实际初始化顺序调整）
            saveConfig();
            mc.player.sendMessage(new TextComponentString("配置已保存"));
        } else if (button.id == 9) { // 返回按钮
            this.mc.displayGuiScreen(parentScreen);
        }
    }

    /**
     * 保存配置到处理器并写入文件
     */
    private void saveConfig() {
        try {
            // 解析输入框的值并设置到处理器
            double x = Double.parseDouble(txtX.getText());
            double y = Double.parseDouble(txtY.getText());
            double z = Double.parseDouble(txtZ.getText());
            double radius = Double.parseDouble(txtRadius.getText());
            int initialDelay = Integer.parseInt(txtInitialDelay.getText());
            int repeatInterval = Integer.parseInt(txtRepeatInterval.getText());

            // 验证参数合法性
            if (radius <= 0) radius = 3.0;
            if (initialDelay < 0) initialDelay = 20;
            if (repeatInterval < 0) repeatInterval = 200;

            // 设置参数（自动保存到文件）
            handler.setTriggerX(x);
            handler.setTriggerY(y);
            handler.setTriggerZ(z);
            handler.setTriggerRadius(radius);
            handler.setInitialDelayTicks(initialDelay);
            handler.setRepeatIntervalTicks(repeatInterval);

            KeyCommandMod.LOGGER.info("位置触发配置已更新：X={}, Y={}, Z={}, 半径={}, 初始延迟={}, 重复间隔={}",
                    x, y, z, radius, initialDelay, repeatInterval);
        } catch (NumberFormatException e) {
            mc.player.sendMessage(new TextComponentString("输入格式错误！请输入数字"));
            KeyCommandMod.LOGGER.error("解析配置参数失败", e);
        }
    }

    /**
     * 处理键盘输入
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        // 将输入传递给输入框
        txtX.textboxKeyTyped(typedChar, keyCode);
        txtY.textboxKeyTyped(typedChar, keyCode);
        txtZ.textboxKeyTyped(typedChar, keyCode);
        txtRadius.textboxKeyTyped(typedChar, keyCode);
        txtInitialDelay.textboxKeyTyped(typedChar, keyCode);
        txtRepeatInterval.textboxKeyTyped(typedChar, keyCode);

        // ESC键返回上一级界面
        if (keyCode == 1) {
            this.mc.displayGuiScreen(parentScreen);
        }
    }

    /**
     * 处理鼠标点击
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        // 激活输入框
        txtX.mouseClicked(mouseX, mouseY, mouseButton);
        txtY.mouseClicked(mouseX, mouseY, mouseButton);
        txtZ.mouseClicked(mouseX, mouseY, mouseButton);
        txtRadius.mouseClicked(mouseX, mouseY, mouseButton);
        txtInitialDelay.mouseClicked(mouseX, mouseY, mouseButton);
        txtRepeatInterval.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * GUI不暂停游戏
     */
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}