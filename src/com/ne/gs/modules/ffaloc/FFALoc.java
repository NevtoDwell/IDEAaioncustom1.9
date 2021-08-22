/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2014, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.ffaloc;

import com.ne.commons.Sys;
import com.ne.commons.annotations.NotNull;
import com.ne.commons.func.tuple.Tuple2;
import com.ne.commons.func.tuple.Tuple3;
import com.ne.commons.utils.*;
import com.ne.gs.configs.main.GroupConfig;
import com.ne.gs.controllers.attack.AggroInfo;
import com.ne.gs.controllers.attack.AggroList;
import com.ne.gs.controllers.effect.PlayerEffectController;
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.instance.handlers.InstanceHandler;
import com.ne.gs.model.EmotionType;
import com.ne.gs.model.conds.*;
import com.ne.gs.model.events.ExceptBuffHandler;
import com.ne.gs.model.events.PlayerSpawn;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.Item;
import com.ne.gs.model.gameobjects.Pet;
import com.ne.gs.model.gameobjects.TransformModel;
import com.ne.gs.model.gameobjects.player.AbyssRank;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.handlers.EffectResurrectBaseHandler;
import com.ne.gs.model.handlers.PlayerDieHandler;
import com.ne.gs.model.instance.InstanceScoreType;
import com.ne.gs.model.items.ItemSlot;
import com.ne.gs.model.skill.SkillId;
import com.ne.gs.model.team2.alliance.PlayerAllianceService;
import com.ne.gs.model.team2.group.PlayerGroupService;
import com.ne.gs.model.templates.item.ItemTemplate;
import com.ne.gs.modules.common.CustomLocManager;
import com.ne.gs.modules.common.CustomLocScript;
import com.ne.gs.modules.common.CustomLocTemplate;
import com.ne.gs.modules.common.Pos;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.network.aion.Packets;
import com.ne.gs.network.aion.serverpackets.*;
import com.ne.gs.services.PvpService;
import com.ne.gs.services.abyss.AbyssPointsService;
import com.ne.gs.services.player.PlayerReviveService;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.services.toypet.PetSpawnService;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.skillengine.model.SkillTemplate;
import com.ne.gs.skillengine.model.TransformType;
import com.ne.gs.utils.ChatUtil;
import com.ne.gs.utils.MathUtil;
import com.ne.gs.utils.PacketSendUtility;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.idfactory.IDFactory;
import com.ne.gs.utils.stats.StatFunctions;
import com.ne.gs.world.World;
import com.ne.gs.world.WorldMapInstance;
import gnu.trove.set.hash.THashSet;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ne.gs.skillengine.model.Skill.SkillMethod;
import static java.lang.String.format;

/**
 * This class ...
 *
 * @author hex1r0
 */
public class FFALoc extends CustomLocScript {

    private static final String ENTERED = "player=%s entered loc=%s";
    private static final String LEFT = "player=%s left loc=%s";

    private static final String FORMAT = "Игрок %s вошел в %s. Сейчас в локации %d игроков\"";

    private final OnSpawn _onPlayerSpawn = new OnSpawn(this);
    private final OnPlayerDie _onPlayerDie;

    private final ExceptBuffHandler _dontRemoveBuffs = new ExceptBuffHandler() {
        @Override
        public Boolean onEvent(@NotNull Player e) {
            return true;
        }
    };

    private final Set<Integer> _participants = new THashSet<>();

    //private final NameFactory _nameFactory = new NameFactory();

    private final int MAX_PARTICIPANTS;
    private final boolean STATIC_AP;
    private final boolean STATIC_DP;
    private final int GAIN_AP;
    private final int PREM_GAIN_AP;
    private final int VIP_GAIN_AP;
    private final int LOSE_AP;
    private final int GAIN_DP;
    private final int PREM_GAIN_DP;
    private final int VIP_GAIN_DP;
    private final int GAIN_GP;
    private final int VIP_GAIN_GP;
    private final int MORF_ID;
    private final int HELMET_ID;
    private final int BRAND_ID;
    private final String ANNOUNCE;
    private final String DOORS;
    private final int PROTECTION_SKILL_ID;
    private final int PROTECTION_SKILL_MS;

    private long _lastAnnounceMs = 0;
    private Integer doorId;

    @SuppressWarnings("unchecked")
    public FFALoc(CustomLocTemplate loc, Date expiresAt) {
        super(loc, expiresAt);

        MAX_PARTICIPANTS = Integer.valueOf(loc.getPropertyList().getValue("maxParticipants", "-1"));
        STATIC_AP = Boolean.valueOf(loc.getPropertyList().getValue("staticAp", "false"));
        STATIC_DP = Boolean.valueOf(loc.getPropertyList().getValue("staticDp", "false"));
        GAIN_AP = Integer.valueOf(loc.getPropertyList().getValue("gainAp", "0"));
        PREM_GAIN_AP = Integer.valueOf(loc.getPropertyList().getValue("premGainAp", "0"));
        VIP_GAIN_AP = Integer.valueOf(loc.getPropertyList().getValue("vipGainAp", "0"));
        LOSE_AP = Integer.valueOf(loc.getPropertyList().getValue("loseAp", "0"));
        GAIN_DP = Integer.valueOf(loc.getPropertyList().getValue("gainDp", "0"));
        PREM_GAIN_DP = Integer.valueOf(loc.getPropertyList().getValue("premGainDp", "0"));
        VIP_GAIN_DP = Integer.valueOf(loc.getPropertyList().getValue("vipGainDp", "0"));
        GAIN_GP = Integer.valueOf(loc.getPropertyList().getValue("gainGp", "0"));
        VIP_GAIN_GP = Integer.valueOf(loc.getPropertyList().getValue("vipGainGp", "0"));
        MORF_ID = Integer.valueOf(loc.getPropertyList().getValue("morfId", "110900339"));
        HELMET_ID = Integer.valueOf(loc.getPropertyList().getValue("helmetId", "0"));
        BRAND_ID = Integer.valueOf(loc.getPropertyList().getValue("brandId", "0"));
        ANNOUNCE = String.valueOf(loc.getPropertyList().getValue("announce", ""));
        DOORS = loc.getPropertyList().getValue("doors", null);
        PROTECTION_SKILL_ID = Integer.valueOf(loc.getPropertyList().getValue("protectionSkillId", "9833"));
        PROTECTION_SKILL_MS = Integer.valueOf(loc.getPropertyList().getValue("protectionSkillTime", "10000"));

        _onPlayerDie = new OnPlayerDie(PROTECTION_SKILL_ID, PROTECTION_SKILL_MS);

        scheduleAtFixedRate(() ->

                doOnAllPlayers(p1 ->
                        p1.getKnownList().doOnAllPlayers(p2 ->
                                p1.sendPck(new SM_SHOW_BRAND(BRAND_ID, p2.getObjectId()))
                        )
                ), 1000L, 3000L);
    }


    @Override
    public void onEnterInstance(Player player) {
        PlayerGroupService.removePlayer(player);
        PlayerAllianceService.removePlayer(player);
        PlayerReviveService.revive(player, 100, 100, false, 0);

        Pet toyPet = player.getPet();
        if (toyPet != null) {
            player.sendPck(new SM_PET(4, toyPet));
            PetSpawnService.dismissPet(player, true);
        }
    }

    @Override
    public void onInstanceCreate(WorldMapInstance instance) {
        super.onInstanceCreate(instance);
        World.getInstance().doOnAllPlayers(player -> PacketSendUtility.sendBrightYellowMessageOnCenter(player, ANNOUNCE));
        //EventNotifier.GLOBAL.attach(_listener);
        EventNotifier.GLOBAL.attach(_onPlayerSpawn);

        if (DOORS != null) {
            List<String> doorsList = Arrays.asList(DOORS.split(" "));
            for (String door : doorsList) {
                doorId = Integer.parseInt(door);
                instance.openDoor(doorId);
                CustomLocManager.log().info(format("FFALoc: Door (id " + doorId + ") is opened"));
            }
        }
    }

    @Override
    public void onInstanceDestroy() {
        super.onInstanceDestroy();

        //EventNotifier.GLOBAL.detach(_listener);
        EventNotifier.GLOBAL.detach(_onPlayerSpawn);
    }

    @Override
    public void onExitInstance(final Player player) {
        _onLeaveLoc(player);

        _participants.remove(player.getObjectId());

        TeleportService.moveToBindLocation(player, true);
    }

    @Override
    public void onLeaveInstance(final Player player) {
        _onLeaveLoc(player);

        _participants.remove(player.getObjectId());
    }

    @Override
    public void onPlayerLogOut(final Player player) {
        _participants.remove(player.getObjectId());

        TeleportService.moveToBindLocation(player, true);
    }

    @Override
    public boolean canEnter(Player player) {
        if (MAX_PARTICIPANTS >= 0 && instance.playersCount() >= MAX_PARTICIPANTS) {
            player.sendMsg("Вы не можете войти. Максимальное количество участников: " + MAX_PARTICIPANTS);
            return false;
        }

        return true;
    }

    private void _onLeaveLoc(Player p) {
        //String customName = p.getImplementator().result(VisiblePlayerName.class, p);

        //_nameFactory.push(customName);

        p.getChainer().detach(OnResurrectBase.STATIC);
        p.getChainer().detach(_onPlayerDie);
        p.getChainer().detach(_dontRemoveBuffs);

        p.getConditioner().detach(SpawnObjCustomCond.STATIC);
        //p.getConditioner().detach(IsEnemyCond.TRUE);
        p.getConditioner().detach(IsAggroIconCond.TRUE);
        p.getConditioner().detach(CanBeInvitedToAlliance.FALSE);
        p.getConditioner().detach(CanBeInvitedToGroup.FALSE);
        p.getConditioner().detach(CanInviteToGroup.FALSE);
        p.getConditioner().detach(CanInviteToAlliance.FALSE);
        p.getConditioner().detach(CanChat.FALSE);
        p.getConditioner().detach(CanEmote.FALSE);
        p.getConditioner().detach(CanSummonPet.FALSE);
        p.getConditioner().detach(IsLegionVisibleImpl.STATIC);
        p.getConditioner().detach(IsVisuallyTransformedImpl.STATIC);

        p.getImplementator().detach(PlayerName.class.getName());
        p.getImplementator().detach(MorfedEquipment.class.getName());
        p.getImplementator().detach(VisualRankImpl.STATIC);

        p.sendPck(new SM_PLAYER_INFO(p, false));

        PacketSendUtility.broadcastPacket(p, new SM_UPDATE_PLAYER_APPEARANCE(p.getObjectId(), p.getEquipment().getEquippedForAppearance()), true);

        p.sendPck(new SM_SHOW_BRAND(0, 0));
        p.getKnownList().doOnAllPlayers(p1 ->
                p.sendPck(new SM_SHOW_BRAND(0, p1.getObjectId())));

        info(format(LEFT, p.getName(), _template.getId()));
    }


    private void sendScopePacket(Player p, long remainingTimeMs) {
        p.sendPck(new SM_QUEST_ACTION(0, (int) (remainingTimeMs / 1000)));
    }

    private void info(String msg) {
        CustomLocManager.log().info(format("FFALoc: %s", msg));
    }

    private static final class Entry implements Cloneable {
        int AP;
        int kills;
        int deaths;

        Entry() {
        }

        Entry(int AP, int kills, int deaths) {
            this.AP = AP;
            this.kills = kills;
            this.deaths = deaths;
        }

        @SuppressWarnings("CloneDoesntCallSuperClone")
        @Override
        protected final Entry clone() {
            return new Entry(AP, kills, deaths);
        }
    }

    static {
        Packets.addLoaderAndRun(() -> Packets.regSP(SpInstanceScore.class, 0x79));
    }

    private static class SpInstanceScore extends AionServerPacket {
        private final Entry _entry;
        private final long _remainingTimeMS;

        public SpInstanceScore(Entry entry, long remainingTimeMS) {
            _entry = entry.clone();
            _remainingTimeMS = remainingTimeMS;
        }

        @Override
        protected void writeImpl(AionConnection con) {
            writeD(300040000);
            writeD((int) _remainingTimeMS);
            writeD(InstanceScoreType.START_PROGRESS.getId());
            writeD(_entry.AP);
            writeD(_entry.kills);
            writeD(_entry.deaths);
            writeD(0);
        }
    }

    private static class SpawnObjCustomCond extends SpawnObjCond {
        public static final SpawnObjCustomCond STATIC = new SpawnObjCustomCond();

        @Override
        public Boolean onEvent(@NotNull Env env) {
            env.player.sendMsg("Здесь нельзя устанавливать ники.");
            return false;
        }
    }

    public static abstract class SimpleImplementation<E, R> implements Implementation<E, R> {
        @Override
        public int getPriority() {
            return 0;
        }
    }

    public static abstract class VisualEquipment extends SimpleImplementation<Player, List<Item>> {

        public static final VisualEquipment STATIC = new VisualEquipment() {
            @Override
            public List<Item> onEvent(@NotNull Player e) {
                Collection<Item> allItems = e.getEquipment().getEquippedItems();
                List<Item> equippedItems = new ArrayList<>(allItems.size());
                for (Item item : allItems) {
                    int slot = item.getEquipmentSlot();
                    if (!ItemSlot.isStigma(slot) && (slot != ItemSlot.WAIST.id())) {
                        equippedItems.add(item);
                    }
                }
                return equippedItems;
            }
        };

        @Override
        public String getType() {
            return VisualEquipment.class.getName();
        }
    }

    public static abstract class VisiblePlayerName extends SimpleImplementation<Tuple2<Player, Player>, String> {

        public static final VisiblePlayerName STATIC = new VisiblePlayerName() {
            @Override
            public String onEvent(@NotNull Tuple2<Player, Player> e) {
                return ChatUtil.decorateName(e._1);
            }
        };

        @Override
        public String getType() {
            return VisiblePlayerName.class.getName();
        }
    }

    public static abstract class IsVisuallyTransformed extends SimpleCond<Creature> {

        public static final IsVisuallyTransformed STATIC = new IsVisuallyTransformed() {
            @Override
            public Boolean onEvent(@NotNull Creature e) {
                return e.getTransformModel().isActive();
            }
        };

        @NotNull
        @Override
        public final String getType() {
            return IsVisuallyTransformed.class.getName();
        }
    }

    public static abstract class VisualRank extends SimpleImplementation<Player, Integer> {

        public static final VisualRank STATIC = new VisualRank() {
            @Override
            public Integer onEvent(@NotNull Player e) {
                return e.getAbyssRank().getRank().getId();
            }
        };

        @Override
        public String getType() {
            return VisualRank.class.getName();
        }
    }

    public static abstract class IsLegionVisible extends SimpleCond<Player> {

        public static final IsLegionVisible STATIC = new IsLegionVisible() {
            @Override
            public Boolean onEvent(@NotNull Player e) {
                return e.isLegionMember();
            }
        };

        @NotNull
        @Override
        public final String getType() {
            return IsLegionVisible.class.getName();
        }
    }


    private class OnSpawn extends PlayerSpawn.PlayerSpawnChannelBound {
        public OnSpawn(InstanceHandler instanceHandler) {
            super(instanceHandler);
        }

        @Override
        public void onEventImpl(@NotNull final Player p) {
            sendScopePacket(p, remainingTimeMs());

            boolean inside = _participants.contains(p.getObjectId());
            if (inside) return;

            _participants.add(p.getObjectId());

            p.getChainer().attach(_onPlayerDie);
            p.getChainer().attach(OnResurrectBase.STATIC);
            p.getChainer().attach(_dontRemoveBuffs);

            p.getConditioner().attach(SpawnObjCustomCond.STATIC);
            //p.getConditioner().attach(IsEnemyCond.TRUE);
            p.getConditioner().attach(IsAggroIconCond.TRUE);
            p.getConditioner().attach(CanBeInvitedToAlliance.FALSE);
            p.getConditioner().attach(CanBeInvitedToGroup.FALSE);
            p.getConditioner().attach(CanInviteToGroup.FALSE);
            p.getConditioner().attach(CanInviteToAlliance.FALSE);
            p.getConditioner().attach(CanChat.FALSE);
            p.getConditioner().attach(CanEmote.FALSE);
            p.getConditioner().attach(CanSummonPet.FALSE);
            p.getConditioner().attach(IsLegionVisibleImpl.STATIC);
            p.getConditioner().attach(IsVisuallyTransformedImpl.STATIC);

            //String customName = _nameFactory.pop();
            String className = "Клоун";
            switch (p.getPlayerClass()) {
                case GLADIATOR:
                    className = "Гладиатор";
                    break;
                case TEMPLAR:
                    className = "Страж";
                    break;
                case ASSASSIN:
                    className = "Убийца";
                    break;
                case RANGER:
                    className = "Стрелок";
                    break;
                case SORCERER:
                    className = "Волшебник";
                    break;
                case SPIRIT_MASTER:
                    className = "Заклинатель";
                    break;
                case CLERIC:
                    className = "Целитель";
                    break;
                case CHANTER:
                    className = "Чародей";
                    break;
            }
            p.getImplementator().attach(PlayerName.class.getName(), new PlayerName(className));
            p.getImplementator().attach(MorfedEquipment.class.getName(), new MorfedEquipment(MORF_ID, HELMET_ID));
            p.getImplementator().attach(VisualRankImpl.STATIC);

            // remove transformation visually
            PacketSendUtility.broadcastPacketAndReceive(p, new SM_TRANSFORM(p, 0, false));

            // required to update self name
            p.sendPck(new SM_PLAYER_INFO(p, false));
            PacketSendUtility.broadcastPacket(p, new SM_UPDATE_PLAYER_APPEARANCE(p.getObjectId(), p.getEquipment().getEquippedForAppearance()), true);

            if (!p.isGM()) {
                if (Sys.millis() - _lastAnnounceMs > TimeUnit.SECONDS.toMillis(10)) {
                    _lastAnnounceMs = Sys.millis();

                    final String msg = String.format(FORMAT, p.getName(), _template.getId(), instance.playersCount());
                    World.getInstance().doOnAllPlayers(o -> o.sendMsg(msg));
                }
            }

            // trick to fix unvisible objects
            p.getKnownList().clear();
            p.getKnownList().doUpdate();

            info(format(ENTERED, p.getName(), _template.getId()));
        }
    }

    public static class PlayerName extends VisiblePlayerName {
        public final String name;

        public PlayerName(String name) {
            this.name = name;
        }

        @Override
        public String onEvent(@NotNull Tuple2<Player, Player> e) {
            return e._2.isGM() ? e._1.getName() : name;
        }
    }

    private static class IsLegionVisibleImpl extends IsLegionVisible {
        static final IsLegionVisibleImpl STATIC = new IsLegionVisibleImpl();

        @Override
        public Boolean onEvent(@NotNull Player e) {
            return false;
        }
    }

    private static class IsVisuallyTransformedImpl extends IsVisuallyTransformed {
        static final IsVisuallyTransformedImpl STATIC = new IsVisuallyTransformedImpl();

        @Override
        public Boolean onEvent(@NotNull Creature e) {
            TransformModel tm = e.getTransformModel();
            return tm.isActive() && tm.getType() == TransformType.NONE;
        }
    }

    private static class MorfedEquipment extends VisualEquipment {

        private final ItemTemplate tpl;

        private final ItemTemplate helmetTpl;

        public MorfedEquipment(int morfId, int helmetId) {

            tpl = DataManager.ITEM_DATA.getItemTemplate(morfId);

            if (helmetId != 0)
                helmetTpl = DataManager.ITEM_DATA.getItemTemplate(helmetId);
            else
                helmetTpl = null;
        }

        public List<Item> onEvent(@NotNull Player e) {
            // build regular list but replace necessary slot
            List<Item> equippedItems = VisualEquipment.STATIC.onEvent(e);

            List<Item> filtered = new ArrayList<>(equippedItems.size());
            for (Item equippedItem : equippedItems) {
                ItemSlot slot = ItemSlot.getSlotFor(equippedItem.getEquipmentSlot());
                switch (slot) {
                    case MAIN_HAND:
                    case SUB_HAND:
                        filtered.add(equippedItem);
                        break;
                }
            }

            if (helmetTpl != null) {

                Item helmet = new Item(IDFactory.getInstance().nextId(), helmetTpl);
                helmet.setEquipmentSlot(ItemSlot.HELMET.id());
                helmet.setEquipped(true);
                filtered.add(helmet);
            }

            int uid = IDFactory.getInstance().nextId();
            Item item = new Item(uid, tpl);
            item.setEquipped(true);
            item.setEquipmentSlot(ItemSlot.TORSO.id());
            filtered.add(item);

            return filtered;
        }
    }

    public static class VisualRankImpl extends VisualRank {
        public static final VisualRankImpl STATIC = new VisualRankImpl();

        @Override
        public Integer onEvent(@NotNull Player e) {
            return 1;
        }
    }

    private static class OnResurrectBase extends EffectResurrectBaseHandler {
        static final OnResurrectBase STATIC = new OnResurrectBase();

        @Override
        public Boolean onEvent(@NotNull Tuple2<Player, Integer> e) {
            return true;
        }
    }

    private class OnPlayerDie extends PlayerDieHandler {

        protected final int _protectionSkillId;
        protected final int _protectionSkillMs;

        public OnPlayerDie(int protectionSkillId, int protectionSkillMs){
            _protectionSkillId = protectionSkillId;
            _protectionSkillMs = protectionSkillMs;
        }

        @Override
        public Boolean onEvent(@NotNull Tuple3<Player, Creature, Boolean> tt) {
            final Player victim = tt._1;
            Player lastAttacker = (Player) tt._2.getActingCreature();

            int newHp = lastAttacker.getLifeStats().getHpPercentage();
            newHp = Math.min(100, newHp + 20);
            lastAttacker.getLifeStats().setCurrentHpPercent(newHp);
            lastAttacker.getLifeStats().sendHpPacketUpdate();

            victim.getMoveController().abortMove();
            victim.setCasting(null);

            PlayerEffectController ec = victim.getEffectController();
            for (Effect e : ec) {
                SkillTemplate tpl = e.getSkillTemplate();
                int skillId = tpl.getSkillId();

                if (SkillId.RuneCarve.contains(skillId)) {
                    ec.removeEffect(skillId);
                } else {
                    Skill skill = e.getSkill();
                    if (skill != null) {
                        if (skill.getSkillMethod() != SkillMethod.ITEM) {
                            ec.removeEffect(skillId);
                        }
                    } else {
                        ec.removeEffect(skillId);
                    }
                }
            }

            boolean samePlayer = victim.getObjectId().equals(lastAttacker.getObjectId());
            PacketSendUtility.broadcastPacket(victim,
                    new SM_EMOTION(victim, EmotionType.DIE, 0, samePlayer ? 0 : lastAttacker.getObjectId()), true);

            ThreadPoolManager.getInstance().schedule(() -> {
                PlayerReviveService.revive(victim, 100, 100, false, 0);
                victim.getGameStats().updateStatsAndSpeedVisually();
                PacketSendUtility.broadcastPacketAndReceive(victim, new SM_EMOTION(victim, EmotionType.RESURRECT));
                victim.sendPck(new SM_PLAYER_SPAWN(victim));

                List<Pos> positions = getTemplate().getRevivePositions().getPositions();
                Pos pos;
                if (positions.isEmpty()) {
                    pos = new Pos();
                    pos.setMapId(victim.getPosition().getMapId());
                    pos.setX(victim.getPosition().getX());
                    pos.setY(victim.getPosition().getY());
                    pos.setZ(victim.getPosition().getZ());
                } else {
                    pos = Rnd.get(getTemplate().getRevivePositions().getPositions());
                }

                pos.setMapId(getTemplate().getMapId()); // make sure map id is correct

                // teleport
                TeleportService.teleportTo(victim, pos.getMapId(), pos.getX(), pos.getY(), pos.getZ());

                if(_protectionSkillId > 0 && _protectionSkillMs > 0)
                    SkillEngine.getInstance().applyEffectDirectly(_protectionSkillId, victim, victim, _protectionSkillMs);

                victim.getController().startProtectionActiveTask();
            }, 5000L);

            if (!STATIC_AP) {
                PvpService.doReward(victim);
            } else {
                reward(victim);
            }

            return true; // break
        }

        private void reward(Player victim) {
            if (!STATIC_AP) {
                PvpService.doReward(victim);
            } else {
                AggroList victimAggroList = victim.getAggroList();
                int totalDamage = victim.getAggroList().getTotalDamage();
                if (totalDamage == 0) return;

                Collection<AggroInfo> aggroInfos = victimAggroList.getFinalDamageList(true);
                int aggroInfoSize = aggroInfos.size();

                int gainAp = GAIN_AP;
                int premGainAp = PREM_GAIN_AP;
                int vipGainAp = VIP_GAIN_AP;
                int gainGp = GAIN_GP;
                int vipGainGp = VIP_GAIN_GP;

                if (aggroInfoSize > 0) {
                    gainAp = gainAp / aggroInfoSize;
                    premGainAp = premGainAp / aggroInfoSize;
                    vipGainAp = vipGainAp / aggroInfoSize;
                    gainGp = gainGp / aggroInfoSize;
                    vipGainGp = vipGainGp / aggroInfoSize;
                }

                for (AggroInfo aggro : aggroInfos) {
                    Player winner = (Player) aggro.getAttacker();
                    AbyssRank war = winner.getAbyssRank();
                    war.updateKillCounts();

                    if (winner.getLifeStats().isAlreadyDead()) return;
                    if (!MathUtil.isIn3dRange(winner, victim, GroupConfig.GROUP_MAX_DISTANCE)) return;

                    int gainDp = GAIN_DP;
                    byte membership = winner.getPlayerAccount().getMembership();
                    if (membership == 1)
                        gainDp = PREM_GAIN_DP;
                    if (membership >= 2)
                        gainDp = VIP_GAIN_DP;

                    if (!STATIC_DP) {
                        int baseDpReward = StatFunctions.calculatePvpDpGained(victim, war.getRank().getId(), winner.getLevel());
                        gainDp = Math.round(baseDpReward * winner.getRates().getDpPlayerRate() * aggro.getDamage() / totalDamage);
                    }

                    if (gainDp > 0)
                        winner.getLifeStats().increaseDp(gainDp);

                    int actualGainAp = gainAp;
                    int actualGainGp = gainGp;
                    if (membership == 1)
                        actualGainAp = premGainAp;
                    if (membership >= 2) {
                        actualGainAp = vipGainAp;
                        actualGainGp = vipGainGp;
                    }

                    if (actualGainAp != 0)
                        AbyssPointsService.addAp(winner, actualGainAp);
                }

                if (LOSE_AP != 0) {
                    AbyssPointsService.addAp(victim, LOSE_AP);
                }
            }
        }
    }
}
