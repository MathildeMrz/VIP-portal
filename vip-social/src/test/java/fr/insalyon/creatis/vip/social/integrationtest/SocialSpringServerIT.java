package fr.insalyon.creatis.vip.social.integrationtest;


import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.integrationtest.ServerMockConfig;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.social.server.business.MessageBusiness;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // testing framework must recreate a MessageBusiness after each test method
public class SocialSpringServerIT extends BaseSpringIT
{
    @Autowired
    public MessageBusiness messageBusiness;

    @BeforeEach
    public void setUp() throws BusinessException, GRIDAClientException
    {

        Group group1 = new Group("group1", true, true, true);
        // Adding a new group to configurationBusiness
        configurationBusiness.addGroup(group1);

        // Adding 3 new users to configurationBusiness
        configurationBusiness.getOrCreateUser("test1@test.fr", "institution", "group1");
        configurationBusiness.getOrCreateUser("test2@test.fr", "institution", "group1");
        configurationBusiness.getOrCreateUser("test3@test.fr", "institution", "group1");
        configurationBusiness.getOrCreateUser("test4@test.fr", "institution", null);

        messageBusiness.sendMessage
        (
            configurationBusiness.getUser(ServerMockConfig.TEST_ADMIN_EMAIL),
            new String[]{"test1@test.fr", "test3@test.fr"},
            "test subject", "test message"
        );

        messageBusiness.sendGroupMessage
        (
            configurationBusiness.getUser("test1@test.fr"),
            "group1",
            configurationBusiness.getUsersFromGroup("group1"),
            "subject user 1",
            "message user 1"
        );

    }


    @Test
    public void testCatchCreateGroupAlreadyExisting() throws BusinessException
    {
        Group group1 = new Group("group1", true, true, true);

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addGroup(group1)
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is already a group registered with the name :"));

    }

    @Test
    public void testInitialisation() throws BusinessException
    {
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 1, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 0, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 1, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 0, "Incorrect number of messages not read");

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Assertions.assertEquals(messageBusiness.getMessagesByUser("test1@test.fr", startDate).size() , 1, "Invalid number of individual messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test2@test.fr", startDate).size() , 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test3@test.fr", startDate).size() , 1, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test4@test.fr", startDate).size() , 0, "Incorrect number of individual messages received");

        Assertions.assertEquals(messageBusiness.getGroupMessages("group1", startDate).size() , 1, "Incorrect number of group messages received");

    }

    /* *********************************************************************************************************************************************************************************** */
    /* *********************************************************************************** Create user *********************************************************************************** */
    /* *********************************************************************************************************************************************************************************** */

    // Add user catch
    @Test
    public void testCreateUser() throws BusinessException, GRIDAClientException {
        configurationBusiness.getOrCreateUser("test5@test.fr", "institution", "group1");

        assertRowsNbInTable("VIPUsers", 6);
        Assertions.assertEquals(messageBusiness.verifyMessages("test5@test.fr") , 0, "Incorrect number of unread messages");
    }

    // FIXME , Même adresse mail => pas d'erreur juste get
    @Test
    public void testCatchExistingEmailCreateUser() throws BusinessException, GRIDAClientException
    {
        configurationBusiness.getOrCreateUser("test4@test.fr", "institution", "group1");

    }


    /* *********************************************************************************************************************************************************************************** */
    /* ************************************************************************************ Add user ************************************************************************************* */
    /* *********************************************************************************************************************************************************************************** */
    @Test
    public void testAddUserToGroup() throws BusinessException, GRIDAClientException
    {
        configurationBusiness.addUserToGroup("test4@test.fr", "group1");
    }

    @Test
    public void testCatchInexistingUserAddUserToGroup()
    {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addUserToGroup("inexisting user","group1")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "Referential integrity constraint violation"));

    }

    @Test
    public void testCatchInexistingGroupAddUserToGroup()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addUserToGroup("test4@test.fr","inexisting group")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));

    }

    /* *********************************************************************************************************************************************************************************** */
    /* ******************************************************************************* individual messages ******************************************************************************* */
    /* *********************************************************************************************************************************************************************************** */


    /* ********************************************************************************************************************************************** */
    /* *********************************************************** send individual message ********************************************************** */
    /* ********************************************************************************************************************************************** */
    @Test
    public void testMessageIsSent() throws BusinessException
    {
        messageBusiness.sendMessage
        (
            configurationBusiness.getUser("test2@test.fr"),
            new String[]{"test1@test.fr", "test3@test.fr"},
            "subject user 2",
            "message user 2"
        );

        assertRowsNbInTable("VIPSocialMessage", 2);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 4);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 2, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 2, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 0, "Incorrect number of unread messages");

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Assertions.assertEquals(messageBusiness.getMessagesByUser("test1@test.fr", startDate).size() , 2, "Invalid number of individual messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test2@test.fr", startDate).size() , 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test3@test.fr", startDate).size() , 2, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test4@test.fr", startDate).size() , 0, "Incorrect number of individual messages received");

    }

    @Test
    public void testAllMessageIsSent() throws BusinessException
    {
        messageBusiness.sendMessage
            (
                    configurationBusiness.getUser("test2@test.fr"),
                    new String[]{"All"},
                    "subject user 2",
                    "message user 2"
            );

        assertRowsNbInTable("VIPSocialMessage", 2);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 7);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 2, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 1, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 2, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages("test4@test.fr") , 1, "Incorrect number of messages not read");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 1, "Incorrect number of messages not read");

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Assertions.assertEquals(messageBusiness.getMessagesByUser("test1@test.fr", startDate).size() , 2, "Invalid number of individual messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test2@test.fr", startDate).size() , 1, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test3@test.fr", startDate).size() , 2, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test4@test.fr", startDate).size() , 1, "Incorrect number of individual messages received");

        Assertions.assertEquals(messageBusiness.getGroupMessages("group1", startDate).size() , 1, "Incorrect number of group messages received");
    }

    @Test
    public void testCatchInexistantUserMessageIsSent() throws BusinessException {

        Exception exception = assertThrows(
                BusinessException.class, () ->

                messageBusiness.sendMessage
                (
                        configurationBusiness.getUser("inexistant user"),
                        new String[]{"test1@test.fr", "test3@test.fr"},
                        "subject user 2",
                        "message user 2"
                )
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexistant user"));
    }

    /* ********************************************************************************************************************************************** */
    /* *********************************************************** remove individual message ******************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveMessage() throws BusinessException
    {
        //Parmi tous les messages individuels
        messageBusiness.remove(1);

        assertRowsNbInTable("VIPSocialMessage", 0);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 0);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 0, "Incorrect number of unread messages");

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Assertions.assertEquals(messageBusiness.getMessagesByUser("test1@test.fr", startDate).size() , 0, "Invalid number of indivud messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test2@test.fr", startDate).size() , 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test3@test.fr", startDate).size() , 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test4@test.fr", startDate).size() , 0, "Incorrect number of individual messages received");

        Assertions.assertEquals(messageBusiness.getGroupMessages("group1", startDate).size() , 1, "Incorrect number of group messages received");
    }


    @Test
    public void testCatchRemoveMessage() throws BusinessException
    {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.remove(2)
                );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no individual message registered with the id : 2"));
    }


    /* ********************************************************************************************************************************************** */
    /* ****************************************************** remove receiver individual message **************************************************** */
    /* ********************************************************************************************************************************************** */
    @Test
    public void testRemoveByReceiver() throws BusinessException {
        messageBusiness.removeByReceiver(1, "test3@test.fr");

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 1);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 0, "Incorrect number of unread messages");

    }

    @Test
    public void testCatchInexistingUserRemoveByReceiver() throws BusinessException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                    messageBusiness.removeByReceiver(1, "inexisting user")
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

    /* ********************************************************************************************************************************************** */
    /* *************************************************** send individual message adding support *************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testCopyMessageToVipSupport() throws BusinessException
    {
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

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 0, "Incorrect number of unread messages");

    }

    // test catch
    @Test
    public void testCatchRCopyMessageToVipSupport() throws BusinessException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.copyMessageToVipSupport
                                (
                                        configurationBusiness.getUser("inexisting user"),
                                        new String[]{"test1@test.fr", "test3@test.fr"},
                                        "subject test copy message to Vip support",
                                        "message test copy message to Vip support"
                                )
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));

    }

    /* ********************************************************************************************************************************************** */
    /* ***************************************************** send individual message to support ***************************************************** */
    /* ********************************************************************************************************************************************** */
    @Test
    public void testSendMessageToVipSupport() throws BusinessException
    {

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

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 0, "Incorrect number of unread messages");
    }

    @Test
    public void testCatchInexistingEmailSendMessageToVipSupport() throws BusinessException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
            messageBusiness.sendMessageToVipSupport
                    (
                            configurationBusiness.getUser("inexisting user"),
                            "subject",
                            "message from test2@test.fr to Vip support",
                            List.of("workflow 1", "workflow 2"),
                            List.of("simulation 1", "simulation 2")

                    )
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));


    }



    /* ********************************************************************************************************************************************** */
    /* ****************************************************** mark individual message as read ******************************************************* */
    /* ********************************************************************************************************************************************** */
    @Test
    public void testMarkAsRead() throws BusinessException
    {
        messageBusiness.markAsRead(1, "test1@test.fr");

        // Nothing changes
        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 0, "Incorrect number of unread messages");
    }

    @Test
    public void testCatchInexistingUserMarkAsRead() throws BusinessException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                    messageBusiness.markAsRead(1, "inexisting user")
        );
        System.out.println("exception.getMessage(): "+exception.getMessage());
        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));

    }

    @Test
    public void testCatchInexistingMessageMarkAsRead() throws BusinessException
    {
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

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getMessagesByUser("inexisting user", startDate)
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));

    }


    /* *********************************************************************************************************************************************************************************** */
    /* ********************************************************************************* group messages ********************************************************************************** */
    /* *********************************************************************************************************************************************************************************** */

    /* ********************************************************************************************************************************************** */
    /* ************************************************************** send group message ************************************************************ */
    /* ********************************************************************************************************************************************** */
    @Test
    public void testGroupMessageIsSent() throws BusinessException
    {

        messageBusiness.sendGroupMessage
        (
            configurationBusiness.getUser("test2@test.fr"),
            "group1",
            configurationBusiness.getUsersFromGroup("group1"),
            "subject user 2",
            "message user 2"
        );

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 2);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 0, "Incorrect number of unread messages");

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Assertions.assertEquals(messageBusiness.getMessagesByUser("test1@test.fr", startDate).size() , 1, "Invalid number of individual messages");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test2@test.fr", startDate).size() , 0, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test3@test.fr", startDate).size() , 1, "Incorrect number of individual messages received");
        Assertions.assertEquals(messageBusiness.getMessagesByUser("test4@test.fr", startDate).size() , 0, "Incorrect number of individual messages received");

        Assertions.assertEquals(messageBusiness.getGroupMessages("group1", startDate).size() , 2, "Incorrect number of group messages received");
    }


    @Test
    public void testCatchInexistingSenderGroupMessage() throws BusinessException {

        // Inexisting sender
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
    public void testCatchInexistingGroupGroupMessage() throws BusinessException {

        // Inexisting group
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
                        messageBusiness.sendGroupMessage
                        (
                                configurationBusiness.getUser("test3@test.fr"),
                                "group1",
                                configurationBusiness.getUsersFromGroup("inexisting group"),
                                "subject user 2",
                                "message user 2"
                        )
                );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));

    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* remove group message *********************************************************** */
    /* ********************************************************************************************************************************************** */

    @Test
    public void testRemoveGroupMessage() throws BusinessException
    {
        messageBusiness.removeGroupMessage(1);

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 0);
        assertRowsNbInTable("VIPUsers", 5);

        Assertions.assertEquals(messageBusiness.verifyMessages("test1@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test2@test.fr") , 0, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages("test3@test.fr") , 1, "Incorrect number of unread messages");
        Assertions.assertEquals(messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) , 0, "Incorrect number of unread messages");

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Assertions.assertEquals(messageBusiness.getGroupMessages("group1", startDate).size() , 0, "Incorrect number of group messages received");
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

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getGroupMessages("inexisting group", startDate)
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no group registered with the groupname : inexisting group"));
    }

    /* ******************************************************************************************************************************************************* */
    /* **************************************************************** Get individual message by user ***************************************************************** */
    /* ******************************************************************************************************************************************************* */


    @Test
    public void testGetSentMessageByUser() throws BusinessException {

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Assertions.assertEquals(messageBusiness.getSentMessagesByUser("test1@test.fr", startDate).size() , 0 , "Inccorect number of individual messages sent");
    }

    @Test
    public void testAdminGetSentMessageByUser() throws BusinessException {

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Assertions.assertEquals(messageBusiness.getSentMessagesByUser(ServerMockConfig.TEST_ADMIN_EMAIL, startDate).size() , 1 , "Inccorect number of individual messages sent");
    }

    @Test
    public void testCatchGetSentMessageByUser() {

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getSentMessagesByUser("inexisting user", startDate)
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
    public void testCatchInexistingUserAssociateMessageToUser()
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.sendMessage(configurationBusiness.getUser("test1@test.fr"),new String[]{"inexisting user"}, "subject", "message")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail : inexisting user"));

    }


}
