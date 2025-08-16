package com.deflanko.MCCFishingMessages;

import java.util.Arrays;
import java.util.List;

/* Notes on locations
        minX    minZ    maxX    maxZ
---     -----   -----   ----    ----
i9:     1050    2570    1510    3080
i8:     2680    1110    2960    1460
i7:     2560    -484    3100    -10
i6:     1573    -406    1982    -60
i5:     -488    1633    -72     2023
i4:     596     1532    1030    1982
i3:     1589    1626    1976    1983
i2:     1630    570     2000    928
i1:     -470    30      -50     460

warp points

        x       y       z
---     -----   -----   ----
i9:     1101    152     2752
i8:     2697    153     1213
i7:     2856    153     -96
i6:     1941    151     -285
i5:     -191    151     1921
i4:     778     152     1758
i3:     1860    151     1684
i2:     1871    151     684
i1:     -154    150     260

*/

public class FishingLocation {
    private int islandNumber = 0;
    private final List<IslandBoundary> islands = Arrays.asList(
            new IslandBoundary(1, -470, -50, 30, -50, 250, 460),        // Island 1
            new IslandBoundary(2, 1630, -50, 570, 2000, 250, 928),      // Island 2
            new IslandBoundary(3, 1589, -50, 1626, 1976, 250, 1983),    // Island 3
            new IslandBoundary(4, 596, -50, 1532, 1030, 250, 1982),     // Island 4
            new IslandBoundary(5, -488, -50, 1633, -72, 250, 2023),     // Island 5
            new IslandBoundary(6, 1570, -50, -406, 1982, 250, -60),     // Island 6
            new IslandBoundary(7, 2560, -50, -484, 3100, 250, -10),     // Island 7
            new IslandBoundary(8, 2680, -50, 1110, 2960, 250, 1460),    // Island 8
            new IslandBoundary(9, 1050, -50, 2570, 1510, 250, 3080)     // Island 9
    );

    public void updateLocation(double x, double y, double z) {
        for (IslandBoundary island : islands) {
            if (island.contains(x, y, z)) {
                islandNumber = island.number;
                return;
            }
        }
        islandNumber = 0; // Not in any known island
    }

    public int getIslandNumber() {
        return islandNumber;
    }

    private static class IslandBoundary {
        final int number;
        final double minX, minY, maxX, maxY, minZ, maxZ;

        IslandBoundary(int number, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.number = number;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        boolean contains(double x, double y, double z) {
            return x >= minX && x <= maxX &&
                    y >= minY && y <= maxY &&
                    z >= minZ && z <= maxZ;
        }
    }
}
