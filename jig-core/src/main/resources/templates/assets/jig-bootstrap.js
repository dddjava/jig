globalThis.Jig ??= {};

globalThis.Jig.bootstrap = (() => {

    /**
     * 指定の body クラスが付いたページでのみ DOMContentLoaded 後に init を実行する。
     * Node.js テスト環境（document 未定義）では何もしない。
     *
     * @param {string} bodyClass - 対象ページの body 要素に付いているクラス名
     * @param {() => void} initFn - 初期化関数
     */
    function register(bodyClass, initFn) {
        if (typeof document === "undefined") return;
        document.addEventListener("DOMContentLoaded", () => {
            if (document.body.classList.contains(bodyClass)) initFn();
        });
    }

    return {register};
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.bootstrap;
}
