package org.springframework.data.repository;

public interface CrudRepository<T, ID> extends Repository<T, ID> {

    <S extends T> S save(S entity);

    T findById(ID id);

    void deleteById(ID id);
}
