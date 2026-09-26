package com.ciallo.module.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import com.ciallo.api.Listener3;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.ColorSetting;
import com.ciallo.setting.NumberSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.util.MC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TntHud extends AbstractHudModule implements Listener3 {
    private static final int LIST_BAR_WIDTH = 50;
    private static final int LIST_BAR_HEIGHT = 8;
    private static final int LIST_BAR_GAP = 5;

    private static final class TntRow {
        final String text;
        final float progress;

        TntRow(String text, float progress) {
            this.text = text;
            this.progress = progress;
        }
    }

    private static TntHud instance;
    private final BooleanSetting shadow = (BooleanSetting) this.m28(new BooleanSetting("Shadow", true));
    private final ColorSetting color = (ColorSetting) this.m28(new ColorSetting("Color", -1184275));
    private final NumberSetting scale = (NumberSetting) this.m28(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.1));
    private final TextSetting format = (TextSetting) this.m28(new TextSetting("Format", "TNT : {Time}s", "Display format"));
    private final BooleanSetting barMode = (BooleanSetting) this.m28(new BooleanSetting("Bar Mode", false));
    private final NumberSetting decimalPlaces = (NumberSetting) this.m28(new NumberSetting("Decimal Places", 1.0, 1.0, 2.0, 1.0));
    private final BooleanSetting listMode = (BooleanSetting) this.m28(new BooleanSetting("List Mode", false));
    private final BooleanSetting showTime = (BooleanSetting) this.m28(new BooleanSetting("Show Time", true));
    private final BooleanSetting showDistance = (BooleanSetting) this.m28(new BooleanSetting("Show Distance", true));
    private final BooleanSetting showCoords = (BooleanSetting) this.m28(new BooleanSetting("Show Coords", true));

    public TntHud() {
        super("TntHud", "Shows TNT countdown.", Category.HUD);
        this.setChinese("TNT计时");
        this.setChineseDescription("显示TNT爆炸倒计时");
        instance = this;
        this.relX.setValue(0.01);
        this.relY.setValue(0.16);
    }

    @Override
    public int getWidth() {
        if (listMode.getValue()) {
            int maxWidth = 0;
            List<TntRow> rows = buildRows();
            for (TntRow row : rows) {
                maxWidth = Math.max(maxWidth, MC.getMc().font.width(row.text));
            }
            if (barMode.getValue()) {
                return Math.round(Math.max(LIST_BAR_WIDTH, LIST_BAR_WIDTH + LIST_BAR_GAP + maxWidth) * scale.getFloat());
            }
            return Math.round(maxWidth * scale.getFloat());
        }
        if (barMode.getValue()) {
            return Math.round(50.0f * scale.getFloat());
        }
        return Math.round(MC.getMc().font.width(format.getValue()) * scale.getFloat());
    }

    @Override
    public int getHeight() {
        if (listMode.getValue()) {
            int count = Math.max(1, buildRows().size());
            return Math.round(count * MC.getMc().font.lineHeight * scale.getFloat());
        }
        if (barMode.getValue()) {
            return Math.round(8.0f * scale.getFloat());
        }
        return Math.round(MC.getMc().font.lineHeight * scale.getFloat());
    }

    @Override
    public void render(GuiGraphics context, float partialTicks) {
        Minecraft client = MC.getMc();
        if (client.level == null || client.player == null) return;

        if (listMode.getValue()) {
            renderList(context);
            return;
        }

        double camX = client.player.getX();
        double camY = client.player.getEyeY();
        double camZ = client.player.getZ();
        float yaw = client.player.getYRot();
        float pitch = client.player.getXRot();

        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        float lookX = (float) (-Math.sin(yawRad) * Math.cos(pitchRad));
        float lookY = (float) (-Math.sin(pitchRad));
        float lookZ = (float) (Math.cos(yawRad) * Math.cos(pitchRad));

        Matrix4f viewMatrix = new Matrix4f().setLookAt(
            (float) camX, (float) camY, (float) camZ,
            (float) (camX + lookX), (float) (camY + lookY), (float) (camZ + lookZ),
            0.0f, 1.0f, 0.0f
        );

        float fov = (float) client.options.fov().get();
        int scaledWidth = client.getWindow().getGuiScaledWidth();
        int scaledHeight = client.getWindow().getGuiScaledHeight();
        float aspect = (float) scaledWidth / (float) scaledHeight;
        Matrix4f projMatrix = new Matrix4f().perspective(
            (float) Math.toRadians(fov),
            aspect,
            0.05f,
            256.0f
        );

        Matrix4f viewProj = new Matrix4f();
        projMatrix.mul(viewMatrix, viewProj);

        boolean bar = barMode.getValue();
        int decimals = (int) Math.round(decimalPlaces.getValue());
        String fmt = decimals == 1 ? "%.1f" : "%.2f";

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity instanceof PrimedTnt tnt && tnt.getFuse() > 0) {
                double timeLeft = tnt.getFuse() / 20.0;
                String text = format.getValue().replace("{Time}", String.format(fmt, timeLeft));

                Vec3 pos = tnt.getEyePosition();
                Vector4f clip = new Vector4f((float) pos.x, (float) pos.y + 0.5f, (float) pos.z, 1.0f);
                clip.mul(viewProj);

                if (clip.w > 0.0f) {
                    int screenX = (int) (scaledWidth / 2.0f * (1.0f + clip.x / clip.w));
                    int screenY = (int) (scaledHeight / 2.0f * (1.0f - clip.y / clip.w));

                    context.pose().pushMatrix();
                    context.pose().translate(screenX, screenY);
                    context.pose().scale(scale.getFloat(), scale.getFloat());

                    if (bar) {
                        int barWidth = 50;
                        int barHeight = 8;
                        float progress = (float) Math.max(0.0, Math.min(1.0, timeLeft / 4.0));
                        context.fill(0, 0, barWidth, barHeight, 0xAA000000);
                        context.fill(1, 1, (int) ((barWidth - 2) * progress) + 1, barHeight - 1, color.getColor());
                    } else {
                        context.drawString(client.font, text, 0, 0, color.getColor(), shadow.getValue());
                    }

                    context.pose().popMatrix();
                }
            }
        }
    }

    private List<TntRow> buildRows() {
        Minecraft client = MC.getMc();
        List<TntRow> rows = new ArrayList<>();
        if (client.level == null || client.player == null) return rows;

        String fmt = decimalPlaces.getValue() == 1.0 ? "%.1f" : "%.2f";
        Vec3 playerEye = client.player.getEyePosition();

        List<PrimedTnt> tnts = new ArrayList<>();
        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity instanceof PrimedTnt tnt && tnt.getFuse() > 0) {
                tnts.add(tnt);
            }
        }
        tnts.sort(Comparator.comparingInt(PrimedTnt::getFuse));

        for (PrimedTnt tnt : tnts) {
            double timeLeft = tnt.getFuse() / 20.0;
            float progress = (float) Math.max(0.0, Math.min(1.0, timeLeft / 4.0));
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            if (showTime.getValue()) {
                sb.append(String.format(fmt, timeLeft)).append("s");
                first = false;
            }
            if (showDistance.getValue()) {
                double dist = tnt.getEyePosition().distanceTo(playerEye);
                if (!first) sb.append(' ');
                sb.append(String.format(fmt, dist)).append("m");
                first = false;
            }
            if (showCoords.getValue()) {
                if (!first) sb.append(' ');
                sb.append("(")
                    .append(String.format(fmt, tnt.getX())).append(", ")
                    .append(String.format(fmt, tnt.getY())).append(", ")
                    .append(String.format(fmt, tnt.getZ())).append(")");
            }
            rows.add(new TntRow(sb.toString(), progress));
        }
        return rows;
    }

    private void renderList(GuiGraphics context) {
        List<TntRow> rows = buildRows();
        context.pose().pushMatrix();
        context.pose().translate(getX(), getY());
        context.pose().scale(scale.getFloat(), scale.getFloat());

        int lineHeight = MC.getMc().font.lineHeight;
        if (barMode.getValue()) {
            int barYOffset = (lineHeight - LIST_BAR_HEIGHT) / 2;
            for (int i = 0; i < rows.size(); i++) {
                int rowY = i * lineHeight;
                TntRow row = rows.get(i);
                context.fill(0, rowY + barYOffset, LIST_BAR_WIDTH, rowY + barYOffset + LIST_BAR_HEIGHT, 0xAA000000);
                int fillWidth = (int) (LIST_BAR_WIDTH * row.progress);
                if (fillWidth > 0) {
                    context.fill(1, rowY + barYOffset + 1, 1 + fillWidth, rowY + barYOffset + LIST_BAR_HEIGHT - 1, color.getColor());
                }
                if (!row.text.isEmpty()) {
                    context.drawString(MC.getMc().font, row.text, LIST_BAR_WIDTH + LIST_BAR_GAP, rowY, color.getColor(), shadow.getValue());
                }
            }
        } else {
            for (int i = 0; i < rows.size(); i++) {
                context.drawString(MC.getMc().font, rows.get(i).text, 0, i * lineHeight, color.getColor(), shadow.getValue());
            }
        }

        context.pose().popMatrix();
    }
}
