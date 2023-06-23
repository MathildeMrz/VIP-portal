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
        Group group1 = new Group("group1", true, true, true);
        configurationBusiness.addGroup(group1);
        configurationBusiness.getOrCreateUser("test1@test.fr", "institution", "group1");
        configurationBusiness.getOrCreateUser("test2@test.fr", "institution", "group1");
        configurationBusiness.getOrCreateUser("test3@test.fr", "institution", "group1");
        configurationBusiness.getOrCreateUser("test4@test.fr", "institution", null);
        admin = configurationBusiness.getUser(ServerMockConfig.TEST_ADMIN_EMAIL);
        adminEmail = ServerMockConfig.TEST_ADMIN_EMAIL;
        messageBusiness.sendMessage(admin, new String[]{"test1@test.fr", "test3@test.fr"}, "test subject", "test message");
        messageBusiness.sendGroupMessage(configurationBusiness.getUser("test1@test.fr"), "group1", configurationBusiness.getUsersFromGroup("group1"), "subject user 1", "message user 1");
    }

    @Test
    public void testCatchCreateGroupAlreadyExisting() throws BusinessException {
        Group group1 = new Group("group1", true, true, true);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addGroup(group1)
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is already a group registered with the name :"));

    }

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

        Assertions.assertEquals(1, messageBusiness.getMessagesByUser("test1@test.fr", new Date()).size(), "Incorrect number of individual messages");
        Assertions.assertEquals(0, messageBusiness.getMessagesByUser("test2@test.fr", new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(1, messageBusiness.getMessagesByUser("test3@test.fr", new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(0, messageBusiness.getMessagesByUser("test4@test.fr", new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(1, messageBusiness.getGroupMessages("group1", new Date()).size(), "Incorrect number of group messages received");
        Assertions.assertEquals("test-admin@test.com", individualMessage.getSender().getEmail(), "Incorrect sender email");
        Assertions.assertEquals("test message", individualMessage.getMessage(), "Incorrect message");

        //Assertions.assertEquals(formattedDate, individualMessage.getPosted(), "Incorrect message posted");
        Assertions.assertEquals(false, individualMessage.isRead(), "Incorrect message isRead");
        Assertions.assertEquals("test subject", individualMessage.getTitle(), "Incorrect message title");

        Assertions.assertEquals("test1@test.fr", individualMessage.getReceivers()[0].getEmail(), "Incorrect message receivers number");
        Assertions.assertEquals("test3@test.fr", messageBusiness.getMessagesByUser("test3@test.fr", new Date()).get(0).getReceivers()[0].getEmail(), "Incorrect message receivers number");

        Assertions.assertEquals("group1", groupMessage.getGroupName(), "Incorrect group name");
        Assertions.assertEquals("message user 1", groupMessage.getMessage(), "Incorrect group message");
        Assertions.assertEquals("test1@test.fr", groupMessage.getSender().getEmail(), "Incorrect group message sender");
    }

    @Test
    public void testCreateUser() throws BusinessException, GRIDAClientException {
        configurationBusiness.getOrCreateUser("test5@test.fr", "institution", "group1");

        assertRowsNbInTable("VIPUsers", 6);
        Assertions.assertEquals(messageBusiness.verifyMessages("test5@test.fr"), 0, "Incorrect number of unread messages");
    }

    // FIXME , Même adresse mail => pas d'erreur juste get
    @Test
    public void testCatchExistingEmailCreateUser() throws BusinessException, GRIDAClientException {
        configurationBusiness.getOrCreateUser("test4@test.fr", "institution", "group1");

    }

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

    @Test
    public void testMessageIsSent() throws BusinessException {
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

    @Test
    public void testAllMessageIsSent() throws BusinessException {
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

    @Test
    public void testCatchInexistantUserMessageIsSent() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.sendMessage(configurationBusiness.getUser("inexistant user"),new String[]{"test1@test.fr", "test3@test.fr"},"subject user 2","message user 2")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexistant user"));
    }

    /* ********************************************************************************************************************************************** */
    /* *********************************************************** remove individual message ******************************************************** */
    /* ********************************************************************************************************************************************** */

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


    @Test
    public void testCatchRemoveMessage() throws BusinessException {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.remove(100)
                );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no individual message registered with the id : 100"));
    }


    /* ********************************************************************************************************************************************** */
    /* ****************************************************** remove receiver individual message **************************************************** */
    /* ********************************************************************************************************************************************** */
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

    @Test
    public void testCatchInexistingUserRemoveByReceiver() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.removeByReceiver(messageBusiness.getMessagesByUser("test1@test.fr", new Date(System.currentTimeMillis())).get(0).getId(), "inexisting user")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    @Test
    public void testCatchInexistingMessageemoveByReceiver() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.removeByReceiver(2, "test3@test.fr")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no individual message registered with the id : 2"));
    }

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

    // test catch
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

    @Test
    public void testCatchInexistingEmailSendMessageToVipSupport() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.sendMessageToVipSupport(
                                configurationBusiness.getUser("inexisting user"),"subject","message from test2@test.fr to Vip support", List.of("workflow 1", "workflow 2"), List.of("simulation 1", "simulation 2"))
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

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

    @Test
    public void testCatchInexistingUserMarkAsRead() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.markAsRead
                        (
                            messageBusiness.getMessagesByUser
                            (
                        "test1@test.fr",
                                new Date(System.currentTimeMillis())).get(0).getId(),
                            "inexisting user"
                        )
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    @Test
    public void testCatchInexistingMessageMarkAsRead() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.markAsRead(2, "test1@test.fr")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no individual message registered with the id : 2"));
    }

    /* ********************************************************************************************************************************************** */
    /* **************************************************************** Get individual message by user ***************************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCatchGetMessageByUser() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getMessagesByUser("inexisting user", new Date())
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    // Sending a correct message to a group
    @Test
    public void testGroupMessageIsSent() throws BusinessException {
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


    // Sending a message to a group with an inexisting sender email
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

    // Sending a message to a group with an inexisting group name
    @Test
    public void testCatchInexistingGroupGroupMessage() throws BusinessException {
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

    @Test
    public void testCatchRemoveGroupMessage() {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.removeGroupMessage(2) // inexisting group message id
                );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group message registered with the id : 2"));

    }

    /* ******************************************************************************************************************************************************* */
    /* **************************************************************** Get message by group ***************************************************************** */
    /* ******************************************************************************************************************************************************* */

    @Test
    public void testCatchGetMessageByGroup() throws BusinessException, GRIDAClientException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getGroupMessages("inexisting group", new Date())
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));
    }

    /* ******************************************************************************************************************************************************* */
    /* **************************************************************** Get individual message by user ***************************************************************** */
    /* ******************************************************************************************************************************************************* */


    @Test
    public void testGetSentMessageByUser() throws BusinessException {
        Assertions.assertEquals(messageBusiness.getSentMessagesByUser("test1@test.fr", new Date()).size(), 0, "Incorrect number of individual messages sent");
    }

    @Test
    public void testAdminGetSentMessageByUser() throws BusinessException {

        Assertions.assertEquals(messageBusiness.getSentMessagesByUser(adminEmail, new Date()).size(), 1, "Incorrect number of individual messages sent");
    }

    @Test
    public void testCatchGetSentMessageByUser() {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getSentMessagesByUser("inexisting user", new Date())
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));

    }

    @Test
    public void testCatchIncorrectEmailVerifyMessages() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.verifyMessages("inexisting user")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));

    }

    @Test
    public void testCatchInexistingUserAssociateMessageToUser() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.sendMessage(configurationBusiness.getUser("test1@test.fr"), new String[]{"inexisting user"}, "subject", "message")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));

    }

    @Test
    public void testCreateMessage() throws BusinessException {
        // With parameters
        Message message = new Message(1L, configurationBusiness.getUser("test1@test.fr"), configurationBusiness.getUser("test2@test.fr"), "title", "message", "22/06/2023", new Date(), false);

        // Without parameters
        Message message2 = new Message();

    }

}
