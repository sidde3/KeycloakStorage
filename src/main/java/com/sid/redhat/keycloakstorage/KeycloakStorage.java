/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sid.redhat.keycloakstorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;
/**
 *
 * @author sidde
 */
public class KeycloakStorage implements UserStorageProvider, UserLookupProvider, CredentialInputValidator, CredentialInputUpdater {

    protected Map<String, UserModel> loadedUsers = new HashMap<>();
    protected KeycloakSession session;
    protected String dsname;
    protected ComponentModel model;
    protected String principalsQuery = "select password from keycloak_user where user_name=?";

    KeycloakStorage(KeycloakSession session, ComponentModel model, String dsname) {
        this.session = session;
        this.model = model;
        this.dsname = dsname;
    }

    protected UserModel createAdapter(RealmModel realm, String username) {
        return new AbstractUserAdapter(session, realm, model) {
            @Override
            public String getUsername() {
                return username;
            }
        };
    }

    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(username, realm);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        UserModel adapter = loadedUsers.get(username);
        if (adapter == null) {
            String password = getUsersPassword(username); //Need to implement logic here
            if (password != null) {
                adapter = createAdapter(realm, username);
                loadedUsers.put(username, adapter);
            }
        }
        return adapter;
    }

    @Override
    public UserModel getUserByEmail(String string, RealmModel realm) {
        return null;
    }

    // CredentialInputValidator methods
    @Override
    public boolean supportsCredentialType(String credentialType) {
        return credentialType.equals(CredentialModel.PASSWORD);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        String password = getUsersPassword(user.getUsername());  //Need to implement logic
        return credentialType.equals(CredentialModel.PASSWORD) && password != null;
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }

        UserCredentialModel cred = (UserCredentialModel) input;
        String password = getUsersPassword(user.getUsername()); //Need to implement logic
        if (password == null) {
            return false;
        }
        return password.equals(cred.getValue());
    }

    // CredentialInputUpdater methods
    @Override
    public boolean updateCredential(RealmModel realm, UserModel user, CredentialInput input) {

        if (input.getType().equals(CredentialModel.PASSWORD)) {
            throw new ReadOnlyException("user is read only for this update");
        }

        return false;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {

    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
        return Collections.EMPTY_SET;
    }

    @Override
    public void close() {
        System.out.println("Closing...");
    }

    //Get password through DataSource 
    protected String getUsersPassword(String username) {
        System.out.println(username);
        System.out.println(dsname);
        String password = null;
        Connection conn = null;
        try {
            InitialContext ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(dsname);
            conn = ds.getConnection();
            PreparedStatement ps = conn.prepareStatement(principalsQuery);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next() == false) {
                System.out.println("No username found");
            }
            password = rs.getString(1);
            conn.close();
        } catch (NamingException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return password;
    }

}
