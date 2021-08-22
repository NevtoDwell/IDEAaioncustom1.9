/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager;

import com.ne.commons.utils.concurrent.ExecuteWrapper;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author NB4L1
 */
public abstract class FIFORunnableQueue<T extends Runnable> extends FIFOSimpleExecutableQueue<T> {

    @Override
    protected final void removeAndExecuteFirst() {
        ExecuteWrapper.execute(removeFirst(), ThreadPoolManager.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING);
    }
}
