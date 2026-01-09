package com.keycommand.keycommandmod.util;

import java.util.function.Consumer;

// 设置角度（与游戏中对应）XXX.addAction(player -> ActionUtils.setPlayerViewAngles(player, 66.5f, -46.0f));
// 发送聊天内容（可用于发送指令） XXX.addAction(player -> ActionUtils.sendChatCommand("/jump"));
// 指定坐标方块右键 XXX.addAction(player -> ActionUtils.rightClickOnBlock(player, new BlockPos(190, 8, -488)));
// 手动添加延迟ticks（20tick = 1s） XXX.addAction(new ActionUtils.DelayAction(10));
// 指定坐标范围实体右键 XXX.addAction(player -> ActionUtils.rightClickOnNearestEntity(player, new BlockPos(100, 65, 200), 3.0)); 
// 自动村民交易（第1个交易2次） XXX.addAction(player -> ActionUtils.autoVillagerTradeFull(player, 0, 2)); 
// 自动箱子GUI点击（第31格） XXX.addAction(player -> ActionUtils.autoChestClick(player, 30)); 

import com.keycommand.keycommandmod.KeyCommandMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class ActionUtils {
    /* ■■■■■■■■■■■■■■
     * 设置玩家视角角度
     * ■■■■■■■■■■■■■■
     */
    public static void setPlayerViewAngles(EntityPlayerSP player, float yaw, float pitch) {
        player.rotationYaw = yaw;
        player.rotationPitch = pitch;
        player.rotationYawHead = yaw;
        player.prevRotationYaw = yaw;
        player.prevRotationPitch = pitch;
        KeyCommandMod.LOGGER.info("Set player view angles: yaw={}, pitch={}", yaw, pitch);
    }

    /* ■■■■■■■■■■
     * 发送聊天命令
     * ■■■■■■■■■■
     */
    public static void sendChatCommand(String command) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null && !player.isSpectator()) {
            player.sendChatMessage(command);
            KeyCommandMod.LOGGER.info("Sent command: " + command);
        }
    }

    /* ■■■■■■■■■■■
     * 右键点击方块
     * ■■■■■■■■■■■
     */
    public static void rightClickOnBlock(EntityPlayerSP player, BlockPos pos) {
        EnumFacing facing = EnumFacing.UP;
        Vec3d hitVec = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        Minecraft.getMinecraft().playerController.processRightClickBlock(player, Minecraft.getMinecraft().world, pos, facing, hitVec, EnumHand.MAIN_HAND);
        player.swingArm(EnumHand.MAIN_HAND);
        KeyCommandMod.LOGGER.info("Right clicked at: " + pos);
    }

    /* ■■■■■■■■■■■■■■
     * 右键点击附近实体
     * ■■■■■■■■■■■■■■
     */
    public static void rightClickOnNearestEntity(EntityPlayerSP player, BlockPos pos, double range) {
        double px = pos.getX() + 0.5;
        double py = pos.getY() + 0.5;
        double pz = pos.getZ() + 0.5;
        Entity nearest = null;
        double minDistSq = Double.MAX_VALUE;

        for (Entity entity : Minecraft.getMinecraft().world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(px - range, py - range, pz - range, px + range, py + range, pz + range))) {
            if (entity == player) continue;
            double distSq = entity.getDistanceSq(px, py, pz);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                nearest = entity;
            }
        }

        if (nearest != null) {
            Minecraft.getMinecraft().playerController.interactWithEntity(player, nearest, EnumHand.MAIN_HAND);
            player.swingArm(EnumHand.MAIN_HAND);
            KeyCommandMod.LOGGER.info("Right clicked entity {} at {}", nearest.getName(), pos);
        } else {
            KeyCommandMod.LOGGER.warn("No entity found near: " + pos);
        }
    }

    /* ■■■■■■■
     * 延迟动作
     * ■■■■■■■
     */
    public static class DelayAction implements Consumer<EntityPlayerSP> {
        private final int delayTicks;

        public DelayAction(int delayTicks) {
            this.delayTicks = delayTicks;
        }

        @Override
        public void accept(EntityPlayerSP player) {}

        public int getDelayTicks() {
            return delayTicks;
        }
    }

    /* ■■■■■■■■■■
     * 自动村民交易
     * ■■■■■■■■■■
     */
    public static void autoVillagerTradeFull(EntityPlayerSP player, int tradeIndex, int tradeCount) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof GuiMerchant) || tradeCount <= 0) return;
        GuiMerchant gui = (GuiMerchant) mc.currentScreen;
        MerchantRecipeList recipes = gui.getMerchant().getRecipes(player);

        if (recipes == null || tradeIndex < 0 || tradeIndex >= recipes.size()) return;
        MerchantRecipe recipe = recipes.get(tradeIndex);
        if (recipe == null || recipe.isRecipeDisabled()) return;

        // 反射设置当前交易索引
        try {
            java.lang.reflect.Field field = GuiMerchant.class.getDeclaredField("currentRecipeIndex");
            field.setAccessible(true);
            field.setInt(gui, tradeIndex);
        } catch (Exception e) {
            try {
                java.lang.reflect.Field field = GuiMerchant.class.getDeclaredField("field_147041_z");
                field.setAccessible(true);
                field.setInt(gui, tradeIndex);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        for (int t = 0; t < tradeCount; t++) {
            if (!fillMerchantInputsWithNBT(gui, recipe)) {
                KeyCommandMod.LOGGER.warn("背包中缺少交易所需物品（含NBT），无法继续交易");
                break;
            }
            Slot outputSlot = gui.inventorySlots.getSlot(2);
            if (outputSlot != null && outputSlot.getHasStack()) {
                int emptySlot = findFirstEmptyInventorySlot(gui);
                if (emptySlot >= 0) {
                    mc.playerController.windowClick(gui.inventorySlots.windowId, 2, 0, ClickType.PICKUP, player);
                    mc.playerController.windowClick(gui.inventorySlots.windowId, emptySlot, 0, ClickType.PICKUP, player);
                } else {
                    KeyCommandMod.LOGGER.warn("背包已满，无法领取交易物品！");
                    break;
                }
            } else {
                mc.playerController.windowClick(gui.inventorySlots.windowId, 2, 0, ClickType.PICKUP, player);
            }
        }

        KeyCommandMod.LOGGER.info("自动完成村民交易（含NBT精确匹配），交易序号: " + tradeIndex + "，次数: " + tradeCount);
        clearMerchantInputSlot(gui, 0);
        clearMerchantInputSlot(gui, 1);
        mc.playerController.windowClick(gui.inventorySlots.windowId, 0, 0, ClickType.QUICK_MOVE, player);
        mc.playerController.windowClick(gui.inventorySlots.windowId, 1, 0, ClickType.QUICK_MOVE, player);
    }

    // 填充村民交易输入物品（支持NBT）
    private static boolean fillMerchantInputsWithNBT(GuiMerchant gui, MerchantRecipe recipe) {
        clearMerchantInputSlot(gui, 0);
        clearMerchantInputSlot(gui, 1);
        boolean ok1 = moveItemToInputWithNBT(gui, recipe.getItemToBuy(), 0, recipe.getItemToBuy().getCount());
        boolean ok2 = true;
        if (!recipe.getSecondItemToBuy().isEmpty()) {
            ok2 = moveItemToInputWithNBT(gui, recipe.getSecondItemToBuy(), 1, recipe.getSecondItemToBuy().getCount());
        }
        return ok1 && ok2;
    }

    // 移动物品到输入槽（匹配NBT）
    private static boolean moveItemToInputWithNBT(GuiMerchant gui, ItemStack targetStack, int inputSlot, int neededCount) {
        int moved = 0;
        for (int i = 0; i <= 35; i++) {
            Slot slot = gui.inventorySlots.getSlot(i);
            if (slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                if (itemStackNBTEquals(stack, targetStack)) {
                    int toMove = Math.min(stack.getCount(), neededCount - moved);
                    for (int j = 0; j < toMove; j++) {
                        Minecraft.getMinecraft().playerController.windowClick(gui.inventorySlots.windowId, i, 0, ClickType.PICKUP, Minecraft.getMinecraft().player);
                        Minecraft.getMinecraft().playerController.windowClick(gui.inventorySlots.windowId, inputSlot, 0, ClickType.PICKUP, Minecraft.getMinecraft().player);
                        moved++;
                        stack = slot.getStack();
                        if (stack == null || stack.isEmpty()) break;
                    }
                }
            }
            if (moved >= neededCount) break;
        }
        return moved >= neededCount;
    }

    // 比较ItemStack的NBT
    private static boolean itemStackNBTEquals(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        NBTTagCompound nbtA = a.getTagCompound();
        NBTTagCompound nbtB = b.getTagCompound();
        if (nbtA == null && nbtB == null) return true;
        if (nbtA == null || nbtB == null) return false;
        return nbtA.equals(nbtB);
    }

    // 查找空槽位
    private static int findFirstEmptyInventorySlot(GuiMerchant gui) {
        for (int i = 0; i <= 35; i++) {
            Slot slot = gui.inventorySlots.getSlot(i);
            if (slot != null && !slot.getHasStack()) {
                return i;
            }
        }
        return -1;
    }

    // 清空村民输入槽
    private static void clearMerchantInputSlot(GuiMerchant gui, int slotId) {
        Slot slot = gui.inventorySlots.getSlot(slotId);
        if (slot != null && slot.getHasStack()) {
            Minecraft.getMinecraft().playerController.windowClick(gui.inventorySlots.windowId, slotId, 0, ClickType.QUICK_MOVE, Minecraft.getMinecraft().player);
            if (slot.getHasStack()) {
                Minecraft.getMinecraft().playerController.windowClick(gui.inventorySlots.windowId, slotId, 1, ClickType.PICKUP, Minecraft.getMinecraft().player);
            }
        }
    }

    /* ■■■■■■■■■■■■■■
     * 自动点击箱子格子
     * ■■■■■■■■■■■■■■
     */
    public static void autoChestClick(EntityPlayerSP player, int chestSlotIndex) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof GuiChest) {
            GuiChest gui = (GuiChest) mc.currentScreen;
            if (chestSlotIndex >= 0 && chestSlotIndex < gui.inventorySlots.inventorySlots.size()) {
                mc.playerController.windowClick(gui.inventorySlots.windowId, chestSlotIndex, 0, ClickType.PICKUP, player);
                KeyCommandMod.LOGGER.info("自动点击箱子格子: " + chestSlotIndex);
            }
        }
    }
}