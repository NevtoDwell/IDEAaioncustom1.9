/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */

package com.ne.gs.modules.pvpevent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import com.ne.commons.utils.Rnd;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.modules.common.Item;
import com.ne.gs.modules.common.PollRegistry;
import com.ne.gs.services.abyss.AbyssPointsService;
import com.ne.gs.services.item.ItemService;

/**
* @author hex1r0
*/
public class PvpRewardQuery extends PollRegistry.Query {
    private final PvpRewardList _reward;
    private final Player _player;

    public PvpRewardQuery(Player player, PvpRewardList reward) {
        _player = player;
        _reward = reward;
    }

    @Override
    public void run() {
        randomItems();
        selectiveItems();

        if (_reward.getAp() > 0) {
            AbyssPointsService.addAp(_player, _reward.getAp());
        }

        if (_reward.getLvl() > 0) {
            _player.getCommonData().setLevel(_player.getLevel() + _reward.getLvl());
        }

        if (_reward.getGp() > 0) {
            InGameShopEn.getInstance().addToll(_player, _reward.getGp());
        }
    }

    void randomItems() {
        PvpItemList randomItems = _reward.getRandomRewardList();
        List<Item> items = new ArrayList<>(randomItems.getItems());
        int limit = Math.min(randomItems.getLimit(), items.size());
        if (limit > 0) {
            while (limit-- > 0) {
                Item item = Rnd.take(items);
                ItemService.addItem(_player, item.getItemId(), item.getCount(), true);
            }
        }
    }

    void selectiveItems() {
        PvpItemList selectiveItems = _reward.getSelectiveRewardList();
        Iterator<Integer> it = getItemIds().iterator();
        for (int i = 0; i < selectiveItems.getLimit(); i++) {
            if (!it.hasNext()) { break; }

            final Integer itemId = it.next();
            Optional<Item> itemOption = Iterables.tryFind(selectiveItems, new Predicate<Item>() {
                @Override
                public boolean apply(Item item) {
                    return item.getItemId() == itemId;
                }
            });

            Item item = itemOption.orNull();
            if (item != null) {
                ItemService.addItem(_player, item.getItemId(), item.getCount(), true);
            }
        }
    }
}
