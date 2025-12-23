package com.keycommand.keycommandmod.gui.path;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.entity.EntityPlayerSP;

public class PathStep {
    private final double[] gotoPoint;
    private final List<Consumer<EntityPlayerSP>> actions = new ArrayList<>();

    public PathStep(double[] gotoPoint) {
        this.gotoPoint = gotoPoint;
    }

    public void addAction(Consumer<EntityPlayerSP> action) {
        actions.add(action);
    }

    public double[] getGotoPoint() {
        return gotoPoint;
    }

    public List<Consumer<EntityPlayerSP>> getActions() {
        return actions;
    }
}