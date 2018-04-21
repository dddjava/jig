package stub.domain.model.type.fuga;

public interface FugaRepository {

    Fuga get(FugaIdentifier identifier);

    void register(Fuga fuga);
}
