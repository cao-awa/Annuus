package com.github.cao.awa.annuus.mixin.registry;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RegistryKey.class)
public interface RegistryKeyAccessor {
    @Invoker("of")
    static <T> RegistryKey<T> ofIdentifier(Identifier registry, Identifier value) {
        throw new AssertionError("Assert error");
    }
}
