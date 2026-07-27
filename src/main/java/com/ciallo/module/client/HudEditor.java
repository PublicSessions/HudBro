package com.ciallo.module.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.ciallo.gui.HudEditorScreen;
import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.ColorSetting;
import com.ciallo.util.MC;

public class HudEditor extends Module {
    public HudEditor() {
        super("HudEditor", "Edits HUD element positions.", Category.CLIENT);
        this.setChinese("HUD编辑器");
        this.setChineseDescription("打开HUD编辑器");
        ColorSetting color = (ColorSetting) this.getColorSetting2();
        if (color != null) {
            color.setColor(-1);
        }
    }

    @Override
    public void onEnable() {
        MC.client3.setScreen(new HudEditorScreen());
        this.setFlag3(false);
    }
}

