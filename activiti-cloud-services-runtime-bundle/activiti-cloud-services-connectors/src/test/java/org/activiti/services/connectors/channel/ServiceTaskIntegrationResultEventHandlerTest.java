/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
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

package org.activiti.services.connectors.channel;

import java.util.Collections;
import java.util.Map;

import org.activiti.cloud.services.events.ProcessEngineChannels;
import org.activiti.cloud.services.events.configuration.ApplicationProperties;
import org.activiti.cloud.services.events.integration.IntegrationResultReceivedEvent;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.integration.IntegrationContextEntityImpl;
import org.activiti.engine.integration.IntegrationContextService;
import org.activiti.services.connectors.model.IntegrationResultEvent;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServiceTaskIntegrationResultEventHandlerTest {


    @InjectMocks
    private ServiceTaskIntegrationResultEventHandler handler;

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private IntegrationContextService integrationContextService;

    @Mock
    private ProcessEngineChannels channels;

    @Mock
    private MessageChannel auditChannel;

    @Mock
    private ApplicationProperties applicationProperties;

    @Captor
    private ArgumentCaptor<Message<IntegrationResultReceivedEvent[]>> messageCaptor;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(channels.auditProducer()).thenReturn(auditChannel);
    }

    @Test
    public void receiveShouldTriggerTheExecutionDeleteTheRelatedIntegrationContextAndSendAuditEvent() throws Exception {
        //given
        String executionId = "execId";
        String entityId = "entityId";
        String procInstId = "procInstId";
        String procDefId = "procDefId";

        IntegrationContextEntityImpl integrationContext = new IntegrationContextEntityImpl();
        integrationContext.setExecutionId(executionId);
        integrationContext.setId(entityId);
        integrationContext.setProcessInstanceId(procInstId);
        integrationContext.setProcessDefinitionId(procDefId);

        given(integrationContextService.findIntegrationContextByExecutionId(executionId)).willReturn(integrationContext);
        Map<String, Object> variables = Collections.singletonMap("var1",
                                                                 "v");

        given(applicationProperties.getName()).willReturn("myApp");

        IntegrationResultEvent integrationResultEvent = new IntegrationResultEvent(executionId,
                                                                                   variables);

        //when
        handler.receive(integrationResultEvent);

        //then
        verify(integrationContextService).deleteIntegrationContext(integrationContext);
        verify(runtimeService).trigger(executionId,
                                       variables);

        verify(auditChannel).send(messageCaptor.capture());
        Message<IntegrationResultReceivedEvent[]> message = messageCaptor.getValue();
        assertThat(message.getPayload()).hasSize(1);
        IntegrationResultReceivedEvent integrationResultReceivedEvent = message.getPayload()[0];
        assertThat(integrationResultReceivedEvent.getIntegrationContextId()).isEqualTo(entityId);
        assertThat(integrationResultReceivedEvent.getApplicationName()).isEqualTo("myApp");
        assertThat(integrationResultReceivedEvent.getExecutionId()).isEqualTo(executionId);
        assertThat(integrationResultReceivedEvent.getProcessInstanceId()).isEqualTo(procInstId);
        assertThat(integrationResultReceivedEvent.getProcessDefinitionId()).isEqualTo(procDefId);
    }

    @Test
    public void receiveShouldThrowAnExceptionWhenNoRelatedIntegrationContextIsFound() throws Exception {
        //given
        String executionId = "execId";

        given(integrationContextService.findIntegrationContextByExecutionId(executionId)).willReturn(null);
        IntegrationResultEvent integrationResultEvent = new IntegrationResultEvent(executionId,
                                                                                   null);

        //then
        Assertions.assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                //when
                () -> handler.receive(integrationResultEvent)
        ).withMessageContaining("No task is waiting for integration result with execution id");
    }
}