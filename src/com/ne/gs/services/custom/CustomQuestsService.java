package com.ne.gs.services.custom;

import com.ne.commons.DateUtil;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.services.CronService;
import com.ne.gs.configs.main.PvPConfig;
import com.ne.gs.controllers.attack.AggroInfo;
import com.ne.gs.controllers.attack.AggroList;
import com.ne.gs.controllers.attack.KillList;
import com.ne.gs.dataholders.CustomQuestsData;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.QuestStateList;
import com.ne.gs.model.templates.custom_quests.CustomQuestTemplate;
import com.ne.gs.model.templates.custom_quests.QuestRewards;
import com.ne.gs.model.templates.custom_quests.Schedule;
import com.ne.gs.modules.common.Item;
import com.ne.gs.modules.common.PollBuilder;
import com.ne.gs.modules.common.PollRegistry;
import com.ne.gs.modules.pvpevent.Messages;
import com.ne.gs.modules.pvpevent.PvpItemList;
import com.ne.gs.modules.pvpevent.PvpRewardList;
import com.ne.gs.modules.pvpevent.PvpRewardQuery;
import com.ne.gs.questEngine.model.QuestState;
import com.ne.gs.questEngine.model.QuestStatus;
import com.ne.gs.services.HTMLService;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.world.World;
import javolution.util.FastMap;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ViAl
 */
public class CustomQuestsService {
    private static final Logger log = LoggerFactory.getLogger(CustomQuestsService.class);
    private final CustomQuestsData TEMPLATES = DataManager.CUSTOM_QUESTS_DATA;
    private final List<CustomQuestTemplate> activeQuests;

    private static final FastMap<Integer, KillList> questFrequentKills = new FastMap<Integer, KillList>().shared();
    private static final FastMap<Integer, KillList> questDailyKills = new FastMap<Integer, KillList>().shared();


    private static final FastMap<Integer, HashMap<Integer, Tuple2<QuestState, CustomQuestTemplate>>> cachedRewards
            = new FastMap<Integer, HashMap<Integer, Tuple2<QuestState, CustomQuestTemplate>>>().shared();

    private CustomQuestsService() {
        activeQuests = new CopyOnWriteArrayList<>();
        for (CustomQuestTemplate template : TEMPLATES.getTemplates()) {
            for (Schedule schedule : template.getSchedule()) {
                scheduleQuestEnd(template, schedule);

                try {
                    DateUtil.CronExpr from = new DateUtil.CronExpr(schedule.getStartDate());
                    DateUtil.CronExpr to = new DateUtil.CronExpr(schedule.getEndDate());

                    if (DateUtil.cronBetween(from, to)) {
                        enableQuest(template);
                        continue;
                    }

                } catch (ParseException e) {
                    throw new Error(e);
                }


                scheduleQuestStart(template, schedule);
            }
        }
    }

    private void scheduleQuestEnd(final CustomQuestTemplate template, Schedule schedule) {
        CronService.getInstance().schedule(() -> {
            log.info("CustomQuest #" + template.getId() + " setted to inactive state.");
            activeQuests.remove(template);
        }, schedule.getEndDate());
    }

    private void scheduleQuestStart(final CustomQuestTemplate template, Schedule schedule) {
        CronService.getInstance().schedule(() -> enableQuest(template), schedule.getStartDate());
    }

    private void enableQuest(CustomQuestTemplate template) {
        log.info("CustomQuest #" + template.getId() + " setted to active state.");
        activeQuests.add(template);
        for (Player p : World.getInstance().getAllPlayers()) {
            for (QuestState qs : p.getCustomQuestStateList().getAllQuestState()) {
                if (qs.getQuestId() == template.getId())
                    PacketSendUtility.sendBrightYellowMessageOnCenter(p, template.getAnnouncements().getOnQuestRestart());
            }
            giveNewQuests(p);
        }
    }

    public void enableQuestByHand(Player admin, CustomQuestTemplate template) {
        if (activeQuests.contains(template)) {
            PacketSendUtility.sendMessage(admin, "This quest is already enable.");
        } else {
            enableQuest(template);
            PacketSendUtility.sendMessage(admin, "Quest " + template.getName() + " enabled.");
        }
    }

    public void onPlayerLogin(Player player) {
        sendQuestsInfo(player);
        resetExpiredQuests(player);
        giveNewQuests(player);

        //sry 4 that =(
        HashMap<Integer, Tuple2<QuestState, CustomQuestTemplate>> data = cachedRewards.remove(player.getObjectId());
        if (data == null)
            return;

        for (Map.Entry<Integer, Tuple2<QuestState, CustomQuestTemplate>> entry : data.entrySet())
            reward(player, entry.getValue()._1, entry.getValue()._2);

        //refreshOldQuests(player);
    }

    private void sendQuestsInfo(Player player) {
        if (player.getCustomQuestStateList().size() > 0) {
            PacketSendUtility.sendMessage(player, "Сводка баллов по Вашим автоматическим заданиям:");
            int i = 1;
            for (QuestState qs : player.getCustomQuestStateList().getAllQuestState()) {
                CustomQuestTemplate template = TEMPLATES.getTemplate(qs.getQuestId());
                if (template == null) {
                    continue;
                }
                PacketSendUtility.sendMessage(player, i + ". Задание: " + template.getName() + ", Набрано баллов:" + qs.getCompleteCount());
                if (isActive(template))
                    PacketSendUtility.sendBrightYellowMessageOnCenter(player, template.getAnnouncements().getOnQuestRestart());
                i++;
            }
        }
    }

    private void resetExpiredQuests(Player player) {
        QuestStateList qsl = player.getCustomQuestStateList();
        for (QuestState qs : qsl.getAllQuestState()) {
            Timestamp completeTime = qs.getCompleteTime();
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (completeTime != null) {
                CustomQuestTemplate template = TEMPLATES.getTemplate(qs.getQuestId());
                if (template != null) {
                    Timestamp resetTime = new Timestamp(completeTime.getTime() + template.getResetAfterDays() * 24 * 60 * 60 * 1000);
                    if (resetTime.before(now)) {
                        log.info("CUSTOM QUESTS Player " + player.getName() + " didn't finished quest #" + template.getId() + " too long, resetting it's complete count to null.");
                        qs.setCompleteCount(0);
                    }
                }
            }
        }
    }

    public void giveNewQuests(Player player) {
        for (CustomQuestTemplate template : activeQuests) {
            boolean minLevelMatch = player.getLevel() >= template.getStartProps().getMinLevel();
            boolean maxLevelMatch = player.getLevel() <= template.getStartProps().getMaxLevel();
            boolean raceMatch = player.getRace() == template.getStartProps().getRace();
            boolean notStarted = player.getCustomQuestStateList().getQuestState(template.getId()) == null;
            if (minLevelMatch && maxLevelMatch && raceMatch && notStarted) {
                log.info("CUSTOM QUESTS Player " + player.getName() + " has aquired custom quest #" + template.getId());
                QuestState newQuest = new QuestState(template.getId(), QuestStatus.START, 0, 0, null, 0, null);
                player.getCustomQuestStateList().addQuest(template.getId(), newQuest);
                PacketSendUtility.sendBrightYellowMessageOnCenter(player, template.getAnnouncements().getOnQuestStart());
            }
        }
    }

	/*private void refreshOldQuests(Player player) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
		for(QuestState qs : player.getCustomQuestStateList().getAllQuestState()) {
			if(qs.getNextRepeatTime() != null && qs.getNextRepeatTime().before(now))
				qs.setCompleteCount(0);
		}
	}*/

    public void onNpcKill(Player killer, Npc victim) {
        for (QuestState qs : killer.getCustomQuestStateList().getAllQuestState()) {
            CustomQuestTemplate template = TEMPLATES.getTemplate(qs.getQuestId());
            if (template == null)
                continue;
            if (!isActive(template))
                continue;
            if (killer.getWorldId() != template.getWorldId())
                continue;
            if (template.getEndProps().getMonsterKills() == 0)
                continue;
            if (qs.getCompleteCount() >= template.getAmountPerDay())
                continue;
            if (template.isSoloKill()) {

                if (killer.isInGroup2())
                    continue;

                AggroList lst = victim.getAggroList();
                if (lst == null)
                    continue;

                int damage = 0;

                AggroInfo oinfo = victim.getAggroList().getAggroInfo(killer);

                if (killer.getSummon() != null) {
                    AggroInfo sinfo = victim.getAggroList().getAggroInfo(killer.getSummon());

                    if (sinfo != null)
                        damage += sinfo.getDamage();
                }

                if (oinfo != null)
                    damage += oinfo.getDamage();

                if (damage < lst.getTotalDamage() - 1)
                    continue;
            }
            if (template.getEndProps().getMonsterId().contains(String.valueOf(victim.getNpcId()))) {
                if (qs.getStatus() == QuestStatus.START) {
                    int newVar = qs.getQuestVarById(0) + 1;
                    if (newVar <= template.getEndProps().getMonsterKills()) {
                        qs.setQuestVarById(0, newVar);
                        log.info("Player " + killer.getName() + " has reached next step in custom quest #" + template.getId() + ". Now it is " + newVar);
                        PacketSendUtility.sendBrightYellowMessageOnCenter(killer, "Обновлена информация по заданию \"" + template.getName() + "\". Убито: " + newVar + "/" + template.getEndProps().getMonsterKills());
                        checkIfComplete(killer, qs, template);
                    }
                } else if (qs.getStatus() == QuestStatus.COMPLETE) {
                    qs.setStatus(QuestStatus.START);
                    qs.setQuestVarById(0, 1);
                    qs.setQuestVarById(1, 0);
                    PacketSendUtility.sendBrightYellowMessageOnCenter(killer, template.getAnnouncements().getOnQuestStart());
                    PacketSendUtility.sendBrightYellowMessageOnCenter(killer, "Обновлена информация по заданию \"" + template.getName() + "\". Убито: 1");
                }
            }
        }
    }

    public void onPlayerKill(Player killer, Player victim) {
        int frequentKills = getQuestKillsFor(killer.getObjectId(), victim.getObjectId());
        int dailyKills = getQuestDailyKillsFor(killer.getObjectId(), victim.getObjectId());

        if (frequentKills != 0) {
            killer.sendMsg(String.format("Убийство %s не будет засчитано по заданию чаще чем раз в %d сек!", victim.getName(), PvPConfig.QUEST_CHAIN_KILL_TIME_RESTRICTION / 1000));
            return;
        }

        if (dailyKills > PvPConfig.QUEST_CHAIN_KILL_NUMBER_RESTRICTION) {
            killer.sendMsg(String.format("Убийство %s более %d раз не будет засчитано по заданию!", victim.getName(), dailyKills));
            return;
        }

        boolean atLeastOne = false;

        for (QuestState qs : killer.getCustomQuestStateList().getAllQuestState()) {
            CustomQuestTemplate template = TEMPLATES.getTemplate(qs.getQuestId());
            if (template == null)
                continue;
            if (!isActive(template))
                continue;
            if (killer.getWorldId() != template.getWorldId())
                continue;
            if (template.getEndProps().getPlayerKills() == 0)
                continue;
            if (qs.getCompleteCount() >= template.getAmountPerDay())
                continue;
            if (template.isSoloKill()) {

                if (killer.isInGroup2())
                    continue;

                AggroList lst = victim.getAggroList();
                if (lst == null)
                    continue;

                int damage = 0;

                AggroInfo oinfo = victim.getAggroList().getAggroInfo(killer);

                if (killer.getSummon() != null) {
                    AggroInfo sinfo = victim.getAggroList().getAggroInfo(killer.getSummon());

                    if (sinfo != null)
                        damage += sinfo.getDamage();
                }

                if (oinfo != null)
                    damage += oinfo.getDamage();

                if (damage < lst.getTotalDamage() - 1)
                    continue;
            }
            if (qs.getStatus() == QuestStatus.START) {
                int newVar = qs.getQuestVarById(1) + 1;
                if (newVar <= template.getEndProps().getPlayerKills()) {
                    atLeastOne = true;
                    qs.setQuestVarById(1, newVar);
                    log.info("Player " + killer.getName() + " has reached next step in custom quest #" + template.getId() + ". Now it is " + newVar);
                    PacketSendUtility.sendBrightYellowMessageOnCenter(killer, "Обновлена информация по заданию \"" + template.getName() + "\". Убито противников: " + newVar + "/" + template.getEndProps().getPlayerKills());
                    checkIfComplete(killer, qs, template);
                }
            } else if (qs.getStatus() == QuestStatus.COMPLETE) {
                atLeastOne = true;
                qs.setStatus(QuestStatus.START);
                qs.setQuestVarById(1, 1);
                qs.setQuestVarById(0, 0);
                PacketSendUtility.sendBrightYellowMessageOnCenter(killer, template.getAnnouncements().getOnQuestStart());
                PacketSendUtility.sendBrightYellowMessageOnCenter(killer, "Обновлена информация по заданию \"" + template.getName() + "\". Убито противников: 1");
            }
        }

        if (atLeastOne) {
            addQuestKillFor(killer.getObjectId(), victim.getObjectId());
            addQuestDailyKillFor(killer.getObjectId(), victim.getObjectId());
        }
    }

    public boolean isActive(CustomQuestTemplate template) {
        return activeQuests.contains(template);
    }

    private void checkIfComplete(Player player, QuestState qs, CustomQuestTemplate template) {
        boolean allMonstersKilled = qs.getQuestVarById(0) >= template.getEndProps().getMonsterKills();
        boolean allPlayersKilled = qs.getQuestVarById(1) >= template.getEndProps().getPlayerKills();
        log.info("Player " + player.getName() + ", custom quest #" + template.getId() + ", monsters_killed: " + qs.getQuestVarById(0) + ", players_killed:" + qs.getQuestVarById(1));
        if (allMonstersKilled && allPlayersKilled) {
            finishQuest(player, qs, template);
            reward(player, qs, template);
        }
    }

    private void finishQuest(Player player, QuestState qs, CustomQuestTemplate template) {
        log.info("Player " + player.getName() + " finished custom quest #" + template.getId());
        Timestamp now = new Timestamp(System.currentTimeMillis());
        qs.setQuestVarById(0, 0);
        qs.setQuestVarById(1, 0);
        qs.setStatus(QuestStatus.COMPLETE);
        if (qs.getNextRepeatTime() == null)
            qs.setCompleteCount(qs.getCompleteCount() + 1);
        if (qs.getNextRepeatTime() != null && qs.getNextRepeatTime().before(now))
            qs.setCompleteCount(qs.getCompleteCount() + 1);
        qs.setNextRepeatTime(countNextRepeatTime());
        PacketSendUtility.sendBrightYellowMessageOnCenter(player, template.getAnnouncements().getOnQuestEnd());
    }

    private void reward(Player player, QuestState qs, CustomQuestTemplate template) {
        QuestRewards reward = null;
        for (QuestRewards qr : template.getRewards()) {
            if (qr.getCompleteCount() == qs.getCompleteCount()) {
                reward = qr;
                break;
            }
        }
        if (reward == null) {
            reward = template.getRewards().get(0);
        }
        if (reward.getPollRewards() != null) {
            Integer pollUid = IDFactory.getInstance().nextId();
            PollRegistry.insert(pollUid, new PvpRewardQuery(player, reward.getPollRewards()));
            String HTML = createQuestPoll(player, template, reward.getPollRewards());

            if (!cachedRewards.containsKey(player.getObjectId()))
                cachedRewards.put(player.getObjectId(), new HashMap<>());

            cachedRewards.get(player.getObjectId()).put(pollUid, Tuple2.of(qs, template));

            HTMLService.sendData(player, pollUid, HTML);
        }
    }

    public void removeCached(Player player, Integer poolUid) {

        HashMap<Integer, Tuple2<QuestState, CustomQuestTemplate>> data = cachedRewards.get(player.getObjectId());
        if (data == null)
            return;

        data.remove(poolUid);
    }

    public static String createQuestPoll(Player player, CustomQuestTemplate template, PvpRewardList reward) {
        PvpItemList randomItems = reward.getRandomRewardList();
        PvpItemList selectiveItems = reward.getSelectiveRewardList();

        String title = new PollBuilder.TextBuilder(player.getLang())
                .printf(Messages.POLL_EVENT, template.getName())
                .build();

        PollBuilder.TextBuilder tb = new PollBuilder.TextBuilder(player.getLang());
        tb.println(Messages.POLL_EVENT_REWARD);

        if (reward.getAp() > 0) {
            tb.printlnf(Messages.POLL_EVENT_REWARD_AP, reward.getAp());
        }

        if (reward.getLvl() > 0) {
            tb.printlnf(Messages.POLL_EVENT_REWARD_LVL, reward.getLvl());
        }

        if (reward.getGp() > 0) {
            tb.printlnf(Messages.POLL_EVENT_REWARD_GP, reward.getGp());
        }

        if (!randomItems.getItems().isEmpty()) {
            tb.printlnf(Messages.POLL_EVENT_REWARD_RND_ITEM, randomItems.getLimit());
        }

        String body = tb.build();

        PollBuilder pb = new PollBuilder()
                .setTitle(title)
                .setBody(body)
                .setItemCaption(player.translate(Messages.CHOOSE_ITEM, selectiveItems.getLimit()))
                .setItemChooseLimit(selectiveItems.getLimit());

        for (Item item : selectiveItems.getItems()) {
            pb.addItem(item.getItemId(), (int) item.getCount());
        }

        return pb.build();
    }

    private static Timestamp countNextRepeatTime() {
        DateTime repeatDate = DateTime.now().plusHours(24);
        return Timestamp.valueOf(repeatDate.getYear() + "-" + repeatDate.getMonthOfYear() + "-" + repeatDate.getDayOfMonth() + " 09:00:00");
    }

    public static CustomQuestsService getInstance() {
        return SingletonHolder.instance;
    }

    private static final class SingletonHolder {
        protected static final CustomQuestsService instance = new CustomQuestsService();
    }

    public static int getQuestKillsFor(int winnerId, int victimId) {
        KillList winnerKillList = questFrequentKills.get(winnerId);

        if (winnerKillList == null) {
            return 0;
        }
        return winnerKillList.getQuestFrequentKillsFor(victimId);
    }

    private static void addQuestKillFor(int winnerId, int victimId) {
        KillList winnerKillList = questFrequentKills.get(winnerId);
        if (winnerKillList == null) {
            winnerKillList = new KillList();
            questFrequentKills.put(winnerId, winnerKillList);
        }
        winnerKillList.addKillFor(victimId);
    }

    public static int getQuestDailyKillsFor(int winnerId, int victimId) {
        KillList winnerKillList = questDailyKills.get(winnerId);

        if (winnerKillList == null) {
            return 0;
        }
        return winnerKillList.getDailyKillsFor(victimId);
    }

    private static void addQuestDailyKillFor(int winnerId, int victimId) {
        KillList winnerKillList = questDailyKills.get(winnerId);
        if (winnerKillList == null) {
            winnerKillList = new KillList();
            questDailyKills.put(winnerId, winnerKillList);
        }
        winnerKillList.addKillFor(victimId);
    }
}
