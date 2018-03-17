package sut.infrastructure.datasource;

import org.springframework.stereotype.Repository;
import sut.domain.model.hoge.Hoge;
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
        Hoge one = mapper.findOne();
        return new Hoges(Collections.singletonList(one));
    }
}
