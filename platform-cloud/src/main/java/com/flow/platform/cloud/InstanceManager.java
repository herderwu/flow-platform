package com.flow.platform.cloud;

import com.flow.platform.domain.AgentPath;
import com.flow.platform.domain.Instance;
import com.flow.platform.domain.Zone;

import java.util.Collection;
import java.util.List;

/**
 * Created by gy@fir.im on 05/06/2017.
 * Copyright fir.im
 */
public interface InstanceManager {

    /**
     * Create unique instance name
     *
     * @return Instance name
     */
    String instanceName();

    /**
     * Find instance by unique name
     *
     * @param name instance unique name
     * @return instance object
     */
    Instance find(String name);

    /**
     * Find instance by agent path
     */
    Instance find(AgentPath agentPath);

    /**
     * Get all instance list
     *
     * @return instance list
     */
    Collection<Instance> instances();

    /**
     * Async to start instance
     *
     * @return List of instance name
     */
    List<String> batchStartInstance(Zone zone);

    /**
     * Add instance to clean list
     */
    void addToCleanList(Instance instance);

    /**
     * Load instance from provider, check alive duration is over the target and status
     *
     * @param maxAliveDuration target alive duration in seconds
     * @param status instance status
     */
    void cleanFromProvider(long maxAliveDuration, String status);

    /**
     * Delete all instance and cleanAll the cloud
     */
    void cleanAll();

    /**
     * Periodically check and delete cleanAll up instance list
     */
    void cleanInstanceTask();
}
