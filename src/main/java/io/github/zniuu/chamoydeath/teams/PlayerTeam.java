package io.github.zniuu.chamoydeath.teams;

import net.kyori.adventure.text.format.TextColor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerTeam {

    private final String name;
    private final TextColor color;
    private final Set<UUID> members = new LinkedHashSet<>();

    public PlayerTeam(String name, TextColor color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public TextColor getColor() { return color; }
    public Set<UUID> getMembers() { return members; }

    public void addMember(UUID uuid) { members.add(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }
    public boolean isMember(UUID uuid) { return members.contains(uuid); }
}
