package com.hirensharma.astralium;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class ModCriteriaTriggers {
    public static final Trigger SHIFTING_EXCAVATION = new Trigger("shifting_excavation");
    public static final Trigger PERFECT_RHYTHM = new Trigger("perfect_rhythm");
    public static final Trigger VOIDBOUND = new Trigger("voidbound");
    public static final Trigger FULL_ARMOUR = new Trigger("full_armour");
    public static final Trigger STARLIT_SOIL = new Trigger("starlit_soil");

    private ModCriteriaTriggers() {}

    public static void register() {
        registerTrigger(SHIFTING_EXCAVATION);
        registerTrigger(PERFECT_RHYTHM);
        registerTrigger(VOIDBOUND);
        registerTrigger(FULL_ARMOUR);
        registerTrigger(STARLIT_SOIL);
    }

    private static void registerTrigger(Trigger trigger) {
        try {
            Method register;
            try {
                register = CriteriaTriggers.class.getDeclaredMethod("register", CriterionTrigger.class);
            } catch (NoSuchMethodException ignored) {
                register = CriteriaTriggers.class.getDeclaredMethod("m_10595_", CriterionTrigger.class);
            }
            register.setAccessible(true);
            register.invoke(null, trigger);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to register Astralium criterion trigger " + trigger.getId(), exception);
        }
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
