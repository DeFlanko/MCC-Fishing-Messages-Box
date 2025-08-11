package com.deflanko.MCCFishingMessages;


import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.util.math.Box;


import java.util.Arrays;
import java.util.List;


public class FishingSpot{

    private static boolean isFishing = false;
    private static int waitTime = 0;
    private static FishingSpot currentFishingSpot = null;

    private final String location;
    private final List<String> perks;
    private final DisplayEntity.TextDisplayEntity entity;

    public List<String> getPerks() {
        return perks;
    }

    public String getLocation() {
        return location;
    }

    public FishingSpot(String location, List<String> perks, DisplayEntity.TextDisplayEntity entity) {
        this.location = location;
        this.perks = perks;
        this.entity = entity;
    }

    public DisplayEntity.TextDisplayEntity getEntity() {
        return entity;
    }

    private void checkFishing() {
        PlayerEntity player = MinecraftClient.getInstance().player;

        assert player != null;

        FishingBobberEntity fishHook = player.fishHook;

        if (fishHook == null) {
            if (isFishing) {
                isFishing = false;
                return;
            }
            if (currentFishingSpot == null) {
                return;
            }

        }

        if (fishHook.isInFluid() && !isFishing) {
            isFishing = true;
            waitTime = 0;
            getFishingSpot(player, fishHook);
        }
    }

    private void getFishingSpot(PlayerEntity player, FishingBobberEntity fishHook) {

        BlockPos blockPos = fishHook.getBlockPos();
        Box box = Box.of(blockPos.toCenterPos(), 3.5, 6.0, 3.5);
        List<Entity> entities = player.getWorld().getOtherEntities(null, box)
                .stream()
                .filter(entity -> entity instanceof DisplayEntity.TextDisplayEntity)
                .toList();

        if (!entities.isEmpty()) {
            DisplayEntity.TextDisplayEntity textDisplay = (DisplayEntity.TextDisplayEntity) entities.getFirst();
            if (currentFishingSpot != null && currentFishingSpot.getEntity().equals(textDisplay)) {
                return;
            }

            String text = textDisplay.getText().getString();

            int fishingSpotX = textDisplay.getBlockX();
            int fishingSpotZ = textDisplay.getBlockZ();

            List<String> perks = Arrays.stream(text.split("\n"))
                    .filter(line -> line.contains("+"))
                    .map(line -> "+" + line.split("\\+")[1])
                    .toList();
            if (!perks.isEmpty()) {

                currentFishingSpot = new FishingSpot(fishingSpotX + "/" + fishingSpotZ, perks, textDisplay);


            }
        }

    }

}
