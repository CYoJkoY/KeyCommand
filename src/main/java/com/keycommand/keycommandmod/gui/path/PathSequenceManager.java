package com.keycommand.keycommandmod.gui.path;

import java.util.HashMap;
import java.util.Map;

public class PathSequenceManager {
    private final Map<String, PathSequence> sequences = new HashMap<>();

    public void addSequence(PathSequence sequence) {
        sequences.put(sequence.getName(), sequence);
    }

    public PathSequence getSequence(String name) {
        return sequences.get(name);
    }

    public boolean hasSequence(String name) {
        return sequences.containsKey(name);
    }
}