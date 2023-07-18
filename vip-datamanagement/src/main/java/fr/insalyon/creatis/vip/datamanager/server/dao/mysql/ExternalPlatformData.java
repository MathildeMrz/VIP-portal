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
package fr.insalyon.creatis.vip.datamanager.server.dao.mysql;

import fr.insalyon.creatis.vip.core.server.dao.DAOException;
import fr.insalyon.creatis.vip.datamanager.client.bean.ExternalPlatform;
import fr.insalyon.creatis.vip.datamanager.server.dao.ExternalPlatformsDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abonnet on 9/5/19.
 */
@Repository
@Transactional
public class ExternalPlatformData extends JdbcDaoSupport implements ExternalPlatformsDAO {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public void useDataSource(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public ExternalPlatform getById(String identifier) throws DAOException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM VIPExternalPlatforms " +
                        "WHERE identifier=?")) {
            ps.setString(1, identifier);
            ResultSet rs = ps.executeQuery();


            if (rs.next()) {
                ExternalPlatform externalPlatform = new ExternalPlatform();
                externalPlatform.setIdentifier(rs.getString("identifier"));
                externalPlatform.setType(
                        getExternalPlatformTypeFromBDDString(rs.getString("type")));
                externalPlatform.setUrl(rs.getString("url"));
                externalPlatform.setDescription(rs.getString("description"));
                externalPlatform.setUploadUrl(rs.getString("upload_url"));
                externalPlatform.setKeycloakClientId(rs.getString("keycloak_client_id"));
                externalPlatform.setRefreshTokenUrl(rs.getString("refresh_token_url"));
                return externalPlatform;
            }

            logger.error("Cannot find external plaform {" + identifier + "}");
            throw new DAOException("Cannot find an external platform");

        } catch (SQLException e) {
            logger.error("Error getting external platform {} ", identifier, e);
            throw new DAOException(e);
        }
    }

    @Override
    public List<ExternalPlatform> getAll() throws DAOException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT * FROM VIPExternalPlatforms ")) {

            ResultSet rs = ps.executeQuery();
            List<ExternalPlatform> externalPlatformsList = new ArrayList<>();

            while (rs.next()) {
                ExternalPlatform externalPlatform = new ExternalPlatform();
                externalPlatform.setIdentifier(rs.getString("identifier"));
                externalPlatform.setType(
                        getExternalPlatformTypeFromBDDString(rs.getString("type")));
                externalPlatform.setUrl(rs.getString("url"));
                externalPlatform.setDescription(rs.getString("description"));
                externalPlatform.setUploadUrl(rs.getString("upload_url"));
                externalPlatform.setKeycloakClientId(rs.getString("keycloak_client_id"));
                externalPlatform.setRefreshTokenUrl(rs.getString("refresh_token_url"));
                externalPlatformsList.add(externalPlatform);
            }

            return externalPlatformsList;

        } catch (SQLException e) {
            logger.error("Error getting all external platforms", e);
            throw new DAOException(e);
        }
    }

    private ExternalPlatform.Type getExternalPlatformTypeFromBDDString(String bddString) throws DAOException {
        try {
            return ExternalPlatform.Type.valueOf(bddString.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.error("External platform type not found {}", bddString, e);
            throw new DAOException(e);
        }
    }

}
