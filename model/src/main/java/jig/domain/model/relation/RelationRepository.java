package jig.domain.model.relation;

public interface RelationRepository {

    void persist(Relation relation);

    Relations all();
}
