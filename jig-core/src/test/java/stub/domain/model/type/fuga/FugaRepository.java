package stub.domain.model.type.fuga;

/**
 * リポジトリ和名。句点以降はサマリから除外する。
 */
public interface FugaRepository {

    Fuga get(FugaIdentifier identifier);

    void register(Fuga fuga);
}
