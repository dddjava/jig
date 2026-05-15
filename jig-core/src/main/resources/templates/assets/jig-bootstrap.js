globalThis.Jig ??= {};

globalThis.Jig.bootstrap = (() => {

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
