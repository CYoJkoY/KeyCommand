package com.keycommand.keycommandmod.gui.path;

import java.util.ArrayList;
import java.util.List;

public class PathSequence {
    private final String name;
    private final List<PathStep> steps = new ArrayList<>();

    public PathSequence(String name) {
        this.name = name;
    }

    public void addStep(PathStep step) {
        steps.add(step);
    }

    public String getName() {
        return name;
    }

    public List<PathStep> getSteps() {
        return steps;
    }
}