package com.github.cao.awa.annuus.config.key;

import com.github.cao.awa.apricot.util.collection.ApricotCollectionFactor;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public record AnnuusConfigKey<T>(String name, Consumer<T> callback, Class<T> type, T defaultValue, List<T> limits) {
    public static <X> AnnuusConfigKey<X> create(String name, X defaultValue) {
        return new AnnuusConfigKey<>(name, (x) -> {
        }, Manipulate.cast(defaultValue.getClass()), defaultValue, ApricotCollectionFactor.arrayList());
    }

    public static <X> AnnuusConfigKey<X> create(String name, Consumer<X> callback, X defaultValue) {
        return new AnnuusConfigKey<>(name, callback, Manipulate.cast(defaultValue.getClass()), defaultValue, ApricotCollectionFactor.arrayList());
    }

    @SafeVarargs
    public static <X> AnnuusConfigKey<X> create(String name, Consumer<X> callback, X defaultValue, X... limits) {
        return new AnnuusConfigKey<>(name, callback, Manipulate.cast(defaultValue.getClass()), defaultValue, ApricotCollectionFactor.arrayList(limits));
    }

    public static <X> AnnuusConfigKey<X> create(String name, Consumer<X> callback, X defaultValue, Collection<X> limits) {
        return new AnnuusConfigKey<>(name, callback, Manipulate.cast(defaultValue.getClass()), defaultValue, ApricotCollectionFactor.arrayList(limits));
    }

    @SafeVarargs
    public final AnnuusConfigKey<T> withLimits(T... limits) {
        this.limits.clear();
        this.limits.addAll(ApricotCollectionFactor.arrayList(limits));
        return this;
    }

    public T getValue(JsonPrimitive primary) {
        T value;

        if (this.type.isAssignableFrom(String.class) && primary.isString()) {
            value = this.type.cast(primary.getAsString());
        } else if (this.type.isAssignableFrom(Boolean.class) && primary.isBoolean()) {
            value = this.type.cast(primary.getAsBoolean());
        } else if (this.type.isAssignableFrom(Integer.class) && primary.isNumber()) {
            value = this.type.cast(primary.getAsInt());
        } else  {
            throw new IllegalStateException("Unexpected config value type, annuus only supported to [String, Boolean, Integer] and this config key '" + this.name + "' required '" + this.type.getName() + "'");
        }
        if (this.limits.isEmpty() || this.limits.contains(value)) {
            this.callback.accept(value);
            return value;
        }
        throw new IllegalStateException("Unexpected config value '" + value + "', the config key '" + this.name + "' only allow these values: " + this.limits);
    }

    public JsonElement check(T value) {
        JsonElement result = switch (value) {
            case String string -> new JsonPrimitive(string);
            case Boolean bool -> new JsonPrimitive(bool);
            case Integer integer -> new JsonPrimitive(integer);
            case null, default -> throw new IllegalStateException("Unexpected config value type, annuus only supported to [String, Boolean, Integer]");
        };

        if (this.limits.isEmpty() || this.limits.contains(value)) {
            this.callback.accept(value);
            return result;
        }
        throw new IllegalStateException("Unexpected config value '" + value + "', the config '" + this.name + "' only allow these values: " + this.limits);
    }
}
