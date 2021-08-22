package com.ne.gs.services.custom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ne.gs.database.GDB;
import com.ne.commons.utils.Rnd;
import com.ne.gs.configs.main.CustomConfig;
import com.ne.gs.database.dao.MailDAO;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.LetterType;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.ingameshop.InGameShopEn;
import com.ne.gs.model.templates.onlinebonus.Bonus;
import com.ne.gs.model.templates.onlinebonus.RewardItem;
import com.ne.gs.services.mail.MailService;
import com.ne.gs.services.mail.SystemMailService;
import com.ne.gs.utils.ThreadPoolManager;

/**
 *
 * @author ViAl
 *
 */
public class OnlineBonusService {

    private static final String SENDER = "Bonus Service";
    private static final String TITLE = "Thanks for choosing us!";
    private static final String MESSAGE = "You was rewarded!";
    /**
     * playerObjId, timeInMinutes
     */
    private Map<Integer, Integer> onlineTime;
    /**
     * playerObjId, onlineReward
     */
    private Map<Integer, OnlineReward> rewards;

    private OnlineBonusService() {
        this.onlineTime = new ConcurrentHashMap<>();
        this.rewards = new ConcurrentHashMap<>();
        int lifeTime = CustomConfig.ONLINE_BONUSES_LIFETIME / 2;
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new RewardsUpdateTask(), lifeTime * 1000, lifeTime * 1000);
    }

    public void onLogout(Player player) {
        this.onlineTime.remove(player.getObjectId());
        this.rewards.remove(player.getObjectId());
    }

    public void checkPlayer(Player player) {
        updateOnlineTime(player);
        int playerOnline = onlineTime.get(player.getObjectId());
        if (canReward(player, playerOnline)) {
            reward(player, playerOnline);
        }
    }

    private void updateOnlineTime(Player player) {
        if (onlineTime.containsKey(player.getObjectId())) {
            int playerOnline = onlineTime.get(player.getObjectId());
            onlineTime.put(player.getObjectId(), playerOnline + 5);
        } else {
            onlineTime.put(player.getObjectId(), 5);
        }
    }

    private boolean canReward(Player player, int playerOnline) {
        return DataManager.ONLINE_BONUS_DATA.getBonusForTime(playerOnline) != null;
    }

    private void reward(Player player, int playerOnline) {
        Bonus bonus = DataManager.ONLINE_BONUS_DATA.getBonusForTime(playerOnline);
        int rewardLetters[];
        if (bonus.isRandomReward()) {
            rewardLetters = new int[1];
            RewardItem rewardItem = Rnd.get(bonus.getRewardItems());
            rewardLetters[0] = SystemMailService.getInstance().sendMail(SENDER, player.getName(), TITLE, MESSAGE, rewardItem.getId(), rewardItem.getAmount(), bonus.getRewardKinah(), LetterType.EXPRESS);
        } else {
            rewardLetters = new int[bonus.getRewardItems().size()];
            for (int i = 0; i < bonus.getRewardItems().size(); i++) {
                RewardItem rewardItem = bonus.getRewardItems().get(i);
                if (i == 0) {
                    rewardLetters[i] = SystemMailService.getInstance().sendMail(SENDER, player.getName(), TITLE, MESSAGE, rewardItem.getId(), rewardItem.getAmount(), bonus.getRewardKinah(), LetterType.EXPRESS);
                } else {
                    rewardLetters[i] = SystemMailService.getInstance().sendMail(SENDER, player.getName(), TITLE, MESSAGE, rewardItem.getId(), rewardItem.getAmount(), 0, LetterType.EXPRESS);
                }
            }
        }
        player.getMailbox().setSkipPostmanCooldown(true);
        OnlineReward bonusEntry = new OnlineReward(player, bonus.getRewardToll(), rewardLetters);
        rewards.put(player.getObjectId(), bonusEntry);
        if (bonus.isResetTime()) {
            onlineTime.put(player.getObjectId(), 0);
        }
    }

    public void updateRewardStatus(Player player, int letterId) {
        if (rewards.containsKey(player.getObjectId())) {
            OnlineReward reward = rewards.get(player.getObjectId());
            boolean isRewardMatches = false;
            for (int rewardLetterId : reward.letterIds) {
                if (rewardLetterId == letterId) {
                    isRewardMatches = true;
                    break;
                }
            }
            if (isRewardMatches) {
                reward.isRewarded = true;
            }
        }
    }

    private class OnlineReward {

        private final Player player;
        private final int tolls;
        private final int[] letterIds;
        private final long creationTime;
        private boolean isRewarded;

        public OnlineReward(Player player, int tolls, int[] letterIds) {
            this.player = player;
            this.tolls = tolls;
            this.letterIds = letterIds;
            this.creationTime = System.currentTimeMillis();
            this.isRewarded = false;
        }
    }

    private class RewardsUpdateTask implements Runnable {

        @Override
        public void run() {
            for (OnlineReward reward : rewards.values()) {
                if (reward.isRewarded) {
                    if (reward.tolls > 0) {
                        InGameShopEn.getInstance().addToll(reward.player, reward.tolls);
                    }
                    rewards.remove(reward.player.getObjectId());
                    continue;
                }
                if ((System.currentTimeMillis() - reward.creationTime) / 1000 / 60 < CustomConfig.ONLINE_BONUSES_LIFETIME) {
                    continue;
                }
                Player looser = reward.player;
                if (looser.isOnline()) {
                    MailService.getInstance().deleteMail(looser, reward.letterIds);
                } else {
                    for (int letterId : reward.letterIds) {
                        GDB.get(MailDAO.class).deleteLetter(letterId);
                    }
                }
                rewards.remove(looser.getObjectId());
                looser.getMailbox().setSkipPostmanCooldown(false);

            }
        }
    }

    public static OnlineBonusService getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder {

        protected static final OnlineBonusService instance = new OnlineBonusService();
    }
}
