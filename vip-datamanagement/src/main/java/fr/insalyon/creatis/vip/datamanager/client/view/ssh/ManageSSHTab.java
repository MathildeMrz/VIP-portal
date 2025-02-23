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
package fr.insalyon.creatis.vip.datamanager.client.view.ssh;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import fr.insalyon.creatis.vip.core.client.view.common.AbstractManageTab;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;
import fr.insalyon.creatis.vip.core.client.view.util.WidgetUtil;
import fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants;
import fr.insalyon.creatis.vip.datamanager.client.bean.TransferType;
import fr.insalyon.creatis.vip.datamanager.client.rpc.DataManagerService;

/**
 * @author glatard,
 * @author Nouha Boujelben
 */
public class ManageSSHTab extends AbstractManageTab {

    private SSHLayout sshLayout;
    private EditSSHLayout editLayout;

    public ManageSSHTab() {

        super(DataManagerConstants.ICON_SSH, DataManagerConstants.APP_SSH, DataManagerConstants.TAB_MANAGE_SSH);
        sshLayout = new SSHLayout();
        editLayout = new EditSSHLayout("25%", "100%");

        loadKey();

        HLayout sshLayout = new HLayout(5);
        sshLayout.setHeight100();
        sshLayout.setWidth100();
        sshLayout.addMember(this.sshLayout);
        sshLayout.addMember(editLayout);
        this.vLayout.addMember(sshLayout);
    }

    private void loadKey() {
        DataManagerService.Util.getInstance().getSSHPublicKey(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                Layout.getInstance().setWarningMessage("Cannot get VIP SSH public key:<br />" + caught.getMessage());
            }

            @Override
            public void onSuccess(String result) {

                editLayout.addMember(WidgetUtil.getLabel("<b>VIP's public ssh key (add it to user@host)", 50));
                Label l = WidgetUtil.getLabel(result, 20);
                l.setCanSelectText(true);
                l.setWidth(350);
                l.setOverflow(Overflow.CLIP_H);
                editLayout.addMember(l);

            }
        });
    }

    public void loadSSHConnections() {
        sshLayout.loadData();
    }

    public void setSSH(String name, String lfcDir, String email, String user, String host, String port, TransferType transferType, String directory, String status, boolean checkFilesContent, boolean deleteFilesFromSource, boolean activate) {
        editLayout.setSSH(email, lfcDir, name, user, host, port, transferType, directory, status, checkFilesContent, deleteFilesFromSource, activate);
    }

}
