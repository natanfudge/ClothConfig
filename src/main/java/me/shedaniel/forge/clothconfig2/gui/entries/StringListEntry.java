package me.shedaniel.forge.clothconfig2.gui.entries;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringListEntry extends TextFieldListEntry<String> {
    
    private Consumer<String> saveConsumer;
    
    @Deprecated
    public StringListEntry(String fieldName, String value, String resetButtonKey, Supplier<String> defaultValue, Consumer<String> saveConsumer, Supplier<Optional<String[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, value, resetButtonKey, defaultValue, tooltipSupplier, requiresRestart);
        this.saveConsumer = saveConsumer;
    }
    
    @Override
    public String getValue() {
        return textFieldWidget.getText();
    }
    
    @Override
    public void save() {
        if (saveConsumer != null)
            saveConsumer.accept(getValue());
    }
    
    @Override
    protected boolean isMatchDefault(String text) {
        return getDefaultValue().isPresent() ? text.equals(getDefaultValue().get()) : false;
    }
    
}
