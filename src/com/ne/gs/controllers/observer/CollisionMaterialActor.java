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
import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.TaskId;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.materials.MaterialActTime;
import com.ne.gs.model.templates.materials.MaterialSkill;
import com.ne.gs.model.templates.materials.MaterialTemplate;
import com.ne.gs.model.templates.zone.ZoneClassName;
import com.ne.gs.services.WeatherService;
import com.ne.gs.skillengine.SkillEngine;
import com.ne.gs.skillengine.model.Skill;
import com.ne.gs.utils.ThreadPoolManager;
import com.ne.gs.utils.gametime.DayTime;
import com.ne.gs.utils.gametime.GameTime;
import com.ne.gs.utils.gametime.GameTimeManager;
import com.ne.gs.world.zone.ZoneInstance;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.collision.CollidableType;
import mw.engines.geo.collision.CollisionResult;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.math.Vector3f;
import mw.engines.geo.scene.AionMesh;
import mw.engines.geo.scene.AionModel;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Rolandas
 */
public class CollisionMaterialActor extends AbstractCollisionObserver implements IActor {

	private MaterialTemplate actionTemplate;
	private AtomicReference<MaterialSkill> currentSkill = new AtomicReference<MaterialSkill>();

	public CollisionMaterialActor(Creature creature, AionMesh geometry, MaterialTemplate actionTemplate) {
		super(creature, geometry, CollidableType.MATERIAL.getId());
		this.actionTemplate = actionTemplate;
	}

	private MaterialSkill getSkillForTarget(Creature creature) {
		if (creature instanceof Player) {
			Player player = (Player) creature;
			if (player.isProtectionActive())
				return null;
		}

		MaterialSkill foundSkill = null;
		for (MaterialSkill skill : actionTemplate.getSkills()) {
			if (skill.getTarget().isTarget(creature)) {
				foundSkill = skill;
				break;
			}
		}
		if (foundSkill == null)
			return null;

		int weatherCode = -1;
		if (creature.getActiveRegion() == null)
			return null;
		List<ZoneInstance> zones = creature.getActiveRegion().getZones(creature);
		for (ZoneInstance regionZone : zones) {
			if (regionZone.getZoneTemplate().getZoneType() == ZoneClassName.WEATHER) {
				Vector3f center = geometry.getBoundingBox().getCenter();
				if (!regionZone.getAreaTemplate().isInside3D(center.x, center.y, center.z))
					continue;
				int weatherZoneId = DataManager.ZONE_DATA.getWeatherZoneId(regionZone.getZoneTemplate());
				weatherCode = WeatherService.getInstance().getWeatherCode(creature.getWorldId(), weatherZoneId);
				break;
			}
		}

		boolean dependsOnWeather = geometryName.contains("WEATHER");
		// TODO: fix it
		if (dependsOnWeather && weatherCode > 0)
			return null; // not active in any weather (usually, during rain and after rain, not before)

		if (foundSkill.getTime() == null)
			return foundSkill;

		GameTime gameTime = (GameTime) GameTimeManager.getGameTime().clone();
		if (foundSkill.getTime() == MaterialActTime.DAY && weatherCode == 0)
			return foundSkill; // Sunny day, according to client data

		if (gameTime.getDayTime() == DayTime.NIGHT) {
			if (foundSkill.getTime() == MaterialActTime.NIGHT)
				return foundSkill;
		}
		else
			return foundSkill;

		return null;
	}

	@Override
	public void onMoved(CollisionResults collisionResults) {
		if (collisionResults.size() == 0) {
			return;
		}
		else {
			if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
				Player player = (Player) creature;
				if (player.isGM()) {
					CollisionResult result = collisionResults.getClosestCollision();
					player.sendMsg("Entered " + result.getAionMesh().ModelId);
				}
			}
			act();
		}
	}

	@Override
	public void act() {
		final MaterialSkill actSkill = getSkillForTarget(creature);
		if (currentSkill.getAndSet(actSkill) != actSkill) {
			if (actSkill == null)
				return;
			if (creature.getEffectController().hasAbnormalEffect(actSkill.getId())) {
				return;
			}
			Future<?> task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					if (!creature.getEffectController().hasAbnormalEffect(actSkill.getId())) {
						if (GeoDataConfig.GEO_MATERIALS_SHOWDETAILS && creature instanceof Player) {
							Player player = (Player) creature;
							if (player.isGM()) {
								player.sendMsg("Use skill=" + actSkill.getId());
							}
						}
						Skill skill = SkillEngine.getInstance().getSkill(creature, actSkill.getId(), actSkill.getSkillLevel(), creature);
						skill.getEffectedList().add(creature);
						skill.useWithoutPropSkill();
					}
				}
			}, 0, (long) (actSkill.getFrequency() * 1000));
			creature.getController().addTask(TaskId.MATERIAL_ACTION, task);
		}
	}

	@Override
	public void abort() {
		Future<?> existingTask = creature.getController().getTask(TaskId.MATERIAL_ACTION);
		if (existingTask != null) {
			creature.getController().cancelTask(TaskId.MATERIAL_ACTION);
		}
		currentSkill.set(null);
	}

	@Override
	public void died(Creature creature) {
		abort();
	}

	@Override
	public void setEnabled(boolean enable) {
		// TODO Auto-generated method stub	
	};

}
