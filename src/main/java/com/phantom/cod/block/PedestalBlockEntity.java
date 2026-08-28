package com.phantom.cod.block;

import com.mojang.serialization.MapCodec;
import com.phantom.cod.network.ModNetwork;
import com.phantom.cod.registry.ModBlockEntities;
import com.phantom.cod.registry.ModEntities;
import com.phantom.cod.registry.ModItems;
import com.phantom.cod.temple.TempleStone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public class PedestalBlockEntity extends BlockEntity {

    // ==================================================
    // CORE
    // ==================================================

    private boolean coreInserted = false;


    // ==================================================
    // ACTIVATION
    // ==================================================

    private int activationTicks = 0;


    // ==================================================
    // BOSS
    // ==================================================

    private UUID bossUUID = null;


    // ==================================================
    // COOLDOWN
    // ==================================================

    private long cooldownEnd = 0L;

    private static final long COOLDOWN_DURATION =
            30L * 60L * 20L;


    // ==================================================
    // TEMPLE
    // ==================================================

    private boolean templePedestal = false;

    private TempleStone templeStone =
            TempleStone.NONE;


    // ==================================================
    // CHOICE ROOM WEAPON
    // ==================================================

    public enum ChoiceWeapon {

        NONE,

        DIAMOND_SWORD,

        NETHERITE_SWORD
    }

    private ChoiceWeapon choiceWeapon =
            ChoiceWeapon.NONE;


    // ==================================================
    // CONSTRUCTOR
    // ==================================================

    public PedestalBlockEntity(
            BlockPos pos,
            BlockState state) {

        super(
                ModBlockEntities.PEDESTAL.get(),
                pos,
                state
        );
    }


    // ==================================================
    // TEMPLE PEDESTAL
    // ==================================================

    public boolean isTemplePedestal() {

        if (templePedestal) {
            return true;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        return isInsideStructure(
                serverLevel,
                "temple_of_god"
        );
    }


    public void setTemplePedestal(
            boolean temple) {

        this.templePedestal = temple;

        setChanged();
        sync();
    }


    // ==================================================
    // TEMPLE STONE
    // ==================================================

    public TempleStone getTempleStone() {

        return templeStone;
    }


    public boolean hasTempleStone() {

        return templeStone != TempleStone.NONE;
    }


    public void setTempleStone(
            TempleStone stone) {

        this.templeStone = stone;

        setChanged();
        sync();
    }


    // ==================================================
    // CHOICE ROOM WEAPON
    // ==================================================

    public ChoiceWeapon getChoiceWeapon() {

        return choiceWeapon;
    }


    public boolean hasChoiceWeapon() {

        return choiceWeapon != ChoiceWeapon.NONE;
    }


    public void setChoiceWeapon(
            ChoiceWeapon weapon) {

        this.choiceWeapon = weapon;

        setChanged();
        sync();
    }


    public void clearChoiceWeapon() {

        this.choiceWeapon =
                ChoiceWeapon.NONE;

        setChanged();
        sync();
    }


    // ==================================================
    // GET WEAPON ITEM
    // ==================================================

    public ItemStack getChoiceWeaponStack() {

        return switch (choiceWeapon) {

            case DIAMOND_SWORD ->
                    new ItemStack(
                            ModItems.HEROBRINES_LEGACY.get()
                    );

            case NETHERITE_SWORD ->
                    new ItemStack(
                            ModItems.NULLS_SILENCE.get()
                    );

            case NONE ->
                    ItemStack.EMPTY;
        };
    }


    // ==================================================
    // CORE STATE
    // ==================================================

    public boolean isCoreInserted() {

        return coreInserted;
    }


    public void setCoreInserted(
            boolean inserted) {

        coreInserted = inserted;

        setChanged();
        sync();
    }


    // ==================================================
    // COOLDOWN
    // ==================================================

    public boolean isOnCooldown() {

        if (level == null) {
            return false;
        }

        return cooldownEnd >
                level.getGameTime();
    }


    public long getRemainingCooldown() {

        if (level == null) {
            return 0L;
        }

        return Math.max(
                0L,
                cooldownEnd
                        - level.getGameTime()
        );
    }


    // ==================================================
    // ACTIVATE
    // ==================================================

    public boolean activate() {

        if (coreInserted) {
            return false;
        }

        if (isOnCooldown()) {
            return false;
        }


        // ==================================================
        // HISTORIC PLACE CHECK
        // ==================================================

        if (!isInHistoricPlace()) {
            return false;
        }


        // ==================================================
        // INSERT CORE
        // ==================================================

        coreInserted = true;

        activationTicks = 120;

        setChanged();
        sync();

        startSummoningShake();

        return true;
    }


    // ==================================================
    // TICK
    // ==================================================

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            PedestalBlockEntity pedestal) {

        // Server only.
        if (level.isClientSide()) {
            return;
        }


        // ==================================================
        // ACTIVATION COUNTDOWN
        // ==================================================

        if (pedestal.activationTicks > 0) {

            pedestal.activationTicks--;

            if (pedestal.activationTicks == 0) {

                pedestal.spawnBoss();
            }
        }


        // ==================================================
        // COOLDOWN FINISHED
        // ==================================================

        if (pedestal.cooldownEnd != 0L
                && level.getGameTime()
                >= pedestal.cooldownEnd) {

            pedestal.cooldownEnd = 0L;

            pedestal.setChanged();
            pedestal.sync();
        }
    }


    // ==================================================
    // VALID KINGDOM
    // ==================================================

    public boolean isInHistoricPlace() {

        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        return isInsideStructure(serverLevel, "ashenreach")
                || isInsideStructure(serverLevel, "eldoria")
                || isInsideStructure(serverLevel, "valdren")
                || isInsideStructure(serverLevel, "myrath");
    }


    // ==================================================
    // SPAWN BOSS
    // ==================================================

    private void spawnBoss() {

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Mob boss = null;


        // ==================================================
        // ASHENREACH
        // ==================================================

        if (isInsideStructure(
                serverLevel,
                "ashenreach")) {

            boss = ModEntities.KINGS_WILL.get().spawn(
                    serverLevel,
                    worldPosition.above(),
                    EntitySpawnReason.TRIGGERED
            );
        }


        // ==================================================
        // ELDORIA
        // ==================================================

        else if (isInsideStructure(
                serverLevel,
                "eldoria")) {

            boss = ModEntities.ALL_SEEING_EYE.get().spawn(
                    serverLevel,
                    worldPosition.above(),
                    EntitySpawnReason.TRIGGERED
            );
        }


        // ==================================================
        // VALDREN
        // ==================================================

        else if (isInsideStructure(
                serverLevel,
                "valdren")) {

            boss = ModEntities.OATHBOUND.get().spawn(
                    serverLevel,
                    worldPosition.above(),
                    EntitySpawnReason.TRIGGERED
            );
        }


        // ==================================================
        // MYRATH
        // ==================================================

        else if (isInsideStructure(
                serverLevel,
                "myrath")) {

            boss = ModEntities.LAST_MONARCH.get().spawn(
                    serverLevel,
                    worldPosition.above(),
                    EntitySpawnReason.TRIGGERED
            );
        }


        // ==================================================
        // STORE EXACT BOSS UUID
        // ==================================================

        if (boss != null) {

            bossUUID =
                    boss.getUUID();

            setChanged();
            sync();
        }
    }


    // ==================================================
    // BOSS UUID
    // ==================================================

    public UUID getBossUUID() {

        return bossUUID;
    }


    // ==================================================
    // BOSS DEFEATED
    // ==================================================

    public void bossDefeated() {

        if (bossUUID == null) {
            return;
        }


        // ==================================================
        // RESTORE ENVIRONMENT
        // ==================================================

        if (level instanceof ServerLevel serverLevel) {

            ModNetwork.ScreenShakePayload clearEnvironment =
                    new ModNetwork.ScreenShakePayload(
                            0,
                            0.0F,
                            worldPosition.getX(),
                            worldPosition.getY(),
                            worldPosition.getZ(),
                            1
                    );

            PacketDistributor.sendToPlayersNear(
                    serverLevel,
                    null,
                    worldPosition.getX(),
                    worldPosition.getY(),
                    worldPosition.getZ(),
                    32.0D,
                    clearEnvironment
            );
        }


        // ==================================================
        // CLEAR BOSS
        // ==================================================

        bossUUID = null;


        // ==================================================
        // REMOVE DISPLAYED CORE
        // ==================================================

        coreInserted = false;


        // ==================================================
        // START 30-MINUTE COOLDOWN
        // ==================================================

        cooldownEnd =
                level.getGameTime()
                        + COOLDOWN_DURATION;


        setChanged();

        sync();
    }


    // ==================================================
    // STRUCTURE CHECK
    // ==================================================

    private boolean isInsideStructure(
            ServerLevel level,
            String structureName) {

        var structureKey =
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.STRUCTURE,
                        net.minecraft.resources.Identifier.fromNamespaceAndPath(
                                "cod",
                                structureName
                        )
                );

        var start =
                level.structureManager()
                        .getStructureWithPieceAt(
                                worldPosition,
                                holder ->
                                        holder.is(
                                                structureKey
                                        )
                        );

        return start != null
                && start.isValid();
    }


    // ==================================================
    // SAVE
    // ==================================================

    @Override
    protected void saveAdditional(
            ValueOutput output) {

        super.saveAdditional(output);


        // ==================================================
        // CORE
        // ==================================================

        output.putBoolean(
                "CoreInserted",
                coreInserted
        );


        // ==================================================
        // ACTIVATION
        // ==================================================

        output.putInt(
                "ActivationTicks",
                activationTicks
        );


        // ==================================================
        // COOLDOWN
        // ==================================================

        output.putLong(
                "CooldownEnd",
                cooldownEnd
        );


        // ==================================================
        // BOSS UUID
        // ==================================================

        if (bossUUID != null) {

            output.putString(
                    "BossUUID",
                    bossUUID.toString()
            );
        }


        // ==================================================
        // TEMPLE PEDESTAL
        // ==================================================

        output.putBoolean(
                "TemplePedestal",
                templePedestal
        );


        // ==================================================
        // TEMPLE STONE
        // ==================================================

        output.putString(
                "TempleStone",
                templeStone.name()
        );


        // ==================================================
        // CHOICE ROOM WEAPON
        // ==================================================

        output.putString(
                "ChoiceWeapon",
                choiceWeapon.name()
        );
    }


    // ==================================================
    // LOAD
    // ==================================================

    @Override
    protected void loadAdditional(
            ValueInput input) {

        super.loadAdditional(input);


        // ==================================================
        // CORE
        // ==================================================

        coreInserted =
                input.getBooleanOr(
                        "CoreInserted",
                        false
                );


        // ==================================================
        // ACTIVATION
        // ==================================================

        activationTicks =
                input.getIntOr(
                        "ActivationTicks",
                        0
                );


        // ==================================================
        // COOLDOWN
        // ==================================================

        cooldownEnd =
                input.getLongOr(
                        "CooldownEnd",
                        0L
                );


        // ==================================================
        // BOSS UUID
        // ==================================================

        bossUUID = null;

        String uuid =
                input.getString(
                        "BossUUID"
                ).orElse(null);

        if (uuid != null) {

            try {

                bossUUID =
                        UUID.fromString(uuid);

            } catch (IllegalArgumentException ignored) {
            }
        }


        // ==================================================
        // TEMPLE PEDESTAL
        // ==================================================

        templePedestal =
                input.getBooleanOr(
                        "TemplePedestal",
                        false
                );


        // ==================================================
        // TEMPLE STONE
        // ==================================================

        String stoneName =
                input.getString(
                        "TempleStone"
                ).orElse("NONE");

        try {

            templeStone =
                    TempleStone.valueOf(
                            stoneName
                    );

        } catch (IllegalArgumentException ignored) {

            templeStone =
                    TempleStone.NONE;
        }


        // ==================================================
        // CHOICE ROOM WEAPON
        // ==================================================

        String weaponName =
                input.getString(
                        "ChoiceWeapon"
                ).orElse("NONE");

        try {

            choiceWeapon =
                    ChoiceWeapon.valueOf(
                            weaponName
                    );

        } catch (IllegalArgumentException ignored) {

            choiceWeapon =
                    ChoiceWeapon.NONE;
        }
    }


    // ==================================================
    // CLIENT UPDATE TAG
    // ==================================================

    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider registries) {

        return saveWithoutMetadata(
                registries
        );
    }


    // ==================================================
    // CLIENT UPDATE PACKET
    // ==================================================

    @Override
    public Packet<ClientGamePacketListener>
    getUpdatePacket() {

        return ClientboundBlockEntityDataPacket.create(
                this
        );
    }


    // ==================================================
    // SYNC
    // ==================================================

    private void sync() {

        if (level == null
                || level.isClientSide()) {

            return;
        }

        BlockState state =
                getBlockState();

        level.sendBlockUpdated(
                worldPosition,
                state,
                state,
                Block.UPDATE_ALL
        );
    }


    // ==================================================
    // SUMMONING SHAKE
    // ==================================================

    private void startSummoningShake() {

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ModNetwork.ScreenShakePayload payload =
                new ModNetwork.ScreenShakePayload(
                        120,
                        1.5F,
                        worldPosition.getX(),
                        worldPosition.getY(),
                        worldPosition.getZ(),
                        0
                );

        PacketDistributor.sendToPlayersNear(
                serverLevel,
                null,
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                32.0D,
                payload
        );
    }
}