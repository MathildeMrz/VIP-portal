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
package fr.insalyon.creatis.vip.visualization.client.view;

import com.smartgwt.client.widgets.HTMLPane;
import fr.insalyon.creatis.vip.visualization.client.bean.VisualizationItem;

/**
 * @author glatard
 */
public class BrainBrowserViewTab extends AbstractViewTab {

    public static final String ID = "brain_browser_tab";
    private final HTMLPane htmlPane;

    public BrainBrowserViewTab(String lfn) {
        super(lfn);
        htmlPane = new HTMLPane();
        htmlPane.setShowEdges(false);
        htmlPane.setContents("<div id=\"brain-browser\"></div>");
        htmlPane.setWidth100();
        htmlPane.setHeight100();
        this.setID(ID);
        this.getPane().addChild(htmlPane);
    }

    public static boolean isFileSupported(String fileName) {
        return fileName.endsWith(".obj") || fileName.endsWith(".asc");
    }

    public static String fileTypeName() {
        return "surface";
    }

    @Override
    public void displayFile(VisualizationItem item) {
        String url = getFileUrl(item.getLfn());
        if (item.getLfn().endsWith(".asc")) {
            showBrainBrowserForAsc(url);
        } else {
            showBrainBrowserForObj(url);
        }
    }

    public native void showBrainBrowserForObj(String fileUrl) /*-{
           $wnd.$("#brain-browser").load("https://brainbrowser.cbrain.mcgill.ca/surface-viewer-widget?version=2.5.2&model="+fileUrl);
    }-*/;

    public native void showBrainBrowserForAsc(String fileUrl) /*-{
           $wnd.$("#brain-browser").load("https://brainbrowser.cbrain.mcgill.ca/surface-viewer-widget?version=2.5.2&model="+fileUrl+"&format=freesurferasc");
    }-*/;
}
