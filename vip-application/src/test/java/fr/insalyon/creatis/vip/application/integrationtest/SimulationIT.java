package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.application.client.bean.Simulation;
import fr.insalyon.creatis.vip.application.client.view.monitor.SimulationStatus;
import fr.insalyon.creatis.vip.application.server.business.SimulationBusiness;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.GregorianCalendar;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
// testing framework must recreate a MessageBusiness after each test method
public class SimulationIT {

    @Autowired
    private SimulationBusiness simulationBusiness;

    @BeforeEach
    public void setUp() throws BusinessException, GRIDAClientException, BusinessException {

        Simulation simulation = new Simulation("pipelineTest1", "3", null, "execId1",
                "fullName", new GregorianCalendar(2016, 9, 2).getTime(),
                "Exec test 1", SimulationStatus.Running.toString(), "test engine");
    }


    @Test
    public void testInitialization() {


    }


}
