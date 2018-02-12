/**
 * Specから業務フロー図を作成する。
 * 
 * レーンはwhenもしくはandラベルに記述する。
 * アクティビティはテストで呼び出されるオブジェクトのメソッド名が入る。
 * 対象オブジェクトはテストコード内のsut/service/mockの名前で固定。（現状）
 *
 * 実行すると businessflow.puml ができるので、plantumlに食わせる。
 */

def specFile = new File("対象のSpecファイル")

def pumlFile = new File("businessflow.puml")
pumlFile.metaClass.leftShift = { delegate.append(it + '\n') }

pumlFile << "@startuml"

specFile.eachLine {
    def actor = (it =~ /.+(when|and): "(.+)"/)
    if (actor.find()) pumlFile << "|${actor[0][2]}|"

    def activity = (it =~ /.+(sut|service|mock)\.([^()]+)\(/)
    if (activity.find()) pumlFile << ":${activity[0][2]};"
}

pumlFile << "@enduml"

