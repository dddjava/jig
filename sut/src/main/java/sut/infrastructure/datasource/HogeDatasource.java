package sut.infrastructure.datasource;

import org.springframework.stereotype.Repository;
import sut.domain.model.hoge.HogeRepository;
import sut.domain.model.hoge.Hoges;

import java.util.Collections;

@Repository
public class HogeDatasource implements HogeRepository {

    HogeMapper mapper;

    public HogeDatasource(HogeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Hoges all() {
        return new Hoges(Collections.emptyList());
    }
}
