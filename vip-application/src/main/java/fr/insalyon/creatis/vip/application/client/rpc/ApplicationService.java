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
package fr.insalyon.creatis.vip.application.client.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import fr.insalyon.creatis.vip.application.client.bean.*;
import fr.insalyon.creatis.vip.application.client.view.ApplicationException;

import java.util.HashMap;
import java.util.List;

/**
 * @author Rafael Ferreira da Silva
 */
public interface ApplicationService extends RemoteService {

    public static final String SERVICE_URI = "/applicationservice";

    public void signout() throws ApplicationException;

    List<Application> getPublicApplications() throws ApplicationException;

    public List<Application> getApplications() throws ApplicationException;

    public List<String[]> getApplications(String className) throws ApplicationException;

    public List<String[]> getApplicationsByClass(String className) throws ApplicationException;

    public List<AppVersion> getVersions(String applicationName) throws ApplicationException;

    public AppVersion getVersion(String applicationName, String applicationVersion) throws ApplicationException;

    public void add(Application application) throws ApplicationException;

    public void update(Application application) throws ApplicationException;

    public void remove(String name) throws ApplicationException;

    public void addVersion(AppVersion version) throws ApplicationException;

    public void updateVersion(AppVersion version) throws ApplicationException;

    public void removeVersion(String applicationName, String version) throws ApplicationException;

    public String publishVersion(String applicationName, String version) throws ApplicationException;

    public void addClass(AppClass c) throws ApplicationException;

    public void updateClass(AppClass c) throws ApplicationException;

    public void removeClass(String className) throws ApplicationException;

    public List<AppClass> getClasses() throws ApplicationException;

    public List<String>[] getApplicationsAndUsers(List<String> reservedClasses) throws ApplicationException;

    public ApplicationStatus getApplicationStatus() throws ApplicationException;

    public String getCitation(String applicationName) throws ApplicationException;

    public void addEngine(Engine engine) throws ApplicationException;

    public void updateEngine(Engine engine) throws ApplicationException;

    public void removeEngine(String engineName) throws ApplicationException;

    public List<Engine> getEngines() throws ApplicationException;

    public HashMap<String, Integer> getReservedClasses() throws ApplicationException;

    public static class Util {

        public static ApplicationServiceAsync getInstance() {

            ApplicationServiceAsync instance = (ApplicationServiceAsync) GWT.create(ApplicationService.class);
            ServiceDefTarget target = (ServiceDefTarget) instance;
            target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
            return instance;
        }
    }

}
