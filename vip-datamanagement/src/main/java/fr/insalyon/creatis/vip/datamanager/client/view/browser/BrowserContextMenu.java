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
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import fr.insalyon.creatis.vip.core.client.view.ModalWindow;
import fr.insalyon.creatis.vip.core.client.view.layout.Layout;
import fr.insalyon.creatis.vip.datamanager.client.DataManagerConstants;
import fr.insalyon.creatis.vip.datamanager.client.bean.Data;
import fr.insalyon.creatis.vip.datamanager.client.rpc.DataManagerService;
import fr.insalyon.creatis.vip.datamanager.client.rpc.DataManagerServiceAsync;
import fr.insalyon.creatis.vip.datamanager.client.view.ValidatorUtil;
import fr.insalyon.creatis.vip.datamanager.client.view.operation.OperationLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Rafael Ferreira da Silva
 */
public class BrowserContextMenu extends Menu {

    private static List<Visualizer> visualizers = new LinkedList<>();

    public BrowserContextMenu(final ModalWindow modal, final String baseDir,
                              final DataRecord data) {

        this.setShowShadow(true);
        this.setShadowDepth(10);
        this.setWidth(90);

        MenuItem uploadItem = new MenuItem("Upload");
        uploadItem.setIcon(DataManagerConstants.ICON_UPLOAD);
        uploadItem.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(MenuItemClickEvent event) {
                if (ValidatorUtil.validateRootPath(baseDir, "upload a file in")
                        && ValidatorUtil.validateUserLevel(baseDir, "upload a file to")) {

                    DataUploadWindow window = new DataUploadWindow(modal, baseDir, "dataManagerUploadComplete");
                    BrowserLayout.getInstance().setDataUploadWindow(window);
                    window.show();
                }
            }
        });

        MenuItem downloadItem = new MenuItem("Download");
        downloadItem.setIcon(DataManagerConstants.ICON_DOWNLOAD);
        downloadItem.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(MenuItemClickEvent event) {
                download(modal, baseDir, data);
            }
        });

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setIcon(CoreConstants.ICON_DELETE);
        deleteItem.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(MenuItemClickEvent event) {
                if (ValidatorUtil.validateRootPath(baseDir, "delete from")
                        && ValidatorUtil.validateUserLevel(baseDir, "delete from")) {

                    delete(modal, baseDir, data.getName());
                }
            }
        });

        MenuItem propertiesItem = new MenuItem("Properties");
        propertiesItem.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(MenuItemClickEvent event) {
                if (baseDir.equals(DataManagerConstants.ROOT)) {
                    Layout.getInstance().setWarningMessage("There are no properties for root folders.");
                } else {
                    new DataPropertiesWindow(baseDir, data).show();
                }
            }
        });


        ArrayList<MenuItem> menuItems = new ArrayList<MenuItem>();
        String fileName = baseDir + "/" + data.getName();
        addVizualisers(menuItems, fileName);
        MenuItemSeparator separator = new MenuItemSeparator();
        menuItems.add(uploadItem);
        menuItems.add(downloadItem);
        menuItems.add(separator);
        menuItems.add(deleteItem);
        menuItems.add(separator);
        menuItems.add(propertiesItem);

        this.setItems(menuItems.toArray(new MenuItem[0]));
    }

    public static void addVizualisers(
            final ArrayList<MenuItem> menuItems,
            final String fileName) {

        boolean hasVisualizers = visualizers.stream()
                .filter(v -> v.isFileSupported(fileName))
                .mapToInt(
                        // Creating the menu item as a side-effect.
                        v -> {
                            MenuItem viewItem = menuItemFor(
                                    fileName, v.fileTypeName(), v.viewStarter());
                            menuItems.add(viewItem);
                            return 1;
                        })
                // Using sum to be sure to consume the whole stream.
                .sum() > 0;

        if (hasVisualizers)
            menuItems.add(new MenuItemSeparator());
    }

    private static MenuItem menuItemFor(
            final String fileName,
            final String fileTypeName,
            final Consumer<String> viewStarter) {

        MenuItem viewItem = new MenuItem("View " + fileTypeName);
        viewItem.setIcon(DataManagerConstants.ICON_VIEW);
        viewItem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(MenuItemClickEvent event) {
                viewStarter.accept(fileName);
            }
        });
        return viewItem;
    }

    public static void addVisualizer(Visualizer v) {
        visualizers.add(v);
    }

    /**
     * @param modal
     * @param baseDir
     * @param name
     */
    private void delete(final ModalWindow modal, final String baseDir, final String name) {

        final DataManagerServiceAsync service = DataManagerService.Util.getInstance();

        if (baseDir.startsWith(DataManagerConstants.ROOT + "/" + DataManagerConstants.TRASH_HOME)) {
            SC.ask("Do you really want to permanently delete \"" + name + "\"?", new BooleanCallback() {

                @Override
                public void execute(Boolean value) {
                    if (value) {
                        final AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                modal.hide();
                                Layout.getInstance().setWarningMessage("Unable to delete file/folder:<br />" + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void result) {
                                modal.hide();
                                Layout.getInstance().setNoticeMessage("The file/folder was successfully scheduled to be permanentely deleted.");
                                BrowserLayout.getInstance().loadData(baseDir, true);
                            }
                        };
                        modal.show("Deleting " + name + "...", true);
                        service.delete(baseDir + "/" + name, callback);
                    }
                }
            });

        } else {
            SC.ask("Do you really want to delete \"" + name + "\"?", new BooleanCallback() {

                @Override
                public void execute(Boolean value) {
                    if (value) {
                        final AsyncCallback<Void> callback = new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                modal.hide();
                                Layout.getInstance().setWarningMessage("Unable to delete file/folder:<br />" + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Void result) {
                                modal.hide();
                                Layout.getInstance().setNoticeMessage("The file/folder was successfully scheduled to be permanentely deleted.");
                                BrowserLayout.getInstance().loadData(baseDir, true);
                            }
                        };
                        modal.show("Deleting " + name + "...", true);
                        service.delete(baseDir + "/" + name, callback);
                    }
                }
            });
        }
    }

    /**
     * @param modal
     * @param baseDir
     * @param data
     */
    private void download(final ModalWindow modal, final String baseDir,
                          final DataRecord data) {

        if (data.getType() == Data.Type.file) {
            DataManagerServiceAsync service = DataManagerService.Util.getInstance();
            AsyncCallback<String> callback = new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                    modal.hide();
                    if (caught.getMessage().contains("No such file or directory")
                            || caught.getMessage().contains("Error while performing:LINKSTAT")) {
                        Layout.getInstance().setWarningMessage("The file " + baseDir + "/" + data.getName() + " is unavailable.");
                        BrowserLayout.getInstance().loadData(baseDir, true);
                    } else {
                        Layout.getInstance().setWarningMessage("Unable to download file:<br />" + caught.getMessage());
                    }
                }

                @Override
                public void onSuccess(String result) {
                    modal.hide();
                    OperationLayout.getInstance().addOperation(result);
                }
            };
            modal.show("Adding file to transfer queue...", true);
            service.downloadFile(baseDir + "/" + data.getName(), callback);

        } else {
            DataManagerServiceAsync service = DataManagerService.Util.getInstance();
            AsyncCallback<String> callback = new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                    modal.hide();
                    if (caught.getMessage().contains("No such file or directory")
                            || caught.getMessage().contains("Error while performing:LINKSTAT")) {
                        Layout.getInstance().setWarningMessage("The folder " + baseDir + "/" + data.getName() + " is unavailable.");
                        BrowserLayout.getInstance().loadData(baseDir, true);
                    } else {
                        Layout.getInstance().setWarningMessage("Unable to download folder:<br />" + caught.getMessage());
                    }
                }

                @Override
                public void onSuccess(String result) {
                    modal.hide();
                    OperationLayout.getInstance().addOperation(result);
                }
            };
            modal.show("Adding folder to transfer queue...", true);
            service.downloadFolder(baseDir + "/" + data.getName(), callback);
        }
    }

    public static interface Visualizer {
        boolean isFileSupported(String filename);

        String fileTypeName();

        Consumer<String> viewStarter();
    }
}
