package com.ne.gs.modules.common;

import java.util.List;
import java.util.Map;
import gnu.trove.map.hash.THashMap;

import com.ne.commons.utils.Actor;
import com.ne.commons.utils.ActorRef;
import com.ne.gs.utils.idfactory.IDFactory;

/**
 * @author hex1r0
 */
public final class PollRegistry extends Actor {

    private static final ActorRef _instance = ActorRef.of(new PollRegistry());
    private final Map<Integer, Query> _polls = new THashMap<>();

    private PollRegistry() {}

    public static ActorRef getInstance() {
        return _instance;
    }

    public static void insert(final Integer pollUid, final Query query) {
        getInstance().tell(new Message<PollRegistry>() {
            @Override
            public void run() {
                actor()._insert(pollUid, query);
            }
        });
    }

    public static void query(final Integer pollUid, final List<Integer> items) {
        getInstance().tell(new Message<PollRegistry>() {
            @Override
            public void run() {
                actor()._query(pollUid, items);
            }
        });
    }

    private void _insert(Integer pollId, Query query) {
        _polls.put(pollId, query);
    }

    private void _query(Integer pollId, List<Integer> items) {
        Query query = _polls.remove(pollId);
        if (query != null) {
            query.setItemIds(items);
            query.run();
            IDFactory.getInstance().releaseId(pollId);
        }
    }

    public static abstract class Query implements Runnable {
        private List<Integer> _itemIds;

        public List<Integer> getItemIds() {
            return _itemIds;
        }

        public void setItemIds(List<Integer> itemIds) {
            _itemIds = itemIds;
        }
    }

}
