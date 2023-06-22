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

        /*@Test
        public void testInitialization() throws BusinessException {
            assert applicationBusiness.getApplications().size() == 1 : "Incorrect number of applications";
            assert applicationBusiness.getApplications().get(0).getName().equals("Application1") : "Incorrect name of application";
            assert applicationBusiness.getApplication("Application1").getCitation().equals("citation1") : "Incorrect citation of application";
            assert applicationBusiness.getApplication("Application1").getName().equals("Application1") : "Incorrect name of application";
            assert applicationBusiness.getApplication("Application1").getOwner().equals("test1@test.fr") : "Incorrect owner of application";


            assert applicationBusiness.getApplication("Application1").getFullName().equals("test1") : "Incorrect full name of application";
            assert applicationBusiness.getApplication("Application1").getApplicationClasses().get(0).equals("class1") : "Incorrect class of application";
            assert applicationBusiness.getApplication("Application1").getApplicationGroups().get(0).equals("group1") : "Incorrect group of application";

        }*/
}
