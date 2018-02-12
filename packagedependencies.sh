# クラスからパッケージ相関図を作成する。
# 実行すると package-dependencies.png ファイルができる。

# 条件:
# gradleが入っていること
# jdeps(JDK)が入っていること
# plantumlが入っていること
# 対象のクラスが *.domain.model.* パッケージに属していること

gradle clean compileJava
# modelパッケージがどのmodelパッケージに依存しているか
jdeps -dotoutput build/jdepsdot -include ".*.domain.model\..*" -e ".*.domain.model\..*" build/classes/java

# 名前の余計なものを掃除
sed -ie 's/[^"]*.domain.model.//g' build/jdepsdot/java.dot
sed -ie 's/ (java)//g' build/jdepsdot/java.dot

# 1,2行目と最終行を削除
sed -ie '1,2d' build/jdepsdot/java.dot
sed -ie '$d' build/jdepsdot/java.dot

# 末尾のセミコロンを削除
sed -ie 's/";/"/g' build/jdepsdot/java.dot

# . を - に変更（中途半端にパッケージになったりするので）
sed -ie 's/\./-/g' build/jdepsdot/java.dot
# -> を ..> に変更
sed -ie 's/->/..>/g' build/jdepsdot/java.dot

# pumlファイルを作成
PUMLFILE=build/package-dependencies.puml
echo "@startuml">$PUMLFILE
echo "title パッケージ依存関係">>$PUMLFILE
echo "hide members">>$PUMLFILE
echo "hide circle">>$PUMLFILE
cat build/jdepsdot/java.dot>>$PUMLFILE
echo "@enduml">>$PUMLFILE

# plantumlに食わせる
plantuml $PUMLFILE

# pngを移動
mv build/package-dependencies.png ./
