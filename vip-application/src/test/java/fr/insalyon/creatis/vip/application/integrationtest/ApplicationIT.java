package fr.insalyon.creatis.vip.application.integrationtest;

import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.application.client.bean.AppClass;
import fr.insalyon.creatis.vip.application.client.bean.AppVersion;
import fr.insalyon.creatis.vip.application.client.bean.Application;
import fr.insalyon.creatis.vip.application.client.bean.Engine;
import fr.insalyon.creatis.vip.application.server.business.ApplicationBusiness;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        createUserInGroup("test1@test.fr", "suffix1", "group1");
        createUserInGroup("test2@test.fr", "suffix2", "group1");

        String engineName = "test engine";
        String engineEndpoint = "test endpoint";
        String engineStatus = "enabled";
        Engine engine = new Engine(engineName, engineEndpoint, engineStatus);
        List<String> engines = new ArrayList<>();
        engines.add("test engine");
        engineBusiness.add(engine);

        AppClass appClass = new AppClass("class1", engines, applicationGroups);
        classBusiness.addClass(appClass);
        applicationClasses = new ArrayList<>();
        applicationClasses.add("class1");

        Application application = new Application("Application1", applicationClasses, "test1@test.fr", "test1", "citation1");
        applicationBusiness.add(application);

    }

    @Test
    public void testInitialization() throws BusinessException {
        // verify number of applications
        Assertions.assertEquals(1, applicationBusiness.getApplications().size(), "Incorrect number of applications");

        // verify properties of one application
        Application application = applicationBusiness.getApplication("Application1");
        Assertions.assertEquals("citation1", application.getCitation(), "Incorrect citation of application");
        Assertions.assertEquals("Application1", application.getName(), "Incorrect name of application");
        Assertions.assertEquals("test1@test.fr", application.getOwner(), "Incorrect owner of application");
        Assertions.assertNull(application.getFullName(), "getApplication should not fill fullname");
        Assertions.assertEquals("class1", application.getApplicationClasses().get(0), "Incorrect class of application");
        Assertions.assertNull(application.getApplicationGroups(), "getApplication should not fill applicationGroups");
        Assertions.assertEquals(0, applicationBusiness.getVersions("Application1").size(), "Incorrect versions number");

        // verify that there is no applicaion in the first class
        List<String[]> strEmpty = new ArrayList<>();
        Assertions.assertEquals(strEmpty, applicationBusiness.getApplications("class1"), "Incorrect number of application");

    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************** update application ************************************************************ */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testUpdateApplication() throws BusinessException {
        Application updatedApplication = new Application("Application1", applicationClasses, "test2@test.fr", "test1", "citation1");
        applicationBusiness.update(updatedApplication);

        Assertions.assertEquals("test2@test.fr", applicationBusiness.getApplication("Application1").getOwner(), "Incorrect owner of updated application");
    }

    // TODO : corriger
    @Test
    public void testCatchUpdateInexistingApplication() throws BusinessException {
        Application updatedApplication = new Application("Inexisting Application", applicationClasses, "test2@test.fr", "test1", "citation1");

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        applicationBusiness.update(updatedApplication)
        );

        // UPDATE + inexisting foreign key name for VIPAPPLICATIONCLASSES => violation
        assertTrue(StringUtils.contains(exception.getMessage(), "JdbcSQLException: Referential integrity constraint violation"));


    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************** remove application ************************************************************ */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveApplication() throws BusinessException {
        applicationBusiness.remove("Application1");

        Assertions.assertEquals(0, applicationBusiness.getApplications().size(), "Incorrect number of applications");
    }

    @Test
    public void testCatchRemoveInexistingApplication() throws BusinessException
    {
        // DELETE + inexisting primary key publicationId => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be deleted
        applicationBusiness.remove("Inexisting application");

        Assertions.assertEquals(1, applicationBusiness.getApplications().size(), "Incorrect number of applications");
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** get citation *************************************************************** */
    /* ********************************************************************************************************************************************** */


    @Test
    public void testGetCitationApplication() throws BusinessException {
        Assertions.assertEquals("citation1", applicationBusiness.getCitation("Application1"), "Incorrect citation");
    }

    /*@Test
    public void testCatchGetCitationInexistingApplication() throws BusinessException {
        assertNull(applicationBusiness.getCitation("Inexisting application"));
    }*/

    /* ********************************************************************************************************************************************** */
    /* ****************************************************************** add version *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testAddVersionApplication() throws BusinessException {
        AppVersion appVersion = new AppVersion("Application1", "version 1.0", "lfn", "jsonLfn", true, true);
        applicationBusiness.addVersion(appVersion);
        Assertions.assertEquals(1, applicationBusiness.getVersions("Application1").size(), "Incorrect versions number");
    }


}
