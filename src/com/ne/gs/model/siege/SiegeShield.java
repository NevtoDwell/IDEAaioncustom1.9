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
package com.ne.gs.model.siege;

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.IActor;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.services.ShieldService;
import com.ne.gs.services.SiegeService;
import com.ne.gs.world.zone.ZoneInstance;
import com.ne.gs.world.zone.handler.ZoneHandler;
import javolution.util.FastMap;
import mw.engines.geo.scene.AionMesh;

/**
 * Shields have material ID 11 in geo.
 * 
 * @author Rolandas
 */
public class SiegeShield implements ZoneHandler {

	FastMap<Integer, IActor> observed = new FastMap<Integer, IActor>();
	private AionMesh geometry;
	private int siegeLocationId;
	private boolean isEnabled = false;

	public SiegeShield(AionMesh geometry) {
		this.geometry = geometry;
	}

	public AionMesh getGeometry() {
		return geometry;
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		if (!(creature instanceof Player))
			return;
		Player player = (Player) creature;
		if (isEnabled || siegeLocationId == 0) {
			FortressLocation loc = SiegeService.getInstance().getFortress(siegeLocationId);
			if (loc == null || loc.getRace() != SiegeRace.getByRace(player.getRace())) {
				ActionObserver actor = ShieldService.getInstance().createShieldObserver(this, creature);
				if (actor instanceof IActor) {
					creature.getObserveController().addObserver(actor);
					observed.put(creature.getObjectId(), (IActor) actor);
				}
			}
		}
	}

	@Override
	public void onLeaveZone(Creature creature, ZoneInstance zone) {
		IActor actor = observed.get(creature.getObjectId());
		if (actor != null) {
			creature.getObserveController().removeObserver((ActionObserver) actor);
			observed.remove(creature.getObjectId());
			actor.abort();
		}
	}

	public void setEnabled(boolean enable) {
		isEnabled = enable;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public int getSiegeLocationId() {
		return siegeLocationId;
	}

	public void setSiegeLocationId(int siegeLocationId) {
		this.siegeLocationId = siegeLocationId;
	}

	@Override
	public String toString() {
		return "LocId=" + siegeLocationId + "; Name=" + geometry.toString() + "; Bounds=" + geometry.getBoundingBox();
	}

}
