package com.keycommand.keycommandmod;

import com.keycommand.keycommandmod.handler.AutoLoopHandler;
import com.keycommand.keycommandmod.handler.LocationTriggerHandler;
import com.keycommand.keycommandmod.gui.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = KeyCommandMod.MODID, name = KeyCommandMod.NAME, version = KeyCommandMod.VERSION)
public class KeyCommandMod {
    public static final String MODID = "keycommandmod";
    public static final String NAME = "Key Command Mod";
    public static final String VERSION = "1.0";

    public static Logger LOGGER = LogManager.getLogger(KeyCommandMod.class);
    public static KeyCommandMod instance;

    private static final KeyBinding teleKey = new KeyBinding("传送", Keyboard.KEY_GRAVE, "key.categories.keycommand");
    private static final KeyBinding srpOpenKey = new KeyBinding("灵魂空间", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_I, "key.categories.keycommand");
    private static final KeyBinding menuKey = new KeyBinding("菜单", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_O, "key.categories.keycommand");
    private static final KeyBinding ecKey = new KeyBinding("末影箱", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_U, "key.categories.keycommand");
    private static final KeyBinding hbKey = new KeyBinding("货币兑换", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_P, "key.categories.keycommand");
    private static final KeyBinding nzwKey = new KeyBinding("农作物兑换", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_L, "key.categories.keycommand");
    private static final KeyBinding guiKey = new KeyBinding("快捷菜单", KeyConflictContext.UNIVERSAL, KeyModifier.ALT, Keyboard.KEY_T, "key.categories.keycommand");
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        // 初始化路径序列
        GuiInventory.initializePathSequences();
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        instance = this;

        // 注册按键
        ClientRegistry.registerKeyBinding(teleKey);
        ClientRegistry.registerKeyBinding(srpOpenKey);
        ClientRegistry.registerKeyBinding(menuKey);
        ClientRegistry.registerKeyBinding(ecKey);
        ClientRegistry.registerKeyBinding(hbKey);
        ClientRegistry.registerKeyBinding(nzwKey);
        ClientRegistry.registerKeyBinding(guiKey);

        // 注册事件监听器
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Key Command Mod initialized!");

        // 注册自动循环处理器
        MinecraftForge.EVENT_BUS.register(AutoLoopHandler.getInstance());
        
        // 初始化并注册位置触发处理器
        LocationTriggerHandler.getInstance().init();

        // 初始化路径序列
        GuiInventory.initializePathSequences();

        // 尝试自动启动循环
        GuiInventory.tryAutoStartLoop();
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKeyState()) { // 按键按下时
            if (teleKey.isPressed()) {
                sendChatMessage("/jump");
                LOGGER.info("Sent command: /jump");
            }
            if (srpOpenKey.isKeyDown()) {
                sendChatMessage("/srp open");
                LOGGER.info("Sent command: /srp open");
            }
            if (ecKey.isKeyDown()) {
                sendChatMessage("/ec");
                LOGGER.info("Sent command: /ec");
            }
            if (menuKey.isKeyDown()) {
                sendChatMessage("/menu");
                LOGGER.info("Sent command: /menu");
            }
            if (hbKey.isKeyDown()) {
                sendChatMessage("/sre open 货币兑换");
                LOGGER.info("Sent command: /sre open 货币兑换");
            }
            if (nzwKey.isKeyDown()) {
                sendChatMessage("/sre open 农作物");
                LOGGER.info("Sent command: /sre open 农作物");
            }
            if (guiKey.isKeyDown()) {
                net.minecraft.client.Minecraft.getMinecraft().displayGuiScreen(new GuiInventory());
                LOGGER.info("Opened custom inventory GUI.");
            }
        } else {
            LOGGER.debug("Key released.");
        }
    }

    // 发送聊天消息工具方法
    private void sendChatMessage(String message) {
        if (!net.minecraft.client.Minecraft.getMinecraft().player.isSpectator()) {
            net.minecraft.client.Minecraft.getMinecraft().player.sendChatMessage(message);
        }
    }
}
