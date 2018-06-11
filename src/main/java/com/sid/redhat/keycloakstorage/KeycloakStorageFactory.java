/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sid.redhat.keycloakstorage;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 *
 * @author sidde
 */
public class KeycloakStorageFactory implements UserStorageProviderFactory<KeycloakStorage>{

    protected KeycloakSession session;
    protected String dsname;
    protected ComponentModel model;

//    public KeycloakStorageFactory(KeycloakSession session, ComponentModel model, String dsname) {
//        this.session = session;
//        this.model = model;
//        this.dsname = dsname;
//    }
    
    @Override
    public void init(Config.Scope config){
         dsname = config.get("dsname");
         System.out.println("Datasource configured as " + dsname);
    }
    @Override
    public KeycloakStorage create(KeycloakSession ks, ComponentModel cm) {
        return new KeycloakStorage(ks, cm, dsname);
    }

    @Override
    public String getId() {
        return "keycloak-ds-storage"; 
    }
}
