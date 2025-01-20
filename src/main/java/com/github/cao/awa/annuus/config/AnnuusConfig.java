package com.github.cao.awa.annuus.config;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.github.cao.awa.annuus.config.key.AnnuusConfigKey;
import com.github.cao.awa.sinuatum.manipulate.Manipulate;
import com.github.cao.awa.sinuatum.util.collection.CollectionFactor;
import com.github.cao.awa.sinuatum.util.io.IOUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class AnnuusConfig {
    private static final Logger LOGGER = LogManager.getLogger("AnnuusConfig");
    private static final File CONFIG_FILE = new File("config/annuus.json");
    public static final AnnuusConfigKey<Boolean> ZZZ_COMPRESS_ENABLED = AnnuusConfigKey.create("zzz_compress_enabled", false);

    private final JSONObject config = new JSONObject();

    public boolean isZzzCompressEnabled() {
        return getConfig(ZZZ_COMPRESS_ENABLED);
    }

    public <X> void setConfig(AnnuusConfigKey<X> configKey, X value) {
        this.config.put(configKey.name(), configKey.checkLimits(checkOrThrow(configKey, value)));
    }

    public <X> void setConfig(AnnuusConfigKey<X> configKey, JSONObject json) {
        this.config.put(configKey.name(), configKey.checkLimits(checkOrThrow(configKey, json.get(configKey.name()))));
    }

    public <X> X getConfig(@NotNull AnnuusConfigKey<X> configKey) {
        Object value = this.config.get(configKey.name());
        if (value == null) {
            return configKey.defaultValue();
        }
        return checkOrThrow(configKey, value);
    }

    @NotNull
    private static <X> X checkOrThrow(@NotNull AnnuusConfigKey<X> configKey, Object value) {
        if (value == null) {
            throw new NullPointerException("Config value should not be null");
        }
        if (configKey.type().isInstance(value) || configKey.type().isAssignableFrom(value.getClass())) {
            return Manipulate.cast(value);
        }
        throw new IllegalArgumentException("Config '" + configKey.name() + "' required '" + configKey.type() + "' but got '" + value.getClass() + "'");
    }

    public void load() {
        loadAsDefault();

        try {
            final JSONObject config = JSONObject.parse(IOUtil.read(new FileReader(CONFIG_FILE, StandardCharsets.UTF_8)));

            setConfig(ZZZ_COMPRESS_ENABLED, config);
        } catch (Exception e) {
            LOGGER.warn("Config not found, use default values", e);
        }

        write();
    }

    public void write() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            IOUtil.write(
                    new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8),
                    this.config.toString(JSONWriter.Feature.PrettyFormat)
            );
        } catch (Exception e) {
            LOGGER.warn("Failed to save config", e);
        }
    }

    public void loadAsDefault() {
        setConfig(ZZZ_COMPRESS_ENABLED, ZZZ_COMPRESS_ENABLED.defaultValue());
    }

    public void copyFrom(@NotNull AnnuusConfig config) {
        setConfig(ZZZ_COMPRESS_ENABLED, config.isZzzCompressEnabled());
    }

    public void print() {
        if (isZzzCompressEnabled()) {
            LOGGER.info("Annuus is enabled zzz compress");
        }
    }

    public Set<AnnuusConfigKey<?>> collectEnabled() {
        Set<AnnuusConfigKey<?>> enabled = CollectionFactor.hashSet();

        if (isZzzCompressEnabled()) {
            enabled.add(ZZZ_COMPRESS_ENABLED);
        }

        return enabled;
    }
}
