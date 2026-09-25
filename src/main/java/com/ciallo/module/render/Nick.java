package com.ciallo.module.render;

import com.ciallo.module.Category;
import com.ciallo.module.Module;
import com.ciallo.setting.BooleanSetting;
import com.ciallo.setting.TextSetting;
import com.ciallo.util.MC;
import net.minecraft.network.chat.Component;

/**
 * Fake name module, ported from the original mod's {@code Nick}.
 * Replaces the local player's own name with a custom nickname in chat, name tags,
 * the tab list, signs, the scoreboard and death messages.
 */
public class Nick extends Module {
    public static Nick INSTANCE;

    private final TextSetting nickName = this.m28(new TextSetting("Name", "HUD_User", "Nickname that replaces your real name"));
    private final BooleanSetting replaceInChat = this.m28(new BooleanSetting("Chat", true));
    private final BooleanSetting replaceInNametags = this.m28(new BooleanSetting("Nametags", true));
    private final BooleanSetting replaceInTabList = this.m28(new BooleanSetting("TabList", true));
    private final BooleanSetting replaceInSigns = this.m28(new BooleanSetting("Signs", true));
    private final BooleanSetting replaceInDeathMessages = this.m28(new BooleanSetting("DeathMessages", true));
    private final BooleanSetting replaceInScoreboard = this.m28(new BooleanSetting("Scoreboard", true));

    public Nick() {
        super("Nick", "Replaces your own name with a nickname.", Category.RENDER);
        this.setChinese("假名");
        this.setChineseDescription("把聊天、标签、告示牌等处的自己的名字替换为假名");
        INSTANCE = this;
    }

    public String getNickName() {
        return nickName.getValue();
    }

    public String getRealName() {
        return MC.getMc().player != null ? MC.getMc().player.getName().getString() : "";
    }

    public boolean shouldReplaceInChat() {
        return replaceInChat.getValue();
    }

    public boolean shouldReplaceInNametags() {
        return replaceInNametags.getValue();
    }

    public boolean shouldReplaceInTabList() {
        return replaceInTabList.getValue();
    }

    public boolean shouldReplaceInSigns() {
        return replaceInSigns.getValue();
    }

    public boolean shouldReplaceInDeathMessages() {
        return replaceInDeathMessages.getValue();
    }

    public boolean shouldReplaceInScoreboard() {
        return replaceInScoreboard.getValue();
    }

    /** Replaces the local player's real name with the nickname inside a plain string. */
    public String replaceName(String text) {
        if (!isEnabled() || text == null || MC.getMc().player == null) return text;
        String realName = MC.getMc().player.getName().getString();
        if (realName == null || realName.isEmpty()) return text;
        String nick = nickName.getValue();
        if (nick == null || nick.isEmpty() || realName.equals(nick)) return text;
        return text.replace(realName, nick);
    }

    /** Component variant, keeping the component's own text if nothing is replaced. */
    public Component replaceComponent(Component text) {
        if (text == null) return null;
        if (!isEnabled()) return text;
        String original = text.getString();
        String processed = replaceName(original);
        if (original.equals(processed)) return text;
        return Component.literal(processed).setStyle(text.getStyle());
    }
}
