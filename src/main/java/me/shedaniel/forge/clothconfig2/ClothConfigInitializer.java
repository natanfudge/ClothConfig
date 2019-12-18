package me.shedaniel.forge.clothconfig2;

import me.shedaniel.forge.clothconfig2.api.ConfigBuilder;
import me.shedaniel.forge.clothconfig2.api.ConfigCategory;
import me.shedaniel.forge.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.forge.clothconfig2.impl.EasingMethod;
import me.shedaniel.forge.clothconfig2.impl.EasingMethod.EasingMethodImpl;
import me.shedaniel.forge.clothconfig2.impl.EasingMethods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Properties;
import java.util.function.BiFunction;

public class ClothConfigInitializer {
    
    public static final Logger LOGGER = LogManager.getFormatterLogger("ClothConfig");
    private static EasingMethod easingMethod = EasingMethodImpl.QUART;
    private static long scrollDuration = 1000;
    private static double scrollStep = 16;
    private static double bounceBackMultiplier = .24;
    
    public ClothConfigInitializer() {
        loadConfig();
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (BiFunction<Minecraft, Screen, Screen>) (client, parent) -> {
            ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle("Cloth Mod Config Config");
            builder.setDefaultBackgroundTexture(new ResourceLocation("minecraft:textures/block/oak_planks.png"));
            ConfigCategory scrolling = builder.getOrCreateCategory("Scrolling");
            ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
            scrolling.addEntry(entryBuilder.startSelector("Easing Method", EasingMethods.getMethods().toArray(new EasingMethod[0]), easingMethod).setDefaultValue(EasingMethodImpl.QUART).setSaveConsumer(o -> easingMethod = (EasingMethod) o).build());
            scrolling.addEntry(entryBuilder.startLongSlider("Scroll Duration", scrollDuration, 0, 5000).setTextGetter(integer -> {
                return integer <= 0 ? "Value: Disabled" : (integer > 1500 ? String.format("Value: %.1fs", integer / 1000f) : "Value: " + integer + "ms");
            }).setDefaultValue(1000).setSaveConsumer(i -> scrollDuration = i).build());
            scrolling.addEntry(entryBuilder.startDoubleField("Scroll Step", scrollStep).setDefaultValue(16).setSaveConsumer(i -> scrollStep = i).build());
            scrolling.addEntry(entryBuilder.startDoubleField("Bounce Multiplier", bounceBackMultiplier).setDefaultValue(0.84).setSaveConsumer(i -> bounceBackMultiplier = i).build());
            builder.setSavingRunnable(() -> {
                saveConfig();
            });
            return builder.build();
        });
    }
    
    public static EasingMethod getEasingMethod() {
        return easingMethod;
    }
    
    public static long getScrollDuration() {
        return scrollDuration;
    }
    
    public static double getScrollStep() {
        return scrollStep;
    }
    
    public static double getBounceBackMultiplier() {
        return bounceBackMultiplier;
    }
    
    private static void loadConfig() {
        File file = new File(FMLPaths.CONFIGDIR.get().toFile(), "cloth-config2/config.properties");
        try {
            file.getParentFile().mkdirs();
            easingMethod = EasingMethodImpl.QUART;
            scrollDuration = 1000;
            scrollStep = 16;
            bounceBackMultiplier = .24;
            if (!file.exists()) {
                saveConfig();
            }
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            String easing = properties.getProperty("easingMethod", "QUART");
            for(EasingMethod value : EasingMethods.getMethods()) {
                if (value.toString().equalsIgnoreCase(easing)) {
                    easingMethod = value;
                    break;
                }
            }
            scrollDuration = Long.parseLong(properties.getProperty("scrollDuration", "1000"));
            scrollStep = Double.parseDouble(properties.getProperty("scrollStep", "16"));
            bounceBackMultiplier = Double.parseDouble(properties.getProperty("bounceBackMultiplier2", "0.24"));
        } catch (Exception e) {
            e.printStackTrace();
            easingMethod = EasingMethodImpl.QUART;
            scrollDuration = 1000;
            scrollStep = 16;
            bounceBackMultiplier = .24;
            try {
                if (file.exists())
                    file.delete();
            } catch (Exception ignored) {
            }
            saveConfig();
        }
    }
    
    private static void saveConfig() {
        File file = new File(FMLPaths.CONFIGDIR.get().toFile(), "cloth-config2/config.properties");
        try {
            FileWriter writer = new FileWriter(file, false);
            Properties properties = new Properties();
            properties.setProperty("easingMethod", easingMethod.toString());
            properties.setProperty("scrollDuration", scrollDuration + "");
            properties.setProperty("scrollStep", scrollStep + "");
            properties.setProperty("bounceBackMultiplier2", bounceBackMultiplier + "");
            properties.store(writer, null);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            easingMethod = EasingMethodImpl.QUART;
            scrollDuration = 1000;
            scrollStep = 16;
            bounceBackMultiplier = .24;
        }
    }
    
}
