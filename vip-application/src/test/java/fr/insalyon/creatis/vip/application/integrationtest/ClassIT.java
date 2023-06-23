package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.vip.application.client.bean.AppClass;
import fr.insalyon.creatis.vip.application.client.bean.Application;
import fr.insalyon.creatis.vip.application.client.bean.Engine;
import fr.insalyon.creatis.vip.application.server.business.ClassBusiness;
import fr.insalyon.creatis.vip.application.server.business.EngineBusiness;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // testing framework must recreate a MessageBusiness after each test method
public class ClassIT extends BaseSpringIT {
    @Autowired
    public ClassBusiness classBusiness;

    @Autowired
    public EngineBusiness engineBusiness;

    static public Map < AppClass, List < Application >> applicationsPerClass = new HashMap < > ();

    @BeforeEach
    public void setUp() throws BusinessException, DAOException {
        List < String > applicationGroups = new ArrayList < > ();
        applicationGroups.add("group1");
        Group group1 = new Group("group1", true, true, true);
        configurationBusiness.addGroup(group1);

        String engineName = "test engine";
        String engineEndpoint = "test endpoint";
        String engineStatus = "enabled";
        Engine engine = new Engine(engineName, engineEndpoint, engineStatus);
        List < String > engines = new ArrayList < > ();
        engines.add("test engine");
        engineBusiness.add(engine);

        AppClass appClass = new AppClass("class1", engines, applicationGroups);
        classBusiness.addClass(appClass);
    }

    @Test
    public void testInitialization() throws BusinessException {
        Assertions.assertEquals(classBusiness.getClasses().size(), 1, "Incorrect number of classes");
        Assertions.assertEquals(classBusiness.getClassesName().get(0), "class1", "Incorrect name of class");
    }

    @Test
    public void testCatchGetInexistingClass() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        classBusiness.getClass("inexisting class")
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no class registered with the name : inexisting class"));
    }

    @Test
    public void testRemoveClass() throws BusinessException {
        classBusiness.removeClass("class1");

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        classBusiness.getClass("class1")
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no class registered with the name : class1"));
        Assertions.assertEquals(classBusiness.getClasses().size(), 0, "Incorrect number of classes");
    }

    @Test
    public void testCatchInexistingClassRemoveClass() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        classBusiness.removeClass("inexisting class")
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no class registered with the name : inexisting class"));
    }

    @Test
    public void testAddClass() throws BusinessException {

        List < String > engines = new ArrayList < > ();
        engines.add("test engine");

        List < String > applicationGroups = new ArrayList < > ();
        applicationGroups.add("group1");

        AppClass appClass = new AppClass("class2", engines, applicationGroups);
        classBusiness.addClass(appClass);

        Assertions.assertEquals(classBusiness.getClasses().size(), 2, "Incorrect number of classes");
        Assertions.assertEquals(classBusiness.getClassesName().get(1), "class2", "Incorrect name of class");
    }

    @Test
    public void testUpdateClass() throws BusinessException {
        List < String > applicationGroups = new ArrayList < > ();
        applicationGroups.add("group1");

        String engineName = "test engine 2";
        String engineEndpoint = "test endpoint";
        String engineStatus = "enabled";

        Engine engine = new Engine(engineName, engineEndpoint, engineStatus);

        List < String > engines = new ArrayList < > ();
        engines.add("test engine 2");
        engineBusiness.add(engine);

        AppClass appClass = new AppClass("class1", engines, applicationGroups);

        classBusiness.updateClass(appClass);

        Assertions.assertEquals(classBusiness.getClasses().size() , 1, "Incorrect number of classes");
        Assertions.assertEquals(classBusiness.getClasses().get(0).getEngines().get(0) , "test engine 2", "Incorrect number of classes");

    }

    @Test
    public void testCreate()
    {
        List < String > applicationGroups = new ArrayList < > ();
        applicationGroups.add("group1");

        List < String > engines = new ArrayList < > ();
        engines.add("test engine");

        // With all parameters
        AppClass appClass = new AppClass("class1", engines, applicationGroups);

        // With string and list
        AppClass appclass2 = new AppClass("class2", applicationGroups);

        // Without parameters
        AppClass appclass3 = new AppClass();

    }

    // TODO : correct empty array
    /*@Test
    public void testGetUserClassesName() throws BusinessException {
        configurationBusiness.getOrCreateUser("test1@test.fr", "institution", "group1");
        System.out.println("JJJJ " + classBusiness.getUserClassesName("test1@test.fr", true));
    }

    // TODO : correct empty array
    @Test
    public void testGetUserClasses() throws BusinessException {
        configurationBusiness.getOrCreateUser("test1@test.fr", "institution", "group1");
        System.out.println("IIIII " + classBusiness.getUserClasses("test1@test.fr", true));
    }*/
}