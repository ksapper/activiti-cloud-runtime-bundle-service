/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.services.job.executor;

import org.activiti.cloud.services.events.configuration.RuntimeBundleProperties;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;

public class DefaultMessageBasedJobManagerFactory implements MessageBasedJobManagerFactory {
    
    private final RuntimeBundleProperties runtimeBundleProperties;
    private final JobMessageProducer jobMessageProducer;
    
    public DefaultMessageBasedJobManagerFactory(RuntimeBundleProperties runtimeBundleProperties,
                                                JobMessageProducer jobMessageProducer) {
        this.runtimeBundleProperties = runtimeBundleProperties;
        this.jobMessageProducer = jobMessageProducer;
    }

    @Override
    public MessageBasedJobManager create(ProcessEngineConfigurationImpl processEngineConfiguration) {
        return new MessageBasedJobManager(processEngineConfiguration,
                                          runtimeBundleProperties,
                                          jobMessageProducer);
    }
}
