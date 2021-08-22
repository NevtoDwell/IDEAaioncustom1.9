package mw.engines.geo.collision;

import java.util.EnumSet;

public enum CollidableType {

    NONE(0), //all known objects
    PHYSICAL(1), // Physical collision
    MATERIAL(1 << 1), // Mesh materials with skills
    SKILL(1 << 2), // Skill obstacles
    WALK(1 << 3), // Walk/NoWalk obstacles
    DOOR(1 << 4), // Doors which have a state opened/closed
    EVENT(1 << 5), // Appear on event only
    MOVEABLE(1 << 6), // Ships, shugo boxes
    // This is used for nodes only, means they allow to enumerate their child geometries
    // Nodes which do not specify it won't let their children enumerated for collisions,
    // to speed up processing
    ALL(PHYSICAL.getId() | MATERIAL.getId() | SKILL.getId() | WALK.getId() | DOOR.getId() | EVENT.getId() | MOVEABLE.getId());

    private byte id;

    private CollidableType(int id) {
        this.id = (byte) id;
    }

    public byte getId() {
        return id;
    }

    public static EnumSet<CollidableType> getFlagsFormValue(int value) {
        EnumSet<CollidableType> result = EnumSet.noneOf(CollidableType.class);
        for (CollidableType m : CollidableType.values()) {
            if ((value & m.getId()) == m.getId()) {
                if (m == NONE || m == ALL) {
                    continue;
                }
                result.add(m);
            }
        }
        return result;
    }

    public static String toString(int value) {
        String str = "";
        for (CollidableType m : CollidableType.values()) {
            if (m == NONE || m == ALL) {
                continue;
            }
            if ((value & m.getId()) == m.getId()) {
                str += m.toString();
                str += ", ";
            }
        }
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 2);
        }
        return str;
    }
}
