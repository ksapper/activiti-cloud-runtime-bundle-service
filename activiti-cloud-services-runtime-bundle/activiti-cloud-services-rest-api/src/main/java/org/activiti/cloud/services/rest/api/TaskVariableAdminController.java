package org.activiti.cloud.services.rest.api;

import org.activiti.api.task.model.payloads.CreateTaskVariablePayload;
import org.activiti.api.task.model.payloads.UpdateTaskVariablePayload;
import org.activiti.cloud.api.model.shared.CloudVariableInstance;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = "/admin/v1/tasks/{taskId}/variables",
        produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE})
public interface TaskVariableAdminController {

    @RequestMapping(method = RequestMethod.GET)
    Resources<Resource<CloudVariableInstance>> getVariables(@PathVariable String taskId);

    @RequestMapping(method = RequestMethod.POST)
    ResponseEntity<Void> createVariable(@PathVariable String taskId,
                                        @RequestBody CreateTaskVariablePayload createTaskVariablePayload);

    @RequestMapping(value = "/{variableName}", method = RequestMethod.PUT)
    ResponseEntity<Void> updateVariable(@PathVariable String taskId,
                                        @PathVariable String variableName,
                                        @RequestBody UpdateTaskVariablePayload updateTaskVariablePayload);
}
