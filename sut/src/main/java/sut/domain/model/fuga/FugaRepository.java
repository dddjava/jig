package sut.domain.model.fuga;

public interface FugaRepository {

    Fuga get(FugaIdentifier identifier);

    void register(Fuga fuga);
}
