globalThis.domainData = {
    // サンプルデータ
    "packages": [
    {
        "id": "com",
        "label": "com",
        "fqn": "com",
        "description": "",
        "parent": null
    },
    {
        "id": "com.example",
        "label": "example",
        "fqn": "com.example",
        "description": "example root package",
        "parent": "com"
    },
    {
        "id": "com.example.service",
        "label": "service",
        "fqn": "com.example.service",
        "description": "service layer",
        "parent": "com.example"
    },
    {
        "id": "com.example.repository",
        "label": "repository",
        "fqn": "com.example.repository",
        "description": "repository layer",
        "parent": "com.example"
    }
],
    "classes": [
    {
        "id": "com.example.service.UserService",
        "label": "UserService",
        "fqn": "com.example.service.UserService",
        "description": "user operations",
        "package": "com.example.service",
        "fields": [
            {
                "label": "repository",
                "name": "repository",
                "description": "user repository",
                "type": "com.example.repository.UserRepository"
            }
        ],
        "methods": [
            {
                "label": "findUser",
                "name": "findUser",
                "description": "find user",
                "returnType": "com.example.domain.User",
                "parameters": [
                    {
                        "name": "id",
                        "type": "java.lang.String"
                    }
                ]
            }
        ]
    },
    {
        "id": "com.example.repository.UserRepository",
        "label": "UserRepository",
        "fqn": "com.example.repository.UserRepository",
        "description": "user persistence",
        "package": "com.example.repository",
        "fields": [],
        "methods": [
            {
                "label": "findById",
                "name": "findById",
                "description": "load user",
                "returnType": "com.example.domain.User",
                "parameters": [
                    {
                        "name": "id",
                        "type": "java.lang.String"
                    }
                ]
            }
        ]
    }
]
}
