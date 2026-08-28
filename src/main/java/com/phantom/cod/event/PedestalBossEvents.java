package com.phantom.cod.event;

import com.phantom.cod.block.PedestalBlockEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public final class PedestalBossEvents {

    private PedestalBossEvents() {
    }

    @SubscribeEvent
    public static void onLivingDeath(
            LivingDeathEvent event) {

        LivingEntity entity =
                event.getEntity();

        // Only server side.
        if (!(entity.level()
                instanceof ServerLevel level)) {

            return;
        }

        // Search for the pedestal that owns this boss.
        //
        // We'll improve this later so we don't need
        // to search like this.

        int radius = 64;

        for (int x = -radius; x <= radius; x++) {

            for (int y = -radius; y <= radius; y++) {

                for (int z = -radius; z <= radius; z++) {

                    var pos =
                            entity.blockPosition()
                                    .offset(x, y, z);

                    if (!(level.getBlockEntity(pos)
                            instanceof PedestalBlockEntity pedestal)) {

                        continue;
                    }

                    if (pedestal.getBossUUID() == null) {
                        continue;
                    }

                    if (!pedestal.getBossUUID()
                            .equals(entity.getUUID())) {

                        continue;
                    }

                    // THIS is the boss belonging
                    // to THIS pedestal.

                    pedestal.bossDefeated();

                    return;
                }
            }
        }
    }
}