package com.hirensharma.astralium;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.Level;
import java.util.UUID;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Bridges named development calls to the production SRG names used by the test profile. */
public final class RuntimeMappings {
    private RuntimeMappings() {}

    public static Object call(Object target, String named, String srg, Object... args) {
        return invoke(target.getClass(), target, named, srg, args);
    }

    public static Object staticCall(Class<?> owner, String named, String srg, Object... args) {
        return invoke(owner, null, named, srg, args);
    }

    public static Object staticField(Class<?> owner, String named, String srg) {
        try {
            Field field;
            try {
                field = owner.getDeclaredField(named);
            } catch (NoSuchFieldException ignored) {
                field = owner.getDeclaredField(srg);
            }
            field.setAccessible(true);
            return field.get(null);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Missing mapped field " + named + "/" + srg + " on " + owner.getName(), exception);
        }
    }

    public static Object instanceField(Object target, String named, String srg) {
        try {
            return mappedField(target.getClass(), named, srg).get(target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                "Missing mapped field " + named + "/" + srg + " on " + target.getClass().getName(), exception);
        }
    }

    public static void setInstanceField(Object target, String named, String srg, Object value) {
        try {
            mappedField(target.getClass(), named, srg).set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(
                "Missing mapped field " + named + "/" + srg + " on " + target.getClass().getName(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> ResourceKey<Registry<T>> registryKey(String named, String srg) {
        return (ResourceKey<Registry<T>>) staticField(net.minecraft.core.registries.Registries.class, named, srg);
    }

    public static <T> TagKey<T> tagKey(ResourceKey<? extends Registry<T>> registry, ResourceLocation location) {
        return (TagKey<T>) staticCall(TagKey.class, "create", "m_203882_", registry, location);
    }

    public static BlockState setValue(BlockState state, Property<?> property, Comparable<?> value) {
        return (BlockState) call(state, "setValue", "m_61124_", property, value);
    }

    public static Comparable<?> getValue(BlockState state, Property<?> property) {
        return (Comparable<?>) call(state, "getValue", "m_61143_", property);
    }

    public static boolean isRemoved(ItemEntity entity) {
        return (Boolean) call(entity, "isRemoved", "m_213877_");
    }

    public static UUID uuid(Entity entity) {
        return (UUID) call(entity, "getUUID", "m_20148_");
    }

    public static boolean onGround(Entity entity) {
        return (Boolean) call(entity, "onGround", "m_20096_");
    }

    public static ResourceKey<?> dimension(Level level) {
        return (ResourceKey<?>) call(level, "dimension", "m_46472_");
    }

    public static Level level(Entity entity) {
        return (Level) call(entity, "level", "m_9236_");
    }

    public static ItemStack mainHandItem(Player player) {
        return (ItemStack) call(player, "getMainHandItem", "m_21205_");
    }

    public static AttributeInstance attribute(Player player, Attribute attribute) {
        return (AttributeInstance) call(player, "getAttribute", "m_21051_", attribute);
    }

    public static MutableComponent translatable(String key, Object... arguments) {
        if (arguments.length == 0) {
            return (MutableComponent) staticCall(Component.class, "translatable", "m_237115_", key);
        }
        return (MutableComponent) staticCall(Component.class, "translatable", "m_237110_", key, arguments);
    }

    private static Object invoke(Class<?> owner, Object target, String named, String srg, Object[] args) {
        Method method = find(owner, named, args);
        if (method == null) method = find(owner, srg, args);
        if (method == null) {
            throw new IllegalStateException("Missing mapped method " + named + "/" + srg + " on " + owner.getName());
        }
        try {
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            Throwable cause = exception instanceof InvocationTargetException invocation
                    && invocation.getCause() != null ? invocation.getCause() : exception;
            throw new IllegalStateException("Unable to invoke mapped method " + method, cause);
        }
    }

    private static Method find(Class<?> owner, String name, Object[] args) {
        for (Method method : owner.getMethods()) {
            if (matches(method, name, args)) return method;
        }
        for (Method method : owner.getDeclaredMethods()) {
            if (matches(method, name, args)) return method;
        }
        return null;
    }

    private static Field mappedField(Class<?> owner, String named, String srg) throws NoSuchFieldException {
        for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            for (String candidate : new String[] {named, srg}) {
                try {
                    Field field = current.getDeclaredField(candidate);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                    // Try the other runtime name, then the superclass.
                }
            }
        }
        throw new NoSuchFieldException(named + "/" + srg);
    }

    private static boolean matches(Method method, String name, Object[] args) {
        if (!method.getName().equals(name) || method.getParameterCount() != args.length) return false;
        if (Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass() == Object.class) return false;
        Class<?>[] parameters = method.getParameterTypes();
        for (int index = 0; index < parameters.length; index++) {
            if (args[index] == null) continue;
            Class<?> parameter = boxed(parameters[index]);
            if (!parameter.isAssignableFrom(args[index].getClass())) return false;
        }
        return true;
    }

    private static Class<?> boxed(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }
}
