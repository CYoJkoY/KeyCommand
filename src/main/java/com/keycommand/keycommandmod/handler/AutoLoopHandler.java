package com.keycommand.keycommandmod.handler;

import com.keycommand.keycommandmod.KeyCommandMod;
import com.keycommand.keycommandmod.gui.GuiInventory;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

/**
 * 服务器连接后自动触发循环的处理器
 * 负责在玩家连接服务器并加载完成后，执行自动循环逻辑
 */
public class AutoLoopHandler {
    // 单例实例（确保全局唯一）
    private static final AutoLoopHandler instance = new AutoLoopHandler();

    private AutoLoopHandler() {}

    public static AutoLoopHandler getInstance() {
        return instance;
    }

    /**
     * 监听客户端连接服务器事件
     */
    @SubscribeEvent
    public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        KeyCommandMod.LOGGER.info("KeyCommandMod: 已连接服务器，准备启动自动循环...");

        // 启动新线程等待玩家加载（避免主线程阻塞）
        new Thread(() -> {
            int waitedSeconds = 0;
            // 最多等待15秒，防止无限循环
            while (Minecraft.getMinecraft().player == null && waitedSeconds < 15) {
                KeyCommandMod.LOGGER.info("AutoLoopHandler: 玩家尚未加载，已等待{}秒", waitedSeconds);
                try {
                    Thread.sleep(1000); // 每秒检测一次
                } catch (InterruptedException e) {
                    KeyCommandMod.LOGGER.error("等待玩家加载时线程被中断", e);
                    Thread.currentThread().interrupt();
                    return;
                }
                waitedSeconds++;
            }

            // 检查玩家是否加载成功
            if (Minecraft.getMinecraft().player == null) {
                KeyCommandMod.LOGGER.error("等待超时，玩家对象仍为null，自动循环取消");
                return;
            }

            // 在Minecraft主线程中执行自动循环（Forge调度任务）
            Minecraft.getMinecraft().addScheduledTask(() -> {
                KeyCommandMod.LOGGER.info("AutoLoopHandler: 玩家已加载，执行自动循环检测");
                GuiInventory.tryAutoStartLoop(); // 调用GuiInventory的自动循环方法
            });
        }, "KeyCommand-AutoLoop-Waiter").start();
    }
}