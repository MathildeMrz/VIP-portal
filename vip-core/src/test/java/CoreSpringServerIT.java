import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CoreSpringServerIT extends BaseSpringIT
{


    @BeforeEach
    public void setUp() throws BusinessException, GRIDAClientException, DAOException
    {
        super.setUp();

        // Create test group
        Group group1 = new Group("group1", true, true, true);
        configurationBusiness.addGroup(group1);

        // Create test group
        Group group2 = new Group("group2", false, true, false);
        configurationBusiness.addGroup(group2);

        // Create test users
        createUserInGroup(emailUser1, "suffix1", "group1");
        createUserInGroup(emailUser2, "suffix2", "group1");
        createUserInGroup(emailUser3, "suffix3", "group1");
        createUserInGroup(emailUser4, "suffix4", "group2");
    }

    @Test
    public void testInitialization() throws BusinessException
    {
        assertEquals(3, configurationBusiness.getGroups().size(), "incorrect groups number");// Support + group1
        assertEquals(5, configurationBusiness.getUsers().size(), "incorrect users number");// Created users + admin
    }


    /* ********************************************************************************************************************************************** */
    /* ****************************************************************** create user *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCreateUser() throws BusinessException, GRIDAClientException
    {
        createUser("test5@test.fr", "suffix5");
        assertRowsNbInTable("VIPUsers", 6);
    }

    @Test
    public void testCatchExistingEmailCreateUser()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        createUser(emailUser4, "suffix0")
        );

        // INSERT + existing primary key groupName => Unique index or primary key violation
        assertTrue(StringUtils.contains(exception.getMessage(), "There is an existing account associated with this email : test4@test.fr or with this first name,last name : test firstName suffix0,test lastName suffix0"));
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** create group *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCreateGroup() throws DAOException, BusinessException
    {
        Group group = new Group("group3", true, true, true);
        configurationBusiness.addGroup(group);
        assertNotNull(configurationBusiness.getGroup("group3"));
    }

    @Test
    public void testCatchCreateGroupAlreadyExisting()
    {
        Group group = new Group("group1", true, true, true);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addGroup(group)
        );
        // INSERT + existing primary key groupName => Unique index or primary key violation
        assertTrue(StringUtils.contains(exception.getMessage(), "There is already a group registered with the name : group1"));
    }

    /* ********************************************************************************************************************************************** */
    /* ******************************************************************* get group **************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchGetGroup() throws BusinessException
    {
        Group group = configurationBusiness.getGroup(nameGroup1);
        Assertions.assertEquals(nameGroup1, group.getName(), "Incorrect group name");
        Assertions.assertEquals(true, group.isPublicGroup(), "Incorrect group privacy");
    }

    @Test
    public void testCatchGetInexistingGroup() throws BusinessException
    {
        assertNull(configurationBusiness.getGroup("group3"));
    }


    /* ********************************************************************************************************************************************** */
    /* **************************************************************** update group **************************************************************** */
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
        // SELECT + inexisting foreign key / part of primary key groupName => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be selected
        assertNull(configurationBusiness.getGroup("inexisting_group"));
    }


    /* ********************************************************************************************************************************************** */
    /* ************************************************************** add user to group ************************************************************* */
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
        // INSERT + inexisting foreign key / part of primary key groupName => user email
        assertTrue(StringUtils.contains(exception.getMessage(), "JdbcSQLException: Referential integrity constraint violation"));
    }

    @Test
    public void testCatchInexistingGroupAddUserToGroup()
    {

        Exception exception = assertThrows
                (BusinessException.class, () ->
                        configurationBusiness.addUserToGroup(emailUser4, "inexisting group")
                );

        // INSERT + inexisting foreign key / part of primary key groupName => violation
        assertTrue(StringUtils.contains(exception.getMessage(), "JdbcSQLException: Referential integrity constraint violation"));

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

        // getUser is called and had an exception before the beginning of the intership
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** remove group *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveGroup() throws BusinessException
    {
        configurationBusiness.removeGroup(emailUser1, nameGroup1);
        Assertions.assertEquals(2, configurationBusiness.getGroups().size(), "incorrect groups number");
    }

    @Test
    public void testCatchRemoveInexistingGroup() throws BusinessException
    {
        // DELETE + inexisting foreign key / part of primary key groupName => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be deleted
        configurationBusiness.removeGroup(emailUser1, "inexisting group");
        Assertions.assertEquals(3, configurationBusiness.getGroups().size(), "incorrect groups number");

    }


    /* ********************************************************************************************************************************************** */
    /* ************************************************************ remove user from group ********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveUserFromGroup() throws BusinessException
    {
        configurationBusiness.removeUserFromGroup(emailUser1, nameGroup1);

        List<String> emails = configurationBusiness.getUsersFromGroup(nameGroup1)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        Assertions.assertEquals(2, emails.size(), "incorrect number of users in the group");
        Assertions.assertTrue(emails.containsAll(Arrays.asList(emailUser2, emailUser3)), "Incorrect group message receivers");
    }



    /* ********************************************************************************************************************************************** */
    /* ************************************************************** get or create user ************************************************************ */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetOrCreateExistingUser() throws BusinessException, DAOException
    {
        User user = configurationBusiness.getOrCreateUser(emailUser2, "test institution", "group1");

        Assertions.assertEquals("test firstName suffix2", user.getFirstName(), "incorrect user firstname");
        Assertions.assertEquals("test lastName suffix2", user.getLastName(), "incorrect user firstname");

    }

    @Test
    public void testGetOrCreateInexistingUser() throws BusinessException, DAOException
    {
        configurationBusiness.getOrCreateUser("inexistingUser@test.fr", "institution", "group1");

        // verify entry numbers in VIPUsers table
        assertRowsNbInTable("VIPUsers", 6);
    }

    @Test
    public void testGetOrCreateIncorrectEmailUser()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.getOrCreateUser("inexisting_user", "institution", "group1")
        );

        // exception added because before the exception java.lang.StringIndexOutOfBoundsException was raised
        assertTrue(StringUtils.contains(exception.getMessage(), "The email inexisting_user is invalid : it does not contain an @"));
    }


    /* ********************************************************************************************************************************************** */
    /* ********************************************************* get last update term of use ******************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetLastUpdateTermOfUse() throws BusinessException
    {
        Timestamp timestamp = configurationBusiness.getLastUpdateTermsOfUse();
        assertEquals("2023", timestamp.toString().substring(0, 4), "Incorrect new user api key value");
    }

    /* ********************************************************************************************************************************************** */
    /* *************************************************************** get user api key ************************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchGetApiKeyInexistingUser()
    {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.getUserApikey("inexisting_user")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting_user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* get user admin groups ********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetUserAdminGroups() throws BusinessException
    {
        Map<Group, CoreConstants.GROUP_ROLE> userGroups = configurationBusiness.getUserGroups(emailUser3);
        assertFalse(userGroups.isEmpty());

    }

    /* ********************************************************************************************************************************************** */
    /* *************************************************************** validate session ************************************************************* */
    /* ********************************************************************************************************************************************** */


    @Test
    public void testValidateInexistingSession() throws BusinessException
    {
        // SELECT + inexisting primary key session => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be selected
        assertFalse(configurationBusiness.validateSession(emailUser3, "inexisting session"));
    }

    @Test
    public void testValidateNullSession() throws BusinessException
    {
        assertFalse(configurationBusiness.validateSession(emailUser3, null));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* get user with groups *********************************************************** */
    /* ********************************************************************************************************************************************** */

    /*@Test
    public void testGetUserWithGroups() throws BusinessException
    {
        User user = configurationBusiness.getUserWithGroups(emailUser3);
        System.out.println("NNNNNNNNN "+user.getEmail());
    }*/

    /* ********************************************************************************************************************************************** */
    /* *********************************************************** generate new user api key ******************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGenerateNewApiKeyUser() throws BusinessException
    {
        String newApiKey = configurationBusiness.generateNewUserApikey(emailUser3);
        assertEquals(configurationBusiness.getUserApikey(emailUser3), newApiKey, "Incorrect new user api key value");
    }

    @Test
    public void testCatchGenerateNewApiKeyInexistingUser()
    {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.generateNewUserApikey("inexisting_user")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting_user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************** delete user api key *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchDeleteApiKeyInexistingUser() throws BusinessException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.deleteUserApikey("inexisting_user")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting_user"));
    }


    /* ********************************************************************************************************************************************** */
    /* *************************************************************** get public groups ************************************************************ */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetPublicGroups() throws BusinessException
    {
        List<String> groupsNames = configurationBusiness.getPublicGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.toList());

        Assertions.assertEquals(1, groupsNames.size(), "incorrect number of public groups");
        Assertions.assertTrue(groupsNames.containsAll(Arrays.asList(nameGroup1)), "Incorrect public groups names");
    }


    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** get user data ************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetUserData() throws BusinessException {
        User user = configurationBusiness.getUserData(emailUser4);
        Assertions.assertEquals("test firstName suffix4", user.getFirstName(), "incorrect user firstname");
        Assertions.assertEquals("test lastName suffix4", user.getLastName(), "incorrect user firstname");
    }

    @Test
    public void testCatchGetInexistingUserData()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.getUserData("inexisting_user")
        );

        // Exception added before the beginning of the internship
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting_user"));

    }

    @Test
    public void testCatchInexistingUserRemoveGroup() throws BusinessException
    {
        // DELETE + inexisting foreign key user email => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be deleted
        configurationBusiness.removeGroup("inexisting user", nameGroup1);
        Assertions.assertEquals(2, configurationBusiness.getGroups().size(), "incorrect groups number");
    }

    /* ********************************************************************************************************************************************** */
    /* *********************************************************** get user properties groups ******************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetUserPropertiesGroup() throws BusinessException
    {
        assertTrue(configurationBusiness.getUserPropertiesGroups(emailUser1).get(0)); // isPublic : group is public, it is accessible to every user
        assertTrue(configurationBusiness.getUserPropertiesGroups(emailUser1).get(1)); // isGridFile : group allows to files sharing
        assertTrue(configurationBusiness.getUserPropertiesGroups(emailUser1).get(2)); // isGridJobs : group allows job offers sharing

        System.out.println("emailUser4 = " + emailUser4);
        assertFalse(configurationBusiness.getUserPropertiesGroups(emailUser4).get(0));
        assertTrue(configurationBusiness.getUserPropertiesGroups(emailUser4).get(1));
        assertFalse(configurationBusiness.getUserPropertiesGroups(emailUser4).get(2));
    }

    /* ********************************************************************************************************************************************** */
    /* ****************************************************************** signin user *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testSigninUser() throws BusinessException {
        assertNotNull(configurationBusiness.signin(emailUser3, "testPassword"));
    }

    @Test
    public void testCatchSigninUserWrongPassword() throws BusinessException
    {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.signin(emailUser3, "test wrong password")
        );

        // Exception added before the beginning of the internship
        assertTrue(StringUtils.contains(exception.getMessage(), "Authentication failed (email or password incorrect, or user is locked)"));

    }

    /* ********************************************************************************************************************************************** */
    /* ******************************************************************** send code *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchSendActivationCode() throws BusinessException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.sendActivationCode("inexistingUser@test.fr")
        );

        // getUser is called and had an exception before the beginning of the intership
        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexistingUser@test.fr"));
    }

    @Test
    public void testCatchSendResetCode() throws BusinessException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.sendResetCode("inexistingUser@test.fr")
        );

        // getUser is called and had an exception before the beginning of the intership
        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexistingUser@test.fr"));
    }

    /* ********************************************************************************************************************************************** */
    /* ****************************************************************** update user *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testUpdateUser() throws BusinessException
    {
        User user = configurationBusiness.getUser(emailUser1);
        user.setFolder("folder_updated");
        configurationBusiness.updateUser(user);
        User userUpdated = configurationBusiness.getUser(emailUser1);
        Assertions.assertEquals("folder_updated", userUpdated.getFolder(), "Incorrect user folder");
    }

    @Test
    public void testUpdateUserEmail() throws BusinessException
    {
        configurationBusiness.updateUserEmail(emailUser1, "newEmail@test.fr");

        // verify users number
        assertEquals(5, configurationBusiness.getUsers().size(), "incorrect users number");// Created users + admin

        // verify modified user infos
        Assertions.assertEquals("newEmail@test.fr", configurationBusiness.getUser("newEmail@test.fr").getEmail(), "incorrect email update user");
        Assertions.assertEquals("test firstName suffix1", configurationBusiness.getUser("newEmail@test.fr").getFirstName(), "incorrect first name update user");
    }

    @Test
    public void testCatchUpdateInexistantUserEmail() throws BusinessException
    {
        configurationBusiness.updateUserEmail("inexistant email", "newEmail@test.fr");

        // verify users number
        assertEquals(5, configurationBusiness.getUsers().size(), "incorrect users number");// Created users + admin

        // verify modified user infos
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        Assertions.assertNull(configurationBusiness.getUser("newEmail@test.fr"))
        );

        // getUser is called and had an exception before the beginning of the intership
        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : newEmail@test.fr"));



    }

    @Test
    public void testUpdatePassword() throws BusinessException
    {
        configurationBusiness.updateUserPassword(emailUser1, "testPassword", "testPassword updated");

        // because getPassword() returns empty, try to signin
        configurationBusiness.signin(emailUser1, "testPassword updated");
        
        Assertions.assertEquals("", configurationBusiness.getUser(emailUser1).getPassword(), "incorrect password update user");
    }

    @Test
    public void testCatchUpdateWrongCurrentPassword() throws BusinessException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.updateUserPassword(emailUser1, "test password", "testPassword updated")
        );

        // Exception added before the beginning of the internship
        assertTrue(StringUtils.contains(exception.getMessage(), "The current password mismatch"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************** reset next email user ********************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testResetNextEmail() throws BusinessException
    {
        configurationBusiness.resetNextEmail(emailUser1);


        // verify next email is null
        Assertions.assertEquals(null, configurationBusiness.getUser(emailUser1).getNextEmail(), "incorrect next email update user");
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** get user names ************************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetUserNames() throws BusinessException
    {
        List<String> userNames = configurationBusiness.getUserNames(emailUser1, true);
        System.out.println("AAAAAAAA : " + userNames);
        Assertions.assertTrue(userNames.containsAll(Arrays.asList("test firstName suffix1 test lastName suffix1")), "Incorrect user names");
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** reset password ************************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchSendResetPasswordWrongCode() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.resetPassword(emailUser1, "test code", "test new password")
        );

        // Exception added before the beginning of the internship
        assertTrue(StringUtils.contains(exception.getMessage(), "Wrong reset code"));


    }


}
