package com.github.kratos.resources

class User {

    static def cfGetTransform = {result -> [id: result.metadata.guid, roles: result.entity.admin ? ['ADMIN'] : []]}

    static def uaaGetTransform = {result -> [id:result.user_id, username: result.user_name, roles:[]]}

}
