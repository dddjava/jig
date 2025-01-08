package org.dddjava.jig.domain.model.information.lowmodel.alias;

import org.dddjava.jig.domain.model.data.comment.Comment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    void 空() throws Exception {
        Comment sut = Comment.fromCodeComment("");

        assertAll(
                () -> assertEquals("", sut.summaryText()),
                () -> assertEquals("", sut.bodyText()),
                () -> assertEquals("", sut.fullText()),
                () -> assertFalse(sut.exists())
        );
    }

    @Test
    void 一文() throws Exception {
        Comment sut = Comment.fromCodeComment("なんの変哲もない文章");

        assertAll(
                () -> assertEquals("なんの変哲もない文章", sut.summaryText()),
                () -> assertEquals("", sut.bodyText()),
                () -> assertEquals("なんの変哲もない文章", sut.fullText()),
                () -> assertTrue(sut.exists())
        );
    }

    @Test
    void 句点で終わる一文() throws Exception {
        Comment sut = Comment.fromCodeComment("なんの変哲もない文章。");

        assertAll(
                () -> assertEquals("なんの変哲もない文章", sut.summaryText()),
                () -> assertEquals("", sut.bodyText()),
                () -> assertEquals("なんの変哲もない文章。", sut.fullText()),
                () -> assertTrue(sut.exists())
        );
    }

    @Test
    void 改行で終わる一文() throws Exception {
        Comment sut = Comment.fromCodeComment("なんの変哲もない文章\n");

        assertAll(
                () -> assertEquals("なんの変哲もない文章", sut.summaryText()),
                () -> assertEquals("", sut.bodyText()),
                () -> assertEquals("なんの変哲もない文章\n", sut.fullText()),
                () -> assertTrue(sut.exists())
        );
    }

    @Test
    void 句点つなぎの複数文() throws Exception {
        Comment sut = Comment.fromCodeComment("なんの変哲もない文章。ボディは最初の句点や\n改行の後ろになる。二番目以降は全部。");

        assertAll(
                () -> assertEquals("なんの変哲もない文章", sut.summaryText()),
                () -> assertEquals("ボディは最初の句点や\n改行の後ろになる。二番目以降は全部。", sut.bodyText()),
                () -> assertEquals("なんの変哲もない文章。ボディは最初の句点や\n改行の後ろになる。二番目以降は全部。", sut.fullText()),
                () -> assertTrue(sut.exists())
        );
    }
}