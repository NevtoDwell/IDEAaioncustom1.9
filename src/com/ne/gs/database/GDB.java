package com.ne.gs.database;

import com.ne.gs.database.dao.*;
import com.ne.gs.database.mysql5.*;
import mw.utils.custom.IoCContainer;

public class GDB extends IoCContainer {

    public enum DbKind {
        MySQL5
    }

    private static final GDB INSTANCE = new GDB(DbKind.MySQL5);

    public GDB(DbKind databaseKind) {

        switch (databaseKind){
            case MySQL5:
                loadMysql();
                break;
                default:
                    throw new Error("Database kind " + databaseKind + " not supported");
        }
    }

    public static GDB getInstance(){return INSTANCE;}

    public static <T> T get(Class<T> clazz){

        T instance = INSTANCE.resolve(clazz);
        if(instance == null)
            throw new Error("No any instance of database class assotiated with " + clazz.getName() + " class");

        return instance;
    }

    private void loadMysql(){
        try {
            bind(AbyssRankDAO.class).to(MySQL5AbyssRankDAO.class);
            bind(AnnouncementsDAO.class).to(MySQL5Announcements.class);
            bind(BlockListDAO.class).to(MySQL5BlockListDAO.class);
            bind(BrokerDAO.class).to(MySQL5BrokerDAO.class);
            bind(CraftCooldownsDAO.class).to(MySQL5CraftCooldownsDAO.class);
            bind(FriendListDAO.class).to(MySQL5FriendListDAO.class);
            bind(InGameShopDAO.class).to(MySQL5inGameShopDAO.class);
            bind(InGameShopLogDAO.class).to(MySQL5InGameShopLogDAO.class);
            bind(InventoryDAO.class).to(MySQL5InventoryDAO.class);
            bind(ItemCooldownsDAO.class).to(MySQL5ItemCooldownsDAO.class);
            bind(ItemStoneListDAO.class).to(MySQL5ItemStoneListDAO.class);
            bind(LegionDAO.class).to(MySQL5LegionDAO.class);
            bind(LegionMemberDAO.class).to(MySQL5LegionMemberDAO.class);
            bind(MailDAO.class).to(MySQL5MailDAO.class);
            bind(MotionDAO.class).to(MySQL5MotionDAO.class);
            bind(OldNamesDAO.class).to(MySQL5OldNamesDAO.class);
            bind(PetitionDAO.class).to(MySQL5PetitionDAO.class);
            bind(PlayerAppearanceDAO.class).to(MySQL5PlayerAppearanceDAO.class);
            bind(PlayerBindPointDAO.class).to(MySQL5PlayerBindPointDAO.class);
            bind(PlayerCooldownsDAO.class).to(MySQL5PlayerCooldownsDAO.class);
            bind(PlayerDAO.class).to(MySQL5PlayerDAO.class);
            bind(PlayerEffectsDAO.class).to(MySQL5PlayerEffectsDAO.class);
            bind(PlayerEmotionListDAO.class).to(MySQL5PlayerEmotionListDAO.class);
            bind(PlayerLifeStatsDAO.class).to(MySQL5PlayerLifeStatsDAO.class);
            bind(PlayerMacrossesDAO.class).to(MySQL5PlayerMacrossesDAO.class);
            bind(PlayerNpcFactionsDAO.class).to(MySQL5PlayerNpcFactionsDAO.class);
            bind(PlayerPasskeyDAO.class).to(MySQL5PlayerPasskeyDAO.class);
            bind(PlayerPetsDAO.class).to(MySQL5PlayerPetsDAO.class);
            bind(PlayerPunishmentsDAO.class).to(MySQL5PlayerPunishmentsDAO.class);
            bind(PlayerRecipesDAO.class).to(MySQL5PlayerRecipesDAO.class);
            bind(PlayerSettingsDAO.class).to(MySQL5PlayerSettingsDAO.class);
            bind(PlayerSkillListDAO.class).to(MySQL5PlayerSkillListDAO.class);
            bind(PlayerTitleListDAO.class).to(MySQL5PlayerTitleListDAO.class);
            bind(PortalCooldownsDAO.class).to(MySQL5PortalCooldownsDAO.class);
            bind(RewardServiceDAO.class).to(MySQL5RewardServiceDAO.class);
            bind(ServerVariablesDAO.class).to(MySQL5ServerVariablesDAO.class);
            bind(SiegeDAO.class).to(MySQL5SiegeDAO.class);
            bind(SurveyControllerDAO.class).to(MySQL5SurveyControllerDAO.class);
            bind(TaskFromDBDAO.class).to(MySQL5TaskFromDBDAO.class);
            bind(WeddingDAO.class).to(MySQL5WeddingDAO.class);

        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
