package com.bindothorpe.champions.domain.game.capturePoint;

import com.bindothorpe.champions.domain.game.GameManager;
import com.bindothorpe.champions.domain.team.TeamColor;
import com.bindothorpe.champions.events.game.capturepoint.CapturePointCaptureEvent;
import com.bindothorpe.champions.events.game.capturepoint.CapturePointCapturingEvent;
import com.bindothorpe.champions.events.game.capturepoint.CapturePointStartCaptureEvent;
import com.bindothorpe.champions.util.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class CapturePoint {

    private static final List<Vector> CAPTURE_POINT_BLOCK_LOCATIONS = List.of(
            new Vector(-2, 0, -1),
            new Vector(2, 0, -2),
            new Vector(-1, 0, 2),
            new Vector(1, 0, -2),
            new Vector(-2, 0, 1),
            new Vector(2, 0, 2),
            new Vector(-1, 0, -1),
            new Vector(1, 0, 1),
            new Vector(-2, 0, 2),
            new Vector(2, 0, -1),
            new Vector(-1, 0, 0),

            new Vector(1, 0, -1),
            new Vector(-2, 0, -2),
            new Vector(2, 0, 1),
            new Vector(0, 0, 1),
            new Vector(1, 0, 2),
            new Vector(-1, 0, 1),
            new Vector(0, 0, -1),
            new Vector(2, 0, 0),
            new Vector(-1, 0, -2),
            new Vector(1, 0, 0),

            new Vector(-2, 0, 0),
            new Vector(0, 0, 2),
            new Vector(0, 0, -2),
            new Vector(0, 0, 0)
    );
    private final GameManager gameManager;
    private final UUID id;
    private final String name;
    private final Vector location;
    private final World world;
    private TeamColor team;
    private int captureProgress;
    private BoundingBox boundingBox;

    public CapturePoint(GameManager gameManager, String name, Vector location, World world) {
        this.gameManager = gameManager;
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.world = world;
    }

    public void update() {
        Map<TeamColor, Integer> teamColorIntegerMap = getEntitiesInRange().stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .collect(Collectors.groupingBy(player -> gameManager.getDc().getTeamFromEntity(player), Collectors.summingInt(player -> 1)));

        TeamColor teamColor = teamColorIntegerMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        List<Integer> topTwo = teamColorIntegerMap.values().stream()
                .sorted(Comparator.reverseOrder())
                .limit(2)
                .collect(Collectors.toList());

        int result = 0;
        if (topTwo.size() == 2) {
            result = topTwo.get(0) - topTwo.get(1);
        } else if (topTwo.size() == 1) {
            result = topTwo.get(0);
        }

        increaseCaptureProgress(teamColor, result);
    }

    public void increaseCaptureProgress(TeamColor capturingTeam, int playersInRange) {
        // Get the increment of the team, if red it will be negative, if blue it will be positive
        int increment = getIncrement(capturingTeam);

        // Save the oldCaptureProgress for later
        int oldCaptureProgress = captureProgress;

        if (oldCaptureProgress == 25 || oldCaptureProgress == -25) {
            Bukkit.getPluginManager().callEvent(new CapturePointStartCaptureEvent(this, capturingTeam));
        }

        // Increment the capture progress
        incrementCaptureProgress(increment * playersInRange);

        // Update the team depending on the capture progress
        updateTeam();

        // Update the blocks depending on the capture progress
        updateBlocks(oldCaptureProgress, captureProgress);

        if(oldCaptureProgress != captureProgress) {
            if(oldCaptureProgress < captureProgress) {
                Bukkit.getPluginManager().callEvent(new CapturePointCapturingEvent(this, TeamColor.BLUE));
            } else {
                Bukkit.getPluginManager().callEvent(new CapturePointCapturingEvent(this, TeamColor.RED));
            }
        }

    }

    private void incrementCaptureProgress(int increment) {
        if (increment != 0) {
            captureProgress += increment;
            captureProgress = Math.max(-25, Math.min(captureProgress, 25));
            return;
        }

        if (captureProgress < 25 && team == TeamColor.BLUE) {
            captureProgress = captureProgress + 1;
        } else if (captureProgress > -25 && team == TeamColor.RED) {
            captureProgress = captureProgress - 1;
        } else if (team == null) {
            if (captureProgress > 0)
                captureProgress = captureProgress - 1;
            else if (captureProgress < 0)
                captureProgress = captureProgress + 1;
        }
    }

    private void updateTeam() {
        if (captureProgress == 25) {
            if (team == TeamColor.BLUE)
                return;
            setTeam(TeamColor.BLUE);
        } else if (captureProgress == -25) {
            if (team == TeamColor.RED)
                return;
            setTeam(TeamColor.RED);
        } else if (captureProgress <= 0 && team == TeamColor.BLUE) {
            setTeam(null);
        } else if (captureProgress >= 0 && team == TeamColor.RED) {
            setTeam(null);
        }
    }

    private void updateBlocks(int oldCaptureProgress, int newCaptureProgress) {
        if (oldCaptureProgress == newCaptureProgress)
            return;

        if (newCaptureProgress == 0) {
            for (Vector vector : CAPTURE_POINT_BLOCK_LOCATIONS) {
                world.getBlockAt(getLocation().add(vector).subtract(0, 1, 0)).setType(Material.WHITE_STAINED_GLASS);
                if(vector.getX() != 0 || vector.getZ() != 0)
                    world.getBlockAt(getLocation().add(vector).subtract(0, 2, 0)).setType(Material.WHITE_WOOL);
            }

            playCapturingSound();
            return;
        }

        TeamColor color = newCaptureProgress > 0 ? TeamColor.BLUE : newCaptureProgress < 0 ? TeamColor.RED : null;


        oldCaptureProgress = Math.abs(oldCaptureProgress);
        newCaptureProgress = Math.abs(newCaptureProgress);


        int increment = 0;

        if (newCaptureProgress > oldCaptureProgress) {
            increment = 1;
        } else {
            increment = -1;
        }


        Material glassMaterial = Material.WHITE_STAINED_GLASS;
        Material woolMaterial = Material.WHITE_WOOL;

        if (color == TeamColor.BLUE && increment == 1) {
            glassMaterial = Material.BLUE_STAINED_GLASS;
            woolMaterial = Material.BLUE_WOOL;
        } else if (color == TeamColor.RED && increment == 1) {
            glassMaterial = Material.RED_STAINED_GLASS;
            woolMaterial = Material.RED_WOOL;
        }

        if (newCaptureProgress != 25) {
            Vector vector = CAPTURE_POINT_BLOCK_LOCATIONS.get(newCaptureProgress - 1);
            Location glassLocation = getLocation().add(vector).subtract(0, 1, 0);
            Location woolLocation = getLocation().add(vector).subtract(0, 2, 0);

            glassLocation.getBlock().setType(glassMaterial);
            if(vector.getX() != 0 || vector.getZ() != 0)
                woolLocation.getBlock().setType(woolMaterial);

        } else {
            for (Vector vector : CAPTURE_POINT_BLOCK_LOCATIONS) {
                world.getBlockAt(getLocation().add(vector).subtract(0, 1, 0)).setType(glassMaterial);
                if(vector.getX() != 0 || vector.getZ() != 0)
                    world.getBlockAt(getLocation().add(vector).subtract(0, 2, 0)).setType(woolMaterial);
            }
        }
        playCapturingSound();

    }

    private int getIncrement(TeamColor teamColor) {
        return TeamColor.BLUE.equals(teamColor) ? 1 : TeamColor.RED.equals(teamColor) ? -1 : 0;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return new Location(world, location.getX(), location.getY(), location.getZ());
    }

    private void setTeam(TeamColor team) {
        this.team = team;

        if(team == null)
            return;

        Bukkit.getPluginManager().callEvent(new CapturePointCaptureEvent(this, team));
    }

    private void playCapturingSound() {
        world.playSound(getLocation(), Sound.BLOCK_STONE_PLACE, 1, 1);
        world.playSound(getLocation(), Sound.BLOCK_WOOL_PLACE, 1, 1);
    }

    public Set<LivingEntity> getEntitiesInRange() {
        return world.getNearbyEntities(getBoundingBox()).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .collect(Collectors.toSet());
    }

    private BoundingBox getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new BoundingBox(
                    location.getX() - 2,
                    location.getY(),
                    location.getZ() - 2,
                    location.getX() + 2,
                    location.getY() + 3,
                    location.getZ() + 2
            );
        }
        return boundingBox;
    }
}
