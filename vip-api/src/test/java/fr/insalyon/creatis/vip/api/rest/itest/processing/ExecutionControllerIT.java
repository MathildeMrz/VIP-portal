/*
 * Copyright and authors: see LICENSE.txt in base repository.
 *
 * This software is a web portal for pipeline execution on distributed systems.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.api.rest.itest.processing;

import fr.insalyon.creatis.vip.api.data.ExecutionTestUtils;
import fr.insalyon.creatis.vip.api.data.PathTestUtils;
import fr.insalyon.creatis.vip.api.exception.ApiException.ApiError;
import fr.insalyon.creatis.vip.api.model.Execution;
import fr.insalyon.creatis.vip.api.rest.config.BaseVIPSpringIT;
import fr.insalyon.creatis.vip.api.rest.config.RestTestUtils;
import fr.insalyon.creatis.vip.application.client.bean.Simulation;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static fr.insalyon.creatis.vip.api.data.AppVersionTestUtils.version42;
import static fr.insalyon.creatis.vip.api.data.ApplicationTestUtils.app1;
import static fr.insalyon.creatis.vip.api.data.ClassesTestUtils.class1;
import static fr.insalyon.creatis.vip.api.data.ExecutionTestUtils.*;
import static fr.insalyon.creatis.vip.api.data.UserTestUtils.baseUser1;
import static fr.insalyon.creatis.vip.api.rest.mockconfig.ApplicationsConfigurator.configureAnApplication;
import static fr.insalyon.creatis.vip.api.rest.mockconfig.ApplicationsConfigurator.configureApplications;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by abonnet on 7/20/16.
 * <p>
 * Test method on platform path
 */
@Disabled
public class ExecutionControllerIT extends BaseVIPSpringIT {

    @Test
    public void shouldListExecutions() throws Exception {
        when(workflowBusiness.getSimulations(baseUser1.getFullName(), null, null, null, null, null))
                .thenReturn(Arrays.asList(simulation1, simulation2));
        when(workflowBusiness.getSimulation(simulation1.getID(), true))
                .thenReturn(simulation1);
        when(workflowBusiness.getSimulation(simulation2.getID(), true))
                .thenReturn(simulation2);
        mockMvc.perform(
                get("/rest/executions").with(baseUser1()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("[*]", hasSize(2)))
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        jsonCorrespondsToExecution(summariseExecution(execution1)),
                        jsonCorrespondsToExecution(summariseExecution(execution2))
                )));
    }

    @Test
    public void shouldCountExecutions() throws Exception {
        when(workflowBusiness.getSimulations(baseUser1.getFullName(), null, null, null, null, null))
                .thenReturn(Arrays.asList(simulation1, simulation2));
        mockMvc.perform(
                get("/rest/executions/count").with(baseUser1()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestTestUtils.TEXT_CONTENT_TYPE_UTF8))
                .andExpect(content().string("2"));
    }

    @Test
    public void shouldGetExecution1() throws Exception {
        when(workflowBusiness.getSimulation(simulation1.getID()))
                .thenReturn(simulation1);
        when(workflowBusiness.getSimulation(simulation1.getID(), true))
                .thenReturn(simulation1);
        when(workflowBusiness.getInputData(
                 eq(simulation1.getID()), eq(baseUser1.getFolder())))
            .thenReturn(simulation1InData);
        when(workflowBusiness.getOutputData(
                 eq(simulation1.getID()), eq(baseUser1.getFolder())))
            .thenReturn(simulation1OutData);
        mockMvc.perform(
                get("/rest/executions/" + simulation1.getID()).with(baseUser1()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$",
                        jsonCorrespondsToExecution(execution1)
                ));
    }

    @Test
    public void shouldGetExecution2() throws Exception {
        when(workflowBusiness.getSimulation(simulation2.getID()))
                .thenReturn(simulation2);
        when(workflowBusiness.getSimulation(simulation2.getID(), true))
                .thenReturn(simulation2);
        when(workflowBusiness.getInputData(
                 eq(simulation2.getID()), eq(baseUser1.getFolder())))
            .thenReturn(simulation2InData);
        when(workflowBusiness.getOutputData(
                 eq(simulation2.getID()), eq(baseUser1.getFolder())))
            .thenReturn(simulation2OutData);
        mockMvc.perform(
                get("/rest/executions/" + simulation2.getID()).with(baseUser1()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$",
                        jsonCorrespondsToExecution(execution2)
                ));
    }

    @Test
    public void shouldGetErrorWhenGettingUnknownExecution() throws Exception {
        when(workflowBusiness.getSimulation(anyString()))
                .thenThrow(new BusinessException("no test execution"));
            mockMvc.perform(
                    get("/rest/executions/WrongExecId").with(baseUser1()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(jsonPath("$.errorCode").value(ApiError.GENERIC_API_ERROR.getCode()));
        }

    @Test
    public void shouldUpdateExecution1() throws Exception {
        String newName = "Exec test 1 - modified";
        Execution modifiedExecution =
                ExecutionTestUtils.copyExecutionWithNewName(execution1, newName);
        Simulation modifiedSimulation =
                ExecutionTestUtils.copySimulationWithNewName(simulation1, newName);
        when(workflowBusiness.getSimulation(simulation1.getID(), true))
                .thenReturn(modifiedSimulation).thenThrow(new RuntimeException());
        when(workflowBusiness.getSimulation(simulation1.getID()))
                .thenReturn(simulation1).thenThrow(new RuntimeException());
        when(workflowBusiness.getInputData(
                 eq(simulation1.getID()), eq(baseUser1.getFolder())))
            .thenReturn(simulation1InData);
        when(workflowBusiness.getOutputData(
                 eq(simulation1.getID()), eq(baseUser1.getFolder())))
            .thenReturn(simulation1OutData);
        mockMvc.perform(
                put("/rest/executions/" + simulation1.getID())
                        .contentType("application/json")
                        .content(getResourceAsString("jsonObjects/execution1-name-updated.json"))
                        .with(baseUser1()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$",
                        jsonCorrespondsToExecution(modifiedExecution)
                ));
        verify(workflowBusiness).updateDescription(simulation1.getID(), newName);
    }

    @Test
    public void testInitExecution() throws Exception {
        // configure pipeline access right
        configureApplications(
                this, baseUser1, Collections.singletonList(class1),
                app1, version42);
        // configure pipeline input
        configureAnApplication(this, baseUser1, app1, version42, 0, 1);
        // configure lauch
        when(workflowBusiness.launch(
                 eq(baseUser1), anyList(), anyMap(), eq(app1.getName()),
                 eq(version42.getVersion()), eq(class1.getName()),
                 eq(execution1.getName())))
                .thenReturn(execution1.getIdentifier());
        // configure returne execution
        when(workflowBusiness.getSimulation(execution1.getIdentifier(), true))
                .thenReturn(simulation1);
        when(workflowBusiness.getInputData(
                 eq(simulation1.getID()), eq(baseUser1.getFolder())))
            .thenReturn(simulation1InData);
        when(workflowBusiness.getOutputData(
                 eq(simulation1.getID()), eq(baseUser1.getFolder())))
            .thenReturn(simulation1OutData);
        // misc config
        when(configurationBusiness.getUserGroups(eq(baseUser1.getEmail())))
                .thenReturn(new HashMap<>());
        mockMvc.perform(
                post("/rest/executions").contentType("application/json")
                        .content(getResourceAsString("jsonObjects/execution1.json"))
                        .with(baseUser1()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$",
                        jsonCorrespondsToExecution(execution1)
                ));
        ArgumentCaptor<Map> inputCaptor =
                ArgumentCaptor.forClass(Map.class);
        verify(workflowBusiness).launch(
            eq(baseUser1), anyList(), inputCaptor.capture(), eq(app1.getName()),
            eq(version42.getVersion()), eq(class1.getName()),
            eq(execution1.getName()));
        assertEquals(inputCaptor.getValue().size(), 2);
        MatcherAssert.<Map<?, ?>>assertThat(inputCaptor.getValue(), allOf(
                hasEntry("param 1", "test text"),
                hasEntry("param 2", "/path/test")));
    }

    @Test
    public void testNotInitExecutionMissingField() throws Exception {
        mockMvc.perform(
            post("/rest/executions").contentType("application/json")
                    .content("{}")
                    .with(baseUser1())
        ).andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.errorCode")
                .value(ApiError.INPUT_FIELD_NOT_VALID.getCode())
        );
    }

    @Test
    public void shouldGetExecution2Results() throws Exception {
        when(workflowBusiness.getSimulation(simulation2.getID()))
                .thenReturn(simulation2);
        when(workflowBusiness.getOutputData(
                 eq(simulation2.getID()), eq(baseUser1.getFolder())))
            .thenReturn(simulation2OutData);
        String resultPath = simulation2OutData.get(0).getPath();
        when(lfcBusiness.exists(eq(baseUser1), eq(resultPath)))
                .thenReturn(true);
        when(lfcBusiness.listDir(eq(baseUser1), eq(resultPath), eq(true)))
                .thenReturn(Collections.singletonList(PathTestUtils.testFile1));
        when(transferPoolBusiness.downloadFile(any(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(
                get("/rest/executions/" + simulation2.getID() + "/results").with(baseUser1()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[*]", hasSize(1)))
                .andExpect(jsonPath("$[0]",
                        PathTestUtils.jsonCorrespondsToPath(PathTestUtils.testFile1PathProperties)));
    }

    @Test
    public void shouldGetExecution2Stdout() throws Exception {
        when(workflowBusiness.getSimulation(simulation2.getID()))
                .thenReturn(simulation2);
        String testOutput = "blablabla";
        when(simulationBusiness.readFile(simulation2.getID(), "", "workflow", ".out"))
                .thenReturn(testOutput);
        mockMvc.perform(
                get("/rest/executions/" + simulation2.getID() + "/stdout").with(baseUser1()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestTestUtils.TEXT_CONTENT_TYPE_UTF8))
                .andExpect(content().string(testOutput));
    }

    @Test
    public void shouldGetExecution2Stderr() throws Exception {
        when(workflowBusiness.getSimulation(simulation2.getID()))
                .thenReturn(simulation2);
        String testOutput = "blablabla";
        when(simulationBusiness.readFile(simulation2.getID(), "", "workflow", ".err"))
                .thenReturn(testOutput);
        mockMvc.perform(
                get("/rest/executions/" + simulation2.getID() + "/stderr").with(baseUser1()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(RestTestUtils.TEXT_CONTENT_TYPE_UTF8))
                .andExpect(content().string(testOutput));
    }

    @Test
    public void testPlayExecutionIsNotImplemented() throws Exception {
        mockMvc.perform(
                put("/rest/executions/" + simulation1.getID() + "/play")
                        .with(baseUser1()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errorCode").value(ApiError.NOT_IMPLEMENTED.getCode()));
    }

    @Test
    public void shouldKillExecution2() throws Exception {
        when(workflowBusiness.getSimulation(simulation2.getID()))
                .thenReturn(simulation2);
        mockMvc.perform(
                put("/rest/executions/" + simulation2.getID() + "/kill").with(baseUser1()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(workflowBusiness).kill(simulation2.getID());

    }

    @Test
    public void shouldDeleteWithFilesExecution2() throws Exception {
        when(workflowBusiness.getSimulation(simulation2.getID()))
                .thenReturn(simulation2);
        mockMvc.perform(
                put("/rest/executions/" + simulation2.getID() + "/delete")
                        .contentType("application/json")
                        .content("{\"deleteFiles\":true}")
                        .with(baseUser1()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(workflowBusiness).clean(simulation2.getID(), baseUser1.getEmail(), true);

    }

    @Test
    public void shouldDeleteWithoutExecution2() throws Exception {
        when(workflowBusiness.getSimulation(simulation2.getID()))
                .thenReturn(simulation2);
        mockMvc.perform(
                put("/rest/executions/" + simulation2.getID() + "/delete")
                        .contentType("application/json")
                        .content("{\"deleteFiles\":false}")
                        .with(baseUser1()))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(workflowBusiness).clean(simulation2.getID(), baseUser1.getEmail(), false);

    }

    @Test
    public void shouldReturn400() throws Exception {
        mockMvc.perform(
                put("/rest/executions/whynotthisid").with(baseUser1()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errorCode").value(ApiError.GENERIC_API_ERROR.getCode()));
    }

    @Test
    public void shouldReturn500() throws Exception {
        when(workflowBusiness.getSimulations(baseUser1.getFullName(), null, null, null, null, null))
                .thenThrow(new RuntimeException("test exception"));
        mockMvc.perform(
                get("/rest/executions").with(baseUser1()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errorCode").value(ApiError.GENERIC_API_ERROR.getCode()));
    }
}
