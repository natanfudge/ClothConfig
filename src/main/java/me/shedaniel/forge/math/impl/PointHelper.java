package me.shedaniel.forge.math.impl;

import me.shedaniel.forge.math.api.Point;
import net.minecraft.client.Minecraft;

public class PointHelper {
    public static Point fromMouse() {
        Minecraft client = Minecraft.getInstance();
        double mx = client.mouseHelper.getMouseX() * (double) client.func_228018_at_().getScaledWidth() / (double) client.func_228018_at_().getWidth();
        double my = client.mouseHelper.getMouseY() * (double) client.func_228018_at_().getScaledHeight() / (double) client.func_228018_at_().getHeight();
        return new Point(mx, my);
    }
    
    public static int getMouseX() {
        return fromMouse().x;
    }
    
    public static int getMouseY() {
        return fromMouse().y;
    }
}
