package com.thingverse.common.env.postprocessor;

import java.util.concurrent.atomic.AtomicInteger;

public interface OnceOnlyEnvProcessor {

    AtomicInteger executionCount();
}
