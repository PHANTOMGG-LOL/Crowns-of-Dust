package com.phantom.cod.temple;

import com.phantom.cod.CrownsOfDust;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.util.RandomSource;

import java.util.Optional;

public final class ChoiceRoomStructure {

    private ChoiceRoomStructure() {
    }

    // ==================================================
    // STRUCTURE ID
    // ==================================================

    private static final Identifier STRUCTURE_ID =
            Identifier.fromNamespaceAndPath(
                    CrownsOfDust.MOD_ID,
                    "choice_room"
            );

    // ==================================================
    // STRUCTURE ORIGIN
    // ==================================================

    /*
     * The NBT is placed with its origin at:
     *
     * X = 0
     * Y = 0
     * Z = 0
     */
    private static final BlockPos STRUCTURE_ORIGIN =
            new BlockPos(0, 0, 0);

    // ==================================================
    // PLAYER SPAWN POSITION
    // ==================================================

    /*
     * Center of the 91 x 39 structure.
     *
     * X center = 45
     * Z center = 19
     *
     * The .5 puts the player in the center
     * of the block.
     *
     * Y = 1 means the player stands one block
     * above the structure's Y=0 layer.
     */
    private static final double PLAYER_X =
            45.5D;

    private static final double PLAYER_Y =
            1.0D;

    private static final double PLAYER_Z =
            19.5D;

    // ==================================================
    // GET PLAYER X
    // ==================================================

    public static double getPlayerX() {
        return PLAYER_X;
    }

    // ==================================================
    // GET PLAYER Y
    // ==================================================

    public static double getPlayerY() {
        return PLAYER_Y;
    }

    // ==================================================
    // GET PLAYER Z
    // ==================================================

    public static double getPlayerZ() {
        return PLAYER_Z;
    }

    // ==================================================
    // CHECK IF STRUCTURE EXISTS
    // ==================================================

    public static boolean isAlreadyPlaced(
            ServerLevel level) {

        /*
         * We use a block inside the expected structure
         * area as a simple marker.
         *
         * The choice room starts at 0,0,0, so checking
         * this position tells us whether the dimension
         * has already been populated.
         */
        BlockPos marker =
                STRUCTURE_ORIGIN;

        return !level.getBlockState(marker).isAir();
    }

    // ==================================================
    // PLACE STRUCTURE
    // ==================================================

    public static boolean place(
            ServerLevel level) {

        // ==================================================
        // DON'T PLACE TWICE
        // ==================================================

        if (isAlreadyPlaced(level)) {

            CrownsOfDust.LOGGER.info(
                    "Choice room already exists. "
                            + "Skipping structure placement."
            );

            return true;
        }

        // ==================================================
        // LOAD NBT
        // ==================================================

        Optional<StructureTemplate> template =
                level.getStructureManager()
                        .get(STRUCTURE_ID);

        if (template.isEmpty()) {

            CrownsOfDust.LOGGER.error(
                    "Could not find choice room structure: {}",
                    STRUCTURE_ID
            );

            return false;
        }

        // ==================================================
        // PLACE SETTINGS
        // ==================================================

        StructurePlaceSettings settings =
                new StructurePlaceSettings();

        // ==================================================
        // PLACE STRUCTURE
        // ==================================================

        boolean placed =
                template.get().placeInWorld(
                        level,
                        STRUCTURE_ORIGIN,
                        STRUCTURE_ORIGIN,
                        settings,
                        RandomSource.create(),
                        2
                );

        // ==================================================
        // RESULT
        // ==================================================

        if (placed) {

            CrownsOfDust.LOGGER.info(
                    "Choice room structure placed at {}",
                    STRUCTURE_ORIGIN
            );

        } else {

            CrownsOfDust.LOGGER.error(
                    "Failed to place choice room structure."
            );
        }

        return placed;
    }
}