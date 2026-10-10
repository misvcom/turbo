package com.didiglobal.turbo.engine.executor;


import com.didiglobal.turbo.engine.common.FlowEventType;
import com.didiglobal.turbo.engine.common.RuntimeContext;
import com.didiglobal.turbo.engine.dao.FlowInstanceMappingDAO;
import com.didiglobal.turbo.engine.dao.InstanceDataDAO;
import com.didiglobal.turbo.engine.dao.NodeInstanceDAO;
import com.didiglobal.turbo.engine.dao.NodeInstanceLogDAO;
import com.didiglobal.turbo.engine.dao.ProcessInstanceDAO;
import com.didiglobal.turbo.engine.exception.ProcessException;
import com.didiglobal.turbo.engine.plugin.IdGeneratorPlugin;
import com.didiglobal.turbo.engine.plugin.ListenerPlugin;
import com.didiglobal.turbo.engine.plugin.manager.PluginManager;
import com.didiglobal.turbo.engine.util.IdGenerator;
import com.didiglobal.turbo.engine.util.StrongUuidGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

import java.util.List;
import java.util.Objects;

public abstract class RuntimeExecutor {

    protected static final Logger LOGGER = LoggerFactory.getLogger(RuntimeExecutor.class);

    @Resource
    protected ExecutorFactory executorFactory;

    @Resource
    protected InstanceDataDAO instanceDataDAO;

    @Resource
    protected NodeInstanceDAO nodeInstanceDAO;

    @Resource
    protected ProcessInstanceDAO processInstanceDAO;

    @Resource
    protected NodeInstanceLogDAO nodeInstanceLogDAO;

    private IdGenerator ID_GENERATOR;
    @Resource
    protected FlowInstanceMappingDAO flowInstanceMappingDAO;

    @Resource
    protected PluginManager pluginManager;

    protected String genId() {
        if (null == ID_GENERATOR) {
            List<IdGeneratorPlugin> idGeneratorPlugins = pluginManager.getPluginsFor(IdGeneratorPlugin.class);
            if (!idGeneratorPlugins.isEmpty()) {
                ID_GENERATOR = idGeneratorPlugins.get(0).getIdGenerator();
            } else {
                ID_GENERATOR = new StrongUuidGenerator();
            }
        }
        return ID_GENERATOR.getNextId();
    }

    /**
     * 触发监听器事件
     * <p>
     * 遍历所有已注册的 ListenerPlugin，调用其 onEvent 方法。
     * 监听器异常不会影响主流程执行。
     * </p>
     *
     * @param eventType      事件类型
     * @param runtimeContext 运行时上下文
     */
    protected void fireListenerEvent(FlowEventType eventType, RuntimeContext runtimeContext) {
        if (pluginManager == null) {
            return;
        }
        List<ListenerPlugin> listeners = pluginManager.getPluginsFor(ListenerPlugin.class);
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (ListenerPlugin listener : listeners) {
            try {
                if (Boolean.TRUE.equals(listener.support())) {
                    listener.onEvent(eventType, runtimeContext);
                }
            } catch (Exception e) {
                LOGGER.warn("fireListenerEvent failed.||eventType={}||listener={}", eventType, listener.getName(), e);
            }
        }
    }

    public abstract void execute(RuntimeContext runtimeContext) throws ProcessException;

    public abstract void commit(RuntimeContext runtimeContext) throws ProcessException;

    public abstract void rollback(RuntimeContext runtimeContext) throws ProcessException;

    protected abstract boolean isCompleted(RuntimeContext runtimeContext) throws ProcessException;

    protected boolean isSubFlowInstance(RuntimeContext runtimeContext) throws ProcessException {
        return runtimeContext.getParentRuntimeContext() != null;
    }

    protected abstract RuntimeExecutor getExecuteExecutor(RuntimeContext runtimeContext) throws ProcessException;

    protected abstract RuntimeExecutor getRollbackExecutor(RuntimeContext runtimeContext) throws ProcessException;
}
