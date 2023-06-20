package fr.insalyon.creatis.vip.social.integrationtest;


import fr.insalyon.creatis.grida.client.GRIDAClientException;
import fr.insalyon.creatis.vip.core.client.bean.Group;
import fr.insalyon.creatis.vip.core.integrationtest.ServerMockConfig;
import fr.insalyon.creatis.vip.core.integrationtest.database.BaseSpringIT;
import fr.insalyon.creatis.vip.core.server.business.BusinessException;
import fr.insalyon.creatis.vip.social.server.business.MessageBusiness;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Calendar;
import java.util.Date;
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
         // Adding 3 new users to configurationBusiness
        createUser("test1@test.fr", "1");
        createUser("test2@test.fr", "2");
        createUser("test3@test.fr", "3");
        createUser("test4@test.fr", "4");

        Group group1 = new Group("group1", true, true, true);
        // Adding a new group to configurationBusiness
        configurationBusiness.addGroup(group1);

        configurationBusiness.addUserToGroup("test1@test.fr", group1.getName());
        configurationBusiness.addUserToGroup("test2@test.fr", group1.getName());
        configurationBusiness.addUserToGroup("test3@test.fr", group1.getName());

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
    public void testInitialisation() throws BusinessException
    {

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 1);
        assertRowsNbInTable("VIPUsers", 5);

        assert messageBusiness.verifyMessages("test1@test.fr") == 1: "Incorrect number of messages not read";
        assert messageBusiness.verifyMessages("test2@test.fr") == 0: "Incorrect number of messages not read";
        assert messageBusiness.verifyMessages("test3@test.fr") == 1: "Incorrect number of messages not read";
        assert messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) == 0: "Incorrect number of messages not read";

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        assert (messageBusiness.getMessagesByUser("test1@test.fr", startDate).size() == 1): "Invalid number of indivud messages";
        assert (messageBusiness.getMessagesByUser("test2@test.fr", startDate).size() == 0): "Incorrect number of individual messages received";
        assert (messageBusiness.getMessagesByUser("test3@test.fr", startDate).size() == 1): "Incorrect number of individual messages received";
        assert (messageBusiness.getMessagesByUser("test4@test.fr", startDate).size() == 0): "Incorrect number of individual messages received";

        assert (messageBusiness.getGroupMessages("group1", startDate).size() == 1): "Incorrect number of group messages received";

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

        assert messageBusiness.verifyMessages("test1@test.fr") == 2: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test2@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test3@test.fr") == 2: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) == 0: "Incorrect number of unread messages";

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        assert (messageBusiness.getMessagesByUser("test1@test.fr", startDate).size() == 2): "Invalid number of indivud messages";
        assert (messageBusiness.getMessagesByUser("test2@test.fr", startDate).size() == 0): "Incorrect number of individual messages received";
        assert (messageBusiness.getMessagesByUser("test3@test.fr", startDate).size() == 2): "Incorrect number of individual messages received";
        assert (messageBusiness.getMessagesByUser("test4@test.fr", startDate).size() == 0): "Incorrect number of individual messages received";

    }

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

        assert messageBusiness.verifyMessages("test1@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test2@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test3@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) == 0: "Incorrect number of unread messages";

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        assert (messageBusiness.getMessagesByUser("test1@test.fr", startDate).size() == 1): "Invalid number of individual messages";
        assert (messageBusiness.getMessagesByUser("test2@test.fr", startDate).size() == 0): "Incorrect number of individual messages received";
        assert (messageBusiness.getMessagesByUser("test3@test.fr", startDate).size() == 1): "Incorrect number of individual messages received";
        assert (messageBusiness.getMessagesByUser("test4@test.fr", startDate).size() == 0): "Incorrect number of individual messages received";

        assert (messageBusiness.getGroupMessages("group1", startDate).size() == 2): "Incorrect number of group messages received";
    }

    /* ********************************************************************************************************************************************** */
    /* ************************************************************** send group message ************************************************************ */
    /* ********************************************************************************************************************************************** */
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

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail: inexisting user"));

    }

    @Test
    public void testCatchInexistingGroupGroupMessage() throws BusinessException {

        // Inexisting group
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.sendGroupMessage
                                (
                                        configurationBusiness.getUser("test3@test.fr"),
                                        "group2",
                                        configurationBusiness.getUsersFromGroup("group1"),
                                        "subject user 2",
                                        "message user 2"
                                )
                );

        assertTrue(StringUtils.contains(exception.getMessage(), "Referential integrity constraint violation: \"CONSTRAINT_4970: PUBLIC.VIPSOCIALGROUPMESSAGE FOREIGN KEY(GROUPNAME) REFERENCES PUBLIC.VIPGROUPS(GROUPNAME) ('group2')"));
    }

    // FIXME : no exception "Error getting users from group" thrown
    /*@Test
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

         assertTrue(StringUtils.contains(exception.getMessage(), "Error getting users from group"));

    }*/

    /* ********************************************************************************************************************************************** */
    /* ************************************************************* remove group message *********************************************************** */
    /* ********************************************************************************************************************************************** */

    // FIXME : Does not work anymore
    @Test
    public void testRemoveGroupMessage() throws BusinessException
    {
        messageBusiness.removeGroupMessage(1);

        assertRowsNbInTable("VIPSocialMessage", 1);
        assertRowsNbInTable("VIPSocialMessageSenderReceiver", 2);
        assertRowsNbInTable("VIPSocialGroupMessage", 0);
        assertRowsNbInTable("VIPUsers", 5);

        assert messageBusiness.verifyMessages("test1@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test2@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test3@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) == 0: "Incorrect number of unread messages";

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        assert (messageBusiness.getGroupMessages("group1", startDate).size() == 0): "Incorrect number of group messages received";
    }

    // FIXME : no exception "Error removing group message" thrown
    @Test
    public void testCatchRemoveGroupMessage() {
        Exception exception = assertThrows
            (BusinessException.class, () ->
                messageBusiness.removeGroupMessage(2) // inexisting group message id
            );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no message registered with the id 2"));

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

        assert messageBusiness.verifyMessages("test1@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test2@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test3@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) == 0: "Incorrect number of unread messages";

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        assert (messageBusiness.getMessagesByUser("test1@test.fr", startDate).size() == 0): "Invalid number of indivud messages";
        assert (messageBusiness.getMessagesByUser("test2@test.fr", startDate).size() == 0): "Incorrect number of individual messages received";
        assert (messageBusiness.getMessagesByUser("test3@test.fr", startDate).size() == 0): "Incorrect number of individual messages received";
        assert (messageBusiness.getMessagesByUser("test4@test.fr", startDate).size() == 0): "Incorrect number of individual messages received";

        assert (messageBusiness.getGroupMessages("group1", startDate).size() == 1): "Incorrect number of group messages received";
    }


    // FIXME : no exception thrown
    // test catch
    @Test
    public void testCatchRemoveMessage() throws BusinessException
    {
        Exception exception = assertThrows
                (BusinessException.class, () ->
                        messageBusiness.remove(2)
                );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no message registered with the id 2"));
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

        assert messageBusiness.verifyMessages("test1@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test2@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test3@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) == 0: "Incorrect number of unread messages";

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

        //Through VIPSocialMessageSenderReceiver
        assert messageBusiness.verifyMessages("test1@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test2@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test3@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) == 0: "Incorrect number of unread messages";

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

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail: inexisting user"));

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

        assert messageBusiness.verifyMessages("test1@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test2@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test3@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) == 0: "Incorrect number of unread messages";
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

        assert messageBusiness.verifyMessages("test1@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test2@test.fr") == 0: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages("test3@test.fr") == 1: "Incorrect number of unread messages";
        assert messageBusiness.verifyMessages(ServerMockConfig.TEST_ADMIN_EMAIL) == 0: "Incorrect number of unread messages";
    }

    /* ********************************************************************************************************************************************** */
    /* **************************************************************** Create user ***************************************************************** */
    /* ********************************************************************************************************************************************** */

    // Add user catch
    @Test
    public void testCreateUser() throws BusinessException, GRIDAClientException {
        createUser("test5@test.fr", "5");

        assertRowsNbInTable("VIPUsers", 6);
        assert messageBusiness.verifyMessages("test5@test.fr") == 0: "Incorrect number of unread messages";
    }

    @Test
    public void testCatchExistingEmailCreateUser() throws BusinessException, GRIDAClientException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        createUser("test4@test.fr", "5")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "Unique index or primary key violation"));

    }


    @Test
    public void testCatchExistingKeyCreateUser() throws BusinessException, GRIDAClientException {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        createUser("inexisting user", "1")
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "Unique index or primary key violation"));

    }

    /* ********************************************************************************************************************************************** */
    /* **************************************************************** Add user ***************************************************************** */
    /* ********************************************************************************************************************************************** */
    @Test
    public void testAddUserToGroup() throws BusinessException, GRIDAClientException {
        configurationBusiness.addUserToGroup("test4@test.fr", "group1");
    }

    @Test
    public void testCatchInexistingUserAddUserToGroup() throws BusinessException, GRIDAClientException {

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addUserToGroup("inexisting user","group1")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "Referential integrity constraint violation"));

    }

    @Test
    public void testCatchInexistingGroupAddUserToGroup() throws BusinessException, GRIDAClientException
    {
        Exception exception = assertThrows(
                BusinessException.class, () ->
                        configurationBusiness.addUserToGroup("test4@test.fr","Inexisting group")
        );
        assertTrue(StringUtils.contains(exception.getMessage(), "Referential integrity constraint violation"));

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

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail: inexisting user"));

    }


    /* ********************************************************************************************************************************************** */
    /* **************************************************************** Get message by group ***************************************************************** */
    /* ********************************************************************************************************************************************** */

    // FIXME : no exception thrown
    /*@Test
    public void testCatchGetMessageByGroup() throws BusinessException, GRIDAClientException {

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getGroupMessages("inexisting group", startDate)
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "..."));

    }*/

    @Test
    public void testCatchGetSentMessageByUser() {

        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();

        Exception exception = assertThrows(
                BusinessException.class, () ->
                        messageBusiness.getSentMessagesByUser("inexisting user", startDate)
        );

        assertTrue(StringUtils.contains(exception.getMessage(), "There is no user registered with the e-mail: inexisting user"));

    }


}
