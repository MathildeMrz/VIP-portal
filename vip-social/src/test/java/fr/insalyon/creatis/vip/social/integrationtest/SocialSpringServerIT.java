package fr.insalyon.creatis.vip.social.integrationtest;


import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.integrationtest.ServerMockConfig;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.social.client.bean.GroupMessage;
import fr.insalyon.creatis.vip.social.client.bean.Message;
import fr.insalyon.creatis.vip.social.server.business.MessageBusiness;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SocialSpringServerIT extends BaseSpringIT {
    @Autowired
    private MessageBusiness messageBusiness;
    private User admin;
    private String adminEmail;

    @BeforeEach
    public void setUp() throws BusinessException, GRIDAClientException, DAOException {
        super.setUp();

        // Create test group
        Group group1 = new Group("group1", true, true, true);
        configurationBusiness.addGroup(group1);

        // Create test users
        createUserInGroup("test1@test.fr", "suffix1", "group1");
        createUserInGroup("test2@test.fr", "suffix2","group1");
        createUserInGroup("test3@test.fr", "suffix3", "group1");
        createUser("test4@test.fr", "suffix4");

        // Get admin
        admin = configurationBusiness.getUser(ServerMockConfig.TEST_ADMIN_EMAIL);
        adminEmail = ServerMockConfig.TEST_ADMIN_EMAIL;

        // Send test messages
        messageBusiness.sendMessage(admin, new String[]{"test1@test.fr", "test3@test.fr"}, "test subject", "test message");
        messageBusiness.sendGroupMessage(configurationBusiness.getUser("test1@test.fr"), "group1", configurationBusiness.getUsersFromGroup("group1"), "subject user 1", "message user 1");
    }

    // Check that setUp() worked
    @Test
    public void testInitialisation() throws BusinessException {
        Message individualMessage = messageBusiness.getMessagesByUser("test1@test.fr", new Date()).get(0);
        GroupMessage groupMessage = messageBusiness.getGroupMessages("group1", new Date()).get(0);

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(1, messageBusiness.verifyMessages("test1@test.fr"), "Incorrect number of messages not read");
        Assertions.assertEquals(0, messageBusiness.verifyMessages("test2@test.fr"), "Incorrect number of messages not read");
        Assertions.assertEquals(1, messageBusiness.verifyMessages("test3@test.fr"), "Incorrect number of messages not read");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(adminEmail), "Incorrect number of messages not read");

        Assertions.assertEquals(1, messageBusiness.getMessagesByUser("test1@test.fr", new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(0, messageBusiness.getMessagesByUser("test2@test.fr", new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(1, messageBusiness.getMessagesByUser("test3@test.fr", new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(0, messageBusiness.getMessagesByUser("test4@test.fr", new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(1, messageBusiness.getGroupMessages("group1", new Date()).size(), "Incorrect number of group messages received");
        Assertions.assertEquals("test-admin@test.com", individualMessage.getSender().getEmail(), "Incorrect sender email");
        Assertions.assertEquals("test message", individualMessage.getMessage(), "Incorrect message");

        Assertions.assertEquals(false, individualMessage.isRead(), "Incorrect message isRead");
        Assertions.assertEquals("test subject", individualMessage.getTitle(), "Incorrect message title");

        Assertions.assertEquals("test1@test.fr", individualMessage.getReceivers()[0].getEmail(), "Incorrect message receivers number");
        Assertions.assertEquals("test3@test.fr", messageBusiness.getMessagesByUser("test3@test.fr", new Date()).get(0).getReceivers()[0].getEmail(), "Incorrect message receivers number");

        Assertions.assertEquals("group1", groupMessage.getGroupName(), "Incorrect group name");
        Assertions.assertEquals("message user 1", groupMessage.getMessage(), "Incorrect group message");
        Assertions.assertEquals("test1@test.fr", groupMessage.getSender().getEmail(), "Incorrect group message sender");
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* create user *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCreateUser() throws BusinessException, GRIDAClientException
    {
        createUser("test5@test.fr", "suffix5");
        assertRowsNbInTable("VIPUsers", 6);
        Assertions.assertEquals(messageBusiness.verifyMessages("test5@test.fr"), 0, "Incorrect number of unread messages");
    }

    @Test
    public void testCatchExistingEmailCreateUser() throws BusinessException, GRIDAClientException
    {
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
    public void testCreateGroup() throws DAOException, BusinessException {
        Group group = new Group("group2", true, true, true);
        configurationBusiness.addGroup(group);
    }

    @Test
    public void testCatchCreateGroupAlreadyExisting() {
        Group group = new Group("group1", true, true, true);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addGroup(group)
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is already a group registered with the name : group1"));
    }


    /* ******************************************************************************************************************************************************* */
    /* **************************************************************** create message ***************************************************************** */
    /* ******************************************************************************************************************************************************* */

    @Test
    public void testCreateMessage() throws BusinessException {
        // With parameters
        Message message = new Message(1L, configurationBusiness.getUser("test1@test.fr"), configurationBusiness.getUser("test2@test.fr"), "title", "message", "22/06/2023", new Date(), false);

        // Without parameters
        Message message2 = new Message();

    }


    /* ********************************************************************************************************************************************** */
    /* ************************************************************* add user to group *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testAddUserToGroup() throws BusinessException, GRIDAClientException {
        configurationBusiness.addUserToGroup("test4@test.fr", "group1");
    }

    @Test
    public void testCatchInexistingUserAddUserToGroup() {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addUserToGroup("inexisting user", "group1")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "Referential integrity constraint violation"));

    }

    @Test
    public void testCatchInexistingGroupAddUserToGroup() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addUserToGroup("test4@test.fr", "inexisting group")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));

    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* send individual message *********************************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    @Test
    public void testSendMessage() throws BusinessException {
        messageBusiness.sendMessage(configurationBusiness.getUser("test2@test.fr"), new String[]{"test1@test.fr", "test3@test.fr"}, "subject user 2","message user 2");
        assertRowsNbInTable("VIPSocialMessage", 2);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 4);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr"), 2, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr"), 2, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test1@test.fr", new Date()).size(), 2, "Invalid number of individual messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test2@test.fr", new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test3@test.fr", new Date()).size(), 2, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test4@test.fr", new Date()).size(), 0, "Incorrect number of individual messages received");
    }

    //OK
    @Test
    public void testSendMessageAll() throws BusinessException {
        messageBusiness.sendMessage(configurationBusiness.getUser("test2@test.fr"),new String[]{"All"},"subject user 2","message user 2");
        assertRowsNbInTable("VIPSocialMessage", 2);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 7);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr"), 2, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr"), 1, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr"), 2, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages("test4@test.fr"), 1, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 1, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test1@test.fr", new Date()).size(), 2, "Invalid number of individual messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test2@test.fr", new Date()).size(), 1, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test3@test.fr", new Date()).size(), 2, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test4@test.fr", new Date()).size(), 1, "Incorrect number of individual messages received");

        Assertions.assertEquals(messageBusiness.getGroupMessages("group1", new Date()).size(), 1, "Incorrect number of group messages received");
    }

    //OK
    @Test
    public void testCatchInexistantUserSendMessage() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.sendMessage(configurationBusiness.getUser("inexistant user"),new String[]{"test1@test.fr", "test3@test.fr"},"subject user 2","message user 2")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexistant user"));
    }

    /* ********************************************************************************************************************************************** */
    /* *********************************************************** remove individual message ******************************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    @Test
    public void testRemoveMessage() throws BusinessException {
        //Parmi tous les messages individuels
        messageBusiness.remove(messageBusiness.getMessagesByUser("test1@test.fr", new Date(System.currentTimeMillis())).get(0).getId());

        assertRowsNbInTable("VIPSocialMessage", 0);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 0);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test1@test.fr", new Date()).size(), 0, "Invalid number of indivud messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test2@test.fr", new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test3@test.fr", new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test4@test.fr", new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getGroupMessages("group1", new Date()).size(), 1, "Incorrect number of group messages received");
    }


    //OK
    @Test
    public void testCatchRemoveMessage() throws BusinessException {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.remove(100)
                );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no individual message registered with the id : 100"));
    }


    /* ********************************************************************************************************************************************** */
    /* ****************************************************** remove individual message from receiver  **************************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    @Test
    public void testRemoveByReceiver() throws BusinessException {
        messageBusiness.removeByReceiver(messageBusiness.getMessagesByUser("test1@test.fr", new Date(System.currentTimeMillis())).get(0).getId(), "test3@test.fr");
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 1);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr"), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
    }

    //OK
    @Test
    public void testCatchInexistingUserRemoveByReceiver() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.removeByReceiver(messageBusiness.getMessagesByUser("test1@test.fr", new Date(System.currentTimeMillis())).get(0).getId(), "inexisting user")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user or individual message registered with this email : inexisting user or individual message with this id : "+messageBusiness.getMessagesByUser("test1@test.fr", new Date(System.currentTimeMillis())).get(0).getId()));
    }

    //OK
    @Test
    public void testCatchInexistingMessageRemoveByReceiver() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.removeByReceiver(2, "test3@test.fr")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user or individual message registered with this email : test3@test.fr or individual message with this id : 2"));
    }

    /* ********************************************************************************************************************************************** */
    /* ****************************************************** send individual message with support in copy **************************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    @Test
    public void testCopyMessageToVipSupport() throws BusinessException {
        messageBusiness.copyMessageToVipSupport
        (
            configurationBusiness.getUser("test1@test.fr"),
            new String[]{"test1@test.fr", "test3@test.fr"},
            "subject test copy message to Vip support",
            "message test copy message to Vip support"
        );

        // Nothing changes
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(1, messageBusiness.verifyMessages("test1@test.fr"), "Incorrect number of unread messages");
        Assertions.assertEquals(0, messageBusiness.verifyMessages("test2@test.fr"), "Incorrect number of unread messages");
        Assertions.assertEquals(1, messageBusiness.verifyMessages("test3@test.fr"),"Incorrect number of unread messages");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(adminEmail),"Incorrect number of unread messages");
    }

    //OK
    @Test
    public void testCatchRCopyMessageToVipSupport() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.copyMessageToVipSupport(configurationBusiness.getUser("inexisting user"), new String[]{"test1@test.fr", "test3@test.fr"},"subject test copy message to Vip support","message test copy message to Vip support")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************** send individual message to support ***************************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    @Test
    public void testSendMessageToVipSupport() throws BusinessException {
        messageBusiness.sendMessageToVipSupport
        (
            configurationBusiness.getUser("test2@test.fr"),
            "subject",
            "message from test2@test.fr to Vip support",
            List.of("workflow 1", "workflow 2"),
            List.of("simulation 1", "simulation 2")
        );

        // Nothing changes
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr"), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr"), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
    }

    //OK
    @Test
    public void testCatchInexistingEmailSendMessageToVipSupport() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.sendMessageToVipSupport(
                                configurationBusiness.getUser("inexisting user"),"subject","message from test2@test.fr to Vip support", List.of("workflow 1", "workflow 2"), List.of("simulation 1", "simulation 2"))
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ****************************************************** mark individual message as read **************************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    @Test
    public void testMarkAsRead() throws BusinessException {
        messageBusiness.markAsRead
        (
            messageBusiness.getMessagesByUser("test1@test.fr",
            new Date(System.currentTimeMillis())).get(0).getId(),
            "test1@test.fr"
        );

        // Nothing changes
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr"), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
    }

    //OK
    @Test
    public void testCatchInexistingUserMarkAsRead() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.markAsRead
                        (
                            messageBusiness.getMessagesByUser("test1@test.fr", new Date(System.currentTimeMillis())).get(0).getId(),
                            "inexisting user"
                        )
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is/are no user and/or individual message registered with the given parameters : inexisting user - "+messageBusiness.getMessagesByUser("test1@test.fr", new Date(System.currentTimeMillis())).get(0).getId()));
    }

    //OK
    @Test
    public void testCatchInexistingMessageMarkAsRead() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.markAsRead(2, "test1@test.fr")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is/are no user and/or individual message registered with the given parameters : test1@test.fr - 2"));
    }

    /* ********************************************************************************************************************************************** */
    /* **************************************************************** get individual message by user ***************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetMessageByUser() throws BusinessException
    {
        messageBusiness.getMessagesByUser("test1@test.fr", new Date());
    }

    @Test
    public void testCatchGetMessageByUser() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getMessagesByUser("inexisting user", new Date())
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    /* ********************************************************************************************************************************************** */
    /* **************************************************************** send group message ***************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testSendGroupMessage() throws BusinessException {
        messageBusiness.sendGroupMessage
        (
            configurationBusiness.getUser("test2@test.fr"),
    "group1", configurationBusiness.getUsersFromGroup("group1"),
                "subject user 2",
                "message user 2"
        );

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 2);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr"), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr"), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test1@test.fr", new Date()).size(), 1, "Invalid number of individual messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test2@test.fr", new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test3@test.fr", new Date()).size(), 1, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test4@test.fr", new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getGroupMessages("group1", new Date()).size(), 2, "Incorrect number of group messages received");
    }

    @Test
    public void testCatchInexistingSenderGroupMessage() throws BusinessException {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.sendGroupMessage
                        (
                            configurationBusiness.getUser("inexisting user"),
                "group1",
                            configurationBusiness.getUsersFromGroup("group1"),
                            "subject user 2",
                            "message user 2"
                        )
                );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    @Test
    public void testCatchInexistingGroupMessage() throws BusinessException {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.sendGroupMessage
                        (
                                configurationBusiness.getUser("test3@test.fr"),
                                "inexisting group",
                                configurationBusiness.getUsersFromGroup("group1"),
                                "subject user 2",
                                "message user 2"
                        )
                );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));
    }

    @Test
    public void testCatchInexistingUsersGroupMessage() throws BusinessException {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.sendGroupMessage(configurationBusiness.getUser("test3@test.fr"), "group1", configurationBusiness.getUsersFromGroup("inexisting group"),"subject user 2","message user 2"));
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* remove group message *********************************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    @Test
    public void testRemoveGroupMessage() throws BusinessException {

        messageBusiness.removeGroupMessage(messageBusiness.getGroupMessages("group1", new Date()).get(0).getId());

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 0);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr"), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr"), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr"), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");

        Assertions.assertEquals(messageBusiness.getGroupMessages("group1", new Date()).size(), 0, "Incorrect number of group messages received");
    }

    //OK
    @Test
    public void testCatchRemoveGroupMessage() {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.removeGroupMessage(2) // inexisting group message id
                );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group message registered with the id : 2"));

    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* remove user *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchRemoveUser() throws BusinessException {
        configurationBusiness.removeUser("test1@test.fr", false);
        assertRowsNbInTable("VIPUsers", 4);
    }

    @Test
    public void testCatchRemoveInexistingUser() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.removeUser("inexisting user", false)

        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* remove group *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveGroup() throws BusinessException {
        configurationBusiness.removeGroup("test1@test.fr", "group1");
    }

    @Test
    public void testCatchRemoveInexistingGroup() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.removeGroup("test1@test.fr", "inexisting group")

        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the name : inexisting group"));
    }

    //FIXME : reussir à check inexisting user
    /*@Test
    public void testCatchInexistingUserRemoveGroup() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.removeGroup("inexisting user", "group1")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }*/


    /* ******************************************************************************************************************************************************* */
    /* **************************************************************** get message by group ***************************************************************** */
    /* ******************************************************************************************************************************************************* */

    //OK
    @Test
    public void testGetGroupMessages() throws BusinessException {
        messageBusiness.getGroupMessages("group1", new Date());
    }

    //OK
    @Test
    public void testCatchGetInexistingGroupMessages() throws BusinessException, GRIDAClientException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getGroupMessages("inexisting group", new Date())
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));
    }

    /* ******************************************************************************************************************************************************* */
    /* **************************************************************** get individual message by user ***************************************************************** */
    /* ******************************************************************************************************************************************************* */


    //OK
    @Test
    public void testGetSentMessageByUser() throws BusinessException {
        Assertions.assertEquals(messageBusiness.getSentMessagesByUser("test1@test.fr", new Date()).size(), 0, "Incorrect number of individual messages sent");
    }

    //OK
    @Test
    public void testAdminGetSentMessageByUser() throws BusinessException
    {
        Assertions.assertEquals(messageBusiness.getSentMessagesByUser(adminEmail, new Date()).size(), 1, "Incorrect number of individual messages sent");
    }

    //OK
    @Test
    public void testCatchGetSentMessageByUser() {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getSentMessagesByUser("inexisting user", new Date())
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    /* ******************************************************************************************************************************************************* */
    /* **************************************************************** verify messages ***************************************************************** */
    /* ******************************************************************************************************************************************************* */


    //OK
    @Test
    public void testCatchIncorrectEmailVerifyMessages() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.verifyMessages("inexisting user")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));

    }

}
