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
package com.ne.gs.model.templates.zone;

import com.ne.gs.dataholders.DataManager;
import mw.engines.geo.GeoEngine;
import mw.engines.geo.bounding.BoundingBox;
import mw.engines.geo.math.Vector3f;
import mw.engines.geo.scene.AionMesh;

/**
 * @author Rolandas
 */
public class MaterialZoneTemplate extends ZoneTemplate {

    @Deprecated
    public MaterialZoneTemplate(AionMesh geometry, int mapId) {
        mapid = mapId;
        flags = DataManager.WORLD_MAPS_DATA.getTemplate(mapId).getFlags();

        String geomName = GeoEngine.getModelAlias(geometry);

        //what the fuck?! mark as deprescated too...
        setXmlName(geomName + "_" + mapId);

        BoundingBox box = (BoundingBox) geometry.getBoundingBox();
        Vector3f center = box.getCenter();
        // 1: don't use polygons for small areas, they are bugged in Java API
        // 2: any proofs?
        if (geomName.contains("CYLINDER") ||
                geomName.contains("CONE") ||
                geomName.contains("H_COLUME")) {
            areaType = AreaType.CYLINDER;
            cylinder = new Cylinder(center.x, center.y, Math.max(box.getXExtent(), box.getYExtent() + 1), center.z + box.getZExtent() + 1, center.z
                    - box.getZExtent() - 1);
        } else if (geomName.contains("SEMISPHERE")) { //toString, but leave it now as error
            areaType = AreaType.SEMISPHERE;
            semisphere = new Semisphere(center.x, center.y, center.z, Math.max(Math.max(box.getXExtent(), box.getYExtent()), box.getZExtent()) + 1);
        } else {
            areaType = AreaType.SPHERE;
            sphere = new Sphere(center.x, center.y, center.z, Math.max(Math.max(box.getXExtent(), box.getYExtent()), box.getZExtent()) + 1);
        }
    }

}
