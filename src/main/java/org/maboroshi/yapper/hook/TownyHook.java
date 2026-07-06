package org.maboroshi.yapper.hook;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;

public class TownyHook {
    private static final TownyAPI API = TownyAPI.getInstance();

    public static boolean isVisibleTo(Player sender, Player recipient, String integrationType) {
        if (API == null) return false;

        Resident senderRes = API.getResident(sender);
        Resident recipientRes = API.getResident(recipient);
        if (senderRes == null || recipientRes == null) return true;

        switch (integrationType.toLowerCase()) {
            case "towny-town":
                Town senderTown = senderRes.getTownOrNull();
                if (senderTown == null) return true;
                return !senderTown.equals(recipientRes.getTownOrNull());

            case "towny-nation":
                Nation senderNation = senderRes.getNationOrNull();
                if (senderNation == null) return true;
                return !senderNation.equals(recipientRes.getNationOrNull());

            case "towny-alliance":
                Nation senderAllianceNation = senderRes.getNationOrNull();
                if (senderAllianceNation == null) return true;
                Nation recipientNation = recipientRes.getNationOrNull();
                if (recipientNation == null) return true;
                
                return !senderAllianceNation.equals(recipientNation) && !senderAllianceNation.hasAlly(recipientNation);

            default:
                return false;
        }
    }
}