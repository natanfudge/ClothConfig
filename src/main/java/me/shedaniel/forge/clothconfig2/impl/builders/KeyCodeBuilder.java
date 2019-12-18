package me.shedaniel.forge.clothconfig2.impl.builders;

import me.shedaniel.forge.clothconfig2.gui.entries.KeyCodeEntry;
import net.minecraft.client.util.InputMappings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class KeyCodeBuilder extends FieldBuilder<InputMappings.Input, KeyCodeEntry> {
    
    @Nullable private Consumer<InputMappings.Input> saveConsumer = null;
    @Nonnull private Function<InputMappings.Input, Optional<String[]>> tooltipSupplier = bool -> Optional.empty();
    private InputMappings.Input value;
    private boolean allowKey = true, allowMouse = true;
    
    public KeyCodeBuilder(String resetButtonKey, String fieldNameKey, InputMappings.Input value) {
        super(resetButtonKey, fieldNameKey);
        this.value = value;
    }
    
    public KeyCodeBuilder setAllowKey(boolean allowKey) {
        if (!allowMouse && !allowKey)
            throw new IllegalArgumentException();
        this.allowKey = allowKey;
        return this;
    }
    
    public KeyCodeBuilder setAllowMouse(boolean allowMouse) {
        if (!allowKey && !allowMouse)
            throw new IllegalArgumentException();
        this.allowMouse = allowMouse;
        return this;
    }
    
    public KeyCodeBuilder setErrorSupplier(@Nullable Function<InputMappings.Input, Optional<String>> errorSupplier) {
        this.errorSupplier = errorSupplier;
        return this;
    }
    
    public KeyCodeBuilder requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public KeyCodeBuilder setSaveConsumer(Consumer<InputMappings.Input> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public KeyCodeBuilder setDefaultValue(Supplier<InputMappings.Input> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public KeyCodeBuilder setDefaultValue(InputMappings.Input defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public KeyCodeBuilder setTooltipSupplier(
            @Nonnull Function<InputMappings.Input, Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public KeyCodeBuilder setTooltipSupplier(@Nonnull Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = bool -> tooltipSupplier.get();
        return this;
    }
    
    public KeyCodeBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = bool -> tooltip;
        return this;
    }
    
    public KeyCodeBuilder setTooltip(@Nullable String... tooltip) {
        this.tooltipSupplier = bool -> Optional.ofNullable(tooltip);
        return this;
    }
    
    @Override
    public KeyCodeEntry build() {
        KeyCodeEntry entry = new KeyCodeEntry(getFieldNameKey(), value, getResetButtonKey(), defaultValue, saveConsumer, null, isRequireRestart());
        entry.setTooltipSupplier(() -> tooltipSupplier.apply(entry.getValue()));
        if (errorSupplier != null)
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        entry.setAllowKey(allowKey);
        entry.setAllowMouse(allowMouse);
        return entry;
    }
    
}