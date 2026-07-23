# showcase

## 固定する契約

主要画面が意味のある内容で描画されること。Web の Contract と E2E の入力になる。

## 必要な出力

`JigDocument.canonical()` の各ドキュメントが空表示にならないこと。具体的には、型・パッケージ階層・型間の関連・Javadoc 由来の和名・入力/出力インタフェースの識別が揃っていること。

## 構成

三層＋ドメインモデルで、各ドキュメントが埋まる最小の要素を持つ。

| パッケージ | 役割 | 埋まるドキュメント |
| --- | --- | --- |
| `showcase.presentation` | 入力インタフェース（`@Controller`） | InboundInterface、Usecase |
| `showcase.application` | 業務機能（`@Service`）と出力インタフェース | Usecase、OutboundInterface |
| `showcase.infrastructure` | 出力インタフェースの実装（`@Repository`） | OutboundInterface |
| `showcase.domain.order` `showcase.domain.customer` | ドメインモデル | DomainModel、PackageRelation |

`org.springframework` 配下は実ライブラリを模した最小定義。実ライブラリをテスト依存に加えないための fixture であり、JIG が層を判定するのに必要なアノテーションだけを置く。

Glossary が埋まるよう、型・メソッド・パッケージに Javadoc の和名を付ける。画面の見え方を確認するための入力なので、解析規則ごとの検証は個別の代表プロジェクトへ置き、ここには持ち込まない。
