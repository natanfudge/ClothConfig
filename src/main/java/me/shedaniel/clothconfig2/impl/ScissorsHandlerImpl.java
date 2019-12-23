package me.shedaniel.clothconfig2.impl;

import com.google.common.collect.Lists;
import fudge.notenoughcrashes.api.MinecraftCrashes;
import me.shedaniel.clothconfig2.ClothConfigInitializer;
import me.shedaniel.clothconfig2.api.ScissorsHandler;
import me.shedaniel.math.api.Rectangle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

public final class ScissorsHandlerImpl implements ScissorsHandler {

    @Deprecated public static final ScissorsHandler INSTANCE = new ScissorsHandlerImpl();

    static {
        MinecraftCrashes.onEveryCrash(() -> {
            try {
                ScissorsHandler.INSTANCE.clearScissors();
            } catch (Throwable t) {
                ClothConfigInitializer.LOGGER.error("[ClothConfig] Failed clear scissors on game crash!", t);
            }
        });
    }

    private List<Rectangle> scissorsAreas;

    public ScissorsHandlerImpl() {
        this.scissorsAreas = Lists.newArrayList();
    }

    @Override
    public void clearScissors() {
        scissorsAreas.clear();
        applyScissors();
    }

    @Override
    public List<Rectangle> getScissorsAreas() {
        return Collections.unmodifiableList(scissorsAreas);
    }

    @Override
    public void scissor(Rectangle rectangle) {
        scissorsAreas.add(rectangle);
        applyScissors();
    }

    @Override
    public void removeLastScissor() {
        if (!scissorsAreas.isEmpty())
            scissorsAreas.remove(scissorsAreas.size() - 1);
        applyScissors();
    }

    @Override
    public void applyScissors() {
        if (!scissorsAreas.isEmpty()) {
            Rectangle r = scissorsAreas.get(0).clone();
            scissorsAreas.stream().skip(1L).forEach(rectangle -> r.setBounds(r.intersects(rectangle) ? r.intersection(rectangle) : new Rectangle()));
            Window window = MinecraftClient.getInstance().getWindow();
            double scaleFactor = window.getScaleFactor();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int) (r.x * scaleFactor), (int) ((window.getScaledHeight() - r.height - r.y) * scaleFactor), (int) (r.width * scaleFactor), (int) (r.height * scaleFactor));
        } else
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
}
