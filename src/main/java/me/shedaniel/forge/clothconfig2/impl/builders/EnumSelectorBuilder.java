package me.shedaniel.forge.clothconfig2.impl.builders;

import me.shedaniel.forge.clothconfig2.gui.entries.SelectionListEntry;
import me.shedaniel.forge.clothconfig2.gui.entries.SelectionListEntry.Translatable;
import net.minecraft.client.resources.I18n;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnumSelectorBuilder<T extends Enum<?>> extends FieldBuilder<T, SelectionListEntry<T>> {
    
    private Consumer<T> saveConsumer = null;
    private Function<T, Optional<String[]>> tooltipSupplier = e -> Optional.empty();
    private T value;
    private Class<T> clazz;
    private Function<Enum, String> enumNameProvider = t -> I18n.format(t instanceof Translatable ? ((Translatable) t).getKey() : t.toString());
    
    public EnumSelectorBuilder(String resetButtonKey, String fieldNameKey, Class<T> clazz, T value) {
        super(resetButtonKey, fieldNameKey);
        this.value = Objects.requireNonNull(value);
        this.clazz = Objects.requireNonNull(clazz);
    }
    
    public EnumSelectorBuilder<T> setErrorSupplier(Function<T, Optional<String>> errorSupplier) {
        this.errorSupplier = errorSupplier;
        return this;
    }
    
    public EnumSelectorBuilder<T> requireRestart() {
        requireRestart(true);
        return this;
    }
    
    public EnumSelectorBuilder setSaveConsumer(Consumer<T> saveConsumer) {
        this.saveConsumer = saveConsumer;
        return this;
    }
    
    public EnumSelectorBuilder setDefaultValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
    
    public EnumSelectorBuilder setDefaultValue(T defaultValue) {
        Objects.requireNonNull(defaultValue);
        this.defaultValue = () -> defaultValue;
        return this;
    }
    
    public EnumSelectorBuilder setTooltipSupplier(Function<T, Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = tooltipSupplier;
        return this;
    }
    
    public EnumSelectorBuilder setTooltipSupplier(Supplier<Optional<String[]>> tooltipSupplier) {
        this.tooltipSupplier = e -> tooltipSupplier.get();
        return this;
    }
    
    public EnumSelectorBuilder setTooltip(Optional<String[]> tooltip) {
        this.tooltipSupplier = e -> tooltip;
        return this;
    }
    
    public EnumSelectorBuilder setTooltip(String... tooltip) {
        this.tooltipSupplier = e -> Optional.ofNullable(tooltip);
        return this;
    }
    
    public EnumSelectorBuilder setEnumNameProvider(Function<Enum, String> enumNameProvider) {
        Objects.requireNonNull(enumNameProvider);
        this.enumNameProvider = enumNameProvider;
        return this;
    }
    
    @Override
    public SelectionListEntry<T> build() {
        SelectionListEntry<T> entry = new SelectionListEntry<>(getFieldNameKey(), clazz.getEnumConstants(), value, getResetButtonKey(), defaultValue, saveConsumer, v -> enumNameProvider.apply(v), null, isRequireRestart());
        entry.setTooltipSupplier(() -> tooltipSupplier.apply(entry.getValue()));
        if (errorSupplier != null)
            entry.setErrorSupplier(() -> errorSupplier.apply(entry.getValue()));
        return entry;
    }
    
}