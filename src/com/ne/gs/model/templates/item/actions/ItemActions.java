/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemActions")
public class ItemActions {

    @XmlElements({@XmlElement(name = "skilllearn", type = SkillLearnAction.class),
                  @XmlElement(name = "extract", type = ExtractAction.class),
                  @XmlElement(name = "skilluse", type = SkillUseAction.class),
                  @XmlElement(name = "enchant", type = EnchantItemAction.class),
                  @XmlElement(name = "queststart", type = QuestStartAction.class),
                  @XmlElement(name = "dye", type = DyeAction.class),
                  @XmlElement(name = "craftlearn", type = CraftLearnAction.class),
                  @XmlElement(name = "toypetspawn", type = ToyPetSpawnAction.class),
                  @XmlElement(name = "decompose", type = DecomposeAction.class),
                  @XmlElement(name = "titleadd", type = TitleAddAction.class),
                  @XmlElement(name = "learnemotion", type = EmotionLearnAction.class),
                  @XmlElement(name = "read", type = ReadAction.class),
                  @XmlElement(name = "fireworkact", type = FireworksUseAction.class),
                  @XmlElement(name = "instancetimeclear", type = InstanceTimeClear.class),
                  @XmlElement(name = "expandinventory", type = ExpandInventoryAction.class),
                  @XmlElement(name = "animation", type = AnimationAddAction.class),
                  @XmlElement(name = "cosmetic", type = CosmeticItemAction.class),
                  @XmlElement(name = "charge", type = ChargeAction.class),
                  @XmlElement(name = "ride", type = RideAction.class),
                  @XmlElement(name = "houseobject", type = SummonHouseObjectAction.class),
                  @XmlElement(name = "housedeco", type = DecorateAction.class),
                  @XmlElement(name = "assemble", type = AssemblyItemAction.class)
    })

    protected List<AbstractItemAction> itemActions;

    /**
     * Gets the value of the itemActions property. Objects of the following type(s) are allowed in the list
     * {@link SkillLearnAction } {@link SkillUseAction }
     */
    public List<AbstractItemAction> getItemActions() {
        if (itemActions == null) {
            itemActions = new ArrayList<>();
        }
        return this.itemActions;
    }

    public List<ToyPetSpawnAction> getToyPetSpawnActions() {
        List<ToyPetSpawnAction> result = new ArrayList<>();
        if (itemActions == null) {
            return result;
        }

        for (AbstractItemAction action : itemActions) {
            if (action instanceof ToyPetSpawnAction) {
                result.add((ToyPetSpawnAction) action);
            }
        }
        return result;
    }

    public EnchantItemAction getEnchantAction() {
        if (itemActions == null) {
            return null;
        }
        for (AbstractItemAction action : itemActions) {
            if ((action instanceof EnchantItemAction)) {
                return (EnchantItemAction) action;
            }
        }
        return null;
    }

    public SummonHouseObjectAction getHouseObjectAction() {
        if (itemActions == null) {
            return null;
        }
        for (AbstractItemAction action : itemActions) {
            if ((action instanceof SummonHouseObjectAction)) {
                return (SummonHouseObjectAction) action;
            }
        }
        return null;
    }

    public CraftLearnAction getCraftLearnAction() {
        if (itemActions == null) {
            return null;
        }
        for (AbstractItemAction action : itemActions) {
            if ((action instanceof CraftLearnAction)) {
                return (CraftLearnAction) action;
            }
        }
        return null;
    }

    public DecorateAction getDecorateAction() {
        if (itemActions == null) {
            return null;
        }
        for (AbstractItemAction action : itemActions) {
            if ((action instanceof DecorateAction)) {
                return (DecorateAction) action;
            }
        }
        return null;
    }
}
