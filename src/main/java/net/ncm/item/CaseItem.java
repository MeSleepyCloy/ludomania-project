package net.ncm.item;

import net.minecraft.item.Item;

public class CaseItem extends Item {
    private final String caseId;

    public CaseItem(Settings settings, String caseId) {
        super(settings);
        this.caseId = caseId;
    }

    public String getCaseId() {
        return caseId;
    }
}