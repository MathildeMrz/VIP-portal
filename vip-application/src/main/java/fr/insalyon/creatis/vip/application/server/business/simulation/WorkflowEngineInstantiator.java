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
package fr.insalyon.creatis.vip.application.server.business.simulation;

import fr.insalyon.creatis.vip.application.client.view.monitor.SimulationStatus;
import fr.insalyon.creatis.vip.application.server.business.util.FileUtil;

import java.io.File;
import java.util.List;

/**
 * @author Rafael Ferreira da Silva
 * @author kboulebiar
 */
public abstract class WorkflowEngineInstantiator {

    // content of the xml file that describe the workflow (read on a file) */
    protected String workflow;
    // content of the input for the workflow (generated depending of the user)*/
    protected String input;

    /**
     * @param workflow   workflow file
     * @param parameters list of parameters
     */
    public WorkflowEngineInstantiator(File workflow, List<ParameterSweep> parameters) {

        this.workflow = (workflow != null) ? FileUtil.read(workflow) : null;
        this.input = (parameters != null) ? WorkflowEngineInstantiator.setParametersAsXMLInput(parameters) : null;
    }

    private static String setParametersAsXMLInput(List<ParameterSweep> parameters) {

        //generate the xml input file according to the user input on the GUI
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<inputdata>\n");

        for (ParameterSweep parameter : parameters) {

            xml.append("\t<source name=\"")
                    .append(parameter.getParameterName())
                    .append("\"  type=\"String\">\n")
                    .append("<array>\n");

            int counter = 0;
            for (String value : parameter.getValues()) {


                xml.append("\t\t<item>")
                        .append("<tag name=\"Group\" value=\"")
                        .append(counter)
                        .append("\"/>")
                        .append(value)
                        .append("</item>\n");

                counter++;
            }

            xml.append("</array>\n");
            xml.append("\t</source>\n");
        }

        xml.append("</inputdata>\n");

        return xml.toString();
    }

    public String getSimulationId(String launchID) {

        return launchID.substring(launchID.lastIndexOf("/") + 1, launchID.lastIndexOf("."));
    }

    public abstract String launch(String proxyFileName, String userDN)
            throws
            java.rmi.RemoteException,
            javax.xml.rpc.ServiceException;

    public abstract void kill(String workflowID)
            throws
            java.rmi.RemoteException,
            javax.xml.rpc.ServiceException;

    public abstract SimulationStatus getStatus(String workflowID)
            throws
            java.rmi.RemoteException,
            javax.xml.rpc.ServiceException;

    public String getWorkflow() {

        return workflow;
    }

    public void setWorkflow(File workflow) {
        this.workflow = FileUtil.read(workflow);
    }

    public String getInput() {

        return input;
    }

    public void setInput(List<ParameterSweep> parameters) {

        this.input = WorkflowEngineInstantiator.setParametersAsXMLInput(parameters);
    }
}
