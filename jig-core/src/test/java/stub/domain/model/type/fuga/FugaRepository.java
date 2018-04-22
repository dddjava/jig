package stub.domain.model.type.fuga;

/**
 * リポジトリ和名
 */
public interface FugaRepository {

    Fuga get(FugaIdentifier identifier);

    void register(Fuga fuga);
}
