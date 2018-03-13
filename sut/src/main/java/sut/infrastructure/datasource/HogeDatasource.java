package sut.infrastructure.datasource;

import org.springframework.stereotype.Repository;
import sut.domain.model.hoge.HogeRepository;
import sut.domain.model.hoge.Hoges;

@Repository
public class HogeDatasource implements HogeRepository {

    HogeMapper mapper;

    @Override
    public Hoges all() {
        return null;
    }
}
