package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.vip.application.server.business.EngineBusiness;
import fr.insalyon.creatis.vip.application.server.business.SimulationBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // testing framework must recreate a MessageBusiness after each test method
public class SimulationIT {

        @Autowired
        private SimulationBusiness simulationBusiness;

        @Autowired
        private EngineBusiness engineBusiness;

        /*@BeforeEach
        public void setUp() throws BusinessException, GRIDAClientException {
            String engineName = "test engine";
            String engineEndpoint = "test endpoint";
            String engineStatus = "enabled";
            Engine engine = new Engine(engineName, engineEndpoint, engineStatus);
            engineBusiness.add(engine);

            Simulation simulation = new Simulation("pipelineTest1", "3", null, "execId1",
                    "fullName", new GregorianCalendar(2016, 9, 2).getTime(),
                    "Exec test 1", SimulationStatus.Running.toString(), "test engine");
        }


        @Test
        public void testInitialization() {



        }*/


    }
