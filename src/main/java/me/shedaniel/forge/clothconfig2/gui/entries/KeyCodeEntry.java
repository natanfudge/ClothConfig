package me.shedaniel.forge.clothconfig2.gui.entries;

import com.google.common.collect.Lists;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.settings.KeyModifier;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KeyCodeEntry extends TooltipListEntry<InputMappings.Input> {
    
    private InputMappings.Input value;
    private Widget buttonWidget, resetButton;
    private Consumer<InputMappings.Input> saveConsumer;
    private Supplier<InputMappings.Input> defaultValue;
    private List<IGuiEventListener> widgets;
    private boolean allowMouse = true, allowKey = true;
    
    @Deprecated
    public KeyCodeEntry(String fieldName, InputMappings.Input value, String resetButtonKey, Supplier<InputMappings.Input> defaultValue, Consumer<InputMappings.Input> saveConsumer, Supplier<Optional<String[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        this.defaultValue = defaultValue;
        this.value = value;
        this.buttonWidget = new Button(0, 0, 150, 20, "", widget -> {
            getScreen().focusedBinding = this;
            getScreen().setEdited(true, isRequiresRestart());
        }) {};
        this.resetButton = new Button(0, 0, Minecraft.getInstance().fontRenderer.getStringWidth(I18n.format(resetButtonKey)) + 6, 20, I18n.format(resetButtonKey), widget -> {
            KeyCodeEntry.this.value = getDefaultValue().get();
            getScreen().focusedBinding = null;
            getScreen().setEdited(true, isRequiresRestart());
        });
        this.saveConsumer = saveConsumer;
        this.widgets = Lists.newArrayList(buttonWidget, resetButton);
    }
    
    public boolean isAllowKey() {
        return allowKey;
    }
    
    public void setAllowKey(boolean allowKey) {
        this.allowKey = allowKey;
    }
    
    public boolean isAllowMouse() {
        return allowMouse;
    }
    
    public void setAllowMouse(boolean allowMouse) {
        this.allowMouse = allowMouse;
    }
    
    @Override
    public void save() {
        if (saveConsumer != null)
            saveConsumer.accept(getValue());
    }
    
    @Override
    public InputMappings.Input getValue() {
        return value;
    }
    
    public void setValue(InputMappings.Input value) {
        this.value = value;
    }
    
    @Override
    public Optional<InputMappings.Input> getDefaultValue() {
        return Optional.ofNullable(defaultValue).map(Supplier::get);
    }
    
    private String getLocalizedName() {
        return KeyModifier.NONE.getLocalizedComboName(this.value, () -> {
            String s = this.value.getTranslationKey();
            int i = this.value.getKeyCode();
            String s1 = null;
            switch (this.value.getType()) {
                case KEYSYM:
                    s1 = InputMappings.func_216507_a(i);
                    break;
                case SCANCODE:
                    s1 = InputMappings.func_216502_b(i);
                    break;
                case MOUSE:
                    String s2 = I18n.format(s);
                    s1 = Objects.equals(s2, s) ? I18n.format(InputMappings.Type.MOUSE.func_216500_a(), i + 1) : s2;
            }
            
            return s1 == null ? I18n.format(s) : s1;
        });
    }
    
    @Override
    public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        MainWindow window = Minecraft.getInstance().func_228018_at_();
        this.resetButton.active = isEditable() && getDefaultValue().isPresent() && !getDefaultValue().get().equals(value);
        this.resetButton.y = y;
        this.buttonWidget.active = isEditable();
        this.buttonWidget.y = y;
        this.buttonWidget.setMessage(getLocalizedName());
        if (getScreen().focusedBinding == this)
            this.buttonWidget.setMessage(TextFormatting.WHITE + "> " + TextFormatting.YELLOW + this.buttonWidget.getMessage() + TextFormatting.WHITE + " <");
        if (Minecraft.getInstance().fontRenderer.getBidiFlag()) {
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(I18n.format(getFieldName()), window.getScaledWidth() - x - Minecraft.getInstance().fontRenderer.getStringWidth(I18n.format(getFieldName())), y + 5, 16777215);
            this.resetButton.x = x;
            this.buttonWidget.x = x + resetButton.getWidth() + 2;
            this.buttonWidget.setWidth(150 - resetButton.getWidth() - 2);
        } else {
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(I18n.format(getFieldName()), x, y + 5, getPreferredTextColor());
            this.resetButton.x = x + entryWidth - resetButton.getWidth();
            this.buttonWidget.x = x + entryWidth - 150;
            this.buttonWidget.setWidth(150 - resetButton.getWidth() - 2);
        }
        resetButton.render(mouseX, mouseY, delta);
        buttonWidget.render(mouseX, mouseY, delta);
    }
    
    @Override
    public List<? extends IGuiEventListener> children() {
        return widgets;
    }
    
}
