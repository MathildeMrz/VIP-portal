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
package fr.insalyon.creatis.vip.datamanager.client.view.browser;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.ModalWindow;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;
import fr.insalyon.creatis.vip.core.client.view.util.WidgetUtil;
import fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants;
import fr.insalyon.creatis.vip.datamanager.client.bean.Data;
import fr.insalyon.creatis.vip.datamanager.client.rpc.DataManagerService;
import fr.insalyon.creatis.vip.datamanager.client.rpc.DataManagerServiceAsync;
import fr.insalyon.creatis.vip.datamanager.client.view.ValidatorUtil;
import fr.insalyon.creatis.vip.datamanager.client.view.common.BasicBrowserToolStrip;
import fr.insalyon.creatis.vip.datamanager.client.view.operation.OperationLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rafael Ferreira da Silva
 */
public class BrowserToolStrip extends BasicBrowserToolStrip {

    private ToolStripButton pasteButton;

    public BrowserToolStrip(final ModalWindow modal, final ListGrid grid) {

        super(modal, grid);

        // Upload a File Button
        this.addSeparator();
        this.addButton(WidgetUtil.getToolStripButton(
                DataManagerConstants.ICON_UPLOAD, "Upload a File", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        String path = pathItem.getValueAsString();
                        if (ValidatorUtil.validateRootPath(path, "upload a file in")
                                && ValidatorUtil.validateUserLevel(path, "upload a file to")) {

                            new FileUploadWindow(modal, path, "dataManagerUploadComplete").show();
                        }
                    }
                }));

        // Upload Multiple Data Button
        this.addButton(WidgetUtil.getToolStripButton(
                DataManagerConstants.ICON_UPLOAD_MULTIPLE, "Upload Multiple Data", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        String path = pathItem.getValueAsString();
                        if (ValidatorUtil.validateRootPath(path, "upload data in")
                                && ValidatorUtil.validateUserLevel(path, "upload data to")) {

                            DataUploadWindow window = new DataUploadWindow(modal, path, "dataManagerUploadComplete");
                            BrowserLayout.getInstance().setDataUploadWindow(window);
                            window.show();
                        }
                    }
                }));

        // Download Button
        this.addButton(WidgetUtil.getToolStripButton(
                DataManagerConstants.ICON_DOWNLOAD, "Download Selected Data", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        download();
                    }
                }));

        // Delete Button
        this.addButton(WidgetUtil.getToolStripButton(
                CoreConstants.ICON_DELETE, "Delete Selected Files/Folders", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        String path = pathItem.getValueAsString();
                        if (ValidatorUtil.validateRootPath(path, "delete from")
                                && ValidatorUtil.validateUserLevel(path, "delete from")) {

                            delete();
                        }
                    }
                }));
    }

    private void download() {
        ListGridRecord[] records = BrowserLayout.getInstance().getGridSelection();

        for (ListGridRecord record : records) {
            DataRecord data = (DataRecord) record;

            if (data.getType() == Data.Type.file) {
                DataManagerServiceAsync service = DataManagerService.Util.getInstance();
                AsyncCallback<String> callback = new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        modal.hide();
                        Layout.getInstance().setWarningMessage("Unable to download file:<br />" + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        modal.hide();
                        OperationLayout.getInstance().addOperation(result);
                    }
                };
                modal.show("Adding file to transfer queue...", true);
                service.downloadFile(
                        pathItem.getValueAsString() + "/" + data.getName(),
                        callback);

            } else {
                DataManagerServiceAsync service = DataManagerService.Util.getInstance();
                AsyncCallback<String> callback = new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        modal.hide();
                        Layout.getInstance().setWarningMessage("Unable to download folder: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        modal.hide();
                        OperationLayout.getInstance().addOperation(result);
                    }
                };
                modal.show("Adding folder to transfer queue...", true);
                service.downloadFolder(
                        pathItem.getValueAsString() + "/" + data.getName(),
                        callback);
            }
        }
    }

    private void delete() {

        final List<String> paths = new ArrayList<String>();
        final String baseDir = pathItem.getValueAsString();
        final DataManagerServiceAsync service = DataManagerService.Util.getInstance();

        if (baseDir.startsWith(DataManagerConstants.ROOT + "/" + DataManagerConstants.TRASH_HOME)) {
            SC.ask("Do you really want to permanently delete the selected files/folders?", new BooleanCallback() {
                @Override
                public void execute(Boolean value) {
                    if (value) {

                        for (ListGridRecord record : BrowserLayout.getInstance().getGridSelection()) {
                            DataRecord data = (DataRecord) record;
                            paths.add(baseDir + "/" + data.getName());
                        }

                        final AsyncCallback<Void> callback = new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                modal.hide();
                                Layout.getInstance().setWarningMessage("Unable to delete files/folders:<br />" + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void result) {
                                modal.hide();
                                Layout.getInstance().setNoticeMessage("The selected files/folders were successfully scheduled to be permanentely deleted.");
                                BrowserLayout.getInstance().loadData(baseDir, true);
                            }
                        };
                        modal.show("Deleting files/folders...", true);
                        service.delete(paths, callback);
                    }
                }
            });

        } else {
            SC.ask("Do you really want to delete the selected files/folders?", new BooleanCallback() {
                @Override
                public void execute(Boolean value) {
                    if (value) {

                        for (ListGridRecord record : BrowserLayout.getInstance().getGridSelection()) {
                            DataRecord data = (DataRecord) record;
                            if (data.getType().equals(Data.Type.folderSync) || data.getType().equals(Data.Type.fileSync)) {
                                Layout.getInstance().setWarningMessage("could not delete a synchronized files/folders <br />");
                            } else {
                                paths.add(baseDir + "/" + data.getName());
                            }
                        }
                        if (!paths.isEmpty()) {
                            final AsyncCallback<Void> callback = new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    modal.hide();
                                    Layout.getInstance().setWarningMessage("Unable to delete files/folders:<br />" + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    modal.hide();
                                    Layout.getInstance().setNoticeMessage("The selected files/folders were successfully scheduled to be permanentely deleted.");
                                    BrowserLayout.getInstance().loadData(baseDir, true);
                                }
                            };
                            // trash desactivated : raw delete
                            modal.show("Deleting files/folders...", true);
                            service.delete(paths, callback);
                        }
                    }
                }
            });
        }
    }
}
