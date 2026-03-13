// 表示確認用のサンプルデータ
// 実際の実行時にこのファイルは使用しません。
// テストなどにも使用してはいけない。
// TODO このファイルをJava側のテストで書き出したい
globalThis.outputPortData = {
    ports: {
        "com.example.application.HogePort": {
            "fqn": "com.example.application.HogePort",
            "label": "ポート"
        }
    },
    operations: {
        "com.example.application.HogePort#register()": {
            "name": "登録",
            "signature": "com.example.application.HogePort#register()"
        }
    },
    adapters: {
        "com.example.infrastructure.HogeAdapter": {
            "fqn": "com.example.infrastructure.HogeAdapter",
            "label": "アダプター"
        }
    },
    executions: {
        "com.example.infrastructure.HogeAdapter#insert()": {
            "name": "インサート",
            "signature": "com.example.infrastructure.HogeAdapter#insert()"
        }
    },
    persistenceAccessors: {
        "com.example.infrastructure.HogePersistenceAccessor.insert": {
            "id": "com.example.infrastructure.HogePersistenceAccessor.insert",
            "sqlType": "INSERT",
            "targets": ["HOGE_TABLE"],
            "group": "com.example.infrastructure.HogePersistenceAccessor"
        }
    },
    links: [
        {
            "port": "com.example.application.HogePort",
            "operation": "com.example.application.HogePort#register()",
            "adapter": "com.example.infrastructure.HogeAdapter",
            "execution": "com.example.infrastructure.HogeAdapter#insert()",
            "persistenceAccessors": [
                "com.example.infrastructure.HogePersistenceAccessor.insert"
            ]
        },
    ]
}
