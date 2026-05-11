package com.deflanko.MCCFishingMessages;


import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;


import java.util.Arrays;
import java.util.List;


public class FishingSpot{

    private static boolean isFishing = false;
    private static int waitTime = 0;
    private static FishingSpot currentFishingSpot = null;

    private final String location;
    private final List<String> perks;
    private final Display.TextDisplay entity;

    public static FishingSpot getCurrentFishingSpot() {
        return currentFishingSpot;
    }

    public List<String> getPerks() {
        return perks;
    }

    public String getLocation() {
        return location;
    }

    public FishingSpot(String location, List<String> perks, Display.TextDisplay entity) {
        this.location = location;
        this.perks = perks;
        this.entity = entity;
    }

    public Display.TextDisplay getEntity() {
        return entity;
    }

    public static void checkFishing() {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        FishingHook fishHook = player.fishing;

        // Fix the logical flow to prevent NPE
        if (fishHook == null) {
            if (isFishing) {
                isFishing = false;
            }
            // Always return if fishHook is null
            return;
        }

        if (fishHook.isInLiquid() && !isFishing) {
            isFishing = true;
            waitTime = 0;
            getFishingSpot(player, fishHook);
        }
    }

    private static void getFishingSpot(Player player, FishingHook fishHook) {

        BlockPos blockPos = fishHook.blockPosition();
        AABB box = AABB.ofSize(blockPos.getCenter(), 3.5, 6.0, 3.5);
        //List<Entity> entities = player.getWorld().getOtherEntities(null, box)
        //New for 1.21.11
        List<Entity> entities = Minecraft.getInstance().level.getEntities(null, box)
                .stream()
                .filter(entity -> entity instanceof Display.TextDisplay)
                .toList();

        if (!entities.isEmpty()) {
            Display.TextDisplay textDisplay = (Display.TextDisplay) entities.getFirst();
            if (currentFishingSpot != null && currentFishingSpot.getEntity().equals(textDisplay)) {
                return;
            }

            String text = textDisplay.getText().getString();
            //debug logging (pre)
            System.out.println("Raw text from display: [" + text + "]");

            int fishingSpotX = textDisplay.getBlockX();
            int fishingSpotY = textDisplay.getBlockY();
            int fishingSpotZ = textDisplay.getBlockZ();
            // Format location to match the game's paste format
            String location = String.format("%d %d %d", fishingSpotX, fishingSpotY, fishingSpotZ);

            // Create a comma-separated list on a single line.
            // Split by newlines and look for lines starting with +
            List<String> perks = Arrays.stream(text.split("\n"))
                    .map(String::trim)
                    .filter(line -> line.contains("+"))
                    // Sanitize perks by removing invalid characters
                    .map(line -> line.replaceAll("[^\\x20-\\x7E]", ""))
                    .toList();

            System.out.println("Parsed location: " + location);
            System.out.println("Parsed perks: " + perks);

            if (!perks.isEmpty()) {
                currentFishingSpot = new FishingSpot(location, perks, textDisplay);
                String formattedPerks = String.join(", ", perks);

                //debug logging (post)
                System.out.println("Formatted perks for clipboard: " + formattedPerks);

            }
        }

    }
    @Override
    public String toString() {
        // Get just the perk text without any coordinates
        String perksText = perks != null ? String.join(", ", perks) : "none";

        // Return formatted string with location and perks on one line
        return perksText;
    }
}
