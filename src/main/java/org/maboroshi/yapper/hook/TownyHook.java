package org.maboroshi.yapper.hook;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class TownyHook {

    public static boolean isVisibleTo(Player sender, Player recipient, String channelId) {
        TownyAPI api = TownyAPI.getInstance();
        if (api == null) return false;

        Resident senderResident = api.getResident(sender);
        Resident recipientResident = api.getResident(recipient);
        if (senderResident == null || recipientResident == null) return false;

        return switch (channelId.toLowerCase()) {
            case "towny-town" -> {
                Town town = senderResident.getTownOrNull();
                yield town != null && town.equals(recipientResident.getTownOrNull());
            }
            case "towny-nation" -> {
                Nation nation = senderResident.getNationOrNull();
                yield nation != null && nation.equals(recipientResident.getNationOrNull());
            }
            case "towny-alliance" -> {
                Nation senderNation = senderResident.getNationOrNull();
                Nation recipientNation = recipientResident.getNationOrNull();

                yield senderNation != null
                        && recipientNation != null
                        && (senderNation.equals(recipientNation) || senderNation.hasAlly(recipientNation));
            }
            default -> false;
        };
    }
}
