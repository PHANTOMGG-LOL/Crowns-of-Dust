package com.phantom.cod.registry;

import com.phantom.cod.CrownsOfDust;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    // ==================================================
    // Sound Event Registry
    // ==================================================

    // This registry is responsible for registering all
    // custom sound events used by the mod.
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(
                    Registries.SOUND_EVENT,
                    CrownsOfDust.MOD_ID
            );


    // ==================================================
    // All Seeing Eye
    // ==================================================

    // The idle sound event for the All Seeing Eye.
    public static final DeferredHolder<SoundEvent, SoundEvent> ALL_SEEING_EYE_IDLE =
            SOUND_EVENTS.register(
                    "all_seeing_eye.idle",
                    SoundEvent::createVariableRangeEvent
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> ALL_SEEING_EYE_WALK =
            SOUND_EVENTS.register(
                    "all_seeing_eye.walk",
                    SoundEvent::createVariableRangeEvent
            );


    // ==================================================
    // King's Will
    // ==================================================

    // The idle sound event for King's Will.
    public static final DeferredHolder<SoundEvent, SoundEvent> KINGS_WILL_IDLE =
            SOUND_EVENTS.register(
                    "kings_will.idle",
                    SoundEvent::createVariableRangeEvent
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> KINGS_WILL_WALK =
            SOUND_EVENTS.register(
                    "kings_will.walk",
                    SoundEvent::createVariableRangeEvent
            );


    // ==================================================
    // Oathbound
    // ==================================================

    // The idle sound event for the Oathbound.
    public static final DeferredHolder<SoundEvent, SoundEvent> OATHBOUND_IDLE =
            SOUND_EVENTS.register(
                    "oathbound.idle",
                    SoundEvent::createVariableRangeEvent
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> OATHBOUND_WALK =
            SOUND_EVENTS.register(
                    "oathbound.walk",
                    SoundEvent::createVariableRangeEvent
            );


    // ==================================================
    // Last Monarch
    // ==================================================

    // The idle sound event for the Last Monarch.
    public static final DeferredHolder<SoundEvent, SoundEvent> LAST_MONARCH_IDLE =
            SOUND_EVENTS.register(
                    "last_monarch.idle",
                    SoundEvent::createVariableRangeEvent
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> LAST_MONARCH_WALK =
            SOUND_EVENTS.register(
                    "last_monarch.walk",
                    SoundEvent::createVariableRangeEvent
            );



    // ==================================================
    // Boss Awakening
    // ==================================================

    public static final DeferredHolder<SoundEvent, SoundEvent> BOSS_AWAKENING =
            SOUND_EVENTS.register(
                    "boss_awakening",
                    SoundEvent::createVariableRangeEvent
            );

    // ==================================================
    // Choice Room Whisper
    // ==================================================

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISPER =
            SOUND_EVENTS.register(
                    "whisper",
                    SoundEvent::createVariableRangeEvent
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISPER2 =
            SOUND_EVENTS.register(
                    "whisper2",
                    SoundEvent::createVariableRangeEvent
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISPER3 =
            SOUND_EVENTS.register(
                    "whisper3",
                    SoundEvent::createVariableRangeEvent
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> WHISPER4 =
            SOUND_EVENTS.register(
                    "whisper4",
                    SoundEvent::createVariableRangeEvent
            );

    // --------------------------------------------------
    // Walking Footsteps
    // --------------------------------------------------

    public static final DeferredHolder<SoundEvent, SoundEvent> ENCOUNTER_WALKING =
            SOUND_EVENTS.register(
                    "walking",
                    SoundEvent::createVariableRangeEvent
            );

    // --------------------------------------------------
    // Running Footsteps
    // --------------------------------------------------

    public static final DeferredHolder<SoundEvent, SoundEvent> ENCOUNTER_RUNNING =
            SOUND_EVENTS.register(
                    "running",
                    SoundEvent::createVariableRangeEvent
            );


    // --------------------------------------------------
    // Heartbeat
    // --------------------------------------------------

    public static final DeferredHolder<SoundEvent, SoundEvent> HEART_BEAT =
            SOUND_EVENTS.register(
                    "heart_beat",
                    SoundEvent::createVariableRangeEvent
            );

    // ==================================================
    // Registration
    // ==================================================

    // Register all custom sound events to the NeoForge
    // mod event bus.
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}