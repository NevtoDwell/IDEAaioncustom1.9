package com.ne.gs.network.aion.clientpackets;


import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import java.util.logging.Logger;

/**
 * @author Alex
 */
public class CM_FLY_UNK extends AionClientPacket {

  private static final Logger LOG = Logger.getLogger(CM_FLY_UNK.class.getName());

  @Override
  protected void readImpl() {
  }

  @Override
  protected void runImpl() {
    final Player p = getConnection().getActivePlayer();
    float x = p.getX();
    float y = p.getY();
    //if (x == 50 && y == 50) {
    //PacketSendUtility.sendMessage(p, "" + p.getX() + " " + p.getY() + " " + p.getZ());
    //PacketSendUtility.sendPacket(p, new SM_MOVE(p));
//    ThreadPoolManager.getInstance().schedule(new Runnable() {
//      @Override
//      public void run() {
//        if (p.isFixZ() && !p.isUseTP()) {
//          FixZNpc.getInstance().endFall(p);
//          p.setUseTP(false);
//          PacketSendUtility.sendMessage(p, "T2");
//        }
//      }
//    }, 2000);

  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
