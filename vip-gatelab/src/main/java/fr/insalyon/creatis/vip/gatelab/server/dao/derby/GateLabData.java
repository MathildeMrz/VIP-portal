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
package fr.insalyon.creatis.vip.gatelab.server.dao.derby;

import fr.insalyon.creatis.vip.application.server.dao.h2.AbstractJobData;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.gatelab.server.dao.GateLabDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Each GateLabData is specific to a single database, and so to a single simulation.
 * So a new instance is needed at each call and this needs the prototype scope
 * <p>
 * The h2 connection is configured in AbstractJobData
 *
 * @author Ibrahim Kallel, Rafael Silva
 */
@Component
@Scope("prototype")
public class GateLabData extends AbstractJobData implements GateLabDAO {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public GateLabData(String dbPath) throws DAOException {

        super(dbPath);
    }

    /**
     * @return
     * @throws DAOException
     */
    public long getNumberParticles() throws DAOException {

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT somme FROM somme ");
            ResultSet rs = ps.executeQuery();
            boolean hasNext = rs.next();

            if (!hasNext) {
                // no row in somme table, no job is running yet
                ps.close();
                return 0;
            }

            long sum = rs.getLong("somme");

            ps.close();
            return sum;

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Table \"SOMME\" not found")) {
                // table does not exist, there is a problem in the h2 server or in the workflow
                // silence it for the user as it is not blocking
                logger.warn("Table Somme does not exist for {}", getDbPath(), ex);
                return 0;
            }
            logger.error("Error fetching simulation particle number for {}", getDbPath(), ex);
            throw new DAOException(ex);
        } finally {
            close(logger);
        }
    }

    /**
     * @throws DAOException
     */
    public void StopWorkflowSimulation() throws DAOException {

        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE somme SET simulation = 'true' ");
            ps.execute();
            ps.close();

        } catch (SQLException ex) {
            logger.error("Error stopping a workflow", ex);
            throw new DAOException(ex);
        }

    }
}
