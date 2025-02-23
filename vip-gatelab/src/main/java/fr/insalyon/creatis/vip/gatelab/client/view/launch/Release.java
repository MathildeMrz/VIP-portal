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
package fr.insalyon.creatis.vip.gatelab.client.view.launch;

/**
 * @author glatard
 */
public class Release implements Comparable<Release> {

    private String releaseName;

    public Release(String name) {
        releaseName = name;
    }

    public String getReleaseName() {
        return releaseName;
    }

    @Override
    public int compareTo(Release o) {
        ReleaseNumber _this = getReleaseNumber();
        ReleaseNumber _it = o.getReleaseNumber();

        if (_this.major > _it.major)
            return -1;
        if (_it.major > _this.major)
            return 1;
        //both major releases are equal
        if (_this.minor > _it.minor)
            return -1;
        if (_it.minor > _this.minor)
            return 1;
        //major and minor numbers are equal
        if (_this.bug > _it.bug)
            return -1;
        if (_it.bug > _this.bug)
            return 1;
        return 0;
    }

    public ReleaseNumber getReleaseNumber() {

        String[] s = releaseName.split("[0-9].*(.[0-9])+");

        String rName = releaseName;

        ReleaseNumber rn = new ReleaseNumber();
        try {

            for (String remove : s)
                rName = rName.replace(remove, "");

            String[] result = rName.split("\\.");

            switch (result.length) {
                case 3:
                    rn.bug = Integer.parseInt(result[2]);
                case 2:
                    rn.minor = Integer.parseInt(result[1]);
                case 1:
                    rn.major = Integer.parseInt(result[0]);
                default:
            }
        } catch (NumberFormatException e) {
        }

        return rn;
    }

    private class ReleaseNumber {
        public int major;
        public int minor;
        public int bug;

        public ReleaseNumber() {
            major = 0;
            minor = 0;
            bug = 0;
        }
    }

}
