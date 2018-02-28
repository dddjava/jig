package jig.model.relation;

public interface RelationRepository {

    void persist(Relation relation);

    Relations all();
}
