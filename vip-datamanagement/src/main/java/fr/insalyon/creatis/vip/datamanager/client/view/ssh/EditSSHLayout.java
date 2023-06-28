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
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.PickerIcon;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangeEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangeHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemClickHandler;
import com.smartgwt.client.widgets.form.fields.events.FormItemIconClickEvent;
import fr.insalyon.creatis.vip.core.client.CoreModule;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.common.AbstractFormLayout;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;
import fr.insalyon.creatis.vip.core.client.view.util.FieldUtil;
import fr.insalyon.creatis.vip.core.client.view.util.ValidatorUtil;
import fr.insalyon.creatis.vip.core.client.view.util.WidgetUtil;
import fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants;
import fr.insalyon.creatis.vip.datamanager.client.bean.SSH;
import fr.insalyon.creatis.vip.datamanager.client.bean.TransferType;
import fr.insalyon.creatis.vip.datamanager.client.rpc.DataManagerService;
import fr.insalyon.creatis.vip.datamanager.client.view.selection.PathSelectionWindow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author glatard
 * @author Nouha Boujelben
 */
public class EditSSHLayout extends AbstractFormLayout {

    private boolean newSSH = true;
    private TextItem emailField;
    private TextItem nameField;
    private TextItem userField;
    private TextItem hostField;
    private TextItem portField;
    private TextItem directoryField;
    private TextItem statusField;
    private SelectItem transferTypeField;
    private IButton saveButton;
    private IButton removeButton;
    private CheckboxItem deleteFilesFromSourceField;
    private CheckboxItem activateField;
    private CheckboxItem checkFilesContentField;
    PickerIcon browsePicker;
    private TextItem lfcDirItem;

    public EditSSHLayout(String width, String height) {

        super(width, height);
        addTitle("Add/Edit SSH Connection", DataManagerConstants.ICON_SSH);

        configure();
    }

    private void configure() {
        emailField = FieldUtil.getTextItem(350, null);
        nameField = FieldUtil.getTextItem(350, null);
        userField = FieldUtil.getTextItem(350, null);
        hostField = FieldUtil.getTextItem(350, null);
        portField = FieldUtil.getTextItem(350, null);
        directoryField = FieldUtil.getTextItem(350, null);
        statusField = FieldUtil.getTextItem(350, null);
        transferTypeField = new SelectItem();
        transferTypeField.setShowTitle(false);
        transferTypeField.setWidth(350);
        lfcDirItem = FieldUtil.getTextItem(350, false, "", "[0-9.,A-Za-z-+/_(){} ]");
        lfcDirItem.setValue("");
        lfcDirItem.setValidators(ValidatorUtil.getStringValidator());
        browsePicker = new PickerIcon(PickerIcon.SEARCH, new FormItemClickHandler() {

            @Override
            public void onFormItemClick(FormItemIconClickEvent event) {
                new PathSelectionWindow(lfcDirItem).show();
            }
        });
        browsePicker.setPrompt("Browse on the Grid");
        lfcDirItem.setIcons(browsePicker);
        LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
        valueMap.put(TransferType.Synchronization.toString(), TransferType.Synchronization.toString());
        valueMap.put(TransferType.DeviceToLFC.toString(), TransferType.DeviceToLFC.toString());
        valueMap.put(TransferType.LFCToDevice.toString(), TransferType.LFCToDevice.toString());
        transferTypeField.setValueMap(valueMap);
        transferTypeField.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                if (event.getValue().equals(TransferType.Synchronization.toString())) {
                    deleteFilesFromSourceField.setValue(false);
                    deleteFilesFromSourceField.setDisabled(true);
                } else {
                    deleteFilesFromSourceField.setDisabled(false);
                }

            }
        });

        deleteFilesFromSourceField = new CheckboxItem();
        deleteFilesFromSourceField.setTitle("Delete files from Source");
        deleteFilesFromSourceField.setDisabled(true);
        deleteFilesFromSourceField.setWidth(350);

        activateField = new CheckboxItem();
        activateField.setTitle("Activate the SSH connection");
        activateField.setDisabled(false);
        activateField.setWidth(350);

        checkFilesContentField = new CheckboxItem();
        checkFilesContentField.setTitle("Check File Content");
        checkFilesContentField.setDisabled(false);
        checkFilesContentField.setWidth(350);

        saveButton = WidgetUtil.getIButton("Save", CoreConstants.ICON_SAVED,
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (emailField.validate() & nameField.validate() & userField.validate() & hostField.validate() & portField.validate() & directoryField.validate()) {

                            List<String> values = new ArrayList<String>();
                            save(new SSH(emailField.getValueAsString().trim(),
                                            lfcDirItem.getValueAsString().trim(),
                                            userField.getValueAsString().trim(),
                                            hostField.getValueAsString().trim(),
                                            Integer.parseInt(portField.getValueAsString()),
                                            TransferType.valueOf(transferTypeField.getValueAsString()),
                                            directoryField.getValueAsString().trim(),
                                            statusField.getValueAsString(),
                                            checkFilesContentField.getValueAsBoolean(),
                                            deleteFilesFromSourceField.getValueAsBoolean(),
                                            activateField.getValueAsBoolean()
                                    ));
                        }
                    }
                });

        removeButton = WidgetUtil.getIButton("Remove", CoreConstants.ICON_DELETE,
                new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        SC.ask("Do you really want to remove this SSH connection?", new BooleanCallback() {
                            @Override
                            public void execute(Boolean value) {
                                if (value) {
                                    remove(nameField.getValueAsString().trim(), emailField.getValueAsString().trim());
                                }
                            }
                        });
                    }
                });
        removeButton.setDisabled(true);

        addField("VIP User", emailField);
        addField("Connection Name", nameField);
        addField("SSH User", userField);
        addField("SSH Host", hostField);
        addField("SSH Port", portField);
        addField("SSH Directory (absolute path)", directoryField);
        addField("Transfer Type", transferTypeField);
        addField("VIP Directory", lfcDirItem);

        this.addMember(FieldUtil.getForm(checkFilesContentField));
        this.addMember(FieldUtil.getForm(deleteFilesFromSourceField));
        this.addMember(FieldUtil.getForm(activateField));

        addButtons(saveButton, removeButton);
    }

    public void setSSH(String email, String lfcDir, String name, String user, String host, String port, TransferType transferType, String directory, String status, boolean checkFilesContent, boolean deleteFilesFromSourceField, boolean active) {

        if (name != null & email != null & user != null & host != null & transferType != null & directory != null & status != null & port != null) {
            this.emailField.setValue(email);
            this.lfcDirItem.setValue(lfcDir);
            this.lfcDirItem.setDisabled(true);
            this.emailField.setDisabled(true);
            this.nameField.setValue(name);
            this.nameField.setDisabled(true);
            this.userField.setValue(user);
            this.hostField.setValue(host);
            this.portField.setValue(port);
            this.transferTypeField.setValue(transferType);
            this.directoryField.setValue(directory);
            this.statusField.setValue(status);
            this.checkFilesContentField.setValue(checkFilesContent);

            if (transferType.equals(transferType.Synchronization)) {
                this.deleteFilesFromSourceField.setValue(false);
                this.deleteFilesFromSourceField.setDisabled(true);
            } else {
                this.deleteFilesFromSourceField.setDisabled(false);
                this.deleteFilesFromSourceField.setValue(deleteFilesFromSourceField);
            }
            this.activateField.setValue(active);
            this.newSSH = false;
            this.removeButton.setDisabled(false);

        } else {
            this.emailField.setValue(CoreModule.user.getEmail());
            if (!CoreModule.user.isSystemAdministrator()) {
                this.emailField.setDisabled(true);
            } else {
                this.emailField.setDisabled(false);
            }
            this.nameField.setValue("This is an automatically generated field");
            this.nameField.setDisabled(true);
            this.userField.setValue("");
            this.lfcDirItem.setValue("");
            this.lfcDirItem.setDisabled(false);
            this.hostField.setValue("");
            this.portField.setValue("22");
            this.transferTypeField.setValue(transferType.Synchronization);
            this.directoryField.setValue("");
            this.statusField.setValue("");
            this.checkFilesContentField.setValue(false);
            this.deleteFilesFromSourceField.setValue(false);
            this.deleteFilesFromSourceField.setDisabled(true);
            this.activateField.setValue(true);
            this.newSSH = true;
            this.removeButton.setDisabled(true);
        }

    }

    private void save(SSH ssh) {

        WidgetUtil.setLoadingIButton(saveButton, "Saving...");

        if (newSSH) {
            DataManagerService.Util.getInstance().addSSH(ssh, getCallback("add"));
        } else {
            DataManagerService.Util.getInstance().updateSSH(ssh, getCallback("update"));
        }
    }

    private void remove(String name, String email) {

        WidgetUtil.setLoadingIButton(removeButton, "Removing...");
        DataManagerService.Util.getInstance().removeSSH(email, name, getCallback("remove"));
    }

    private AsyncCallback<Void> getCallback(final String text) {

        return new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                WidgetUtil.resetIButton(saveButton, "Save", CoreConstants.ICON_SAVED);
                WidgetUtil.resetIButton(removeButton, "Remove", CoreConstants.ICON_DELETE);
                Layout.getInstance().setWarningMessage("Unable to " + text + " SSH connection:<br />" + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                WidgetUtil.resetIButton(saveButton, "Save", CoreConstants.ICON_SAVED);
                WidgetUtil.resetIButton(removeButton, "Remove", CoreConstants.ICON_DELETE);
                setSSH(null, null, null, null, null, null, null, null, null, false, false, true);
                ManageSSHTab tab = (ManageSSHTab) Layout.getInstance().
                        getTab(DataManagerConstants.TAB_MANAGE_SSH);
                tab.loadSSHConnections();

            }
        };
    }

}
