/* Copyright CNRS-CREATIS
 *
 * Rafael Silva
 * rafael.silva@creatis.insa-lyon.fr
 * http://www.rafaelsilva.com
 *
 * This software is a grid-enabled data-driven workflow manager and editor.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
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
 * knowledge of the CeCILL license and that you accept its terms.
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
import fr.insalyon.creatis.vip.common.client.view.Context;
import fr.insalyon.creatis.vip.common.client.view.modal.ModalWindow;
import fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants;
import fr.insalyon.creatis.vip.datamanager.client.bean.Data;
import fr.insalyon.creatis.vip.datamanager.client.rpc.FileCatalogService;
import fr.insalyon.creatis.vip.datamanager.client.rpc.FileCatalogServiceAsync;
import fr.insalyon.creatis.vip.datamanager.client.rpc.TransferPoolService;
import fr.insalyon.creatis.vip.datamanager.client.rpc.TransferPoolServiceAsync;
import fr.insalyon.creatis.vip.datamanager.client.view.common.BasicBrowserToolStrip;
import fr.insalyon.creatis.vip.datamanager.client.view.operation.OperationLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rafael Silva
 */
public class BrowserToolStrip extends BasicBrowserToolStrip {

    private BasicBrowserToolStrip toolStrip;

    public BrowserToolStrip(final ModalWindow modal, final ListGrid grid) {

        super(modal);
        this.toolStrip = this;

        ToolStripButton folderUpButton = new ToolStripButton();
        folderUpButton.setIcon("icon-folderup.png");
        folderUpButton.setPrompt("Folder up");
        folderUpButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                if (!pathItem.getValueAsString().equals(DataManagerConstants.ROOT)) {
                    String newPath = pathItem.getValueAsString();
                    BrowserLayout.getInstance().loadData(
                            newPath.substring(0, newPath.lastIndexOf("/")), false);
                }
            }
        });
        this.addButton(folderUpButton);
        ToolStripButton refreshButton = new ToolStripButton();
        refreshButton.setIcon("icon-refresh.png");
        refreshButton.setPrompt("Refresh");
        refreshButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                BrowserLayout.getInstance().loadData(pathItem.getValueAsString(), true);
            }
        });
        this.addButton(refreshButton);

        ToolStripButton homeButton = new ToolStripButton();
        homeButton.setIcon("icon-home.png");
        homeButton.setPrompt("Home");
        homeButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                BrowserLayout.getInstance().loadData(DataManagerConstants.ROOT, false);
            }
        });
        this.addButton(homeButton);

        ToolStripButton addFolderButton = new ToolStripButton();
        addFolderButton.setIcon("icon-addfolder.png");
        addFolderButton.setPrompt("Create Folder");
        addFolderButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                String path = pathItem.getValueAsString();
                if (path.equals(DataManagerConstants.ROOT)) {
                    SC.warn("You cannot create a folder in the root folder.");
                } else {
                    new AddFolderWindow(modal, path, grid, toolStrip).show();
                }
            }
        });
        this.addButton(addFolderButton);

        this.addSeparator();
        ToolStripButton uploadButton = new ToolStripButton();
        uploadButton.setIcon("icon-upload.png");
        uploadButton.setPrompt("Upload");
        uploadButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                String path = pathItem.getValueAsString();
                if (path.equals(DataManagerConstants.ROOT)) {
                    SC.warn("You cannot upload a file in the root folder.");
                } else {
                    DataUploadWindow window = new DataUploadWindow(modal, path);
                    BrowserLayout.getInstance().setDataUploadWindow(window);
                    window.show();
                }
            }
        });
        this.addButton(uploadButton);

        ToolStripButton downloadButton = new ToolStripButton();
        downloadButton.setIcon("icon-download.png");
        downloadButton.setPrompt("Download Selected Files");
        downloadButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                download();
            }
        });
        this.addButton(downloadButton);

        ToolStripButton deleteButton = new ToolStripButton();
        deleteButton.setIcon("icon-delete.png");
        deleteButton.setPrompt("Delete Selected Files/Folders");
        deleteButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                String path = pathItem.getValueAsString();
                if (path.equals(DataManagerConstants.ROOT)) {
                    SC.warn("You cannot delete a root folder.");
                } else {
                    delete();
                }
            }
        });
        this.addButton(deleteButton);

        this.addSeparator();
        ToolStripButton trashButton = new ToolStripButton();
        trashButton.setIcon("icon-trash.png");
        trashButton.setPrompt("Trash");
        trashButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                BrowserLayout.getInstance().loadData(DataManagerConstants.ROOT
                        + "/" + DataManagerConstants.TRASH_HOME, false);
            }
        });
        this.addButton(trashButton);

        ToolStripButton emptyTrashButton = new ToolStripButton();
        emptyTrashButton.setIcon("icon-trash-empty.png");
        emptyTrashButton.setPrompt("Empty Trash");
        emptyTrashButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                emptyTrash();
            }
        });
        this.addButton(emptyTrashButton);
    }

    private void download() {
        ListGridRecord[] records = BrowserLayout.getInstance().getGridSelection();

        for (ListGridRecord record : records) {
            DataRecord data = (DataRecord) record;
            if (data.getType().contains("file")) {
                TransferPoolServiceAsync service = TransferPoolService.Util.getInstance();
                AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                    public void onFailure(Throwable caught) {
                        modal.hide();
                        SC.warn("Unable to download file: " + caught.getMessage());
                    }

                    public void onSuccess(Void result) {
                        modal.hide();
                        OperationLayout.getInstance().loadData();
                        OperationLayout.getInstance().activateAutoRefresh();
                    }
                };
                modal.show("Adding files to transfer queue...", true);
                Context context = Context.getInstance();
                service.downloadFile(
                        context.getUser(),
                        pathItem.getValueAsString() + "/" + data.getName(),
                        context.getUserDN(), context.getProxyFileName(),
                        callback);
            } else {
                TransferPoolServiceAsync service = TransferPoolService.Util.getInstance();
                AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                    public void onFailure(Throwable caught) {
                        modal.hide();
                        SC.warn("Unable to download folder: " + caught.getMessage());
                    }

                    public void onSuccess(Void result) {
                        modal.hide();
                        OperationLayout.getInstance().loadData();
                        OperationLayout.getInstance().activateAutoRefresh();
                    }
                };
                modal.show("Adding folder to transfer queue...", true);
                Context context = Context.getInstance();
                service.downloadFolder(
                        context.getUser(),
                        pathItem.getValueAsString() + "/" + data.getName(),
                        context.getUserDN(), context.getProxyFileName(),
                        callback);
            }
        }
    }

    private void delete() {
        ListGridRecord[] records = BrowserLayout.getInstance().getGridSelection();
        final Map<String, String> paths = new HashMap<String, String>();

        for (ListGridRecord record : records) {
            DataRecord data = (DataRecord) record;
            paths.put(pathItem.getValueAsString() + "/" + data.getName(),
                    DataManagerConstants.ROOT + "/"
                    + DataManagerConstants.TRASH_HOME + "/" + data.getName());
        }
        SC.confirm("Do you really want to delete the selected files/folders?", new BooleanCallback() {

            public void execute(Boolean value) {
                if (value != null && value) {
                    FileCatalogServiceAsync service = FileCatalogService.Util.getInstance();
                    AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                        public void onFailure(Throwable caught) {
                            modal.hide();
                            SC.warn("Error executing delete files/folders: " + caught.getMessage());
                        }

                        public void onSuccess(Void result) {
                            modal.hide();
                            BrowserLayout.getInstance().loadData(pathItem.getValueAsString(), true);
                        }
                    };
                    modal.show("Deleting files/folders...", true);
                    Context context = Context.getInstance();
                    service.renameFiles(context.getUser(), context.getProxyFileName(),
                            paths, callback);
                }
            }
        });
    }

    private void emptyTrash() {
        SC.confirm("Do you really want to remove the items in the trash permanently?", new BooleanCallback() {

            public void execute(Boolean value) {
                if (value != null && value) {
                    final FileCatalogServiceAsync service = FileCatalogService.Util.getInstance();
                    AsyncCallback<List<Data>> callback = new AsyncCallback<List<Data>>() {

                        public void onFailure(Throwable caught) {
                            modal.hide();
                            SC.warn("Error executing delete files/folders: " + caught.getMessage());
                        }

                        public void onSuccess(List<Data> result) {

                            List<String> paths = new ArrayList<String>();
                            for (Data data : result) {
                                paths.add(DataManagerConstants.ROOT + "/"
                                        + DataManagerConstants.TRASH_HOME
                                        + "/" + data.getName());
                            }
                            AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                                public void onFailure(Throwable caught) {
                                    modal.hide();
                                    SC.warn("Error executing empty trash: " + caught.getMessage());
                                }

                                public void onSuccess(Void result) {
                                    modal.hide();
                                    BrowserLayout.getInstance().loadData(
                                            DataManagerConstants.ROOT + "/"
                                            + DataManagerConstants.TRASH_HOME, true);
                                }
                            };
                            Context context = Context.getInstance();
                            service.deleteFiles(context.getUser(), context.getProxyFileName(),
                                    paths, callback);
                        }
                    };
                    modal.show("Emptying Trash...", true);
                    Context context = Context.getInstance();
                    service.listDir(context.getUser(), context.getProxyFileName(),
                            DataManagerConstants.ROOT + "/" + DataManagerConstants.TRASH_HOME,
                            true, callback);
                }
            }
        });
    }
}
