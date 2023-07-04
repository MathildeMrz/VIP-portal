import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.integrationtest.SpingTestConfig;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.EmailBusiness;
import fr.insalyon.creatis.vip.core.server.business.proxy.ProxyClient;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static fr.insalyon.creatis.vip.core.client.view.user.UserLevel.Developer;
import static org.junit.jupiter.api.Assertions.*;

public class CoreSpringServerIT extends BaseSpringIT {

    @BeforeEach
    public void setUp() throws BusinessException, GRIDAClientException, DAOException {
        super.setUp();

        // Create test group
        group1 = new Group("group1", true, true, true);
        configurationBusiness.addGroup(group1);

        // Create test group
        group2 = new Group("group2", false, true, false);
        configurationBusiness.addGroup(group2);

        // Create test users
        createUserInGroup(emailUser1, "suffix1", "group1");
        createUserInGroup(emailUser2, "suffix2", "group1");
        createUserInGroup(emailUser3, "suffix3", "group1");
        createUserInGroup(emailUser4, "suffix4", "group2");

        user1 = configurationBusiness.getUser(emailUser1);
        user2 = configurationBusiness.getUser(emailUser2);
        user3 = configurationBusiness.getUser(emailUser3);
        user4 = configurationBusiness.getUser(emailUser4);

        Map<Group, CoreConstants.GROUP_ROLE> groups = new HashMap<>();
        groups.put(group1, CoreConstants.GROUP_ROLE.User);
        user1.setGroups(groups);
        user2.setGroups(groups);
        user3.setGroups(groups);

        groups = new HashMap<>();
        groups.put(group2, CoreConstants.GROUP_ROLE.Admin);
        user4.setGroups(groups);

        user1.setIsgridfile(true);
        user1.setIsgridjobs(true);
    }

    @Test
    public void testInitialization() throws BusinessException {
        assertEquals(3, configurationBusiness.getGroups().size(), "incorrect groups number");// Support + group1
        assertEquals(5, configurationBusiness.getUsers().size(), "incorrect users number");// Created users + admin
    }


    /* ********************************************************************************************************************************************** */
    /* ****************************************************************** create user *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCreateUser() throws BusinessException, GRIDAClientException {
        createUser("test5@test.fr", "suffix5");
        assertRowsNbInTable("VIPUsers", 6);
    }

    @Test
    public void testCatchExistingEmailCreateUser() {
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
    public void testCreateGroup() throws DAOException, BusinessException {
        Group group = new Group("group3", true, true, true);
        configurationBusiness.addGroup(group);
        assertNotNull(configurationBusiness.getGroup("group3"));
    }

    @Test
    public void testCatchCreateGroupAlreadyExisting() {
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
    public void testCatchGetGroup() throws BusinessException {
        Group group = configurationBusiness.getGroup(nameGroup1);
        Assertions.assertEquals(nameGroup1, group.getName(), "Incorrect group name");
        assertTrue(group.isPublicGroup(), "Incorrect group privacy");
    }

    @Test
    public void testCatchGetNonExistentGroup() throws BusinessException {
        assertNull(configurationBusiness.getGroup("group3"));
    }


    /* ********************************************************************************************************************************************** */
    /* **************************************************************** update group **************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testUpdateGroup() throws BusinessException {
        Group group = configurationBusiness.getGroup(nameGroup1);
        group.setPublicGroup(false);
        group.setName("group_name_updated");
        configurationBusiness.updateGroup(nameGroup1, group);
        Group updatedGroup = configurationBusiness.getGroup("group_name_updated");
        Assertions.assertEquals("group_name_updated", updatedGroup.getName(), "Incorrect group name");
        assertFalse(updatedGroup.isPublicGroup(), "Incorrect group privacy");
    }

    @Test
    public void testUpdateNonExistentGroup() throws BusinessException {
        // SELECT + nonExistent foreign key / part of primary key groupName => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be selected
        assertNull(configurationBusiness.getGroup("nonExistent_group"));
    }


    /* ********************************************************************************************************************************************** */
    /* ************************************************************** add user to group ************************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testAddUserToGroup() throws BusinessException {
        configurationBusiness.addUserToGroup(emailUser4, nameGroup1);

        List<String> emails = configurationBusiness.getUsersFromGroup(nameGroup1)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        Assertions.assertEquals(4, emails.size(), "incorrect number of users in the group");
        Assertions.assertTrue(emails.containsAll(Arrays.asList(emailUser1, emailUser2, emailUser3, emailUser4)), "Incorrect group message receivers");

    }


    @Test
    public void testCatchNonExistentUserAddUserToGroup() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addUserToGroup("nonExistent user", nameGroup1)
        );
        // INSERT + nonExistent foreign key / part of primary key groupName => user email
        assertTrue(StringUtils.contains(exception.getMessage(), "JdbcSQLException: Referential integrity constraint violation"));
    }

    @Test
    public void testCatchNonExistentGroupAddUserToGroup() {

        Exception exception = assertThrows
                (BusinessException.class, () ->
                        configurationBusiness.addUserToGroup(emailUser4, "nonExistent group")
                );

        // INSERT + nonExistent foreign key / part of primary key groupName => violation
        assertTrue(StringUtils.contains(exception.getMessage(), "JdbcSQLException: Referential integrity constraint violation"));

    }

    /* ********************************************************************************************************************************************** */
    /* ****************************************************************** remove user *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchRemoveUser() throws BusinessException {
        configurationBusiness.removeUser(emailUser1, false);
        assertRowsNbInTable("VIPUsers", 4);
    }

    @Test
    public void testCatchRemoveNonExistentUser() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.removeUser("nonExistent user", false)

        );

        // getUser is called and had an exception before the beginning of the intership
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : nonExistent user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** remove group *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveGroup() throws BusinessException {
        configurationBusiness.removeGroup(emailUser1, nameGroup1);
        Assertions.assertEquals(2, configurationBusiness.getGroups().size(), "incorrect groups number");
    }

    @Test
    public void testCatchRemoveNonExistentGroup() throws BusinessException {
        // DELETE + nonExistent foreign key / part of primary key groupName => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be deleted
        configurationBusiness.removeGroup(emailUser1, "nonExistent group");
        Assertions.assertEquals(3, configurationBusiness.getGroups().size(), "incorrect groups number");
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************ remove user from group ********************************************************** */
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
    /* ************************************************************** get or create user ************************************************************ */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetOrCreateExistingUser() throws BusinessException, DAOException {
        User user = configurationBusiness.getOrCreateUser(emailUser2, "test institution", "group1");

        Assertions.assertEquals("test firstName suffix2", user.getFirstName(), "incorrect user firstname");
        Assertions.assertEquals("test lastName suffix2", user.getLastName(), "incorrect user firstname");

    }

    @Test
    public void testGetOrCreateNonExistentUser() throws BusinessException, DAOException {
        configurationBusiness.getOrCreateUser("nonExistentUser@test.fr", "institution", "group1");

        // verify entry numbers in VIPUsers table
        assertRowsNbInTable("VIPUsers", 6);
    }

    @Test
    public void testGetOrCreateIncorrectEmailUser() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.getOrCreateUser("nonExistent_user", "institution", "group1")
        );

        // exception added because before the exception java.lang.StringIndexOutOfBoundsException was raised
        assertTrue(StringUtils.contains(exception.getMessage(), "The email nonExistent_user is invalid : it does not contain an @"));
    }


    /* ********************************************************************************************************************************************** */
    /* ********************************************************* get last update term of use ******************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetLastUpdateTermOfUse() throws BusinessException {
        Timestamp timestamp = configurationBusiness.getLastUpdateTermsOfUse();
        assertEquals("2023", timestamp.toString().substring(0, 4), "Incorrect new user api key value");
    }

    /* ********************************************************************************************************************************************** */
    /* *************************************************************** get user api key ************************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchGetApiKeyNonExistentUser() {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.getUserApikey("nonExistent_user")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : nonExistent_user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* get user admin groups ********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetUserAdminGroups() throws BusinessException {
        Map<Group, CoreConstants.GROUP_ROLE> userGroups = configurationBusiness.getUserGroups(emailUser3);
        assertFalse(userGroups.isEmpty());

    }

    /* ********************************************************************************************************************************************** */
    /* *************************************************************** validate session ************************************************************* */
    /* ********************************************************************************************************************************************** */


    @Test
    public void testValidateNonExistentSession() throws BusinessException {
        // SELECT + nonExistent primary key session => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be selected
        assertFalse(configurationBusiness.validateSession(emailUser3, "nonExistent session"));
    }

    @Test
    public void testValidateNullSession() throws BusinessException {
        assertFalse(configurationBusiness.validateSession(emailUser3, null));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* get user with groups *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetUserWithGroups() throws BusinessException
    {
        User user = configurationBusiness.getUserWithGroups(emailUser3);
        System.out.println("NNNNNNNNN "+user.getEmail());
    }

    /* ********************************************************************************************************************************************** */
    /* *********************************************************** generate new user api key ******************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGenerateNewApiKeyUser() throws BusinessException {
        String newApiKey = configurationBusiness.generateNewUserApikey(emailUser3);
        assertEquals(configurationBusiness.getUserApikey(emailUser3), newApiKey, "Incorrect new user api key value");
    }

    @Test
    public void testCatchGenerateNewApiKeyNonExistentUser() {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.generateNewUserApikey("nonExistent_user")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : nonExistent_user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************** delete user api key *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchDeleteApiKeyNonExistentUser() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.deleteUserApikey("nonExistent_user")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : nonExistent_user"));
    }


    /* ********************************************************************************************************************************************** */
    /* *************************************************************** get public groups ************************************************************ */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetPublicGroups() throws BusinessException {
        List<String> groupsNames = configurationBusiness.getPublicGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.toList());

        Assertions.assertEquals(1, groupsNames.size(), "incorrect number of public groups");
        Assertions.assertTrue(groupsNames.containsAll(List.of(nameGroup1)), "Incorrect public groups names");
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
    public void testCatchGetNonExistentUserData() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.getUserData("nonExistent_user")
        );

        // Exception added before the beginning of the internship
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : nonExistent_user"));

    }

    @Test
    public void testCatchNonExistentUserRemoveGroup() throws BusinessException {
        // DELETE + nonExistent foreign key user email => no exception
        // We decided not to add an exception because if this occurs, it will not create problem, just no row will be deleted
        configurationBusiness.removeGroup("nonExistent user", nameGroup1);
        Assertions.assertEquals(2, configurationBusiness.getGroups().size(), "incorrect groups number");
    }

    /* ********************************************************************************************************************************************** */
    /* *********************************************************** get user properties groups ******************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetUserPropertiesGroup() throws BusinessException {
        assertTrue(configurationBusiness.getUserPropertiesGroups(emailUser1).get(0)); // isPublic : group is public, it is accessible to every user
        assertTrue(configurationBusiness.getUserPropertiesGroups(emailUser1).get(1)); // isGridFile : group allows to files sharing
        assertTrue(configurationBusiness.getUserPropertiesGroups(emailUser1).get(2)); // isGridJobs : group allows job offers sharing

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
    public void testCatchSigninUserWrongPassword() throws BusinessException {

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
    public void testCatchSendActivationCode() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.sendActivationCode("nonExistentUser@test.fr")
        );

        // getUser is called and had an exception before the beginning of the intership
        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : nonExistentUser@test.fr"));
    }

    @Test
    public void testCatchSendResetCode() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.sendResetCode("nonExistentUser@test.fr")
        );

        // getUser is called and had an exception before the beginning of the intership
        Assertions.assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : nonExistentUser@test.fr"));
    }

    /* ********************************************************************************************************************************************** */
    /* ****************************************************************** update user *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testUpdateUser() throws BusinessException {
        User user = configurationBusiness.getUser(emailUser1);
        user.setFolder("folder_updated");
        configurationBusiness.updateUser(user);
        User userUpdated = configurationBusiness.getUser(emailUser1);
        Assertions.assertEquals("folder_updated", userUpdated.getFolder(), "Incorrect user folder");
    }

    @Test
    public void testUpdateUserEmail() throws BusinessException {
        configurationBusiness.updateUserEmail(emailUser1, "newEmail@test.fr");

        // verify users number
        assertEquals(5, configurationBusiness.getUsers().size(), "incorrect users number");// Created users + admin

        // verify modified user infos
        Assertions.assertEquals("newEmail@test.fr", configurationBusiness.getUser("newEmail@test.fr").getEmail(), "incorrect email update user");
        Assertions.assertEquals("test firstName suffix1", configurationBusiness.getUser("newEmail@test.fr").getFirstName(), "incorrect first name update user");
    }

    @Test
    public void testCatchUpdateInexistantUserEmail() throws BusinessException {
        configurationBusiness.updateUserEmail("nonExistent email", "newEmail@test.fr");

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
    public void testUpdatePassword() throws BusinessException {
        configurationBusiness.updateUserPassword(emailUser1, "testPassword", "testPassword updated");

        // because getPassword() returns empty, try to signin
        configurationBusiness.signin(emailUser1, "testPassword updated");

        Assertions.assertEquals("", configurationBusiness.getUser(emailUser1).getPassword(), "incorrect password update user");
    }

    @Test
    public void testCatchUpdateWrongCurrentPassword() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.updateUserPassword(emailUser1, "test password", "testPassword updated")
        );

        // Exception added before the beginning of the internship
        assertTrue(StringUtils.contains(exception.getMessage(), "The current password mismatch"));
    }

    @Test
    public void testSetRegistrationDate() throws BusinessException {
        Date now = new Date();
        user1.setRegistration(now);
        Assertions.assertEquals(now, user1.getRegistration(), "incorrect registration date");

    }

    @Test
    public void testIsGridFile() throws BusinessException {
        assertTrue(user1.getIsgridfile(), " isGridFile method should be true");
        user1.setIsgridfile(false);
        assertFalse(user1.getIsgridfile(), " isGridFile method should be false");
    }


    @Test
    public void testIsGridJob() throws BusinessException {
        assertTrue(user1.getIsgridjobs(), " isGridJobs method should be true");
        user1.setIsgridjobs(false);
        assertFalse(user1.getIsgridjobs(), " isGridJobs method should be false");
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************** reset next email user ********************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testResetNextEmail() throws BusinessException {
        configurationBusiness.resetNextEmail(emailUser1);

        // verify next email is null
        assertNull(configurationBusiness.getUser(emailUser1).getNextEmail(), "incorrect next email update user");
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** get user names ************************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetUserNames() throws BusinessException {
        List<String> userNames = configurationBusiness.getUserNames(emailUser1, true);
        Assertions.assertTrue(userNames.containsAll(List.of("test firstName suffix1 test lastName suffix1")), "Incorrect user names");
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************************** reset password ************************************************************* */
    /* ****************************Les PRI sélectionnés bénéficieront, au-delà des ressources directement mobilisées par les laboratoires participants, de crédits d'un montant total de :****************************************************************************************************************** */

    @Test
    public void testCatchSendResetPasswordWrongCode() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.resetPassword(emailUser1, "test code", "test new password")
        );

        // Exception added before the beginning of the internship
        assertTrue(StringUtils.contains(exception.getMessage(), "Wrong reset code"));
    }

    /* ************************************************************************************************************************ */
    /* ***************************************************************** check roles *************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testIsDeveloper() throws BusinessException {
        assertFalse(user3.isDeveloper());
        user3.setLevel(Developer);
        assertTrue(user3.isDeveloper());
    }

    @Test
    public void testIsGroupNotAdmin() throws BusinessException {
        assertFalse(user3.isGroupAdmin());
    }

    @Test
    public void testIsGroupAdmin() throws BusinessException {
        assertTrue(user4.isGroupAdmin());
    }

    @Test
    public void testAssAcceptedTermOfUses() throws BusinessException {
        assertTrue(user4.hasAcceptTermsOfUse());
    }

    /* ************************************************************************************************************************ */
    /* ***************************************************************** send mail *************************************************************** */
    /* ********************************************************************************************************************************************** */


    //@Autowired
    //EmailBusiness emailBusiness;

    @Test
    public void testSendEmail() throws BusinessException {
        SpingTestConfig spingTestConfig = new SpingTestConfig();
        EmailBusiness emailBusiness = spingTestConfig.testEmailBusiness();

        String subject = "Test subject";
        String content = "Test message content";
        String[] recipients = {emailUser1, emailUser2};
        boolean direct = true;
        String username = "username";

        try {
            // Calling the method with the specific arguments
            emailBusiness.sendEmail(subject, content, recipients, direct, username);
        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }

        // Verify the parameters are the good ones
        Mockito.verify(emailBusiness).sendEmail(Mockito.eq(subject), Mockito.eq(content), Mockito.eq(recipients), Mockito.eq(direct), Mockito.eq(username));
    }

    @Test
    public void testCatchExceptionSendEmail() throws BusinessException {
        SpingTestConfig spingTestConfig = new SpingTestConfig();
        EmailBusiness emailBusiness = spingTestConfig.testEmailBusiness();

        String subject = "Test subject";
        String content = "Test message content";
        String[] recipients = {emailUser1, emailUser2};
        boolean direct = true;
        String username = "username";

        // sendEmail must throw an exception when the arguments are the last ones
        Mockito.doThrow(new BusinessException("Test exception")).when(emailBusiness).sendEmail(Mockito.eq(subject), Mockito.eq(content), Mockito.eq(recipients), Mockito.eq(direct), Mockito.eq(username));

        // Calling the method with the specific arguments
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            emailBusiness.sendEmail(subject, content, recipients, direct, username);
        });

        // Check that the good exception was thrown
        assertEquals("Test exception", exception.getMessage());
    }

    /* ************************************************************************************************************************ */
    /* ***************************************************************** proxy attributes *************************************************************** */
    /* ********************************************************************************************************************************************** */

    /*@Test
    public void test() throws BusinessException, IOException {
        SpingTestConfig spingTestConfig = new SpingTestConfig();
        ProxyClient proxyClient = spingTestConfig.testProxyClient();

        // Check that no exception is thrown
        assertDoesNotThrow(proxyClient::checkProxy);
        String proxyFileName = server.getServerProxy();

        System.out.println("TESTT : "+proxyFileName);
        File proxyFile = new File(proxyFileName);
        assertTrue(proxyFile.exists());
        assertEquals("0600", getPermissions(proxyFile)); //  "0600" signifie que seul le propriétaire du fichier a le droit de lire et d'écrire dans le fichier, sans le droit de l'exécuter. Les autres utilisateurs (groupe et autres) n'ont aucune permission sur ce fichier.

    }

    private String getPermissions(File file) throws IOException {
        PosixFileAttributes attrs = Files.readAttributes(file.toPath(), PosixFileAttributes.class);
        Set<PosixFilePermission> permissions = attrs.permissions();
        return PosixFilePermissions.toString(permissions);
    }*/

}
