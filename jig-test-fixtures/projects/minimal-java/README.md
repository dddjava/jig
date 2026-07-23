# minimal-java

## 固定する契約

最小の Java プロジェクトを解析し、サイトを生成できること。

## 必要な出力

`JigDocument.canonical()` の全ドキュメントが生成され、型・パッケージ・型間の関連が空にならないこと。

## 構成

JIG が想定する三層＋ドメインモデルを最小サイズで表す。

| パッケージ | 役割 |
| --- | --- |
| `fixture.minimal.presentation` | 入力インタフェース |
| `fixture.minimal.application` | 業務機能と出力インタフェース |
| `fixture.minimal.domain` | ドメインモデル |

「サイトが生成できる」ことを見るためのものなので、解析規則ごとの検証は個別の代表プロジェクトへ置き、ここには持ち込まない。
