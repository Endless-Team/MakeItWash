package com.sushidays.systems;

import com.sushidays.entities.Upgrade;
import com.sushidays.entities.Upgrade.UpgradeType;

import java.util.ArrayList;
import java.util.List;

public class UpgradeRegistry {

    private static final List<Upgrade> ALL = new ArrayList<>();

    static {
        // --- Upgrade funzionali ---
        ALL.add(new Upgrade("knife_better",   "Coltello Affilato",
                "+15% velocità taglio", 150, UpgradeType.FUNCTIONAL, 1));
        ALL.add(new Upgrade("knife_pro",      "Coltello da Chef",
                "+30% velocità taglio (si aggiunge al precedente)", 350, UpgradeType.FUNCTIONAL, 5));
        ALL.add(new Upgrade("extra_cooking",  "Piano Cottura Extra",
                "Prepara 2 piatti contemporaneamente", 300, UpgradeType.FUNCTIONAL, 3));
        ALL.add(new Upgrade("deluxe_tray",    "Vassoio Deluxe",
                "Consegna 2 piatti in un'azione sola", 200, UpgradeType.FUNCTIONAL, 4));
        ALL.add(new Upgrade("bell",           "Campanella Dorata",
                "+5 secondi pazienza per ogni cliente", 180, UpgradeType.FUNCTIONAL, 2));
        ALL.add(new Upgrade("speed_roll",     "Stuoia Professionale",
                "Step ROLL completato più facilmente", 220, UpgradeType.FUNCTIONAL, 6));
        ALL.add(new Upgrade("timer_eye",      "Occhio del Tempo",
                "Il timer dello step è più visibile", 120, UpgradeType.FUNCTIONAL, 1));

        // --- Cosmetici ristorante ---
        ALL.add(new Upgrade("theme_sakura",   "Tema Sakura",
                "Decorazioni primaverili con fiori di ciliegio", 80, UpgradeType.COSMETIC, 1));
        ALL.add(new Upgrade("theme_winter",   "Tema Invernale",
                "Decorazioni invernali con neve", 80, UpgradeType.COSMETIC, 1));
        ALL.add(new Upgrade("theme_night",    "Tema Notturno",
                "Luci soffuse per una cena romantica", 100, UpgradeType.COSMETIC, 5));
        ALL.add(new Upgrade("theme_festival", "Tema Matsuri",
                "Lanterne e decorazioni festival giapponese", 120, UpgradeType.COSMETIC, 8));

        // --- Cosmetici chef ---
        ALL.add(new Upgrade("outfit_blue",    "Divisa Blu",
                "Uniforme blu elegante per il tuo chef", 60, UpgradeType.COSMETIC, 1));
        ALL.add(new Upgrade("outfit_red",     "Divisa Rossa",
                "Uniforme rossa per un chef di carattere", 60, UpgradeType.COSMETIC, 1));
        ALL.add(new Upgrade("hat_star",       "Cappello Stellato",
                "Un cappello da chef con stelline dorate", 45, UpgradeType.COSMETIC, 1));
        ALL.add(new Upgrade("hat_panda",      "Cappello Panda",
                "Per i fan della dolcezza!", 55, UpgradeType.COSMETIC, 3));
    }

    public static List<Upgrade> getFunctional() {
        List<Upgrade> r = new ArrayList<>();
        for (Upgrade u : ALL) if (u.type == UpgradeType.FUNCTIONAL) r.add(u);
        return r;
    }

    public static List<Upgrade> getCosmetics() {
        List<Upgrade> r = new ArrayList<>();
        for (Upgrade u : ALL) if (u.type == UpgradeType.COSMETIC) r.add(u);
        return r;
    }

    public static Upgrade getById(String id) {
        for (Upgrade u : ALL) if (u.id.equals(id)) return u;
        return null;
    }
}
