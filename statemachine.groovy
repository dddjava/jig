/**
 * enumから状態遷移図を作成する。
 * 状態とイベントをenumを定義、イベントが発生することで状態が遷移する。
 * 状態enumに制約はない。
 * イベントenumは状態enum型のallowStates/nextStatusesフィールドが必要。
 *
 * 実行すると statemachine.puml ができるので、plantumlに食わせる。
 */

// 状態enumのクラス
def state = 
// イベントenumのクラス（複数可）
def events = []

new PrintStream("statemachine.puml").withCloseable {
    System.setOut(it)

    println "@startuml"
    state.values().each {
        println "state ${it}"
    }

    events*.values()*.each { event ->
        [event.allowStatuses, event.nextStatuses].eachCombination {
            println "${it[0]} --> ${it[1]}: ${event}"
        }
    }
    println "@enduml"
}
