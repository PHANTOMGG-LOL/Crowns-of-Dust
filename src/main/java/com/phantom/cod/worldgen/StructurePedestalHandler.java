package com.phantom.cod.worldgen;

import com.phantom.cod.CrownsOfDust;
import com.phantom.cod.block.PedestalBlockEntity;
import com.phantom.cod.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public final class StructurePedestalHandler {

    private StructurePedestalHandler() {
    }

    // =========================================================
    // STRUCTURE KEYS
    // =========================================================

    private static final ResourceKey<Structure> ASHENREACH =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            CrownsOfDust.MOD_ID,
                            "ashenreach"
                    )
            );

    private static final ResourceKey<Structure> ELDORIA =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            CrownsOfDust.MOD_ID,
                            "eldoria"
                    )
            );

    private static final ResourceKey<Structure> VALDREN =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            CrownsOfDust.MOD_ID,
                            "valdren"
                    )
            );

    private static final ResourceKey<Structure> MYRATH =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            CrownsOfDust.MOD_ID,
                            "myrath"
                    )
            );

    private static final ResourceKey<Structure> TEMPLE =
            ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath(
                            CrownsOfDust.MOD_ID,
                            "temple_of_god"
                    )
            );


    // =========================================================
    // CHUNK LOAD
    // =========================================================

    @SubscribeEvent
    public static void onChunkLoad(
            ChunkEvent.Load event) {

        if (!(event.getLevel()
                instanceof ServerLevel level)) {

            return;
        }

        // Only process newly generated chunks.
        if (!event.isNewChunk()) {
            return;
        }

        var chunk = event.getChunk();

        int minX =
                chunk.getPos().getMinBlockX();

        int maxX =
                chunk.getPos().getMaxBlockX();

        int minZ =
                chunk.getPos().getMinBlockZ();

        int maxZ =
                chunk.getPos().getMaxBlockZ();


        // =====================================================
        // SEARCH CHUNK
        // =====================================================

        for (int x = minX;
             x <= maxX;
             x++) {

            for (int z = minZ;
                 z <= maxZ;
                 z++) {

                for (int y = level.getMinY();
                     y <= level.getMaxY();
                     y++) {

                    BlockPos pos =
                            new BlockPos(x, y, z);


                    // =================================================
                    // REINFORCED DEEPSLATE
                    // =================================================

                    if (!level.getBlockState(pos)
                            .is(Blocks.REINFORCED_DEEPSLATE)) {

                        continue;
                    }


                    // =================================================
                    // FIND OUR STRUCTURE
                    // =================================================

                    StructureType structureType =
                            getStructureType(
                                    level,
                                    pos
                            );

                    if (structureType ==
                            StructureType.NONE) {

                        continue;
                    }


                    // =================================================
                    // PEDESTAL POSITION
                    // =================================================

                    BlockPos pedestalPos =
                            pos.above();


                    // Don't replace an existing block.
                    var stateAbove =
                            level.getBlockState(
                                    pedestalPos
                            );

                    if (!stateAbove.isAir()
                            && !stateAbove.is(Blocks.SNOW)) {

                        continue;
                    }


                    // =================================================
                    // PLACE PEDESTAL
                    // =================================================

                    level.setBlock(
                            pedestalPos,
                            ModBlocks.PEDESTAL
                                    .get()
                                    .defaultBlockState(),
                            3
                    );


                    // =================================================
                    // INITIALIZE PEDESTAL
                    // =================================================

                    BlockEntity blockEntity =
                            level.getBlockEntity(
                                    pedestalPos
                            );

                    if (blockEntity
                            instanceof PedestalBlockEntity pedestal) {

                        pedestal.setTemplePedestal(
                                structureType ==
                                        StructureType.TEMPLE
                        );
                    }


                    CrownsOfDust.LOGGER.info(
                            "Placed {} pedestal at {} {} {}",
                            structureType,
                            pedestalPos.getX(),
                            pedestalPos.getY(),
                            pedestalPos.getZ()
                    );
                }
            }
        }
    }


    // =========================================================
    // STRUCTURE TYPE
    // =========================================================

    private enum StructureType {

        NONE,

        KINGDOM,

        TEMPLE
    }


    // =========================================================
    // STRUCTURE CHECK
    // =========================================================

    private static StructureType getStructureType(
            ServerLevel level,
            BlockPos pos) {

        StructureStart start =
                level.structureManager()
                        .getStructureWithPieceAt(
                                pos,
                                holder ->
                                        holder.is(ASHENREACH)
                                                || holder.is(ELDORIA)
                                                || holder.is(VALDREN)
                                                || holder.is(MYRATH)
                                                || holder.is(TEMPLE)
                        );

        if (start == null ||
                !start.isValid()) {

            return StructureType.NONE;
        }


        /*
         * We need to determine which structure
         * the position belongs to.
         *
         * Check Temple first.
         */

        StructureStart templeStart =
                level.structureManager()
                        .getStructureWithPieceAt(
                                pos,
                                holder ->
                                        holder.is(TEMPLE)
                        );

        if (templeStart != null &&
                templeStart.isValid()) {

            return StructureType.TEMPLE;
        }


        // =====================================================
        // KINGDOM
        // =====================================================

        StructureStart kingdomStart =
                level.structureManager()
                        .getStructureWithPieceAt(
                                pos,
                                holder ->
                                        holder.is(ASHENREACH)
                                                || holder.is(ELDORIA)
                                                || holder.is(VALDREN)
                                                || holder.is(MYRATH)
                        );

        if (kingdomStart != null &&
                kingdomStart.isValid()) {

            return StructureType.KINGDOM;
        }


        return StructureType.NONE;
    }
}