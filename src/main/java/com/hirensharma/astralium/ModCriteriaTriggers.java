package com.hirensharma.astralium;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ModCriteriaTriggers {
    public static final Trigger SHIFTING_EXCAVATION = new Trigger("shifting_excavation");
    public static final Trigger PERFECT_RHYTHM = new Trigger("perfect_rhythm");
    public static final Trigger VOIDBOUND = new Trigger("voidbound");
    public static final Trigger FULL_ARMOUR = new Trigger("full_armour");
    public static final Trigger STARLIT_SOIL = new Trigger("starlit_soil");

    private ModCriteriaTriggers() {}

    public static void register() {
        CriteriaTriggers.register(SHIFTING_EXCAVATION);
        CriteriaTriggers.register(PERFECT_RHYTHM);
        CriteriaTriggers.register(VOIDBOUND);
        CriteriaTriggers.register(FULL_ARMOUR);
        CriteriaTriggers.register(STARLIT_SOIL);
    }

    public static final class Trigger extends SimpleCriterionTrigger<AbstractCriterionTriggerInstance> {
        private final ResourceLocation id;

        private Trigger(String path) {
            id = new ResourceLocation(AstraliumMod.MOD_ID, path);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        protected AbstractCriterionTriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
            return new AbstractCriterionTriggerInstance(id, player) {};
        }

        public void trigger(ServerPlayer player) {
            trigger(player, instance -> true);
        }
    }
}
