/*
 * Copyright and authors: see LICENSE.txt in base repository.
 *
 * This software is a web portal for pipeline execution on distributed systems.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.social.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import fr.insalyon.creatis.vip.core.client.bean.User;
import fr.insalyon.creatis.vip.social.client.bean.GroupMessage;
import fr.insalyon.creatis.vip.social.client.bean.Message;
import fr.insalyon.creatis.vip.social.client.view.SocialException;

import java.util.Date;
import java.util.List;

/**
 * @author Rafael Silva
 */
public interface SocialService extends RemoteService {

    public static final String SERVICE_URI = "/socialservice";

    public List<Message> getMessagesByUser(Date startDate) throws SocialException;

    public List<Message> getSentMessagesByUser(Date startDate) throws SocialException;

    public List<GroupMessage> getGroupMessages(String groupName, Date startDate) throws SocialException;

    public void markMessageAsRead(long id, String receiver) throws SocialException;

    public void removeMessage(long id) throws SocialException;

    public void removeMessageByReceiver(long id) throws SocialException;

    public void removeGroupMessage(long id) throws SocialException;

    public List<User> getUsers() throws SocialException;

    public void sendMessage(String[] recipients, String subject, String message) throws SocialException;

    public void sendMessageWithSupportCopy(String[] recipients, String subject, String message) throws SocialException;

    public void sendMessageToVipSupport(String subject, String message, List<String> workflowID, List<String> simulationNames) throws SocialException;

    public void sendGroupMessage(String groupName, String subject, String message) throws SocialException;

    public int verifyMessages() throws SocialException;

    public static class Util {

        public static SocialServiceAsync getInstance() {

            SocialServiceAsync instance = (SocialServiceAsync) GWT.create(SocialService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
            return instance;
        }
    }
}
