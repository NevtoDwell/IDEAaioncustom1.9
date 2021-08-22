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

package com.ne.gs.world.zone.handler;

import com.ne.gs.controllers.observer.ActionObserver;
import com.ne.gs.controllers.observer.CollisionMaterialActor;
import com.ne.gs.controllers.observer.IActor;
import com.ne.gs.model.Race;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.templates.materials.MaterialSkill;
import com.ne.gs.model.templates.materials.MaterialTemplate;
import com.ne.gs.world.zone.ZoneInstance;
import javolution.util.FastMap;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.scene.AionMesh;

/**
 * @author Rolandas
 */
public class MaterialZoneHandler implements ZoneHandler {

	private FastMap<Integer, IActor> observed = new FastMap<Integer, IActor>();

	private AionMesh geometry;
	private MaterialTemplate template;
	private boolean actOnEnter = false;
	private Race ownerRace = Race.NONE;

	@Deprecated
	public MaterialZoneHandler(AionMesh geometry, MaterialTemplate template) {
		this.geometry = geometry;
		this.template = template;

		//mark now as deprescated...
		String name = GeoEngine.getModelAlias(geometry);

		if (name.contains("FIRE_BOX") || name.contains("FIRE_SEMISPHERE") || name.contains("FIREPOT") ||
				name.contains("FIRE_CYLINDER") || name.contains("FIRE_CONE") || name.startsWith("BU_H_CENTERHALL"))
			actOnEnter = true;
		if (name.startsWith("BU_AB_DARKSP"))
			ownerRace = Race.ASMODIANS;
		else if (name.startsWith("BU_AB_LIGHTSP"))
			ownerRace = Race.ELYOS;
	}

	@Override
	public void onEnterZone(Creature creature, ZoneInstance zone) {
		if (ownerRace == creature.getRace())
			return;
		MaterialSkill foundSkill = null;
		for (MaterialSkill skill : template.getSkills()) {
			if (skill.getTarget().isTarget(creature)) {
				foundSkill = skill;
				break;
			}
		}
		if (foundSkill == null)
			return;
		CollisionMaterialActor actor = new CollisionMaterialActor(creature, geometry, template);
		creature.getObserveController().addObserver(actor);
		observed.put(creature.getObjectId(), actor);
		if (actOnEnter)
			actor.act();
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

}
