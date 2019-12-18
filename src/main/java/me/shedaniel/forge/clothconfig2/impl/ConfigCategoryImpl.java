package me.shedaniel.forge.clothconfig2.impl;

import com.mojang.datafixers.util.Pair;
import me.shedaniel.forge.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.forge.clothconfig2.api.ConfigCategory;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigCategoryImpl implements ConfigCategory {
    
    private Supplier<List<Pair<String, Object>>> listSupplier;
    private Consumer<ResourceLocation> backgroundConsumer;
    private Runnable destroyCategory;
    private String categoryKey;
    
    ConfigCategoryImpl(String categoryKey, Consumer<ResourceLocation> backgroundConsumer, Supplier<List<Pair<String, Object>>> listSupplier, Runnable destroyCategory) {
        this.listSupplier = listSupplier;
        this.backgroundConsumer = backgroundConsumer;
        this.categoryKey = categoryKey;
        this.destroyCategory = destroyCategory;
    }
    
    @Override
    public String getCategoryKey() {
        return categoryKey;
    }
    
    @Override
    public List<Object> getEntries() {
        return listSupplier.get().stream().map(Pair::getSecond).collect(Collectors.toList());
    }
    
    @Override
    public ConfigCategory addEntry(AbstractConfigListEntry entry) {
        listSupplier.get().add(new Pair<>(entry.getFieldName(), entry));
        return this;
    }
    
    @Override
    public ConfigCategory setCategoryBackground(ResourceLocation identifier) {
        backgroundConsumer.accept(identifier);
        return this;
    }
    
    @Override
    public void removeCategory() {
        destroyCategory.run();
    }
    
}
