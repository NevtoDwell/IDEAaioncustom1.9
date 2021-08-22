package com.ne.gs.modules.pvpevent;

import com.ne.gs.modules.common.PosList;
import com.ne.gs.modules.common.PropertyDesc;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author hex1r0
 */
public class Config {
    public final long ROUND_TIME_MS, APPLY_TIME_MS, PREPARATION_TIME_MS, EVENT_COOLDOWN_MS;
    public final int WIN_LIMIT;
    public final int MEMBER_COUNT;

    public Config(PvpLocTemplate tpl) {
        ROUND_TIME_MS = MINUTES.toMillis(PropertyDesc.of(tpl, P_ROUND_TIME, "5"));
        APPLY_TIME_MS = MINUTES.toMillis(PropertyDesc.of(tpl, P_APPLY_TIME, "10"));
        PREPARATION_TIME_MS = SECONDS.toMillis(PropertyDesc.of(tpl, P_PREPARATION_TIME, "15"));
        EVENT_COOLDOWN_MS = MINUTES.toMillis(PropertyDesc.of(tpl, P_EVENT_COOLDOWN, "15"));
        WIN_LIMIT = PropertyDesc.of(tpl, P_WIN_LIMIT, "2");
        int count = 0;
        for (PosList posList : tpl.getStartPositions()) {
           count += posList.getPositions().size();
        }
        MEMBER_COUNT = count;
    }

    private final static PropertyDesc<Integer> P_ROUND_TIME = new PropertyDesc<>("roundTimeMinutes", Integer.TYPE);
    private final static PropertyDesc<Integer> P_APPLY_TIME = new PropertyDesc<>("applyTimeMinutes", Integer.TYPE);
    private final static PropertyDesc<Integer> P_PREPARATION_TIME = new PropertyDesc<>("prepareTimeSeconds", Integer.TYPE);
    private final static PropertyDesc<Integer> P_EVENT_COOLDOWN = new PropertyDesc<>("eventCooldownMinutes", Integer.TYPE);
    private final static PropertyDesc<Integer> P_WIN_LIMIT = new PropertyDesc<>("winLimit", Integer.TYPE);
}
