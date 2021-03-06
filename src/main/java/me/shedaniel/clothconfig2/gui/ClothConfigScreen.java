package me.shedaniel.clothconfig2.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.QueuedTooltip;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.clothconfig2.gui.entries.KeyCodeEntry;
import me.shedaniel.clothconfig2.gui.widget.DynamicElementListWidget;
import me.shedaniel.math.api.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public abstract class ClothConfigScreen extends Screen {
    
    private static final Identifier CONFIG_TEX = new Identifier("cloth-config2", "textures/gui/cloth_config.png");
    private final List<QueuedTooltip> queuedTooltips = Lists.newArrayList();
    public KeyCodeEntry focusedBinding;
    public int nextTabIndex;
    public int selectedTabIndex;
    public double tabsScrollVelocity = 0d;
    public double tabsScrollProgress = 0d;
    public ListWidget<AbstractConfigEntry<AbstractConfigEntry>> listWidget;
    private Screen parent;
    private LinkedHashMap<String, List<AbstractConfigEntry>> tabbedEntries;
    private List<Pair<String, Integer>> tabs;
    private boolean edited;
    private boolean requiresRestart;
    private boolean confirmSave;
    private AbstractButtonWidget buttonQuit, buttonSave, buttonLeftTab, buttonRightTab;
    private Rectangle tabsBounds, tabsLeftBounds, tabsRightBounds;
    private String title;
    private double tabsMaximumScrolled = -1d;
    private boolean displayErrors;
    private List<ClothConfigTabButton> tabButtons;
    private boolean smoothScrollingTabs = true;
    private boolean smoothScrollingList = true;
    private Identifier defaultBackgroundLocation;
    private Map<String, Identifier> categoryBackgroundLocation;
    private boolean transparentBackground = false;
    private boolean editable = true;
    @Nullable private String defaultFallbackCategory = null;
    private boolean alwaysShowTabs = false;
    
    @Deprecated
    public ClothConfigScreen(Screen parent, String title, Map<String, List<Pair<String, Object>>> o, boolean confirmSave, boolean displayErrors, boolean smoothScrollingList, Identifier defaultBackgroundLocation, Map<String, Identifier> categoryBackgroundLocation) {
        super(new LiteralText(""));
        this.parent = parent;
        this.title = title;
        this.tabbedEntries = Maps.newLinkedHashMap();
        this.smoothScrollingList = smoothScrollingList;
        this.defaultBackgroundLocation = defaultBackgroundLocation;
        o.forEach((tab, pairs) -> {
            List<AbstractConfigEntry> list = Lists.newArrayList();
            for(Pair<String, Object> pair : pairs) {
                if (pair.getRight() instanceof AbstractConfigListEntry) {
                    list.add((AbstractConfigListEntry) pair.getRight());
                } else {
                    throw new IllegalArgumentException("Unsupported Type (" + pair.getLeft() + "): " + pair.getRight().getClass().getSimpleName());
                }
            }
            list.forEach(entry -> entry.setScreen(this));
            tabbedEntries.put(tab, list);
        });
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        this.tabs = tabbedEntries.keySet().stream().map(s -> new Pair<>(s, textRenderer.getStringWidth(I18n.translate(s)) + 8)).collect(Collectors.toList());
        this.nextTabIndex = 0;
        this.selectedTabIndex = 0;
        for(int i = 0; i < tabs.size(); i++) {
            Pair<String, Integer> pair = tabs.get(i);
            if (pair.getLeft().equals(getFallbackCategory())) {
                this.nextTabIndex = i;
                this.selectedTabIndex = i;
                break;
            }
        }
        this.confirmSave = confirmSave;
        this.edited = false;
        this.requiresRestart = false;
        this.tabsScrollProgress = 0d;
        this.tabButtons = Lists.newArrayList();
        this.displayErrors = displayErrors;
        this.categoryBackgroundLocation = categoryBackgroundLocation;
    }
    
    public boolean isShowingTabs() {
        return isAlwaysShowTabs() || tabs.size() > 1;
    }
    
    public boolean isAlwaysShowTabs() {
        return alwaysShowTabs;
    }
    
    @Deprecated
    public void setAlwaysShowTabs(boolean alwaysShowTabs) {
        this.alwaysShowTabs = alwaysShowTabs;
    }
    
    public boolean isTransparentBackground() {
        return transparentBackground && MinecraftClient.getInstance().world != null;
    }
    
    @Deprecated
    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }
    
    public String getFallbackCategory() {
        if (defaultFallbackCategory != null)
            return defaultFallbackCategory;
        return tabs.get(0).getLeft();
    }
    
    @Deprecated
    public void setFallbackCategory(@Nullable String defaultFallbackCategory) {
        this.defaultFallbackCategory = defaultFallbackCategory;
    }
    
    @Override
    public void tick() {
        super.tick();
        for(Element child : children())
            if (child instanceof Tickable)
                ((Tickable) child).tick();
    }
    
    public Identifier getBackgroundLocation() {
        if (categoryBackgroundLocation.containsKey(Lists.newArrayList(tabbedEntries.keySet()).get(selectedTabIndex)))
            return categoryBackgroundLocation.get(Lists.newArrayList(tabbedEntries.keySet()).get(selectedTabIndex));
        return defaultBackgroundLocation;
    }
    
    public boolean isSmoothScrollingList() {
        return smoothScrollingList;
    }
    
    @Deprecated
    public void setSmoothScrollingList(boolean smoothScrollingList) {
        this.smoothScrollingList = smoothScrollingList;
    }
    
    public boolean isSmoothScrollingTabs() {
        return smoothScrollingTabs;
    }
    
    @Deprecated
    public void setSmoothScrollingTabs(boolean smoothScrolling) {
        this.smoothScrollingTabs = smoothScrolling;
    }
    
    public boolean isEdited() {
        return edited;
    }
    
    @Deprecated
    public void setEdited(boolean edited) {
        this.edited = edited;
        buttonQuit.setMessage(edited ? I18n.translate("text.cloth-config.cancel_discard") : I18n.translate("gui.cancel"));
        buttonSave.active = edited;
    }
    
    public void setEdited(boolean edited, boolean requiresRestart) {
        setEdited(edited);
        if (!this.requiresRestart && requiresRestart)
            this.requiresRestart = requiresRestart;
    }
    
    public void saveAll(boolean openOtherScreens) {
        for(List<AbstractConfigEntry> entries : Lists.newArrayList(tabbedEntries.values()))
            for(AbstractConfigEntry entry : entries)
                entry.save();
        save();
        if (openOtherScreens) {
            if (requiresRestart)
                ClothConfigScreen.this.minecraft.openScreen(new ClothRequiresRestartScreen(parent));
            else
                ClothConfigScreen.this.minecraft.openScreen(parent);
        }
    }
    
    @Override
    protected void init() {
        super.init();
        this.children.clear();
        this.tabButtons.clear();
        if (listWidget != null)
            tabbedEntries.put(tabs.get(selectedTabIndex).getLeft(), (List) listWidget.children());
        selectedTabIndex = nextTabIndex;
        children.add(listWidget = new ListWidget(minecraft, width, height, isShowingTabs() ? 70 : 30, height - 32, getBackgroundLocation()));
        listWidget.setSmoothScrolling(this.smoothScrollingList);
        if (tabbedEntries.size() > selectedTabIndex)
            Lists.newArrayList(tabbedEntries.values()).get(selectedTabIndex).forEach(entry -> listWidget.children().add(entry));
        addButton(buttonQuit = new ButtonWidget(width / 2 - 154, height - 26, 150, 20, edited ? I18n.translate("text.cloth-config.cancel_discard") : I18n.translate("gui.cancel"), widget -> {
            if (confirmSave && edited)
                minecraft.openScreen(new ConfirmScreen(new QuitSaveConsumer(), new TranslatableText("text.cloth-config.quit_config"), new TranslatableText("text.cloth-config.quit_config_sure"), I18n.translate("text.cloth-config.quit_discard"), I18n.translate("gui.cancel")));
            else
                minecraft.openScreen(parent);
        }));
        addButton(buttonSave = new AbstractPressableButtonWidget(width / 2 + 4, height - 26, 150, 20, "") {
            @Override
            public void onPress() {
                saveAll(true);
            }
            
            @Override
            public void render(int int_1, int int_2, float float_1) {
                boolean hasErrors = false;
                if (displayErrors)
                    for(List<AbstractConfigEntry> entries : Lists.newArrayList(tabbedEntries.values())) {
                        for(AbstractConfigEntry entry : entries)
                            if (entry.getConfigError().isPresent()) {
                                hasErrors = true;
                                break;
                            }
                        if (hasErrors)
                            break;
                    }
                active = edited && !hasErrors;
                setMessage(displayErrors && hasErrors ? I18n.translate("text.cloth-config.error_cannot_save") : I18n.translate("text.cloth-config.save_and_done"));
                super.render(int_1, int_2, float_1);
            }
        });
        buttonSave.active = edited;
        if (isShowingTabs()) {
            tabsBounds = new Rectangle(0, 41, width, 24);
            tabsLeftBounds = new Rectangle(0, 41, 18, 24);
            tabsRightBounds = new Rectangle(width - 18, 41, 18, 24);
            children.add(buttonLeftTab = new AbstractPressableButtonWidget(4, 44, 12, 18, "") {
                @Override
                public void onPress() {
                    tabsScrollProgress = Integer.MIN_VALUE;
                    tabsScrollVelocity = 0d;
                    clampTabsScrolled();
                }
                
                @Override
                public void renderButton(int int_1, int int_2, float float_1) {
                    minecraft.getTextureManager().bindTexture(CONFIG_TEX);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                    int int_3 = this.getYImage(this.isHovered());
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(770, 771, 0, 1);
                    RenderSystem.blendFunc(770, 771);
                    this.blit(x, y, 12, 18 * int_3, width, height);
                }
            });
            int j = 0;
            for(Pair<String, Integer> tab : tabs) {
                tabButtons.add(new ClothConfigTabButton(this, j, -100, 43, tab.getRight(), 20, I18n.translate(tab.getLeft())));
                j++;
            }
            tabButtons.forEach(children::add);
            children.add(buttonRightTab = new AbstractPressableButtonWidget(width - 16, 44, 12, 18, "") {
                @Override
                public void onPress() {
                    tabsScrollProgress = Integer.MAX_VALUE;
                    tabsScrollVelocity = 0d;
                    clampTabsScrolled();
                }
                
                @Override
                public void renderButton(int int_1, int int_2, float float_1) {
                    minecraft.getTextureManager().bindTexture(CONFIG_TEX);
                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
                    int int_3 = this.getYImage(this.isHovered());
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(770, 771, 0, 1);
                    RenderSystem.blendFunc(770, 771);
                    this.blit(x, y, 0, 18 * int_3, width, height);
                }
            });
        } else {
            tabsBounds = tabsLeftBounds = tabsRightBounds = new Rectangle();
        }
    }
    
    @Override
    public boolean mouseScrolled(double double_1, double double_2, double double_3) {
        if (tabsBounds.contains(double_1, double_2) && !tabsLeftBounds.contains(double_1, double_2) && !tabsRightBounds.contains(double_1, double_2) && double_3 != 0d) {
            if (double_3 < 0)
                tabsScrollVelocity += 16;
            if (double_3 > 0)
                tabsScrollVelocity -= 16;
            return true;
        }
        return super.mouseScrolled(double_1, double_2, double_3);
    }
    
    public double getTabsMaximumScrolled() {
        if (tabsMaximumScrolled == -1d) {
            AtomicDouble d = new AtomicDouble();
            tabs.forEach(pair -> d.addAndGet(pair.getRight() + 2));
            tabsMaximumScrolled = d.get();
        }
        return tabsMaximumScrolled + 8;
    }
    
    public void resetTabsMaximumScrolled() {
        tabsMaximumScrolled = -1d;
        tabsScrollVelocity = 0f;
    }
    
    public void clampTabsScrolled() {
        int xx = 0;
        for(ClothConfigTabButton tabButton : tabButtons)
            xx += tabButton.getWidth() + 2;
        if (xx > width - 40)
            tabsScrollProgress = MathHelper.clamp(tabsScrollProgress, 0, getTabsMaximumScrolled() - width + 40);
        else
            tabsScrollProgress = 0d;
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
        if (isShowingTabs()) {
            if (smoothScrollingTabs) {
                double change = tabsScrollVelocity * 0.2f;
                if (change != 0) {
                    if (change > 0 && change < .2)
                        change = .2;
                    else if (change < 0 && change > -.2)
                        change = -.2;
                    tabsScrollProgress += change;
                    tabsScrollVelocity -= change;
                    if (change > 0 == tabsScrollVelocity < 0)
                        tabsScrollVelocity = 0f;
                    clampTabsScrolled();
                }
            } else {
                tabsScrollProgress += tabsScrollVelocity;
                tabsScrollVelocity = 0d;
                clampTabsScrolled();
            }
            int xx = 24 - (int) tabsScrollProgress;
            for(ClothConfigTabButton tabButton : tabButtons) {
                tabButton.x = xx;
                xx += tabButton.getWidth() + 2;
            }
            buttonLeftTab.active = tabsScrollProgress > 0d;
            buttonRightTab.active = tabsScrollProgress < getTabsMaximumScrolled() - width + 40;
        }
        if (isTransparentBackground()) {
            fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            renderDirtBackground(0);
        }
        listWidget.render(int_1, int_2, float_1);
        ScissorsHandler.INSTANCE.scissor(new Rectangle(listWidget.left, listWidget.top, listWidget.width, listWidget.bottom - listWidget.top));
        for(AbstractConfigEntry child : listWidget.children())
            child.lateRender(int_1, int_2, float_1);
        ScissorsHandler.INSTANCE.removeLastScissor();
        if (isShowingTabs()) {
            drawCenteredString(minecraft.textRenderer, title, width / 2, 18, -1);
            Rectangle onlyInnerTabBounds = new Rectangle(tabsBounds.x + 20, tabsBounds.y, tabsBounds.width - 40, tabsBounds.height);
            ScissorsHandler.INSTANCE.scissor(onlyInnerTabBounds);
            if (isTransparentBackground())
                fillGradient(onlyInnerTabBounds.x, onlyInnerTabBounds.y, onlyInnerTabBounds.getMaxX(), onlyInnerTabBounds.getMaxY(), 0x68000000, 0x68000000);
            else
                overlayBackground(onlyInnerTabBounds, 32, 32, 32, 255, 255);
            tabButtons.forEach(widget -> widget.render(int_1, int_2, float_1));
            drawTabsShades(0, isTransparentBackground() ? 120 : 255);
            ScissorsHandler.INSTANCE.removeLastScissor();
            buttonLeftTab.render(int_1, int_2, float_1);
            buttonRightTab.render(int_1, int_2, float_1);
        } else
            drawCenteredString(minecraft.textRenderer, title, width / 2, 12, -1);
        
        if (displayErrors && isEditable()) {
            List<String> errors = Lists.newArrayList();
            for(List<AbstractConfigEntry> entries : Lists.newArrayList(tabbedEntries.values()))
                for(AbstractConfigEntry entry : entries)
                    if (entry.getConfigError().isPresent())
                        errors.add(((Optional<String>) entry.getConfigError()).get());
            if (errors.size() > 0) {
                minecraft.getTextureManager().bindTexture(CONFIG_TEX);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                String text = "§c" + (errors.size() == 1 ? errors.get(0) : I18n.translate("text.cloth-config.multi_error"));
                if (isTransparentBackground()) {
                    int stringWidth = minecraft.textRenderer.getStringWidth(text);
                    fillGradient(8, 9, 20 + stringWidth, 14 + minecraft.textRenderer.fontHeight, 0x68000000, 0x68000000);
                }
                blit(10, 10, 0, 54, 3, 11);
                drawString(minecraft.textRenderer, text, 18, 12, -1);
            }
        } else if (!isEditable()) {
            minecraft.getTextureManager().bindTexture(CONFIG_TEX);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            String text = "§c" + I18n.translate("text.cloth-config.not_editable");
            if (isTransparentBackground()) {
                int stringWidth = minecraft.textRenderer.getStringWidth(text);
                fillGradient(8, 9, 20 + stringWidth, 14 + minecraft.textRenderer.fontHeight, 0x68000000, 0x68000000);
            }
            blit(10, 10, 0, 54, 3, 11);
            drawString(minecraft.textRenderer, text, 18, 12, -1);
        }
        super.render(int_1, int_2, float_1);
        queuedTooltips.forEach(queuedTooltip -> renderTooltip(queuedTooltip.getText(), queuedTooltip.getX(), queuedTooltip.getY()));
        queuedTooltips.clear();
    }
    
    public void queueTooltip(QueuedTooltip queuedTooltip) {
        queuedTooltips.add(queuedTooltip);
    }
    
    private void drawTabsShades(int lightColor, int darkColor) {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 0, 1);
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(tabsBounds.getMinX() + 20, tabsBounds.getMinY() + 4, 0.0D).texture(0, 1f).color(0, 0, 0, lightColor).next();
        buffer.vertex(tabsBounds.getMaxX() - 20, tabsBounds.getMinY() + 4, 0.0D).texture(1f, 1f).color(0, 0, 0, lightColor).next();
        buffer.vertex(tabsBounds.getMaxX() - 20, tabsBounds.getMinY(), 0.0D).texture(1f, 0).color(0, 0, 0, darkColor).next();
        buffer.vertex(tabsBounds.getMinX() + 20, tabsBounds.getMinY(), 0.0D).texture(0, 0).color(0, 0, 0, darkColor).next();
        tessellator.draw();
        buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(tabsBounds.getMinX() + 20, tabsBounds.getMaxY(), 0.0D).texture(0, 1f).color(0, 0, 0, darkColor).next();
        buffer.vertex(tabsBounds.getMaxX() - 20, tabsBounds.getMaxY(), 0.0D).texture(1f, 1f).color(0, 0, 0, darkColor).next();
        buffer.vertex(tabsBounds.getMaxX() - 20, tabsBounds.getMaxY() - 4, 0.0D).texture(1f, 0).color(0, 0, 0, lightColor).next();
        buffer.vertex(tabsBounds.getMinX() + 20, tabsBounds.getMaxY() - 4, 0.0D).texture(0, 0).color(0, 0, 0, lightColor).next();
        tessellator.draw();
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }
    
    @SuppressWarnings("deprecation")
    protected void overlayBackground(Rectangle rect, int red, int green, int blue, int startAlpha, int endAlpha) {
        if (isTransparentBackground())
            return;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        minecraft.getTextureManager().bindTexture(getBackgroundLocation());
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;
        buffer.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(rect.getMinX(), rect.getMaxY(), 0.0D).texture(rect.getMinX() / 32.0F, rect.getMaxY() / 32.0F).color(red, green, blue, endAlpha).next();
        buffer.vertex(rect.getMaxX(), rect.getMaxY(), 0.0D).texture(rect.getMaxX() / 32.0F, rect.getMaxY() / 32.0F).color(red, green, blue, endAlpha).next();
        buffer.vertex(rect.getMaxX(), rect.getMinY(), 0.0D).texture(rect.getMaxX() / 32.0F, rect.getMinY() / 32.0F).color(red, green, blue, startAlpha).next();
        buffer.vertex(rect.getMinX(), rect.getMinY(), 0.0D).texture(rect.getMinX() / 32.0F, rect.getMinY() / 32.0F).color(red, green, blue, startAlpha).next();
        tessellator.draw();
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (this.focusedBinding != null) {
            if (focusedBinding.isAllowMouse())
                focusedBinding.setValue(InputUtil.Type.MOUSE.createFromCode(int_1));
            else
                focusedBinding.setValue(InputUtil.UNKNOWN_KEYCODE);
            this.focusedBinding = null;
            return true;
        } else {
            return super.mouseClicked(double_1, double_2, int_1);
        }
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.focusedBinding != null) {
            if (int_1 == 256 || !focusedBinding.isAllowKey()) {
                focusedBinding.setValue(InputUtil.UNKNOWN_KEYCODE);
            } else {
                focusedBinding.setValue(InputUtil.getKeyCode(int_1, int_2));
            }
            
            this.focusedBinding = null;
            return true;
        }
        if (int_1 == 256 && this.shouldCloseOnEsc()) {
            if (confirmSave && edited)
                minecraft.openScreen(new ConfirmScreen(new QuitSaveConsumer(), new TranslatableText("text.cloth-config.quit_config"), new TranslatableText("text.cloth-config.quit_config_sure"), I18n.translate("text.cloth-config.quit_discard"), I18n.translate("gui.cancel")));
            else
                minecraft.openScreen(parent);
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    public void save() {
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    @Deprecated
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    private class QuitSaveConsumer implements BooleanConsumer {
        @Override
        public void accept(boolean t) {
            if (!t)
                minecraft.openScreen(ClothConfigScreen.this);
            else
                minecraft.openScreen(parent);
            return;
        }
    }
    
    public class ListWidget<R extends DynamicElementListWidget.ElementEntry<R>> extends DynamicElementListWidget<R> {
        
        public ListWidget(MinecraftClient client, int width, int height, int top, int bottom, Identifier backgroundLocation) {
            super(client, width, height, top, bottom, backgroundLocation);
            visible = false;
        }
        
        @Override
        public int getItemWidth() {
            return width - 80;
        }
        
        public ClothConfigScreen getScreen() {
            return ClothConfigScreen.this;
        }
        
        @Override
        protected int getScrollbarPosition() {
            return width - 36;
        }
        
        protected final void clearStuff() {
            this.clearItems();
        }
        
        @Override
        public boolean mouseClicked(double double_1, double double_2, int int_1) {
            this.updateScrollingState(double_1, double_2, int_1);
            if (!this.isMouseOver(double_1, double_2)) {
                return false;
            } else {
                for(R entry : children()) {
                    if (entry.mouseClicked(double_1, double_2, int_1)) {
                        this.setFocused(entry);
                        this.setDragging(true);
                        return true;
                    }
                }
                if (int_1 == 0) {
                    this.clickedHeader((int) (double_1 - (double) (this.left + this.width / 2 - this.getItemWidth() / 2)), (int) (double_2 - (double) this.top) + (int) this.getScroll() - 4);
                    return true;
                }
                
                return this.scrolling;
            }
        }
        
        @Override
        protected void renderBackBackground(BufferBuilder buffer, Tessellator tessellator) {
            if (!isTransparentBackground())
                super.renderBackBackground(buffer, tessellator);
            else {
                fillGradient(left, top, right, bottom, 0x68000000, 0x68000000);
            }
        }
        
        @Override
        protected void renderHoleBackground(int int_1, int int_2, int int_3, int int_4) {
            if (!isTransparentBackground())
                super.renderHoleBackground(int_1, int_2, int_3, int_4);
        }
    }
    
}
