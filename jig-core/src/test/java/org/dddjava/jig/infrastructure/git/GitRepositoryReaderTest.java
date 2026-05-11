package org.dddjava.jig.infrastructure.git;

import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePath;
import org.dddjava.jig.domain.model.sources.filesystem.SourceBasePaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitRepositoryReaderTest {

    @Test
    void HEADがref形式でrefs_heads配下に解決される(@TempDir Path root) throws IOException {
        Path gitDir = Files.createDirectory(root.resolve(".git"));
        Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/main\n");
        Path refsHeads = Files.createDirectories(gitDir.resolve("refs/heads"));
        Files.writeString(refsHeads.resolve("main"), "abcdef0123456789abcdef0123456789abcdef01\n");

        GitRepositoryInfo info = GitRepositoryReader.read(sourceBasePaths(root));
        assertEquals("abcdef0", info.shortHash().orElseThrow());
    }

    @Test
    void HEADがref形式でpacked_refsに解決される(@TempDir Path root) throws IOException {
        Path gitDir = Files.createDirectory(root.resolve(".git"));
        Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/main\n");
        Files.writeString(gitDir.resolve("packed-refs"),
                "# pack-refs with: peeled fully-peeled sorted\n" +
                        "1234567abcdef0123456789abcdef0123456789a refs/heads/main\n");

        GitRepositoryInfo info = GitRepositoryReader.read(sourceBasePaths(root));
        assertEquals("1234567", info.shortHash().orElseThrow());
    }

    @Test
    void HEADがdetached形式(@TempDir Path root) throws IOException {
        Path gitDir = Files.createDirectory(root.resolve(".git"));
        Files.writeString(gitDir.resolve("HEAD"), "fedcba9876543210fedcba9876543210fedcba98\n");

        GitRepositoryInfo info = GitRepositoryReader.read(sourceBasePaths(root));
        assertEquals("fedcba9", info.shortHash().orElseThrow());
    }

    @Test
    void gitディレクトリがなければ空(@TempDir Path root) {
        GitRepositoryInfo info = GitRepositoryReader.read(sourceBasePaths(root));
        assertFalse(info.isPresent());
    }

    @Test
    void 親ディレクトリのgitを探索する(@TempDir Path root) throws IOException {
        Path gitDir = Files.createDirectory(root.resolve(".git"));
        Files.writeString(gitDir.resolve("HEAD"), "fedcba9876543210fedcba9876543210fedcba98\n");
        Path nested = Files.createDirectories(root.resolve("a/b/c/classes"));

        GitRepositoryInfo info = GitRepositoryReader.read(sourceBasePaths(nested));
        assertEquals("fedcba9", info.shortHash().orElseThrow());
    }

    @Test
    void remote_origin_url_をHTTPSパースして正規化(@TempDir Path root) throws IOException {
        Path gitDir = Files.createDirectory(root.resolve(".git"));
        Files.writeString(gitDir.resolve("HEAD"), "fedcba9876543210fedcba9876543210fedcba98\n");
        Files.writeString(gitDir.resolve("config"),
                "[core]\n\trepositoryformatversion = 0\n" +
                        "[remote \"origin\"]\n\turl = https://github.com/foo/bar.git\n\tfetch = +refs/heads/*:refs/remotes/origin/*\n");

        GitRepositoryInfo info = GitRepositoryReader.read(sourceBasePaths(root));
        GitRepositoryInfo.RemoteUrl remote = info.remoteUrl().orElseThrow();
        GitRepositoryInfo.KnownHost known = remote.knownHost().orElseThrow();
        assertEquals("https://github.com/foo/bar", known.baseUrl());
        assertEquals("foo/bar", known.displayName());
        assertEquals("https://github.com/foo/bar/commit/fedcba9", remote.commitUrl("fedcba9").orElseThrow());
    }

    @Test
    void remote_origin_url_をSSH形式でパース(@TempDir Path root) throws IOException {
        Path gitDir = Files.createDirectory(root.resolve(".git"));
        Files.writeString(gitDir.resolve("HEAD"), "fedcba9876543210fedcba9876543210fedcba98\n");
        Files.writeString(gitDir.resolve("config"),
                "[remote \"origin\"]\n\turl = git@github.com:foo/bar.git\n");

        GitRepositoryInfo info = GitRepositoryReader.read(sourceBasePaths(root));
        GitRepositoryInfo.KnownHost known = info.remoteUrl().orElseThrow().knownHost().orElseThrow();
        assertEquals("https://github.com/foo/bar", known.baseUrl());
        assertEquals("foo/bar", known.displayName());
    }

    @Test
    void 未知ホストはWeb_URL正規化しない(@TempDir Path root) throws IOException {
        Path gitDir = Files.createDirectory(root.resolve(".git"));
        Files.writeString(gitDir.resolve("HEAD"), "fedcba9876543210fedcba9876543210fedcba98\n");
        Files.writeString(gitDir.resolve("config"),
                "[remote \"origin\"]\n\turl = https://git.example.com/foo/bar.git\n");

        GitRepositoryInfo info = GitRepositoryReader.read(sourceBasePaths(root));
        GitRepositoryInfo.RemoteUrl remote = info.remoteUrl().orElseThrow();
        assertTrue(remote.knownHost().isEmpty());
        assertEquals("https://git.example.com/foo/bar.git", remote.rawUrl());
    }

    private SourceBasePaths sourceBasePaths(Path path) {
        return new SourceBasePaths(
                new SourceBasePath(List.of(path)),
                new SourceBasePath(List.of(path))
        );
    }
}
