package com.didiglobal.turbo.engine.plugin;

import com.didiglobal.turbo.engine.common.FlowEventType;
import com.didiglobal.turbo.engine.common.RuntimeContext;

/**
 * 监听器插件接口
 * <p>
 * 用于监听流程生命周期中的各类事件，如流程启动、节点执行、任务提交等。
 * </p>
 *
 * @author turbo
 */
public interface ListenerPlugin extends Plugin {

    /**
     * 流程事件回调
     *
     * @param eventType      事件类型
     * @param runtimeContext 运行时上下文，包含完整的流程实例信息
     */
    void onEvent(FlowEventType eventType, RuntimeContext runtimeContext);
}
