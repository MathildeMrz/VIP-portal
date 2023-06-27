package fr.insalyon.creatis.vip.social.integrationtest;


import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.core.integrationtest.ServerMockConfig;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.core.server.business.VipSessionBusiness;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.social.client.bean.GroupMessage;
import fr.insalyon.creatis.vip.social.client.bean.Message;
import fr.insalyon.creatis.vip.social.server.business.MessageBusiness;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SocialSpringServerIT extends BaseSpringIT {
    @Autowired
    private MessageBusiness messageBusiness;

    @Autowired
    private VipSessionBusiness vipSessionBusiness;

    @Autowired
    private ApplicationContext applicationContext;

    private User admin;
    private String adminEmail;
    private User user1;
    private final String emailUser1 = "test1@test.fr";
    private User user2;
    private final String emailUser2 = "test2@test.fr";
    private User user3;
    private final String emailUser3 = "test3@test.fr";
    private User user4;
    private final String emailUser4 = "test4@test.fr";
    private final String nameGroup1 = "group1";

    @BeforeEach
    public void setUp() throws BusinessException, GRIDAClientException, DAOException {
        super.setUp();

        // Create test group
        Group group1 = new Group(nameGroup1, true, true, true);
        configurationBusiness.addGroup(group1);

        // Create test users
        createUserInGroup(emailUser1, "suffix1", nameGroup1);
        createUserInGroup(emailUser2, "suffix2", nameGroup1);
        createUserInGroup(emailUser3, "suffix3", nameGroup1);
        createUser(emailUser4, "suffix4");

        // Get test users
        user1 = configurationBusiness.getUser(emailUser1);
        user2 = configurationBusiness.getUser(emailUser2);
        user3 = configurationBusiness.getUser(emailUser3);
        user4 = configurationBusiness.getUser(emailUser4);
        adminEmail = ServerMockConfig.TEST_ADMIN_EMAIL;
        admin = configurationBusiness.getUser(adminEmail);

        // Send test messages
        messageBusiness.sendMessage(admin, new String[]{emailUser1, emailUser3}, "test subject", "test message");
        messageBusiness.sendGroupMessage(user1, nameGroup1,
                configurationBusiness.getUsersFromGroup(nameGroup1),
                "subject user 1", "message user 1");

    }

    @Test
    public void testInitialisation() throws BusinessException {
        Message firstIndividualMessage = messageBusiness.getMessagesByUser(emailUser1, new Date()).get(0);
        GroupMessage firstGroupMessage = messageBusiness.getGroupMessages(nameGroup1, new Date()).get(0);
        List<Message> sentMessagesByAdmin = messageBusiness.getSentMessagesByUser(adminEmail, new Date());

        // verify entry numbers in each table
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        // verify message nb by user
        Assertions.assertEquals(1, messageBusiness.getMessagesByUser(emailUser1, new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(0, messageBusiness.getMessagesByUser(emailUser2, new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(1, messageBusiness.getMessagesByUser(emailUser3, new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(0, messageBusiness.getMessagesByUser(emailUser4, new Date()).size(), "Incorrect number of individual messages received");
        Assertions.assertEquals(1, messageBusiness.getGroupMessages(nameGroup1, new Date()).size(), "Incorrect number of group messages received");
        Assertions.assertEquals(1, messageBusiness.verifyMessages(emailUser1), "Incorrect number of messages not read");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(emailUser2), "Incorrect number of messages not read");
        Assertions.assertEquals(1, messageBusiness.verifyMessages(emailUser3), "Incorrect number of messages not read");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(adminEmail), "Incorrect number of messages not read");

        // verify first individual message properties (sender, subject, content, receiver)
        Assertions.assertEquals("test-admin@test.com", firstIndividualMessage.getSender().getEmail(), "Incorrect sender email");
        Assertions.assertEquals("test message", firstIndividualMessage.getMessage(), "Incorrect message");
        Assertions.assertEquals(false, firstIndividualMessage.isRead(), "Incorrect message isRead");
        Assertions.assertEquals("test subject", firstIndividualMessage.getTitle(), "Incorrect message title");
        Assertions.assertEquals(1, firstIndividualMessage.getReceivers().length, "Incorrect message receivers number");
        Assertions.assertEquals(emailUser1, firstIndividualMessage.getReceivers()[0].getEmail(), "Incorrect message receivers");

        // verify first individual message receivers
        Assertions.assertEquals(1, sentMessagesByAdmin.size(), "Incorrect admin messages number");
        List<String> receivers = Arrays.stream(sentMessagesByAdmin.get(0).getReceivers()).map(User::getEmail).collect(Collectors.toList());
        Assertions.assertEquals(2, receivers.size(), "Incorrect message receivers number");
        Assertions.assertTrue(receivers.containsAll(Arrays.asList(emailUser1, emailUser3)), "Incorrect message receivers");

        // verify group message properties
        Assertions.assertEquals(emailUser3, messageBusiness.getMessagesByUser(emailUser3, new Date()).get(0).getReceivers()[0].getEmail(), "Incorrect message receivers number");
        Assertions.assertEquals(nameGroup1, firstGroupMessage.getGroupName(), "Incorrect group name");
        Assertions.assertEquals("message user 1", firstGroupMessage.getMessage(), "Incorrect group message");
        Assertions.assertEquals(emailUser1, firstGroupMessage.getSender().getEmail(), "Incorrect group message sender");
    }


    /* ********************************************************************************************************************************************** */
    /* ************************************************************ send individual message ********************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testSendMessage() throws Exception {
        messageBusiness.sendMessage(user1, new String[]{emailUser1, emailUser3}, "subject user 2", "message user 2");

        // verify entry numbers in each table
        assertRowsNbInTable("VIPSocialMessage", 2);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 4);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        // verify number of messages by user
        Assertions.assertEquals(2, messageBusiness.getMessagesByUser(emailUser1, new Date()).size(), "Nombre incorrect de messages individuels");
        Assertions.assertEquals(0, messageBusiness.getMessagesByUser(emailUser2, new Date()).size(), "Nombre incorrect de messages individuels reçus");
        Assertions.assertEquals(2, messageBusiness.getMessagesByUser(emailUser3, new Date()).size(), "Nombre incorrect de messages individuels reçus");
        Assertions.assertEquals(0, messageBusiness.getMessagesByUser(emailUser4, new Date()).size(), "Nombre incorrect de messages individuels reçus");
        Assertions.assertEquals(2, messageBusiness.verifyMessages(emailUser1), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(emailUser2), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(2, messageBusiness.verifyMessages(emailUser3), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(adminEmail), "Nombre incorrect de messages non lus");

        // verify nb group message
        Assertions.assertEquals(1, messageBusiness.getGroupMessages(nameGroup1, new Date()).size(), "Nombre incorrect de messages de groupe reçus");
    }

    @Test
    public void testSendMessageAll() throws BusinessException {
        messageBusiness.sendMessage(user2, new String[]{"All"}, "subject user 2", "message user 2");

        // verify entry numbers in each table
        assertRowsNbInTable("VIPSocialMessage", 2);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 7);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        // verify number of messages by user
        Assertions.assertEquals(2, messageBusiness.getMessagesByUser(emailUser1, new Date()).size(), "Nombre incorrect de messages individuels");
        Assertions.assertEquals(1, messageBusiness.getMessagesByUser(emailUser2, new Date()).size(), "Nombre incorrect de messages individuels reçus");
        Assertions.assertEquals(2, messageBusiness.getMessagesByUser(emailUser3, new Date()).size(), "Nombre incorrect de messages individuels reçus");
        Assertions.assertEquals(1, messageBusiness.getMessagesByUser(emailUser4, new Date()).size(), "Nombre incorrect de messages individuels reçus");
        Assertions.assertEquals(2, messageBusiness.verifyMessages(emailUser1), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(1, messageBusiness.verifyMessages(emailUser2), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(2, messageBusiness.verifyMessages(emailUser3), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(1, messageBusiness.verifyMessages(emailUser4), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(1, messageBusiness.verifyMessages(adminEmail), "Nombre incorrect de messages non lus");

        // vérifier le nombre de messages de groupe
        Assertions.assertEquals(1, messageBusiness.getGroupMessages(nameGroup1, new Date()).size(), "Nombre incorrect de messages de groupe reçus");

    }

    @Test
    public void testCatchNotExistentUserSendMessage() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.sendMessage(
                                configurationBusiness.getUser("not_existent_user"),
                                new String[]{emailUser1, emailUser3},
                                "subject user 2", "message user 2")
        );

        // in VIPUsers table email is a primary key, not a foreign key, so no exception is raised by default => code modified
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : not_existent_user"));
    }


    /* ********************************************************************************************************************************************** */
    /* *********************************************************** remove individual message ******************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveMessage() throws BusinessException
    {
        messageBusiness.remove(messageBusiness.getMessagesByUser(emailUser1, new Date(System.currentTimeMillis())).get(0).getId());

        // verify entry numbers in each table
        assertRowsNbInTable("VIPSocialMessage", 0);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 0);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        // verify number of messages by user
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser1, new Date()).size(), 0, "Invalid number of indivud messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser2, new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser3, new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser4, new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getGroupMessages(nameGroup1, new Date()).size(), 1, "Incorrect number of group messages received");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser1), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser2), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser3), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
    }


    @Test
    public void testCatchRemoveMessage() throws BusinessException {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.remove(100)
                );

        // in VIPMessages table id is a primary key, not a foreign key, so no exception is raised by default => code modified
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no individual message registered with the id : 100"));
    }


    /* ********************************************************************************************************************************************** */
    /* *************************************************** remove individual message from receiver  ************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveByReceiver() throws BusinessException {
        messageBusiness.removeByReceiver(messageBusiness.getMessagesByUser(emailUser1, new Date(System.currentTimeMillis())).get(0).getId(), emailUser3);

        // verify entry numbers in each table
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 1);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        // verify number of messages by user
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser1, new Date()).size(), 1, "Invalid number of indivud messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser2, new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser3, new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser4, new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(1, messageBusiness.verifyMessages(emailUser1), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(emailUser2), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(emailUser3), "Nombre incorrect de messages non lus");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(adminEmail), "Nombre incorrect de messages non lus");

    }

    @Test
    public void testCatchInexistingUserRemoveByReceiver() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.removeByReceiver(messageBusiness.getMessagesByUser(emailUser1, new Date(System.currentTimeMillis())).get(0).getId(), "inexisting user")
        );

        // in VIPMessages table receiver is a foreign key, so no exception is raised by default => code modified
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user or individual message registered with this email : inexisting user or individual message with this id : " + messageBusiness.getMessagesByUser(emailUser1, new Date(System.currentTimeMillis())).get(0).getId()));
    }

    //OK
    @Test
    public void testCatchInexistingMessageRemoveByReceiver() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.removeByReceiver(2, emailUser3)
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user or individual message registered with this email : test3@test.fr or individual message with this id : 2"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************* send individual message with support in copy *********************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    @Test
    public void testCopyMessageToVipSupport() throws BusinessException {
        messageBusiness.copyMessageToVipSupport
                (
                        user1,
                        new String[]{emailUser1, emailUser3},
                        "subject test copy message to Vip support",
                        "message test copy message to Vip support"
                );

        // Nothing changes
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(1, messageBusiness.verifyMessages(emailUser1), "Incorrect number of unread messages");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(emailUser2), "Incorrect number of unread messages");
        Assertions.assertEquals(1, messageBusiness.verifyMessages(emailUser3), "Incorrect number of unread messages");
        Assertions.assertEquals(0, messageBusiness.verifyMessages(adminEmail), "Incorrect number of unread messages");
    }

    //OK
    @Test
    public void testCatchRCopyMessageToVipSupport() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.copyMessageToVipSupport(configurationBusiness.getUser("inexisting user"), new String[]{emailUser1, emailUser3}, "subject test copy message to Vip support", "message test copy message to Vip support")
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
                        user2,
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
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser1), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser2), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser3), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
    }

    //OK
    @Test
    public void testCatchInexistingEmailSendMessageToVipSupport() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.sendMessageToVipSupport(
                                configurationBusiness.getUser("inexisting user"), "subject", "message from test2@test.fr to Vip support", List.of("workflow 1", "workflow 2"), List.of("simulation 1", "simulation 2"))
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ******************************************************* mark individual message as read ****************************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    @Test
    public void testMarkAsRead() throws BusinessException {
        messageBusiness.markAsRead
                (
                        messageBusiness.getMessagesByUser(emailUser1,
                                new Date(System.currentTimeMillis())).get(0).getId(),
                        emailUser1
                );

        // Nothing changes
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser1), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser2), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser3), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
    }

    //OK
    @Test
    public void testCatchInexistingUserMarkAsRead() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.markAsRead
                                (
                                        messageBusiness.getMessagesByUser(emailUser1, new Date(System.currentTimeMillis())).get(0).getId(),
                                        "inexisting user"
                                )
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is/are no user and/or individual message registered with the given parameters : inexisting user - " + messageBusiness.getMessagesByUser(emailUser1, new Date(System.currentTimeMillis())).get(0).getId()));
    }

    /*
    //OK
    @Test
    public void testCatchInexistingMessageMarkAsRead()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.markAsRead(2, emailUser1)
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is/are no user and/or individual message registered with the given parameters : test1@test.fr - 2"));
    }*/

    /* ********************************************************************************************************************************************** */
    /* ******************************************************* get individual message by user ******************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testGetMessageByUser() throws BusinessException {
        messageBusiness.getMessagesByUser(emailUser1, new Date());
    }

    @Test
    public void testCatchGetMessageByUser() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getMessagesByUser("inexisting user", new Date())
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* send group message ************************************************************* */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testSendGroupMessage() throws BusinessException {
        messageBusiness.sendGroupMessage
                (
                        user2,
                        nameGroup1, configurationBusiness.getUsersFromGroup(nameGroup1),
                        "subject user 2",
                        "message user 2"
                );

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 2);
        assertRowsNbInTable("VIPUsers", 5);
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser1), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser2), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser3), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser1, new Date()).size(), 1, "Invalid number of individual messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser2, new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser3, new Date()).size(), 1, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser(emailUser4, new Date()).size(), 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getGroupMessages(nameGroup1, new Date()).size(), 2, "Incorrect number of group messages received");
    }

    @Test
    public void testCatchInexistingSenderGroupMessage() {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.sendGroupMessage
                                (
                                        configurationBusiness.getUser("inexisting user"),
                                        nameGroup1,
                                        configurationBusiness.getUsersFromGroup(nameGroup1),
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
                                        user3,
                                        "inexisting group",
                                        configurationBusiness.getUsersFromGroup(nameGroup1),
                                        "subject user 2",
                                        "message user 2"
                                )
                );
        assertTrue(StringUtils.contains(exception.getMessage(), "JdbcSQLException: Referential integrity constraint violation"));
    }

    @Test
    public void testCatchInexistingUsersGroupMessage() {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.sendGroupMessage(user3, nameGroup1, configurationBusiness.getUsersFromGroup("inexisting group"), "subject user 2", "message user 2"));
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* remove group message *********************************************************** */
    /* ********************************************************************************************************************************************** */

    //OK
    //FIXME : parfois ne passe pas : [ERROR]   SocialSpringServerIT.testRemoveGroupMessage:512 » IndexOutOfBounds Index 0 out of bounds for length 0
    @Test
    public void testRemoveGroupMessage() throws BusinessException {
        messageBusiness.removeGroupMessage(messageBusiness.getGroupMessages(nameGroup1, new Date()).get(0).getId());

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 0);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser1), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser2), 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(emailUser3), 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(adminEmail), 0, "Incorrect number of unread messages");

        Assertions.assertEquals(messageBusiness.getGroupMessages(nameGroup1, new Date()).size(), 0, "Incorrect number of group messages received");
    }

    //OK
    @Test
    public void testCatchRemoveGroupMessage() {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.removeGroupMessage(2)
                );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group message registered with the id : 2"));

    }



    /* ******************************************************************************************************************************************************* */
    /* **************************************************************** get message by group ***************************************************************** */
    /* ******************************************************************************************************************************************************* */

    //OK
    @Test
    public void testGetGroupMessages() throws BusinessException {
        messageBusiness.getGroupMessages(nameGroup1, new Date());
    }

    //OK
    @Test
    public void testCatchGetInexistingGroupMessages() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getGroupMessages("inexisting group", new Date())
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));
    }

    /* ******************************************************************************************************************************************************* */
    /* *********************************************************** get individual message by user ************************************************************ */
    /* ******************************************************************************************************************************************************* */


    //OK
    @Test
    public void testGetSentMessageByUser() throws BusinessException {
        Assertions.assertEquals(messageBusiness.getSentMessagesByUser(emailUser1, new Date()).size(), 0, "Incorrect number of individual messages sent");
    }

    //OK
    @Test
    public void testAdminGetSentMessageByUser() throws BusinessException {
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
    /* ****************************************************************** verify messages ******************************************************************** */
    /* ******************************************************************************************************************************************************* */


    //OK
    @Test
    public void testCatchIncorrectEmailVerifyMessages() {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.verifyMessages("inexisting user")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));

    }

    public static final String TEST_ADMIN_FIRST_NAME = "test admin";
    public static final String TEST_ADMIN_LAST_NAME = "TEST ADMIN";
    public static final String TEST_ADMIN_EMAIL = "test-admin@test.com";
    public static final String TEST_ADMIN_PASSWORD = "test-admin-password";
    public static final String TEST_ADMIN_INSTITUTION = "test admin institution";

    // Added because the max number of simulations was 0
    public static final String MAX_NUMBER_EXECUTIONS = "5";
    public static final String TEST_CAS_URL = "testCasURL";

    /*public static void reset(Server server) {
        Mockito.reset(server);
        Mockito.when(server.getAdminFirstName()).thenReturn(TEST_ADMIN_FIRST_NAME);
        Mockito.when(server.getAdminLastName()).thenReturn(TEST_ADMIN_LAST_NAME);
        Mockito.when(server.getAdminEmail()).thenReturn(TEST_ADMIN_EMAIL);
        Mockito.when(server.getAdminPassword()).thenReturn(TEST_ADMIN_PASSWORD);
        Mockito.when(server.getAdminInstitution()).thenReturn(TEST_ADMIN_INSTITUTION);
        Mockito.when(server.getCasURL()).thenReturn(TEST_CAS_URL);
        Mockito.when(server.getMaxPlatformRunningSimulations()).thenReturn(Integer.valueOf(MAX_NUMBER_EXECUTIONS));
        when(server.getDataManagerUsersHome()).thenReturn("/test/prefix/vip/data/test_users");
        when(server.getDataManagerGroupsHome()).thenReturn("/test/prefix/vip/data/test_groups");
        when(server.getVoRoot()).thenReturn("/vo_test/root");
    }


    @Test
    public void testRpc() throws SocialException, ServletException {
        /*
        // Simulate user session
        SocialServiceImpl mockSocialServiceImpl = Mockito.mock(SocialServiceImpl.class, Mockito.withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(new SocialServiceImpl()
        {
            @Override
            public User getSessionUser() {
                return user1;
            }

        })));


        mockSocialServiceImpl.init();

        Server server = mock(Server.class);
        reset(server);


        SocialServiceImpl socialService = new SocialServiceImpl();
        socialService.init();
        socialService.sendMessage(new String[]{emailUser1, emailUser3}, "subject user 2","message user 2");
    }*/

}
