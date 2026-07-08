package org.maboroshi.yapper.hook;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class TownyHook {
    private static final TownyAPI API = TownyAPI.getInstance();

    public static boolean isVisibleTo(Player sender, Player recipient, String channelId) {
        if (API == null) return false;

        Resident senderResident = API.getResident(sender);
        Resident recipientResident = API.getResident(recipient);

        if (senderResident == null || recipientResident == null) return false;

        return switch (channelId.toLowerCase()) {
            case "towny-town" -> {
                Town senderTown = senderResident.getTownOrNull();
                yield senderTown != null && senderTown.equals(recipientResident.getTownOrNull());
            }
            case "towny-nation" -> {
                Nation senderNation = senderResident.getNationOrNull();
                yield senderNation != null && senderNation.equals(recipientResident.getNationOrNull());
            }
            case "towny-alliance" -> {
                Nation senderAllianceNation = senderResident.getNationOrNull();
                Nation recipientNation = recipientResident.getNationOrNull();

                yield senderAllianceNation != null
                        && recipientNation != null
                        && (senderAllianceNation.equals(recipientNation)
                                || senderAllianceNation.hasAlly(recipientNation));
            }
            default -> false;
        };
    }
}
