package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.grida.client.GRIDAClientException;
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

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // testing framework must recreate a MessageBusiness after each test method
public class ClassIT extends BaseSpringIT {
    @Autowired
    public ClassBusiness classBusiness;

    @Autowired
    public EngineBusiness engineBusiness;

    static public Map < AppClass, List < Application >> applicationsPerClass = new HashMap < > ();

    private List < String > applicationGroups = new ArrayList < > ();
    private List < String > engines = new ArrayList < > ();

    @BeforeEach
    public void setUp() throws BusinessException, DAOException, GRIDAClientException {
        super.setUp();

        Group group1 = new Group("group1", true, true, true);
        applicationGroups.add("group1");
        configurationBusiness.addGroup(group1);

        Engine engine = new Engine("test engine", "test endpoint", "enabled");
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

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* create class *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCreateClass()
    {
        // With all parameters
        AppClass appClass = new AppClass("class1", engines, applicationGroups);

        // With string and list
        AppClass appclass2 = new AppClass("class2", applicationGroups);

        // Without parameters
        AppClass appclass3 = new AppClass();
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* add class *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testAddClass() throws BusinessException
    {
        AppClass appClass = new AppClass("class2", engines, applicationGroups);
        classBusiness.addClass(appClass);

        Assertions.assertEquals(classBusiness.getClasses().size(), 2, "Incorrect number of classes");
        Assertions.assertEquals(classBusiness.getClassesName().get(1), "class2", "Incorrect name of class");
    }

    @Test
    public void testAddExistingClass() throws BusinessException {

        AppClass appClass = new AppClass("class1", engines, applicationGroups);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        classBusiness.addClass(appClass)
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "A class named class1 already exists"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* get class *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetClass() throws BusinessException {
        classBusiness.getClass("class1");
    }

    @Test
    public void testCatchGetInexistingClass() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        classBusiness.getClass("inexisting class")
        );

        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no class registered with the name : inexisting class"));
    }


    /* ********************************************************************************************************************************************** */
    /* ************************************************************* remove class *********************************************************** */
    /* ********************************************************************************************************************************************** */
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

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* update class *********************************************************** */
    /* ********************************************************************************************************************************************** */


    @Test
    public void testUpdateClass() throws BusinessException {
        List < String > applicationGroups = new ArrayList < > ();
        applicationGroups.add("group1");

        Engine engine = new Engine("test engine 2", "test endpoint", "enabled");
        List < String > engines = new ArrayList < > ();
        engines.add("test engine 2");
        engineBusiness.add(engine);

        AppClass appClass = new AppClass("class1", engines, applicationGroups);
        classBusiness.updateClass(appClass);

        Assertions.assertEquals(classBusiness.getClasses().size() , 1, "Incorrect number of classes");
        Assertions.assertEquals(classBusiness.getClasses().get(0).getEngines().get(0) , "test engine 2", "Incorrect number of classes");
    }

    // TODO : correct empty array
    @Test
    public void testGetUserClassesName() throws BusinessException, GRIDAClientException {
        createUser("test1@test.fr", "group1");
    }

    /*// TODO : correct empty array
    @Test
    public void testGetUserClasses() throws BusinessException {
        configurationBusiness.getOrCreateUser("test1@test.fr", "institution", "group1");
        System.out.println("IIIII " + classBusiness.getUserClasses("test1@test.fr", true));
    }*/
}