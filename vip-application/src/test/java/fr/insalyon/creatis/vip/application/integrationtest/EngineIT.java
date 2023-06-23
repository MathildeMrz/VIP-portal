package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.application.client.bean.Engine;
import fr.insalyon.creatis.vip.application.server.business.EngineBusiness;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // testing framework must recreate a MessageBusiness after each test method
public class EngineIT extends BaseSpringIT {

    @Autowired
    private EngineBusiness engineBusiness;

    @BeforeEach
    public void setUp() throws BusinessException, GRIDAClientException {
        String engineName = "test engine";
        String engineEndpoint = "test endpoint";
        String engineStatus = "enabled";
        Engine engine = new Engine(engineName, engineEndpoint, engineStatus);
        engineBusiness.add(engine);
    }
    @Test
    public void testInitialization() throws BusinessException {
        Assertions.assertEquals(1, engineBusiness.get().size());
        Assertions.assertEquals("test engine", engineBusiness.get().get(0).getName());
        Assertions.assertEquals("test endpoint", engineBusiness.get().get(0).getEndpoint());
        Assertions.assertEquals("enabled", engineBusiness.get().get(0).getStatus());
    }

    @Test
    public void testRemove() throws BusinessException, DAOException {
        engineBusiness.remove("test engine");
        Assertions.assertEquals(0, engineBusiness.get().size());
    }

    @Test
    public void testCatchRemoveInexistingEngine() {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        engineBusiness.remove("inexisting engine")
        );
        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no engine registered with the name : inexisting engine"));

    }

    @Test
    public void testAdd() throws BusinessException {
        String engineName = "test engine 2";
        String engineEndpoint = "test endpoint 2";
        String engineStatus = "enabled";
        Engine engine = new Engine(engineName, engineEndpoint, engineStatus);
        engineBusiness.add(engine);

        Assertions.assertEquals(2, engineBusiness.get().size());
        Assertions.assertEquals("test engine 2", engineBusiness.get().get(1).getName());
        Assertions.assertEquals("test endpoint 2", engineBusiness.get().get(1).getEndpoint());
        Assertions.assertEquals("enabled", engineBusiness.get().get(1).getStatus());
    }

    @Test
    public void testCatchAddExistingEngine() throws BusinessException {
        String engineName = "test engine";
        String engineEndpoint = "test endpoint 2";
        String engineStatus = "enabled";
        Engine engine = new Engine(engineName, engineEndpoint, engineStatus);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        engineBusiness.add(engine)
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is already an engine registered with the name : test engine"));

    }

    @Test
    public void testUpdate() throws BusinessException {
        String engineName = "test engine";
        String engineEndpoint = "test endpoint 2";
        String engineStatus = "enabled";
        Engine engine = new Engine(engineName, engineEndpoint, engineStatus);
        engineBusiness.update(engine);

        Assertions.assertEquals(1, engineBusiness.get().size());
        Assertions.assertEquals("test engine", engineBusiness.get().get(0).getName());
        Assertions.assertEquals("test endpoint 2", engineBusiness.get().get(0).getEndpoint());
        Assertions.assertEquals("enabled", engineBusiness.get().get(0).getStatus());
    }

    @Test
    public void testUpdateInexistingEngine() throws BusinessException {
        String engineName = "inexisting engine";
        String engineEndpoint = "test endpoint 2";
        String engineStatus = "enabled";
        Engine engine = new Engine(engineName, engineEndpoint, engineStatus);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        engineBusiness.update(engine)
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no engine registered with the name : inexisting engine"));

    }

    @Test
    public void testSet() throws BusinessException {
        Engine engine = engineBusiness.get().get(0);
        engine.setStatus("status updated");
        engineBusiness.update(engine);
        Assertions.assertTrue(StringUtils.contains(engineBusiness.get().get(0).getStatus(), "status updated"));
    }
    @Test
    public void testCreate()
    {
        // Without parameters
        Engine engine = new Engine();

    }
}