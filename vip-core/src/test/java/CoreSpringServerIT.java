import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.integrationtest.ServerMockConfig;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.SpringCoreConfig;
import fr.insalyon.creatis.vip.core.server.auth.SamlAuthenticationService;
import fr.insalyon.creatis.vip.core.server.business.*;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.module.Configuration;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CoreSpringServerIT extends BaseSpringIT {

    /*@Autowired
    private CoreUtil coreUtil;

    @Autowired
    private BusinessException businessException;

    @Autowired
    private EmailBusiness emailBusiness;

    @Autowired
    private StatsBusiness statsBusiness;

    @Autowired
    private VipSessionBusiness vipSessionBusiness;

    @Autowired
    private SpringConfigServer springConfigServer;

    @Autowired
    private SamlTokenValidator samlTokenValidator;

    @Autowired
    private StatsBusiness.UserSearchCriteria userSearchCriteria;

    @Autowired
    private SpringConfigServer.ReloadablePropertySource reloadablePropertySource;*/



    private User admin;
    private String adminEmail;
    private User user1;
    private String emailUser1 = "test1@test.fr";
    private User user2;
    private String emailUser2 = "test2@test.fr";
    private User user3;
    private String emailUser3 = "test3@test.fr";
    private User user4;
    private String emailUser4 = "test4@test.fr";
    private String nameGroup1 = "group1";

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* create user *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @BeforeEach
    public void setUp() throws BusinessException, GRIDAClientException, DAOException
    {
        super.setUp();

        // Create test group
        Group group1 = new Group("group1", true, true, true);
        configurationBusiness.addGroup(group1);

        // Create test users
        createUserInGroup("test1@test.fr", "suffix1", "group1");
        createUserInGroup("test2@test.fr", "suffix2", "group1");
        createUserInGroup("test3@test.fr", "suffix3", "group1");
        createUser("test4@test.fr", "suffix4");
    }
    @Test
    public void testCreateUser() throws BusinessException, GRIDAClientException, BusinessException, GRIDAClientException {
        createUser("test5@test.fr", "suffix5");
        assertRowsNbInTable("VIPUsers", 6);
    }

    @Test
    public void testCatchExistingEmailCreateUser() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        createUser("test4@test.fr", "suffix0")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is an existing account associated with this email : test4@test.fr or with this first name,last name : test firstName suffix0,test lastName suffix0"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* create group *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCreateGroup() throws DAOException, BusinessException
    {
        Group group = new Group("group2", true, true, true);
        configurationBusiness.addGroup(group);
        assertNotNull(configurationBusiness.getGroup("group2"));
    }

    @Test
    public void testCatchCreateGroupAlreadyExisting()
    {
        Group group = new Group("group1", true, true, true);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addGroup(group)
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is already a group registered with the name : group1"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* get group *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchGetGroup() throws BusinessException {
        Group group = configurationBusiness.getGroup(nameGroup1);
        Assertions.assertEquals(nameGroup1, group.getName(), "Incorrect group name");
        Assertions.assertEquals(true, group.isPublicGroup(), "Incorrect group privacy");
    }

    @Test
    public void testCatchGetInexistingGroup() throws BusinessException {
        assertNull(configurationBusiness.getGroup("group2"));
    }


    /* ********************************************************************************************************************************************** */
    /* ************************************************************* update group *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testUpdateGroup() throws BusinessException
    {
        Group group = configurationBusiness.getGroup(nameGroup1);
        group.setPublicGroup(false);
        group.setName("group_name_updated");
        configurationBusiness.updateGroup(nameGroup1, group);
        Group updatedGroup = configurationBusiness.getGroup("group_name_updated");
        Assertions.assertEquals("group_name_updated", updatedGroup.getName(), "Incorrect group name");
        Assertions.assertEquals(false, updatedGroup.isPublicGroup(), "Incorrect group privacy");
    }

    //FIXME : do not return error
    @Test
    public void testUpdateInexistingGroup() throws BusinessException
    {
        Group group = configurationBusiness.getGroup("inexisting_group");
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* update group *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testUpdateUser() throws BusinessException
    {
        User user = configurationBusiness.getUser(emailUser1);
        user.setFolder("folder_updated");
        //user.setPassword("password_updated");
        configurationBusiness.updateUser(user);
        User userUpdated = configurationBusiness.getUser(emailUser1);
        Assertions.assertEquals("folder_updated",userUpdated.getFolder(), "Incorrect user folder");
        //Assertions.assertEquals("password_updated", userUpdated.getPassword(), "Incorrect user password");
    }


    /* ********************************************************************************************************************************************** */
    /* ************************************************************* add user to group *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testAddUserToGroup() throws BusinessException
    {
        configurationBusiness.addUserToGroup(emailUser4, nameGroup1);

        List<String> emails = configurationBusiness.getUsersFromGroup(nameGroup1)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        Assertions.assertEquals(4, emails.size(), "incorrect number of users in the group");
        Assertions.assertTrue(emails.containsAll(Arrays.asList(emailUser1, emailUser2, emailUser3, emailUser4)), "Incorrect group message receivers");

    }


    @Test
    public void testCatchInexistingUserAddUserToGroup()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addUserToGroup("inexisting user", nameGroup1)
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "Referential integrity constraint violation"));

    }

    @Test
    public void testCatchInexistingGroupAddUserToGroup()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addUserToGroup(emailUser4, "inexisting group")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));
    }

    /* ********************************************************************************************************************************************** */
    /* ****************************************************************** remove user *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchRemoveUser() throws BusinessException
    {
        configurationBusiness.removeUser(emailUser1, false);
        assertRowsNbInTable("VIPUsers", 4);
    }

    @Test
    public void testCatchRemoveInexistingUser()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.removeUser("inexisting user", false)

        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** remove group *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveGroup() throws BusinessException
    {
        configurationBusiness.removeGroup(emailUser1, nameGroup1);
    }

    @Test
    public void testCatchRemoveInexistingGroup() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.removeGroup(emailUser1, "inexisting group")

        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the name : inexisting group"));
    }


    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** remove user from group *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveUserFromGroup() throws BusinessException {
        configurationBusiness.removeUserFromGroup(emailUser1, nameGroup1);

        List<String> emails = configurationBusiness.getUsersFromGroup(nameGroup1)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        Assertions.assertEquals(2, emails.size(), "incorrect number of users in the group");
        Assertions.assertTrue(emails.containsAll(Arrays.asList(emailUser2, emailUser3)), "Incorrect group message receivers");

    }



    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** get or create user *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetOrCreateExistingUser() throws BusinessException {
        User user = configurationBusiness.getOrCreateUser(emailUser2, "test institution", "group1");

        Assertions.assertEquals("test firstname "+emailUser2, user.getFirstName(), "incorrect user firstname");
        Assertions.assertEquals("test lastName "+emailUser2, user.getLastName(), "incorrect user firstname");

    }

    @Test
    public void testGetOrCreateInexistingUser() throws BusinessException {
        configurationBusiness.getOrCreateUser("inexistingUser@test.fr", "institution", "group1");

        // verify entry numbers in VIPUsers table
        assertRowsNbInTable("VIPUsers", 6);
    }

    /*@Test
    public void testGetOrCreateIncorrectEmailUser() throws BusinessException {

        configurationBusiness.getOrCreateUser("inexisting_user", "institution", "group1");
        // Pas de @ -> error
    }*/




    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** get last update term of use *************************************************************** */
    /* ********************************************************************************************************************************************** */

    /*@Test
    public void testGetLastUpdateTermOfUse() throws BusinessException {
        Timestamp timestamp = configurationBusiness.getLastUpdateTermsOfUse();
        System.out.println("IIIIIIIIIIIIIIII");
    }*/


    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** get user api key *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchGetApiKeyInexistingUser() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.getUserApikey("inexisting_user")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting_user"));

    }


    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** generate new user api key *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGenerateNewApiKeyUser() throws BusinessException {
        String newApiKey = configurationBusiness.generateNewUserApikey(emailUser3);
        assertEquals(newApiKey, configurationBusiness.getUserApikey(emailUser3), "Incorrect new user api key value");
    }

    @Test
    public void testCatchGenerateNewApiKeyInexistingUser() {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.generateNewUserApikey("inexisting_user")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting_user"));

    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** delete user api key *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchDeleteApiKeyInexistingUser() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.deleteUserApikey("inexisting_user")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting_user"));

    }


    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** get public groups *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetPublicGroups() throws BusinessException {
        List<String> groupsNames = configurationBusiness.getPublicGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.toList());

        Assertions.assertEquals(1, groupsNames.size(), "incorrect number of public groups");
        Assertions.assertTrue(groupsNames.containsAll(Arrays.asList(nameGroup1)), "Incorrect public groups names");
    }


    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** get user data *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetUserData() throws BusinessException {
        User user = configurationBusiness.getUserData(emailUser4);

        Assertions.assertEquals("test firstName suffix4", user.getFirstName(), "incorrect user firstname");
        Assertions.assertEquals("test lastName suffix4", user.getLastName(), "incorrect user firstname");
    }

    @Test
    public void testCatchGetInexistingUserData() throws BusinessException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.getUserData("inexisting_user")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting_user"));

    }


    //FIXME : reussir à check inexisting user
    /*@Test
    public void testCatchInexistingUserRemoveGroup() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.removeGroup("inexisting user", nameGroup1)
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }*/


}
