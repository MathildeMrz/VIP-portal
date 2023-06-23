package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.application.client.bean.AppClass;
import fr.insalyon.creatis.vip.application.client.bean.Application;
import fr.insalyon.creatis.vip.application.client.bean.Engine;
import fr.insalyon.creatis.vip.application.server.business.ApplicationBusiness;
import fr.insalyon.creatis.vip.application.server.business.ClassBusiness;
import fr.insalyon.creatis.vip.application.server.business.EngineBusiness;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationIT extends BaseSpringIT {
        @Autowired
        private ApplicationBusiness applicationBusiness;

        @Autowired
        private ClassBusiness classBusiness;

        @Autowired
        private EngineBusiness engineBusiness;

        static public Map<AppClass, List<Application>> applicationsPerClass = new HashMap<>();

        @BeforeEach
        public void setUp() throws BusinessException, GRIDAClientException, DAOException {
            super.setUp();
            Group group1 = new Group("group1", true, true, true);
            configurationBusiness.addGroup(group1);
            List<String> applicationGroups = new ArrayList<>();
            applicationGroups.add("group1");
            configurationBusiness.getOrCreateUser("test1@test.fr", "institution", "group1");

            String engineName = "test engine";
            String engineEndpoint = "test endpoint";
            String engineStatus = "enabled";
            Engine engine = new Engine(engineName, engineEndpoint, engineStatus);
            List<String> engines = new ArrayList<>();
            engines.add("test engine");
            engineBusiness.add(engine);

            AppClass appClass = new AppClass("class1", engines, applicationGroups);
            classBusiness.addClass(appClass);
            List<String> applicationClasses = new ArrayList<>();
            applicationClasses.add("class1");

            Application application = new Application("Application1", applicationClasses, "test1@test.fr", "test1", "citation1");
            applicationBusiness.add(application);

        }

        @Test
        public void testInitialization() throws BusinessException
        {
                Application application = applicationBusiness.getApplication("Application1");
                Assertions.assertEquals(1,applicationBusiness.getApplications().size(), "Incorrect number of applications");
                Assertions.assertEquals( "Application1", application.getName(), "Incorrect name of application");
                Assertions.assertEquals( "citation1", application.getCitation(), "Incorrect citation of application");
                Assertions.assertEquals( "Application1", application.getName(), "Incorrect name of application");
                Assertions.assertEquals( "test1@test.fr", application.getOwner(), "Incorrect owner of application");

                Assertions.assertNull(application.getFullName(), "getApplication should not fill fullname");
                Assertions.assertEquals( "class1", application.getApplicationClasses().get(0), "Incorrect class of application");
                Assertions.assertNull(application.getApplicationGroups(), "getApplication should not fill applicationGroups");

        }
}
