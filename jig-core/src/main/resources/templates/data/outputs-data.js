// 表示確認用のサンプルデータ
// 実際の実行時にこのファイルは使用しません。
// テストなどにも使用してはいけない。
// TODO このファイルをJava側のテストで書き出したい
globalThis.outputPortData = {
    "outputPorts": [
        {
            "fqn": "com.example.application.HogePort",
            "label": "ポート",
            "operations": [
                {
                    "fqn": "com.example.application.HogePort#register()",
                    "label": "登録",
                    "signature": "register()"
                }
            ]
        }
    ],
    "outputAdapters": [
        {
            "fqn": "com.example.infrastructure.HogeAdapter",
            "label": "アダプター",
            "executions": [
                {
                    "fqn": "com.example.infrastructure.HogeAdapter#insert()",
                    "label": "インサート",
                    "signature": "insert()"
                }
            ]
        }
    ],
    "persistenceAccessors": [
        {
            "id": "com.example.infrastructure.HogePersistenceAccessor.insert",
            "sqlType": "INSERT",
            "group": "com.example.infrastructure.HogePersistenceAccessor",
            "groupLabel": "HogePersistenceAccessor",
            "targets": ["HOGE_TABLE"]
        }
    ],
    "targets": ["HOGE_TABLE"],
    "links": {
        "operationToExecution": [
            {
                "operation": "com.example.application.HogePort#register()",
                "execution": "com.example.infrastructure.HogeAdapter#insert()"
            }
        ],
        "executionToAccessor": [
            {
                "execution": "com.example.infrastructure.HogeAdapter#insert()",
                "accessor": "com.example.infrastructure.HogePersistenceAccessor.insert"
            }
        ]
    }
}
