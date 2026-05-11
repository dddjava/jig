package org.dddjava.jig.infrastructure.git;

import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 解析対象プロジェクトのgit情報を {@code .git} ディレクトリから直接読み取る。
 * 外部依存（git CLIやJGit）を持たず、best-effortで動作する。
 */
public class GitRepositoryReader {

    private static final Pattern HASH_PATTERN = Pattern.compile("[0-9a-f]{40}");
    private static final Pattern REMOTE_SSH_PATTERN = Pattern.compile("^(?:ssh://)?(?:git@)?([^:/]+)[:/](.+?)(?:\\.git)?/?$");
    private static final Pattern REMOTE_HTTPS_PATTERN = Pattern.compile("^https?://(?:[^@]+@)?([^/]+)/(.+?)(?:\\.git)?/?$");

    private static final List<String> KNOWN_WEB_HOSTS = List.of("github.com", "gitlab.com", "bitbucket.org");

    public static GitRepositoryInfo read(SourceBasePaths sourceBasePaths) {
        Optional<Path> gitDir = findGitDir(sourceBasePaths);
        if (gitDir.isEmpty()) {
            return GitRepositoryInfo.empty();
        }
        Optional<String> shortHash = readShortHash(gitDir.get());
        Optional<GitRepositoryInfo.RemoteUrl> remoteUrl = readRemoteUrl(gitDir.get());
        return new GitRepositoryInfo(shortHash, remoteUrl);
    }

    private static Optional<Path> findGitDir(SourceBasePaths sourceBasePaths) {
        return Stream.concat(
                        sourceBasePaths.classSourceBasePaths().stream(),
                        sourceBasePaths.javaSourceBasePaths().stream())
                .map(GitRepositoryReader::findGitDirFrom)
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static Optional<Path> findGitDirFrom(Path start) {
        try {
            Path current = start.toAbsolutePath().normalize();
            while (current != null) {
                Path candidate = current.resolve(".git");
                if (Files.isDirectory(candidate)) {
                    return Optional.of(candidate);
                }
                // .git がファイル（worktree/submodule）の場合は未対応
                current = current.getParent();
            }
        } catch (RuntimeException ignored) {
        }
        return Optional.empty();
    }

    private static Optional<String> readShortHash(Path gitDir) {
        try {
            Path head = gitDir.resolve("HEAD");
            if (!Files.isRegularFile(head)) return Optional.empty();
            String headContent = Files.readString(head, StandardCharsets.UTF_8).trim();

            String fullHash;
            if (headContent.startsWith("ref:")) {
                String ref = headContent.substring(4).trim();
                Optional<String> resolved = resolveRef(gitDir, ref);
                if (resolved.isEmpty()) return Optional.empty();
                fullHash = resolved.get();
            } else {
                fullHash = headContent;
            }

            if (!HASH_PATTERN.matcher(fullHash).matches()) return Optional.empty();
            return Optional.of(fullHash.substring(0, 7));
        } catch (IOException | RuntimeException e) {
            return Optional.empty();
        }
    }

    private static Optional<String> resolveRef(Path gitDir, String ref) throws IOException {
        Path refFile = gitDir.resolve(ref);
        if (Files.isRegularFile(refFile)) {
            return Optional.of(Files.readString(refFile, StandardCharsets.UTF_8).trim());
        }
        Path packed = gitDir.resolve("packed-refs");
        if (Files.isRegularFile(packed)) {
            for (String line : Files.readAllLines(packed, StandardCharsets.UTF_8)) {
                if (line.startsWith("#") || line.startsWith("^")) continue;
                int sp = line.indexOf(' ');
                if (sp < 0) continue;
                String hash = line.substring(0, sp).trim();
                String refName = line.substring(sp + 1).trim();
                if (refName.equals(ref)) {
                    return Optional.of(hash);
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<GitRepositoryInfo.RemoteUrl> readRemoteUrl(Path gitDir) {
        try {
            Path config = gitDir.resolve("config");
            if (!Files.isRegularFile(config)) return Optional.empty();
            List<String> lines = Files.readAllLines(config, StandardCharsets.UTF_8);
            boolean inOriginSection = false;
            for (String raw : lines) {
                String line = raw.trim();
                if (line.startsWith("[")) {
                    inOriginSection = line.equals("[remote \"origin\"]");
                    continue;
                }
                if (inOriginSection && line.startsWith("url")) {
                    int eq = line.indexOf('=');
                    if (eq < 0) continue;
                    String url = line.substring(eq + 1).trim();
                    if (url.isEmpty()) continue;
                    return Optional.of(normalizeRemoteUrl(url));
                }
            }
        } catch (IOException | RuntimeException ignored) {
        }
        return Optional.empty();
    }

    static GitRepositoryInfo.RemoteUrl normalizeRemoteUrl(String rawUrl) {
        Matcher m = REMOTE_HTTPS_PATTERN.matcher(rawUrl);
        String host = null;
        String path = null;
        if (m.matches()) {
            host = m.group(1);
            path = m.group(2);
        } else {
            m = REMOTE_SSH_PATTERN.matcher(rawUrl);
            if (m.matches()) {
                host = m.group(1);
                path = m.group(2);
            }
        }
        if (host != null && path != null && KNOWN_WEB_HOSTS.contains(host)) {
            String webBase = "https://" + host + "/" + path;
            return new GitRepositoryInfo.RemoteUrl(rawUrl, Optional.of(new GitRepositoryInfo.KnownHost(webBase, path)));
        }
        return new GitRepositoryInfo.RemoteUrl(rawUrl, Optional.empty());
    }
}
