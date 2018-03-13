package sut.application.service;

import org.springframework.stereotype.Service;
import sut.domain.model.fuga.Fuga;
import sut.domain.model.fuga.FugaIdentifier;
import sut.domain.model.fuga.FugaRepository;
import sut.domain.model.hoge.HogeRepository;
import sut.domain.model.hoge.Hoges;

/**
 * 典型的なサービス
 */
@Service
public class CanonicalService {

    HogeRepository hogeRepository;
    FugaRepository fugaRepository;

    public Hoges allHoges() {
        return hogeRepository.all();
    }

    public Fuga fuga(FugaIdentifier identifier) {
        hogeRepository.all();
        return fugaRepository.get(identifier);
    }
}
