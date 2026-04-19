package net.ncm;

import java.util.HashMap;
import java.util.Map;

public class QuestRegistry {

    public record QuestData(String title, String description, long reward, int maxProgress) {}

    private static final Map<String, QuestData> QUESTS = new HashMap<>();

    static {
        QUESTS.put("start", new QuestData(
                "Начало пути",
                "Откройте свой первый базовый кейс",
                1000,
                1
        ));

        QUESTS.put("rich", new QuestData(
                "Магнат",
                "Накопите состояние и купите 10 премиум кейсов",
                15000,
                10
        ));
    }

    public static QuestData getQuest(String id) {
        return QUESTS.get(id);
    }
}