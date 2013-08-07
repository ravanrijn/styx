package com.github.styx.domain;

public class SpaceUser extends User {

    private final boolean developer;

    public SpaceUser(final String id, final String username, final boolean manager, final boolean auditor, final boolean developer) {
        super(id, username, manager, auditor);
        this.developer = developer;
    }

    public boolean isDeveloper() {
        return developer;
    }

    public static final class Builder{

        private final String id;
        private String username;
        private boolean manager;
        private boolean auditor;
        private boolean developer;

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

        public Builder setDeveloperRole(){
            developer = true;
            return this;
        }

        public Builder setAuditorRole(){
            auditor = true;
            return this;
        }

        public SpaceUser build(){
            return new SpaceUser(id, username, manager, auditor, developer);
        }

    }

}
