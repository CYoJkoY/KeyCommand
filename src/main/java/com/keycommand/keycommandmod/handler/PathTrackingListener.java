package com.keycommand.keycommandmod.handler;

import com.keycommand.keycommandmod.KeyCommandMod;
import com.keycommand.keycommandmod.gui.GuiInventory;
import com.keycommand.keycommandmod.gui.path.PathSequence;
import com.keycommand.keycommandmod.gui.path.PathStep;
import com.keycommand.keycommandmod.util.ActionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PathTrackingListener {
    private static final PathTrackingListener instance = new PathTrackingListener();
    private PathSequence currentSequence;
    private int currentStepIndex = 0;
    private int actionIndex = 0;
    private boolean tracking = false;
    private int tickDelay = 0;
    private boolean atTarget = false;
    private int remainingLoops = 0;
    private String status = "";

    private PathTrackingListener() {}

    public static PathTrackingListener getInstance() {
        return instance;
    }

    public boolean isTracking() {
        return tracking;
    }

    public void setStatus(String s) {
        status = s;
    }

    public String getStatus() {
        return status;
    }

    public void startTracking(PathSequence sequence, int remainingLoops) {
        this.currentSequence = sequence;
        this.currentStepIndex = 0;
        this.actionIndex = 0;
        this.tracking = true;
        this.atTarget = false;
        this.tickDelay = 0;
        this.remainingLoops = remainingLoops;
        KeyCommandMod.LOGGER.info("开始跟踪路径: " + sequence.getName());
    }

    public void stopTracking() {
        if (tracking) {
            this.tracking = false;
            this.currentSequence = null;
            status = "已停止";
            KeyCommandMod.LOGGER.info("路径跟踪已停止");
            AutoLoopHandler.getInstance().clearAutoLoopConfig();
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!tracking || event.phase != TickEvent.Phase.START || event.side != net.minecraftforge.fml.relauncher.Side.CLIENT) return;
        if (event.player == null || !event.player.equals(Minecraft.getMinecraft().player)) return;

        EntityPlayerSP player = (EntityPlayerSP) event.player;

        // 延迟处理
        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        if (currentStepIndex >= currentSequence.getSteps().size()) {
            ActionUtils.sendChatCommand(".goto cancel");

            if (remainingLoops != 0 || GuiInventory.loopCount < 0) {
                if (remainingLoops > 0) remainingLoops--;
                if (remainingLoops != 0 || GuiInventory.loopCount < 0) {
                    status = "等待循环...";
                    tracking = false;
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        if (currentSequence != null) {
                            GuiInventory.startNextLoop(currentSequence.getName());
                        }
                    });
                    return;
                }
            }

            GuiInventory.isLooping = false;
            status = "已完成 (" + GuiInventory.loopCounter + " 次)";
            stopTracking();
            return;
        }

        PathStep currentStep = currentSequence.getSteps().get(currentStepIndex);
        double[] target = currentStep.getGotoPoint();

        if (!atTarget) {
            // 检查是否到达目标点
            double tx = Double.isNaN(target[0]) ? player.posX : target[0];
            double ty = Double.isNaN(target[1]) ? player.posY : target[1];
            double tz = Double.isNaN(target[2]) ? player.posZ : target[2];
            double distanceSq = Math.pow(player.posX - tx, 2) + Math.pow(player.posY - ty, 2) + Math.pow(player.posZ - tz, 2);

            if (distanceSq < 4.0) {
                KeyCommandMod.LOGGER.info("到达目标 {} for {}", currentStepIndex, currentSequence.getName());
                atTarget = true;
                actionIndex = 0;
            }
        } else {
            // 执行当前步骤的动作
            if (actionIndex >= currentStep.getActions().size()) {
                currentStepIndex++;
                actionIndex = 0;
                atTarget = false;

                if (currentStepIndex < currentSequence.getSteps().size()) {
                    double[] nextTarget = currentSequence.getSteps().get(currentStepIndex).getGotoPoint();
                    ActionUtils.sendChatCommand(".goto cancel");
                    ActionUtils.sendChatCommand(String.format(".goto %.0f %.0f %.0f", nextTarget[0], nextTarget[1], nextTarget[2]));
                } else {
                    ActionUtils.sendChatCommand(".goto cancel");
                }
                return;
            }

            // 处理动作
            java.util.function.Consumer<EntityPlayerSP> action = currentStep.getActions().get(actionIndex);
            if (action instanceof ActionUtils.DelayAction) {
                ActionUtils.DelayAction delay = (ActionUtils.DelayAction) action;
                tickDelay = delay.getDelayTicks();
                KeyCommandMod.LOGGER.info("延迟 {} tick", tickDelay);
                actionIndex++;
                return;
            }

            try {
                action.accept(player);
                KeyCommandMod.LOGGER.info("执行动作 {} for step {}", actionIndex, currentStepIndex);
            } catch (Exception e) {
                KeyCommandMod.LOGGER.error("执行动作失败", e);
            }

            actionIndex++;
            tickDelay = 5; // 小延迟确保服务器处理
        }
    }
}