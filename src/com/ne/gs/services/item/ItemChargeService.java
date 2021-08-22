/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services.item;

import java.util.Collection;
import java.util.Collections;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import com.ne.gs.model.DescId;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.PersistentState;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.items.ChargeInfo;
import com.ne.gs.model.templates.item.Improvement;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.serverpackets.SM_EMOTION;
import com.ne.gs.network.aion.serverpackets.SM_INVENTORY_UPDATE_ITEM;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.services.abyss.AbyssPointsService;

/**
 * @author ATracer
 */
public final class ItemChargeService {

	/**
	 * @return collection of items for conditioning
	 */
	public static Collection<Item> filterItemsToCondition(Player player, Item selectedItem, final int chargeWay) {
		if (selectedItem != null) {
			return Collections.singletonList(selectedItem);
		}
		return Collections2.filter(player.getEquipment().getEquippedItems(), new Predicate<Item>() {

			@Override
			public boolean apply(Item item) {
				return item.getChargeLevelMax() != 0 && item.getImprovement() != null && item.getImprovement().getChargeWay() == chargeWay
						&& (item.getChargePoints() < ChargeInfo.LEVEL2);
			}
		});
	}

	public static void startChargingEquippedItems(final Player player, int senderObj, final int chargeWay) {

		final Collection<Item> filteredItems = filterItemsToCondition(player, null, chargeWay);
		if (filteredItems.isEmpty()) {
			AionServerPacket packet = new SM_SYSTEM_MESSAGE(chargeWay == 1 ? 1400895 : 1401343);
			player.sendPck(packet);
			return;
		}

		final long payAmount = calculatePrice(filteredItems);
		RequestResponseHandler request = new RequestResponseHandler(player) {

			@Override
			public void acceptRequest(Creature requester, Player responder) {
				if (processPayment(player, chargeWay, payAmount)) {
					for (Item item : filteredItems) {
						chargeItem(player, item, item.getChargeLevelMax());
					}
				}
			}

			@Override
			public void denyRequest(Creature requester, Player responder) {
				// Nothing Happens
			}

		};
		int msg = chargeWay == 1 ? 903026 : 904039;
		if (player.getResponseRequester().putRequest(msg, request)) {
			player.sendPck(new SM_QUESTION_WINDOW(msg, senderObj, 0, String.valueOf(payAmount)));
		}
	}

	private static long calculatePrice(Collection<Item> items) {
		long result = 0L;
		for (Item item : items) {
			result += getPayAmountForService(item, item.getChargeLevelMax());
		}
		return result;
	}

	public static void chargeItems(Player player, Collection<Item> items, int level) {
		for (Item item : items) {
			chargeItem(player, item, level);
		}
	}

	public static void chargeItem(Player player, Item item, int level) {
		Improvement improvement = item.getImprovement();
		if (improvement == null) {
			return;
		}
		int chargeWay = improvement.getChargeWay();
		int currentCharge = item.getChargePoints();
		switch (level) {
			case 1:
				item.getConditioningInfo().updateChargePoints(ChargeInfo.LEVEL1 - currentCharge);
				break;
			case 2:
				item.getConditioningInfo().updateChargePoints(ChargeInfo.LEVEL2 - currentCharge);
				break;
		}
		player.sendPck(new SM_INVENTORY_UPDATE_ITEM(player, item));
		player.getEquipment().setPersistentState(PersistentState.UPDATE_REQUIRED);
		player.getInventory().setPersistentState(PersistentState.UPDATE_REQUIRED);
		if (chargeWay == 1) {
			player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE_SUCCESS(DescId.of(item.getNameID()), level));
		} else {
			player.sendPck(SM_SYSTEM_MESSAGE.STR_MSG_ITEM_CHARGE2_SUCCESS(DescId.of(item.getNameID()), level));
		}
		VisibleObject v = player.getTarget();
		if (v != null && v instanceof Creature) {
			player.sendPck(new SM_EMOTION((Creature) v, EmotionType.EMOTE, 145, v.getObjectId()));
		}
		player.getGameStats().updateStatsVisually();
	}

	/**
	 * Pay for conditioning of item
	 */
	public static boolean processPayment(Player player, Item item, int level) {
		return processPayment(player, item.getImprovement().getChargeWay(), getPayAmountForService(item, level));
	}

	public static boolean processPayment(Player player, int chargeWay, long amount) {
		switch (chargeWay) {
			case 1:
				return processKinahPayment(player, amount);
			case 2:
				return processAPPayment(player, amount);
		}
		return false;
	}

	public static boolean processKinahPayment(Player player, long requiredKinah) {
		if (player.getInventory().getKinah() < requiredKinah) {
			return false;
		}
		player.getInventory().decreaseKinah(requiredKinah);
		return true;
	}

	public static boolean processAPPayment(Player player, long requiredAP) {
		if (player.getAbyssRank().getAp() < requiredAP) {
			return false;
		}
		AbyssPointsService.addAp(player, (int) (-requiredAP));
		return true;
	}

	public static long getPayAmountForService(Item item, int chargeLevel) {
		Improvement improvement = item.getImprovement();
		if (improvement == null) {
			return 0;
		}
		switch (chargeLevel) {
			case 1:
				return calculateAmount(item, improvement, chargeLevel);
			case 2:
				int nextChargeLevel = getNextChargeLevel(item);
				switch (nextChargeLevel) {
					case 1:
						return calculateAmount(item, improvement, nextChargeLevel) + improvement.getPrice2();
					case 2:
						return calculateAmount(item, improvement, nextChargeLevel);
				}
		}
		return 0;
	}

	private static int calculateAmount(Item item, Improvement imp, int level) {
		switch (level) {
			case 1: {
				double oneCharge = (double) imp.getPrice1() / ChargeInfo.LEVEL1;
				return (int) Math.ceil(oneCharge * (ChargeInfo.LEVEL1 - item.getChargePoints()) / 2);
			} case 2: {
				double oneCharge = (double) imp.getPrice2() / (ChargeInfo.LEVEL2 - ChargeInfo.LEVEL1);
				return (int) Math.ceil(oneCharge * (ChargeInfo.LEVEL2 - item.getChargePoints()));
			} default:
				return 0;
		}
	}

	private static int getNextChargeLevel(Item item) {
		int charge = item.getChargePoints();
		if (charge < ChargeInfo.LEVEL1) {
			return 1;
		}
		if (charge < ChargeInfo.LEVEL2) {
			return 2;
		}
		throw new IllegalArgumentException("Invalid charge level " + charge);
	}
}
