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

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.utils.ThreadPoolManager;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.collision.CollisionResults;
import mw.engines.geo.math.Ray;
import mw.engines.geo.math.Vector3f;
import mw.engines.geo.scene.AionMesh;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author MrPoke
 * @moved Rolandas
 */
public abstract class AbstractCollisionObserver extends ActionObserver {

	protected Creature creature;
	protected Vector3f oldPos;
	protected AionMesh geometry;
	protected String geometryName;
	protected byte intentions;
	private AtomicBoolean isRunning = new AtomicBoolean();

	public AbstractCollisionObserver(Creature creature, AionMesh geometry, byte intentions) {
		super(ObserverType.MOVE_OR_DIE);
		this.creature = creature;
		this.geometry = geometry;
		this.oldPos = new Vector3f(creature.getX(), creature.getY(), creature.getZ());
		this.intentions = intentions;

		this.geometryName = GeoEngine.getModelAlias(geometry);
	}

	@Override
	public void moved() {
		if (!isRunning.getAndSet(true)) {
			ThreadPoolManager.getInstance().execute(new Runnable() {

				@Override
				public void run() {
					try {
						Vector3f pos = new Vector3f(creature.getX(), creature.getY(), creature.getZ());
						Vector3f dir = oldPos.clone();
						Float limit = pos.distance(dir);
						dir.subtractLocal(pos).normalizeLocal();
						Ray r = new Ray(pos, dir);
						r.setLimit(limit);
						CollisionResults results = new CollisionResults(true, creature.getInstanceId());
						geometry.collideWith(r, results);
						onMoved(results);
						oldPos = pos;
					}
					finally {
						isRunning.set(false);
					}
				}
			});
		}
	}

	public abstract void onMoved(CollisionResults result);
}
