package net.vulkanmod.render.vertex;

import net.minecraft.client.renderer.RenderType;
import net.vulkanmod.Initializer;
import net.vulkanmod.interfaces.ExtendedRenderType;

import java.util.EnumSet;
import java.util.function.Function;

public enum TerrainRenderType {
    SOLID,
    CUTOUT,
    CUTOUT_MIPPED,
    TRANSLUCENT,
    TRIPWIRE;

    public static final TerrainRenderType[] VALUES = TerrainRenderType.values();

    public static final EnumSet<TerrainRenderType> COMPACT_RENDER_TYPES = EnumSet.of(CUTOUT_MIPPED, TRANSLUCENT);
    public static final EnumSet<TerrainRenderType> SEMI_COMPACT_RENDER_TYPES = EnumSet.of(CUTOUT_MIPPED, CUTOUT, TRANSLUCENT);

    private static Function<TerrainRenderType, TerrainRenderType> remapper;

    static {
        SEMI_COMPACT_RENDER_TYPES.add(CUTOUT);
        SEMI_COMPACT_RENDER_TYPES.add(CUTOUT_MIPPED);
        SEMI_COMPACT_RENDER_TYPES.add(TRANSLUCENT);

        COMPACT_RENDER_TYPES.add(CUTOUT_MIPPED);
        COMPACT_RENDER_TYPES.add(TRANSLUCENT);
    }

    TerrainRenderType() {
    }

    public static TerrainRenderType get(RenderType renderType) {
        return ((ExtendedRenderType)renderType).getTerrainRenderType();
    }

    public static TerrainRenderType get(String name) {
        return switch (name) {
            case "solid" -> TerrainRenderType.SOLID;
            case "cutout" -> TerrainRenderType.CUTOUT;
            case "cutout_mipped" -> TerrainRenderType.CUTOUT_MIPPED;
            case "translucent" -> TerrainRenderType.TRANSLUCENT;
            case "tripwire" -> TerrainRenderType.TRIPWIRE;
            default -> null;
        };
    }

    public static RenderType getRenderType(TerrainRenderType renderType) {
        return switch (renderType) {
            case SOLID -> RenderType.solid();
            case CUTOUT -> RenderType.cutout();
            case CUTOUT_MIPPED -> RenderType.cutoutMipped();
            case TRANSLUCENT -> RenderType.translucent();
            case TRIPWIRE -> RenderType.tripwire();
        };
    }

    public static void updateMapping() {
        if (Initializer.CONFIG.uniqueOpaqueLayer) {
            remapper = (renderType) -> switch (renderType) {
                case SOLID, CUTOUT, CUTOUT_MIPPED -> TerrainRenderType.CUTOUT_MIPPED;
                case TRANSLUCENT, TRIPWIRE -> TerrainRenderType.TRANSLUCENT;
            };
        } else {
            remapper = (renderType) -> switch (renderType) {
                case SOLID, CUTOUT_MIPPED -> TerrainRenderType.CUTOUT_MIPPED;
                case CUTOUT -> TerrainRenderType.CUTOUT;
                case TRANSLUCENT, TRIPWIRE -> TerrainRenderType.TRANSLUCENT;
            };
        }
    }

    public static TerrainRenderType getRemapped(TerrainRenderType renderType) {
        return remapper.apply(renderType);
    }
}
