//package com.ne.gs.model.conds;
//
//import com.ne.commons.annotations.NotNull;
//import com.ne.commons.func.tuple.Tuple2;
//import com.ne.commons.utils.SimpleCond;
//import com.ne.gs.model.gameobjects.player.Player;
//import com.ne.gs.model.templates.zone.ZoneType;
//
///**
// * @author hex1r0
// */
//public abstract class IsEnemyCond extends SimpleCond<Tuple2<Player, Player>> {
//
//    public static final IsEnemyCond TRUE = new IsEnemyCond() {
//        @Override
//        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
//            return true;
//        }
//    };
//
//    public static final IsEnemyCond STATIC = new IsEnemyCond() {
//        @Override
//        public Boolean onEvent(@NotNull Tuple2<Player, Player> e) {
//            Player owner = e._1;
//            Player opponent = e._2;
//
//            if (owner.getAdminEnmity() > 1 || opponent.getAdminEnmity() > 1) {
//                return true;
//            }
//
////            if (owner.isDueling(opponent)) {
////                return true;
////            }
//
//            if (owner.isInsideZoneType(ZoneType.NEUTRAL) || opponent.isInsideZoneType(ZoneType.NEUTRAL)) {
//                return true;
//            }
//
//            if (owner.isPvP(opponent)) {
//                return true;
//            }
//
//            return owner.isInsideZoneType(ZoneType.PVP) && opponent.isInsideZoneType(ZoneType.PVP)
//                && !owner.isInSameTeam(opponent)
//                && opponent.getWorldId() != 600020000 && opponent.getWorldId() != 600030000;
//        }
//    };
//
//    @NotNull
//    @Override
//    public final String getType() {
//        return IsEnemyCond.class.getName();
//    }
//}
