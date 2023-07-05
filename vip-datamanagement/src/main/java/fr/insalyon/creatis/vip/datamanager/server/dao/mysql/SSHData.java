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
import fr.insalyon.creatis.vip.datamanager.client.bean.SSH;
import fr.insalyon.creatis.vip.datamanager.client.bean.TransferType;
import fr.insalyon.creatis.vip.datamanager.server.DataManagerUtil;
import fr.insalyon.creatis.vip.datamanager.server.dao.SSHDAO;
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
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author glatard, Nouha Boujelben
 */
@Repository
@Transactional
public class SSHData extends JdbcDaoSupport implements SSHDAO {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public void useDataSource(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public List<SSH> getSSHConnections() throws DAOException {
        try {
            PreparedStatement ps = getConnection().prepareStatement("SELECT "
                    + "* FROM "
                    + "VIPSSHAccounts");

            ResultSet rs = ps.executeQuery();
            List<SSH> ssh = new ArrayList<SSH>();

            while (rs.next()) {

                String email = rs.getString("email");
                String name = DataManagerUtil.extractName(rs.getString("LFCDir"));
                String lfcDir = rs.getString("LFCDir");
                String sshUser = rs.getString("sshUser");
                String sshHost = rs.getString("sshHost");
                TransferType sshTransferType = TransferType.valueOf(rs.getString("transferType"));
                String sshDir = rs.getString("sshDir");
                int sshPort = rs.getInt("sshPort");
                boolean validated = rs.getBoolean("validated");
                boolean auth_failed = rs.getBoolean("auth_failed");
                Timestamp theEarliestNextSynchronistation = rs.getTimestamp("theEarliestNextSynchronistation");
                long numberSynchronizationFailed = rs.getLong("numberSynchronizationFailed");
                boolean deleteFilesFromSource = rs.getBoolean("deleteFilesFromSource");
                boolean active = rs.getBoolean("active");
                boolean checkFilesContent = rs.getBoolean("checkFilesContent");

                String lfcFiles = "+" + String.valueOf(rs.getInt("numberOfFilesTransferredToLFC")) + " (" + readableUnitFileSize(rs.getLong("sizeOfFilesTransferredToLFC")) + ") "
                        + "-" + String.valueOf(rs.getInt("numberOfFilesDeletedInLFC")) + " (" + readableUnitFileSize(rs.getLong("sizeOfFilesDeletedInLFC")) + ")";

                String sshFiles = "+" + String.valueOf(rs.getInt("numberOfFilesTransferredToDevice")) + " (" + readableUnitFileSize(rs.getLong("sizeOfFilesTransferredToDevice")) + ") "
                        + "-" + String.valueOf(rs.getInt("numberOfFilesDeletedInDevice")) + " (" + readableUnitFileSize(rs.getLong("sizeOfFilesDeletedInDevice")) + ")";

                String status = "ok";
                if (auth_failed) {
                    status = "authentication failed";
                }
                if (!validated) {
                    status = "waiting for validation";
                }

                ssh.add(new SSH(email, lfcDir, name, sshUser, sshHost, sshPort, sshTransferType, sshDir, status, theEarliestNextSynchronistation, numberSynchronizationFailed, checkFilesContent, deleteFilesFromSource, active, sshFiles, lfcFiles));
            }
            ps.close();
            return ssh;

        } catch (SQLException ex) {
            logger.error("Error getting ssh connections", ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public void addSSH(SSH ssh) throws DAOException {
        try {
            PreparedStatement ps = getConnection().prepareStatement(
                    "INSERT INTO VIPSSHAccounts(email,LFCDir,sshUser,sshHost,transferType,sshDir,sshPort,validated,auth_failed,checkFilesContent,numberSynchronizationFailed,deleteFilesFromSource) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");

            ps.setString(1, ssh.getEmail());
            ps.setString(2, ssh.getLfcDir());
            ps.setString(3, ssh.getUser());
            ps.setString(4, ssh.getHost());
            ps.setString(5, ssh.getTransferType().name());
            ps.setString(6, ssh.getDirectory());
            ps.setInt(7, ssh.getPort());
            ps.setString(8, "1");
            ps.setString(9, "0");
            ps.setBoolean(10, ssh.isCheckFilesContent());
            ps.setLong(11, 0);
            ps.setBoolean(12, ssh.isDeleteFilesFromSource());
            ps.execute();
            ps.close();

        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate entry")) {
                logger.error("An ssh connection with this LFC Directory \"" + ssh.getLfcDir() + "\" already exists for user " + ssh.getEmail() + ".");
                throw new DAOException("An ssh connection with this LFC Directory \"" + ssh.getLfcDir() + "\" already exists for user " + ssh.getEmail() + ".", ex);
            } else {
                logger.error("Error adding ssh connection {}", ssh.getLfcDir(), ex);
                throw new DAOException(ex);
            }
        }
    }

    @Override
    public void updateSSH(SSH ssh) throws DAOException {
        try {
            PreparedStatement ps = getConnection().prepareStatement("UPDATE "
                    + "VIPSSHAccounts "
                    + "SET sshUser=?, sshHost=?, transferType=?, sshDir=?, sshPort=?, checkFilesContent=?, deleteFilesFromSource=?, active=? "
                    + "WHERE email=? AND LFCDir=?");
            ps.setString(1, ssh.getUser());
            ps.setString(2, ssh.getHost());
            ps.setString(3, ssh.getTransferType().toString());
            ps.setString(4, ssh.getDirectory());
            ps.setInt(5, ssh.getPort());
            ps.setBoolean(6, ssh.isCheckFilesContent());
            ps.setBoolean(7, ssh.isDeleteFilesFromSource());
            ps.setBoolean(8, ssh.isActive());
            ps.setString(9, ssh.getEmail());
            ps.setString(10, ssh.getLfcDir());

            ps.executeUpdate();
            ps.close();

        } catch (SQLException ex) {
            logger.error("Error updating ssh connection {}", ssh.getLfcDir(), ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public void removeSSH(String email, String lfcDir) throws DAOException {
        try {
            PreparedStatement ps = getConnection().prepareStatement("DELETE "
                    + "FROM VIPSSHAccounts WHERE email=? AND LFCDir=?");

            ps.setString(1, email);
            ps.setString(2, lfcDir);
            logger.info("Removing connection " + email + " " + lfcDir);
            ps.execute();
            ps.close();

        } catch (SQLException ex) {
            logger.error("Error removing ssh connection {}", lfcDir, ex);
            throw new DAOException(ex);
        }
    }

    @Override
    public void resetSSHConnections(List<List<String>> sshConnections) throws DAOException {

        for (List<String> sshC : sshConnections) {
            try {
                PreparedStatement ps = getConnection().prepareStatement("UPDATE "
                        + "VIPSSHAccounts "
                        + "SET auth_failed='1', numberSynchronizationFailed='0', theEarliestNextSynchronistation=? "
                        + "WHERE email=? AND LFCDir=?");
                ps.setTimestamp(1, new Timestamp(Calendar.getInstance().getTime().getTime()));
                ps.setString(2, sshC.get(0));
                ps.setString(3, sshC.get(1));
                logger.info("Reset connection " + sshC.get(0) + " " + sshC.get(1));

                ps.execute();
                ps.close();

            } catch (SQLException ex) {
                logger.error("Error reseting ssh connections {}", sshC, ex);
                throw new DAOException(ex);
            }

        }
    }

    public static String readableUnitFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
