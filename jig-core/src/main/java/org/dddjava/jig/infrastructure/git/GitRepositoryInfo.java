package org.dddjava.jig.infrastructure.git;

import java.nio.file.Path;
import java.util.Optional;

/**
 * 解析対象プロジェクトのgit情報
 */
public record GitRepositoryInfo(Optional<Path> repositoryRoot,
                                Optional<String> shortHash,
                                Optional<RemoteUrl> remoteUrl) {

    public static GitRepositoryInfo empty() {
        return new GitRepositoryInfo(Optional.empty(), Optional.empty(), Optional.empty());
    }

    public boolean isPresent() {
        return shortHash.isPresent() || remoteUrl.isPresent();
    }

    /**
     * blob URLのプレフィックス（例: https://github.com/foo/bar/blob/abc1234）。
     * 既知ホストかつshortHashがある場合のみ返す。
     */
    public Optional<String> blobUrlPrefix() {
        return remoteUrl.flatMap(remote -> remote.knownHost()
                .flatMap(known -> shortHash.map(hash -> known.baseUrl() + "/blob/" + hash)));
    }

    /**
     * リモートリポジトリのURL情報。
     *
     * @param rawUrl    生のURL（git config の url 値そのまま）
     * @param knownHost github.com 等の既知ホストにマッチした場合のWeb表示情報
     */
    public record RemoteUrl(String rawUrl, Optional<KnownHost> knownHost) {

        public Optional<String> commitUrl(String hash) {
            return knownHost.map(h -> h.baseUrl() + "/commit/" + hash);
        }
    }

    /**
     * 既知ホスト（GitHub等）でのWeb表示情報。
     */
    public record KnownHost(String baseUrl, String displayName) {
    }
}
