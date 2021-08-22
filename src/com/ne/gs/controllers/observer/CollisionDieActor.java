/*
 * This file is part of aion-lightning <aion-lightning.com>.
 *
 *  aion-lightning is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  aion-lightning is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.ne.gs.controllers.observer;

import com.ne.gs.configs.main.GeoDataConfig;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.PacketSendUtility;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.collision.CollisionResult;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.scene.AionMesh;

/**
 * @author Rolandas
 */
public class CollisionDieActor extends AbstractCollisionObserver implements IActor {

	private boolean isEnabled = true;
	
	public CollisionDieActor(Creature creature, AionMesh geometry) {
		super(creature, geometry, CollidableType.MATERIAL.getId());
	}
	
	@Override
	public void setEnabled(boolean enable) {
		isEnabled = enable;
	}

	@Override
	public void onMoved(CollisionResults collisionResults) {
		if (isEnabled && collisionResults.size() != 0) {
			if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
				Player player = (Player) creature;
				if (player.isGM()) {
					CollisionResult result = collisionResults.getClosestCollision();
					player.sendMsg("Entered " + result.toString());
				}
			}
			act();
		}
	}

	@Override
	public void act() {
		if (isEnabled)
			creature.getController().die();
	}

	@Override
	public void abort() {
		// Nothing to do
	}

}
