# STYX

Styx is a web console for Cloud Foundry V2.

# Installation

1. Register Styx as client in the UAA (optional, you can also run Styx as client cf).
2. Update styx.properties in src/main/resources with the base URLs to your Cloud Foundry API and UAA and the client id
   and client secret you used to register Styx (use client id cf and leave client secret empty to run as cf).
3. Create a new war using mvn clean package
4. Push Styx to your Cloud Foundry

# Enable UAA username feature

For some reason the Cloud Foundry API does not return user names for any operation. As a workaround you can enable
an endpoint on the UAA that will return the user names.

In your BOSH deployment descriptor enable the following setting:

scim:
    userids_enabled: true

# Screenshots

![App Spaces](https://raw.github.com/ravanrijn/styx/master/appspaces.png)
![App](https://raw.github.com/ravanrijn/styx/master/app.png)




