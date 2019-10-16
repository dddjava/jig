package stub.application.service;

import org.springframework.stereotype.Service;
import stub.domain.model.type.HogeRepository;
import stub.domain.model.type.fuga.Fuga;
import stub.domain.model.type.fuga.FugaIdentifier;
import stub.domain.model.type.fuga.FugaRepository;

/**
 * サービス別名
 * 2行目や
 * 3行目は出力しない
 */
@Service
public class CanonicalService {

    HogeRepository hogeRepository;
    FugaRepository fugaRepository;

    public CanonicalService(HogeRepository hogeRepository, FugaRepository fugaRepository) {
        this.hogeRepository = hogeRepository;
        this.fugaRepository = fugaRepository;
    }

    public Fuga fuga(FugaIdentifier identifier) {
        hogeRepository.method();
        return fugaRepository.get(identifier);
    }

    void method() {
    }
}
