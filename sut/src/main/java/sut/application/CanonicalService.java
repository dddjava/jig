package sut.application;

import sut.domain.model.fuga.Fuga;
import sut.domain.model.fuga.FugaRepository;
import sut.domain.model.hoge.Hoge;
import sut.domain.model.hoge.HogeRepository;

/**
 * 典型的なサービス
 */
public class CanonicalService {

    HogeRepository hogeRepository;
    FugaRepository fugaRepository;

    public Hoge hoge() {
        return hogeRepository.get();
    }

    public Fuga fuga() {
        hogeRepository.get();
        return fugaRepository.get();
    }
}
