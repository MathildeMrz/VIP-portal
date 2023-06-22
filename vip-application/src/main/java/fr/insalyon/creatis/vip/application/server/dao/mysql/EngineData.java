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
package fr.insalyon.creatis.vip.application.server.dao.mysql;

import fr.insalyon.creatis.vip.application.client.bean.Engine;
import fr.insalyon.creatis.vip.application.server.dao.EngineDAO;
import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

/**
 *
 * @author Rafael Ferreira da Silva
 */

@Repository
@Transactional
public class EngineData extends JdbcDaoSupport implements EngineDAO {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public void useDataSource(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public void add(Engine engine) throws DAOException
    {
        try
        {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO VIPEngines(name, endpoint, status) "
                            + "VALUES (?, ?, ?)");

            ps.setString(1, engine.getName());
            ps.setString(2, engine.getEndpoint());
            ps.setString(3, engine.getStatus());
            ps.execute();
            ps.close();

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Unique index or primary key")) {
                logger.error("There is already an engine registered with the name {}", engine.getName());
                 throw new DAOException("There is already an engine registered with the name : " + engine.getName());
            } else {
                logger.error("Error adding engine {}", engine.getEndpoint(), ex);
                throw new DAOException(ex);
            }
        }
    }

    @Override
    public void update(Engine engine) throws DAOException {
        try {
            if(isEngine(engine.getName()))// TODO : voir si sql renvoie erreur comme quoi
            {
                PreparedStatement ps = getConnection().prepareStatement(
                        "UPDATE VIPEngines SET endpoint = ?, "
                                + "status = ? "
                                + "WHERE name = ?");

                ps.setString(1, engine.getEndpoint());
                ps.setString(2, engine.getStatus());
                ps.setString(3, engine.getName());
                //ps.executeUpdate();

                if(ps.executeUpdate() == 0)
                {
                    logger.error("There is no engine registered with the name {}", engine.getName());
                    throw new DAOException("There is no engine registered with the name : " + engine.getName());
                }

                ps.close();
            }

        } catch (SQLException ex) {
            logger.error("Error updating engine {} to {}", engine.getName(), engine.getEndpoint(), ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public void remove(String name) throws DAOException
    {
            try {
                PreparedStatement ps = getConnection().prepareStatement("DELETE "
                        + "FROM VIPEngines WHERE name=?");

                ps.setString(1, name);
                ps.execute();


                System.out.println("IIIIIIIIIIII : "+ps.executeUpdate());
                if(ps.executeUpdate() == 0)
                {
                    logger.error("There is no engine registered with the name {}", name);
                    throw new DAOException("There is no engine registered with the name : " + name);
                }

                ps.close();

            } catch (SQLException ex) {
                logger.error("Error removing engine {}", name, ex);
                throw new DAOException(ex);
            }

    }

    @Override
    public List<Engine> get() throws DAOException {

        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT "
                    + "name, endpoint, status FROM VIPEngines "
                    + "ORDER BY name");

            ResultSet rs = ps.executeQuery();
            List<Engine> list = new ArrayList<Engine>();

            while (rs.next()) {
                list.add(new Engine(rs.getString("name"), rs.getString("endpoint"), rs.getString("status")));
            }
            return list;

        } catch (SQLException ex) {
            logger.error("Error getting all engines", ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public List<Engine> getByClass(String className) throws DAOException {

        String status= "enabled";
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT "
                    + "e.name AS engineName, endpoint, status "
                    + "FROM VIPEngines e, VIPClassesEngines c "
                    + "WHERE e.name = c.engine AND "
                    + "e.status = ? AND "
                    + "c.class = ?");
            ps.setString(1, status);
            ps.setString(2, className);

            ResultSet rs = ps.executeQuery();

            List<Engine> list = new ArrayList<Engine>();
            while (rs.next()) {
                list.add(new Engine(rs.getString("engineName"), rs.getString("endpoint"), rs.getString("status")));
            }
            ps.close();
            return list;

        } catch (SQLException ex) {
            logger.error("Error getting engines by class {}", className, ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public boolean isEngine(String name)
    {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT *"
                    + "FROM VIPEngines "
                    + "WHERE name=?");

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
