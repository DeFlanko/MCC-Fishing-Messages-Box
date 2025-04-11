package com.deflanko.MCCFishingMessages.config;

import com.google.gson.*;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import java.util.*;

public class Config {

    public static final InstanceCreator<Config> INSTANCE_CREATOR = type -> new Config();

    @SuppressWarnings("unused")
    public Config() {
    }

    public Config(Config other) {
        this.boxX = other.boxX;
        this.boxY = other.boxY;
        this.boxWidth = other.boxWidth;
        this.boxHeight = other.boxHeight;
        this.fontSize = other.fontSize;
        this.pulledPhrases = other.pulledPhrases;
        this.blockedPhrases = other.blockedPhrases;
    }

    @SerialEntry
    public int boxX = 0;// Default position
    @SerialEntry
    public int boxY = 30; // Top of screen, below  bar
    @SerialEntry
    public int boxWidth = 330;
    @SerialEntry
    public int boxHeight = 110;
    @SerialEntry
    public float fontSize = 1.0f;
    @SerialEntry
    public List<String> pulledPhrases= new ArrayList<>(List.of(
            "all pots are fully repaired!"
            , "an error occurred whilst catching a fish. please try again or use /bugreport for details of how to report bugs. your consumables will not be consumed."
            , "changed crab pot climate!"
            , "crab pot claimed! contents sent to your infinibag."
            , "fishing spot stock replenished!"
            , "fishing stock has replenished!"
            , " has run out of uses"
            , "repaired crab pots!"
            , "research meter ready to claim! check your a.n.g.l.r. panel."
            , "special:"
            , "that's not a fishing spot! locate one and cast there."
            , "this spot is depleted, so you can no longer fish here."
            , "triggered:"
            , "while active, all islands in this climate receive:"
            , "you caught:"
            , "you earned:"
            , "you moved too far away from your currently cast spot, and your cast was canceled"
            , "you receive:"
            , "you've discovered a "
            , "you've reached fishing level"
            , "you've run out of your equipped"
            , "your grotto has become unstable, teleporting you back to safety..."
    ));
    @SerialEntry
    public List<String> blockedPhrases= new ArrayList<>(List.of(
            "this is an example of a blocked message",
            "and this is another example of a blocked phrase"
            //best not to ship with a phrase that a portion of the community uses.
    ));
}
