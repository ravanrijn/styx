package com.github.styx.domain;

public class OrganizationUser extends User{

    private final boolean billingManager;

    public OrganizationUser(final String id, final String username, final boolean manager, final boolean auditor, final boolean billingManager) {
        super(id, username, manager, auditor);
        this.billingManager = billingManager;
    }

    public boolean isBillingManager() {
        return billingManager;
    }

    public static final class Builder{

        private final String id;
        private String username;
        private boolean manager;
        private boolean auditor;
        private boolean billingManager;

        private Builder(final String id){
            this.id = id;
        }

        public static Builder newBuilder(final String id){
            return new Builder(id);
        }

        public Builder setUserName(final String userName){
            this.username = userName;
            return this;
        }

        public Builder setManagerRole(){
            manager = true;
            return this;
        }

        public Builder setBillingManager(){
            billingManager = true;
            return this;
        }

        public Builder setAuditorRole(){
            auditor = true;
            return this;
        }

        public OrganizationUser build(){
            return new OrganizationUser(id, username, manager, auditor, billingManager);
        }

    }

}
