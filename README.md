# DB based user-storage provider of Keycloak

Require to have following spi configuration in keycloak 

~~~
            <spi name="storage">
                 <provider name="keycloak-ds" enabled="true">
                     <properties>
                         <property name="dsname" value="java:jboss/datasources/KeycloakDS"/>
                     </properties>
                 </provider>
            </spi>

~~~
