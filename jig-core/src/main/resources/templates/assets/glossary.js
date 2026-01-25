function updateArticleVisibility() {
    const showEmptyDescription = document.getElementById("show-empty-description").checked;
    const kindVisibilityMap = {
        "パッケージ": document.getElementById("show-package").checked,
        "クラス": document.getElementById("show-class").checked,
        "メソッド": document.getElementById("show-method").checked,
        "フィールド": document.getElementById("show-field").checked,
    };

    const searchKeyword = document.getElementById("search-input").value.toLowerCase();
    const termArticles = document.getElementsByClassName("term");

    Array.from(termArticles).forEach(term => {
        const kindText = term.getElementsByClassName("kind")[0]?.textContent || "";

        // 種類で絞り込む
        if (!kindVisibilityMap[kindText]) {
            term.classList.add("hidden");
            return;
        }

        // 以降の判定で使用する説明文を取得
        const description = term.getElementsByClassName("description")[0]?.textContent?.toLowerCase() || "";

        // 説明文有無での判定
        if (!showEmptyDescription && !description) {
            term.classList.add("hidden");
            return;
        }
        // 検索キーワードでの判定（タイトルと説明文）
        const title = term.getElementsByClassName("term-title")[0]?.textContent?.toLowerCase() || "";
        if (searchKeyword && !title.includes(searchKeyword) && !description.includes(searchKeyword)) {
            term.classList.add("hidden");
            return;
        }

        // すべての条件をパスした場合に表示
        term.classList.remove("hidden");
    });

    // 表示が変わるのでnavも更新する
    updateLetterNavigationVisibility();
}

function updateLetterNavigationVisibility() {
    const letterNavigations = Array.from(document.getElementsByClassName("letter-navigation"));
    const showNavigation = document.getElementById("show-letter-navigation").checked;
    if (!showNavigation) {
        letterNavigations.forEach(nav => nav.classList.add("invisible"));
        return;
    }

    letterNavigations.forEach((nav, index) => {
        // 1件目は無視
        if (index === 0) {
            nav.classList.remove("invisible");
            return;
        }

        let visibleCount = 0;
        let sibling = nav.previousElementSibling;
        while (sibling) {
            // 表示しているものだけ対象にする
            if (!sibling.classList.contains("invisible")) {
                // letter-navigationはカウント対象外
                if (sibling.classList.contains("letter-navigation")) break;
                visibleCount++;
                // これ以上カウントする意味がないので抜ける
                if (visibleCount >= 10) break;
            }
            sibling = sibling.previousElementSibling;
        }

        // 10個以上表示するものがあったら表示する
        if (visibleCount >= 10) {
            nav.classList.remove("invisible");
        } else {
            nav.classList.add("invisible");
        }
    });
}

document.addEventListener("DOMContentLoaded", function () {
    if (!document.body.classList.contains("glossary")) return;

    document.getElementById("search-input").addEventListener("input", updateArticleVisibility);
    document.getElementById("show-empty-description").addEventListener("change", updateArticleVisibility);
    document.getElementById("show-package").addEventListener("change", updateArticleVisibility);
    document.getElementById("show-class").addEventListener("change", updateArticleVisibility);
    document.getElementById("show-method").addEventListener("change", updateArticleVisibility);
    document.getElementById("show-field").addEventListener("change", updateArticleVisibility);
    document.getElementById("show-letter-navigation").addEventListener("change", updateLetterNavigationVisibility);

    updateLetterNavigationVisibility();
});
