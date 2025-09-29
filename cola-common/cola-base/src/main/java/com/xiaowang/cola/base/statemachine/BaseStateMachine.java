package com.xiaowang.cola.base.statemachine;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.xiaowang.cola.base.exception.BizException;

import java.util.Map;

import static com.xiaowang.cola.base.exception.BizErrorCode.STATE_MACHINE_TRANSITION_FAILED;


/**
 * @author cola
 */
public class BaseStateMachine<STATE, EVENT> implements StateMachine<STATE, EVENT> {
    private Map<String, STATE> stateTransitions = Maps.newHashMap();

    protected void putTransition(STATE origin, EVENT event, STATE target) {
        stateTransitions.put(Joiner.on("_").join(origin, event), target);
    }

    @Override
    public STATE transition(STATE state, EVENT event) {
        STATE target = stateTransitions.get(Joiner.on("_").join(state, event));
        if (target == null) {
            throw new BizException("state = " + state + " , event = " + event, STATE_MACHINE_TRANSITION_FAILED);
        }
        return target;
    }
}
