package org.maboroshi.yapper.hook;

import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.GameChatMessagePreProcessEvent;
import org.bukkit.entity.Player;
import org.maboroshi.yapper.Yapper;
import org.maboroshi.yapper.util.Log;

public class DiscordSRVHook {
    private final Yapper plugin;

    public DiscordSRVHook(Yapper plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onDiscordChat(GameChatMessagePreProcessEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        String channelId = plugin.getSessionManager().getCurrentMessageChannel(player);
        Log.debug("DiscordSRV processing message for channel context: " + channelId);

        event.setChannel(channelId);
        plugin.getSessionManager().clearCurrentMessageChannel(player);
    }
}
